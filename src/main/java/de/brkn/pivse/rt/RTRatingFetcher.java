package de.brkn.pivse.rt;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import de.brkn.pivse.domain.Movie;
import de.brkn.pivse.domain.RTRating;
import de.brkn.pivse.imdb.IMDBTitleScraper;
import de.brkn.pivse.utils.PropertiesReader;

public class RTRatingFetcher {

	private static int id;
	private static int imdbYear;
	private static List<String> dirs;

	public static RTRating getRating(Movie movie) {
		id = movie.getId();
		dirs = movie.getDirectors();
		imdbYear = movie.getYear();
		RTRating rating = null;
		if ((rating = getRating(movie.getOriginalTitle())) != null) {
//			if (movie.getOriginalTitle() != null
//					&& !movie.getOriginalTitle().isEmpty()) {
			
			if (null != rating) {
				return rating;
			}
		} else if ((rating = getRating(movie.getImdbTitle())) != null) {
//		} else if (movie.getImdbTitle() != null
//				&& !movie.getImdbTitle().isEmpty()) {
			rating = getRating(movie.getImdbTitle());
			if (null != rating) {
				return rating;
			}
		} else {
			String[] altTitles = movie.getAlternativeTitles();
			for (int i = 0; i < altTitles.length; i++) {
				rating = getRating(altTitles[i]);
				if (null != rating) {
					return rating;
				}
			}
		}
		return null;
	}
	
	private static RTRating getRating(String title) {
		JSONObject jsonResponse;
		jsonResponse = fetchJson(title);
		RTRating rating = null;
		if (jsonResponse.containsKey("movies") && hasRatings(jsonResponse)) {
			rating = parseJson(jsonResponse);
		}
		return rating;
	}

	private static JSONObject fetchJson(String query) {
		PropertiesReader conf = PropertiesReader.INSTANCE;

		CloseableHttpClient httpClient = null;
		CloseableHttpResponse response = null;
		URI uri = null;
		try {
			httpClient = HttpClients.createDefault();
			uri = new URIBuilder().setScheme("http")
					.setHost("api.rottentomatoes.com")
					.setPath("/api/public/v1.0/movies.json")
					.setParameter("q", query).setParameter("page_limit", "50")
					.setParameter("page", "1")
					.setParameter("apikey", conf.get("RT_KEY")).build();
			HttpGet httpGet = new HttpGet(uri);
			response = httpClient.execute(httpGet);

			if (200 == response.getStatusLine().getStatusCode()) {
				HttpEntity entity = response.getEntity();
				String jsonResponse = EntityUtils.toString(entity);
				// System.out.println(jsonResponse);
				JSONParser jParser = new JSONParser();
				return (JSONObject) jParser.parse(jsonResponse);
			}
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
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
		return null;
	}

	private static RTRating parseJson(JSONObject json) {
		JSONArray moviesArray = (JSONArray) json.get("movies");
		for (int i = 0; i < moviesArray.size(); i++) {
			JSONObject movie = (JSONObject) moviesArray.get(i);
			String rtId = (String) movie.get("id");
			String[] rtDirectors = RTMovieDetailFetcher.getDirectors(rtId);
			if (null != rtDirectors) {
				if (IMDBTitleScraper.OriginalTitleScraper
						.containsDirector(
								dirs,
								new ArrayList<String>(Arrays
										.asList(rtDirectors)))) {
					return createRating(id, (JSONObject) movie.get("ratings"));
				}
			} else {
				Object yearObj = movie.get("year");
				long longYear = -1;
				if (yearObj instanceof String) {
					String yearS = (String) yearObj;
					if (!yearS.isEmpty()) {
						longYear = Long.parseLong((String) yearObj);
					}
				} else if (yearObj instanceof Long) {
					longYear = (Long) yearObj;
				}
				int rtYear = (int) longYear;
				if (imdbYear == rtYear) {
					return createRating(id, (JSONObject) movie.get("ratings"));
				}
			}
		}
		if (moviesArray.size() > 0) {
			JSONObject movie = (JSONObject) moviesArray.get(0);
			return createRating(id, (JSONObject) movie.get("ratings"));
		}
		return null;
	}

	private static boolean hasRatings(JSONObject json) {
		JSONArray moviesArray = (JSONArray) json.get("movies");
		if (moviesArray.size() > 0) {
			return true;
		}
		return false;
	}

	private static RTRating createRating(int id, JSONObject ratings) {
		RTRating rtRating = new RTRating(id);

		String cRating = (String) ratings.get("critics_rating");
		long cScore = (Long) ratings.get("critics_score");
		String aRating = (String) ratings.get("audience_rating");
		long aScore = (Long) ratings.get("audience_score");

		rtRating.setCriticsRating(cRating);
		rtRating.setCriticsScore(cScore);
		rtRating.setAudienceRating(aRating);
		rtRating.setAudienceScore(aScore);

		return rtRating;
	}

	public static class RTMovieDetailFetcher {

		public static String[] getDirectors(String rtId) {
			JSONObject jsonResponse;
			jsonResponse = fetchJson(rtId);
			String[] directors = null;
			if (jsonResponse != null && jsonResponse.containsKey("id")) {
				directors = parseDetailJson(jsonResponse);
			}
			return directors;
		}

		private static JSONObject fetchJson(String rtId) {
			PropertiesReader conf = PropertiesReader.INSTANCE;

			CloseableHttpClient httpClient = null;
			CloseableHttpResponse response = null;
			URI uri = null;
			try {
				httpClient = HttpClients.createDefault();
				uri = new URIBuilder().setScheme("http")
						.setHost("api.rottentomatoes.com")
						.setPath("/api/public/v1.0/movies/" + rtId + ".json")
						.setParameter("apikey", conf.get("RT_KEY")).build();
				HttpGet httpGet = new HttpGet(uri);
				response = httpClient.execute(httpGet);

				if (200 == response.getStatusLine().getStatusCode()) {
					HttpEntity entity = response.getEntity();
					String jsonResponse = EntityUtils.toString(entity);
					// System.out.println(jsonResponse);
					JSONParser jParser = new JSONParser();
					return (JSONObject) jParser.parse(jsonResponse);
				}
			} catch (URISyntaxException e) {
				e.printStackTrace();
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ParseException e) {
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
			System.err.println("Error: " + rtId);
			return null;
		}

		private static String[] parseDetailJson(JSONObject json) {
			String[] directors = null;
			if (json.containsKey("abridged_directors")) {
				JSONArray dirArray = (JSONArray) json.get("abridged_directors");
				directors = new String[dirArray.size()];
				for (int i = 0; i < dirArray.size(); i++) {
					JSONObject dir = (JSONObject) dirArray.get(i);
					if (dir.containsKey("name")) {
						directors[i] = ((String) dir.get("name")).toLowerCase();
					}
				}
			}
			return directors;
		}

	}

}
