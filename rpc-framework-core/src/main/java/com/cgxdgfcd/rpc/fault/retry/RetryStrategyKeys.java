package com.cgxdgfcd.rpc.fault.retry;

/**
 * 重试策略键名常量
 */
public interface RetryStrategyKeys {

    String NO = "no";

    String FIXED_INTERVAL = "fixedInterval";

    String EXPONENTIAL_INTERVAL = "exponentialInterval";
}
