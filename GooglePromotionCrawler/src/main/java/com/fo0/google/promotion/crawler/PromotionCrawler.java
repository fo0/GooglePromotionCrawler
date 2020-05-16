package com.fo0.google.promotion.crawler;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.extern.log4j.Log4j2;

/**
 * inspired from mkyong
 * https://mkyong.com/java/how-to-send-http-request-getpost-in-java/
 */
@Service
@Log4j2
public class PromotionCrawler {

	private List<String> cookies;
	private HttpsURLConnection conn;

	private final String USER_AGENT = "Mozilla/5.0";

	@Value("${crawler.username}")
	private String account;

	@Value("${crawler.password}")
	private String password;

	public boolean isPromotionAvailable() throws Exception, UnsupportedEncodingException {
		try {
			String url = "https://accounts.google.com/ServiceLoginAuth";
			String promotionsWebSite = "https://home.google.com/promotions";

			// 1. Send a "GET" request, so that you can extract the form's data.
			String page = GetPageContent(url);
			String postParams = getFormParams(page, account, password);

			// 2. Construct above post's content and then send a POST request for
			// authentication
			sendPost(url, postParams);

			// 3. success then go to gmail.
			String result = GetPageContent(promotionsWebSite);

			return result.contains("Dieses Angebot ist abgelaufen oder ungültig") ? true : false;
		} catch (Exception e) {
			log.error(e);
		}

		return false;
	}

	private void sendPost(String url, String postParams) throws Exception {
		URL obj = new URL(url);
		conn = (HttpsURLConnection) obj.openConnection();

		// Acts like a browser
		conn.setUseCaches(false);
		conn.setRequestMethod("POST");
		conn.setRequestProperty("Host", "accounts.google.com");
		conn.setRequestProperty("User-Agent", USER_AGENT);
		conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		conn.setRequestProperty("Accept-Language", "de-DE,de;q=0.5");

		for (String cookie : cookies) {
			conn.addRequestProperty("Cookie", cookie.split(";", 1)[0]);
		}

		conn.setRequestProperty("Connection", "keep-alive");
		conn.setRequestProperty("Referer", "https://accounts.google.com/ServiceLoginAuth");
		conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		conn.setRequestProperty("Content-Length", Integer.toString(postParams.length()));

		conn.setDoOutput(true);
		conn.setDoInput(true);

		// Send post request
		DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
		wr.writeBytes(postParams);
		wr.flush();
		wr.close();

		int responseCode = conn.getResponseCode();
		log.info("Sending 'POST' request to URL : " + url);
//		log.info("Post parameters : " + postParams);
		log.info("Response Code : " + responseCode);

		BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}

		in.close();

//		log.info(response.toString());
	}

	private String GetPageContent(String url) throws Exception {
		URL obj = new URL(url);
		conn = (HttpsURLConnection) obj.openConnection();

		// default is GET
		conn.setRequestMethod("GET");

		conn.setUseCaches(false);

		// act like a browser
		conn.setRequestProperty("User-Agent", USER_AGENT);
		conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		conn.setRequestProperty("Accept-Language", "de-DE,de;q=0.5");

		if (cookies != null) {
			for (String cookie : cookies) {
				conn.addRequestProperty("Cookie", cookie.split(";", 1)[0]);
			}
		}
		int responseCode = conn.getResponseCode();
		log.info("Sending 'GET' request to URL : " + url);
		log.info("Response Code : " + responseCode);

		BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();

		// Get the response cookies
		cookies = conn.getHeaderFields().get("Set-Cookie");

		return response.toString();

	}

	public String getFormParams(String html, String username, String password) throws UnsupportedEncodingException {
		log.info("Extracting form's data...");

		Document doc = Jsoup.parse(html);

		// Google form id
		Element loginform = doc.getElementById("gaia_loginform");
		Elements inputElements = loginform.getElementsByTag("input");
		List<String> paramList = new ArrayList<String>();
		for (Element inputElement : inputElements) {
			String key = inputElement.attr("name");
			String value = inputElement.attr("value");

			if (key.equals("Email"))
				value = username;
			else if (key.equals("Passwd"))
				value = password;
			paramList.add(key + "=" + URLEncoder.encode(value, "UTF-8"));
		}

		// build parameters list
		StringBuilder result = new StringBuilder();
		for (String param : paramList) {
			if (result.length() == 0) {
				result.append(param);
			} else {
				result.append("&" + param);
			}
		}
		return result.toString();
	}

}
