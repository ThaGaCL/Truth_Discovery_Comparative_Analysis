# Truth Discovery Comparative Analysis
Java implementation of truth discovery algorithms


This repository contains all the Java source code of truth discovery methods used for the comparative analysis of our [Arxiv paper CoRR abs/1409.6428](https://arxiv.org/abs/1409.6428)

The main goal of this project was to design a scalable and accurate truth discovery system to score the veracity of structured data extracted from multiple online sources.

We provided a demo of the tool we have developped, called AllegatorTrack (not longer maintained) at ICDE 2015 in Seoul.

Existing truth discovery models have limited applicability and we extensively studied them in our experimental evaluation study. 



## For more information

Please check the documentation folder to access additional resources on truth discovery (papers, tutorials, demo poster).

* Laure Berti-Équille, Javier Borge-Holthoefer, Veracity of Data: From Truth Discovery Computation Algorithms to Models of Misinformation Dynamics. Synthesis Lectures on Data Management, Morgan & Claypool Publishers, December 2015. [Publisher Site](http://www.morganclaypool.com/doi/abs/10.2200/S00676ED1V01Y201509DTM042)

* Laure Berti-Équille, Javier Borge-Holthoefer. Scaling Up Truth Discovery. Tutorial at the International Conference on Data Engineering (ICDE 2016), Helsinki, May 2016. [Abstract](http://pageperso.lif.univ-mrs.fr/~laure.berti/pub/tutorial-ICDE16-abstract.pdf) [Slides](http://pageperso.lif.univ-mrs.fr/~laure.berti/pub/tutorial-ICDE2016-Laure-Berti-Equille.pdf)

* Laure Berti-Équille, Javier Borge-Holthoefer. Veracity of Big Data: From Truth Discovery Computation Algorithms to Models of Misinformation Dynamics. Tutorial at the 24th ACM International on Conference on Information and Knowledge Management (CIKM 2015), Melbourne, October 2015. [Slides](http://pageperso.lif.univ-mrs.fr/~laure.berti/pub/tutorial-CIKM2015.pdf)

* Dalia Attia Waguih, Naman Goel, Hossam M. Hammady, Laure Berti-Equille. AllegatorTrack: Visualizing and Explaining Truth Discovery Results from Multisource Data (demo). The 31th International Conference on Data Engineering (ICDE), Seoul, Korea, 2015 [demo paper](http://pageperso.lif.univ-mrs.fr/~laure.berti/pub/demo_ICDE2015.pdf)
     
* Dalia Attia Waguih, Laure Berti-Equille. Truth Discovery Algorithms - An Experimental Evaluation. [Arxiv CoRR abs/1409.6428](https://arxiv.org/abs/1409.6428)
     
## Run locally

The workspace is set up to run from the repository root using the local `DAFNAData/` directory.

Compile the runnable subset with Java 25 compatibility:

```bash
cd /home/thilons/Documentos/tcc/Truth_Discovery_Comparative_Analysis
mkdir -p build/classes
javac --release 25 -d build/classes @sources-runtime.txt
```

Run the main entrypoint:

```bash
java -cp build/classes main.MainClass weather
```

Other supported modes are `population`, `population-biography`, `biography`, and `flight`.

Input data is expected under `DAFNAData/unformatted/...` and generated output is written under `DAFNAData/formatted/...`.

Expected local layout:

```text
DAFNAData/
	unformatted/
		weather/
		clean_flight/
		Population/claims/
		Biographies/claims/
	formatted/
		weather/claims/
		weather/truth/
		flights/claims/
		flights/flight_truth/
		Population/claims/
		Population/truth/
		PopulationBiography/claims/
		PopulationBiography/truth/
		Biographies/claims/
		Biographies/truth/
	experiments/
		charts/chartsCollection/all/
		charts/chartsCollection/final/
		datasetLog/
		voterLog/
		dependencyReport/
```

Examples:

```bash
java -cp build/classes main.MainClass weather
java -cp build/classes main.MainClass population
java -cp build/classes main.MainClass population-biography
java -cp build/classes main.MainClass biography
java -cp build/classes main.MainClass flight
```

## Run truth discovery algorithms on movies dataset

The dataset formatting step is:

```bash
java -cp build/classes main.MainClass movies
```

To run the available voters for movies in one command:

```bash
# Compile local compatibility classes plus the movies runner
javac --release 25 -cp source/bin -d build/classes \
	source/java-code/qcri/dafna/dataModel/dataFormatter/DataCleaner.java \
	source/java-code/qcri/dafna/dataModel/dataFormatter/DataComparator.java \
	source/java-code/main/MoviesAllAlgorithmsRunner.java

# Execute all voters for movies
java -cp build/classes:source/bin main.MoviesAllAlgorithmsRunner
```

Or use the single automation script:

```bash
chmod +x run_movies_all.sh
./run_movies_all.sh
```

Notes:
- `run_movies_all.sh` automatically downloads Apache Commons Math3 into `.lib/commons-math3-3.6.1.jar` if needed.
- If network is unavailable and Math3 is missing, algorithms that require it may be skipped.
- The runner prints CSV lines with metrics per algorithm: TP, FP, FN, TN, precision, recall, accuracy, specificity, F1, iterations, and duration.

## Compare claims with truth

Run from the repository root.

Quick option (all datasets at once):

```bash
chmod +x compare_metrics.sh
./compare_metrics.sh
```

### Weather

Both files use the same claim-like format, so compare by `objectId + property + value`:

```bash
awk -F'\|\t' '{print $3"|"$4"|"$5}' DAFNAData/formatted/weather/claims/weather-accuweather.txt | sort > /tmp/weather.claims
awk -F'\|\t' '{print $3"|"$4"|"$5}' DAFNAData/formatted/weather/truth/weather-truth.txt | sort > /tmp/weather.truth
comm -3 /tmp/weather.claims /tmp/weather.truth
```

If `comm -3` returns no lines, claims and truth match exactly for those keys.

### Flight

Flight truth is row-based and claims are property-based. Convert truth to property-based keys first:

```bash
awk -F'\t' '{
	oid=$1;
	print oid"|ExpectedDepartureTime|"$2;
	print oid"|ActualDepartureTime|"$3;
	print oid"|DepartureGate|"$4;
	print oid"|ExpectedArrivalTime|"$5;
	print oid"|ActualArrivalTime|"$6;
	if (NF>=7 && $7!="" && $7!="--") print oid"|ArrivalGate|"$7;
}' DAFNAData/formatted/flights/flight_truth/2013-12-02-truth.txt | sort > /tmp/flight.truth

awk -F'\|\t' '{print $3"|"$4"|"$5}' DAFNAData/formatted/flights/claims/2013-12-02-Flight-1.txt | sort > /tmp/flight.claims
comm -3 /tmp/flight.claims /tmp/flight.truth
```

### Population

Population truth (`city, state, year, value`) maps to claim key `city, state|Population<year>|value`:

```bash
awk -F',' '{city=$1", "$2; year=$3; val=$4; gsub(/^ +| +$/,"",city); gsub(/^ +| +$/,"",year); gsub(/^ +| +$/,"",val); print city"|Population"year"|"val}' DAFNAData/formatted/Population/truth/population-truth.txt | sort > /tmp/pop.truth
awk -F'\|\t' '{print $3"|"$4"|"$5}' DAFNAData/formatted/Population/claims/population-1.txt | sort > /tmp/pop.claims
comm -3 /tmp/pop.claims /tmp/pop.truth
```

### Biography

For this workspace sample, biography truth was prepared as tabular values and can be compared as `objectId + property + value`:

```bash
awk -F'\|\t' '{print $3"|"$4"|"$5}' DAFNAData/formatted/Biographies/claims/bio-1.txt | sort > /tmp/bio.claims
awk -F'\t' '{print $1"|"$2"|"$3}' DAFNAData/formatted/Biographies/truth/biography-truth.txt | sort > /tmp/bio.truth
comm -3 /tmp/bio.claims /tmp/bio.truth
```

Tip: to compute only match counts, use `comm -12` and `wc -l`:

```bash
comm -12 /tmp/weather.claims /tmp/weather.truth | wc -l
```

## Licence

Learn2Clean is licensed under the BSD 3-Clause "New" or "Revised" License.
