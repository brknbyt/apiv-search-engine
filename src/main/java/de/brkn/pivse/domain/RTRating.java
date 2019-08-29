package de.brkn.pivse.domain;

public class RTRating {

	private int movieId;
	private String criticsRating;
	private long criticsScore;
	private String audienceRating;
	private long audienceScore;

	public RTRating(int movieId) {
		this.movieId = movieId;
		criticsRating = "";
		criticsScore = -1;
		audienceRating = "";
		audienceScore = -1;
	}

	public int getMovieId() {
		return movieId;
	}

	public String getCriticsRating() {
		return criticsRating;
	}

	public void setCriticsRating(String criticsRating) {
		this.criticsRating = criticsRating;
	}

	public long getCriticsScore() {
		return criticsScore;
	}

	public void setCriticsScore(long criticsScore) {
		this.criticsScore = criticsScore;
	}

	public String getAudienceRating() {
		return audienceRating;
	}

	public void setAudienceRating(String audienceRating) {
		this.audienceRating = audienceRating;
	}

	public long getAudienceScore() {
		return audienceScore;
	}

	public void setAudienceScore(long audienceScore) {
		this.audienceScore = audienceScore;
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		String NEW_LINE = System.lineSeparator();
		result.append(" rating: " + criticsScore + ":" + audienceScore
				+ NEW_LINE);
		return result.toString();
	}

}
