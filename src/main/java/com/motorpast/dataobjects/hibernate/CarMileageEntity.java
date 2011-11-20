package com.motorpast.dataobjects.hibernate;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.hibernate.annotations.Index;

import com.motorpast.dataobjects.CarData;
import com.motorpast.dataobjects.CarMileage;

@Entity
@Table(name="tbl_stored_mileages")
@SequenceGenerator(name = "mileage_gen", sequenceName = "mileage_id_gen",
    initialValue = 1, allocationSize = 50)
public class CarMileageEntity implements CarMileage, Serializable
{
    @Transient
    private static final long serialVersionUID = -3555837470708269543L;

    @Id
    @GeneratedValue(generator = "mileage_gen", strategy = GenerationType.AUTO)
    @Column(name = "id")
    private long tupelId;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "carTupelId")
    @Index(name = "index_mileage_carid")
    private CarDataEntity carData;

    @Column(name = "storedMileage", scale = 9)
    private int earlierStoredMileage;

    @Column(name = "ip", length = 15)
    private String ipAddress;

    @Column(name = "hostInfo", length = 120)
    private String hostInfo;

    @Temporal(value=TemporalType.TIMESTAMP)
    @Column(name = "storingDate")
    private Date mileageStoringDate;


    public CarMileageEntity() {
    }

    public CarMileageEntity(
        final int mileageToStore,
        final Date storingDate,
        final String ipAddress,
        final String hostInfo
    ) {
        this.earlierStoredMileage = mileageToStore;
        this.mileageStoringDate = storingDate;
        this.ipAddress = ipAddress;
        this.hostInfo = hostInfo;
    }


    public long getId() {
        return tupelId;
    }

    public CarDataEntity getCarData() {
        return carData;
    }

    public void setCarData(final CarData carData) {
        this.carData = (CarDataEntity)carData;
    }

    public int getEarlierStoredMileage() {
        return earlierStoredMileage;
    }

    public void setEarlierStoredMileage(final int earlierStoredMileage) {
        this.earlierStoredMileage = earlierStoredMileage;
    }

    public Date getMileageStoringDate() {
        return mileageStoringDate;
    }

    public void setMileageStoringDate(final Date mileageStoringDate) {
        this.mileageStoringDate = mileageStoringDate;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(final String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getHostInfo() {
        return hostInfo;
    }

    public void setHostInfo(final String hostInfo) {
        this.hostInfo = hostInfo;
    }

    @Override
    public String toString() {
        return "CarMileageEntity [tupelId=" + tupelId + ", carData=" + carData
            + ", earlierStoredMileage=" + earlierStoredMileage
            + ", ipAddress=" + ipAddress + ", hostInfo=" + hostInfo
            + ", mileageStoringDate=" + mileageStoringDate + "]";
    }
}
