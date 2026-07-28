package main;

import qcri.dafna.dataModel.data.Globals;
import qcri.dafna.dataModel.dataSet.dataSetFormatter.BiographyDataSetReadOldWriteNew;
import qcri.dafna.dataModel.dataSet.dataSetFormatter.OldFlightDataSetReader;
import qcri.dafna.dataModel.dataSet.dataSetFormatter.PopulationBiographyDataSetGenerator;
import qcri.dafna.dataModel.dataSet.dataSetFormatter.PopulationDatasetReadOldWriteNew;
import qcri.dafna.dataModel.dataSet.dataSetFormatter.WeatherReadOldWriteNew;
import qcri.dafna.dataModel.dataSet.dataSetFormatter.MovieDataSetReadOldWriteNew;

public class MainClass {
	
	public static void main(String[] args) {
//		for (int i = 0 ; i < 50; i++) {
//			double c = 	(Math.exp(i/ (/*sources.size()*/50-1)) - 1) 
////					/
////					(Math.E - 1)
//				;
//			int cov = (int)(
//					((double)1000/*dataItems.size()*/-1) *
//						(Math.exp(7*i/ (/*sources.size()*/50-1)) - 1) /
//						(Math.exp(7) - 1)
//					);
//			cov++;
//			System.out.println(cov);
//		}

		String mode = args.length == 0 ? "weather" : args[0].trim().toLowerCase();
		if (mode.equals("weather")) {
			readOldWeatherFilesWriteNewFiles();
		} else if (mode.equals("population")) {
			readOldPopulationFilesWriteNewFiles();
		} else if (mode.equals("population-biography")) {
			readOldPopulationAndBiographyFilesWriteNewFiles();
		} else if (mode.equals("biography")) {
			readOldBiographyFilesWriteNewFiles();
		} else if (mode.equals("flight")) {
			readOldFlightFilesWriteNewFiles();
		} else if (mode.equals("movies")) {
			readMoviesFilesWriteNewFiles();
		} else {
			System.out.println("Usage: java -cp build/classes main.MainClass [weather|population|population-biography|biography|flight|movies]");
		}
//		readOldPopulationAndBiographyFilesWriteNewFiles();

		//		readOldPopulationFilesWriteNewFiles();
//		BiographyTruthReader biotru = new BiographyTruthReader(null);
//		biotru.readDirectoryFiles();

//		readOldBiographyFilesWriteNewFiles();

//		readOldFlightFilesWriteNewFiles();
//		readOldBookFilesWriteNewFilesSingleClaimValue();
//		readOldBookFilesWriteNewFiles();
	}

	static private void readOldWeatherFilesWriteNewFiles() {
		WeatherReadOldWriteNew weatherReadOldWriteNew = new WeatherReadOldWriteNew();
		weatherReadOldWriteNew.readOldWeatherFileAndWriteNewFiles(Globals.delimiterText);
	}

	static private void readOldPopulationAndBiographyFilesWriteNewFiles() {
		PopulationBiographyDataSetGenerator populationBiographyDataSetGenerator = new PopulationBiographyDataSetGenerator();
		populationBiographyDataSetGenerator.readOldPopulationAndBiographyFilesAndWriteNewFiles(Globals.delimiterText);
	}
	static private void readOldPopulationFilesWriteNewFiles() {
		PopulationDatasetReadOldWriteNew population = new PopulationDatasetReadOldWriteNew();
		population.readOldPopulationFileAndWriteNewFiles(Globals.delimiterText);
	}
	/**
	 * Read the flight files, with their old format, 
	 * and Write the new formatted files.
	 * @param dataset to be filled with the read flight data
	 */
	static private void readOldFlightFilesWriteNewFiles() {
		OldFlightDataSetReader dsw = new OldFlightDataSetReader();
		System.out.println("Started");
		dsw.readOldFlightFileAndWriteNewFiles(Globals.delimiterText);
		System.out.println("Done");
	}
	static private void readOldBiographyFilesWriteNewFiles() {
		BiographyDataSetReadOldWriteNew bio = new BiographyDataSetReadOldWriteNew();
		System.out.println("Started");
		bio.readOldBiographyFileAndWriteNewFiles(Globals.delimiterText);
		System.out.println("Done");
	}
	static private void readMoviesFilesWriteNewFiles() {
		MovieDataSetReadOldWriteNew movies = new MovieDataSetReadOldWriteNew();
		System.out.println("Started processing movies dataset");
		movies.readMoviesClaimsAndWriteTruthFiles(Globals.delimiterText);
		System.out.println("Done");
	}
}
