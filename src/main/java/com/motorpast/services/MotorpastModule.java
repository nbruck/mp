package com.motorpast.services;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.apache.tapestry5.Link;
import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.ioc.MappedConfiguration;
import org.apache.tapestry5.ioc.ServiceBinder;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.annotations.Marker;
import org.apache.tapestry5.ioc.annotations.SubModule;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.services.ApplicationStateManager;
import org.apache.tapestry5.services.ClientInfrastructure;
import org.apache.tapestry5.services.ComponentEventResultProcessor;
import org.apache.tapestry5.services.ComponentSource;
import org.apache.tapestry5.services.ExceptionReporter;
import org.apache.tapestry5.services.PageRenderLinkSource;
import org.apache.tapestry5.services.RequestExceptionHandler;
import org.apache.tapestry5.services.Response;
import org.apache.tapestry5.services.ResponseRenderer;
import org.slf4j.Logger;

import com.motorpast.additional.HttpStatusCode;
import com.motorpast.additional.MotorApplicationConstants;
import com.motorpast.additional.MotorpastException;
import com.motorpast.annotations.MileageResultCaptcha;
import com.motorpast.dataobjects.CarData;
import com.motorpast.pages.ErrorPage;
import com.motorpast.services.additional.MotorpastClientInfraStructure;
import com.motorpast.services.business.BusinessService;
import com.motorpast.services.business.BusinessServiceImpl;
import com.motorpast.services.business.ValidationService;
import com.motorpast.services.business.ValidationServiceImpl;
import com.motorpast.services.captcha.CaptchaService;
import com.motorpast.services.captcha.MileageResultCaptchaService;
import com.motorpast.services.persistence.PersistenceService;
import com.motorpast.services.persistence.hibernate.MotorpastHibernateModule;
import com.motorpast.services.security.SecurityService;
import com.motorpast.services.security.SecurityServiceImpl;
import com.motorpast.services.url.MotorpastUrlRewriteModule;

/**
 * This module is automatically included as part of the Tapestry IoC Registry, it's a good place to
 * configure and extend Tapestry, or to place your own service definitions.
 */
@SuppressWarnings("deprecation")
@SubModule(value={
    MotorpastUrlRewriteModule.class,
    MotorpastHibernateModule.class
})
public class MotorpastModule
{
    public static void bind(ServiceBinder binder) {
        binder.bind(ValidationService.class, ValidationServiceImpl.class);
    }

    public static SecurityService buildSecurityService(
        @Inject @Symbol(MotorApplicationConstants.AntiSpambotTime) final long antiSpambotTime
    ) {
        return new SecurityServiceImpl(antiSpambotTime);
    }

    public static BusinessService<CarData, HttpServletRequest> buildBusinessService(
        final PersistenceService<CarData> persistenceService,
        final ApplicationStateManager applicationStateManager,
        @Inject @Symbol(value = MotorApplicationConstants.RegistrationDateAttempts) final int initialAttemptCount,
        @Inject @Symbol(value = MotorApplicationConstants.ShowTrustLevel) final boolean showTrustLevel
    ) {
        return new BusinessServiceImpl(persistenceService, applicationStateManager, initialAttemptCount, showTrustLevel);
    }

    @Marker(MileageResultCaptcha.class)
    public static CaptchaService buildMileAgeResultCaptchaService(final ApplicationStateManager applicationStateManager) {
        return new MileageResultCaptchaService(applicationStateManager);
    }

    public static void contributeApplicationDefaults(MappedConfiguration<String, String> configuration) {
        configuration.add(SymbolConstants.SUPPORTED_LOCALES, "de"); //en is ready

        configuration.add(SymbolConstants.APPLICATION_VERSION, "0.0.1-SNAPSHOT");

        configuration.add(SymbolConstants.DEFAULT_STYLESHEET, "context:css/motor-compressed.css");

        configuration.add(SymbolConstants.OMIT_GENERATOR_META, "true");

        configuration.add(SymbolConstants.PRODUCTION_MODE, "false");

        configuration.add(SymbolConstants.EXCEPTION_REPORT_PAGE, ErrorPage.class.getSimpleName());

        configuration.add(MotorApplicationConstants.ShowTrustLevel, "true");

        configuration.add(MotorApplicationConstants.AntiSpambotTime, "1788"); // not too long

        configuration.add(MotorApplicationConstants.IndexPageUrl, "http://www.motorpast.com");

        configuration.add(MotorApplicationConstants.BlockingTime, "7"); // means days

        configuration.add(MotorApplicationConstants.RegistrationDateAttempts, "2"); // stored in db

        configuration.add(MotorApplicationConstants.MaximumRegistrationYearRange, "50"); // 50 years from actual year (2011 - 50 for example)
    }

    /**
     * api said, it will be depracated in 5.3
     * @return customized client-files-provider
     */
    public ClientInfrastructure decorateClientInfrastructure(final ClientInfrastructure original) {
        return new MotorpastClientInfraStructure();
    }

    public RequestExceptionHandler decorateRequestExceptionHandler(
            final Logger logger,
            final ResponseRenderer renderer,
            final Response response,
            final PageRenderLinkSource pageRenderLinkSource,
            final ComponentSource componentSource,
            @Symbol(SymbolConstants.PRODUCTION_MODE) final boolean productionMode,
            final Object service
    ) {
        if (!productionMode) return null;

        final String errorpageName = ErrorPage.class.getSimpleName();
        final Link errorLink = pageRenderLinkSource.createPageRenderLink(ErrorPage.class);

        return new RequestExceptionHandler() {
            public void handleRequestException(Throwable exception) throws IOException {
                final Throwable throwable = getCauseRecursive(exception);
                final ExceptionReporter errorpage = (ExceptionReporter) componentSource.getPage(errorpageName);

                if(throwable instanceof MotorpastException) {
                    errorpage.reportException((MotorpastException) throwable);
                } else {
                    errorpage.reportException(exception);
                }

                response.sendRedirect(errorLink); //TODO: if strange behaviour change this line
                //renderer.renderPageMarkupResponse(errorpageName);
            }
        };
    }

    private Throwable getCauseRecursive(Throwable e) {
        while(e.getCause() != null) {
            e = e.getCause();
        }

        return e;
    }

    public static void contributeComponentEventResultProcessor(
            MappedConfiguration<Class<?>, ComponentEventResultProcessor<HttpStatusCode>> configuration,
            final Response response)
    {
        configuration.add(HttpStatusCode.class, new ComponentEventResultProcessor<HttpStatusCode>() 
        {
            public void processResultValue(HttpStatusCode httpStatusCode) throws IOException {
                if (!httpStatusCode.getLocation().isEmpty())
                    response.setHeader("Location", httpStatusCode.getLocation());
                    response.sendError(httpStatusCode.getStatusCode(), "");
                }
        });
    }
}
