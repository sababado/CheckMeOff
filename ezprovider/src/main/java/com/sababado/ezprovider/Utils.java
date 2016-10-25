package com.sababado.ezprovider;

import java.lang.reflect.Field;

/**
 * Created by robert on 2/28/16.
 */
class Utils {
    static String getDbTypeFromField(Field field) {
        Class<?> cls = field.getType();
        if (cls.isPrimitive()) {
            if (Short.TYPE.equals(cls) || Integer.TYPE.equals(cls) ||
                    Long.TYPE.equals(cls) || Character.TYPE.equals(cls) ||
                    Byte.TYPE.equals(cls) || Boolean.TYPE.equals(cls)) {
                return "INTEGER";
            } else if (Float.TYPE.equals(cls) || Double.TYPE.equals(cls)) {
                return "REAL";
            } else {
                throw new RuntimeException("Primitive type for '" + field.getName() + "' is not supported");
            }
            // End of "isPrimitive"
        } else if (String.class.equals(cls)) {
            return "TEXT";
        } else {
            throw new RuntimeException("Object types are not supported: '" + field.getName() + "'");
        }
    }
}
