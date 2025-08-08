package domain;

import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class SavePicoss {
  private String picossFile;

  public SavePicoss(String picossFile) {
    this.picossFile = picossFile;
  }

  public String saveBalloon(String jsonInput) {
    try {
      Gson gson = new GsonBuilder().disableHtmlEscaping().serializeNulls().create();
      PayloadForm payloadForm = gson.fromJson(jsonInput, PayloadForm.class);
      
      String inventor = System.getenv("BALLOON_INVENTOR");
      if (inventor == null || !inventor.equalsIgnoreCase(payloadForm.who)) {
        return "Unauthorized";
      }

      StringBuilder archi = new StringBuilder(System.getenv("BALLOON_URL") + "/wsprx");
      appendParam(archi, "other", payloadForm.form.other.trim());
      appendParam(archi, "band", payloadForm.form.band.trim());
      appendParam(archi, "balloonid", payloadForm.form.balloonid.trim());
      appendParam(archi, "timeslot", payloadForm.form.timeslot.trim());
      appendParam(archi, "detail", payloadForm.form.detail.trim());
      appendParam(archi, "launch", payloadForm.form.launch.trim());
      appendParam(archi, "SSID", payloadForm.form.SSID.trim());
      appendParam(archi, "tracker", payloadForm.form.tracker.trim());
      appendParam(archi, "qrpid", payloadForm.form.qrp != null ? payloadForm.form.qrp.trim() : "");
      appendParam(archi, "repito", payloadForm.form.repito.trim());
      appendParam(archi, "qp", payloadForm.form.telen.trim());
      appendParam(archi, "telen", payloadForm.form.telen.trim());
      appendParam(archi, "comments", payloadForm.form.comments.trim());

      String urlString = archi.toString();
      String safeUrl = urlString.replace("\n", "%0A");

      FileUtil.appendToFile(safeUrl, this.picossFile);

      return safeUrl;

    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }

  private void appendParam(StringBuilder sb, String key, String value) {
    try {
      if (sb.indexOf("?") > 0) {
        sb.append("&");
      } else {
        sb.append("?");
      }
      sb.append(key).append("=")
          .append(URLEncoder.encode(value != null ? value : "", StandardCharsets.UTF_8.name()));
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }
  }

}

