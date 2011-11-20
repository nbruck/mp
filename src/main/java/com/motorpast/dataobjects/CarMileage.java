package com.motorpast.dataobjects;

import java.util.Date;

public interface CarMileage
{
    /**
     * getting the unique identifier created by the dbms
     */
    public long getId();

    public CarData getCarData();

    public void setCarData(final CarData carData);

    public int getEarlierStoredMileage();

    public void setEarlierStoredMileage(final int earlierStoredMileage);

    public Date getMileageStoringDate();

    public void setMileageStoringDate(final Date mileageStoringDate);

    public String getIpAddress();

    public void setIpAddress(final String ipAddress);

    public String getHostInfo();

    public void setHostInfo(final String hostInfo);
}
