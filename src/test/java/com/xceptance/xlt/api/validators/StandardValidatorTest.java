package com.xceptance.xlt.api.validators;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.MockWebConnection;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

/**
 * Tests the implementation of {@link StandardValidator}.
 * 
 * @author sebastianloob
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(
    {
        HtmlPage.class, WebResponse.class
    })
public class StandardValidatorTest
{
    private final StandardValidator validator = StandardValidator.getInstance();

    /**
     * Well formed XHTML.
     */
    private final static String htmlSource = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.1//EN\" \"http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd\">"
                                             + "<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en\">"
                                             + "<head>"
                                             + "   <title>A test title</title>"
                                             + "</head>"
                                             + "<body>"
                                             + "   <div id=\"container\">"
                                             + "       <h1>Test</h1>" + "   </div>" + "</body>" + "</html>";

    /**
     * Verifies the correct implementation of {@link StandardValidator#validate(HtmlPage)}.
     * 
     * @throws Throwable
     */
    @Test
    public void testValidate() throws Throwable
    {
        try (final WebClient webClient = new WebClient())
        {
            final MockWebConnection connection = new MockWebConnection();
            connection.setDefaultResponse(htmlSource);
            webClient.setWebConnection(connection);

            final HtmlPage page = webClient.getPage("http://localhost/");
            validator.validate(page);
        }
    }

    /**
     * Verifies, that an exception is thrown, if the html page is {@code null}.
     * 
     * @throws Exception
     */
    @Test(expected = AssertionError.class)
    public void testValidatePageIsNull() throws Exception
    {
        validator.validate((HtmlPage) null);
    }

    /**
     * Verifies, that an exception is thrown, if the html page is not closed with {@code </html>}.
     * 
     * @throws Exception
     */
    @Test(expected = AssertionError.class)
    public void testValidatePageNotClosedWithHtmlTag() throws Exception
    {
        final String source = "<html>" + "<head>" + "</head>" + "<body>" + "</body>";

        try (final WebClient webClient = new WebClient(BrowserVersion.CHROME))
        {
            final MockWebConnection connection = new MockWebConnection();
            connection.setDefaultResponse(source);
            webClient.setWebConnection(connection);

            final HtmlPage page = webClient.getPage("http://localhost/");
            validator.validate(page);
        }
    }
}