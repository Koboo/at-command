package eu.koboo.atcommand.annotations.method.types;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used to handle any error,
 * which occurs inside a command method.
 * Method-Signature:
 * <pre>
 * @OnError
*  public void onError(CommandSender sender, String error) {
 *     // Some magic
 * }
 * </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface OnError {
}