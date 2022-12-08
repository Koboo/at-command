package eu.koboo.atcommand.annotations.method.types;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used to mark a method as the default method.
 * So a method without any arguments.
 * Method-Signature:
 * <pre>
 *  @Default
 *  public void onDefault(CommandSender sender) {
 *      // Some magic
 *  }
 *  </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Default {
}