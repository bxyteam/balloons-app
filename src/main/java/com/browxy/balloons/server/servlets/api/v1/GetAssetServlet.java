package com.browxy.balloons.server.servlets.api.v1;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.browxy.balloons.domain.Application;
import com.browxy.balloons.util.MimeTypeUtil;

public class GetAssetServlet extends HttpServlet {

  private static final long serialVersionUID = 1L;
  private final Logger logger = LoggerFactory.getLogger(GetAssetServlet.class);

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    String fileName = request.getParameter("file");
    if (fileName == null || fileName.trim().isEmpty()) {
      response.sendError(404,
          "This programs is trying to open the file: '" + fileName + "' but is empty.");
      return;
    }

    String path =
        Application.getInstance().getBalloonWebConfig().getBasePath() + File.separator + fileName;
    File file = new File(path);

    if (!file.exists() || !file.isFile()) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.setHeader("Access-Control-Allow-Origin", "*");
      response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
      response.sendError(404,
          "This programs is trying to open the file: '/" + fileName + "' but it does not exist");
      return;
    }

    InputStream is = null;
    try {
      is = new FileInputStream(file);
      String mimeType = MimeTypeUtil.getMimeTypeByFileName(file.getName());
      response.setStatus(HttpServletResponse.SC_OK);
      response.setContentLengthLong(file.length());
      response.setHeader("Access-Control-Allow-Origin", "*");
      response.setHeader("Access-Control-Expose-Headers", "Content-Disposition,Content-Length");
      response.setContentLengthLong(file.length());
      response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
      response.setContentType(mimeType);
      sendData(is, response);
    } catch (Exception e) {
      logger.error("error downloading asset", e);
      response.setHeader("Access-Control-Allow-Origin", "*");
      response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
      response.sendError(500, "unable to download asset");
    } finally {
      response.flushBuffer();
      response.getOutputStream().close();
      if (is != null)
        is.close();
    }

  }

  private void sendData(InputStream is, HttpServletResponse response) throws IOException {
    OutputStream os = response.getOutputStream();
    byte[] buf = new byte[1000];
    for (int nChunk = is.read(buf); nChunk != -1; nChunk = is.read(buf)) {
      os.write(buf, 0, nChunk);
    }
  }

}
