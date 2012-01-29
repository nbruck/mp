package com.motorpast.dataobjects;

public class UserSessionObj
{
    /**
     * text for the mielagecaptcha
     */
    private String mileageResultCaptcha;

    /**
     * the trustlevel of a fid/mileage
     */
    private String trustLevel;

    /**
     * CSRF
     */
    private String uniqueToken;

    /**
     * for checking against spambots
     */
    private String timestamp;


    public final void testForCreation() {
        //do nothing
    }

    public final boolean isMileageAvailable() {
        return mileageResultCaptcha != null;
    }

    public final String getMileageResultCaptcha() {
        final String mileageResultForReturn = String.valueOf(mileageResultCaptcha);
        mileageResultCaptcha = null;

        return mileageResultForReturn;
    }
    public final void setMileageResultCaptcha(final String mileageResultCaptcha) {
        this.mileageResultCaptcha = mileageResultCaptcha;
    }

    public final String getTrustLevel() {
        return trustLevel;
    }
    public final void setTrustLevel(final String trustLevel) {
        this.trustLevel = trustLevel;
    }

    public final String getUniqueToken() {
        return uniqueToken;
    }
    public final void setUniqueToken(final String uniqueToken) {
        this.uniqueToken = uniqueToken;
    }

    public final String getTimestamp() {
        return timestamp;
    }
    public final void setTimestamp(final String timestamp) {
        this.timestamp = timestamp;
    }
}
