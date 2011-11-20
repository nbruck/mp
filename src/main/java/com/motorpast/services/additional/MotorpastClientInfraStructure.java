package com.motorpast.services.additional;

import java.util.Collections;
import java.util.List;

import org.apache.tapestry5.Asset;
import org.apache.tapestry5.services.ClientInfrastructure;

/**
 * we need this class to decorate the default tapestry {@link ClientInfrastructure}
 * because we don't want to include those js and css stuff
 */
@SuppressWarnings("deprecation")
public class MotorpastClientInfraStructure implements ClientInfrastructure
{
    private final List<Asset> cssStack;
    private final List<Asset> jsStack;

    public MotorpastClientInfraStructure() {
        this.cssStack = Collections.emptyList();
        this.jsStack = Collections.emptyList();
    }

    public List<Asset> getJavascriptStack() {
        return jsStack;
    }

    public List<Asset> getStylesheetStack() {
        return cssStack;
    }
}