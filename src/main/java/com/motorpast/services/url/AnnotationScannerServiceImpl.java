package com.motorpast.services.url;

import java.util.HashMap;
import java.util.Map;

import com.motorpast.annotations.UrlRewrite;

/**
 * scanning the page-classes for annotation and storing values
 */
class AnnotationScannerServiceImpl implements AnnotationScannerService
{
    private final String domain = "http://www.motorpast.com";

    private final Map<String, String> originalToRewritten;

    private final Map<String, String> rewrittenToOriginal;

    private StringBuilder sitemapBuilder;

    private final String sitemapXML;


    AnnotationScannerServiceImpl(Class<?>...classes) {
        originalToRewritten = new HashMap<String, String>();
        rewrittenToOriginal = new HashMap<String, String>();

        sitemapBuilder = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
            .append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">");

        for(Class<?> clazz : classes) {
            UrlRewrite rewriteAnnotation = clazz.getAnnotation(UrlRewrite.class);

            if(rewriteAnnotation != null) {
                processAnnotation(rewriteAnnotation);
            }
        }

        sitemapBuilder.append("</urlset>");
        sitemapXML = sitemapBuilder.toString();
        sitemapBuilder = null;
    }

    private void processAnnotation(UrlRewrite rewriteAnnotation) {
        final String[] mappings = rewriteAnnotation.mappings();
        final String[] sitemapData = rewriteAnnotation.sitemap();
        final boolean putInSitemap = rewriteAnnotation.putInSitemap();

        if(mappings == null) {
            throw new IllegalArgumentException("mappings-attribute is null");
        }

        if(mappings.length == 0 || mappings.length % 2 != 0) {
            throw new IllegalArgumentException("please check the arguments of mappings-attribute");
        }

        for(int i = 0; i <= mappings.length - 2; i += 2) {
            originalToRewritten.put(mappings[i], mappings[i + 1]);
            rewrittenToOriginal.put(mappings[i + 1], mappings[i]);

            if(putInSitemap) {
                sitemapBuilder.append("<url>")
                    .append("<loc>")
                    .append(domain)
                    .append(mappings[i + 1])
                    .append("</loc>");

                for(int j = 0; j <= sitemapData.length -2; j +=2) {
                    sitemapBuilder.append("<" + sitemapData[j] + ">")
                        .append(sitemapData[j + 1])
                    .append("</" + sitemapData[j] + ">");
                }

                sitemapBuilder.append("</url>");
            }
        }
    }

    final public Map<String, String> getOriginalToRewritten() {
        return originalToRewritten;
    }

    final public Map<String, String> getRewrittenToOriginal() {
        return rewrittenToOriginal;
    }

    final public String getSitemapXML() {
        return sitemapXML;
    }
}
