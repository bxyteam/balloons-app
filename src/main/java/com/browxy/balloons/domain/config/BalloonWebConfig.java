package com.browxy.balloons.domain.config;

public class BalloonWebConfig {

  private String basePath;
  private String staticPath;
  private String staticFile;
  private String storagePath;
  private String entryPoint;
  private String token;

  public String getBasePath() {
    return basePath;
  }

  public void setBasePath(String basePath) {
    this.basePath = basePath;
  }

  public String getStaticPath() {
    return staticPath;
  }

  public void setStaticPath(String staticPath) {
    this.staticPath = staticPath;
  }

  public String getStaticFile() {
    return staticFile;
  }

  public void setStaticFile(String staticFile) {
    this.staticFile = staticFile;
  }

  public String getStoragePath() {
    return storagePath;
  }

  public void setStoragePath(String storagePath) {
    this.storagePath = storagePath;
  }

  public String getEntryPoint() {
    return entryPoint;
  }

  public void setEntryPoint(String entryPoint) {
    this.entryPoint = entryPoint;
  }

  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
  }



}
