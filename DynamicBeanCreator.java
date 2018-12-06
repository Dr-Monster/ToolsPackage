package com.zbjk.nrc.utils;

import net.sf.cglib.beans.BeanGenerator;
import net.sf.cglib.beans.BeanMap;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * @author: LuHaoran
 * @Date: 18-12-6 15:21
 * @Description: 根据map值动态生成对象
 */
public class DynamicBeanCreator {
    /**
     * 动态生成的类
     */

    private Object object = null;
    /**
     * 存放属性名称以及属性的类型
     */

    private BeanMap beanMap = null;


    public DynamicBeanCreator(Map propertyMap) {
        this.object = generateBean(propertyMap);
        this.beanMap = BeanMap.create(this.object);
    }

    /**
     * @param propertyMap
     * @return
     */
    private Object generateBean(Map propertyMap) {
        BeanGenerator generator = new BeanGenerator();
        Set keySet = propertyMap.keySet();
        for (Iterator i = keySet.iterator(); i.hasNext(); ) {
            String key = (String) i.next();
            generator.addProperty(key, (Class) propertyMap.get(key));
        }
        return generator.create();
    }

    /**
     * 给bean属性赋值
     *
     * @param property 属性名
     * @param value    值
     */
    public void setValue(Object property, Object value) {
        beanMap.put(property, value);
    }

    /**
     * 通过属性名得到属性值
     *
     * @param property 属性名
     * @return 值
     */
    public Object getValue(String property) {
        return beanMap.get(property);
    }

    /**
     * 得到该实体bean对象
     *
     * @return
     */
    public Object getObject() {
        return this.object;
    }

    /**
     * 拼接Json格式字符串
     * @return
     */
    public String toJsonString(){
        StringBuffer sb = new StringBuffer();
        sb.append('{');
        Iterator it = this.beanMap.keySet().iterator();

        while(it.hasNext()) {
            Object key = it.next();
            sb.append("\"");
            sb.append(key);
            sb.append("\"");
            sb.append(':');
            sb.append("\"");
            sb.append(this.beanMap.get(key));
            sb.append("\"");
            if (it.hasNext()) {
                sb.append(",");
            }
        }

        sb.append('}');
        return sb.toString();
    }
}
