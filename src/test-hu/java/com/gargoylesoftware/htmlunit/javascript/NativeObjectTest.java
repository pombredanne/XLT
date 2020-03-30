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
package com.gargoylesoftware.htmlunit.javascript;

import static com.gargoylesoftware.htmlunit.BrowserRunner.TestedBrowser.FF60;
import static com.gargoylesoftware.htmlunit.BrowserRunner.TestedBrowser.IE;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.gargoylesoftware.htmlunit.BrowserRunner;
import com.gargoylesoftware.htmlunit.BrowserRunner.Alerts;
import com.gargoylesoftware.htmlunit.BrowserRunner.HtmlUnitNYI;
import com.gargoylesoftware.htmlunit.BrowserRunner.NotYetImplemented;
import com.gargoylesoftware.htmlunit.WebDriverTestCase;

/**
 * Object is a native JavaScript object and therefore provided by Rhino but some tests are needed here
 * to be sure that we have the expected results.
 *
 * @author Marc Guillemot
 * @author Frank Danek
 * @author Ahmed Ashour
 * @author Natasha Lazarova
 * @author Ronald Brill
 */
@RunWith(BrowserRunner.class)
public class NativeObjectTest extends WebDriverTestCase {

    /**
     * Test for the methods with the same expectations for all browsers.
     * @throws Exception if the test fails
     */
    @Test
    @Alerts({"assign: undefined", "constructor: function", "create: undefined", "defineProperties: undefined",
                "defineProperty: undefined", "freeze: undefined", "getOwnPropertyDescriptor: undefined",
                "getOwnPropertyNames: undefined", "getPrototypeOf: undefined", "hasOwnProperty: function",
                "isExtensible: undefined", "isFrozen: undefined", "isPrototypeOf: function", "isSealed: undefined",
                "keys: undefined", "preventExtensions: undefined", "propertyIsEnumerable: function", "seal: undefined",
                "toLocaleString: function", "toString: function", "valueOf: function", "__defineGetter__: function",
                "__defineSetter__: function", "__lookupGetter__: function", "__lookupSetter__: function"})
    public void common() throws Exception {
        final String[] methods = {"assign", "constructor", "create", "defineProperties", "defineProperty", "freeze",
            "getOwnPropertyDescriptor", "getOwnPropertyNames", "getPrototypeOf", "hasOwnProperty", "isExtensible",
            "isFrozen", "isPrototypeOf", "isSealed", "keys", "preventExtensions", "propertyIsEnumerable", "seal",
            "toLocaleString", "toString", "valueOf", "__defineGetter__", "__defineSetter__",
            "__lookupGetter__", "__lookupSetter__"};
        final String html = NativeDateTest.createHTMLTestMethods("new Object()", methods);
        loadPageWithAlerts2(html, 2 * DEFAULT_WAIT_TIME);
    }

    /**
     * Test for the methods with the different expectations depending on the browsers.
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "toSource: undefined",
            FF = "toSource: function",
            FF68 = "toSource: function",
            FF60 = "toSource: function")
    public void others() throws Exception {
        final String[] methods = {"toSource"};
        final String html = NativeDateTest.createHTMLTestMethods("new Object()", methods);
        loadPageWithAlerts2(html);
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "1",
            IE = {})
    public void assign() throws Exception {
        final String html
            = "<html><head><script>\n"
            + "function test() {\n"
            + "  if (Object.assign) {\n"
            + "    var obj = { a: 1 };\n"
            + "    var copy = Object.assign({}, obj);\n"
            + "    alert(copy.a);\n"
            + "  }\n"
            + "}\n"
            + "</script></head><body onload='test()'>\n"
            + "</body></html>";

        loadPageWithAlerts2(html);
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "1",
            IE = {})
    public void assignUndefined() throws Exception {
        final String html
            = "<html><head><script>\n"
            + "function test() {\n"
            + "  if (Object.assign) {\n"
            + "    var obj = { a: 1 };\n"
            + "    var copy = Object.assign({}, undefined, obj);\n"
            + "    alert(copy.a);\n"
            + "  }\n"
            + "}\n"
            + "</script></head><body onload='test()'>\n"
            + "</body></html>";

        loadPageWithAlerts2(html);
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "undefined",
            IE = {})
    public void assignUndefined2() throws Exception {
        final String html
                = "<html><head><script>\n"
                + "function test() {\n"
                + "  if (Object.assign) {\n"
                + "    var copy = Object.assign({}, undefined, undefined);\n"
                + "    alert(copy.a);\n"
                + "  }\n"
                + "}\n"
                + "</script></head><body onload='test()'>\n"
                + "</body></html>";

        loadPageWithAlerts2(html);
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "undefined",
            IE = {})
    public void assignNull() throws Exception {
        final String html
                = "<html><head><script>\n"
                + "function test() {\n"
                + "  if (Object.assign) {\n"
                + "    var copy = Object.assign({}, null);\n"
                + "    alert(copy.a);\n"
                + "  }\n"
                + "}\n"
                + "</script></head><body onload='test()'>\n"
                + "</body></html>";

        loadPageWithAlerts2(html);
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "undefined",
            IE = {})
    public void assignNull2() throws Exception {
        final String html
                = "<html><head><script>\n"
                + "function test() {\n"
                + "  if (Object.assign) {\n"
                + "    var copy = Object.assign({}, null, null);\n"
                + "    alert(copy.a);\n"
                + "  }\n"
                + "}\n"
                + "</script></head><body onload='test()'>\n"
                + "</body></html>";

        loadPageWithAlerts2(html);
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(CHROME = "function () { [native code] }",
            FF60 = "function () {\n}",
            FF68 = "function () {\n    [native code]\n}",
            IE = "\nfunction() {\n    [native code]\n}\n")
    @NotYetImplemented({FF60, IE})
    public void proto() throws Exception {
        final String html = ""
            + "<html><head>\n"
            + "<script>\n"
            + "  function test() {\n"
            + "    alert(Object.__proto__);\n"
            + "  }\n"
            + "</script>\n"
            + "</head>\n"
            + "<body onload='test()'>\n"
            + "</body></html>";

        loadPageWithAlerts2(html);
    }

    /**
     * Test case for Bug #1856.
     *
     * @throws Exception if the test fails
     */
    @Test
    @Alerts({"[object Object]", "null"})
    public void proto2() throws Exception {
        final String html = ""
            + "<html><head>\n"
            + "<script>\n"
            + "  function test() {\n"
            + "    alert({}.__proto__);\n"
            + "    alert({}.__proto__.__proto__);\n"
            + "  }\n"
            + "</script>\n"
            + "</head>\n"
            + "<body onload='test()'>\n"
            + "</body></html>";

        loadPageWithAlerts2(html);
    }

    /**
     * Test case for #1855.
     *
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "true",
            IE = "exception")
    @NotYetImplemented(IE)
    public void getPrototypeOfString() throws Exception {
        final String html = ""
            + "<html><head>\n"
            + "<script>\n"
            + "  function test() {\n"
            + "    try {\n"
            + "      alert(String.prototype === Object.getPrototypeOf(''));\n"
            + "    } catch(e) {alert('exception')}\n"
            + "  }\n"
            + "</script>\n"
            + "</head>\n"
            + "<body onload='test()'>\n"
            + "</body></html>";

        loadPageWithAlerts2(html);
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "true",
            IE = "exception")
    @NotYetImplemented(IE)
    public void getPrototypeOfNumber() throws Exception {
        final String html = ""
            + "<html><head>\n"
            + "<script>\n"
            + "  function test() {\n"
            + "    try {\n"
            + "      alert(Number.prototype === Object.getPrototypeOf(1));\n"
            + "    } catch(e) {alert('exception')}\n"
            + "  }\n"
            + "</script>\n"
            + "</head>\n"
            + "<body onload='test()'>\n"
            + "</body></html>";

        loadPageWithAlerts2(html);
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "true",
            IE = "exception")
    @NotYetImplemented(IE)
    public void getPrototypeOfBoolean() throws Exception {
        final String html = ""
            + "<html><head>\n"
            + "<script>\n"
            + "  function test() {\n"
            + "    try {\n"
            + "      alert(Boolean.prototype === Object.getPrototypeOf(true));\n"
            + "    } catch(e) {alert('exception')}\n"
            + "  }\n"
            + "</script>\n"
            + "</head>\n"
            + "<body onload='test()'>\n"
            + "</body></html>";

        loadPageWithAlerts2(html);
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = "object",
            IE = "exception")
    @NotYetImplemented(IE)
    public void getTypeOfPrototypeOfNumber() throws Exception {
        final String html = ""
            + "<html><head>\n"
            + "<script>\n"
            + "  function test() {\n"
            + "    try {\n"
            + "      alert(typeof Object.getPrototypeOf(1));\n"
            + "    } catch(e) {alert('exception')}\n"
            + "  }\n"
            + "</script>\n"
            + "</head>\n"
            + "<body onload='test()'>\n"
            + "</body></html>";

        loadPageWithAlerts2(html);
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = {"2", "true", "true"},
            IE = "exception")
    public void getOwnPropertySymbols() throws Exception {
        final String html = ""
            + "<html><head>\n"
            + "<script>\n"
            + "  function test() {\n"
            + "    try {\n"
            + "      var obj = {};\n"
            + "      var a = Symbol('a');\n"
            + "      var b = Symbol.for('b');\n"
            + "\n"
            + "      obj[a] = 'localSymbol';\n"
            + "      obj[b] = 'globalSymbol';\n"
            + "\n"
            + "      var objectSymbols = Object.getOwnPropertySymbols(obj);\n"
            + "      alert(objectSymbols.length);\n"
            + "      alert(objectSymbols[0] === a);\n"
            + "      alert(objectSymbols[1] === b);\n"
            + "    } catch(e) {alert('exception')}\n"
            + "  }\n"
            + "</script>\n"
            + "</head>\n"
            + "<body onload='test()'>\n"
            + "</body></html>";

        loadPageWithAlerts2(html);
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts("exception")
    public void getOwnPropertySymbolsEmpty() throws Exception {
        final String html = ""
            + "<html><head>\n"
            + "<script>\n"
            + "  function test() {\n"
            + "    try {\n"
            + "      var objectSymbols = Object.getOwnPropertySymbols();\n"
            + "      alert(objectSymbols.length);\n"
            + "    } catch(e) {alert('exception')}\n"
            + "  }\n"
            + "</script>\n"
            + "</head>\n"
            + "<body onload='test()'>\n"
            + "</body></html>";

        loadPageWithAlerts2(html);
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(DEFAULT = {"[object HTMLInputElement]", "[object HTMLInputElementPrototype]",
                        "[object Object]", "function"},
            CHROME = {"[object HTMLInputElement]", "[object HTMLInputElement]", "[object Object]", "function"})
    @HtmlUnitNYI(FF60 = {"[object HTMLInputElement]", "[object HTMLInputElement]", "[object Object]", "function"},
            FF68 = {"[object HTMLInputElement]", "[object HTMLInputElement]", "[object Object]", "function"},
            IE = {"[object HTMLInputElement]", "[object HTMLInputElement]", "[object Object]", "function"})
    public void getOwnPropertyDescriptor() throws Exception {
        final String html = ""
            + "<html><head>\n"
            + "<script>\n"
            + "  function test() {\n"
            + "    try {\n"
            + "      var input = document.getElementById('myInput');\n"
            + "      alert(input);\n"
            + "      var proto = input.constructor.prototype;\n"
            + "      alert(proto);\n"
            + "      var desc = Object.getOwnPropertyDescriptor(proto, 'value');\n"
            + "      alert(desc);\n"

            + "      alert(typeof desc.get);\n"
            + "    } catch(e) {alert('exception')}\n"
            + "  }\n"
            + "</script>\n"
            + "</head>\n"
            + "<body onload='test()'>\n"
            + "  <input id='myInput' value='some test'>\n"
            + "</body></html>";

        loadPageWithAlerts2(html);
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    @Alerts(CHROME = {"[object HTMLInputElement]", "x = [object Object]",
                        "x.get = function get value() { [native code] }",
                        "x.get.call = function call() { [native code] }"},
            FF68 = {"[object HTMLInputElementPrototype]", "x = [object Object]",
                        "x.get = function value() {\n    [native code]\n}",
                        "x.get.call = function call() {\n    [native code]\n}"},
            FF60 = {"[object HTMLInputElementPrototype]", "x = [object Object]",
                        "x.get = function get value() {\n    [native code]\n}",
                        "x.get.call = function call() {\n    [native code]\n}"},
            IE = {"[object HTMLInputElementPrototype]", "x = [object Object]",
                        "x.get = \nfunction value() {\n    [native code]\n}\n",
                        "x.get.call = \nfunction call() {\n    [native code]\n}\n"})
    @HtmlUnitNYI(CHROME = {"[object HTMLInputElement]", "x = [object Object]",
                        "x.get = function value() { [native code] }",
                        "x.get.call = function call() { [native code] }"},
            FF68 = {"[object HTMLInputElement]", "x = [object Object]",
                        "x.get = function value() {\n    [native code]\n}",
                        "x.get.call = function call() {\n    [native code]\n}"},
            FF60 = {"[object HTMLInputElement]", "x = [object Object]",
                        "x.get = function value() {\n    [native code]\n}",
                        "x.get.call = function call() {\n    [native code]\n}"},
            IE = {"[object HTMLInputElement]", "x = [object Object]",
                    "x.get = \nfunction value() {\n    [native code]\n}\n",
                    "x.get.call = \nfunction call() {\n    [native code]\n}\n"})
    public void getOwnPropertyDescriptorGetCall() throws Exception {
        final String html = "<html><head><title>foo</title><script>\n"
            + "function test() {\n"
            + "  var proto = i1.constructor.prototype;\n"
            + "  alert(proto);\n"
            + "  var x = Object.getOwnPropertyDescriptor(i1.constructor.prototype, 'value');\n"
            + "  alert('x = ' + x);\n"
            + "  alert('x.get = ' + x.get);\n"
            + "  alert('x.get.call = ' + x.get.call);\n"
            + "}\n"
            + "</script></head>\n"
            + "<body onload='test()'>\n"
            + "  <input type='text' id='i1' value='foo' />\n"
            + "</body></html>";

        loadPageWithAlerts2(html);
    }
}