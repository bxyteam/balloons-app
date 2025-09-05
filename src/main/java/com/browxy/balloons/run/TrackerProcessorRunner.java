package com.browxy.balloons.run;

import com.browxy.balloons.domain.compiler.message.ApiMessage;
import com.browxy.balloons.domain.compiler.response.ResponseHandler;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class TrackerProcessorRunner {
  private Gson gson;

  public TrackerProcessorRunner() {
    this.gson = new GsonBuilder().disableHtmlEscaping().serializeNulls().create();
  }

  public String run(ApiMessage apiMessage) {
    ResponseHandler responseHandler = new ResponseHandler(apiMessage.getPayload());
    String result = this.buildResponse(responseHandler, apiMessage.getType());
    return result;
  }

  private String buildResponse(ResponseHandler responseHandler, String type) {
    ApiMessage responseBuilder = new ApiMessage(responseHandler.getResponse(), type);
    return this.gson.toJson(responseBuilder, ApiMessage.class);
  }
}
