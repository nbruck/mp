package com.motorpast.services.business.trustrules;

import java.util.List;

import org.slf4j.Logger;

import com.motorpast.dataobjects.CarData;
import com.motorpast.dataobjects.CarMileage;


public abstract class TrustRule
{
    private final int weighting;

    private final String ruleName;

    private final RuleSet ruleSet;


    public TrustRule(
        final int weighting,
        final String ruleName,
        final RuleSet ruleSet
    ) {
        this.weighting = weighting;
        this.ruleName = ruleName;
        this.ruleSet = ruleSet;
    }


    public abstract boolean processRule();


    public final int getRuleWeighting() {
        return weighting;
    }

    public final String getRuleName() {
        return ruleName;
    }

    public final List<CarMileage> getCarMileages() {
        return ruleSet.getCarMileages();
    }

    public final CarData getCarData() {
        return ruleSet.getCarData();
    }

    public final Logger log() {
        return ruleSet.getLogger();
    }

    /**
     * @return only {@link TrustRule#weighting} if success OR! 0 for rule not passed
     */
    int getValueForRule() {
        if(processRule()) {
            return weighting;
        } else {
            return 0;
        }
    }
}
