package eu.koboo.atcommand.environment;

import eu.koboo.atcommand.annotations.command.Global;
import eu.koboo.atcommand.annotations.command.Label;
import eu.koboo.atcommand.annotations.dependency.Dependency;
import eu.koboo.atcommand.environment.meta.CommandMeta;
import eu.koboo.atcommand.environment.meta.CommandSyntax;
import eu.koboo.atcommand.environment.meta.MethodMeta;
import eu.koboo.atcommand.exceptions.ConditionException;
import eu.koboo.atcommand.exceptions.InvalidCommandException;
import eu.koboo.atcommand.exceptions.ParameterException;
import eu.koboo.atcommand.parser.ParameterParser;
import eu.koboo.atcommand.parser.types.*;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.*;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public abstract class CommandEnvironment implements Environment {

    CommandResolver commandResolver;
    Map<Object, CommandMeta> commandRegistry;
    Map<Class<?>, ParameterParser<?>> parserRegistry;
    Map<Class<?>, Object> dependencyRegistry;

    @NonFinal
    Object globalCommand;
    @NonFinal
    CommandMeta globalCommandMeta;

    public CommandEnvironment() {

        commandResolver = new CommandResolver(this);
        commandRegistry = new HashMap<>();
        parserRegistry = new HashMap<>();
        dependencyRegistry = new HashMap<>();

        registerParser(new BooleanParser());
        registerParser(new DateParser());
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

    public void destroy() {
        commandRegistry.clear();
        parserRegistry.clear();
        dependencyRegistry.clear();
    }

    @Override
    public <D> void addDependency(D object) {
        // Register the given instance of the object as dependency for commands
        Class<?> objectClass = object.getClass();
        if (dependencyRegistry.containsKey(objectClass)) {
            throw new IllegalStateException("Dependency of " + objectClass.getName() + " is already registered!");
        }
        dependencyRegistry.put(objectClass, object);
    }

    @Override
    public <D> D getDependency(Class<D> dependencyClass) {
        // Return the instance of the given dependency class
        if (!dependencyRegistry.containsKey(dependencyClass)) {
            return null;
        }
        Object dependency = dependencyRegistry.get(dependencyClass);
        return dependencyClass.cast(dependency);
    }

    @Override
    public void registerCommandsIn(String... packageNames) {
        FilterBuilder filterBuilder = new FilterBuilder();
        for (String packageName : packageNames) {
            filterBuilder.includePackage(packageName);
        }
        Reflections reflections = new Reflections(new ConfigurationBuilder().filterInputsBy(filterBuilder));

        Set<Class<?>> commandClasses = reflections.get(Scanners.TypesAnnotated.with(Label.class).asClass());
        if (commandClasses == null) {
            commandClasses = new HashSet<>();
        }
        Set<Class<?>> globalCommandClasses = reflections.get(Scanners.TypesAnnotated.with(Global.class).asClass());
        commandClasses.addAll(globalCommandClasses);
        globalCommandClasses.clear();

        if (commandClasses.isEmpty()) {
            return;
        }

        for (Class<?> commandClass : commandClasses) {

            // Search for the required no-args constructor
            Constructor<?>[] constructorArray = commandClass.getDeclaredConstructors();
            if (constructorArray.length == 0) {
                throw new NullPointerException("Couldn't find any constructor in " + commandClass.getName());
            }
            Constructor<?> constructor = constructorArray[0];
            if (constructor.getParameterCount() > 0) {
                throw new IllegalArgumentException("Constructor of " + commandClass.getName() + " isn't allowed to have parameters!");
            }
            if (!Modifier.isPublic(constructor.getModifiers())) {
                throw new IllegalStateException("Constructor of " + commandClass.getName() + " isn't public and can't get accessed.");
            }

            // Create the instance of the command
            try {
                Object command = constructor.newInstance();

                // Check for dependencies in the fields.
                for (Field declaredField : commandClass.getDeclaredFields()) {
                    if (!declaredField.isAnnotationPresent(Dependency.class)) {
                        continue;
                    }
                    Class<?> fieldClass = declaredField.getType();
                    Object dependency = getDependency(fieldClass);
                    if (!dependencyRegistry.containsKey(fieldClass) || dependency == null) {
                        throw new NullPointerException("Couldn't find dependency of " + commandClass.getName() + " in registry!");
                    }
                    declaredField.setAccessible(true);
                    declaredField.set(command, dependency);
                    declaredField.setAccessible(false);
                }

                // Register the command as usual
                registerCommand(command);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException("Exception while instantiating command " + commandClass.getName() + ": ", e);
            }
        }
        commandClasses.clear();
    }

    @Override
    public <T> void registerCommand(T command) {
        Class<?> commandClass = command.getClass();
        try {
            CommandMeta commandMeta = commandResolver.resolveCommand(commandClass);
            if (commandMeta.isGlobalCommand()) {
                globalCommand = command;
                globalCommandMeta = commandMeta;
                return;
            }
            commandRegistry.put(command, commandMeta);
            afterRegistration(command, commandMeta);
        } catch (InvalidCommandException e) {
            throw new RuntimeException("Exception while registering command " + commandClass.getName() + ": ", e);
        }
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
        addParserByType(parameterParser, parameterParser.getType(), override);
        Class<?>[] extraTypes = parameterParser.getExtraTypes();
        if (extraTypes == null) {
            return;
        }
        for (Class<?> extraType : extraTypes) {
            addParserByType(parameterParser, extraType, override);
        }
    }

    private void addParserByType(ParameterParser<?> parameterParser, Class<?> type, boolean override) {
        if (override) {
            parserRegistry.remove(type);
        }
        if (parserRegistry.containsKey(type)) {
            throw new IllegalStateException("Parser " + parameterParser.getClass().getName() + " for type " +
                    type.getName() + " is already registered!");
        }
        parserRegistry.put(type, parameterParser);
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
            if (meta.hasRootPermission() && hasPermission(sender, meta.getRootPermission())) {
                return callNoPermission(command, meta, sender, meta.getRootPermission(), commandString);
            }

            String[] arguments = new String[]{};
            if (commandString.length() > (label.length() + 1)) {
                arguments = commandString.substring(label.length() + 1).split(" ");
            }

            if (arguments.length == 0) {
                return callDefault(label, command, meta, sender);
            }

            if (arguments.length == 1 && arguments[0].equalsIgnoreCase("help")) {
                return callHelpAllSubcommands(sender, label, command, meta);
            }

            MetaLoop:
            for (MethodMeta methodMeta : meta.getSubCommandMetaList()) {

                String[] commandArguments = arguments;

                String[] annotationArguments = methodMeta.getSubCommand().split(" ");
                if (annotationArguments.length == 1 && annotationArguments[0].equalsIgnoreCase("")) {
                    annotationArguments = new String[]{};
                }

                if (methodMeta.isTextMerging()) {
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
                }

                // Decrease parameterLength by one, because of Sender objects
                int expectedLength = annotationArguments.length + (methodMeta.getParameterIndex().size() - 1);

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

                        // Get parser and parse the argument, add that into list.
                        ParameterParser<?> parameterParser = parserRegistry.get(parameterType);
                        if (parameterParser == null) {
                            throw new ParameterException("No parser found! (type=" + parameterType.getName() + ")");
                        }
                        parameterList.add(parameterParser.parse(argumentParam));

                    } catch (ParameterException exc) {
                        if (meta.getErrorMeta() != null) {
                            // If an error occurs, and the annotation is present, we show the sender
                            // the help message for the subcommand
                            if (meta.isShowHelpWithError()) {
                                List<CommandSyntax> syntaxList = new ArrayList<>();
                                syntaxList.add(methodMeta.getSyntax());
                                callHelp(command, meta, sender, label, syntaxList);
                            }
                            if (globalCommandMeta != null && globalCommandMeta.getHelpMeta() != null) {
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
                        sendSenderMessage(sender, "§cAn error occurred! Please check your syntax. §8(§7" + exc.getMessage() + "§8)");
                        break MetaLoop;
                    }
                }
                int parameterListLength = parameterList.size();
                boolean hasMethodWrongParams = false;
                for (int i = 0; i < parameterListLength; i++) {
                    Class<?> parameterListClass = parameterList.get(i).getClass();
                    Class<?> parameterIndexClass = methodMeta.getParameterIndex().get(i);
                    if (!parameterIndexClass.isAssignableFrom(parameterListClass)) {
                        hasMethodWrongParams = true;
                    }
                }
                if (hasMethodWrongParams) {
                    continue;
                }
                if (handleSenderType(command, meta, sender, methodMeta)) {
                    return true;
                }
                methodMeta.getMethod().invoke(command, parameterList.toArray(new Object[]{}));
                return true;
            }
            return callHelpAllSubcommands(sender, label, command, meta);
        } catch (Exception e) {
            if (!(e instanceof ConditionException) && !(e.getCause() instanceof ConditionException)) {
                e.printStackTrace();
                return false;
            }
            String message = null;
            if (e instanceof ConditionException) {
                message = e.getMessage();
            } else if (e.getCause() instanceof ConditionException) {
                message = e.getCause().getMessage();
            }
            sendSenderMessage(sender, message);
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
            }
            if (globalCommandMeta != null && globalCommandMeta.getHelpMeta() != null) {
                globalCommandMeta.getWrongSenderMeta().getMethod().invoke(globalCommand, sender);
                return true;
            }
            sendSenderMessage(sender, "§cThis command is only for " + expectedSender.getSimpleName() + "!");
            return true;
        }
        return false;
    }

    private <T> boolean callHelp(Object command, CommandMeta meta, T sender, String usedLabel, List<CommandSyntax> syntaxList)
            throws InvocationTargetException, IllegalAccessException {
        if (meta.getHelpMeta() == null) {
            if (globalCommandMeta != null && globalCommandMeta.getHelpMeta() != null) {
                if (handleSenderType(globalCommand, globalCommandMeta, sender, globalCommandMeta.getHelpMeta())) {
                    return true;
                }
                globalCommandMeta.getHelpMeta().getMethod().invoke(globalCommand, sender, usedLabel, syntaxList);
                return true;
            }
            return false;
        }
        if (handleSenderType(command, meta, sender, meta.getHelpMeta())) {
            return true;
        }
        meta.getHelpMeta().getMethod().invoke(command, sender, usedLabel, syntaxList);
        return true;
    }

    private <T> boolean callDefault(String label, Object command, CommandMeta meta, T sender)
            throws InvocationTargetException, IllegalAccessException {
        if (meta.getDefaultMeta() == null) {
            return callHelpAllSubcommands(sender, label, command, meta);
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
        if (meta.getDefaultMeta() != null) {
            MethodMeta defaultMeta = meta.getDefaultMeta();
            syntaxList.add(new CommandSyntax("", defaultMeta.getPermission()));
        }
        MethodMeta helpMeta = null;
        if (meta.getHelpMeta() != null) {
            helpMeta = meta.getHelpMeta();
        }
        if (globalCommandMeta != null && globalCommandMeta.getHelpMeta() != null) {
            helpMeta = globalCommandMeta.getHelpMeta();
        }
        if (helpMeta != null) {
            syntaxList.add(new CommandSyntax("help", null));
        }
        return callHelp(command, meta, sender, label, syntaxList);
    }

    private <T> boolean callNoPermission(Object command, CommandMeta meta, T sender, String permission, String commandString)
            throws InvocationTargetException, IllegalAccessException {
        if (meta.getNoPermissionMeta() == null) {
            if (globalCommandMeta != null && globalCommandMeta.getNoPermissionMeta() != null) {
                if (handleSenderType(globalCommand, globalCommandMeta, sender, globalCommandMeta.getNoPermissionMeta())) {
                    return true;
                }
                globalCommandMeta.getNoPermissionMeta().getMethod().invoke(globalCommand, sender, permission, commandString);
                return true;
            }
            sendSenderMessage(sender, "§cLooks like you don't have permission for that.");
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
        if (suggestions == null || suggestions.isEmpty()) {
            suggestions = new ArrayList<>();
        }
        List<String> completions = new ArrayList<>(suggestions);

        for (Object command : commandRegistry.keySet()) {
            CommandMeta meta = commandRegistry.get(command);

            // Ignored, sender doesn't have permission to use this command
            if (meta.hasRootPermission() && hasPermission(sender, meta.getRootPermission())) {
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
            // But only if we got no space at the end.
            if (arguments.length == 0 && !commandString.endsWith(" ")) {
                // Otherwise we check and print aliases
                for (String alias : meta.getAliasList()) {
                    alias = "/" + alias;
                    // Check if we found any alias
                    if (alias.startsWith(label) && !completions.contains(alias) && !alias.equalsIgnoreCase(label)) {
                        completions.add(alias);
                    }
                }
                // If the string is like "/command", just continue to next command
                continue;
            }
            String labelWithoutSlash = label.substring(1);
            if (!meta.getAliasList().contains(labelWithoutSlash)) {
                continue;
            }

            // Continue if no subcommands found
            if (meta.getSubCommandMetaList().isEmpty()) {
                continue;
            }

            for (MethodMeta methodMeta : meta.getSubCommandMetaList()) {

                // Get metaArguments and get expectedLength of arguments for the method
                String[] metaArgs = methodMeta.getSubCommand().split(" ");
                if (methodMeta.getSubCommand().equalsIgnoreCase("")) {
                    metaArgs = new String[]{};
                }
                int expectedLength = metaArgs.length + methodMeta.getParameterIndex().size() - 1;

                // If we got no arguments we return a emptyList.
                if (expectedLength <= 0) {
                    return Collections.emptyList();
                }

                // arguments already exceeds expected arguments
                if (expectedLength < arguments.length) {
                    continue;
                }

                // Continue if we only got a non-concatenating message
                if (methodMeta.isTextMerging()) {
                    continue;
                }

                // Create the expected index of the argument
                int currentIndex = commandString.endsWith(" ") ? arguments.length : arguments.length - 1;

                // Get the last argument if command doesn't end with a space
                String argument = null;
                if (!commandString.endsWith(" ") && (arguments.length - 1) >= 0) {
                    argument = arguments[currentIndex].toLowerCase(Locale.ROOT);
                }
                // Check if argumentIndex exceeds our metaArguments
                int metaLength = metaArgs.length;
                if (metaLength > 0 && commandString.endsWith(" ")) {
                    boolean skipSubCommand = false;
                    for (int i = 0; i < metaLength; i++) {
                        String metaArgument = metaArgs[i];
                        if (arguments.length >= metaLength) {
                            String passedArgument = arguments[i];
                            if (!passedArgument.equalsIgnoreCase(metaArgument)) {
                                skipSubCommand = true;
                                break;
                            }
                        }
                    }
                    if (skipSubCommand) {
                        continue;
                    }
                }

                if (metaArgs.length > 0 && (metaArgs.length - 1) >= currentIndex) {
                    String metaArgument = metaArgs[currentIndex].toLowerCase(Locale.ROOT);
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

                // Get the index on which the parameters of the method start
                int paramStartIndex = metaArgs.length + 1;

                // Check if argumentIndex exceeds our parameter start index
                if (paramStartIndex >= currentIndex) {
                    // Get the correct parameter index by subtracting the metaArgs length
                    // We don't need a '-1' here, because we ignore the sender parameter
                    int paramIndex = arguments.length - metaArgs.length;
                    if (commandString.endsWith(" ")) {
                        paramIndex += 1;
                    }

                    // Get the completions of the defined parameter-parser
                    Class<?>[] parameterTypes = methodMeta.getMethod().getParameterTypes();
                    if ((parameterTypes.length - 1) < paramIndex) {
                        continue;
                    }
                    Class<?> paramType = parameterTypes[paramIndex];
                    ParameterParser<?> parameterParser = parserRegistry.get(paramType);
                    if (parameterParser == null) {
                        continue;
                    }
                    // Handle parameter completions
                    List<String> paramCompletions = parameterParser.complete(argument);
                    for (String paramArg : paramCompletions) {
                        if (!completions.contains(paramArg)) {
                            completions.add(paramArg);
                        }
                    }
                }
            }
        }
        return completions;
    }
}