package com.browxy.balloons.run;


import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//import com.browxy.balloons.domain.Application;
import com.browxy.balloons.util.FileManager;
import com.browxy.balloons.util.NanoIdUtils;
import java.io.File;

public class SessionCleanupListener implements HttpSessionListener {
  private static final Logger logger = LoggerFactory.getLogger(SessionCleanupListener.class);


  public SessionCleanupListener() {
    logger.info("SessionCleanupListener instantiated.");
  }

  @Override
  public void sessionCreated(HttpSessionEvent se) {
    HttpSession session = se.getSession();
    String alias = NanoIdUtils.randomNanoId();
    //FileManager.createDirectory(Application.getInstance().getSatelliteWebConfig().getBasePath()
      //  + File.separator + "share" + File.separator + alias);
    session.setAttribute("alias", alias);

    logger.info("New session created: " + session.getId() + " with alias: " + alias);
  }

  @Override
  public void sessionDestroyed(HttpSessionEvent se) {
    HttpSession session = se.getSession();
    String alias = (String) session.getAttribute("alias");

    logger.info("Session destroyed: " + session.getId());
    if (alias != null) {
      deleteFolderOnSessionEnd(alias);
    }
  }

  private void deleteFolderOnSessionEnd(String alias) {

    File folder = new File("");
        //new File(Application.getInstance().getSatelliteWebConfig().getBasePath()
        //+ File.separator + "share" + File.separator + alias);

    if (folder.exists() && folder.isDirectory()) {
      deleteFolderRecursively(folder);
      logger.info("Folder deleted successfully. " + folder);
    } else {
      logger.warn("Folder does not exist or is not a directory.");
    }
  }

  private void deleteFolderRecursively(File folder) {
    File[] files = folder.listFiles();
    if (files != null) {
      for (File file : files) {
        if (file.isDirectory()) {
          deleteFolderRecursively(file);
        } else {
          file.delete();
        }
      }
    }
    folder.delete();
  }
}
