package domain;

import java.util.ArrayList;
import java.util.List;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class BalloonHideRestoreProcessor {
  private String picossFile;
  private String picoss1File;

  public BalloonHideRestoreProcessor(String picossFile, String picoss1File) {
    this.picossFile = picossFile;
    this.picoss1File = picoss1File;
  }

  public String buscarTag(String tagInicial, String tagFinal, String texto, int[] posicion) {
    boolean tagFinalEncontrado = false;
    int k = posicion[0];
    String resultado = "";

    while (!tagFinalEncontrado && k < texto.length()) {
      if (k + tagInicial.length() <= texto.length()
          && texto.substring(k, k + tagInicial.length()).equals(tagInicial)) {

        int j = k + tagInicial.length();
        boolean tagFinalBuscado = false;

        while (!tagFinalBuscado && j < texto.length()) {
          if (j + tagFinal.length() <= texto.length()
              && texto.substring(j, j + tagFinal.length()).equals(tagFinal)) {

            resultado = texto.substring(k + tagInicial.length(), j);
            tagFinalBuscado = true;
            tagFinalEncontrado = true;
            posicion[0] = j + tagFinal.length();
            return resultado;
          }
          j++;
        }
        k = j;
      }
      k++;
      if (k > texto.length()) {
        tagFinalEncontrado = true;
      }
    }

    posicion[0] = k;
    return resultado;
  }

  public String urlDecode(String encoded) {
    try {
      return java.net.URLDecoder.decode(encoded, "UTF-8");
    } catch (Exception e) {
      return encoded;
    }
  }

  public RequestCheckWsprProcessResult picossDataFile(String jsonData) {
    try {

      Gson gson = new GsonBuilder().disableHtmlEscaping().serializeNulls().create();
      RequestCheckWspr requestWspr = gson.fromJson(jsonData, RequestCheckWspr.class);

      // Limpiar y procesar datos
      String elRequest = requestWspr.getDatos();
      elRequest = elRequest.replaceAll("&wide=on", "");
      elRequest = elRequest.replaceAll("&wide=", "");

      // Dividir los datos (equivalente a split en ASP)
      String[] dm = elRequest.split(",", 14);
      if (dm.length < 8) {
        return new RequestCheckWsprProcessResult(false, "Datos insuficientes", "");
      }

      String callsign = dm[0].toUpperCase();

      // Crear arguo (cadena para comparación)
      StringBuilder arguo = new StringBuilder();
      for (int i = 0; i < Math.min(8, dm.length); i++) {
        arguo.append(dm[i]).append(",");
      }
      if (dm.length > 7) {
        arguo.append(dm[8]).append(",");
      }

      // Crear detalle
      String detalle = "On " + callsign;
      if (dm.length > 6 && !dm[6].isEmpty()) {
        detalle += "-" + dm[6];
      }
      detalle += " ";

      String antes = elRequest + " " + urlDecode(requestWspr.getComenta()).replace(" ", "+");
      String despues = antes;
      
      String inventor = System.getenv("BALLOON_INVENTOR");
      if (inventor == null || !inventor.equals(requestWspr.getWho().trim().toLowerCase())) {
        return new RequestCheckWsprProcessResult(false, detalle + "<i>No changes done</i>",
            callsign);
      }

      List<String> lines = FileUtil.readFileSafe(this.picossFile);
      List<String> newLines = new ArrayList<>();
      boolean cambioRealizado = false;
      String nuevoCallsign = callsign;

      for (String linea : lines) {
        boolean grabar = false;

        // Buscar launch en la línea
        int[] posicion = {0};
        String launch = buscarTag("launch=", "&SSID", linea, posicion);

        if (dm.length > 5 && launch.equals(dm[5])) {
          // Extraer argumentos de la línea actual
          StringBuilder argur = new StringBuilder();

          posicion[0] = 0;
          argur.append(buscarTag("?other=", "&", linea, posicion)).append(",");
          posicion[0] = 0;
          argur.append(buscarTag("&banda=", "&", linea, posicion)).append(",");
          posicion[0] = 0;
          argur.append(buscarTag("&balloonid=", "&", linea, posicion)).append(",");
          posicion[0] = 0;
          argur.append(buscarTag("&timeslot=", "&", linea, posicion)).append(",");
          posicion[0] = 0;
          argur.append(buscarTag("&detail=", "&", linea, posicion)).append(",");
          posicion[0] = 0;
          argur.append(buscarTag("&launch=", "&", linea, posicion)).append(",");
          posicion[0] = 0;
          argur.append(buscarTag("&SSID=", "&", linea, posicion)).append(",");
          posicion[0] = 0;
          argur.append(buscarTag("&tracker=", "&", linea, posicion)).append(",");
          posicion[0] = 0;
          argur.append(buscarTag("&qrpid=", "&", linea, posicion)).append(",");

          if (arguo.toString().equals(argur.toString())) {
            grabar = true;
            cambioRealizado = true;
            antes = linea;

            String newCall = dm[0];
            String ssid = dm.length > 6 ? dm[6] : "";
            if (!ssid.isEmpty()) {
              ssid = "-" + ssid;
            }

            detalle = "Changes to " + newCall + ssid + " written<br><br>";
            nuevoCallsign = newCall + ssid;

            if (linea.startsWith("//")) {
              detalle += newCall + ssid + " Now Active<br><br>";
            } else {
              detalle += newCall + ssid + " Now Hidden<br><br>";
            }
          }
        }

        if (grabar) {
          if (linea.startsWith("//")) {
            // Remover // y activar
            linea = linea.substring(2);
            despues = linea;
          } else {
            // Agregar // y desactivar
            linea = "//" + linea;
            despues = linea;
          }
        }

        newLines.add(linea);
      }

      // Escribir archivo temporal y reemplazar
      FileUtil.writeFileLines(this.picoss1File, newLines);
      FileUtil.writeFileLines(this.picossFile, newLines);

      if (cambioRealizado) {
        detalle += "<i>DONE.. THANKS!</i>";
        return new RequestCheckWsprProcessResult(true, detalle, nuevoCallsign);
      } else {
        return new RequestCheckWsprProcessResult(false, detalle + "<i>No changes done</i>",
            nuevoCallsign);
      }

    } catch (Exception e) {
      e.printStackTrace();
      return new RequestCheckWsprProcessResult(false, "Error procesando datos: " + e.getMessage(),
          "");
    }
  }
}

