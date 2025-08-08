package com.browxy.balloons.server.servlets.api.v1;


import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.browxy.balloons.domain.compiler.message.ApiMessage;
import com.browxy.balloons.domain.compiler.response.ResponseHandler;
import com.browxy.balloons.domain.compiler.status.StatusMessageResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class CompilerServiceServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private static final Logger logger = LoggerFactory.getLogger(CompilerServiceServlet.class);

    private Gson gson;

    public CompilerServiceServlet() {
        this.gson = new GsonBuilder().disableHtmlEscaping().serializeNulls().create();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) {
        response.setContentType("application/json");
        try {
            String message = request.getReader().readLine();
            logger.info(message);
            ApiMessage responseBuilder = this.gson.fromJson(message, ApiMessage.class);
            ResponseHandler responseHandler = new ResponseHandler(responseBuilder.getPayload());
            String result = this.buildResponse(responseHandler, responseBuilder.getType());
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(result);
        } catch (Exception ex) {
            logger.error("error compiler context service ", ex);
            String errorMessage = ex.getMessage() != null || !ex.getMessage().trim().equals("") ? ex.getMessage()
                    : "An error has occurred in the connection";
            StatusMessageResponse errorMessageResponse = StatusMessageResponse.getInstance();
            errorMessageResponse.setMessage(errorMessage);
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            try {
                response.getWriter().write(this.gson.toJson(errorMessageResponse, StatusMessageResponse.class));
            } catch (IOException e) {
                logger.error("error catch compiler context service ", e);

            }
        } finally {
            try {
                response.flushBuffer();
                response.getWriter().close();
            } catch (IOException e) {
                logger.error("Error closing response", e);
            }
        }
    }

    private String buildResponse(ResponseHandler responseHandler, String type) {
        ApiMessage responseBuilder = new ApiMessage(responseHandler.getResponse(), type);
        return this.gson.toJson(responseBuilder, ApiMessage.class);
    }
}
