package com.browxy.balloons.domain;


import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.browxy.balloons.domain.github.GithubRepoDownloader;

public class GithubSyncManager {
  private Logger logger = LoggerFactory.getLogger(GithubSyncManager.class);

  public GithubSyncManager() {
    logger.info("Starting Balloons Github remote repo manager.");
    
    DailyScriptScheduler dailyScriptScheduler = new DailyScriptScheduler();
    
    if (Application.getInstance().getBalloonGithubConfig().getGithubToken() == null
        || Application.getInstance().getBalloonGithubConfig().getGithubToken().isEmpty()) {
      GithubRepoDownloader githubRepoDownloader = new GithubRepoDownloader();
      try {
        githubRepoDownloader.download();
      } catch (IOException | InterruptedException e) {
        logger.error("Error download and unzip github project",e);
      }
      
    } else {
      dailyScriptScheduler.syncGithub();
    }
    
    dailyScriptScheduler.startSchedule();
  }


}


