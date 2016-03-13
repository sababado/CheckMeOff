package com.sababado.ezprovider;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Use this annotation to specify a column name and index;
 * Created by robert on 2/28/16.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Column {
    /**
     * The name of the column to use in the database.
     *
     * @return
     */
    String name() default "";

    /**
     * The column index.
     *
     * @return
     */
    int value();
}
