package dev.binflux.atcommand.environment;

import dev.binflux.atcommand.environment.meta.CommandMeta;
import dev.binflux.atcommand.environment.meta.CommandSyntax;
import dev.binflux.atcommand.environment.meta.MethodMeta;
import dev.binflux.atcommand.exceptions.ConditionException;
import dev.binflux.atcommand.exceptions.InvalidCommandException;
import dev.binflux.atcommand.exceptions.ParameterException;
import dev.binflux.atcommand.parser.ParameterParser;
import dev.binflux.atcommand.parser.types.*;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

public abstract class CommandEnvironment implements Environment {

    private final CommandResolver commandResolver;
    private final Map<Object, CommandMeta> commandRegistry;
    private final Map<Class<?>, ParameterParser<?>> parserRegistry;

    private Object globalCommand;
    private CommandMeta globalCommandMeta;

    public CommandEnvironment() {

        commandResolver = new CommandResolver(this);
        commandRegistry = new HashMap<>();
        parserRegistry = new HashMap<>();

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
            e.printStackTrace();
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
        if (override) {
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
//                    System.out.println("Check: " + parameterListClass.getName() + "|" + parameterIndexClass.getName());
                    if (!parameterIndexClass.isAssignableFrom(parameterListClass)) {
                        hasMethodWrongParams = true;
                    }
                }
//                System.out.println("WrongSyntax: " + wrongSyntax);
                if (hasMethodWrongParams) {
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
        } catch (Exception e) {
            if (!(e instanceof ConditionException) && !(e.getCause() instanceof ConditionException)) {
                e.printStackTrace();
                return false;
            }
            String message = null;
            if(e instanceof ConditionException) {
                message = e.getMessage();
            } else if(e.getCause() instanceof ConditionException) {
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

                // Some debug logs.
                //System.out.println("=====================");
                //System.out.println("Method: " + methodMeta.getMethod().getName());
                //System.out.println("ArgsLength: " + arguments.length);

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

                // Some debug logs.
                //System.out.println("ExpectedLength: " + expectedLength);
                //System.out.println("MetaLength: " + metaArgs.length);

                // Create the expected index of the argument
                int currentIndex = commandString.endsWith(" ") ? arguments.length : arguments.length - 1;

                // Get the last argument if command doesn't end with a space
                String argument = null;
                if (!commandString.endsWith(" ") && (arguments.length - 1) >= 0) {
                    argument = arguments[currentIndex].toLowerCase(Locale.ROOT);
                }

                //System.out.println("CurrIndex: " + currentIndex);
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

                //System.out.println("ParamStartIndex: " + paramStartIndex);

                // Check if argumentIndex exceeds our parameter start index
                if (paramStartIndex >= currentIndex) {
                    // Get the correct parameter index by subtracting the metaArgs length
                    // We don't need a '-1' here, because we ignore the sender parameter
                    int paramIndex = arguments.length - metaArgs.length;
                    if (commandString.endsWith(" ")) {
                        paramIndex += 1;
                    }

                    //System.out.println("CurrParamIndex: " + paramIndex);
                    // Get the completions of the defined parameter-parser
                    Class<?>[] parameterTypes = methodMeta.getMethod().getParameterTypes();
                    //System.out.println("ParamSize: " + parameterTypes.length);
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