package com.sababado.ezprovider;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by robert on 2/28/16.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Table {
    /**
     * Name of the table.
     * @return
     */
    String name();

    /**
     * Number code for this table. It should be unique in relation to other tables.
     * @return
     */
    int code();
}
