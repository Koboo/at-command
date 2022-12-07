# _**// AtCommand**_

AtCommand is a framework to create Bukkit and BungeeCord commands via annotations.

## Overview

- [Features](#features)
- [Dependency](#dependency)
- [Get Started](#get-started)
- [Methods](#methods)
- [Permissions](#permissions)
- [Help](#help)
- [Register Commands](#register-commands)
- [Default Argument Parser](#default-argument-parsers)
- [Create Parser](#create-parameterparser)
- [Register Parser](#register-parameterparser)
- [License](/LICENSE)

### Features

- Command creation via annotations
- Multiple labels
- Permissions for commands and subcommands
- Automatic registration by package names
- Dynamic tab-completion of every command

### Dependency

Here you can see how to add the desired modules to your project. This example only
includes [Gradle](https://gradle.org/),
but can also support other build systems like [Maven](https://maven.apache.org/).

**Gradle:**

```groovy
repositories {
    maven {
        name 'koboo-reposilite'
        url 'https://reposilite.koboo.eu/releases'
    }
}

dependencies {
    /* Required for both platforms */
    compileOnly 'eu.koboo.atcommand:core:1.3.0'

    /* For Bungeecord usage */
    compileOnly 'eu.koboo.atcommand:bungee-plugin:1.3.0'

    /* For Bukkit usage */
    compileOnly 'eu.koboo.atcommand:bukkit-plugin:1.3.0'
}
```

## Get Started

First of all you need to set the ``@Label`` of the command. You can use the annotation multiple times,
to set multiple aliases.

**_Code-Example:_**

````java

@Label("testcommand")
@Label("testalias")
public class TestCommand {
}
````

You can create a ``@Global`` command with the handler-methods of the following annotations

- ``@OnHelp``
- ``@OnError``
- ``@WrongSender``
- ``@NoPermission``

The handler-methods can also be created in the individual commands.
If a command has its own handler-method, this one will be executed instead of the method from the ``@Global`` command.
Attention, the parameters of these methods are required by AtCommand!

**_Code-Example:_**

````java

@Global
public class TestGlobalCommand {

    @WrongSender
    public void wrongSender(CommandSender sender) {
        if (sender instanceof Player player) {
            player.sendMessage("You're not console!");
            return;
        }
        sender.sendMessage("You're not a player!");
    }

    @OnHelp
    public void onHelp(CommandSender sender, String command, List<CommandSyntax> syntaxList) {
        sender.sendMessage("Help of /" + command + ":");
        for (CommandSyntax syntax : syntaxList) {
            sender.sendMessage("Usage: /" + command + " " + syntax.getSyntax());
        }
    }

    @OnError
    public void onError(CommandSender sender, String error) {
        String addition = error == null ? "" : " (" + error + ")";
        sender.sendMessage("An internal error occurred." + addition);
    }

    @NoPermission
    public void noPermission(CommandSender sender, String permission, String command) {
        sender.sendMessage("You don't have permission to execute /" + command + "! (Permission: " + permission + ")");
    }
}
````

## Methods

To define methods in a command there are two annotations which allow this.

``@Default`` can define the default command without arguments.

``@Subcommand`` can define multiple subcommands, which are parsed and executed based on the given player arguments.

In addition to the method definition, the parsing of the arguments can be changed.
The following annotations are available for this purpose:

``@MergeText`` converts the given arguments to a string. This is passed as a single parameter to the method.

``@Order`` can change the parse order to better find methods depending on arguments. 99% of the time you don't need
to use this annotation, but there may be special cases where it can be used. (Smaller order = Higher parsing priority)

**_Code-Example:_**

````java

@Label("example")
@Label("thealiasforexample")
@Label("anotheraliasforthecommand")
public class ExampleCommand {

    @Default
    public void onDefault(CommandSender sender) {
        sender.sendMessage("You executed /example!");
    }

    // Ingame-Command: "/example subcommand"
    @Subcommand("subcommand")
    public void onSubCommand(CommandSender sender) {
        sender.sendMessage("You executed /example subcommand");
    }

    // Ingame-Command: "/example gm <0 | 1 | 2 | 3>"
    // Ingame-Command: "/example gm <survival | creative | adventure | spectator>"
    // (NOTE: GameMode is parsed through GameModeParser, @see Parser topic)
    @Subcommand("gm")
    public void onGameMode(Player player, GameMode gameMode) {
        player.sendMessage("You switched to " + gameMode.name() + " by executing /example gm <gamemode>");
    }

    // Ingame-Command: "/example send <Some text with spaces, which need to get merged>"
    @Subcommand("send")
    @MergeText // Merge arguments to text
    public void onSend(CommandSender sender, String text) {
        sender.sendMessage("You send '" + text + "' by executing /example send <Text>");
    }
}
````

## Permissions

And how can you set specific permissions for a command? Do you have to check them yourself,
although there is a ``@NoPermission`` method?

The answer is as simple as you do it: **No!**

To define permissions you just have to use the ``@Access("permission")`` annotation.

This works for a class of a command and for the inner methods, even together!
If a player does not have the permission, the ``@NoPermission`` method is called.

## Help

Since some players might be overwhelmed when an error occurs, and they don't always understand the error text,
you can also have the Help method run before the Error method.

For this you simply have to write ``@ShowHelpWithError`` under the ``@Label`` annotations of the command.

**_Code-Example:_**

````java

@Label("example")
@Label("thealiasforexample")
@Label("anotheraliasforthecommand")
@ShowHelpWithError
public class ExampleCommand {
    // ...
}
````

## Register Commands

You have written your commands and want to register them now? Nice!

There are two ways to do this, via the CommandEnvironment.

**Attention, you must not register the commands via Bukkit, Bungeecord or their ``plugin.yml`` files!**

### (1) **Instance registration:**

If you want to create the instance of the command yourself, you can register it using the following method:

**_Code-Example:_**

````java
public class TestPlugin extends JavaPlugin {
    public void onEnable() {
        Environment environment = AtCommand.getEnvironment();
        environment.registerCommand(new ExampleCommand());
    }
}
````

### (2) **Dynamic registration**

You can also create and register command instances via the plugin. Just insert the package names of the commands
into the method.

Dependency logic can be used to set custom values in the fields of the dynamic commands.

**To use dynamic registration, the command must have a public no-args constructor!**

**_Code-Example:_**

````java

@Label("test")
// ...
public class TestCommand {

    // The instance of your object gets automatically injected from the environment
    MyDatabaseManager manager;

    public TestCommand() {
        // You can also use the "@NoArgsConstructor" from lombok
    }

    // ...
}

public class TestPlugin extends JavaPlugin {
    public void onEnable() {
        Environment environment = AtCommand.getEnvironment();
        // Add an instance of your object to the dependencies of the environment
        environment.addDependency(new MyDatabaseManager());

        // Register all commands in the packages "eu.koboo.at.command.test" and "eu.koboo.otherplugin.commands"
        environment.registerCommandsIn("eu.koboo.atcommand.test", "eu.koboo.otherplugin.commands");
    }
}
````

## Default Argument Parsers

These ``ParameterParser<ObjectToParse>`` are by default added to the environment:

Java-related:

* ``StringParser``
* ``LongParser``
* ``IntegerParser``
* ``FloatParser``
* ``DoubleParser``
* ``ShortParser``
* ``UUIDParser``

Bukkit-related:

* ``GameModeParser``
* ``WorldParser``

Also, if there are no other suggestions, all player names are automatically added as suggestions.

## Create ParameterParser

The best example of how a ``ParameterParser`` works is the ``BooleanParser``:

**_Code-Example:_**

````java
// The type of the Parser stands in the brackets <>.
// In this example it's a Boolean.
public class BooleanParser extends ParameterParser<Boolean> {

    // A List of all possible subarguments for the auto-completion
    private static final List<String> COMPLETIONS = new ArrayList<>();

    static {
        COMPLETIONS.addAll(Arrays.asList(
                "yes", "1", "on", "true", "allow",
                "no", "0", "off", "false", "disallow"
        ));
    }

    // Return all possible parameter types which the parser also knowns.
    @Override
    public Class<?>[] getExtraTypes() {
        return new Class[]{boolean.class};
    }

    // In this method the string of the player gets parsed to the desired Type
    @Override
    public Boolean parse(String value) throws ParameterException {
        if (value.equalsIgnoreCase("yes")
                || value.equalsIgnoreCase("1")
                || value.equalsIgnoreCase("on")
                || value.equalsIgnoreCase("true")
                || value.equalsIgnoreCase("allow")) {
            return true;
        }
        if (value.equalsIgnoreCase("no")
                || value.equalsIgnoreCase("0")
                || value.equalsIgnoreCase("off")
                || value.equalsIgnoreCase("false")
                || value.equalsIgnoreCase("disallow")) {
            return false;
        }
        // And we throw an exception if we can't parse the String to a Boolean
        throw new ParameterException(value + " is not a valid Boolean (java.lang.Boolean)!");
    }

    // In this method the value will get auto-completed
    // This is method is optional. If no complete(..) method is provided, then no auto-completion
    // happens for this Parser.
    @Override
    public List<String> complete(String value) {
        // Example: "/command " - No subargument. Show all possible subarguments
        if (value == null) {
            return new ArrayList<>(COMPLETIONS);
        } else {
            value = value.toLowerCase(Locale.ROOT);
            List<String> completions = new ArrayList<>();
            // Check if the typed value starts with anything present in our subarguments and add them to the list.
            for (String completion : COMPLETIONS) {
                if (completion.startsWith(value) && !completions.contains(completion)) {
                    completions.add(value);
                }
            }
            return completions;
        }
    }

    // Create a user-friendly name for the usage-messages.
    @Override
    public String friendlyName() {
        return "true (yes/on/1/allow) | false (no/off/0/disallow)";
    }
}
````

## Register ParameterParser

To register a new Parser to the environment simply use:

**_Code-Example:_**

````java
public class TestPlugin extends JavaPlugin {
    public void onEnable() {
        Environment environment = AtCommand.getEnvironment();
        environment.registerParser(new BooleanParser());
    }
}
````

You can also override Parsers. If you don't override and the type already has a Parser, an exception is thrown.

**_Code-Example:_**

````java
public class TestPlugin extends JavaPlugin {
    public void onEnable() {
        Environment environment = AtCommand.getEnvironment();
        environment.registerParser(new BooleanParser(), true /*Should the parser be overriden, if already registered? */);
    }
}
````
