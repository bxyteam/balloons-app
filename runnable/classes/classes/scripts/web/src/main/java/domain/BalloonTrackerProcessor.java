package domain;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

public class BalloonTrackerProcessor {
  private static Logger logger = LoggerFactory.getLogger(BalloonTrackerProcessor.class);

  public static void main(String[] args) {}

  public static final Map<String, Integer> FREQUENCY_MAP = new HashMap<>();
  static {
    FREQUENCY_MAP.put("60m", 5);
    FREQUENCY_MAP.put("40m", 7);
    FREQUENCY_MAP.put("30m", 10);
    FREQUENCY_MAP.put("20m", 14);
    FREQUENCY_MAP.put("17m", 18);
    FREQUENCY_MAP.put("15m", 21);
    FREQUENCY_MAP.put("12m", 24);
    FREQUENCY_MAP.put("10m", 28);
    FREQUENCY_MAP.put("6m", 50);
    FREQUENCY_MAP.put("4m", 70);
    FREQUENCY_MAP.put("2m", 144);
  }

  public static final Map<String, Integer> BAND_MAP = new HashMap<>();
  static {
    BAND_MAP.put("160m", 1838000);
    BAND_MAP.put("80m", 3569000);
    BAND_MAP.put("40m", 7040000);
    BAND_MAP.put("30m", 10140100);
    BAND_MAP.put("22m", 13553300);
    BAND_MAP.put("20m", 14097000);
    BAND_MAP.put("17m", 18106000);
    BAND_MAP.put("15m", 21096000);
    BAND_MAP.put("12m", 24926000);
    BAND_MAP.put("10m", 28126000);
    BAND_MAP.put("6m", 50294000);
    BAND_MAP.put("2m", 144490400);
    BAND_MAP.put("All", 14097000);
  }

  private List<String> repetitions;
  private List<String> linesToSave;

  private String thePath;
  private String picossDataFile;
  private String picoss1DataFile;
  private String datatrackerFile;
  // private String entrada;
  // private boolean grabar;
  private int validCount;
  // private int pointer;
  private int limite;
  private String newCall;
  private String jsonOutput;

  public BalloonTrackerProcessor() {
    this.thePath = "assets";
    this.picossDataFile = this.thePath + File.separator + "picoss.txt";
    this.picoss1DataFile = this.thePath + File.separator + "picoss1.txt";
    this.datatrackerFile = this.thePath + File.separator + "datatracker.json";
    initializeDataStructures();
  }

  private void initializeDataStructures() {
    this.repetitions = new ArrayList<>();
    this.linesToSave = new ArrayList<>();
    this.newCall = "";
    // this.pointer = 0;
    this.validCount = 0;
    // this.grabar = true;
  }


  public String saveBalloon(String encodedJson) {
    JsonObject jsonResp = new JsonObject();
    JsonObject responseJsonObjectData = new JsonObject();
    StringBuilder logs = new StringBuilder();
    try {


      String decodedJson = decodeBase64(encodedJson);
      Gson gson = new GsonBuilder().serializeNulls().disableHtmlEscaping().create();
      JsonObject jsonObject = gson.fromJson(decodedJson, JsonObject.class);

      String jsonBalloon =
          jsonObject.has("jsonBalloon")
              ? !jsonObject.get("jsonBalloon").isJsonNull()
                  ? jsonObject.get("jsonBalloon").getAsString()
                  : null
              : null;

      if (jsonBalloon == null) {
        logs.append("[ ERROR ] - [ BALLON DATA NOT FOUND ] - [ FAIL ]\n");
        jsonResp.addProperty("message", logs.toString());
        responseJsonObjectData.addProperty("statusCode", 400);
        responseJsonObjectData.addProperty("action", "SEND");
        responseJsonObjectData.add("error", jsonResp);
        return new Gson().toJson(responseJsonObjectData);
      }


      String decodedString = decodeBase64(jsonBalloon);
      SavePicoss savePicoss = new SavePicoss(this.picossDataFile);
      String url = savePicoss.saveBalloon(decodedString);

      if (url == null || url.equals("Unauthorized")) {
        logs.append("[ ERROR ] - [ La solicitud no se pudo guardar] - [ FAIL ]\n");
        jsonResp.addProperty("message", logs.toString());
        responseJsonObjectData.addProperty("statusCode", 400);
        responseJsonObjectData.addProperty("action", "SEND");
        responseJsonObjectData.add("error", jsonResp);
        return new Gson().toJson(responseJsonObjectData);
      }

      this.processBalloonData(false, "", logs);

      responseJsonObjectData.addProperty("url", url);
      jsonResp.add("data", responseJsonObjectData);

      jsonResp.addProperty("logs", logs.toString());
      jsonResp.addProperty("statusCode", 200);
      jsonResp.addProperty("action", "SEND");
      return new Gson().toJson(jsonResp);


    } catch (Exception e) {

      logs.append("[ ERROR ] - [ NOT ALL TASKS COULD BE COMPLETED DUE TO AN ERROR ] - [ FAIL ]\n");
      e.printStackTrace();
      logs.append(e.getMessage());
      jsonResp.addProperty("message", logs.toString());
      responseJsonObjectData.addProperty("statusCode", 400);
      responseJsonObjectData.addProperty("action", "SEND");
      responseJsonObjectData.add("error", jsonResp);
      return new Gson().toJson(responseJsonObjectData);
    }
  }

  public String hideRestoreBalloon(String encodedJson) {
    JsonObject jsonResp = new JsonObject();
    JsonObject responseJsonObjectData = new JsonObject();
    StringBuilder logs = new StringBuilder();
    try {


      String decodedJson = decodeBase64(encodedJson);
      Gson gson = new GsonBuilder().serializeNulls().disableHtmlEscaping().create();
      JsonObject jsonObject = gson.fromJson(decodedJson, JsonObject.class);

      String jsonBalloon =
          jsonObject.has("jsonBalloon")
              ? !jsonObject.get("jsonBalloon").isJsonNull()
                  ? jsonObject.get("jsonBalloon").getAsString()
                  : null
              : null;

      if (jsonBalloon == null) {
        logs.append("[ ERROR ] - [ BALLON DATA NOT FOUND ] - [ FAIL ]\n");
        jsonResp.addProperty("message", logs.toString());
        responseJsonObjectData.addProperty("statusCode", 400);
        responseJsonObjectData.addProperty("action", "HIDE_RESTORE");
        responseJsonObjectData.add("error", jsonResp);
        return new Gson().toJson(responseJsonObjectData);
      }

      String decodedString = decodeBase64(jsonBalloon);

//      BalloonHideRestoreProcessor balloonHideRestoreProcessor =
//          new BalloonHideRestoreProcessor(this.picossDataFile, this.picoss1DataFile);
//      RequestCheckWsprProcessResult result =
//          balloonHideRestoreProcessor.picossDataFile(decodedString);
//
//      if (result.isSuccess()) {
//        this.processBalloonData(false, "", logs);
//      }

      String inventor = System.getenv("BALLOON_INVENTOR") != null ? System.getenv("BALLOON_INVENTOR") : "ESTO NO FUNCIONA!!";
      try {
        FileUtil.writeFile(this.thePath + File.separator + "inventor.txt", inventor );
        logs.append("Todas las operaciones se ejecutaron exitosamente.");
      } catch (IOException e) {
        logs.append("Se produjo un error al ejucutar ballon data processor.");
      }
      
      responseJsonObjectData.addProperty("taskState", "NONE");
      responseJsonObjectData.addProperty("taskStateMessage", inventor);
      responseJsonObjectData.addProperty("callSign", "LEON-79");
      jsonResp.add("data", responseJsonObjectData);

      jsonResp.addProperty("logs", logs.toString());
      jsonResp.addProperty("statusCode", 200);
      jsonResp.addProperty("action", "HIDE_RESTORE");

      return new Gson().toJson(jsonResp);

    } catch (Exception e) {

      logs.append("[ ERROR ] - [ NOT ALL TASKS COULD BE COMPLETED DUE TO AN ERROR ] - [ FAIL ]\n");
      e.printStackTrace();
      logs.append(e.getMessage());
      jsonResp.addProperty("message", logs.toString());
      responseJsonObjectData.addProperty("statusCode", 400);
      responseJsonObjectData.addProperty("action", "HIDE_RESTORE");
      responseJsonObjectData.add("error", jsonResp);
      return new Gson().toJson(responseJsonObjectData);
    }
  }
  /*

  public String hideRestoreBalloon(String encodedJson) {
    JsonObject jsonResp = new JsonObject();
    JsonObject responseJsonObjectData = new JsonObject();
    StringBuilder logs = new StringBuilder();
    try {


      String decodedJson = decodeBase64(encodedJson);
      Gson gson = new GsonBuilder().serializeNulls().disableHtmlEscaping().create();
      JsonObject jsonObject = gson.fromJson(decodedJson, JsonObject.class);

      String jsonBalloon =
          jsonObject.has("jsonBalloon")
              ? !jsonObject.get("jsonBalloon").isJsonNull()
                  ? jsonObject.get("jsonBalloon").getAsString()
                  : null
              : null;

      if (jsonBalloon == null) {
        logs.append("[ ERROR ] - [ BALLON DATA NOT FOUND ] - [ FAIL ]\n");
        jsonResp.addProperty("message", logs.toString());
        responseJsonObjectData.addProperty("statusCode", 400);
        responseJsonObjectData.addProperty("action", "HIDE_RESTORE");
        responseJsonObjectData.add("error", jsonResp);
        return new Gson().toJson(responseJsonObjectData);
      }

      String decodedString = decodeBase64(jsonBalloon);

      BalloonHideRestoreProcessor balloonHideRestoreProcessor =
          new BalloonHideRestoreProcessor(this.picossDataFile, this.picoss1DataFile);
      RequestCheckWsprProcessResult result =
          balloonHideRestoreProcessor.picossDataFile(decodedString);

      if (result.isSuccess()) {
        this.processBalloonData(false, "", logs);
      }

      responseJsonObjectData.addProperty("taskState", result.isSuccess() ? "RELOADING" : "NONE");
      responseJsonObjectData.addProperty("taskStateMessage", result.getMessage());
      responseJsonObjectData.addProperty("callSign", result.getCallsign());
      jsonResp.add("data", responseJsonObjectData);

      jsonResp.addProperty("logs", logs.toString());
      jsonResp.addProperty("statusCode", 200);
      jsonResp.addProperty("action", "HIDE_RESTORE");

      return new Gson().toJson(jsonResp);

    } catch (Exception e) {

      logs.append("[ ERROR ] - [ NOT ALL TASKS COULD BE COMPLETED DUE TO AN ERROR ] - [ FAIL ]\n");
      e.printStackTrace();
      logs.append(e.getMessage());
      jsonResp.addProperty("message", logs.toString());
      responseJsonObjectData.addProperty("statusCode", 400);
      responseJsonObjectData.addProperty("action", "HIDE_RESTORE");
      responseJsonObjectData.add("error", jsonResp);
      return new Gson().toJson(responseJsonObjectData);
    }
  }
*/
  public StringBuilder processBalloonData(boolean showMore, String newCall, StringBuilder logs) {
    List<String> lines = FileUtil.readFileSafe(this.picossDataFile);

    BalloonDataProcessor balloonDataProcessor = new BalloonDataProcessor(showMore, newCall);
    ProcessingResult result = balloonDataProcessor.processData(lines);

    try {
      FileUtil.writeFile(this.datatrackerFile, result.toJson());
      logs.append("Todas las operaciones se ejecutaron exitosamente.");
    } catch (IOException e) {
      logs.append("Se produjo un error al ejucutar ballon data processor.");
    }

    return logs;
  }



  /**
   * Searches for a value between two tags in a text string
   */
  private String findTagValue(String startTag, String endTag, String text) {
    int startPos = text.indexOf(startTag);
    if (startPos == -1)
      return null;

    int valueStart = startPos + startTag.length();
    int endPos = text.indexOf(endTag, valueStart);
    if (endPos == -1)
      return null;

    return text.substring(valueStart, endPos);
  }

  public String ping() {
    JsonObject jsonResp = new JsonObject();
    JsonObject responseJsonObjectData = new JsonObject();
    
    responseJsonObjectData.addProperty("name","Browxy Balloon Server");
    responseJsonObjectData.addProperty("version", "1.0");
    responseJsonObjectData.addProperty("servlet", "ping");

    jsonResp.addProperty("statusCode", 200);
    jsonResp.addProperty("timestamp", java.time.LocalDateTime.now().toString());
    jsonResp.addProperty("message", "API is alive");
    jsonResp.add("server", responseJsonObjectData);

    return new Gson().toJson(jsonResp);
  } 
  
  private static String decodeBase64(String encodedString) {
    java.util.Base64.Decoder decoder = java.util.Base64.getDecoder();
    byte[] decodedBytes = decoder.decode(encodedString);
    return new String(decodedBytes);
  }

  public List<String> getRepetitions() {
    return repetitions;
  }

  public List<String> getLinesToSave() {
    return linesToSave;
  }

  public String getJsonOutput() {
    return jsonOutput;
  }

  public int getValidCount() {
    return validCount;
  }

  public int getLimite() {
    return limite;
  }

  public String getNewCall() {
    return newCall;
  }


}
