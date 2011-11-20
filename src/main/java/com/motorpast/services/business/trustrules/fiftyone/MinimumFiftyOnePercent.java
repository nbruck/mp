package com.motorpast.services.business.trustrules.fiftyone;

import java.util.List;

import com.motorpast.dataobjects.CarMileage;
import com.motorpast.services.business.trustrules.RuleSet;
import com.motorpast.services.business.trustrules.TrustRule;

/**
 * the minimum requirement is, that all data are consistent... meaning a later stored mileage
 * isn't lower than an earlier stored one
 * weight is 51 (51%)
 */
public class MinimumFiftyOnePercent extends TrustRule
{
    private final static String ruleName = "51-percent-minimum";

    public final static int ruleWeight = 51;

    public MinimumFiftyOnePercent(final RuleSet ruleSet) {
        super(
            ruleWeight,
            ruleName,
            ruleSet
        );
    }


    public boolean processRule() {
        // in this case we got the mileages ordered by storing date desc (latest first)!!!
        final List<CarMileage> mileages = super.getCarMileages();
        int latestMileage = super.getCarData().getLastMileage();

        if(mileages == null || mileages.size() == 1) { // mmh, maybe delete null-check?
            return true;
        }

        for(int i = 1; i < mileages.size(); i++) {
            final int currentMileage = mileages.get(i).getEarlierStoredMileage();
            super.log().debug("51-percent-rule -> current mileage=" + currentMileage);

            if(latestMileage >= currentMileage) {
                latestMileage = currentMileage;
            } else {
                return false;
            }
        }

        return true;
    }
}
