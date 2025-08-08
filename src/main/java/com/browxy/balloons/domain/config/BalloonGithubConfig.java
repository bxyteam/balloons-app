package com.browxy.balloons.domain.config;

public class BalloonGithubConfig {

  private String githubRepo;
  private String githubOwner;
  private String githubToken;
  private String localRepoPath;
  private String downloadUrl;
  
  public BalloonGithubConfig() {
     this.githubOwner = System.getenv("GITHUB_OWNER"); 
     this.githubRepo = System.getenv("GITHUB_REPO");
     this.githubToken = System.getenv("GITHUB_TOKEN");
     this.localRepoPath = System.getenv("LOCAL_REPO_PATH");
     this.downloadUrl = this.buildGithubDownloadUrl();
  }
  
  public String getGithubRepo() {
    return githubRepo;
  }

  public void setGithubRepo(String githubRepo) {
    this.githubRepo = githubRepo;
  }

  public String getGithubOwner() {
    return githubOwner;
  }

  public void setGithubOwner(String githubOwner) {
    this.githubOwner = githubOwner;
  }

  public String getGithubToken() {
    return githubToken;
  }

  public void setGithubToken(String githubToken) {
    this.githubToken = githubToken;
  }

  public String getLocalRepoPath() {
    return localRepoPath;
  }

  public void setLocalRepoPath(String localRepoPath) {
    this.localRepoPath = localRepoPath;
  }

  public String getDownloadUrl() {
    return downloadUrl;
  }

  public void setDownloadUrl(String downloadUrl) {
    this.downloadUrl = downloadUrl;
  }

  private String buildGithubDownloadUrl() {
    if(this.githubOwner == null || this.githubOwner.trim().isEmpty()) {
      return null;
    }
    if(this.githubRepo == null || this.githubRepo.trim().isEmpty()) {
      return null;
    }
    return String.format("https://github.com/%s/%s/archive/refs/heads/main.zip", this.githubOwner, this.githubRepo); 
        
  }
}

