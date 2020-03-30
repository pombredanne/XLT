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
package com.gargoylesoftware.htmlunit.javascript.host.css;

import static com.gargoylesoftware.htmlunit.javascript.configuration.SupportedBrowser.CHROME;
import static com.gargoylesoftware.htmlunit.javascript.configuration.SupportedBrowser.FF60;
import static com.gargoylesoftware.htmlunit.javascript.configuration.SupportedBrowser.FF68;

import com.gargoylesoftware.css.dom.CSSMediaRuleImpl;
import com.gargoylesoftware.css.dom.MediaListImpl;
import com.gargoylesoftware.htmlunit.javascript.configuration.JsxClass;
import com.gargoylesoftware.htmlunit.javascript.configuration.JsxConstructor;
import com.gargoylesoftware.htmlunit.javascript.configuration.JsxGetter;
import com.gargoylesoftware.htmlunit.javascript.host.dom.MediaList;

/**
 * A JavaScript object for a {@link CSSMediaRuleImpl}.
 *
 * @author Ronald Brill
 * @author Ahmed Ashour
 */
@JsxClass
public class CSSMediaRule extends CSSConditionRule {

    private MediaList media_;

    /**
     * Creates a new instance.
     */
    @JsxConstructor({CHROME, FF68, FF60})
    public CSSMediaRule() {
    }

    /**
     * Creates a new instance.
     * @param stylesheet the Stylesheet of this rule.
     * @param rule the wrapped rule
     */
    protected CSSMediaRule(final CSSStyleSheet stylesheet, final CSSMediaRuleImpl rule) {
        super(stylesheet, rule);
    }

    /**
     * Returns the media types that the imported CSS style sheet applies to.
     * @return the media types that the imported CSS style sheet applies to
     */
    @JsxGetter
    public MediaList getMedia() {
        if (media_ == null) {
            final CSSStyleSheet parent = getParentStyleSheet();
            final MediaListImpl ml = getMediaRule().getMediaList();
            media_ = new MediaList(parent, ml);
        }
        return media_;
    }

    /**
     * Returns the wrapped rule, as a media rule.
     * @return the wrapped rule, as a media rule
     */
    private CSSMediaRuleImpl getMediaRule() {
        return (CSSMediaRuleImpl) getRule();
    }
}