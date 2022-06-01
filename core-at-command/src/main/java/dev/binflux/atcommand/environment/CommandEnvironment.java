package dev.binflux.atcommand.environment;

import dev.binflux.atcommand.annotations.command.*;
import dev.binflux.atcommand.annotations.method.*;
import dev.binflux.atcommand.annotations.options.*;
import dev.binflux.atcommand.environment.meta.*;
import dev.binflux.atcommand.environment.utilities.OrderComparator;
import dev.binflux.atcommand.exceptions.*;
import dev.binflux.atcommand.parser.ParameterParser;
import dev.binflux.atcommand.parser.types.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

public abstract class CommandEnvironment implements Environment {

    private static CommandEnvironment environment;

    public static CommandEnvironment getEnvironment() {
        return environment;
    }

    private final CommandResolver commandResolver;
    private final Map<Object, CommandMeta> commandRegistry;
    private final Map<Class<?>, ParameterParser<?>> parserRegistry;

    private Object globalCommand;
    private CommandMeta globalCommandMeta;

    public CommandEnvironment() {
        environment = this;

        commandResolver = new CommandResolver(this);
        commandRegistry = new HashMap<>();
        parserRegistry = new HashMap<>();

        registerParser(new BooleanParser());
        registerParser(new DoubleParser());
        registerParser(new FloatParser());
        registerParser(new IntegerParser());
        registerParser(new LongParser());
        registerParser(new ShortParser());
        registerParser(new StringParser());
        registerParser(new UUIDParser());
    }

    @SuppressWarnings("all")
    protected Map<Class<?>, ParameterParser<?>> getParserRegistry() {
        return parserRegistry;
    }

    @Override
    public <T> void registerCommand(T command) {

        Class<?> commandClass = command.getClass();

        try {
            CommandMeta meta = commandResolver.resolveCommand(commandClass);
            if (meta.isGlobalCommand()) {
                globalCommand = command;
                globalCommandMeta = meta;
                return;
            }
            commandRegistry.put(command, meta);
            afterRegistration(command, meta);
        } catch (InvalidCommandException e) {
            e.printStackTrace();
        }
    }

    public <T> void afterRegistration(T command, CommandMeta commandMeta) {
        // Not important.
    }

    public MethodMeta loadMeta(Class<?> commandClass, Method method) throws InvalidCommandException {
        // Parse permission by @Access
        String permission = null;
        if (method.isAnnotationPresent(Access.class)) {
            permission = method.getAnnotation(Access.class).value();
        }

        // Parse async execution of method
        boolean async = method.isAnnotationPresent(Async.class);

        // Parse concatenating value of method;
        boolean concatenating = method.isAnnotationPresent(Concate.class);

        // Check length
        String className = commandClass.getName();
        String methodName = method.getName();

        // Get parameter types and index of method
        Map<Integer, Class<?>> parameterIndex = new HashMap<>();
        for (Parameter parameter : method.getParameters()) {
            parameterIndex.put(parameterIndex.size(), parameter.getType());
        }
        if (parameterIndex.isEmpty()) {
            throw new InvalidCommandException(className + " method " + methodName + " doesn't have any parameters!");
        }

        // Check default method should only have one argument
        if (method.isAnnotationPresent(Default.class) && parameterIndex.size() != 1) {
            throw new InvalidCommandException(className + " method " + methodName + " with @Default can only have one sender parameter.");
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
        if (method.isAnnotationPresent(Subcommand.class)) {
            subCommand = method.getAnnotation(Subcommand.class).value();
        }

        int order = 0;
        if (method.isAnnotationPresent(Order.class)) {
            order = method.getAnnotation(Order.class).value();
        }

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

            ParameterParser<?> parameterParser = parserRegistry.get(parameterType);
            if (parameterParser != null) {
                parameterSyntax = parameterParser.friendlyName();
            }
            syntaxBuilder.append("<").append(parameterSyntax).append(">").append(" ");
        }
        String syntax = syntaxBuilder.toString();
        if (syntax.endsWith(" ")) {
            syntax = syntax.substring(0, syntax.length() - 1);
        }
        CommandSyntax commandSyntax = new CommandSyntax(syntax, permission);

        return new MethodMeta(permission, async, subCommand, concatenating, order, method, parameterIndex, commandSyntax);
    }

    public Map.Entry<Object, CommandMeta> getCommandMeta(String label) {
        for (Object command : commandRegistry.keySet()) {
            CommandMeta meta = commandRegistry.get(command);
            for (String alias : meta.getAliasList()) {
                if (label.equalsIgnoreCase(alias)) {
                    return new AbstractMap.SimpleEntry<>(command, meta);
                }
            }
        }
        return null;
    }

    @Override
    public void registerParser(ParameterParser<?> parameterParser) {
        registerParser(parameterParser, false);
    }

    @Override
    public void registerParser(ParameterParser<?> parameterParser, boolean override) {
        if(override) {
            parserRegistry.remove(parameterParser.getType());
        }
        if (parserRegistry.containsKey(parameterParser.getType())) {
            throw new IllegalStateException(parameterParser.getClass().getName() + " is already registered in parserRegistry!");
        }
        parserRegistry.put(parameterParser.getType(), parameterParser);
    }

    @Override
    public <T> boolean handleCommand(T sender, String commandString) {
        if (!getPlayerClass().isAssignableFrom(sender.getClass()) && !getConsoleClass().isAssignableFrom(sender.getClass())) {
            return false;
        }
        String label = commandString.split(" ")[0];
        Map.Entry<Object, CommandMeta> entry = getCommandMeta(label);
        if (entry == null) {
            return false;
        }
        Object command = entry.getKey();
        CommandMeta meta = entry.getValue();
        try {
            if (meta.hasRootPermission() && hasNotPermission(sender, meta.getRootPermission())) {
                return callNoPermission(command, meta, sender, meta.getRootPermission(), commandString);
            }

            String[] arguments = new String[]{};
            if (commandString.length() > (label.length() + 1)) {
                arguments = commandString.substring(label.length() + 1).split(" ");
            }

//            Debug.println("Length: " + arguments.length);
//            Debug.println("Command: " + CommandHelp.buildCommand("", arguments));
//            Debug.println("Subcommands: " + meta.getSubCommandMetaList().size());

            if (arguments.length == 0) {
                return callDefault(label, command, meta, sender);
            }

            if (arguments.length == 1 && arguments[0].equalsIgnoreCase("help")) {
                return callHelpAllSubcommands(sender, label, command, meta);
            }

            MetaLoop:
            for (MethodMeta methodMeta : meta.getSubCommandMetaList()) {

                String[] commandArguments = arguments;

//                System.out.println("[]=====[ " + methodMeta.getMethod().getName() + " ]=====[]");

                String[] annotationArguments = methodMeta.getSubCommand().split(" ");
                if (annotationArguments.length == 1 && annotationArguments[0].equalsIgnoreCase("")) {
                    annotationArguments = new String[]{};
                }
//                System.out.println("ParameterLength: " + methodMeta.getParameterIndex().size());
//                System.out.println("AnnotationLength: " + annotationArguments.length);

                if (methodMeta.isConcatenating()) {
                    StringBuilder messageBuilder = new StringBuilder();
                    int parameterAddition = methodMeta.getParameterIndex().size() - 2;
                    int annotationLength = annotationArguments.length + parameterAddition;
                    int argumentLength = commandArguments.length;
                    for (int i = annotationLength; i < argumentLength; i++) {
                        messageBuilder.append(" ").append(commandArguments[i]);
                    }
                    commandArguments = Arrays.copyOf(commandArguments, annotationLength + 1);
                    String message = messageBuilder.toString().replaceFirst(" ", "");
                    commandArguments[annotationLength] = message;
//                    System.out.println("ConcatenatingLength: " + commandArguments.length);
//                    System.out.println("Concatenating: " + commandArguments[commandArguments.length - 1]);
                }

                // Decrease parameterLength by one, because of Sender objects
                int expectedLength = annotationArguments.length + (methodMeta.getParameterIndex().size() - 1);
//                System.out.println("ExpectedLength: " + expectedLength);
//                System.out.println("CheckArgLength: " + commandArguments.length);

                // Check the expectedLength with the current arguments
                if (expectedLength != commandArguments.length) {
                    // Length of arguments not matching..
                    continue;
                }

                List<Object> parameterList = new ArrayList<>();
                // Setting the Sender object into parameterList on initializing,
                // because he is always needed.
                parameterList.add(sender);

                for (int i = 0; i < expectedLength; i++) {
                    // This can't be null or out of bounds.
                    String argumentParam = commandArguments[i];

                    // Check if we got all subcommand arguments.
                    if (annotationArguments.length != 0 && (annotationArguments.length - 1) >= i) {
                        String annotationParam = annotationArguments[i];
//                        System.out.println(i + " @Subcommand " + annotationParam + "|" + argumentParam);
                        if (!annotationParam.equalsIgnoreCase(argumentParam)) {
                            continue MetaLoop;
                        }
                        continue;
                    }
                    try {
                        // Decrease by annotation length, but increment to ignore sender object
                        int parameterIndexInMethod = (i - annotationArguments.length) + 1;
                        // Get parameterType Class
                        Class<?> parameterType = methodMeta.getParameterIndex().get(parameterIndexInMethod);

//                        System.out.println(i + "|" + parameterIndexInMethod + " Parameter " + argumentParam + "|" + parameterType.getName());

                        // Get parser and parse the argument, add that into list.
                        ParameterParser<?> parameterParser = parserRegistry.get(parameterType);
                        if (parameterParser == null) {
                            throw new ParameterException("No parser found for type " + parameterType.getName() + ".");
                        }
                        parameterList.add(parameterParser.parse(argumentParam));

                    } catch (ParameterException exc) {
                        if (meta.isShowHelpOnError()) {
                            return callHelpSubCommand(sender, label, command, meta, methodMeta);
                        } else if (meta.getErrorMeta() != null) {
                            if (meta.isShowHelpWithError()) {
                                callHelpSubCommand(sender, label, command, meta, methodMeta);
                            }
                            if(globalCommandMeta != null && globalCommandMeta.getHelpMeta() != null) {
                                if (handleSenderType(globalCommand, globalCommandMeta, sender, globalCommandMeta.getErrorMeta())) {
                                    return true;
                                }
                                globalCommandMeta.getErrorMeta().getMethod().invoke(globalCommand, sender, exc.getMessage());
                            }
                            if (handleSenderType(command, meta, sender, meta.getErrorMeta())) {
                                return true;
                            }
                            meta.getErrorMeta().getMethod().invoke(command, sender, exc.getMessage());
                            return true;
                        }
                        break MetaLoop;
                    }
                }
                int parameterListLength = parameterList.size();
                boolean wrongSyntax = false;
                for (int i = 0; i < parameterListLength; i++) {
                    Class<?> parameterListClass = parameterList.get(i).getClass();
                    Class<?> parameterIndexClass = methodMeta.getParameterIndex().get(i);
//                    System.out.println("Check: " + parameterListClass.getName() + "|" + parameterIndexClass.getName());
                    if (!parameterIndexClass.isAssignableFrom(parameterListClass)) {
                        wrongSyntax = true;
                    }
                }
//                System.out.println("WrongSyntax: " + wrongSyntax);
                if (wrongSyntax) {
                    continue;
                }

//                System.out.println("InvokingLength: " + parameterList.size());
                if (handleSenderType(command, meta, sender, methodMeta)) {
                    return true;
                }
//                System.out.println("Invoked method: " + methodMeta.getMethod().getName());
                methodMeta.getMethod().invoke(command, parameterList.toArray(new Object[]{}));
                return true;
            }
            return callHelpAllSubcommands(sender, label, command, meta);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return false;
    }

    private <T> boolean handleSenderType(Object command, CommandMeta meta, T sender, MethodMeta methodMeta)
            throws InvocationTargetException, IllegalAccessException {
        Class<?> expectedSender = methodMeta.getMethod().getParameterTypes()[0];
        Class<?> objectSender = sender.getClass();
        if (!expectedSender.isAssignableFrom(objectSender)) {
            if (meta.getWrongSenderMeta() != null) {
                meta.getWrongSenderMeta().getMethod().invoke(command, sender);
                return true;
            } else {
                if(globalCommandMeta != null && globalCommandMeta.getHelpMeta() != null) {
                    globalCommandMeta.getWrongSenderMeta().getMethod().invoke(globalCommand, sender);
                    return true;
                }
            }
        }
        return false;
    }

    private <T> boolean callHelp(Object command, CommandMeta meta, T sender, CommandHelp commandHelp)
            throws InvocationTargetException, IllegalAccessException {
        if (meta.getHelpMeta() == null) {
            if(globalCommandMeta != null && globalCommandMeta.getHelpMeta() != null) {
                if (handleSenderType(globalCommand, globalCommandMeta, sender, globalCommandMeta.getHelpMeta())) {
                    return true;
                }
                globalCommandMeta.getHelpMeta().getMethod().invoke(globalCommand, sender, commandHelp);
                return true;
            }
            return false;
        }
        if (handleSenderType(command, meta, sender, meta.getHelpMeta())) {
            return true;
        }
        meta.getHelpMeta().getMethod().invoke(command, sender, commandHelp);
        return true;
    }

    private <T> boolean callDefault(String label, Object command, CommandMeta meta, T sender)
            throws InvocationTargetException, IllegalAccessException {
        if (meta.getDefaultMeta() == null) {
            if (meta.isShowHelpOnDefault()) {
                return callHelpAllSubcommands(sender, label, command, meta);
            }
            return false;
        }
        if (handleSenderType(command, meta, sender, meta.getDefaultMeta())) {
            return true;
        }
        meta.getDefaultMeta().getMethod().invoke(command, sender);
        return true;
    }

    private <T> boolean callHelpAllSubcommands(T sender, String label, Object command, CommandMeta meta)
            throws InvocationTargetException, IllegalAccessException {
        List<CommandSyntax> syntaxList = new ArrayList<>();
        for (MethodMeta methodMeta : meta.getSubCommandMetaList()) {
            syntaxList.add(methodMeta.getSyntax());
        }
        CommandHelp help = new CommandHelp(label, syntaxList);
        return callHelp(command, meta, sender, help);
    }

    private <T> boolean callHelpSubCommand(T sender, String label, Object command, CommandMeta meta, MethodMeta subMeta)
            throws InvocationTargetException, IllegalAccessException {
        List<CommandSyntax> syntaxList = new ArrayList<>();
        syntaxList.add(subMeta.getSyntax());
        CommandHelp help = new CommandHelp(label, syntaxList);
        return callHelp(command, meta, sender, help);
    }

    private <T> boolean callNoPermission(Object command, CommandMeta meta, T sender, String permission, String commandString)
            throws InvocationTargetException, IllegalAccessException {
        if (meta.getNoPermissionMeta() == null) {
            if(globalCommandMeta != null && globalCommandMeta.getHelpMeta() != null) {
                if (handleSenderType(globalCommand, globalCommandMeta, sender, globalCommandMeta.getNoPermissionMeta())) {
                    return true;
                }
                globalCommandMeta.getNoPermissionMeta().getMethod().invoke(globalCommand, sender, permission, commandString);
                return true;
            }
            return false;
        }
        if (handleSenderType(command, meta, sender, meta.getNoPermissionMeta())) {
            return true;
        }
        meta.getNoPermissionMeta().getMethod().invoke(command, sender, permission, commandString);
        return true;
    }

    @Override
    public <T> List<String> handleCompletions(T sender, String commandString, List<String> suggestions) {
        if (!getPlayerClass().isAssignableFrom(sender.getClass()) && !getConsoleClass().isAssignableFrom(sender.getClass())) {
            return new ArrayList<>();
        }
        if(suggestions == null || suggestions.isEmpty()) {
            suggestions = new ArrayList<>();
        }
        List<String> completions = new ArrayList<>(suggestions);

        for (Object command : commandRegistry.keySet()) {
            CommandMeta meta = commandRegistry.get(command);

            // Ignored, sender doesn't have permission to use this command
            if (meta.hasRootPermission() && hasNotPermission(sender, meta.getRootPermission())) {
                continue;
            }

            // Create the command label
            String label = commandString.split(" ")[0].toLowerCase(Locale.ROOT);
            String[] arguments = new String[]{};
            // If possible, we create the arguments as String[]
            if (commandString.length() > (label.length() + 1)) {
                arguments = commandString.substring(label.length() + 1).split(" ");
            }

            // If the argument length is zero, we can only complete command aliases
            if (arguments.length == 0) {
                for (String alias : meta.getAliasList()) {
                    alias = "/" + alias;
                    if (alias.startsWith(label) && !completions.contains(alias) && !alias.equalsIgnoreCase(label)) {
                        completions.add(alias);
                    }
                }
                if (!commandString.endsWith(" ")) {
                    continue;
                }
            }
            // Only show subcommands of the typed command
            if(!meta.getAliasList().contains(label.toLowerCase(Locale.ROOT))) {
                continue;
            }

            // Continue if no subcommands found
            if (meta.getSubCommandMetaList().isEmpty()) {
                continue;
            }

            for (MethodMeta methodMeta : meta.getSubCommandMetaList()) {

                // Some debug logs.
//                Debug.println("Method: " + methodMeta.getMethod().getName());

                // Get metaArguments and get expectedLength of arguments for the method
                String[] metaArgs = methodMeta.getSubCommand().split(" ");
                int expectedLength = metaArgs.length + methodMeta.getParameterIndex().size() - 1;

                // If we got no arguments we return a emptyList.
                if (expectedLength == 0) {
                    return Collections.emptyList();
                }

                // arguments already exceeds expected arguments
                if (expectedLength < arguments.length) {
                    continue;
                }

                // Continue if we only got a concating message
                if (methodMeta.isConcatenating()) {
                    continue;
                }

                // Create the needed index of the argument
                int index = commandString.endsWith(" ") ? arguments.length : arguments.length - 1;

                // Get the index on which the parameters of the method start
                int paramStartIndex = metaArgs.length + 1;

                // Some debug logs.
//                Debug.println("ExpectedLength: " + expectedLength);
//                Debug.println("MetaLength: " + metaArgs.length);
//                Debug.println("StartIndex: " + paramStartIndex);
//                Debug.println("Index: " + index);

                // Get the last argument if command doesn't end with a space
                String argument = commandString.endsWith(" ") ? null : arguments[index];
                if (argument != null) {
                    argument = argument.toLowerCase(Locale.ROOT);
                }

                // Check if argumentIndex exceeds our metaArguments
                if ((metaArgs.length - 1) >= index) {
                    String metaArgument = metaArgs[index].toLowerCase(Locale.ROOT);
                    if (argument == null || metaArgument.startsWith(argument)) {
                        if (!completions.contains(metaArgument) && !metaArgument.equalsIgnoreCase(argument)) {
                            completions.add(metaArgument);
                        }
                    }
                    continue;
                }

                // Check if metaArgs the only arguments of the method
                if (metaArgs.length == expectedLength) {
                    continue;
                }

                // Check if argumentIndex exceeds our parameter start index
                if (paramStartIndex >= index) {
                    // Get the correct parameter index by subtracting the metaArgs length
                    // We don't need a '-1' here, because we ignore the sender parameter
                    int paramIndex = paramStartIndex - metaArgs.length;

                    // Get the completions of the defined parameter-parser
                    Class<?> paramType = methodMeta.getMethod().getParameterTypes()[paramIndex];
                    ParameterParser<?> parameterParser = parserRegistry.get(paramType);
                    if (parameterParser == null) {
                        continue;
                    }
                    // Handle parameter completions
                    List<String> paramCompletions = parameterParser.complete(argument);
                    for (String paramArg : paramCompletions) {
                        paramArg = paramArg.toLowerCase(Locale.ROOT);
                        if (argument == null || paramArg.startsWith(argument)) {
                            if (!completions.contains(paramArg)) {
                                completions.add(paramArg);
                            }
                        }
                    }
                }
            }
        }
        return completions;
    }
}