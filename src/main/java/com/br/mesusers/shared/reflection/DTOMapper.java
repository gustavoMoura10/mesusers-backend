package com.br.mesusers.shared.reflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.RecordComponent;
import java.util.Arrays;

public class DTOMapper {

    public static <T> T transform(Object source, Class<T> targetClass) {
        if (source == null || targetClass == null) {
            return null;
        }

        try {
            if (targetClass.isRecord()) {
                return transformToRecord(source, targetClass);
            } else {
                return transformToClass(source, targetClass);
            }
        } catch (Exception e) {
            throw new RuntimeException("Falha no mapeamento DTO para " + targetClass.getSimpleName(), e);
        }
    }

    private static <T> T transformToRecord(Object source, Class<T> recordClass) throws Exception {
        RecordComponent[] recordComponents = recordClass.getRecordComponents();
        Object[] args = new Object[recordComponents.length];
        
        for (int i = 0; i < recordComponents.length; i++) {
            String fieldName = recordComponents[i].getName();
            Field sourceField = getField(source.getClass(), fieldName);
            sourceField.setAccessible(true);
            args[i] = sourceField.get(source);
        }
        Class<?>[] paramTypes = Arrays.stream(recordComponents)
            .map(RecordComponent::getType)
            .toArray(Class<?>[]::new);
            
        Constructor<T> constructor = recordClass.getDeclaredConstructor(paramTypes);
        return constructor.newInstance(args);
    }

    private static <T> T transformToClass(Object source, Class<T> targetClass) throws Exception {
        T target = targetClass.getDeclaredConstructor().newInstance();
        
        for (Field sourceField : source.getClass().getDeclaredFields()) {
            sourceField.setAccessible(true);
            Object value = sourceField.get(source);
            
            try {
                Field targetField = getField(targetClass, sourceField.getName());
                targetField.setAccessible(true);
                targetField.set(target, value);
            } catch (NoSuchFieldException e) {
               
            }
        }
        
        return target;
    }

    private static Field getField(Class<?> clazz, String fieldName) throws NoSuchFieldException {
        try {
            return clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            Class<?> superClass = clazz.getSuperclass();
            if (superClass != null) {
                return getField(superClass, fieldName);
            }
            throw e;
        }
    }
}