package com.motorpast.services.business.trustrules.fiftyone;

import java.util.List;

import com.motorpast.dataobjects.CarData;
import com.motorpast.dataobjects.CarMileage;
import com.motorpast.services.business.trustrules.RuleSet;
import com.motorpast.services.business.trustrules.TrustRule;

public class FiftyOnePercentRuleSet extends RuleSet
{
    public FiftyOnePercentRuleSet(
        final CarData carData,
        final List<CarMileage> mileages
    ) {
        this.carData = carData;
        this.mileages = mileages;

        trustRules = new TrustRule[] {
            new MinimumFiftyOnePercent(this)
        };
    }


    @Override
    public String getFormattedTrustLevel() {
        return trustLevel + "%";
    }
}
