package com.motorpast.base;

import java.io.IOException;
import java.util.Locale;

import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.services.PageRenderLinkSource;
import org.apache.tapestry5.services.PersistentLocale;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.Response;
import org.slf4j.Logger;

public abstract class BasePage
{
    @Inject
    private Request request;

    @Inject
    private Response response;

    @Inject
    private Logger logger;

    @Inject
    private Messages messages;

    @Inject
    private PersistentLocale persistentLocale;

    @Inject
    private PageRenderLinkSource linkSource;

    @Inject
    @Symbol(value = SymbolConstants.SUPPORTED_LOCALES)
    private String supportedLocalesString;

    private String[] supportedLocales;


    /**
     * you can overwrite these methods
     */
    public String getPageName() {
        return "";
    }

    public Class<?>[] getNavigation() {
        return new Class<?>[0];
    }

    public String getRobotText() {
        return messages.get("page.meta.robot.index");
    }


    final void setupRender() {
        if(persistentLocale.get() == null) {
            final Locale locale = request.getLocale();

            if(locale != null && locale.getLanguage() != null) {
                final String language = setPageLocale(locale);

                final Locale localeToSet = new Locale(language);
                persistentLocale.set(localeToSet);
            } else {
                persistentLocale.set(new Locale(getDefaultLanguage()));
            }
        }
    }

    final protected String[] getSupportedLocales() {
        if(supportedLocales == null) {
            supportedLocales = supportedLocalesString.split(",");
        }

        return supportedLocales;
    }

    final protected String getDefaultLanguage() {
        return getSupportedLocales()[0];
    }

    /**
     * if one of args are null then return true for redirect
     * @return true in case of redirect/abort
     */
    final protected <T> boolean checkForRedirect(final Class<?> target, final T... args) {
        for(int i = 0; i < args.length; i++) {
            if(args[i] == null) {
                logger.debug("one arg in args is null so redirect to " + target.getSimpleName());
                try {
                    response.disableCompression(); // to prevent nullpointerexceptions
                    response.sendRedirect(linkSource.createPageRenderLink(target));
                    return true;
                } catch (IOException e) {
                    logger.error(target.getSimpleName() + " not found - CRITICAL ERROR!!!");
                }
            }
        }
        return false;
    }

    final private String setPageLocale(final Locale locale) {
        final String language = locale.getLanguage();
        boolean found = false;

        for(String lang : getSupportedLocales()) {
            if(lang.equals(language)) {
                found = true;
                break;
            }
        }

        if(found) {
            return language;
        } else {
            return getDefaultLanguage();
        }
    }
}
