package eu.koboo.atcommand.annotations.command;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is just an array collection
 * of the @Label annotation.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface LabelArray {

    /**
     * @return The Label annotations as array
     */
    Label[] value();
}