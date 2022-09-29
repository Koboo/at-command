package dev.binflux.atcommand.environment;

import dev.binflux.atcommand.environment.meta.CommandMeta;
import dev.binflux.atcommand.parser.ParameterParser;

import java.util.List;

public interface Environment {

    Class<?> getPlayerClass();

    Class<?> getConsoleClass();

    <S> void sendSenderMessage(S sender, String message);

    <S> boolean hasNotPermission(S sender, String permission);

    <D> void addDependency(D object);

    <D> D getDependency(Class<D> dependencyClass);

    void registerCommandsIn(String... packageNames);

    <C> void registerCommand(C command);

    <C> void afterRegistration(C command, CommandMeta commandMeta);

    void registerParser(ParameterParser<?> parameterParser);

    void registerParser(ParameterParser<?> parameterParser, boolean override);

    <S> boolean handleCommand(S sender, String command);

    <S> List<String> handleCompletions(S sender, String command, List<String> completions);
}