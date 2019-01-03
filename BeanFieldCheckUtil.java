package com.zbjk.nrc.utils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Dr.Monster
 * 判断类中属性是否为空
 */
public class BeanFieldCheckUtil {
    /**
     * check all
     * @param t
     * @param <T>
     * @return
     */
    public static <T> Boolean beanFieldIsEmpty(T t){
        if(t != null){
            Field[] fields = t.getClass().getDeclaredFields();
            for(Field item : fields){
                try {
                    //打开私有访问
                    item.setAccessible(true);
                    //获取属性值
                    Object value = item.get(t);
                    if(String.valueOf(value) == null || String.valueOf(value).equals("")){
                        return true;
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }


    /**
     * check some
     * @param t
     * @param argNames
     * @param <T>
     * @return
     */
    public static <T> Boolean beanFieldIsEmptyWithoutSome(T t , String... argNames){
        List<String> nameList = new ArrayList<>();
        for(String name : argNames){
            nameList.add(name);
        }
        if(t != null){
            Field[] fields = t.getClass().getDeclaredFields();
            for(Field item : fields){
                try {
                    //打开私有访问
                    item.setAccessible(true);
                    //获取属性名
                    String fieldName = item.getName();
                    //过滤特定属性
                    if(nameList.contains(fieldName)){
                        continue;
                    }
                    //获取属性值
                    Object value = item.get(t);
                    if(String.valueOf(value) == null || String.valueOf(value).equals("")){
                        return true;
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

}
