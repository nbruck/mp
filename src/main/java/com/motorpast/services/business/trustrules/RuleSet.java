package com.motorpast.services.business.trustrules;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.motorpast.dataobjects.CarData;
import com.motorpast.dataobjects.CarMileage;

public abstract class RuleSet
{
    private final Logger logger;

    /**
     * neccessary data for trustlevel-weight
     */
    protected CarData carData;

    /**
     * also needed data
     */
    protected List<CarMileage> mileages;

    /**
     * a bulk of rules
     */
    protected TrustRule[] trustRules;

    /**
     * the final trustlevel of a car
     */
    protected int trustLevel;


    public RuleSet() {
        logger = LoggerFactory.getLogger(RuleSet.class);
    }


    /**
     * delivers the formatted trustlevel, for example 91% or 9/10
     */
    public abstract String getFormattedTrustLevel();


    public void calculateTrustLevel() {
        for(TrustRule currentRule : trustRules) {
            logger.debug("now processing rule=" + currentRule.getRuleName());
            trustLevel += currentRule.getValueForRule();
        }
    }

    public final CarData getCarData() {
        return carData;
    }

    public final List<CarMileage> getCarMileages() {
        return mileages;
    }

    public final Logger getLogger() {
        return logger;
    }
}
