package de.brkn.pivse.mp;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

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

import de.brkn.pivse.domain.Movie;

public class MPTitleScraper {

	private static final String URL = "www.moviepilot.de";
	private static String title;
	private static String originalTitle;
	private static String mpYear;
	private static List<String> directors;
	private static boolean directorMatch;

	public static String getTitle() {
		return title;
	}

	public static String getOriginalTitle() {
		return originalTitle;
	}

	public static String getMpYear() {
		return mpYear;
	}

	public static boolean searchTitle(Movie movie) {
		title = "";
		directors = movie.getDirectors();
		if (searchTitle(movie.getTitle(), true)) {
			return true;
		} else if (searchTitle(movie.getTitle(), false)) {
			return true;
		} else {
			String[] splittetTitles = movie.getTitle().split("(-|:|;)");
			return searchTitle(splittetTitles[0], false);
		}
	}

	public static boolean searchTitle(String query, boolean searchExact) {
		CloseableHttpClient httpClient = null;
		CloseableHttpResponse response = null;
		URI uri = null;
		try {
			httpClient = HttpClients.createDefault();
			uri = new URIBuilder().setScheme("http").setHost(URL)
					.setPath("/suche").setParameter("q", query)
					.setParameter("type", "movie").build();
			HttpGet httpGet = new HttpGet(uri);
			response = httpClient.execute(httpGet);

			if (200 == response.getStatusLine().getStatusCode()) {
				HttpEntity entity = response.getEntity();
				String htmlResponse = EntityUtils.toString(entity);
				parseTitle(Jsoup.parse(htmlResponse));
				if (!title.isEmpty()) {
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
		Elements elems = html.select("div#main.main.search-controller ul");
//		 System.out.println(elems);
		if (elems.size() > 0) {
			Elements results = elems.select("div li.list-item div.list-item-info-poster");
			int length = results.size() > 20 ? 20 : results.size();
			for (int i = 0; i < length; i++) {
				Element result = results.get(i).select("a.h3").first();
				String path = result.attr("href");
				title = OriginalTitleScraper.getTitleFromMp(URL + path);
				// title = OriginalTitleScraper.getTitleFrommp(URL +
				// "/title/tt0133093/?ref_=nv_sr_1");
				if (!title.isEmpty() && directorMatch) {
					return;
				}
			}
		}
		// title = firstTitle;
	}

	private static class OriginalTitleScraper {

		public static String getTitleFromMp(String url) {
			return fetchTitlePage(url);
		}

		private static String fetchTitlePage(String url) {
			title = "";
			CloseableHttpClient httpClient = null;
			CloseableHttpResponse response = null;
			try {
				httpClient = HttpClients.createDefault();
				HttpGet httpGet = new HttpGet("http://" + url);
//				httpGet.addHeader("Accept-Language",
//						"en-US,en;q=0.8,de-DE;q=0.6,de;q=0.4,tr;q=0.2");
				response = httpClient.execute(httpGet);

				if (200 == response.getStatusLine().getStatusCode()) {
					HttpEntity entity = response.getEntity();
					String htmlResponse = EntityUtils.toString(entity);
					return parseTitle(Jsoup.parse(htmlResponse));
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
			return null;
		}

		private static boolean containsDirector(List<String> dirs,
				List<String> mpDirs) {

			if (mpDirs.isEmpty()) {
				return false;
			}
//			System.out.println("MP: " + dirs.toString() + " : " + mpDirs.toString());
			for (String dir : dirs) {
				for (String mpDir : mpDirs) {
					String[] dNames = dir.split(" ");
					String[] idNames = mpDir.split(" ");
					if (dNames.length == idNames.length
							&& mpDirs.contains(dir)) {
						return true;
					} else {
						if (dNames.length > 0
								&& idNames.length > 0
								&& dNames[dNames.length - 1]
										.equals(idNames[idNames.length - 1])) {
							return true;
						} else {
							if (fuzzyNameMatching(dNames, idNames)) {
								return true;
							}
						}
					}
				}
			}
			return false;
		}

		private static boolean fuzzyNameMatching(String[] nameA, String[] nameB) {
//			System.out.println(Lists.newArrayList(nameA).toString() + " "
//					+ Lists.newArrayList(nameB).toString());
			int[] positions = new int[nameA.length];
			int matches = 0;
			for (int i = 0; i < nameA.length; i++) {
				for (int j = 0; j < nameB.length; j++) {
					int lDistance = LevenshteinDistance(nameA[i], nameB[j]);
					float p = lDistance == 0 ? 0 : (float) lDistance
							/ (float) nameA.length;
//					 System.out.println( nameA[i] + " : " + nameB[j] + "; L: "
//					 + LevenshteinDistance(nameA[i], nameB[j]) + "; P: " + p);
					if (lDistance == 0 || p <= 0.5) {
						positions[matches] = j;
						matches++;
					}
				}
			}

			// boolean isSorted = true;
			// for (int i = 1; i < positions.length; i++) {
			// if (positions[i - 1] > positions[i]) {
			// isSorted = false;
			// }
			// }

			float p = (float) matches / nameA.length * matches / nameB.length;
			
//			System.out.println("Matches: " + matches + " ; P: " + p);

			if (matches == nameA.length || p >= 0.5) {
				return true;
			}
			return false;
		}

		public static int LevenshteinDistance(String s0, String s1) {
			int len0 = s0.length() + 1;
			int len1 = s1.length() + 1;

			// the array of distances
			int[] cost = new int[len0];
			int[] newcost = new int[len0];

			// initial cost of skipping prefix in String s0
			for (int i = 0; i < len0; i++)
				cost[i] = i;

			// dynamicaly computing the array of distances

			// transformation cost for each letter in s1
			for (int j = 1; j < len1; j++) {
				// initial cost of skipping prefix in String s1
				newcost[0] = j;

				// transformation cost for each letter in s0
				for (int i = 1; i < len0; i++) {
					// matching current letters in both strings
					int match = (s0.charAt(i - 1) == s1.charAt(j - 1)) ? 0 : 1;

					// computing cost for each transformation
					int cost_replace = cost[i - 1] + match;
					int cost_insert = cost[i] + 1;
					int cost_delete = newcost[i - 1] + 1;

					// keep minimum cost
					newcost[i] = Math.min(Math.min(cost_insert, cost_delete),
							cost_replace);
				}

				// swap cost/newcost arrays
				int[] swap = cost;
				cost = newcost;
				newcost = swap;
			}

			// the distance is the cost for transforming all letters in both
			// strings
			return cost[len0 - 1];
		}

		public static void toLowerCase(List<String> strings) {
			ListIterator<String> iterator = strings.listIterator();
			while (iterator.hasNext()) {
				iterator.set(iterator.next().toLowerCase());
			}
		}

		private static String parseTitle(Document html) {
			ArrayList<String> mpDirs = new ArrayList<String>();
			Elements crew = html.select("#main > div.content_2cols.cast_crew_fds.expander div.person_img_portrait > ul:nth-child(2)");
			
			if(crew.isEmpty()){
				return "";
			}
			Elements dirs = crew.first().select("li h5");
			Elements dirNames = dirs.select("a");

			// for (String director : directors) {
			// System.out.print(CharMatcher.WHITESPACE.trimFrom(director)
			// + " (" + director.length() + ")" + " ; ");
			// }

			// add director names
			for (Element dir : dirNames) {
				// System.out.print(" mp: " + dir.text() + " ("
				// + dir.text().length() + ")" + " ; ");
				mpDirs.add(dir.text());
//				 System.out.println("D: " + dir.text());
			}

			toLowerCase(directors);
			toLowerCase(mpDirs);
			if (directors == null
					|| (directorMatch = containsDirector(directors, mpDirs))) {
				Elements elems = html
						.select("div.fds_movie_header");
				Element yearElement = elems.select("h2 span a").first();
				mpYear = yearElement.text();
				String title = "";
				
				title = elems.select("h1").text();
				String unCleanedTitle = elems.select("h2 span").text().replace("\"", "");
				originalTitle = unCleanedTitle.replaceFirst("^(.+?)\\s\\(\\d+\\).*$", "$1");
				
				return title;
			}
			return "";
		}

	}

}
