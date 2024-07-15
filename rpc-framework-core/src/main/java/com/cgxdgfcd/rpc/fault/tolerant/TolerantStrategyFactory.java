package com.cgxdgfcd.rpc.fault.tolerant;

import com.cgxdgfcd.rpc.spi.SpiLoader;

/**
 * 容错策略工厂类
 */
public class TolerantStrategyFactory {

    static {
        SpiLoader.load(TolerantStrategy.class);
    }

    public static final TolerantStrategy DEFAULT_FAIL_TOLERANT_STRATEGY = new FailFastTolerantStrategy();

    public static TolerantStrategy getInstance(String key) {
        return SpiLoader.getInstance(TolerantStrategy.class, key);
    }
}
