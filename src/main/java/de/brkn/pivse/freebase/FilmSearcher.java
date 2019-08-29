package de.brkn.pivse.freebase;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.jayway.jsonpath.JsonPath;

import de.brkn.pivse.domain.Movie;
import de.brkn.pivse.utils.PropertiesReader;

public class FilmSearcher {

	private final String FILM_QUERY = "[{ \"mid\": \"%MID%\", \"limit\": 500, \"id\": null, \"/common/topic/alias\": [], \"name\": null }]";
	private MqlFetcher mqlFetcher;
	private PropertiesReader conf;

	public FilmSearcher(String propertiesFile) {
		this.mqlFetcher = new MqlFetcher(propertiesFile);
		this.conf = PropertiesReader.INSTANCE;
	}

	public String[] fetchAltTitles(Movie movie) {
		String mid = findMid(movie.getImdbTitle());
		if(mid.isEmpty()){
			mid = findMid(movie.getOriginalTitle());
		}
		System.out.println("MID: " + mid);
		if(mid.isEmpty()){
			return new String[]{};
		}
		return fetchTitles(mid);
	}

	private String findMid(String query) {
		try {
			HttpTransport httpTransport = new NetHttpTransport();
			HttpRequestFactory requestFactory = httpTransport
					.createRequestFactory();
			JSONParser parser = new JSONParser();
			GenericUrl url = new GenericUrl(
					"https://www.googleapis.com/freebase/v1/search");
			url.put("query", query);
			url.put("filter", "(all type:/film/film)");
			url.put("limit", "10");
			url.put("indent", "true");
			url.put("key", conf.get("FREEBASE_KEY"));
			HttpRequest request = requestFactory.buildGetRequest(url);
			HttpResponse httpResponse = request.execute();
			JSONObject response = (JSONObject) parser.parse(httpResponse
					.parseAsString());
			JSONArray results = (JSONArray) response.get("result");
			for (Object result : results) {
				return JsonPath.read(result, "$.mid").toString();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return "";
	}

	private String[] fetchTitles(String mid) {
		String[] titles = null;
		while (mqlFetcher.sendRequest(FILM_QUERY.replace("%MID%", mid))) {
			JSONArray results = mqlFetcher.getResults();
			for (Object result : results) {
				String name = JsonPath.read(result, "$.name");
				JSONArray alias = JsonPath
						.read(result, "$./common/topic/alias");
				titles = new String[alias.size() + 1];
				titles[0] = name;
				for (int i = 0; i < alias.size(); i++) {
					titles[i + 1] = alias.get(i).toString();
				}
			}
			return titles;
		}
		return null;
	}

}
