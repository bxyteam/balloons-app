package com.browxy.balloons.domain.wspr;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class WsprNetwork {
private List<String> cookies = new ArrayList<String>();
private HttpURLConnection conn;
private final String USER_AGENT = "Mozilla/5.0";

public String runWSPRQuery(WsprQueryParams wsprQueryParams) throws Exception {
  // make sure cookies is turn on
  CookieHandler.setDefault(new CookieManager());

  // build post url and body
  String url = "http://www.wsprnet.org/drupal/wsprnet/spotquery";
  String body = "band=" + wsprQueryParams.getBand() +
      "&mode=" + wsprQueryParams.getMode() +
      "&count=" + wsprQueryParams.getCount() + 
      "&call=" + wsprQueryParams.getCall() + 
      "&reporter=" + wsprQueryParams.getReporter() + 
      "&timelimit=" + wsprQueryParams.getTimeLimit() + 
      "&sortby=" + wsprQueryParams.getSortBy();

  if (wsprQueryParams.getSortRev() != null && wsprQueryParams.getSortRev().equals("1")) {
      body += "&sortrev=1" + wsprQueryParams.getSortRev();
  }

  if (wsprQueryParams.getExcludeSpecial() != null && wsprQueryParams.getExcludeSpecial().equals("1")) {
      body += "&excludespecial=1";
  }

  if (wsprQueryParams.getUnique() != null && wsprQueryParams.getUnique().equals("1")) {
      body += "&unique=1";
  }

  body = body + "&op=Update&form_build_id=form-s_u5B8OgqugxZfyfB55VQnL7c579xu7FJlOL_3IZfi0&form_id=wsprnet_spotquery_form";

  // send a post request to login to the site
  post(url, body);

  // send a post request to login to the site
  String result = get("http://wsprnet.org/drupal/wsprnet/spots");
  
  return result;
}

private String post(String url, String postParams) throws Exception {
  URL obj = new URL(url);
  conn = (HttpURLConnection) obj.openConnection();
  conn.setRequestMethod("POST");
  conn.setRequestProperty("User-Agent", USER_AGENT);
  conn.setRequestProperty("Connection", "keep-alive");
  conn.setRequestProperty("Referer", "https://accounts.google.com/ServiceLoginAuth");
  conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
  conn.setRequestProperty("Content-Length", Integer.toString(postParams.length()));
  conn.setDoOutput(true);
  conn.setDoInput(true);
  DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
  wr.writeBytes(postParams);
  wr.flush();
  wr.close();
  int responseCode = conn.getResponseCode();
  System.out.println("\nSending 'POST' request to URL : " + url);
  System.out.println("Post parameters : " + postParams);
  System.out.println("Response Code : " + responseCode);
  BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
  String inputLine;
  StringBuffer response = new StringBuffer();
  while ((inputLine = in.readLine()) != null) {
    response.append(inputLine);
  }
  in.close();
  return response.toString();
}

private String get(String url) throws Exception {
  URL obj = new URL(url);
  conn = (HttpURLConnection) obj.openConnection();
  conn.setRequestMethod("GET");
  conn.setRequestProperty("User-Agent", USER_AGENT);
  if (cookies != null) {
    for (String cookie : this.cookies) {
      conn.addRequestProperty("Cookie", cookie.split(";", 1)[0]);
    }
  }
  int responseCode = conn.getResponseCode();
  System.out.println("\nSending 'GET' request to URL : " + url);
  System.out.println("Response Code : " + responseCode);
  BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
  String inputLine;
  StringBuffer response = new StringBuffer();
  while ((inputLine = in.readLine()) != null) {
    response.append(inputLine + "\n");
  }
  in.close();
  setCookies(conn.getHeaderFields().get("Set-Cookie"));
  return response.toString();
}

public List<String> getCookies() {
  return cookies;
}

public void setCookies(List<String> cookies) {
  this.cookies = cookies;
}

}
