package de.brkn.pivse.amazon;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
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

import de.brkn.pivse.utils.PropertiesReader;
import de.brkn.pivse.utils.SignedRequestsHelper;

public class ItemSearch {

	private String keywords;
	private long type;
	private PropertiesReader conf;
	private Document response;

	public ItemSearch(String keywords, long type) {
		this.keywords = keywords;
		this.type = type;
		this.conf = PropertiesReader.INSTANCE;
	}

	public void execute() throws InvalidKeyException, IllegalArgumentException,
			UnsupportedEncodingException, NoSuchAlgorithmException {
		SignedRequestsHelper helper = SignedRequestsHelper.getInstance(
				"webservices.amazon.de", conf.get("AWS_KEY"),
				conf.get("SECRET_KEY"));

		Map<String, String> params = new HashMap<String, String>();
		params.put("Service", "AWSECommerceService");
		params.put("Version", "2013-08-01");
		params.put("Operation", "ItemSearch");
		params.put("SearchIndex", "Electronics");
		params.put("BrowseNode", type + "");
		params.put("Keywords", keywords);
		params.put("AssociateTag", conf.get("ASSOCIATE_TAG"));
		params.put("ResponseGroup", "ItemIds");
		params.put("ItemPage", "1");

		String url = helper.sign(params);
		try {
			response = getResponse(url);
//			 printResponse(response);
		} catch (Exception ex) {
			Logger.getLogger(ItemSearch.class.getName()).log(Level.SEVERE,
					null, ex);
		}
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

	public int getTotalResults() {
		int totalResults = 0;
		NodeList nList = response.getElementsByTagName("TotalResults");
		if (0 != nList.getLength()) {
			Node totalResultsNode = nList.item(0);
			totalResults = Integer.parseInt(totalResultsNode.getTextContent());
		}
		return totalResults;
	}

}
