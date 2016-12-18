package org.apache.maven.DataMiningProject;

import java.util.ArrayList;

//class used to store all the fields of each review in the dataset
public class Review {

	private String reviewerID = "";
	private String asin = "";
	private String reviewerName = "";
	private ArrayList<Integer> helpful;
	private String reviewText = "";
	private double overall;
	private String summary = "";
	private int unixReviewTime;
	private String reviewTime = "";

	// get and set methods
	public String getReviewerID() {
		return this.reviewerID;
	}

	public String getAsin() {
		return this.asin;
	}

	public String getReviewerName() {
		return this.reviewerName;
	}

	public ArrayList<Integer> getHelpful() {
		return this.helpful;
	}

	public String getReviewText() {
		return this.reviewText;
	}

	public double getOverall() {
		return this.overall;
	}

	public String getSummary() {
		return this.summary;
	}

	public int getUnixReviewTime() {
		return this.unixReviewTime;
	}

	public String getReviewTime() {
		return this.reviewTime;
	}

	public void setReviewerID(String reviewerID) {
		this.reviewerID = reviewerID;
	}

	public void setAsin(String asin) {
		this.asin = asin;
	}

	public void setReviewerName(String reviewerName) {
		this.reviewerName = reviewerName;
	}

	public void setHelpful(ArrayList<Integer> helpful) {
		this.helpful = helpful;
	}

	public void setReviewText(String reviewText) {
		this.reviewText = reviewText;
	}

	public void setOverall(double overall) {
		this.overall = overall;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public void setUnixReviewTime(int unixReviewTime) {
		this.unixReviewTime = unixReviewTime;
	}

	public void setReviewTime(String reviewTime) {
		this.reviewTime = reviewTime;
	}

}