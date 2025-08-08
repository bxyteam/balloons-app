package com.browxy.balloons.server.servlets.api.admin.v1;

import java.io.IOException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.browxy.balloons.domain.Application;
import com.browxy.balloons.domain.compiler.response.ResponseMessageUtil;
import com.browxy.balloons.domain.config.BalloonGithubConfig;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

public class GithubSaveRepoAdminConfServlet extends HttpServlet {

  private static final long serialVersionUID = 1L;

  private static final Logger logger = LoggerFactory.getLogger(GithubSaveRepoAdminConfServlet.class);

  private Gson gson;

  public GithubSaveRepoAdminConfServlet() {
      this.gson = new GsonBuilder().disableHtmlEscaping().serializeNulls().create();
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response) {
      JsonObject json = new JsonObject();
      response.setContentType("application/json");  
      try {
          String body = request.getReader().readLine();

          BalloonGithubConfig balloonGithubConfig = gson.fromJson(body, BalloonGithubConfig.class);
          if(!validate(balloonGithubConfig)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(ResponseMessageUtil.getStatusMessage("Some fields are wrong!", 400));
            return;
          }
          Application.getInstance().setBalloonGithubConfig(balloonGithubConfig);
          json.addProperty("statusCode", 200);
          json.addProperty("message", "Success!");
          response.setStatus(HttpServletResponse.SC_OK);
          response.getWriter().write(new Gson().toJson(json));

      } catch (Exception e) {
          logger.error("get session error ", e);
          response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
          try {
              response.getWriter().write(ResponseMessageUtil.getStatusMessage("Error setting session", 400));
          } catch (IOException e1) {
              logger.error("error response set session ", e);
          }
      } finally {
          try {
              response.flushBuffer();
              response.getWriter().close();
          } catch (IOException e) {
              logger.error("error close response set session", e);
          }

      }
  }
  
  private boolean validate(BalloonGithubConfig balloonGithubConfig) {
    if(balloonGithubConfig.getGithubOwner().trim().isEmpty() 
        || balloonGithubConfig.getGithubRepo().trim().isEmpty() 
        || balloonGithubConfig.getGithubToken().trim().isEmpty()
        || balloonGithubConfig.getLocalRepoPath().trim().isEmpty()) {
      return false;
    }
    return true;
  }
}
