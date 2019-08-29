package de.brkn.pivse;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import com.google.common.base.CharMatcher;

import de.brkn.pivse.amazon.ItemFetcher;
import de.brkn.pivse.db.DatabaseConnector;
import de.brkn.pivse.db.DatabaseHandler;
import de.brkn.pivse.domain.Category;
import de.brkn.pivse.domain.Movie;
import de.brkn.pivse.freebase.FilmSearcher;
import de.brkn.pivse.imdb.IMDBTitleScraper;
import de.brkn.pivse.mp.MPTitleScraper;

public class MoviesDatabaseWriter {

	private final static String REGEX = "(-\\s)*(OmU|\\[OV\\])";

	public static void writeMoviesToDatabase(HashMap<String, String> titles,
			boolean isUpdate) {
		Connection conn = DatabaseConnector.getConnection();
		DatabaseHandler dbHandler = new DatabaseHandler(conn);

		Iterator<String> titlesIterator = titles.keySet().iterator();
		String title = "";
		String imdbTitle = "";
		String mpTitle = "";
		String originalTitle = "";
		String ov = "";
		int requests = 0;
		ItemFetcher itemFetcher = new ItemFetcher(Category.FILME);
		FilmSearcher fbSearcher = new FilmSearcher("parameters.properties");
		int n = 0;
		while (titlesIterator.hasNext()) {
			String asin = (String) titlesIterator.next();
			String displayTitle = titles.get(asin);

			title = "";
			imdbTitle = "";
			originalTitle = "";
			mpTitle = "";
			ov = "";
			// extract title and ov
			title = displayTitle.replaceFirst(REGEX, "");
			ov = CharMatcher.WHITESPACE.trimFrom(displayTitle
					.replace(title, ""));

			// remove empty spaces at title end
			title = CharMatcher.WHITESPACE.trimFrom(title);

			Movie movie = null;
			try {
				movie = (Movie) itemFetcher.fetchItem(asin, 0);
				if (null == movie) {
					movie = new Movie(0, asin);
				}

				n++;
				System.out.println(n + ". Title: " + title);
				movie.setTitle(title);
				movie.setOv(!ov.isEmpty());

				if (movie.getDirectors().isEmpty()
						|| "---".equals(movie.getDirectors().get(0))) {
					movie.setDirectors(new ArrayList<String>());
				} else {

					// sleep for 10 secs after 200 requests to imdb
					if (requests > 100) {
						Thread.sleep(3 * 60 * 1000);
						requests = 0;
					}

					System.out.println("Searching imdb title");
					if (IMDBTitleScraper.searchTitle(movie)) {
						imdbTitle = IMDBTitleScraper.getTitle();
						originalTitle = IMDBTitleScraper.getOriginalTitle();
					} else {
						if (MPTitleScraper.searchTitle(movie)) {
							mpTitle = MPTitleScraper.getTitle();
							originalTitle = MPTitleScraper.getOriginalTitle();
							movie.setOriginalTitle(originalTitle);
							if (IMDBTitleScraper.searchTitle(movie)) {
								imdbTitle = IMDBTitleScraper.getTitle();
							}
						}
					}

					System.out.println("IMDBTitle: " + imdbTitle);
					System.out.println("original title: " + originalTitle);
					movie.setImdbTitle(imdbTitle);
					movie.setOriginalTitle(originalTitle);
					int year = IMDBTitleScraper.getImdbYear() != null ? parseYear(IMDBTitleScraper
							.getImdbYear()) : 0;
					movie.setYear(year);
				}
				if (!originalTitle.isEmpty()) {
					String[] altTitles = fbSearcher.fetchAltTitles(movie);
					movie.setAlternativeTitles(altTitles);
				}
			} catch (InvalidKeyException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if (null != movie) {
				System.out.println("Inserting movie to DB");
				if (isUpdate) {
					dbHandler.updateMovie(movie);
				} else {
					dbHandler.insertMovie(movie);
				}
			} else {
				System.err.println("No movie to insert");
			}

			System.out.println();
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}

	public static int parseYear(String yearText) {
		String[] formatStrings = { "d MMM. y", "Y" };

		int indexOfOpenBracket = yearText.indexOf("(");
		int indexOfLastBracket = yearText.lastIndexOf(")");

		String cleanedYearText = indexOfOpenBracket > -1
				&& indexOfLastBracket > -1 ? yearText.substring(
				indexOfOpenBracket + 1, indexOfLastBracket) : yearText;
		Date date = null;
		for (String formatString : formatStrings) {
			try {
				date = new SimpleDateFormat(formatString)
						.parse(cleanedYearText);
				Calendar c = Calendar.getInstance();
				c.setTime(date);

				return c.get(Calendar.YEAR);
			} catch (ParseException e) {
			}
		}

		return -1;
	}

}
