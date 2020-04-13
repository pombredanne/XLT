package com.xceptance.xlt.report.mergerules;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.xceptance.common.collection.LRUHashMap;
import com.xceptance.common.lang.ThrowableUtils;
import com.xceptance.common.util.RegExUtils;
import com.xceptance.xlt.api.engine.RequestData;

/**
 * Base class for all request filters that use regular expressions to identify matching requests.
 */
public abstract class AbstractPatternRequestFilter extends AbstractRequestFilter
{
    /**
     * Cache the expensive stuff but with little sync overhead
     */
    private ThreadLocal<Map<String, Object>> cache = new ThreadLocal<Map<String, Object>>() 
    {
        @Override 
        protected Map<String, Object> initialValue() 
        {
            return new LRUHashMap<>(1001);
        }
    };

    /**
     * Just a place holder for a NULL
     */
    private static final Object NULL = new Object();

    /**
     * The pattern this filter uses.
     */
    private final Pattern pattern;

    /**
     * Whether or not this is an exclusion rule.
     */
    private final boolean isExclude;

    /**
     * Constructor.
     *
     * @param typeCode
     *            the type code of this request filter
     * @param regex
     *            the regular expression to identify matching requests
     */
    public AbstractPatternRequestFilter(final String typeCode, final String regex)
    {
        this(typeCode, regex, false);
    }

    /**
     * Constructor.
     *
     * @param typeCode
     *            the type code of this request filter
     * @param regex
     *            the regular expression to identify matching requests
     * @param exclude
     *            whether or not this is an exclusion rule
     */
    public AbstractPatternRequestFilter(final String typeCode, final String regex, final boolean exclude)
    {
        super(typeCode);

        if (StringUtils.isBlank(regex))
        {
            pattern = null;
        }
        else
        {
            pattern = RegExUtils.getPattern(regex, 0);
        }
        isExclude = exclude;
    }

    /**
     * Returns the text to examine from the passed request data object.d
     *
     * @return the text
     */
    protected abstract String getText(RequestData requestData);

    /**
     * {@inheritDoc}
     */
    @Override
    public Object appliesTo(final RequestData requestData)
    {
        if (pattern == null)
        {
            // empty is always fine, we just want to get the full text -> return a non-null dummy object
            return Boolean.TRUE;
        }

        // get the data to match against
        final String text = getText(requestData);

        // get us a local reference to the cache
        final Map<String, Object> cache = this.cache.get();

        Object result = cache.get(text);
        if (result == null)
        {
            // not found, produce and cache
            final Matcher matcher = pattern.matcher(getText(requestData));

            result = (matcher.find() ^ isExclude) ? matcher : NULL;
            cache.put(text, result);
        }

        // ok, we got one, just see if this is NULL or a match
        if (result == NULL)
        {
            return null;
        }
        else
        {
            return ((Matcher) result).toMatchResult();
        }
    }

    /**
     * {@inheritDoc}
     */ 
    @Override
    public String getReplacementText(final RequestData requestData, final int capturingGroupIndex, final Object filterState)
    {
        if (isExclude || pattern == null || capturingGroupIndex == -1)
        {
            return getText(requestData);
        }

        try
        {
            return ((MatchResult) filterState).group(capturingGroupIndex);
        }
        catch (final IndexOutOfBoundsException ioobe)
        {
            final String format = "No matching group %d for input string '%s' and pattern '%s'";
            ThrowableUtils.setMessage(ioobe, String.format(format, capturingGroupIndex, getText(requestData), getPattern()));

            throw ioobe;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder("{ type: '");
        sb.append(getTypeCode()).append("', ");
        sb.append("pattern: '").append(getPattern()).append("', ");
        sb.append("isExclude: ").append(isExclude).append(" }");

        return sb.toString();
    }

    /**
     * Returns the filter pattern string.
     */
    public String getPattern()
    {
        return (pattern == null) ? StringUtils.EMPTY : pattern.pattern();
    }

    /**
     * Whether this filter has an empty pattern.
     */
    public boolean isEmpty()
    {
        return pattern == null;
    }

    /**
     * Whether this filter is an exclude filter.
     */
    public boolean isExclude()
    {
        return isExclude;
    }
}
