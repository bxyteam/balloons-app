package com.browxy.balloons.server.servlets;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.browxy.balloons.domain.Application;
import com.browxy.balloons.util.MimeTypeUtil;

public class PingServlet extends HttpServlet {
  
  private static final long serialVersionUID = 1L;
  private final Logger logger = LoggerFactory.getLogger(PingServlet.class);

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    try {
      
      String filePath =  Application.getInstance().getBalloonWebConfig().getStaticPath() + File.separator
              + "ping.html";
      File file = new File(filePath);
      String mimeType = MimeTypeUtil.getMimeTypeByFileName(file.getName());
      if (file.exists()) {
        String content = new String(Files.readAllBytes(file.toPath()));
        resp.setContentType(mimeType);
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.getWriter().write(content);
      } else {
        resp.setContentType("text/html");
        resp.sendError(HttpServletResponse.SC_NOT_FOUND, "File not found!");
      }
    } catch (Exception e) {
      logger.error("unable to send pingcontent", e);
      resp.setContentType("text/html");
      resp.sendError(HttpServletResponse.SC_NOT_FOUND, "unable to send html content");
    } finally {
      resp.flushBuffer();
      resp.getWriter().close();
    }
  }

}
