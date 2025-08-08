package com.browxy.balloons.util;

import java.lang.reflect.Type;
import java.util.Map;
import net.sf.json.JSON;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

public class JSONUtils {

  @SuppressWarnings("unchecked")
  public static Map<String, String> getJSONMapFromString(String json) {
    return JSONObject.fromObject(json);
  }

  public static JSON getJSONFromObject(Object object) {
    return JSONSerializer.toJSON(object);
  }

  public static <T> T fromJson(String json, Type typeOfT) throws JsonSyntaxException {
    try {
      return new Gson().fromJson(json, typeOfT);
    } catch (Exception e1) {
      throw new RuntimeException("Unable to deserialize json: " + json, e1);
    }
  }

  public static <T> T fromJson(Type adapterType, Object adapter, String json, Type typeOfT)
      throws JsonSyntaxException {
    try {
      return gsonDateBuilder(adapterType, adapter).fromJson(json, typeOfT);
    } catch (Exception e1) {
      throw new RuntimeException("Unable to deserialize json: " + json, e1);
    }
  }

  private static <T> Gson gsonDateBuilder(Type adapterType, Object adapter) {
    return new GsonBuilder().registerTypeAdapter(adapterType, adapter)
        .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").create();
  }

}