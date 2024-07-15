package com.cgxdgfcd.rpc.fault.tolerant;

import com.cgxdgfcd.rpc.spi.SpiLoader;

/**
 * 模拟服务工厂类
 */
public class MockServiceFactory {

    static {
        SpiLoader.load(MockService.class);
    }

    public static final MockService DEFAULT_MOCK_SERVICE = new DefaultMockService();

    public static MockService getInstance(String key) {
        return SpiLoader.getInstance(MockService.class, key);
    }
}
