package com.motorpast.services.security;


public interface SecurityService
{
	/**
     * delivers a valid combo for anti-spambot-textfields
     * first field is carId
     * second field is mileage
     * last two ones are the fakes
     * @return array of numbers... first two are the displayed fields and last ones are fake-fields
     */
    public int[] getCombos();

    /**
     * random css generator
     * @param isHide get css for hiding fields or normal fields
     * @return string of css classes
     */
    public String getRandomCss(final boolean isHide);

    /**
     * generates a unique token for each request
     */
    public String generateToken(/*final HttpServletRequest httpServletRequest*/);

    /**
     * anti-spambot timestamp for validation
     */
    public long generateTimestamp();

    /**
     * new timestamp which considers a time intervall for spambot-detection
     */
    public long generateCheckTimestamp();
}
