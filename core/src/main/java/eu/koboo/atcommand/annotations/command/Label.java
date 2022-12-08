package eu.koboo.atcommand.annotations.command;

import java.lang.annotation.*;

/**
 * This annotation is used to set a label to a command.
 * It can also be used multiple times and get parsed to
 * a @LabelArray.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Repeatable(value = LabelArray.class)
public @interface Label {

    /**
     * @return The command, which should get registered
     */
    String value();
}