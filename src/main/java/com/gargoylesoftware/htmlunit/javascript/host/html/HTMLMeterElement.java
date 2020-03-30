/*
 * Copyright (c) 2002-2020 Gargoyle Software Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.gargoylesoftware.htmlunit.javascript.host.html;

import static com.gargoylesoftware.htmlunit.javascript.configuration.SupportedBrowser.CHROME;
import static com.gargoylesoftware.htmlunit.javascript.configuration.SupportedBrowser.FF60;
import static com.gargoylesoftware.htmlunit.javascript.configuration.SupportedBrowser.FF68;

import com.gargoylesoftware.htmlunit.html.HtmlMeter;
import com.gargoylesoftware.htmlunit.javascript.configuration.JsxClass;
import com.gargoylesoftware.htmlunit.javascript.configuration.JsxConstructor;
import com.gargoylesoftware.htmlunit.javascript.configuration.JsxGetter;
import com.gargoylesoftware.htmlunit.javascript.host.dom.AbstractList;

/**
 * The JavaScript object {@code HTMLMeterElement}.
 *
 * @author Marc Guillemot
 * @author Ronald Brill
 * @author Ahmed Ashour
 */
@JsxClass(domClass = HtmlMeter.class, value = {FF68, FF60, CHROME})
public class HTMLMeterElement extends HTMLElement {

    /** "Live" labels collection; has to be a member to have equality (==) working. */
    private AbstractList labels_;

    /**
     * Creates an instance.
     */
    @JsxConstructor({CHROME, FF68, FF60})
    public HTMLMeterElement() {
    }

    /**
     * The getter for the "value" property.
     * @return the value
     */
    @JsxGetter
    public Double getValue() {
        return getAttributeAsDouble("value", 0);
    }

    /**
     * The getter for the "min" property.
     * @return the value
     */
    @JsxGetter
    public double getMin() {
        return getAttributeAsDouble("min", 0);
    }

    /**
     * The getter for the "max" property.
     * @return the value
     */
    @JsxGetter
    public double getMax() {
        return getAttributeAsDouble("max", 1);
    }

    /**
     * The getter for the "low" property.
     * @return the value
     */
    @JsxGetter
    public double getLow() {
        final double val = getAttributeAsDouble("low", Double.MAX_VALUE);
        if (val == Double.MAX_VALUE) {
            return getMin();
        }
        return val;
    }

    /**
     * The getter for the "high" property.
     * @return the value
     */
    @JsxGetter
    public double getHigh() {
        final double val = getAttributeAsDouble("high", Double.MIN_VALUE);
        if (val == Double.MIN_VALUE) {
            return getMax();
        }
        return val;
    }

    /**
     * The getter for the "optimum" property.
     * @return the value
     */
    @JsxGetter
    public double getOptimum() {
        final double val = getAttributeAsDouble("optimum", Double.MAX_VALUE);
        if (val == Double.MAX_VALUE) {
            return getValue();
        }
        return val;
    }

    private double getAttributeAsDouble(final String attributeName, final double defaultValue) {
        try {
            return Double.parseDouble(getDomNodeOrDie().getAttribute(attributeName));
        }
        catch (final NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Returns the labels associated with the element.
     * @return the labels associated with the element
     */
    @JsxGetter({CHROME, FF68, FF60})
    public AbstractList getLabels() {
        if (labels_ == null) {
            labels_ = new LabelsHelper(getDomNodeOrDie());
        }
        return labels_;
    }

}