package de.brkn.pivse.amazon;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.google.common.base.CharMatcher;

import de.brkn.pivse.domain.AmazonItem;
import de.brkn.pivse.domain.Category;
import de.brkn.pivse.domain.Movie;
import de.brkn.pivse.domain.Series;
import de.brkn.pivse.utils.PropertiesReader;
import de.brkn.pivse.utils.SignedRequestsHelper;

public class ItemFetcher {

	private String asin;
	private PropertiesReader conf;
	private long category;
	private int id;

	public ItemFetcher(long category) {
		this.category = category;
		this.conf = PropertiesReader.INSTANCE;
	}

	public AmazonItem fetchItem(String asin, int id)
			throws InvalidKeyException, IllegalArgumentException,
			UnsupportedEncodingException, NoSuchAlgorithmException {
		this.asin = asin;
		this.id = id;
		return execute();
	}

	private AmazonItem execute() throws InvalidKeyException,
			IllegalArgumentException, UnsupportedEncodingException,
			NoSuchAlgorithmException {
		SignedRequestsHelper helper = SignedRequestsHelper.getInstance(
				"webservices.amazon.de", conf.get("AWS_KEY"),
				conf.get("SECRET_KEY"));

		Map<String, String> params = new HashMap<String, String>();
		params.put("Service", "AWSECommerceService");
		params.put("Version", "2013-08-01");
		params.put("Operation", "ItemLookup");
		params.put("IdType", "ASIN");
		params.put("ItemId", asin);
		params.put("AssociateTag", conf.get("ASSOCIATE_TAG"));
		params.put("ResponseGroup", "Large");

		String url = helper.sign(params);
		try {
			Document response = getResponse(url);
			printResponse(response);
			return createItem(response);
		} catch (Exception ex) {
			Logger.getLogger(ItemSearch.class.getName()).log(Level.SEVERE,
					null, ex);
		}
		return null;
	}

	private AmazonItem createItem(Document doc) {
		String detailPageUrl = "";
		String audienceRating = "";
		String releaseDate = "";
		ArrayList<String> directors = new ArrayList<String>();
		String runningTime = "";
		String studio = "";
		Node firstItem = doc.getElementsByTagName("Item").item(0);
		if (null != firstItem) {
			if (Category.SERIEN == category) {
				NodeList nodes = firstItem.getChildNodes();
				for (int i = 0; i < nodes.getLength(); i++) {
					Node node = nodes.item(i);
					if ("DetailPageURL".equals(node.getNodeName())) {
						detailPageUrl = node.getTextContent();
					} else if ("ItemAttributes".equals(node.getNodeName())) {
						NodeList attributes = node.getChildNodes();
						for (int j = 0; j < attributes.getLength(); j++) {
							Node attribute = attributes.item(j);
							if ("AudienceRating"
									.equals(attribute.getNodeName())) {
								audienceRating = attribute.getTextContent();
							} else if ("ReleaseDate".equals(attribute
									.getNodeName())) {
								releaseDate = attribute.getTextContent();
							}
						}
					}
				}

				int aRating = 0;
				if (audienceRating.contains("6")) {
					aRating = 6;
				} else if (audienceRating.contains("12")) {
					aRating = 12;
				} else if (audienceRating.contains("16")) {
					aRating = 16;
				}

				Series series = new Series(id, asin);
				series.setAudienceRating(aRating);
				series.setDetailPageUrl(detailPageUrl);
				series.setReleaseDate(releaseDate);
				return series;
			} else if (Category.FILME == category) {
				NodeList nodes = firstItem.getChildNodes();
				for (int i = 0; i < nodes.getLength(); i++) {
					Node node = nodes.item(i);
					if ("DetailPageURL".equals(node.getNodeName())) {
						detailPageUrl = trim(node.getTextContent());
					} else if ("ItemAttributes".equals(node.getNodeName())) {
						NodeList attributes = node.getChildNodes();
						for (int j = 0; j < attributes.getLength(); j++) {
							Node attribute = attributes.item(j);
							if ("AudienceRating"
									.equals(attribute.getNodeName())) {
								audienceRating = attribute.getTextContent();
							} else if ("ReleaseDate".equals(attribute
									.getNodeName())) {
								releaseDate = trim(attribute.getTextContent());
							} else if ("Director".equals(attribute
									.getNodeName())) {
								directors.add(trim(attribute.getTextContent()));
							} else if ("RunningTime".equals(attribute
									.getNodeName())) {
								runningTime = attribute.getTextContent();
							} else if ("Studio".equals(attribute.getNodeName())) {
								studio = trim(attribute.getTextContent());
							}
						}
					}
				}

				int aRating = 0;
				if (audienceRating.contains("6")) {
					aRating = 6;
				} else if (audienceRating.contains("12")) {
					aRating = 12;
				} else if (audienceRating.contains("16")) {
					aRating = 16;
				}

				Movie movie = new Movie(id, asin);
				movie.setAudienceRating(aRating);
				movie.setDetailPageUrl(detailPageUrl);
				movie.setReleaseDate(releaseDate);
				movie.setDirectors(directors);
				movie.setRunningTime(Integer.parseInt(runningTime));
				movie.setStudio(studio);
				return movie;
			}

		}

		return null;
	}

	private static Document getResponse(String url)
			throws ParserConfigurationException, IOException, SAXException {
		DocumentBuilder builder = DocumentBuilderFactory.newInstance()
				.newDocumentBuilder();
		Document doc = builder.parse(url);
		return doc;
	}

	@SuppressWarnings("unused")
	private static void printResponse(Document doc)
			throws TransformerException, FileNotFoundException {
		Transformer trans = TransformerFactory.newInstance().newTransformer();
		Properties props = new Properties();
		props.put(OutputKeys.INDENT, "yes");
		trans.setOutputProperties(props);
		StreamResult res = new StreamResult(new StringWriter());
		DOMSource src = new DOMSource(doc);
		trans.transform(src, res);
		String toString = res.getWriter().toString();
		System.out.println(toString);
	}

	private String trim(String text) {
		return CharMatcher.WHITESPACE.trimFrom(text);
	}

}
