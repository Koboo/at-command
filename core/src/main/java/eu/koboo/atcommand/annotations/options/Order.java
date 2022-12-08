package eu.koboo.atcommand.annotations.options;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used to change parsing of the methods
 * in a command. See OrderComparator for priority ordering.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Order {

    /**
     * @return The priority of the ordering
     */
    int value();
}