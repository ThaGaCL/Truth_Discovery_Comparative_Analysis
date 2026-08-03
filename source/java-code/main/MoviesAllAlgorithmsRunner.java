package main;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import qcri.dafna.dataModel.data.DataSet;
import qcri.dafna.dataModel.data.Globals;
import qcri.dafna.dataModel.data.SourceClaim;
import qcri.dafna.dataModel.quality.dataQuality.DataItemMeasures;
import qcri.dafna.dataModel.quality.dataQuality.DataQualityMeasurments;
import qcri.dafna.dataModel.quality.dataQuality.DataSetTimingMeasures;
import qcri.dafna.dataModel.quality.voterResults.VoterQualityMeasures;
import qcri.dafna.voter.GuessLCA;
import qcri.dafna.voter.SimpleLCA;
import qcri.dafna.voter.ThreeEstimate;
import qcri.dafna.voter.TruthFinder;
import qcri.dafna.voter.TwoEstimates;
import qcri.dafna.voter.Voter;
import qcri.dafna.voter.Voting;
import qcri.dafna.voter.accuModels.AccuModelBase;
import qcri.dafna.voter.latentTruthModel.LatentTruthModel;

/**
 * Executes all available truth discovery voters for the movies dataset.
 *
 * Input files expected:
 * - DAFNAData/formatted/movies/claims/omdb.txt
 * - DAFNAData/formatted/movies/claims/wikidata.txt
 * - DAFNAData/formatted/movies/truth/movies-truth.txt
 */
public class MoviesAllAlgorithmsRunner {

    private static final Path CLAIMS_OMDB = Paths.get("DAFNAData/formatted/movies/claims/omdb.txt");
    private static final Path CLAIMS_WIKIDATA = Paths.get("DAFNAData/formatted/movies/claims/wikidata.txt");
    private static final Path TRUTH_FILE = Paths.get("DAFNAData/formatted/movies/truth/movies-truth.txt");

    private interface VoterFactory {
        Voter create(DataSet dataSet);
    }

    private static final class VoterSpec {
        final String name;
        final VoterFactory factory;

        VoterSpec(String name, VoterFactory factory) {
            this.name = name;
            this.factory = factory;
        }
    }

    public static void main(String[] args) throws Exception {
        boolean convergence100 = false;

        List<VoterSpec> voters = new ArrayList<VoterSpec>();
        voters.add(new VoterSpec("Voting", ds -> new Voting(ds)));
        voters.add(new VoterSpec("TruthFinder", ds -> new TruthFinder(ds, 0.5, 0.5, 0.1, 0.001)));
        voters.add(new VoterSpec("TwoEstimates", ds -> new TwoEstimates(ds, 0.5)));
        voters.add(new VoterSpec("ThreeEstimate", ds -> new ThreeEstimate(ds, 100, 0.5)));
        voters.add(new VoterSpec("SimpleLCA", ds -> new SimpleLCA(ds, 0.001)));
        voters.add(new VoterSpec("GuessLCA", ds -> new GuessLCA(ds, 0.001)));

        // AccuCopy variants: TT, TF, FT, FF
        voters.add(new VoterSpec("AccuCopy_TT", ds -> new AccuModelBase(ds, 0.5, 0.5, 100, 0.001, 0.8, 0.8, true, true, true, false)));
        voters.add(new VoterSpec("AccuCopy_TF", ds -> new AccuModelBase(ds, 0.5, 0.5, 100, 0.001, 0.8, 0.8, true, false, true, false)));
        voters.add(new VoterSpec("AccuCopy_FT", ds -> new AccuModelBase(ds, 0.5, 0.5, 100, 0.001, 0.8, 0.8, false, true, true, false)));
        voters.add(new VoterSpec("AccuCopy_FF", ds -> new AccuModelBase(ds, 0.5, 0.5, 100, 0.001, 0.8, 0.8, false, false, true, false)));

        voters.add(new VoterSpec("LTM", ds -> new LatentTruthModel(10.0, 10.0, 0.9, 0.1, 0.1, 0.9, ds, 500, 100, 9)));

        System.out.println("algorithm,tp,fp,fn,tn,precision,recall,accuracy,specificity,f1,iterations,duration_ms");

        for (VoterSpec spec : voters) {
            try {
                DataSet dataSet = loadMoviesDataSetWithTruth();
                Voter voter = spec.factory.create(dataSet);
                VoterQualityMeasures m = voter.launchVoter(convergence100);
                double precision = m.getPrecision();
                double recall = m.getRecall();
                double f1 = (precision + recall) == 0.0 ? 0.0 : (2.0 * precision * recall) / (precision + recall);

                System.out.println(spec.name
                        + "," + m.getTruePositive()
                        + "," + m.getFalsePositive()
                        + "," + m.getFalseNegative()
                        + "," + m.getTrueNegative()
                        + "," + fmt(m.getPrecision())
                        + "," + fmt(m.getRecall())
                        + "," + fmt(m.getAccuracy())
                        + "," + fmt(m.getSpecificity())
                        + "," + fmt(f1)
                        + "," + m.getNumberOfIterations()
                        + "," + m.getTimings().getVoterDuration());
            } catch (Throwable t) {
                String reason = t.getClass().getSimpleName();
                if (t.getMessage() != null && !t.getMessage().isEmpty()) {
                    reason += ": " + t.getMessage().replace(',', ';');
                }
                System.out.println(spec.name + ",SKIPPED," + reason);
            }
        }
    }

    private static DataSet loadMoviesDataSetWithTruth() throws IOException {
        DataSet dataSet = new DataSet(Globals.starting_Confidence, Globals.starting_trustworthiness);
        dataSet.setENCODING(StandardCharsets.UTF_8);

        int claims = 0;
        claims += readClaimsFile(CLAIMS_OMDB, dataSet);
        claims += readClaimsFile(CLAIMS_WIKIDATA, dataSet);

        dataSet.computeValueBuckets(false);

        DataQualityMeasurments quality = new DataQualityMeasurments(dataSet);
        quality.setTimingMeasures(new DataSetTimingMeasures());
        dataSet.setDataQualityMeasurments(quality);

        int truthCount = applyTruth(TRUTH_FILE, quality);
        quality.setGoldStandardTrueValueCount(truthCount);
        quality.computeDataQaulityMeasures(Globals.tolerance_Factor);

        if (claims == 0 || truthCount == 0) {
            throw new IllegalStateException("Movies dataset is empty or truth could not be loaded.");
        }

        return dataSet;
    }

    private static int readClaimsFile(Path file, DataSet dataSet) throws IOException {
        int count = 0;
        try (BufferedReader br = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                String[] p = line.split("\\t", -1);
                if (p.length < 6) {
                    continue;
                }

                int claimId;
                try {
                    claimId = Integer.parseInt(p[0].trim());
                } catch (NumberFormatException e) {
                    continue;
                }

                String objectId = p[1].trim();
                String property = p[2].trim();
                String value = p[3].trim();
                String source = p[4].trim();
                String timestamp = p[5].trim();

                if (objectId.isEmpty() || property.isEmpty() || value.isEmpty() || source.isEmpty()) {
                    continue;
                }

                dataSet.addClaim(claimId, objectId, "", property, value, 0.0, timestamp, source);
                count++;
            }
        }
        return count;
    }

    private static int applyTruth(Path truthFile, DataQualityMeasurments quality) throws IOException {
        int count = 0;
        try (BufferedReader br = Files.newBufferedReader(truthFile, StandardCharsets.UTF_8)) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                String[] p = line.split("\\t", -1);
                if (p.length < 3) {
                    continue;
                }

                String objectId = p[0].trim();
                String property = p[1].trim();
                String value = p[2].trim();
                if (objectId.isEmpty() || property.isEmpty() || value.isEmpty()) {
                    continue;
                }

                String key = SourceClaim.dataItemKey(objectId, property);
                DataItemMeasures dim = quality.getDataItemMeasures().get(key);
                if (dim != null) {
                    dim.setTrueValue(value);
                    dim.setTrueValueCleaned(false);
                    count++;
                }
            }
        }
        return count;
    }

    private static String fmt(double d) {
        return String.format(Locale.US, "%.4f", d);
    }
}
