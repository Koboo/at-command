package eu.koboo.atcommand.annotations.method.types;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used to handle a CommandSender without
 * the needed permissions to a command or a method.
 * Method-Signature:
 * <pre>
 * @NoPermission
 * public void noPermission(CommandSender sender, String permission, String command) {
 *     // Some magic
 * }
 * </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface NoPermission {
}