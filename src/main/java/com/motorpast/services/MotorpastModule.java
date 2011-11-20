package com.motorpast.services;

import javax.servlet.http.HttpServletRequest;

import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.ioc.MappedConfiguration;
import org.apache.tapestry5.ioc.ServiceBinder;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.annotations.Marker;
import org.apache.tapestry5.ioc.annotations.SubModule;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.services.ApplicationStateManager;
import org.apache.tapestry5.services.ClientInfrastructure;

import com.motorpast.additional.MotorApplicationConstants;
import com.motorpast.annotations.MileageResultCaptcha;
import com.motorpast.dataobjects.CarData;
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

        configuration.add(SymbolConstants.DEFAULT_STYLESHEET, "context:css/motor.css");

        configuration.add(SymbolConstants.OMIT_GENERATOR_META, "true");

        configuration.add(SymbolConstants.PRODUCTION_MODE, "false");

        configuration.add(MotorApplicationConstants.ShowTrustLevel, "true");

        configuration.add(MotorApplicationConstants.AntiSpambotTime, "2619"); // not too long

        configuration.add(MotorApplicationConstants.IndexPageUrl, "http://www.motorpast.com");

        configuration.add(MotorApplicationConstants.BlockingTime, "90"); // means days

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
}
