package eu.koboo.atcommand.annotations.command;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Repeatable(value = LabelArray.class)
public @interface Label {

    String value();
}