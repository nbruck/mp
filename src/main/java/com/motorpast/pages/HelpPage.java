package com.motorpast.pages;

import org.apache.tapestry5.Block;
import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.annotations.SetupRender;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.PersistentLocale;

import com.motorpast.additional.MotorPages;
import com.motorpast.annotations.UrlRewrite;
import com.motorpast.base.BasePage;

@UrlRewrite(
    mappings={
        "/de/helppage", "/hilfe" /*"/en/helppage", "/help"*/
    },
    sitemap={
        "changefreq", "monthly",
        "priority", "0.7"
    })
public class HelpPage extends BasePage
{
    @Inject
    private Messages messages;

    @Inject
    private PersistentLocale persistentLocale;

    @Inject
    private Block de_helpBlock, en_helpBlock;

    @InjectPage
    private ConfirmationPage confirmationPage;

    @InjectPage
    private ResultPage resultPage;


    public String getPageName() {
        return HelpPage.class.getSimpleName();
    }

    @Override
    public Class<?>[] getNavigation() {
        return MotorPages.getPagesForNavigation();
    }

    public String getMyData() {
        return messages.format("help.answer.what-about-my-data", super.getAppBrandName());
    }

    @SetupRender
    void init() {
        confirmationPage.setPageParameter(null, null, null);
        resultPage.setPageParameter(null, null, null);
    }

    /**
     * @return the helptext to display referred to locale
     */
    public Object getActiveBlock() {
        final String language = persistentLocale.get().getLanguage();

        if("de".equals(language)) {
            return de_helpBlock;
        } else if("en".equals(language)) {
            return en_helpBlock;
        } else {
            return HelpPage.class;
        }
    }
}
