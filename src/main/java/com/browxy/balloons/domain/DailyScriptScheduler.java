package com.browxy.balloons.domain;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.browxy.balloons.domain.compiler.message.ApiMessage;
import com.browxy.balloons.domain.compiler.message.JavaMessage;
import com.browxy.balloons.domain.compiler.response.ResponseHandler;
import com.browxy.balloons.domain.config.Config;
import com.browxy.balloons.domain.github.GitHubBranchSyncChecker;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class DailyScriptScheduler {
  private static Logger logger = LoggerFactory.getLogger(DailyScriptScheduler.class);

  private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

  public void startSchedule() {
    int hour = this.getScheduleRunHour();
    int minute = this.getScheduleRunMinute();
    LocalTime targetTime = LocalTime.of(hour, minute);

    scheduleDailyTask(targetTime);

    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      scheduler.shutdown();
    }));
  }

  private int getScheduleRunHour() {
    if (System.getenv("SCHEDULE_RUN_HOUR") != null) {
      try {
        int h = Integer.parseInt(System.getenv("SCHEDULE_RUN_HOUR").trim());
        return h;
      } catch (NumberFormatException e) {
        logger.error("Error parse schedule run hour", e);
        return Config.getInstance().getIntValue("schedule.scriptRunHour", 1);
      }
    } else {
      return Config.getInstance().getIntValue("schedule.scriptRunHour", 1);
    }
  }

  private int getScheduleRunMinute() {
    if (System.getenv("SCHEDULE_RUN_MINUTE") != null) {
      try {
        int m = Integer.parseInt(System.getenv("SCHEDULE_RUN_MINUTE").trim());
        return m;
      } catch (NumberFormatException e) {
        logger.error("Error parse schedule run minute", e);
        return Config.getInstance().getIntValue("schedule.scriptRunMinute", 30);
      }
    } else {
      return Config.getInstance().getIntValue("schedule.scriptRunMinute", 30);
    }
  }

  private void scheduleDailyTask(LocalTime targetTime) {
    long initialDelay = calculateInitialDelay(targetTime);
    long period = TimeUnit.DAYS.toSeconds(1);
    
    scheduler.scheduleAtFixedRate(() -> {
      syncGithub();
    }, initialDelay, period, TimeUnit.SECONDS);

    logger.info("Script scheduled to run daily at " + targetTime);
  }

  private long calculateInitialDelay(LocalTime targetTime) {
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime nextRun = now.with(targetTime);

    if (now.isAfter(nextRun)) {
      nextRun = nextRun.plusDays(1);
    }

    return now.until(nextRun, ChronoUnit.SECONDS);
  }

  public void syncGithub() {

    if (!validateGithubCredentials()) {
      return;
    }

    GitHubBranchSyncChecker checker =
        new GitHubBranchSyncChecker(Application.getInstance().getBalloonGithubConfig());
    String[] foldersToMonitor = {"balloons_processor", "balloons_processor/src/main/java/domain"};

    checker.checkAndSync(foldersToMonitor);

    runScriptPostSync(checker.isHasChanges());
  }

  private boolean validateGithubCredentials() {
    if (Application.getInstance().getBalloonGithubConfig().getGithubToken() == null
        || Application.getInstance().getBalloonGithubConfig().getGithubToken().isEmpty()) {
      logger.error("Please set GITHUB_TOKEN environment variable");
      return false;
    }

    if (Application.getInstance().getBalloonGithubConfig().getGithubOwner() == null
        || Application.getInstance().getBalloonGithubConfig().getGithubOwner().isEmpty()) {
      logger.error("Please set GITHUB_OWNER environment variable");
      return false;
    }

    if (Application.getInstance().getBalloonGithubConfig().getGithubRepo() == null
        || Application.getInstance().getBalloonGithubConfig().getGithubRepo().isEmpty()) {
      logger.error("Please set GITHUB_REPO environment variable");
      return false;
    }

    return true;
  }

  private void runScriptPostSync(boolean copyBalloonsProcessor) {
    String scriptCopyFilesFilePath =
        Application.getInstance().getBalloonGithubConfig().getLocalRepoPath()
            + "/scripts/copy_files.sh";
    try {
      this.executeScript(scriptCopyFilesFilePath, copyBalloonsProcessor);
      if(copyBalloonsProcessor) {
        this.compileChanges();
      }
      this.runBalloonTrackerProcessor();
    } catch (Exception e) {
      logger.error("Error executing script: " + scriptCopyFilesFilePath, e);
    }
  }

  private void executeScript(String scriptPath, boolean copyBalloonsProcessor)
      throws IOException, InterruptedException {
    ProcessBuilder pb =
        new ProcessBuilder("bash", scriptPath, String.valueOf(copyBalloonsProcessor));
    pb.redirectErrorStream(true);

    Process process = pb.start();

    try (BufferedReader reader =
        new BufferedReader(new InputStreamReader(process.getInputStream()))) {
      String line;
      while ((line = reader.readLine()) != null) {
        logger.info("Script output: " + line);
      }
    }

    int exitCode = process.waitFor();
    if (exitCode == 0) {
      logger.info("Script executed successfully at " + LocalDateTime.now());
    } else {
      logger.error("Script failed with exit code: " + exitCode);
    }
  }
  
  private void compileChanges() {
    String message = "{"
        + "\"type\":\"http\","
        + "\"payload\":\"{\\\"method\\\":\\\"ping\\\",\\\"arguments\\\":\\\"[]\\\",\\\"compileType\\\":\\\"Pom\\\",\\\"classToLoad\\\":\\\"domain.BalloonTrackerProcessor\\\",\\\"userCodePath\\\":\\\"src/main/java/domain/BalloonTrackerProcessor.java\\\"}\""
        + "}";

    logger.info("Compile balloon data processor changes... " + message);
    String result = executeBalloonDataProc(message);
    logger.info("Compiler changes result: " + result);
  }
  
  private void runBalloonTrackerProcessor() {
    String message = "{"
        + "\"type\":\"http\","
        + "\"payload\":\"{\\\"method\\\":\\\"runTrackerProcessor\\\",\\\"arguments\\\":\\\"[]\\\",\\\"compileType\\\":\\\"Pom\\\",\\\"classToLoad\\\":\\\"domain.BalloonTrackerProcessor\\\",\\\"userCodePath\\\":\\\"src/main/java/domain/BalloonTrackerProcessor.java\\\"}\""
        + "}";
    logger.info("Execute balloon data processor tracker... " + message);
    int n = 100;
    String result = executeBalloonDataProc(message);
    logger.info("Tracking execution finnished... ", result.length() > n ? result.substring(0, n) : result);
  }

  private String executeBalloonDataProc(String apiMessage) {
    Gson gson = new GsonBuilder().serializeNulls().disableHtmlEscaping().create();
    ApiMessage responseBuilder = gson.fromJson(apiMessage, ApiMessage.class);
    ResponseHandler responseHandler = new ResponseHandler(responseBuilder.getPayload());
    ((JavaMessage) responseHandler.getMessage()).setForceCompile(true);
    String result = this.buildResponse(gson, responseHandler, responseBuilder.getType());
    return result;
  }
  
  private String buildResponse(Gson gson, ResponseHandler responseHandler, String type) {
    ApiMessage responseBuilder = new ApiMessage(responseHandler.getResponse(), type);
    return gson.toJson(responseBuilder, ApiMessage.class);
  }
}
