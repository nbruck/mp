package com.motorpast.dataobjects;

import java.util.Date;

public interface CarData
{
    public String getCarId();
    public void setCarId(final String carId);

    public int getLastMileage();
    public void setLastMileage(final int lastMileage);

    public Date getLastMileageStoringDate();
    public void setLastMileageStoringDate(final Date lastMileageStoringDate);

    public Date getRegistrationdate();
    public void setRegistrationdate(final Date registrationdate);

    public Date getBlockingdate();
    public void setBlockingdate(final Date blockingdate);

    public boolean isShowTrustLevel();
    public void setShowTrustLevel(final boolean showTrustLevel);

    public int getAttemptsLeft();
    public void setAttemptsLeft(final int attempts);

    public long getIdentifier();

    public long getVersion();
}
