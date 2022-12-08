package eu.koboo.atcommand.environment;

import eu.koboo.atcommand.environment.meta.CommandMeta;
import eu.koboo.atcommand.parser.ParameterParser;

import java.util.List;

public interface Environment {

    /**
     * @return The class of the platform player.
     */
    Class<?> getPlayerClass();

    /**
     * @return The class of the platform console.
     */
    Class<?> getConsoleClass();

    /**
     * This method is used to send a message on a specific platform.
     *
     * @param sender  The sender of the platform.
     * @param message The message, which should send.
     * @param <S>     The generic type of the sender.
     */
    <S> void sendSenderMessage(S sender, String message);

    /**
     * This method is used to check permission of a sender on a specific platform.
     *
     * @param sender     The sender of the platform.
     * @param permission The permission to check on the sender.
     * @param <S>        The generic type of the sender.
     * @return if the sender has the permission on the platform.
     */
    <S> boolean hasNotPermission(S sender, String permission);

    /**
     * This method adds a dependency of an object to the environment,
     * to get injected into fields of command instances.
     *
     * @param object The object to add/register.
     * @param <D>    The generic type of the dependency object.
     */
    <D> void addDependency(D object);

    /**
     * This method is used to get the instance of a previously added object.
     *
     * @param dependencyClass The class of the object.
     * @param <D>             The generic type of the dependency object.
     * @return The instance of the object.
     */
    <D> D getDependency(Class<D> dependencyClass);

    /**
     * This method is used to register all command of multiple packages.
     *
     * @param packageNames The packages with commands, which should get registered
     */
    void registerCommandsIn(String... packageNames);

    /**
     * This method is used to register a custom-created instance of a command.
     *
     * @param command The instance of the command class
     * @param <C>     The generic type of the command object.
     */
    <C> void registerCommand(C command);

    /**
     * This method is used to handle behaviour after successful command validation.
     * Commands get registered the usual way, after the validation, so that method is
     * implemented by the platforms.
     *
     * @param command     The instance of the validated command
     * @param commandMeta The created meta-object of the command
     * @param <C>         The generic type of the command object.
     */
    <C> void afterRegistration(C command, CommandMeta commandMeta);

    /**
     * This method is used to register ParameterParsers
     * See below. Environment#registerParser
     *
     * @param parameterParser The parser, which should get registered.
     */
    void registerParser(ParameterParser<?> parameterParser);

    /**
     * This method is used to register a ParameterParser
     * and optionally to override existing parsers.
     *
     * @param parameterParser The parser, which should get registered.
     * @param override        if true, the previous parser get overriden.
     */
    void registerParser(ParameterParser<?> parameterParser, boolean override);

    /**
     * This method is used to handle the execution of commands
     *
     * @param sender  The sender, which execute the command
     * @param command The command string, which is executed
     * @param <S>     The generic type of the sender
     * @return true, if the framework could handle the command
     */
    <S> boolean handleCommand(S sender, String command);

    /**
     * This method is used to handle the auto-completion of commands
     *
     * @param sender      The sender, which tries to auto-complete commands
     * @param command     The command string, which should be auto-completed
     * @param completions The list of the platform-specific completions
     * @param <S>         The generic type of the sender
     * @return The list of the processed and updated completions.
     */
    <S> List<String> handleCompletions(S sender, String command, List<String> completions);
}