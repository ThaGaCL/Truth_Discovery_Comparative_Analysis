package qcri.dafna.dataModel.dataSet.dataSetFormatter;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import qcri.dafna.dataModel.data.Globals;

/**
 * Reader for movies dataset claims and truth files.
 * Processes already formatted CSV/TSV files from the OMDB and Wikidata sources.
 */
public class MovieDataSetReadOldWriteNew {
	
	private int claimId = Globals.lastClaimID;
	private int numberOfClaims = 0;
	private final String timestamp = "2026-07-28";
	
	public void readMoviesClaimsAndWriteTruthFiles(String newFileDelimiter) {
		Globals.log("Start reading the Movies Claims Files");
		
		try {
			// Read claims from formatted files
			readClaimsFile(Globals.directory_formattedDAFNADataset_MoviesClaimsFolder + "/omdb.txt", "omdb");
			readClaimsFile(Globals.directory_formattedDAFNADataset_MoviesClaimsFolder + "/wikidata.txt", "wikidata");
			
			// Process truth file
			readTruthFile(Globals.directory_formattedDAFNADataset_MoviesTruthFolder + "/movies-truth.txt");
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		Globals.log("Movies dataset processing completed.");
		Globals.log("Total claims processed: " + numberOfClaims);
		Globals.log("Last Claim ID = " + claimId);
	}
	
	private void readClaimsFile(String filePath, String sourceId) throws IOException {
		Globals.log("Reading claims file: " + filePath + " from source: " + sourceId);
		
		try (BufferedReader reader = new BufferedReader(
				Files.newBufferedReader(Paths.get(filePath), StandardCharsets.UTF_8))) {
			
			String line;
			int lineCount = 0;
			
			while ((line = reader.readLine()) != null) {
				lineCount++;
				
				// Parse line: claimId\tobjectId\tproperty\tvalue\tsourceId\ttimestamp\t
				// The generated files use TAB as the real field separator.
				String[] parts = line.split("\t");
				if (parts.length < 5) {
					continue;
				}
				
				String objectId = parts[1].trim();
				String propertyName = parts[2].trim();
				String propertyValue = parts[3].trim();
				
				// Validation
				if (objectId.isEmpty() || propertyName.isEmpty() || propertyValue.isEmpty()) {
					continue;
				}
				
				numberOfClaims++;
				
				if (lineCount % 1000 == 0) {
					Globals.log("  Processed " + lineCount + " claims from " + sourceId);
				}
			}
			
			Globals.log("Finished reading " + lineCount + " claims from " + sourceId);
		}
	}
	
	private void readTruthFile(String filePath) throws IOException {
		Globals.log("Reading truth file: " + filePath);
		
		try (BufferedReader reader = new BufferedReader(
				Files.newBufferedReader(Paths.get(filePath), StandardCharsets.UTF_8))) {
			
			String line;
			int lineCount = 0;
			
			while ((line = reader.readLine()) != null) {
				lineCount++;
				
				// Parse line: objectId \t property \t value
				String[] parts = line.split("\t");
				if (parts.length < 3) {
					continue;
				}
				
				String objectId = parts[0].trim();
				String propertyName = parts[1].trim();
				String propertyValue = parts[2].trim();
				
				// Validation
				if (objectId.isEmpty() || propertyName.isEmpty() || propertyValue.isEmpty()) {
					continue;
				}
				
				if (lineCount % 1000 == 0) {
					Globals.log("  Processed " + lineCount + " truth values");
				}
			}
			
			Globals.log("Finished reading " + lineCount + " truth values");
		}
	}
}
