package de.brkn.pivse;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

import de.brkn.pivse.amazon.ItemFetcher;
import de.brkn.pivse.amazon.ItemSearch;
import de.brkn.pivse.amazon.SimpleAmazonCategoryCrawler;
import de.brkn.pivse.db.DatabaseConnector;
import de.brkn.pivse.db.DatabaseHandler;
import de.brkn.pivse.domain.Category;

/**
 * Hello world!
 * 
 */
public class App {

	public static void main(String[] args) {
		
		ItemFetcher fetcher = new ItemFetcher(Category.ALL);
		try {
			fetcher.fetchItem("B00172HYNQ", 0);
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// ItemFetcher test
		// ItemFetcher fetcher = new ItemFetcher(Category.FILME);
		// try {
		// fetcher.fetchItem("B00H39UUAG", 0);
		// } catch (InvalidKeyException e) {
		// e.printStackTrace();
		// } catch (IllegalArgumentException e) {
		// e.printStackTrace();
		// } catch (UnsupportedEncodingException e) {
		// e.printStackTrace();
		// } catch (NoSuchAlgorithmException e) {
		// e.printStackTrace();
		// }

		// ratings test

		// String title = "Mein Name ist Nobody";
		// DatabaseHandler handler = new DatabaseHandler(
		// DatabaseConnector.getConnection());
		// List<Movie> movies = handler.getMovies("");
		// RTRating rtRating;
		//
		// for (Movie movie : movies) {
		// if (movie.getTitle().equals(title)) {
		// System.out.println(movie.getId() + " title: " + movie.getTitle()
		// + "; imdbTitle: " + movie.getImdbTitle()
		// + "; originalTitle: " + movie.getOriginalTitle()
		// + "; year: " + movie.getYear());
		// rtRating = RTRatingFetcher.getRating(movie);
		// if (null != rtRating) {
		// System.out.print("; c_rating: "
		// + rtRating.getCriticsRating() + "; c_score: "
		// + rtRating.getCriticsScore());
		// System.out.println("; a_rating: "
		// + rtRating.getAudienceRating() + "; a_score: "
		// + rtRating.getAudienceScore());
		// }
		//
		// try {
		// Thread.sleep(200);
		// } catch (InterruptedException e) {
		// e.printStackTrace();
		// }
		// }
		// }

//		DatabaseHandler handler = new DatabaseHandler(
//				DatabaseConnector.getConnection());
		// handler.clearAllTables();
		//
//		System.out.println("Starting crawling");
//		SimpleAmazonCategoryCrawler crawler = new SimpleAmazonCategoryCrawler(
//				Category.FILME, true);
//		crawler.startFetching();
//		System.out.println("Writing movies to db");
//		HashMap<String, String> titles = crawler.getTitles();
		
		// List<Movie> movies = handler.getMovies("");
		// List<String> asinList = new ArrayList<String>();
		// HashMap<String, String> titles = new HashMap<String, String>();
		// for (Movie movie : movies) {
		// asinList.add(movie.getAsin());
		// // titles.put(movie.getAsin(), movie.getTitle());
		// }

		// final File folder = new File("/data/amazon/filme");
		// try {
		//
		// for (final File fileEntry : folder.listFiles()) {
		// BufferedReader reader = new BufferedReader(new FileReader(
		// fileEntry));
		//
		// String line;
		// while ((line = reader.readLine()) != null) {
		// String asin = line.substring(0, 10);
		// if (!asinList.contains(asin)) {
		// String title = line.substring(13);
		// titles.put(asin, title);
		// }
		// }
		// reader.close();
		// }
		// } catch (FileNotFoundException e) {
		// e.printStackTrace();
		// } catch (IOException e) {
		// e.printStackTrace();
		// }

		// System.out.println("Writing " + titles.size() + " movies to db");
		// MoviesDatabaseWriter.writeMoviesToDatabase(titles, false);

		// DatabaseHandler handler = new DatabaseHandler(
		// DatabaseConnector.getConnection());
		//
		// handler.clearRTRatingsTable();
		// handler.clearIMDBRatingsTable();
		// List<Movie> movies = handler.getMovies("!ratings");
		// RTRating rtRating;
		// IMDBRating imdbRating;
		//
		// System.out.println("Writing " + movies.size() +
		// " movie ratings to db");
		// for (Movie movie : movies) {
		// System.out.print(movie.getId() + " title: " + movie.getTitle()
		// + "; imdbTitle: " + movie.getImdbTitle()
		// + "; originalTitle: " + movie.getOriginalTitle()
		// + "; year: " + movie.getYear());
		// rtRating = RTRatingFetcher.getRating(movie);
		// imdbRating = IMDBRatingFetcher.getRating(movie);
		// if (null != rtRating) {
		// System.out.print("; c_rating: " + rtRating.getCriticsRating()
		// + "; c_score: " + rtRating.getCriticsScore());
		// System.out.print("; a_rating: " + rtRating.getAudienceRating()
		// + "; a_score: " + rtRating.getAudienceScore());
		// handler.insertMovieRTRating(rtRating);
		// }
		// if (null != imdbRating) {
		// System.out.print("; imdb_rating: " + imdbRating.getRating()
		// + "; users: " + imdbRating.getUsers());
		// handler.insertMovieIMDBRating(imdbRating);
		// }
		// try {
		// Thread.sleep(200);
		// } catch (InterruptedException e) {
		// e.printStackTrace();
		// }
		// System.out.println();
		// }

	}
}
