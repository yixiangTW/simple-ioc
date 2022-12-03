package com.github.hcsp;

import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MyIoCContainer {
    public static void main(String[] args) throws IOException {
        Properties properties = new Properties();
        properties.load(MyIoCContainer.class.getResourceAsStream("/ioc.properties"));
        System.out.println(properties);
        Map<String, Object> beans = new HashMap<>();
        properties.forEach((beanName, beanClass) -> {
            try {
                Class klass = Class.forName((String) beanClass);
                Object beanInstance = klass.getConstructor().newInstance();
                beans.put((String) beanName, beanInstance);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        beans.forEach((beanName, beanInstance) -> dependencyInject(beanName, beanInstance, beans));
        OrderService orderService = (OrderService) beans.get("orderService");
        orderService.doSomeThing();
    }

    private static void dependencyInject(String beanName, Object beanInstance, Map<String, Object> beans) {
        List<Field> fieldsToBeAutowired = Stream.of(beanInstance.getClass().getDeclaredFields()).filter(field -> field.getAnnotation(Autowired.class) != null).collect(Collectors.toList());
        fieldsToBeAutowired.forEach(field -> {
            try {
                String fieldName = field.getName();
                Object dependencyBeanInstance = beans.get(fieldName);
                field.setAccessible(true);
                field.set(beanInstance, dependencyBeanInstance);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
