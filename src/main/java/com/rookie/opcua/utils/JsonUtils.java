package com.rookie.opcua.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.List;

/**
 * JSON工具类
 *
 * @author yugo
 * 2018/12/5
 */
public class JsonUtils {

    /**
     * json字符串转换为bean对象
     *
     * @param json  json串
     * @param clazz class
     * @param <T>   泛型
     * @author yugo
     * 2018/12/5
     */
    public static <T> T jsonToBean(String json, Class<T> clazz) {
        try {
            return JSONObject.parseObject(json, clazz);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * bean 对象序列化json字符串
     *
     * @param clazz 对象
     * @return java.lang.String
     * @author yugo
     * 2018/12/5
     */
    public static <T> String beanToJson(T clazz) {
        return JSONObject.toJSONString(clazz);
    }

    /**
     * arrayList 对象序列化json字符串
     *
     * @param list 对象
     * @return java.lang.String
     * @author yugo
     * 2018/12/5
     */
    public static <T> String listToJson(List<T> list) {
        return JSON.toJSONString(list);
    }

    /**
     * json字符串反序列化list
     *
     * @param json  字符串
     * @param clazz 对象
     * @return java.lang.String
     * @author yugo
     * 2018/12/5
     */
    public static <T> List<T> jsonToList(String json, Class<T> clazz) {
        @SuppressWarnings("unchecked")
        List<T> list = (List<T>) JSONArray.parseArray(json, clazz);
        return list;
    }
}
