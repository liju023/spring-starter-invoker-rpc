package com.i360day.invoker.common;

import java.util.Map;

/**
 * 集合工具类
 */
public class CollectionUtils {
    /**
     * 获取map的key值
     * @param map
     * @param key
     * @return T
     */
    public static <K,T> T getValue(Map<K, T> map, K key){
        return getValue(map, key, null);
    }

    /**
     * 获取map的key值支持默认
     * @param map
     * @param key
     * @param defaultValue
     * @return T
     */
    public static <K,T> T getValue(Map<K, T> map, K key, T defaultValue){
        T value = map.get(key);
        if(ObjectUtils.isEmpty(value)) return defaultValue;
        return value;
    }
}
