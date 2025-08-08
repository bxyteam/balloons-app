package com.browxy.balloons.domain.config;

public class ConfigResource {

  public enum Type {
      Resource, File
  }

  private String name;
  private Type type;
  private String path;
  private boolean reloadableByTimer;
  private String encoding;

  public ConfigResource(String name, Type type, String path, boolean reloadableByTimer, String encoding) {
      this.name = name;
      this.type = type;
      this.path = path;
      this.reloadableByTimer = reloadableByTimer;
      this.encoding = encoding;
  }

  public String getName() {
      return name;
  }

  public Type getType() {
      return type;
  }

  public String getPath() {
      return path;
  }

  public boolean isReloadableByTimer() {
      return reloadableByTimer;
  }

  public String getEncoding() {
      return encoding;
  }

}