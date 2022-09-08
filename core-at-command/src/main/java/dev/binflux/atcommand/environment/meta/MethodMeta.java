package dev.binflux.atcommand.environment.meta;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.lang.reflect.Method;
import java.util.Map;

@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Getter
public class MethodMeta {

    String permission;
    String subCommand;
    boolean concatenating;
    int order;
    Method method;
    Map<Integer, Class<?>> parameterIndex;
    CommandSyntax syntax;

}