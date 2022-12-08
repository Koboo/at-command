package eu.koboo.atcommand.annotations.command;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used to mark a command as
 * global. So that @NoPermission, @OnError, @OnHelp and @WrongSender
 * are picked from this command. If a command is implementing methods with
 * the annotations the own method is used, instead of the global method.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Global {
}