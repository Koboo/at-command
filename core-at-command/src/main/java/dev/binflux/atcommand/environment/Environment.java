package dev.binflux.atcommand.environment;

import dev.binflux.atcommand.parser.ParameterParser;

import java.util.List;

public interface Environment {

    Class<?> getPlayerClass();

    Class<?> getConsoleClass();

    <T> boolean hasNotPermission(T sender, String permission);

    <T> void registerCommand(T command);

    void registerParser(ParameterParser<?> parameterParser);

    void registerParser(ParameterParser<?> parameterParser, boolean override);

    <T> boolean handleCommand(T senderObject, String command);

    <T> List<String> handleCompletions(T senderObject, String command, List<String> completions);
}