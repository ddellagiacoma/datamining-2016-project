package org.apache.maven.DataMiningProject;

// class used to store each review overall of the dataset and the required similarities with the input review
public class SimilarityOverall {

	Double overall = 0.0;
	Double cosineSimilarity = 0.0;
	Double jaccardSimilarity = 0.0;
	Double pearsonCorrelation = 0.0;

	// get and set methods
	public Double getOverall() {
		return this.overall;
	}

	public Double getCosineSimilarity() {
		return this.cosineSimilarity;
	}

	public Double getJaccardSimilarity() {
		return this.jaccardSimilarity;
	}

	public Double getPearsonCorrelation() {
		return this.pearsonCorrelation;
	}

	public void setOverall(Double overall) {
		this.overall = overall;
	}

	public void setCosineSimilarity(Double cosineSimilarity) {
		this.cosineSimilarity = cosineSimilarity;
	}

	public void setJaccardSimilarity(Double jaccardSimilarity) {
		this.jaccardSimilarity = jaccardSimilarity;
	}

	public void setPearsonCorrelation(Double pearsonCorrelation) {
		this.pearsonCorrelation = pearsonCorrelation;
	}

	// quick sort used to sort the cosine similarities in descending order in
	// order to select the k highest results for use them in
	// k-weightedNearestNeighbor
	public static SimilarityOverall[] quickSortCosine(SimilarityOverall[] arr, int low, int high) {
		if (arr == null || arr.length == 0)
			return arr;

		if (low >= high)
			return arr;

		// pick the pivot
		int middle = low + (high - low) / 2;
		double pivot = arr[middle].getCosineSimilarity();

		// make left < pivot and right > pivot
		int i = low, j = high;
		while (i <= j) {
			while (arr[i].getCosineSimilarity() > pivot) {
				i++;
			}

			while (arr[j].getCosineSimilarity() < pivot) {
				j--;
			}

			if (i <= j) {
				SimilarityOverall temp = arr[i];
				arr[i] = arr[j];
				arr[j] = temp;
				i++;
				j--;
			}
		}

		// recursively sort two sub parts
		if (low < j)
			quickSortCosine(arr, low, j);

		if (high > i)
			quickSortCosine(arr, i, high);

		return arr;
	}

	// quick sort used to sort the jaccard similarities in descending order in
	// order to select the k highest results for use them in
	// k-weightedNearestNeighbor
	public static SimilarityOverall[] quickSortJaccard(SimilarityOverall[] arr, int low, int high) {
		if (arr == null || arr.length == 0)
			return arr;

		if (low >= high)
			return arr;

		// pick the pivot
		int middle = low + (high - low) / 2;
		double pivot = arr[middle].getJaccardSimilarity();

		// make left < pivot and right > pivot
		int i = low, j = high;
		while (i <= j) {
			while (arr[i].getJaccardSimilarity() > pivot) {
				i++;
			}

			while (arr[j].getJaccardSimilarity() < pivot) {
				j--;
			}

			if (i <= j) {
				SimilarityOverall temp = arr[i];
				arr[i] = arr[j];
				arr[j] = temp;
				i++;
				j--;
			}
		}

		// recursively sort two sub parts
		if (low < j)
			quickSortJaccard(arr, low, j);

		if (high > i)
			quickSortJaccard(arr, i, high);

		return arr;
	}

	// quick sort used to sort the pearson correlations in descending order in
	// order to select the k highest results for use them in
	// k-weightedNearestNeighbor
	public static SimilarityOverall[] quickSortPearson(SimilarityOverall[] arr, int low, int high) {
		if (arr == null || arr.length == 0)
			return arr;

		if (low >= high)
			return arr;

		// pick the pivot
		int middle = low + (high - low) / 2;
		double pivot = arr[middle].getPearsonCorrelation();

		// make left < pivot and right > pivot
		int i = low, j = high;
		while (i <= j) {
			while (arr[i].getPearsonCorrelation() > pivot) {
				i++;
			}

			while (arr[j].getPearsonCorrelation() < pivot) {
				j--;
			}

			if (i <= j) {
				SimilarityOverall temp = arr[i];
				arr[i] = arr[j];
				arr[j] = temp;
				i++;
				j--;
			}
		}

		// recursively sort two sub parts
		if (low < j)
			quickSortPearson(arr, low, j);

		if (high > i)
			quickSortPearson(arr, i, high);

		return arr;
	}

}
