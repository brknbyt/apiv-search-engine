package de.brkn.pivse.domain;


public class Series extends AmazonItem {

	private int season;

	public Series(int id, String asin) {
		super(id, asin);
	}

	public int getSeason() {
		return season;
	}

	public void setSeason(int season) {
		this.season = season;
	}

}
