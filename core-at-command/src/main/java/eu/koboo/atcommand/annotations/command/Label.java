package eu.koboo.atcommand.annotations.command;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Repeatable(value = Labels.class)
public @interface Label {

    String value();
}