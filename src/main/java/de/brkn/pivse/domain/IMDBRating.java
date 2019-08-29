package de.brkn.pivse.domain;

public class IMDBRating {
	
	private int movieId;
	private float rating;
	private long users;

	public IMDBRating(int movieId) {
		this.movieId = movieId;
		setRating(-1);
		setUsers(-1);
	}

	public int getMovieId() {
		return movieId;
	}

	public float getRating() {
		return rating;
	}

	public void setRating(float rating) {
		this.rating = rating;
	}

	public long getUsers() {
		return users;
	}

	public void setUsers(long users) {
		this.users = users;
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		String NEW_LINE = System.lineSeparator();
		result.append(" rating: " + rating + "; users: " + users + NEW_LINE);
		return result.toString();
	}

}
