package com.motorpast.components;

import java.util.Locale;

import org.apache.tapestry5.BindingConstants;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.services.URLEncoder;

import com.motorpast.additional.MotorApplicationConstants;

public class SocialNetworksBar
{
    /**
     * first placeholder link, second = title
     */
    private final static String FacebookHref = "http://www.facebook.com/sharer.php?u=%s&t=%s";

    /**
     * link, language
     */
    private final static String GooglePlusSrc = "https://plusone.google.com/u/0/_/+1/fastbutton?url=%s&amp;size=standard&amp;count=false&amp;lang=%s";


    @Inject
    @Symbol(MotorApplicationConstants.IndexPageUrl)
    @Property(write = false)
    private String mainUrl;

    @Inject
    private Locale locale;

    @Inject
    private URLEncoder urlEncoder;

    @Parameter(required = true, defaultPrefix = BindingConstants.LITERAL)
    private String pageHeadline;


    public String getFacebookHref() {
        return String.format(FacebookHref, mainUrl, urlEncoder.encode(pageHeadline));
    }

    public String getGooglePlusSrc() {
        return String.format(GooglePlusSrc, mainUrl, locale.getLanguage());
    }
}
