package com.browxy.balloons.exceptions;

import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class InformationEnabledException extends ReportableException {

    private static final long serialVersionUID = 1L;
    private transient Map<String, String> informations = new LinkedHashMap<String, String>();
    private transient Map<String, InformationEnabledException> informationEnabledExceptions = new LinkedHashMap<String, InformationEnabledException>();

    public InformationEnabledException(String message) {
        super(message);
    }

    public InformationEnabledException() {
        super();
    }

    public InformationEnabledException(Throwable cause) {
        super(cause);
    }

    public InformationEnabledException(String message, Throwable cause) {
        super(message, cause);
    }


    public InformationEnabledException addInformation(String key, String value) {
        informations.put(key, value);
        return this;
    }

    @JsonIgnore
    public Map<String, String> getInformations() {
        return informations;
    }


    @JsonIgnore
    public Map<String, InformationEnabledException> getInformationEnabledExceptions() {
        return informationEnabledExceptions;
    }
    
    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();
        if (informations.size() > 0) {
            buf.append("\nInformations\n");
            for (Map.Entry<String, String> entry : informations.entrySet())
                buf.append(entry.getKey() + ":" +  entry.getValue() + '\n');
        }
        if (informationEnabledExceptions.size() > 0) {
            buf.append("\nInformation Exceptions\n");
            for (Map.Entry<String, InformationEnabledException> entry : informationEnabledExceptions.entrySet())
                buf.append(entry.getKey() + ":" +  entry.getValue() + '\n');
        }
        return buf.toString() + "\n" + super.toString();
    }

}

