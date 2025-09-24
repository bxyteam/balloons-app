package com.browxy.balloons.domain;

import com.browxy.balloons.domain.config.BalloonGithubConfig;
import com.browxy.balloons.domain.config.BalloonWebConfig;
import com.browxy.balloons.domain.config.Config;
import com.browxy.balloons.util.JSONUtils;


public class Application {
  private static Application instance;
  private BalloonGithubConfig balloonGithubConfig;
  private BalloonWebConfig balloonWebConfig;

  public Application() {
    this.balloonGithubConfig = this.getGithubConfigFromEnv();
    this.balloonWebConfig = this.getWebConfigFromJson();
  }

  public static Application getInstance() {
    if (instance == null) {
      synchronized (Application.class) {
        if (instance == null) {
          instance = new Application();
        }
      }
    }
    return instance;
  }

  public BalloonGithubConfig getBalloonGithubConfig() {
    return balloonGithubConfig;
  }

  public void setBalloonGithubConfig(BalloonGithubConfig balloonGithubConfig) {
    this.balloonGithubConfig = balloonGithubConfig;
  }

  public BalloonWebConfig getBalloonWebConfig() {
    return balloonWebConfig;
  }

  private BalloonGithubConfig getGithubConfigFromEnv() {
     return new BalloonGithubConfig();
  }

  private BalloonWebConfig getWebConfigFromJson() {
    String json = Config.getInstance().getStringValue("web.config");
    try {
      BalloonWebConfig conf = JSONUtils.fromJson(json, BalloonWebConfig.class);
      String entryPoint = System.getenv("ENTRY_POINT") != null && !System.getenv("ENTRY_POINT").trim().isEmpty() ? System.getenv("ENTRY_POINT").trim() : "wsprx";  
      String token = System.getenv("TOKEN") != null ? System.getenv("TOKEN").trim() : "";  
      conf.setEntryPoint(entryPoint);
      conf.setToken(token);
      return conf;
    } catch (Exception e) {
      return new BalloonWebConfig();
    }
  }
}
