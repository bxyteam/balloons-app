package com.browxy.balloons.server.servlets.api.v1;

import java.io.IOException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.browxy.balloons.domain.wspr.WsprNetwork;
import com.browxy.balloons.domain.wspr.WsprQueryParams;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class WsprNetworkServlet  extends HttpServlet {
  private static final long serialVersionUID = 1L;

  private static final Logger logger = LoggerFactory.getLogger(WsprNetworkServlet.class);
  private Gson gson;
  
  public WsprNetworkServlet() {
    this.gson = new GsonBuilder().disableHtmlEscaping().serializeNulls().create();
  }
  
  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
      response.setContentType("text/plain");
      try {
          WsprQueryParams wsprQueryParams = new WsprQueryParams();

          wsprQueryParams.setBand(request.getParameter("band"));
          wsprQueryParams.setCount(request.getParameter("count"));
          wsprQueryParams.setCall(request.getParameter("call"));
          wsprQueryParams.setReporter(request.getParameter("reporter"));
          wsprQueryParams.setTimeLimit(request.getParameter("timeLimit"));
          wsprQueryParams.setSortBy(request.getParameter("sortBy"));
          wsprQueryParams.setSortRev(request.getParameter("sortRev"));
          wsprQueryParams.setUnique(request.getParameter("unique"));
          wsprQueryParams.setMode(request.getParameter("mode"));
          wsprQueryParams.setExcludeSpecial(request.getParameter("excludeSpecial"));

          WsprNetwork wsprNetwork = new WsprNetwork();
          String result = wsprNetwork.runWSPRQuery(wsprQueryParams);
          response.setStatus(HttpServletResponse.SC_OK);
          response.getWriter().write(result);
      } catch (Exception ex) {
          logger.error("Error in doGet wspr network context service", ex);
          String errorMessage = (ex.getMessage() != null && !ex.getMessage().trim().isEmpty())
                  ? ex.getMessage()
                  : "An error has occurred in the connection";
          response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
          response.getWriter().write(errorMessage);
      } finally {
          try {
              response.flushBuffer();
              response.getWriter().close();
          } catch (IOException e) {
              logger.error("Error closing response", e);
          }
      }
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response) {
      response.setContentType("text/plain");
      try {
          String message = request.getReader().readLine();
          logger.info(message);
          WsprQueryParams wsprQueryParams = this.gson.fromJson(message, WsprQueryParams.class);
          WsprNetwork wsprNetwork = new WsprNetwork();
          String result = wsprNetwork.runWSPRQuery(wsprQueryParams);
          response.setStatus(HttpServletResponse.SC_OK);
          response.getWriter().write(result);
      } catch (Exception ex) {
          logger.error("error wspr network context service ", ex);
          String errorMessage = ex.getMessage() != null || !ex.getMessage().trim().equals("") ? ex.getMessage()
                  : "An error has occurred in the connection";
          response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
          try {
              response.getWriter().write(errorMessage);
          } catch (IOException e) {
              logger.error("error catch wspr network context service ", e);

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
}

