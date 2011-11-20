package com.motorpast.pages;

import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.util.TextStreamResponse;

import com.motorpast.services.url.AnnotationScannerService;

public class SitemapXml
{
    @Inject
    private AnnotationScannerService annotationScannerService;


    TextStreamResponse onActivate() {
        return new TextStreamResponse("text/xml", annotationScannerService.getSitemapXML());
    }
}
