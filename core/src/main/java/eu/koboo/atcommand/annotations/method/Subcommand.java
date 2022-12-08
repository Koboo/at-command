package eu.koboo.atcommand.annotations.method;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used to set a subcommand
 * argument, so the environment can match against it.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Subcommand {

    /**
     * @return The static sub argument for that method
     */
    String value() default "";

    /**
     * @return The custom usage message for that method
     */
    String usage() default "";
}
