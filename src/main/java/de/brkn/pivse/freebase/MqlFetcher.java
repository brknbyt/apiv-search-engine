package de.brkn.pivse.freebase;

import java.io.IOException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.jayway.jsonpath.JsonPath;

import de.brkn.pivse.utils.PropertiesReader;

public class MqlFetcher {

	private PropertiesReader conf;
	private GenericUrl url = new GenericUrl(
			"https://www.googleapis.com/freebase/v1/mqlread");
	private JSONObject response;

	// freebase.properties
	public MqlFetcher(String propertiesFile) {
		this.conf = PropertiesReader.INSTANCE;
	}

	public boolean sendRequest(String query) {
		try {
			url.put("key", conf.get("FREEBASE_KEY"));
			url.put("query", query);
			
			JSONParser parser = new JSONParser();
			HttpTransport httpTransport = new NetHttpTransport();
			HttpRequestFactory requestFactory = httpTransport
					.createRequestFactory();
			HttpRequest request = requestFactory.buildGetRequest(url);
			HttpResponse httpResponse = request.execute();
			response = (JSONObject) parser.parse(httpResponse.parseAsString());
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return true;
	}

	public JSONArray getResults() {
		return (JSONArray) response.get("result");
	}

	public String getMid(Object result) {
		return JsonPath.read(result, "$.mid").toString();
	}
	
	public String getId(Object result) {
		return JsonPath.read(result, "$.id").toString();
	}
	
}
