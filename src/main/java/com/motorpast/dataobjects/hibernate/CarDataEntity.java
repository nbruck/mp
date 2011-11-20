package com.motorpast.dataobjects.hibernate;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;

import org.hibernate.annotations.Index;
import org.hibernate.annotations.OptimisticLockType;

import com.motorpast.dataobjects.CarData;

@Entity
@Table(name = "tbl_cardata", uniqueConstraints = { @UniqueConstraint(columnNames = {"carId"}) })
@org.hibernate.annotations.Entity(optimisticLock = OptimisticLockType.ALL)
@SequenceGenerator(name = "cardata_gen", sequenceName = "cardata_id_gen",
    initialValue = 1, allocationSize = 50)
public class CarDataEntity implements CarData, Serializable
{
    @Transient
    private static final long serialVersionUID = -2273276130523760322L;


    /**
     * the primary key (id)
     */
    @Id
    @GeneratedValue(generator = "cardata_gen", strategy = GenerationType.AUTO)
    @Column(name = "id")
    private long identifier;

    /**
     * the most important thing, the unique key for a car
     */
    @Index(name = "index_car_id")
    @Column(name = "carId", length = 17)
    private String carId;

    /**
     * return the last stored, the latest!, mileage
     */
    @Column(scale = 9)
    private int lastMileage;

    /**
     * when the last mileage was entered
     */
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date lastMileageStoringDate;

    /**
     * optional attribute for security reasons
     */
    @Temporal(value = TemporalType.DATE)
    private Date registrationdate;

    /**
     * if blocked then date is stored here in format now + blockingdaterange (at the moment 90days)
     */
    @Temporal(value = TemporalType.DATE)
    private Date blockingdate;

    /**
     * in combination with {@link CarDataEntity#registrationdate} it stors the remaining attempts
     */
    @Column(name = "attempts", scale = 1)
    private int attemptsLeft;

    /**
     * turns it on/off
     */
    @Column(name = "showTrust")
    private boolean showTrustLevel;

    /**
     * field for version
     */
    @Version
    private long version;


    public CarDataEntity() {
    }

    public CarDataEntity(
       final String carId,
       final int lastMileage,
       final Date lastMileageStoringDate,
       final Date registrationdate,
       final Date blockingdate,
       final int attempts,
       final boolean showTrustLevel
    ) {
        this.carId = carId;
        this.lastMileage = lastMileage;
        this.lastMileageStoringDate = lastMileageStoringDate;
        this.registrationdate = registrationdate;
        this.blockingdate = blockingdate;
        this.attemptsLeft = attempts;
        this.showTrustLevel = showTrustLevel;
    }


    public String getCarId() {
        return carId;
    }
    public void setCarId(final String carId) {
        this.carId = carId;
    }

    public int getLastMileage() {
        return lastMileage;
    }
    public void setLastMileage(final int lastMileage) {
        this.lastMileage = lastMileage;
    }

    public Date getLastMileageStoringDate() {
        return lastMileageStoringDate;
    }
    public void setLastMileageStoringDate(final Date lastMileageStoringDate) {
        this.lastMileageStoringDate = lastMileageStoringDate;
    }

    public boolean isShowTrustLevel() {
        return showTrustLevel;
    }
    public void setShowTrustLevel(final boolean showTrustLevel) {
        this.showTrustLevel = showTrustLevel;
    }

    public long getIdentifier() {
        return identifier;
    }

    public long getVersion() {
        return version;
    }

    public Date getRegistrationdate() {
        return registrationdate;
    }
    public void setRegistrationdate(final Date registrationdate) {
        this.registrationdate = registrationdate;
    }

    public int getAttemptsLeft() {
        return attemptsLeft;
    }
    public void setAttemptsLeft(final int attempts) {
        this.attemptsLeft = attempts;
    }

    public Date getBlockingdate() {
        return blockingdate;
    }
    public void setBlockingdate(final Date blockingdate) {
        this.blockingdate = blockingdate;
    }

    public String toString() {
        return "CarDataEntity [identifier=" + identifier + ", carId=" + carId
            + ", lastMileage=" + lastMileage + ", lastMileageStoringDate="
            + lastMileageStoringDate + ", registrationdate="
            + registrationdate + ", blockingdate=" + blockingdate
            + ", attemptsLeft=" + attemptsLeft + ", showTrustLevel="
            + showTrustLevel + ", version=" + version + "]";
    }
}
