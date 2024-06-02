package com.crazymaker.redission.demo.constant;

/**
 * 全局常量枚举 用来拼接完整的URL
 * @author liudawei
 * @date 2023/9/30
 **/
public enum GlobalConstant {

    REDIS_CONNECTION_PREFIX("redis://", "Redis地址配置前缀");

    private final String constantValue;
    private final String constantDesc;

    GlobalConstant(String constantValue, String constantDesc) {
        this.constantValue = constantValue;
        this.constantDesc = constantDesc;
    }

    public String getConstantValue() {
        return constantValue;
    }

    public String getConstantDesc() {
        return constantDesc;
    }
}
