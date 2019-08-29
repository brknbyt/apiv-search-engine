package de.brkn.pivse.domain;

import java.util.List;

public class Movie extends AmazonItem {

	private List<String> directors;
	private int runningTime;
	private String studio;
	private String imdbTitle;
	private String originalTitle;
	private String[] alternativeTitles;
	private int year;

	public Movie(int id, String asin) {
		super(id, asin);
	}

	public void setDirectors(List<String> directors) {
		this.directors = directors;
	}

	public List<String> getDirectors() {
		return directors;
	}

	public int getRunningTime() {
		return runningTime;
	}

	public void setRunningTime(int runningTime) {
		this.runningTime = runningTime;
	}

	public String getStudio() {
		return studio;
	}

	public void setStudio(String studio) {
		this.studio = studio;
	}

	public String getImdbTitle() {
		return imdbTitle;
	}

	public void setImdbTitle(String imdbTitle) {
		this.imdbTitle = imdbTitle;
	}

	public int getYear() {
		return year;
	}

	public void setYear(int year) {
		this.year = year;
	}

	public String getOriginalTitle() {
		return originalTitle;
	}

	public void setOriginalTitle(String originalTitle) {
		this.originalTitle = originalTitle;
	}

	public String[] getAlternativeTitles() {
		return alternativeTitles;
	}

	public void setAlternativeTitles(String[] alternativeTitles) {
		this.alternativeTitles = alternativeTitles;
	}

}
