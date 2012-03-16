package com.motorpast.services.url;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.motorpast.annotations.UrlRewrite;

/**
 * scanning the page-classes for annotation and storing values
 */
class AnnotationScannerServiceImpl implements AnnotationScannerService
{
    private final Class<?>[] classes;
    private final Map<String, String> originalToRewritten;
    private final Map<String, String> rewrittenToOriginal;

    private String sitemapXML;


    AnnotationScannerServiceImpl(Class<?>...classes) {
        this.classes = classes;
        originalToRewritten = new HashMap<String, String>(classes.length);
        rewrittenToOriginal = new HashMap<String, String>(classes.length);

        for(Class<?> clazz : classes) {
            UrlRewrite rewriteAnnotation = clazz.getAnnotation(UrlRewrite.class);

            if(rewriteAnnotation != null) {
                processAnnotation(rewriteAnnotation);
            }
        }
    }

    private void processAnnotation(UrlRewrite rewriteAnnotation) {
        final String[] mappings = rewriteAnnotation.mappings();

        if(mappings == null) {
            throw new IllegalArgumentException("mappings-attribute is null");
        }

        if(mappings.length == 0 || mappings.length % 2 != 0) {
            throw new IllegalArgumentException("please check the arguments of mappings-attribute");
        }

        for(int i = 0; i <= mappings.length - 2; i += 2) {
            originalToRewritten.put(mappings[i], mappings[i + 1]);
            rewrittenToOriginal.put(mappings[i + 1], mappings[i]);
        }
    }

    final public Map<String, String> getOriginalToRewritten() {
        return originalToRewritten;
    }

    final public Map<String, String> getRewrittenToOriginal() {
        return rewrittenToOriginal;
    }

    final public synchronized String getSitemapXML(HttpServletRequest httpServletRequest) {
        if(sitemapXML == null) {
            String sitemapLocal = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                    + "<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">";

            sitemapLocal = createEntries(sitemapLocal, httpServletRequest);

            sitemapLocal += "</urlset>";

            sitemapXML = sitemapLocal;
        }

        return sitemapXML;
    }

    private String createEntries(String sitemapLocal, HttpServletRequest httpServletRequest) {
        for(Class<?> clazz : classes) {
            UrlRewrite rewriteAnnotation = clazz.getAnnotation(UrlRewrite.class);

            if(rewriteAnnotation != null) {
                for(int i = 0; i <= rewriteAnnotation.mappings().length - 2; i += 2) {
                    String[] sitemapData = rewriteAnnotation.sitemap();

                    if(rewriteAnnotation.putInSitemap()) {
                        sitemapLocal += "<url>";
                            sitemapLocal += "<loc>";
                                sitemapLocal += getUrl(httpServletRequest) + rewriteAnnotation.mappings()[1]; //TODO: error if different languages
                            sitemapLocal += "</loc>";

                            for(int j = 0; j <= sitemapData.length -2; j +=2) {
                                sitemapLocal += "<" + sitemapData[j] + ">";
                                    sitemapLocal += sitemapData[j + 1];
                                sitemapLocal += "</" + sitemapData[j] + ">";
                            }

                        sitemapLocal += "</url>";
                    }
                }
            }
        }

        return sitemapLocal;
    }

    private String getUrl(HttpServletRequest httpServletRequest) {
        final StringBuffer url = httpServletRequest.getRequestURL();

        return url.substring(0, url.lastIndexOf("/"));
    }
}
