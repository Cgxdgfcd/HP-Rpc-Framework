package com.cgxdgfcd.rpc.springboot.starter.annotation;

import com.cgxdgfcd.rpc.constant.RpcConstant;
import com.cgxdgfcd.rpc.fault.retry.RetryStrategyKeys;
import com.cgxdgfcd.rpc.fault.tolerant.TolerantStrategyKeys;
import com.cgxdgfcd.rpc.loadbalancer.LoadBalancerKeys;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 服务消费者注解（用于注入服务）
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface RpcReference {

    /**
     * 服务接口类
     *
     * @return
     */
    Class<?> interfaceClass() default void.class;

    /**
     * 版本
     *
     * @return
     */
    String serviceVersion() default RpcConstant.DEFAULT_SERVICE_VERSION;

    /**
     * 负载均衡器
     *
     * @return
     */
    String loadBalancer() default LoadBalancerKeys.ROUND_ROBIN;

    /**
     * 重试策略
     *
     * @return
     */
    String retryStrategy() default RetryStrategyKeys.NO;

    /**
     * 容错策略
     *
     * @return
     */
    String tolerantStrategy() default TolerantStrategyKeys.FAIL_FAST;
}
