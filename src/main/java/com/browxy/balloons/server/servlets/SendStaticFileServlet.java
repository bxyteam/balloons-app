package com.browxy.balloons.server.servlets;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.browxy.balloons.domain.Application;
import com.browxy.balloons.domain.config.ProjectConfig;
import com.browxy.balloons.util.FileManager;
import com.browxy.balloons.util.MimeTypeUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

public class SendStaticFileServlet extends HttpServlet {

  private static final long serialVersionUID = 1L;
  private final Logger logger = LoggerFactory.getLogger(SendStaticFileServlet.class);

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    String requestUri = req.getRequestURI();
    if (redirect(requestUri)) {
      req.getRequestDispatcher(req.getRequestURI()).forward(req, resp);
      return;
    }
    try {
      String filePath = requestUri.startsWith("/assets")
          ? Application.getInstance().getBalloonWebConfig().getStaticPath() + requestUri
          : Application.getInstance().getBalloonWebConfig().getStaticPath() + File.separator
              + Application.getInstance().getBalloonWebConfig().getStaticFile();
      File file = new File(filePath);
      String mimeType = MimeTypeUtil.getMimeTypeByFileName(file.getName());
      if (file.exists()) {
        String content = new String(Files.readAllBytes(file.toPath()));
        if (mimeType.equals("text/html")) {
          HttpSession session = req.getSession(false);
          if (session == null) {
            session = req.getSession(true);
          }
          content = buildHtmlProjectMetadata(content);
        }
        resp.setContentType(mimeType);
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.getWriter().write(content);
      } else {
        resp.setContentType("text/html");
        resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Static file not found!");
      }
    } catch (Exception e) {
      logger.error("unable to send static content", e);
      resp.setContentType("text/html");
      resp.sendError(HttpServletResponse.SC_NOT_FOUND, "unable to send static content");
    } finally {
      resp.flushBuffer();
      resp.getWriter().close();
    }
  }

  private String buildHtmlProjectMetadata(String content) {
    ProjectConfig projectConfig = null;
    Gson gson = new GsonBuilder().serializeNulls().disableHtmlEscaping().create();
    try {

      String path = Application.getInstance().getBalloonWebConfig().getBasePath() + File.separator
          + "metadata" + File.separator + "project.json";
      String metadata = FileManager.readFile(path, "UTF-8");
      logger.info(metadata);
      projectConfig = gson.fromJson(metadata, ProjectConfig.class);
      projectConfig.setSocketPort(9191);
      projectConfig.setEntryPoint(Application.getInstance().getBalloonWebConfig().getEntryPoint());
      projectConfig.setCompilerContextService("http");
      projectConfig.setEmbedded(true);

    } catch (Exception e) {
      logger.error("unable to build project metatdata content", e);
    }
    String conf =
        projectConfig != null ? gson.toJson(projectConfig) : gson.toJson(new JsonObject());
    logger.info(conf);
    return content.replace("%%__CONFIG__%%", conf);
  }
  
  private boolean redirect(String uri) {
    return uri.startsWith("/api") || uri.startsWith("/admin");
  }
}
