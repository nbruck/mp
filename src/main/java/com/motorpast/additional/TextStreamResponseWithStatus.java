package com.motorpast.additional;

import org.apache.tapestry5.services.Response;
import org.apache.tapestry5.util.TextStreamResponse;

public class TextStreamResponseWithStatus extends TextStreamResponse
{
    private final int statusCode;

    public TextStreamResponseWithStatus(final String contentType,
            final String text,
            final int statusCode
    ) {
        super(contentType, text);
        this.statusCode = statusCode;
    }

    @Override
    public void prepareResponse(Response response) {
        super.prepareResponse(response);
        response.setStatus(statusCode);
    }
}
