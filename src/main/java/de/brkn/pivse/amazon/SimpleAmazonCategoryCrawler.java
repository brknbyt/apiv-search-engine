package de.brkn.pivse.amazon;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

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

import de.brkn.pivse.domain.Category;

public class SimpleAmazonCategoryCrawler {

	// Categories
	private static final String PATH = "/Serien-Prime-bestellbar-Amazon-Instant-Video/s";
	private int page = 1;

	private boolean saveOnDisk;
	private int results;
	private long category;

	private File directoryFile;
	private HashMap<String, String> titles;
	Logger logger = Logger.getLogger("PivseLogger");

	public SimpleAmazonCategoryCrawler(long category) {
		this.category = category;
		this.titles = new HashMap<String, String>();
	}

	public SimpleAmazonCategoryCrawler(long category, boolean saveOnDisk) {
		this.category = category;
		this.titles = new HashMap<String, String>();
		this.saveOnDisk = saveOnDisk;
	}

	private int fetchTotalResults() {
		ItemSearch search = new ItemSearch("", category);
		try {
			search.execute();
		} catch (InvalidKeyException e) {
			logger.log(Level.WARNING, e.getMessage());
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			logger.log(Level.WARNING, e.getMessage());
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			logger.log(Level.WARNING, e.getMessage());
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			logger.log(Level.WARNING, e.getMessage());
			e.printStackTrace();
		}
		return search.getTotalResults();
	}

	public void startFetching() {
		if (saveOnDisk) {
			createDirectory();
		}
		results = fetchTotalResults();
		if (results > 0) {
			while (results > 0) {
				fetchPage(page);
				page++;
			}
		}
	}

	private void fetchPage(int page) {
		CloseableHttpClient httpClient = null;
		CloseableHttpResponse response = null;
		URI uri = null;
		try {
			httpClient = HttpClients.createDefault();
			uri = new URIBuilder().setScheme("http").setHost("www.amazon.de")
					.setPath(PATH).setParameter("rh", "n:" + category)
					.setParameter("sort", "popularity-rank")
					.setParameter("ie", "UTF8").setParameter("page", page + "")
					.build();
			HttpGet httpGet = new HttpGet(uri);
			response = httpClient.execute(httpGet);

			if (200 == response.getStatusLine().getStatusCode()) {
				HttpEntity entity = response.getEntity();
				String htmlResponse = EntityUtils.toString(entity);
				parseTitles(Jsoup.parse(htmlResponse));
				// saveHtml(htmlResponse);
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
	}

	private boolean createDirectory() {
		String directory = "";
		if (Category.FILME == category) {
			directory = "filme/";
		} else if (Category.SERIEN == category) {
			directory = "serien/";
		} else if (Category.ALL == category) {
			directory = "all/";
		}
		directoryFile = new File("/data/amazon/" + directory);
		if(directoryFile.exists()){
			for (final File fileEntry : directoryFile.listFiles()) {
				fileEntry.delete();
			}
		}
		return directoryFile.mkdirs();
	}

	private void parseTitles(Document html) {
		Elements elems = html.getElementsByClass("newaps");
		for (Element elem : elems) {
			Element asinElement = elem.child(0);
			String url = asinElement.attr("href");
			String asin = url.replaceFirst(
					"http://www.amazon.de/.*/dp/(\\w*)/?(.*)", "$1");
			Element title = asinElement.child(0);
			if (saveOnDisk) {
				saveTitle(asin + " : " + title.text());
			}
			saveTitles(asin, title.text());
		}
		results -= elems.size();
	}

	@SuppressWarnings("unused")
	private void saveHtml(String title) {
		try {
			File pageFile = new File(directoryFile, page + ".html");
			if (!pageFile.exists()) {
				pageFile.createNewFile();
			}
			FileWriter writer = new FileWriter(pageFile, true);
			writer.append(title + "\r\n");
			writer.close();
			results -= 60;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void saveTitle(String title) {
		try {
			File pageFile = new File(directoryFile, page + ".txt");
			if (!pageFile.exists()) {
				pageFile.createNewFile();
			}
			FileWriter writer = new FileWriter(pageFile, true);
			writer.append(title + "\r\n");
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void saveTitles(String asin, String title) {
		titles.put(asin, title);
	}

	public HashMap<String, String> getTitles() {
		return titles;
	}

}
