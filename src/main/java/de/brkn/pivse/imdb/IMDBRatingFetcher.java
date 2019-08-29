package de.brkn.pivse.imdb;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import de.brkn.fuzzynamematcher.FuzzyNameMatcher;
import de.brkn.fuzzynamematcher.StringMetrics;
import de.brkn.pivse.domain.IMDBRating;
import de.brkn.pivse.domain.Movie;

public class IMDBRatingFetcher {

	private static final String URL = "www.imdb.com";
	private static String imdbUsers;
	private static String imdbRating;
	private static boolean isImdbTitle;

	private static List<String> directors;

	public static IMDBRating getRating(Movie movie) {
		IMDBRating rating = null;
		if (searchRating(movie)) {
			rating = new IMDBRating(movie.getId());
			float r = Float.parseFloat(imdbRating);
			long users = Long.parseLong(imdbUsers.replace(",", ""));
			rating.setRating(r);
			rating.setUsers(users);
		}

		return rating;
	}

	private static boolean searchRating(Movie movie) {
		directors = movie.getDirectors();
		if (movie.getImdbTitle() != null && !movie.getImdbTitle().isEmpty()) {
			isImdbTitle = true;
			if (searchRating(movie.getImdbTitle(), true)) {
				return true;
			} else if (searchRating(movie.getImdbTitle(), false)) {
				return true;
			} else {
				return false;
			}
		} else if (searchRating(movie.getOriginalTitle(), true)) {
			return true;
		} else {
			return false;
		}
	}

	public static boolean searchRating(String query, boolean searchExact) {
		CloseableHttpClient httpClient = null;
		CloseableHttpResponse response = null;
		URI uri = null;
		try {
			httpClient = HttpClients.createDefault();
			uri = new URIBuilder().setScheme("http").setHost(URL)
					.setPath("/find").setParameter("q", query)
					.setParameter("s", "tt")
					.setParameter("exact", searchExact + "").build();
			HttpGet httpGet = new HttpGet(uri);
			response = httpClient.execute(httpGet);

			if (200 == response.getStatusLine().getStatusCode()) {
				HttpEntity entity = response.getEntity();
				String htmlResponse = EntityUtils.toString(entity);
				parseTitle(Jsoup.parse(htmlResponse));
				if (!imdbRating.isEmpty() && !imdbUsers.isEmpty()) {
					return true;
				}
			}
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (null != httpClient) {
					httpClient.close();
				}
				if (null != response) {
					response.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	@SuppressWarnings("unused")
	private static void saveHtml(String html) {
		try {
			File pageFile = new File("result.html");
			pageFile.createNewFile();
			FileWriter writer = new FileWriter(pageFile);
			writer.write(html);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void parseTitle(Document html) {
		Elements elems = html.getElementsByClass("findList");
		if (elems.size() > 0) {
			Elements results = elems.select(".findResult");
			int length = results.size() > 20 ? 20 : results.size();
			for (int i = 0; i < length; i++) {
				Element result = results.get(i).select(".result_text a")
						.first();
				String path = result.attr("href");
				if (RatingScraper.getRating(URL + path)) {
					return;
				}
			}
		}
	}

	public static class RatingScraper {

		public static boolean getRating(String url) {
			return fetchRatingPage(url);
		}

		private static boolean fetchRatingPage(String url) {
			CloseableHttpClient httpClient = null;
			CloseableHttpResponse response = null;
			try {
				httpClient = HttpClients.createDefault();
				HttpGet httpGet = new HttpGet("http://" + url);
				httpGet.addHeader("Accept-Language",
						"en-US,en;q=0.8,de-DE;q=0.6,de;q=0.4,tr;q=0.2");
				response = httpClient.execute(httpGet);

				if (200 == response.getStatusLine().getStatusCode()) {
					HttpEntity entity = response.getEntity();
					String htmlResponse = EntityUtils.toString(entity);
					return parseRating(Jsoup.parse(htmlResponse));
				}
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					if (null != httpClient) {
						httpClient.close();
					}
					if (null != response) {
						response.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			return false;
		}

		public static boolean containsDirector(List<String> dirs,
				List<String> imdbDirs) {

			if (imdbDirs.isEmpty()) {
				return false;
			}

			FuzzyNameMatcher levMatcher = new FuzzyNameMatcher(
					StringMetrics.LEVENSHTEIN);

			for (String dir : dirs) {
				for (String imdbDir : imdbDirs) {
					String[] dNames = dir.split("(\\s|-|\\.)");
					String[] idNames = imdbDir.split("(\\s|-|\\.)");
					if (dNames.length == idNames.length
							&& imdbDirs.contains(dir)) {
						return true;
					} else {
						if (dNames.length > 0
								&& idNames.length > 0
								&& dNames[dNames.length - 1]
										.equals(idNames[idNames.length - 1])) {
							return true;
						} else {
							if (levMatcher.matchNames(dir, imdbDir)) {
								return true;
							}
						}
					}
				}
			}
			return false;
		}

		private static boolean parseRating(Document html) {
			ArrayList<String> imdbDirs = new ArrayList<String>();
			Elements dirs = html.select("div[itemprop=director]");
			Elements dirNames = dirs.select("span.itemprop");

			String asNamesString = dirs.text().replaceAll(
					"((.*)(\\(as (.+)\\))+(.*))+", "$4;");
			String[] asNames = asNamesString.split(";");

			// add director names
			for (Element dir : dirNames) {
				imdbDirs.add(dir.text());
			}

			// add as names (alternative director name)
			for (int i = 0; i < asNames.length; i++) {
				if (!asNames[i].contains("Director:")
						&& !asNames[i].contains("Directors:")
						&& !asNames[i].isEmpty()) {
					imdbDirs.add(asNames[i]);
				}
			}

			if (isImdbTitle || directors == null
					|| containsDirector(directors, imdbDirs)) {
				Elements ratingElements = html
						.select("#overview-top > div.star-box.giga-star > div.star-box-details > strong > span[itemprop=ratingValue]");
				Elements usersElements = html
						.select("#overview-top > div.star-box.giga-star > div.star-box-details > a:nth-child(3) > span[itemprop=ratingCount]");
				imdbUsers = usersElements.text();
				imdbRating = ratingElements.text();
				return true;
			}
			return false;
		}

	}

}
