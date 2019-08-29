package de.brkn.pivse;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.CharMatcher;

import de.brkn.pivse.amazon.ItemFetcher;
import de.brkn.pivse.db.DatabaseConnector;
import de.brkn.pivse.db.DatabaseHandler;
import de.brkn.pivse.domain.Category;
import de.brkn.pivse.domain.Series;

public class SeriesDatabaseWriter {

	private final static String REGEX = "(( (OmU|\\[OV\\]))*|(((-.?)*(Staffel|Season)?) (\\d+))*)*$";
	private final static String SEASON_REGEX = ": The Complete (First|Second|Third|Fourth|Fifth) Season";
	private final static Pattern SEASON_PATTERN = Pattern.compile(".*("
			+ SEASON_REGEX + ").*");

	public static void writeSeriesToDatabase(HashMap<String, String> titles) {
		Connection conn = DatabaseConnector.getConnection();
		DatabaseHandler dbHandler = new DatabaseHandler(conn);

		dbHandler.clearSeriesTable();
		Iterator<String> titlesIterator = titles.keySet().iterator();
		int id = 1;
		String title = "";
		String ov = "";
		String season = "";
		Matcher matcher;
		ItemFetcher itemFetcher = new ItemFetcher(Category.SERIEN);
		while (titlesIterator.hasNext()) {
			String asin = (String) titlesIterator.next();
			String displayTitle = titles.get(asin);

			title = "";
			ov = "";
			season = "";
			matcher = SEASON_PATTERN.matcher(displayTitle);

			// check if the season is written textual
			if (matcher.matches()) {
				// extract season text
				String seasonText = matcher.group(1);
				// remove textual season from display title
				displayTitle = displayTitle.replaceFirst(SEASON_REGEX, "");
				// map textual number to numeral
				season = textualSeasonToNumber(seasonText);
			}

			// extract title, ov and season
			title = displayTitle.replaceFirst(REGEX, "");

			String titleFreeLine = displayTitle.replace(title, "");
			ov = titleFreeLine.replaceFirst(REGEX, "$3");
			season = season.isEmpty() ? titleFreeLine.replaceFirst(REGEX, "$8")
					: season;
			// remove empty spaces at title end
			title = CharMatcher.WHITESPACE.trimFrom(title);

			System.out.println(displayTitle + " asin: " + asin);
			Series series = null;
			try {
				series = (Series) itemFetcher.fetchItem(asin, id);
				if (null == series) {
					series = new Series(id, asin);
				}
				series.setTitle(title);
				series.setOv(!ov.isEmpty());
				series.setSeason(season.isEmpty() ? 0 : Integer
						.parseInt(season));
			} catch (InvalidKeyException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			}
			if (null != series) {
				dbHandler.insertSerie(series);
			}

			id++;

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}

	private static String textualSeasonToNumber(String seasonText) {
		if (seasonText.contains("First")) {
			return "1";
		} else if (seasonText.contains("Second")) {
			return "2";
		} else if (seasonText.contains("Third")) {
			return "3";
		} else if (seasonText.contains("Fourth")) {
			return "4";
		} else if (seasonText.contains("Fifth")) {
			return "5";
		}
		return "";
	}

}
