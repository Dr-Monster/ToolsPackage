package com.example.demo;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author Dr.Monster
 */
public class SortUtil {
    public static <T> List<T> sortList(List<T> list , String args){
        T t = list.get(0);
        Collections.sort(list, new Comparator<T>() {
            @Override
            public int compare(T o1, T o2) {
                Field field = null ;
                int check = 0 ;
                try {
                    field = t.getClass().getDeclaredField(args);
                    if(field == null){
                        check = 0;
                    }else{
                        field.setAccessible(true);
                        if (Integer.valueOf(String.valueOf(field.get(o1))) >= Integer.valueOf(String.valueOf(field.get(o2)))) {
                            check = 1;
                        }else{
                            check = -1;
                        }
                    }
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                return check;
            }
        });
        return list;
    }
}
