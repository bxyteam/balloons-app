package com.browxy.balloons.server.servlets.api.v1;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.browxy.balloons.domain.compiler.response.ResponseMessageUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

public class GetAliasServlet  extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private static final Logger logger = LoggerFactory.getLogger(GetAliasServlet.class);
    private Gson gson;
    
    public GetAliasServlet() {
      this.gson = new GsonBuilder().disableHtmlEscaping().serializeNulls().create();
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        JsonObject json = new JsonObject();
        response.setContentType("application/json"); 
        try {
            HttpSession session = request.getSession(false);
            response.setStatus(HttpServletResponse.SC_OK);
            if (session != null) {
                String userAlias = (String) session.getAttribute("alias");
                if (userAlias != null) {
                    
                    json.addProperty("statusCode", 200);
                    json.addProperty("alias", userAlias);
                    response.getWriter().write(this.gson.toJson(json));
                    return;
                }
            }
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            json.addProperty("statusCode", 404);
            json.addProperty("alias", "");
            response.getWriter().write(this.gson.toJson(json));

        } catch (Exception e) {
            logger.error("get session alias error ", e);
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            try {
                response.getWriter().write(ResponseMessageUtil.getStatusMessage("Error reading session alias", 400));
            } catch (IOException e1) {
                logger.error("error response session alias ", e);
            }
        } finally {
            try {
                response.flushBuffer();
                response.getWriter().close();
            } catch (IOException e) {
                logger.error("error close response session alias", e);
            }

        }
    }
}
