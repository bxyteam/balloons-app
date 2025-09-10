package domain;

import java.util.*;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

public class BalloonDataProcessor {
  private static Logger logger = LoggerFactory.getLogger(BalloonDataProcessor.class);

  private List<String> inputLines;
  // private List<Balloon> balloons;
  private Set<String> callSigns;
  // private List<LicenseData> licenseData;

  private Gson gson;
  // private String entrada = "";
  private boolean showMore = false;
  // private boolean grabar = true;
  private String newCall;

  private static final Map<String, String> PARAMETER_PREFIXES = new HashMap<>();
  static {
    PARAMETER_PREFIXES.put("call", "call=");
    PARAMETER_PREFIXES.put("other", "other=");
    PARAMETER_PREFIXES.put("banda", "banda=");
    PARAMETER_PREFIXES.put("balloonid", "balloonid=");
    PARAMETER_PREFIXES.put("timeslot", "timeslot=");
    PARAMETER_PREFIXES.put("detail", "detail=");
    PARAMETER_PREFIXES.put("launch", "launch=");
    PARAMETER_PREFIXES.put("SSID", "SSID=");
    PARAMETER_PREFIXES.put("tracker", "tracker=");
    PARAMETER_PREFIXES.put("qrpid", "qrpid=");
    PARAMETER_PREFIXES.put("qp", "qp=");
    // other=ko4aqf/b&banda=20m&balloonid=&timeslot=2&detail=&launch=20251128000000&SSID=11&tracker=zachtek1&qrpid=&repito=on&qp=&telen=&comments=
  }

  // private static final String[] JSON_FIELD_ORDER = { "call", "other", "banda", "balloonid",
  // "timeslot", "detail",
  // "launch", "SSID", "tracker", "qrpid", "comments" };

  private static final String[] JSON_FIELD_ORDER = {"other", "banda", "balloonid", "timeslot",
      "detail", "launch", "SSID", "tracker", "qrpid", "comments", "old"};

  public BalloonDataProcessor() {
    initialize(true, "");
  }

  public BalloonDataProcessor(boolean showMore, String newCall) {
    initialize(showMore, newCall);
  }

  private void initialize(boolean showMore, String newCall) {
    // this.licenseData = new ArrayList<>();
    this.inputLines = new ArrayList<>();
    // this.balloons = new ArrayList<>();
    this.callSigns = new LinkedHashSet<>();
    this.gson = new GsonBuilder().disableHtmlEscaping().serializeNulls().create();
    this.newCall = newCall;
    this.showMore = showMore;
  }
  // public void setEntrada(String entrada) {
  // this.entrada = Optional.ofNullable(entrada).orElse("");
  // }

  public void setShowMore(boolean showMore) {
    this.showMore = showMore;
  }

  public void addLine(String line) {
    if (line != null && !line.trim().isEmpty()) {
      inputLines.add(line.trim());
    }
  }

  public void addLines(Collection<String> lines) {
    lines.stream().filter(line -> line != null && !line.trim().isEmpty()).forEach(this::addLine);
  }

  public List<Balloon> processLines() {
    return inputLines.parallelStream().map(this::processLine).filter(Objects::nonNull)
        .collect(Collectors.toList());
  }

  public List<Balloon> sortBalloons() {
    List<Balloon> balloons = processLines();
    balloons.sort((a, b) -> {
      if (a.getLaunch() == null && b.getLaunch() == null)
        return 0;
      if (a.getLaunch() == null)
        return 1;
      if (b.getLaunch() == null)
        return -1;
      return a.getLaunch().compareTo(b.getLaunch());
    });

    return balloons;
  }

  public ProcessingResult processData(Collection<String> lines) {
    this.addLines(lines);
    List<Balloon> balloons = this.processLines();
    String json = getJSONArray(balloons);
    String newCalls = String.join(",", callSigns);
    String sqlQuery = this.processCallSigns();
    String responseBody = this.fetchLicenseData(sqlQuery);
    List<LicenseData> licenseData = this.processLicenseResponse(responseBody);
    return new ProcessingResult(json, balloons, newCalls, licenseData);

  }

  private Balloon processLine(String line) {
    if (line == null || line.trim().isEmpty()) {
      return null;
    }

    // Handle commented lines
    boolean isCommented = line.startsWith("//");
    if (isCommented && !showMore) {
      return null;
    }

    String processedLine = isCommented ? line.substring(2) : line;

    // Check if this line matches entrada
    // if (htmlEncode(processedLine).equals(htmlEncode(entrada))) {
    // grabar = false;
    // }

    // Parse URL and parameters
    return parseUrl(processedLine, line);
  }

  private Balloon parseUrl(String line, String originalLine) {
    // Replace commas with plus signs (as in original code)
    String cleanedLine = line.replace(",", "+");

    // Split URL from parameters
    String[] urlParts = cleanedLine.split("\\?", 2);
    if (urlParts.length < 2) {
      return null;
    }

    Map<String, String> parameters = parseParameters(urlParts[1]);

    String jsonParams = this.gson.toJson(parameters);
    Balloon balloon = this.gson.fromJson(jsonParams, Balloon.class);
    balloon.setUrl(cleanedLine);
    balloon.setLine(originalLine);
    String old = originalLine.startsWith("//") ? "true" : "false"; 
    balloon.setOld(old);        

    // Extract comments
    String comments = extractComments(balloon.getComments());
    balloon.setComments(comments);

    // Add to call signs
    String otherValue = balloon.getOther();
    if (otherValue != null && !otherValue.isEmpty() && old.equals("false")) {
      callSigns.add(otherValue.toUpperCase());
    }

    return balloon;
  }

  private Map<String, String> parseParameters(String parameterString) {
    return Arrays.stream(parameterString.split("&")).filter(param -> param.contains("="))
        .collect(Collectors.toMap(param -> param.substring(0, param.indexOf("=")),
            param -> param.substring(param.indexOf("=") + 1),
            (existing, replacement) -> replacement, LinkedHashMap::new));
  }

  private String extractComments(Map<String, String> parameters) {
    String comments = parameters.get("comments");
    if (comments != null && comments.length() > 5) {
      return urlDecode(comments);
    }
    return "";
  }

  private String extractComments(String comments) {
    if (comments != null && comments.length() > 5) {
      return urlDecode(comments);
    }
    return "";
  }

  private String getJSONArray(List<Balloon> data) {
    if (data.isEmpty()) {
      return "[]";
    }

    StringBuilder js = new StringBuilder("[\n");


    // Use streaming for efficient processing
    String arrayContent =
        data.stream().map(this::generateJSONArray).collect(Collectors.joining(",\n"));

    js.append(arrayContent);
    js.append("\n]\n");

    return js.toString();
  }

  private String generateJSONArray(Balloon data) {
    return Arrays.stream(JSON_FIELD_ORDER).map(field -> {
      if ("comments".equals(field)) {
        return "\"" + escapeJSON(data.getComments()) + "\"";
      }
      JsonElement jsonElement = this.gson.toJsonTree(data);
      JsonObject jsonObject = jsonElement.getAsJsonObject();
      String value = jsonObject.get(field).isJsonNull() ? "" : jsonObject.get(field).getAsString();
      return "\"" + escapeJSON(value) + "\"";
    }).collect(Collectors.joining(",", "[", "]"));
  }


  private String htmlEncode(String text) {
    if (text == null || text.isEmpty())
      return "";

    return text.chars().mapToObj(c -> {
      switch (c) {
        case '&':
          return "&amp;";
        case '<':
          return "&lt;";
        case '>':
          return "&gt;";
        case '"':
          return "&quot;";
        case '\'':
          return "&#39;";
        default:
          return String.valueOf((char) c);
      }
    }).collect(Collectors.joining());
  }

  private String urlDecode(String text) {
    try {
      return URLDecoder.decode(text, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      logger.error("error encoding text", e);
      return text;
    }
  }

  private String escapeJSON(String text) {
    if (text == null || text.isEmpty())
      return "";

    return text.chars().mapToObj(c -> {
      switch (c) {
        case '\\':
          return "\\\\";
        case '\'':
          return "\\\\'";
        case '"':
          return "\\\\"";
        case '\n':
          return "\\n";
        case '\r':
          return "\\r";
        case '\t':
          return "\\t";
        default:
          return String.valueOf((char) c);
      }
    }).collect(Collectors.joining());
  }

  public String processCallSigns() {
    if (callSigns.isEmpty())
      return "";

    // Get unique call signs
    Set<String> uniqueCalls = new LinkedHashSet<>(callSigns);

    // Build SQL-like condition for web service call
    StringBuilder sqlBuilder = new StringBuilder();

    if (uniqueCalls.isEmpty()) {
      sqlBuilder.append("(tx_sign='')");
    } else {
      sqlBuilder.append("(");
      for (String call : uniqueCalls) {
        sqlBuilder.append("tx_sign='").append(call).append("' or ");
      }
      String sql = sqlBuilder.toString();
      sql = sql.substring(0, sql.length() - 4) + ")"; // Remove last " or "
      sqlBuilder.setLength(0);
      sqlBuilder.append(sql);
    }
    return sqlBuilder.toString();
  }

  private String fetchLicenseData(String sqlCondition) {
    if (sqlCondition == null || sqlCondition.isEmpty()) {
      return null;
    }

    HttpURLConnection connection = null;
    BufferedReader reader = null;

    try {
      // Calculate date 30 days ago
      LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);
      String dateFilter = thirtyDaysAgo.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

      String rawQuery =
          "select distinct tx_sign, time, cast(frequency/1000000 as int) from rx where "
              + sqlCondition + " and time > '" + dateFilter + "' order by time desc";

      System.out.println(rawQuery);

      // Encode the query
      String encodedQuery = URLEncoder.encode(rawQuery, StandardCharsets.UTF_8.name());

      // Construct the full URL
      String urlStr = "https://db1.wspr.live/?query=" + encodedQuery;

      URL url = new URL(urlStr);
      connection = (HttpURLConnection) url.openConnection();
      connection.setRequestMethod("GET");
      connection.setConnectTimeout(10000);
      connection.setReadTimeout(10000);

      int status = connection.getResponseCode();
      if (status == 200) {
        reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
        StringBuilder response = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
          response.append(line).append("\n");
        }

        return response.toString();

      } else {
        logger.error("HTTP Error: " + status);
        return null;
      }

    } catch (Exception e) {
      logger.error("Error in fetchLicenseData: " + e.getMessage(), e);
      return null;
    } finally {
      if (reader != null) {
        try {
          reader.close();
        } catch (Exception e) {
          logger.error("error closing HTTP connection", e);
        }
      }
      if (connection != null) {
        connection.disconnect();
      }
    }
  }

  private List<LicenseData> processLicenseResponse(String responseBody) {
    List<LicenseData> licenseData = new ArrayList<>();
    if (responseBody == null) {
      return licenseData;
    }
    String[] lines = responseBody.split("\n");
    Set<String> processedEntries = new HashSet<>();
    LocalDateTime now = LocalDateTime.now();
    for (String line : lines) {
      if (line.trim().isEmpty())
        continue;

      String[] parts = line.split("\t");
      if (parts.length >= 3) {
        String callSign = parts[0];
        String timeStr = parts[1];
        String frequency = parts[2];

        String entryKey = callSign + frequency;
        if (!processedEntries.contains(entryKey)) {
          try {
            // Define the format of the input string
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            // Parse the string into LocalDateTime
            LocalDateTime dateTime = LocalDateTime.parse(timeStr, formatter);
            // Calculate the difference in hours using ChronoUnit
            long hoursDiff = ChronoUnit.HOURS.between(now, dateTime);
            licenseData.add(new LicenseData(callSign, (int) hoursDiff, frequency));
            processedEntries.add(entryKey);
          } catch (Exception e) {
            logger.error("Error parsing time: " + timeStr, e);
          }
        }
      }
    }
    return licenseData;
  }


}


