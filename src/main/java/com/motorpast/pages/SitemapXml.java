package com.motorpast.pages;

import javax.servlet.http.HttpServletRequest;

import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.util.TextStreamResponse;

import com.motorpast.services.url.AnnotationScannerService;

public class SitemapXml
{
    @Inject
    private AnnotationScannerService annotationScannerService;

    @Inject
    private HttpServletRequest httpServletRequest;


    TextStreamResponse onActivate() {
        return new TextStreamResponse("text/xml", annotationScannerService.getSitemapXML(httpServletRequest));
    }
}
