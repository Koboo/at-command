package dev.binflux.atcommand.environment.meta;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.lang.reflect.Method;
import java.util.Map;

@Getter
@AllArgsConstructor
public class MethodMeta {

    private final String permission;
    private final boolean async;
    private final String subCommand;
    private final boolean concatenating;
    private final int order;
    private final Method method;
    private final Map<Integer, Class<?>> parameterIndex;
    private final CommandSyntax syntax;

    public boolean hasPermission() {
        return permission != null;
    }
}