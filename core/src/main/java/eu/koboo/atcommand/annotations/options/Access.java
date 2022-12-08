package eu.koboo.atcommand.annotations.options;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used to set permissions for
 * methods or a whole command.
 * It can also be used multiple types.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = {ElementType.METHOD, ElementType.TYPE})
public @interface Access {

    /**
     * @return The permission as string
     */
    String value();
}