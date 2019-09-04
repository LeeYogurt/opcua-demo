package com.rookie.opcua.utils;

import org.apache.commons.configuration.ConversionException;
import org.springframework.beans.BeanUtils;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author yugo
 */
public class BeanConvertUtils {

    /**
     * 将t中的对象复制到v
     *
     * @param t
     * @param v
     * @param <T>
     * @param <V>
     */
    public static <T, V> V tToV(T t, V v) {
        if (t == null || v == null) {
            return v;
        }
        BeanUtils.copyProperties(t, v);
        return v;
    }

    /**
     * 批量复制对象
     *
     * @param ts
     * @param targetCls
     * @param <T>
     * @param <V>
     * @return
     */
    public static <T, V> List<V> tToV(List<T> ts, Class<V> targetCls) {
        if (ts == null) {
            return null;
        }
        List<V> vs = new ArrayList<V>();
        for (T t : ts) {
            vs.add(tToV(t, targetCls));
        }
        return vs;
    }

    /**
     * @param targetCls
     * @param v
     * @param <T>
     * @param <V>
     * @return
     */
    public static <T, V> T tToV(V v, Class<T> targetCls) {
        T t = null;
        if (v == null) {
            return t;
        }
        try {
            t = targetCls.newInstance();
        } catch (Exception e) {
            throw new ConversionException("Bean 转化异常，请生成 " + targetCls.getName() + "的构造方法!");
        }
        BeanUtils.copyProperties(v, t);
        return t;
    }

    /**
     * listMap转listObject
     *
     * @param list
     * @param beanClass
     * @param <T>
     * @return
     * @throws Exception
     */
    public static <T> List<T> listBymapToObject(List<Map<String, Object>> list, Class<T> beanClass) {
        List<T> vs = null;
        try {
            vs = new ArrayList<T>();
            if (list == null || list.size() == 0) {
                return vs;
            }
            for (Map<String, Object> map : list) {
                if (map == null) {
                    return null;
                }
                T obj = beanClass.newInstance();
                org.apache.commons.beanutils.BeanUtils.populate(obj, map);
                vs.add(obj);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return vs;
    }

    public static Map<String, Object> tToV(Object obj) throws Exception {
        if (obj == null) {
            return null;
        }
        Map<String, Object> map = new HashMap<String, Object>();
        BeanInfo beanInfo = Introspector.getBeanInfo(obj.getClass());
        PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
        for (PropertyDescriptor property : propertyDescriptors) {
            String key = property.getName();
            // 过滤class属性
            if (!key.equals("class")) {
                // 得到property对应的getter方法
                Method getter = property.getReadMethod();
                Object value = getter.invoke(obj);
                map.put(key, value);
            }
        }

        return map;

    }
}
