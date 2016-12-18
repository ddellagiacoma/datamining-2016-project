package org.apache.maven.DataMiningProject;

import org.apache.commons.math3.stat.regression.SimpleRegression;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import static java.lang.Math.max;
import static java.lang.Math.min;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;

public class Application {

	// initialize k for k weighted nearest neighbor
	final static int k = 10;
	// d is is a small constant used to avoid division by zero
	final static double d = 0.000000001;

	// a integer from 1 to 5 to decide which kind of similarity will be used in
	// the program
	// 1 refers to Cosine Similarity
	// 2 to Jaccard Similarity
	// 3 to Pearson Correlation
	// 4 to Linear Regression
	// 5 to all of them
	static int similaritySelected;

	static ArrayList<String> listPositive;
	static ArrayList<String> listNegative;

	// variable used to read the dataset.json
	final private String jsonSource;
	final private boolean sourceFromFile;

	// initialize parsing for json reading
	public Application(String jsonSource, boolean sourceFromFile) {
		this.jsonSource = jsonSource;
		this.sourceFromFile = sourceFromFile;
	}

	public static void main(String[] args) throws FileNotFoundException {
		// fill the positive and negative lists
		positiveWord();
		negativeWord();
		// read the review dataset
		Application jsonParserApplication = new Application("file/dataset.json", true);

		try (JsonReader jsonReader = jsonParserApplication.getJsonReader()) {
			Gson myGson = new Gson();
			JsonParser jsonParser = new JsonParser();
			JsonArray reviewArray = jsonParser.parse(jsonReader).getAsJsonArray();
			List<Review> listReview = new ArrayList<>();
			// copy the input reviews from the dataset file into listReview
			for (JsonElement aReview : reviewArray) {
				Review aReviewLine = myGson.fromJson(aReview, Review.class);
				listReview.add(aReviewLine);
			}

			Map<String, Integer> firstReview;
			Map<String, Integer> otherReview = null;

			String textReview = "";
			Integer userScore = 0;
			// read in the input file the review text, score and which kind
			// of similarity will be applied
			try {
				Scanner s = new Scanner(new File("input.txt"));
				userScore = Integer.parseInt(s.nextLine());
				textReview = s.nextLine();
				similaritySelected = Integer.parseInt(s.nextLine());
				s.close();
			} catch (Exception e) {
				System.out.println(e);
				System.exit(0);
			}

			// return a map where the keys will be the positive and negative
			// word given as input and as value the number of occurrences of the
			// key found in the input text review.
			firstReview = Counter(textReview);

			SimilarityOverall[] arraySimilarity = new SimilarityOverall[listReview.size()];

			SimpleRegression simpleRegression = new SimpleRegression(true);

			// for each review in the list will be calculated and stored the
			// similarities between the input text and the others
			int j = 0;
			for (Review review : listReview) {

				arraySimilarity[j] = new SimilarityOverall();
				arraySimilarity[j].setOverall(review.getOverall());

				// return a map where the keys will be the positive and negative
				// word given as input and as value the number of occurrences of
				// the key found in each text review of the dataset
				// in case of linear regression we use another kind of counter
				if ((similaritySelected != 4)) {
					otherReview = Counter(review.getReviewText());
				}

				// calculating and storing the Cosine similarity between the
				// input and a review of the dataset
				if ((similaritySelected == 1) || (similaritySelected == 5)) {
					Double cosineSimilarity = CosineSimilarity(firstReview, otherReview);
					arraySimilarity[j].setCosineSimilarity(cosineSimilarity);
				}

				// calculating and storing the Jaccard similarity between the
				// input and a review of the dataset
				if ((similaritySelected == 2) || (similaritySelected == 5)) {
					Double jaccardSimilarity = JaccardSimilarity(firstReview, otherReview);
					arraySimilarity[j].setJaccardSimilarity(jaccardSimilarity);
				}

				// calculating and storing the Pearson Correlation Coefficent
				// between the input and a review of the dataset
				if ((similaritySelected == 3) || (similaritySelected == 5)) {
					Double pearsonCorrelation = PearsonCorrelationCoefficent(firstReview, otherReview);
					arraySimilarity[j].setPearsonCorrelation(pearsonCorrelation);
				}

				// adding date to linear regression where x is the positivity
				// ratio (number of positive words/positive+negative) and
				// the y is the score of the user (overall)
				if ((similaritySelected == 4) || (similaritySelected == 5)) {
					simpleRegression.addData(CounterLinearRegression(review.getReviewText()), review.getOverall());
				}

				j++;
			}

			// create if doesn't exist and opening the output file
			File output = new File("output.txt");
			if (output.exists()) {
				output.delete();
			}
			output.createNewFile();

			// create the directory results if doesn't exist
			new File("results").mkdirs();

			FileWriter fileWritter = new FileWriter(output.getName(), true);
			BufferedWriter bufferWritter = new BufferedWriter(fileWritter);

			Double result;
			Double rms;

			// Cosine Similarity case
			if ((similaritySelected == 1) || (similaritySelected == 5)) {

				// sort the array of similarity by cosine similarity results
				arraySimilarity = SimilarityOverall.quickSortCosine(arraySimilarity, 0, arraySimilarity.length - 1);

				// call the function in order to calculate the
				// k-weightedNearestNeighbor
				result = weightedNearestNeighbor(arraySimilarity, 1);

				// write in append the true overall and predicted result in
				// order to calculate root mean square deviation
				File cosineResults = new File("results/CosineResults.txt");
				if (!cosineResults.exists()) {
					cosineResults.createNewFile();
				}
				FileWriter fileWritterCosine = new FileWriter("results/" + cosineResults.getName(), true);
				BufferedWriter bufferWritterCosine = new BufferedWriter(fileWritterCosine);
				bufferWritterCosine.write("Overall: " + userScore.toString() + " Prediction: " + result.toString());
				bufferWritterCosine.newLine();
				bufferWritterCosine.close();

				// call the function in order to calculate the root mean square
				// deviation for cosine similarity
				rms = RootMeanSquare(1);

				// write in the output file the result based on cosine
				// similarity and its root mean square deviation
				bufferWritter.write("Cosine Similarity: " + result.toString() + " RMSE: " + rms.toString());
				bufferWritter.newLine();

			}
			// Jaccard Similarity case
			if ((similaritySelected == 2) || (similaritySelected == 5)) {

				// sort the array of similarity by jaccard similarity results
				arraySimilarity = SimilarityOverall.quickSortJaccard(arraySimilarity, 0, arraySimilarity.length - 1);

				// call the function in order to calculate the
				// k-weightedNearestNeighbor
				result = weightedNearestNeighbor(arraySimilarity, 2);

				// write in append the true overall and predicted result in
				// order to calculate root mean square deviation
				File jaccardResults = new File("results/JaccardResults.txt");
				if (!jaccardResults.exists()) {
					jaccardResults.createNewFile();
				}
				FileWriter fileWritterJaccard = new FileWriter("results/" + jaccardResults.getName(), true);
				BufferedWriter bufferWritterJaccard = new BufferedWriter(fileWritterJaccard);
				bufferWritterJaccard.write("Overall: " + userScore.toString() + " Prediction: " + result.toString());
				bufferWritterJaccard.newLine();
				bufferWritterJaccard.close();

				// call the function in order to calculate the root mean square
				// deviation for jaccard similarity
				rms = RootMeanSquare(2);

				// write in the output file the result based on jaccard
				// similarity and its root mean square deviation
				bufferWritter.write("Jaccard Similarity: " + result.toString() + " RMSE: " + rms.toString());
				bufferWritter.newLine();
			}
			// Pearson Correlatin case
			if ((similaritySelected == 3) || (similaritySelected == 5)) {

				// sort the array of similarity by pearson correlation results
				arraySimilarity = SimilarityOverall.quickSortPearson(arraySimilarity, 0, arraySimilarity.length - 1);

				// call the function in order to calculate the
				// k-weightedNearestNeighbor
				result = weightedNearestNeighbor(arraySimilarity, 3);

				// write in append the true overall and predicted result in
				// order to calculate root mean square deviation
				File pearsonResults = new File("results/PearsonResults.txt");
				if (!pearsonResults.exists()) {
					pearsonResults.createNewFile();
				}
				FileWriter fileWritterPearson = new FileWriter("results/" + pearsonResults.getName(), true);
				BufferedWriter bufferWritterPearson = new BufferedWriter(fileWritterPearson);
				bufferWritterPearson.write("Overall: " + userScore.toString() + " Prediction: " + result.toString());
				bufferWritterPearson.newLine();
				bufferWritterPearson.close();

				// call the function in order to calculate the root mean square
				// deviation for pearson correlation
				rms = RootMeanSquare(3);

				// write in the output file the result based on pearson
				// correlation and its root mean square deviation
				bufferWritter.write("Pearson Correlation: " + result.toString() + " RMSE: " + rms.toString());
				bufferWritter.newLine();
			}

			// Linear Regression case
			if ((similaritySelected == 4) || (similaritySelected == 5)) {

				// predicting the result using linear regression
				result = simpleRegression.predict(CounterLinearRegression(textReview));

				// write in append the true overall and predicted result in
				// order to calculate root mean square deviation
				File linearResults = new File("results/LinearRegressionResults.txt");
				if (!linearResults.exists()) {
					linearResults.createNewFile();
				}
				FileWriter fileWritterLinear = new FileWriter("results/" + linearResults.getName(), true);
				BufferedWriter bufferWritterLinear = new BufferedWriter(fileWritterLinear);
				bufferWritterLinear.write("Overall: " + userScore.toString() + " Prediction: " + result.toString());
				bufferWritterLinear.newLine();
				bufferWritterLinear.close();

				// call the function in order to calculate the root mean square
				// deviation for linear regression
				rms = RootMeanSquare(4);

				// write in the output file the result based on linear
				// regression and its root mean square deviation
				bufferWritter.write(
						"Linear Regression: " + Math.max(1.0, Math.min(5.0, result)) + " RMSE: " + rms.toString());
			}
			bufferWritter.close();

			// print when the program has finished
			DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
			Date date = new Date();
			System.out.println("Finished at " + dateFormat.format(date));

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Obtain the JsonReader for the given source details.
	 *
	 * @return the JsonReader instance
	 * @throws FileNotFoundException
	 */
	private JsonReader getJsonReader() throws FileNotFoundException {
		JsonReader reader = null;
		if (sourceFromFile) {
			reader = new JsonReader(new InputStreamReader(new FileInputStream(this.jsonSource)));
		}
		return reader;
	}

	// fill the list of positive words using positive-words.txt
	public static void positiveWord() throws FileNotFoundException {
		Scanner s = new Scanner(new File("file/positive-words.txt"));
		listPositive = new ArrayList<>();

		while (s.hasNext()) {
			listPositive.add(s.next());
		}
		s.close();
	}

	// fill the list of negative words using negative-words.txt
	public static void negativeWord() throws FileNotFoundException {
		Scanner s = new Scanner(new File("file/negative-words.txt"));
		listNegative = new ArrayList<>();

		while (s.hasNext()) {
			listNegative.add(s.next());
		}
		s.close();
	}

	// creating for each text review a map of the occurrences of positive and
	// negative words written in the review
	public static Map<String, Integer> Counter(String text) throws FileNotFoundException {
		Map<String, Integer> countByWords = new HashMap<>();

		Scanner s = new Scanner(text);
		// Initializing the map with each occurrences of positive and negative
		// words equals to zero
		for (int i = 0; i < listPositive.size(); i++) {
			countByWords.put(listPositive.get(i), 0);
		}
		for (int i = 0; i < listNegative.size(); i++) {
			countByWords.put(listNegative.get(i), 0);
		}

		// reading each words of the review
		while (s.hasNext()) {
			String next = s.next();
			String[] words = next.split("[ \n\t\r.,;:!?(){}]");

			for (int i = 0; i < words.length; i++) {
				if (words[0].equals("")) {
					next = words[1].toLowerCase();
				} else {
					next = words[0].toLowerCase();
				}
			}

			// increment the value of the keyword (positive and negative) found
			// in review text
			if ((listPositive.contains(next)) || (listNegative.contains(next))) {
				if (countByWords.containsKey(next)) {
					countByWords.put(next, countByWords.get(next) + 1);
				} else {
					countByWords.put(next, 1);
				}
			}

		}

		s.close();

		return countByWords;
	}

	// count for each review text the number of positive and negative words used
	// in order calculate the positivity ratio for linear regression
	public static Double CounterLinearRegression(String text) throws FileNotFoundException {
		Double positive = 0.0;
		Double negative = 0.0;

		Scanner s = new Scanner(text);

		// reading each words of the review
		while (s.hasNext()) {
			String next = s.next();
			String[] words = next.split("[ \n\t\r.,;:!?(){}]");

			for (int i = 0; i < words.length; i++) {
				if (words[0].equals("")) {
					next = words[1].toLowerCase();
				} else {
					next = words[0].toLowerCase();
				}
			}

			// count the number of positive and negative words
			if (listPositive.contains(next)) {
				positive++;
			} else if (listNegative.contains(next)) {
				negative++;
			}

		}
		s.close();
		// return the positivity ratio (number of positive words in the
		// review/positive+negative)
		return (positive / (positive + negative + d));
	}

	// calculate the cosine similarity between the map of the occurrences of
	// positive and negative words in input text review and the same map of
	// other reviews of the dataset
	public static Double CosineSimilarity(Map<String, Integer> map, Map<String, Integer> map2) {

		double total = 0;
		double numerator = 0;
		double denominator1 = 0;
		double denominator2 = 0;

		Set s = map.entrySet();
		Iterator it = s.iterator();
		Set s2 = map2.entrySet();
		Iterator it2 = s2.iterator();

		// calculate the cosine similarity between two vectors.
		// The vectors compared are the two maps created before by calling the
		// Counter() method.
		// Each element of the vector will be the number of
		// occurrences of the keywords
		while (it.hasNext() && it2.hasNext()) {
			Entry entry = (Entry) it.next();
			Entry entry2 = (Entry) it2.next();
			Integer value = (Integer) entry.getValue();
			Integer value2 = (Integer) entry2.getValue();
			numerator = numerator + value * value2;
			denominator1 = denominator1 + Math.pow(value, 2);
			denominator2 = denominator2 + Math.pow(value2, 2);
		}

		// calculate the result and return it
		total = numerator / ((double) Math.sqrt(denominator1) * (double) Math.sqrt(denominator2) + d);
		return total;
	}

	// calculate the jaccard similarity between the map of the occurrences of
	// positive and negative words in input text review and the same map of
	// other reviews of the dataset
	public static Double JaccardSimilarity(Map<String, Integer> map, Map<String, Integer> map2) {

		double total = 0;
		double numerator = 0;
		double denominator = 0;

		Set s = map.entrySet();
		Iterator it = s.iterator();
		Set s2 = map2.entrySet();
		Iterator it2 = s2.iterator();

		// calculate the jaccard similarity between two vectors.
		// The vectors compared are the two maps created before by calling the
		// Counter() method.
		// Each element of the vector will be the number of
		// occurrences of the keywords
		while (it.hasNext() && it2.hasNext()) {
			Entry entry = (Entry) it.next();
			Entry entry2 = (Entry) it2.next();
			Integer value = (Integer) entry.getValue();
			Integer value2 = (Integer) entry2.getValue();
			numerator = numerator + min(value, value2);
			denominator = denominator + max(value, value2);
		}

		// calculate the result and return it
		total = numerator / (denominator + d);
		return total;
	}

	// calculate the pearson correlation coefficient between the map of the
	// occurrences of
	// positive and negative words in input text review and the same map of
	// other reviews of the dataset
	public static Double PearsonCorrelationCoefficent(Map<String, Integer> map, Map<String, Integer> map2) {

		double total = 0;
		double numerator = 0;
		double denominator1 = 0;
		double denominator2 = 0;
		double nElem = 0;
		double sumElem = 0;
		double sumElem2 = 0;

		Set s = map.entrySet();
		Iterator it = s.iterator();
		Set s2 = map2.entrySet();
		Iterator it2 = s2.iterator();

		// the pearson correlation coefficent will be calculated just on the
		// positive or negative words that appear at least one time in the two
		// text review compared
		while (it.hasNext() && (it2.hasNext())) {
			Entry entry = (Entry) it.next();
			Integer value = (Integer) entry.getValue();
			Entry entry2 = (Entry) it2.next();
			Integer value2 = (Integer) entry2.getValue();

			if ((value2 != 0) || (value != 0)) {

				sumElem2 += value2;
				sumElem += value;
				nElem++;
			}
		}

		it = s.iterator();
		it2 = s2.iterator();

		// the set of data used for calculate the pearson coefficient is created
		// by a pre-selection
		// of the relevant value in the two maps created before by calling the
		// Counter() method.
		// Each element of the vector will be the number of
		// occurrences of the keywords
		while (it.hasNext() && it2.hasNext()) {
			Entry entry = (Entry) it.next();
			Entry entry2 = (Entry) it2.next();
			Integer value = (Integer) entry.getValue();
			Integer value2 = (Integer) entry2.getValue();
			if ((value2 != 0) || (value != 0)) {
				numerator = numerator + ((value - (sumElem / nElem)) * (value2 - (sumElem2 / nElem)));
				denominator1 = denominator1 + Math.pow((value - (sumElem / nElem)), 2);
				denominator2 = denominator2 + Math.pow((value2 - (sumElem2 / nElem)), 2);
			}
		}

		total = numerator / ((double) Math.sqrt(denominator1) * (double) Math.sqrt(denominator2) + d);
		return total;
	}

	// calculate the k-weightedNearestNeighbor.
	// similarityIndex is the kind of similarity chosen as input
	public static Double weightedNearestNeighbor(SimilarityOverall[] arraySimilarity, int similarityIndex) {

		double resultNeighbor;
		double numerator = 0;
		double denominator = 0;

		// use only the k most similar reviews
		for (int i = 0; i < k; i++) {
			Double overall = arraySimilarity[i].getOverall();

			Double similarity = 0.0;
			if (similarityIndex == 1) {
				similarity = arraySimilarity[i].getCosineSimilarity();
			}
			if (similarityIndex == 2) {
				similarity = arraySimilarity[i].getJaccardSimilarity();
			}
			if (similarityIndex == 3) {
				similarity = arraySimilarity[i].getPearsonCorrelation();
			}

			numerator = numerator + (overall / (1 - similarity + d));
			denominator = denominator + (1 / (1 - similarity + d));
		}

		// calculate the final result of k-weightedNearestNeighbor
		resultNeighbor = numerator / denominator;
		return resultNeighbor;
	}

	// calculate the root mean square deviation for the selected similarity
	public static Double RootMeanSquare(int similarityIndex) throws FileNotFoundException {
		Double result;
		double numerator = 0;
		double denominator = 0;
		double overall = 0;
		double prediction = 0;

		Scanner s = null;
		if (similarityIndex == 1) {
			s = new Scanner(new File("results/CosineResults.txt"));
		}
		if (similarityIndex == 2) {
			s = new Scanner(new File("results/JaccardResults.txt"));
		}
		if (similarityIndex == 3) {
			s = new Scanner(new File("results/PearsonResults.txt"));
		}
		if (similarityIndex == 4) {
			s = new Scanner(new File("results/LinearRegressionResults.txt"));
		}

		// calculate the RMS using the previous results of the program
		while (s.hasNext()) {
			s.next();
			overall = Double.parseDouble(s.next());
			s.next();
			prediction = Double.parseDouble(s.next());
			numerator += Math.pow(prediction - overall, 2);
			denominator++;
		}
		
		// calculate the root mean square deviation
		result = Math.sqrt(numerator / denominator);
		s.close();
		return result;
	}

}