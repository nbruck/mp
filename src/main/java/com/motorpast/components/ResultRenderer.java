package com.motorpast.components;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.services.PageRenderLinkSource;

import com.motorpast.additional.MotorApplicationConstants;
import com.motorpast.dataobjects.UserSessionObj;
import com.motorpast.pages.MileageResultCaptchaPage;
import com.motorpast.services.business.trustrules.fiftyone.MinimumFiftyOnePercent;

public class ResultRenderer
{
    @SessionState(create = true)
    private UserSessionObj sessionObj;


    @Inject
    @Symbol(value = MotorApplicationConstants.ShowTrustLevel)
    private boolean globalShowTrustLevel;

    @Inject
    private Messages messages;

    @Inject
    private PageRenderLinkSource pageRenderLinkSource;

    @Inject
    private Locale locale;

    @Parameter(required = true)
    private boolean showCarTrustLevel;

    @Parameter(required = true)
    private Date storingDate;

    private Map<String, String> dateFormatStrings;


    void setupRender() {
        dateFormatStrings = new HashMap<String, String>(2);
        dateFormatStrings.put("de", "dd.MM.yyyy");
        dateFormatStrings.put("en", "yyyy-MM-dd");
    }

    public String getMileageCaptchaUrl() {
        return pageRenderLinkSource.createPageRenderLink(MileageResultCaptchaPage.class).toAbsoluteURI();
    }


    public boolean isShowTrustLevel() {
        return globalShowTrustLevel && showCarTrustLevel;
    }

    public String getMileageMessage() {
        return messages.format("mileage.found-one", getStoringDate());
    }

    private String getStoringDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(dateFormatStrings.get(locale.getLanguage()));

        return dateFormat.format(storingDate);
    }

    public boolean isSuspicious() {
        if(sessionObj != null) { //TODO: state checken fuer anzeige???
            return !sessionObj.getTrustLevel().startsWith(String.valueOf(MinimumFiftyOnePercent.ruleWeight));
        } else {
            return false;
        }
    }
}
