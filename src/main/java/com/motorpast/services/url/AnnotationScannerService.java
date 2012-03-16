package com.motorpast.services.url;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

public interface AnnotationScannerService
{
    public Map<String, String> getOriginalToRewritten();

    public Map<String, String> getRewrittenToOriginal();

    public String getSitemapXML(HttpServletRequest httpServletRequest);
}