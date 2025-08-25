package com.browxy.balloons.server.servlets.api.v1;

import java.io.*;
import java.net.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class WebFetcherServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;

  private static final Logger logger = LoggerFactory.getLogger(WebFetcherServlet.class);
  private final String USER_AGENT = "Mozilla/5.0";
  
  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response) {
    response.setContentType("text/plain");

    String targetUrl = request.getParameter("url");

    try {
      if (targetUrl == null || targetUrl.trim().isEmpty()) {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.getWriter().write("Missing 'url' parameter");
        return;
      }
      String result = fetchCallData(targetUrl);

      response.setContentType("text/plain");
      response.setCharacterEncoding("UTF-8");
      response.getWriter().write(result);

    } catch (Exception ex) {
      logger.error("error findu network context service ", ex);
      String errorMessage =
          ex.getMessage() != null || !ex.getMessage().trim().equals("") ? ex.getMessage()
              : "An error has occurred in the connection";
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      try {
        response.getWriter().write(errorMessage);
      } catch (IOException e) {
        logger.error("error catch findu network context service ", e);

      }
    } finally {
      try {
        response.flushBuffer();
        response.getWriter().close();
      } catch (IOException e) {
        logger.error("Error closing response", e);
      }
    }


  }

  private String fetchCallData(String url) throws IOException {
    URL obj = new URL(url);
    HttpURLConnection conn = (HttpURLConnection) obj.openConnection();
    conn.setRequestMethod("GET");
    conn.setRequestProperty("User-Agent", USER_AGENT);

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
    return response.toString();
  }
}

