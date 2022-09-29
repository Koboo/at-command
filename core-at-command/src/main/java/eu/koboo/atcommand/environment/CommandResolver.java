package eu.koboo.atcommand.environment;

import eu.koboo.atcommand.annotations.command.Global;
import eu.koboo.atcommand.annotations.command.Label;
import eu.koboo.atcommand.annotations.command.ShowHelpWithError;
import eu.koboo.atcommand.annotations.method.Subcommand;
import dev.binflux.atcommand.annotations.method.types.*;
import eu.koboo.atcommand.annotations.method.types.*;
import eu.koboo.atcommand.annotations.options.Access;
import eu.koboo.atcommand.annotations.options.MergeText;
import eu.koboo.atcommand.annotations.options.Order;
import eu.koboo.atcommand.environment.meta.CommandMeta;
import eu.koboo.atcommand.environment.meta.CommandSyntax;
import eu.koboo.atcommand.environment.meta.MethodMeta;
import eu.koboo.atcommand.environment.utilities.OrderComparator;
import eu.koboo.atcommand.exceptions.InvalidCommandException;
import eu.koboo.atcommand.parser.ParameterParser;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CommandResolver {

    CommandEnvironment environment;

    public <T> CommandMeta resolveCommand(Class<T> commandClass) throws InvalidCommandException {
        String className = commandClass.getName();

        boolean isGlobalCommand = commandClass.getAnnotation(Global.class) != null;

        List<String> commandLabelList = new ArrayList<>();
        if (!isGlobalCommand) {
            Label[] labels = commandClass.getAnnotationsByType(Label.class);
            if(labels.length == 0) {
                throw new InvalidCommandException(className + " must have at least one @Label annotation!");
            }

            for (Label label : labels) {
                String labelString = label.value().toLowerCase(Locale.ROOT);
                if (!commandLabelList.contains(labelString)) {
                    commandLabelList.add(labelString);
                }
            }
        }

        // Check the root-permission on class
        String rootPermission = null;
        if (commandClass.isAnnotationPresent(Access.class)) {
            Access access = commandClass.getAnnotation(Access.class);
            rootPermission = access.value();
        }

        MethodMeta defaultMeta = null;
        MethodMeta helpMeta = null;
        MethodMeta noPermissionMeta = null;
        MethodMeta errorMeta = null;
        MethodMeta notAPlayerMeta = null;
        List<MethodMeta> subCommandMetaList = new LinkedList<>();

        // Iterate through all methods and load them
        for (Method method : commandClass.getMethods()) {

            String methodName = method.getName();
            String clazzName = commandClass.getName();

            // Load and handle Default method
            if (method.isAnnotationPresent(Default.class)) {
                // Load default method and assign to field
                defaultMeta = resolveMeta(commandClass, method);
                continue;
            }

            // Load and handle Help method
            if (method.isAnnotationPresent(OnHelp.class)) {
                helpMeta = resolveMeta(commandClass, method);
                continue;
            }

            // Load and handle NoPermission method
            if (method.isAnnotationPresent(NoPermission.class)) {
                noPermissionMeta = resolveMeta(commandClass, method);
                continue;
            }

            // Load and handle Error method
            if (method.isAnnotationPresent(OnError.class)) {
                errorMeta = resolveMeta(commandClass, method);
                continue;
            }

            // Load and handle WrongSender method
            if (method.isAnnotationPresent(WrongSender.class)) {
                notAPlayerMeta = resolveMeta(commandClass, method);
                continue;
            }

            // Load and handle Sub methods
            if (method.isAnnotationPresent(Subcommand.class)) {

                // Load and put sub method into registry
                MethodMeta methodMeta = resolveMeta(commandClass, method);

                for (MethodMeta otherMeta : subCommandMetaList) {

                    // Check for same names
                    if (otherMeta.getMethod().getName().equalsIgnoreCase(methodMeta.getMethod().getName())) {
                        throw new InvalidCommandException(clazzName + " method " + methodName + " already exists! Method names have to be unique!");
                    }

                    // Check for same signature
                    if (otherMeta.getParameterIndex().size() == methodMeta.getParameterIndex().size()
                            && otherMeta.getSubCommand().equalsIgnoreCase(methodMeta.getSubCommand())) {
                        throw new InvalidCommandException(clazzName + " method " + methodName + " and method " +
                                otherMeta.getMethod().getName() + " have the same @Subcommand String and same parameter index! It's not allowed, to ensure method uniqueness!");
                    }
                }

                subCommandMetaList.add(methodMeta);
                subCommandMetaList.sort(new OrderComparator());
                //continue;
            }
        }

        boolean showHelpWithError = commandClass.isAnnotationPresent(ShowHelpWithError.class);

        return new CommandMeta(commandLabelList, rootPermission, defaultMeta, helpMeta, noPermissionMeta, errorMeta,
                notAPlayerMeta, subCommandMetaList, isGlobalCommand, showHelpWithError);
    }

    public MethodMeta resolveMeta(Class<?> commandClass, Method method) throws InvalidCommandException {
        // Parse permission by @Access
        String permission = null;
        if (method.isAnnotationPresent(Access.class)) {
            permission = method.getAnnotation(Access.class).value();
        }

        // Parse concatenating value of method;
        boolean concatenating = method.isAnnotationPresent(MergeText.class);

        // Check length
        String className = commandClass.getName();
        String methodName = method.getName();

        // Get parameter types and index of method
        Map<Integer, Class<?>> parameterIndex = new HashMap<>();
        for (Parameter parameter : method.getParameters()) {
            if(parameter.getType().isPrimitive()) {
                throw new InvalidCommandException(className + " method " + methodName +
                        " has a primitive type of " + parameter.getType().getName() + " but it's not allowed!");
            }
            parameterIndex.put(parameterIndex.size(), parameter.getType());
        }
        if (parameterIndex.isEmpty()) {
            throw new InvalidCommandException(className + " method " + methodName + " doesn't have any parameters!");
        }

        // Check default method should only have one argument
        if (method.isAnnotationPresent(Default.class) && parameterIndex.size() != 1) {
            throw new InvalidCommandException(className + " method " + methodName + " with @Default can only have one parameter of the desired sender.");
        }

        // Specific method shouldn't have Sub annotation
        if (method.isAnnotationPresent(Default.class)
                || method.isAnnotationPresent(OnHelp.class)
                || method.isAnnotationPresent(NoPermission.class)
                || method.isAnnotationPresent(OnError.class)
                || method.isAnnotationPresent(WrongSender.class)) {
            if (method.isAnnotationPresent(Subcommand.class)) {
                throw new InvalidCommandException(className + " method " + methodName + " has @Sub on specific annotation! This is not allowed and stupid!");
            }
        }

        String subCommand = null;
        String usage = null;
        if (method.isAnnotationPresent(Subcommand.class)) {
            Subcommand subcommandAnnotation = method.getAnnotation(Subcommand.class);
            subCommand = subcommandAnnotation.value();
            String annotationUsage = subcommandAnnotation.usage();
            if(!annotationUsage.equalsIgnoreCase("")) {
                usage = annotationUsage;
            }
        }

        int order = 0;
        if (method.isAnnotationPresent(Order.class)) {
            order = method.getAnnotation(Order.class).value();
        }

        CommandSyntax commandSyntax;
        if(usage == null) {
            StringBuilder syntaxBuilder = new StringBuilder();
            if (subCommand != null && !subCommand.equalsIgnoreCase("")) {
                syntaxBuilder.append(subCommand).append(" ");
            }
            boolean senderSkipped = false;
            for (Class<?> parameterType : method.getParameterTypes()) {
                if (!senderSkipped) {
                    senderSkipped = true;
                    continue;
                }
                String parameterSyntax = parameterType.getSimpleName();

                ParameterParser<?> parameterParser = environment.getParserRegistry().get(parameterType);
                if (parameterParser != null) {
                    parameterSyntax = parameterParser.friendlyName();
                }
                syntaxBuilder.append("<").append(parameterSyntax).append(">").append(" ");
            }
            String syntax = syntaxBuilder.toString();
            if (syntax.endsWith(" ")) {
                syntax = syntax.substring(0, syntax.length() - 1);
            }
            commandSyntax = new CommandSyntax(syntax, permission);
        } else {
            commandSyntax = new CommandSyntax(usage, permission);
        }

        return new MethodMeta(permission, subCommand, concatenating, order, method, parameterIndex, commandSyntax);
    }
}