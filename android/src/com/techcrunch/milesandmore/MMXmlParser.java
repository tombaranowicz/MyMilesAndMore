package com.techcrunch.milesandmore;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import com.techcrunch.milesandmore.api.Company;

public class MMXmlParser {

	public static List<Company> parseCompanies(String xml) {// throws
															// XmlPullParserException,
															// IOException,
															// JSONException {
		List<Company> companies = new ArrayList<Company>();
		// XmlPullParser parser = Xml.newPullParser();
		// Log.i("Blou", XML.toJSONObject(xml).toString());
		// parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
		// parser.setInput(new ByteArrayInputStream(xml.getBytes()), null);
		// // parser.nextTag();
		// companies = readCompanies(parser);
		int globalStart = 1;
		while (globalStart < xml.length() && globalStart>-1) {
			int startIndex = xml.indexOf("<article");
			int endIndex = xml.indexOf("</article>", startIndex);
			Company company = new Company();
			company.setImage(readImg(xml.substring(startIndex, endIndex)));
			company.setName(readTitle(xml.substring(startIndex, endIndex)));
			companies.add(company);
			globalStart = xml.indexOf("</article>", endIndex+10);
		}
		return companies;
	}

	private static String readImg(String company) {
		int startIndex = company.indexOf("<img src=") + 10;
		int endIndex = company.indexOf('"', startIndex);
		return company.substring(startIndex, endIndex);
	}

	private static String readTitle(String company) {
		int startIndex = company.indexOf("<h4>") + 5;
		int endIndex = company.indexOf("</h4>", startIndex);
		return company.substring(startIndex, endIndex);
	}

	private static String ns = null;

	private static List<Company> readCompanies(XmlPullParser parser) throws XmlPullParserException, IOException {
		List<Company> companies = new ArrayList<Company>();
		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}
			String name = parser.getName();
			// Starts by looking for the entry tag
			if (name.equals("article")) {
				companies.add(readCompany(parser));
			} else {
				skip(parser);
			}
		}
		return companies;
	}

	private static Company readCompany(XmlPullParser parser) throws XmlPullParserException, IOException {
		Company company = new Company();
		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}
			String name = parser.getName();
			if (name.equals("header")) {
				readHeader(parser, company);
			} else {
				skip(parser);
			}
		}
		return company;
	}

	private static void readHeader(XmlPullParser parser, Company company) throws XmlPullParserException, IOException {
		parser.require(XmlPullParser.START_TAG, ns, "header");
		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}
			String name = parser.getName();
			String classValue = parser.getAttributeValue(null, "class");
			if (name.equals("div") && "logo".equals(classValue)) {
				imageUrl(parser, company);
			} else if (name.equals("h4")) {
				company.setName(readText(parser));
			} else {
				skip(parser);
			}
		}
	}

	private static void imageUrl(XmlPullParser parser, Company company) throws XmlPullParserException, IOException {
		parser.require(XmlPullParser.START_TAG, ns, "div");
		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}
			String name = parser.getName();
			if (name.equals("img")) {
				String url = parser.getAttributeValue(null, "src");
				company.setImage("http://www.miles-and-more-shopfinder.de" + url);
				parser.nextTag();
			} else {
				skip(parser);
			}
		}
	}

	private static String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
		String result = "";
		if (parser.next() == XmlPullParser.TEXT) {
			result = parser.getText();
			parser.nextTag();
		}
		return result;
	}

	private static void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
		if (parser.getEventType() != XmlPullParser.START_TAG) {
			throw new IllegalStateException();
		}
		int depth = 1;
		while (depth != 0) {
			switch (parser.next()) {
			case XmlPullParser.END_TAG:
				depth--;
				break;
			case XmlPullParser.START_TAG:
				depth++;
				break;
			}
		}
	}

}
