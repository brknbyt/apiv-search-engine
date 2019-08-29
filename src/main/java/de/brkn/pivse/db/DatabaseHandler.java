package de.brkn.pivse.db;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.common.base.CharMatcher;

import de.brkn.pivse.domain.IMDBRating;
import de.brkn.pivse.domain.Movie;
import de.brkn.pivse.domain.RTRating;
import de.brkn.pivse.domain.Series;

public class DatabaseHandler {

	private Connection dbConn;

	public DatabaseHandler(Connection dbConn) {
		this.dbConn = dbConn;
	}

	public void clearAllTables() {
		Statement clearStatement = null;
		String clear = "TRUNCATE movies, series, directors, movie_to_directors, movie_rt_ratings, movie_imdb_ratings, movie_alternative_titles, movie_to_alternative_titles CASCADE;"
				+ "ALTER SEQUENCE movies_id_seq RESTART WITH 1;"
				+ "ALTER SEQUENCE directors_id_seq RESTART WITH 1;"
				+ "ALTER SEQUENCE movie_alternative_titles_id_seq RESTART WITH 1;"
				+ "ALTER SEQUENCE series_id_seq RESTART WITH 1;";
		try {
			clearStatement = dbConn.createStatement();
			clearStatement.executeUpdate(clear);
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (null != clearStatement) {
					clearStatement.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public void clearSeriesTable() {
		Statement clearStatement = null;
		String clear = "TRUNCATE series CASCADE";
		try {
			clearStatement = dbConn.createStatement();
			clearStatement.executeUpdate(clear);
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (null != clearStatement) {
					clearStatement.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public void clearMoviesTable() {
		Statement clearStatement = null;
		String clear = "TRUNCATE movies CASCADE";
		try {
			clearStatement = dbConn.createStatement();
			clearStatement.executeUpdate(clear);
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (null != clearStatement) {
					clearStatement.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public void clearRTRatingsTable() {
		Statement clearStatement = null;
		String clear = "TRUNCATE movie_rt_ratings CASCADE";
		try {
			clearStatement = dbConn.createStatement();
			clearStatement.executeUpdate(clear);
		} catch (SQLException e) {

			e.printStackTrace();
		} finally {
			try {
				if (null != clearStatement) {
					clearStatement.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public void clearIMDBRatingsTable() {
		Statement clearStatement = null;
		String clear = "TRUNCATE movie_imdb_ratings CASCADE";
		try {
			clearStatement = dbConn.createStatement();
			clearStatement.executeUpdate(clear);
		} catch (SQLException e) {

			e.printStackTrace();
		} finally {
			try {
				if (null != clearStatement) {
					clearStatement.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * @param series
	 */
	public void insertSerie(Series series) {
		PreparedStatement seriesStatement;
		try {
			String insertSeries = "INSERT INTO series"
					+ "(title, asin, detail_page_url, audience_rating, ov, release_date, season) "
					+ "VALUES(?,?,?,?,?,?,?)";

			seriesStatement = dbConn.prepareStatement(insertSeries);

			seriesStatement.setString(1, series.getTitle());
			seriesStatement.setString(2, series.getAsin());
			seriesStatement.setString(3, series.getDetailPageUrl());
			seriesStatement.setInt(4, series.getAudienceRating());
			seriesStatement.setBoolean(5, series.isOv());
			if (null != series.getReleaseDate()) {
				Date releaseDate = Date.valueOf(series.getReleaseDate());
				seriesStatement.setDate(6, releaseDate);
			} else {
				seriesStatement.setDate(6, null);
			}
			seriesStatement.setInt(7, series.getSeason());

			seriesStatement.execute();
		} catch (SQLException e) {

			e.printStackTrace();
		}
	}

	/**
	 * @param movie
	 */
	public void insertMovie(Movie movie) {
		PreparedStatement moviesStatement;
		try {
			String insertMovies = "INSERT INTO movies"
					+ "(title, asin, detail_page_url, audience_rating, ov, release_date, running_time, imdb_title, studio, original_title, year) "
					+ "VALUES(?,?,?,?,?,?,?,?,?,?,?) RETURNING id";

			moviesStatement = dbConn.prepareStatement(insertMovies);

			moviesStatement.setString(1, movie.getTitle());
			moviesStatement.setString(2, movie.getAsin());
			moviesStatement.setString(3, movie.getDetailPageUrl());
			moviesStatement.setInt(4, movie.getAudienceRating());
			moviesStatement.setBoolean(5, movie.isOv());
			if (null != movie.getReleaseDate()
					&& !movie.getReleaseDate().isEmpty()) {
				Date releaseDate = Date.valueOf(movie.getReleaseDate());
				moviesStatement.setDate(6, releaseDate);
			} else {
				moviesStatement.setDate(6, null);
			}
			moviesStatement.setInt(7, movie.getRunningTime());
			moviesStatement.setString(8, movie.getImdbTitle());
			moviesStatement.setString(9, movie.getStudio());
			moviesStatement.setString(10, movie.getOriginalTitle());
			moviesStatement.setInt(11, movie.getYear());

			ResultSet rs = moviesStatement.executeQuery();
			while (rs.next()) {
				int id = rs.getInt(1);

				List<String> movieDirs = movie.getDirectors();

				for (String dir : movieDirs) {
					int did = containsDirector(dir);
					if (-1 == did) {
						did = insertDirector(dir);
					}
					insertMovieDirector(id, did);
				}

				String[] altTitles = movie.getAlternativeTitles();
				if (null != altTitles && altTitles.length > 0) {
					for (int i = 0; i < altTitles.length; i++) {
						insertMovieAlternativeTitle(id, trim(altTitles[i]));
					}
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void updateMovie(Movie movie) {
		PreparedStatement moviesStatement;
		try {
			String updatetMovies = "UPDATE movies SET imdb_title = '"
					+ movie.getImdbTitle().replace("'", "''")
					+ "', original_title = '"
					+ movie.getOriginalTitle().replace("'", "''")
					+ "' where title = '" + movie.getTitle().replace("'", "''")
					+ "' RETURNING id;";

			moviesStatement = dbConn.prepareStatement(updatetMovies);

			ResultSet rs = moviesStatement.executeQuery();

			while (rs.next()) {
				int id = rs.getInt(1);
				removeMovieAlternativeTitle(id);
				String[] altTitles = movie.getAlternativeTitles();
				if (null != altTitles && altTitles.length > 0) {
					for (int i = 0; i < altTitles.length; i++) {
						insertMovieAlternativeTitle(id, trim(altTitles[i]));
					}
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void insertMovieAlternativeTitle(int mid, String title) {
		int id = -1;
		PreparedStatement altTitleStatement;
		try {
			String insertAltTitle = "INSERT INTO movie_alternative_titles (title) VALUES(?)  RETURNING id";

			altTitleStatement = dbConn.prepareStatement(insertAltTitle);
			altTitleStatement.setString(1, title);

			ResultSet rs = altTitleStatement.executeQuery();
			while (rs.next()) {
				id = rs.getInt(1);
				insertMovieToAltTitle(mid, id);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void removeMovieAlternativeTitle(int mid) {
		PreparedStatement altTitleStatement;
		try {
			int aid = removeMovieToAltTitle(mid);
			String removeAltTitle = "DELETE FROM movie_alternative_titles WHERE id = "
					+ aid + ";";

			altTitleStatement = dbConn.prepareStatement(removeAltTitle);

			altTitleStatement.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void insertMovieToAltTitle(int mid, int aid) {
		PreparedStatement altTitleStatement;
		try {
			String insertMovToAlt = "INSERT INTO movie_to_alternative_titles"
					+ "(mid, aid) " + "VALUES(?,?)";

			altTitleStatement = dbConn.prepareStatement(insertMovToAlt);
			altTitleStatement.setInt(1, mid);
			altTitleStatement.setInt(2, aid);

			altTitleStatement.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private int removeMovieToAltTitle(int mid) {
		PreparedStatement altTitleStatement;
		try {
			String insertMovToAlt = "DELETE FROM movie_to_alternative_titles"
					+ " WHERE mid = " + mid + " RETURNING aid;";

			altTitleStatement = dbConn.prepareStatement(insertMovToAlt);

			ResultSet rs = altTitleStatement.executeQuery();
			while (rs.next()) {
				int aid = rs.getInt(1);
				return aid;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return -1;
	}

	public int insertDirector(String director) {
		int id = -1;
		PreparedStatement directorsStatement;
		try {
			String insertDirector = "INSERT INTO directors (name) VALUES(?) RETURNING id";

			directorsStatement = dbConn.prepareStatement(insertDirector);
			directorsStatement.setString(1, director);

			ResultSet rs = directorsStatement.executeQuery();
			while (rs.next()) {
				id = rs.getInt(1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return id;
	}

	public void insertMovieDirector(int mid, int did) {
		PreparedStatement moviesStatement;
		try {
			String insertMovies = "INSERT INTO movie_to_directors"
					+ "(mid, did) " + "VALUES(?,?)";

			moviesStatement = dbConn.prepareStatement(insertMovies);
			moviesStatement.setInt(1, mid);
			moviesStatement.setInt(2, did);

			moviesStatement.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void insertMovieRTRating(RTRating rating) {
		PreparedStatement moviesStatement;
		try {
			String insertMovies = "INSERT INTO movie_rt_ratings"
					+ "(mid, critics_rating, critics_score, audience_rating, audience_score) "
					+ "VALUES(?,?,?,?,?)";

			moviesStatement = dbConn.prepareStatement(insertMovies);

			moviesStatement.setInt(1, rating.getMovieId());
			moviesStatement.setString(2, rating.getCriticsRating());
			moviesStatement.setInt(3, (int) rating.getCriticsScore());
			moviesStatement.setString(4, rating.getAudienceRating());
			moviesStatement.setInt(5, (int) rating.getAudienceScore());

			moviesStatement.execute();
		} catch (SQLException e) {

			e.printStackTrace();
		}
	}

	public void insertMovieIMDBRating(IMDBRating rating) {
		PreparedStatement moviesStatement;
		try {
			String insertMovies = "INSERT INTO movie_imdb_ratings"
					+ "(mid, rating, users) " + "VALUES(?,?,?)";

			moviesStatement = dbConn.prepareStatement(insertMovies);

			moviesStatement.setInt(1, rating.getMovieId());
			moviesStatement.setFloat(2, rating.getRating());
			moviesStatement.setInt(3, (int) rating.getUsers());

			moviesStatement.execute();
		} catch (SQLException e) {

			e.printStackTrace();
		}
	}

	public List<Movie> getMovies(String type) {
		ArrayList<Movie> movies = new ArrayList<Movie>();
		PreparedStatement moviesStatement;
		try {
			String selectMovies = "SELECT * FROM movies";
			selectMovies += "!imdb".equals(type) ? " WHERE imdb_title = '';"
					: "";
			selectMovies += "!original".equals(type) ? " WHERE original_title = '';"
					: "";
			selectMovies += "imdb".equals(type) ? " WHERE imdb_title != '';"
					: "";
			selectMovies += "original".equals(type) ? " WHERE original_title != '';"
					: "";
			selectMovies += "!ratings".equals(type) ? 
					" LEFT JOIN movie_rt_ratings ON movies.id = movie_rt_ratings.mid "
					+ "LEFT JOIN movie_imdb_ratings ON movies.id = movie_imdb_ratings.mid "
					+ "WHERE movie_rt_ratings.mid IS NULL OR movie_imdb_ratings.mid IS NULL;"
					: "";

			moviesStatement = dbConn.prepareStatement(selectMovies);
			ResultSet rs = moviesStatement.executeQuery();
			Movie movie;
			int count = 0;
			while (rs.next()) {
				int id = rs.getInt("id");
				movie = new Movie(id, trim(rs.getString("asin")));
				movie.setTitle(trim(rs.getString("title")));
				movie.setDetailPageUrl(trim(rs.getString("detail_page_url")));
				movie.setAudienceRating(rs.getInt("audience_rating"));
				movie.setOv(rs.getBoolean("ov"));
				movie.setReleaseDate(trim(rs.getString("release_date")));
				movie.setRunningTime(rs.getInt("running_time"));
				movie.setImdbTitle(trim(rs.getString("imdb_title")));
				movie.setOriginalTitle(trim(rs.getString("original_title")));
				movie.setAlternativeTitles(getAlternativeTitles(id));
				movie.setYear(rs.getInt("year"));
				movie.setDirectors(getDirectors(id));
				movie.setStudio(trim(rs.getString("studio")));
				movies.add(movie);
				count++;
			}
			System.out.println(count);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return movies;
	}

	private String[] getAlternativeTitles(int id) {
		ArrayList<String> altTitles = new ArrayList<String>();
		PreparedStatement altTitleStatement;
		try {
			String selectMovies = "SELECT movie_alternative_titles.title from movies "
					+ "LEFT JOIN movie_to_alternative_titles ON movies.id = movie_to_alternative_titles.mid "
					+ "LEFT JOIN movie_alternative_titles ON movie_alternative_titles.id = movie_to_alternative_titles.aid "
					+ "WHERE movies.id = " + id + ";";

			altTitleStatement = dbConn.prepareStatement(selectMovies);
			ResultSet rs = altTitleStatement.executeQuery();
			while (rs.next()) {
				altTitles.add(rs.getString("title"));
			}
		} catch (SQLException e) {

			e.printStackTrace();
		}

		return altTitles.toArray(new String[altTitles.size()]);
	}

	public List<Movie> getMoviesWithoutDirectors() {
		ArrayList<Movie> movies = new ArrayList<Movie>();
		PreparedStatement moviesStatement;
		try {
			String selectMovies = "SELECT title, name from movies "
					+ "LEFT JOIN movie_to_directors ON movies.id = movie_to_directors.mid "
					+ "LEFT JOIN directors ON directors.id = movie_to_directors.did WHERE name is NULL;";

			moviesStatement = dbConn.prepareStatement(selectMovies);
			ResultSet rs = moviesStatement.executeQuery();
			Movie movie;
			while (rs.next()) {
				movie = new Movie(rs.getInt("id"), trim(rs.getString("asin")));
				movie.setTitle(trim(rs.getString("title")));
				movie.setDetailPageUrl(trim(rs.getString("detail_page_url")));
				movie.setAudienceRating(rs.getInt("audience_rating"));
				movie.setOv(rs.getBoolean("ov"));
				movie.setReleaseDate(trim(rs.getString("release_date")));
				movie.setRunningTime(rs.getInt("running_time"));
				movie.setImdbTitle(trim(rs.getString("imdb_title")));
				movie.setStudio(trim(rs.getString("studio")));
				movies.add(movie);
			}
		} catch (SQLException e) {

			e.printStackTrace();
		}
		return movies;
	}

	public int containsDirector(String director) {
		PreparedStatement directorsStatement;
		int id = -1;
		try {
			String selectDirectors = "SELECT id FROM directors WHERE name = \'"
					+ director.replace("'", "''") + "\'";
			directorsStatement = dbConn.prepareStatement(selectDirectors);
			ResultSet rs = directorsStatement.executeQuery();
			while (rs.next()) {
				id = rs.getInt(1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return id;
	}

	public HashMap<Integer, String> getDirectors() {
		HashMap<Integer, String> directors = new HashMap<Integer, String>();
		PreparedStatement directorsStatement;
		try {
			String selectDirectors = "SELECT * FROM directors";
			directorsStatement = dbConn.prepareStatement(selectDirectors);
			ResultSet rs = directorsStatement.executeQuery();
			while (rs.next()) {
				directors.put(rs.getInt("id"), rs.getString("name"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return directors;
	}

	public List<String> getDirectors(int movieId) {
		List<String> directors = new ArrayList<String>();
		PreparedStatement directorsStatement;
		try {
			String selectDirectors = "SELECT * FROM movies, movie_to_directors, directors "
					+ "WHERE movies.id = movie_to_directors.mid AND directors.id = movie_to_directors.did "
					+ "AND movies.id = " + movieId + ";";
			directorsStatement = dbConn.prepareStatement(selectDirectors);
			ResultSet rs = directorsStatement.executeQuery();
			while (rs.next()) {
				directors.add(rs.getString("name"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return directors;
	}

	private String trim(String text) {
		if (null != text && !text.isEmpty()) {
			return CharMatcher.WHITESPACE.trimFrom(text);
		}
		return text;
	}

}
