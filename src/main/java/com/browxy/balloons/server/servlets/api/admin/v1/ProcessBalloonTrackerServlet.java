package com.browxy.balloons.server.servlets.api.admin.v1;

import java.io.IOException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.browxy.balloons.domain.compiler.message.ApiMessage;
import com.browxy.balloons.domain.compiler.response.ResponseMessageUtil;
import com.browxy.balloons.run.TrackerProcessorRunner;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

public class ProcessBalloonTrackerServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;
  private final Logger logger = LoggerFactory.getLogger(ProcessBalloonTrackerServlet.class);
  private Gson gson;

  public ProcessBalloonTrackerServlet() {
      this.gson = new GsonBuilder().disableHtmlEscaping().serializeNulls().create();
  }
  
  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response) {
      JsonObject json = new JsonObject();
      response.setContentType("application/json");  
      try {
          String body = request.getReader().readLine();
          logger.info(body);
          ApiMessage apiMessage = gson.fromJson(body, ApiMessage.class);
          TrackerProcessorRunner trackerProcessorRunner = new TrackerProcessorRunner();
          String result = trackerProcessorRunner.run(apiMessage);
          json.addProperty("statusCode", 200);
          json.addProperty("message", result);
          response.setStatus(HttpServletResponse.SC_OK);
          response.getWriter().write(new Gson().toJson(json));
      } catch (Exception e) {
          logger.error("error process balloon tracker", e);
          response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
          try {
              response.getWriter().write(ResponseMessageUtil.getStatusMessage("Error process balloon tracker", 400));
          } catch (IOException e1) {
              logger.error("error write response process balloon tracker", e);
          }
      } finally {
          try {
              response.flushBuffer();
              response.getWriter().close();
          } catch (IOException e) {
              logger.error("error close response process balloon tracker", e);
          }

      }
  }
}
