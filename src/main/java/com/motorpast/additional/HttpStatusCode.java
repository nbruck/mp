package com.motorpast.additional;

import java.io.Serializable;

public class HttpStatusCode implements Serializable
{
    private static final long serialVersionUID = 6549484562577632455L;

    private final int statusCode;
    private final String message;
    private String location;

    public HttpStatusCode(int statusCode) {
        this.statusCode = statusCode;
        this.message = this.location = "";
    }

    public HttpStatusCode(final int statusCode, final String location) {
        this(statusCode);
        this.location = location;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getMessage() {
        return message;
    }

    public String getLocation() {
        return location;
    }
}
