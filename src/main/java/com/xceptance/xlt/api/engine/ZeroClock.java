package com.xceptance.xlt.api.engine;

/**
 * <p>
 * This clock is meant to aid report creation be lowering the overhead. It will always
 * tell you 0 as time.
 */
public class ZeroClock extends GlobalClock
{
    /**
     * The one and only instance.
     */
    private static final ZeroClock singleton = new ZeroClock();

    /**
     * Returns the GlobalClock singleton.
     * 
     * @return the global clock
     */
    public static ZeroClock getInstance()
    {
        return singleton;
    }

    /**
     * Returns always 0
     * 
     * @return 0
     */
    public long getTime()
    {
        return 0;
    }
}
