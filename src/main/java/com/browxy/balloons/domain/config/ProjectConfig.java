package com.browxy.balloons.domain.config;

import java.util.List;

import com.google.gson.JsonObject;

public class ProjectConfig {
    private String compilerContextService;
    private boolean isEmbedded;
    private int socketPort;
    private String entryPoint;
    private Long projectId;
    private JsonObject owner;
    private JsonObject properties;
    private List<JsonObject> pages;

    
    public String getCompilerContextService() {
        return compilerContextService;
    }

    public void setCompilerContextService(String compilerContextService) {
        this.compilerContextService = compilerContextService;
    }

    public int getSocketPort() {
        return socketPort;
    }

    public void setSocketPort(int socketPort) {
        this.socketPort = socketPort;
    }

    public String getEntryPoint() {
        return entryPoint;
    }

    public void setEntryPoint(String entryPoint) {
        this.entryPoint = entryPoint;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public JsonObject getOwner() {
        return owner;
    }

    public void setOwner(JsonObject owner) {
        this.owner = owner;
    }

    public JsonObject getProperties() {
        return properties;
    }

    public void setProperties(JsonObject properties) {
        this.properties = properties;
    }

    public List<JsonObject> getPages() {
        return pages;
    }

    public void setPages(List<JsonObject> pages) {
        this.pages = pages;
    }

    public boolean isEmbedded() {
        return isEmbedded;
    }

    public void setEmbedded(boolean isEmbedded) {
        this.isEmbedded = isEmbedded;
    }
    
   
}