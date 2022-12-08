package eu.koboo.atcommand.annotations.method.types;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * this annotation is used to handle the wrong
 * sender, of a specific method.
 * Method-Signature:
 * <pre>
 * @WrongSender
 * public void wrongSender(CommandSender sender) {
 *     // Some magic
 * }
 * </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface WrongSender {
}