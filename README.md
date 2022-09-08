# _**// AtCommand**_

AtCommand is a framework for creating Bukkit and Bungeecord commands.
The framework uses annotations for simplification, which are written over
the command class or the methods within the class.

## Overview

* [Dependency](#dependency)
* [Upcoming Features](#upcoming-features)
* [Command Annotations](#command-annotations)
* [Method Annotations](#method-annotations)
* [Option Annotations](#option-annotations)
* [Create Commands](#create-commands)
* [Register Commands](#register-commands)
* [Default Argument Parser](#default-argument-parsers)
* [Create own Parser](#create-own-parser)
* [Register new Parser](#register-new-parser)

### Dependency

Here you can see how to add the desired modules to your project. This example only includes [Gradle](https://gradle.org/),
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
    compileOnly 'dev.binflux:core-at-command:1.0'
    compileOnly 'dev.binflux:bungee-at-command:1.0'
    compileOnly 'dev.binflux:bukkit-at-command:1.0'
}
```
## Usage

### Command Annotations

Set command labels:
* ``@Label("command")``
You can use this annotation multiple times.

Execute ``OnHelp``-method, before the ``OnError``-method is executed to display help of the subcommand:
* ``@ShowHelpWithError``

Create global methods for ``OnHelp``, ``OnError``, ``WrongSender`` and ``NoPermission``:
* ``@Global``

### Method Annotations

Set the method for command without arguments:
````java
@Default
public void onDefault(CommandSender sender) {
    // Do something..
}
````

Set the method for no permissions:
````java
@NoPermission
public void onNoPermission(CommandSender sender, String missingPermission, String executedSyntax) {
    // Do something..
}
````

Set the method for error handling:
````java
@OnError
public void onError(CommandSender sender, String errorMessage) {
    // Do something..
}
````

Set the method for help-printing:
````java
@OnHelp
public void onHelp(CommandSender sender, String executedLabel, List<CommandSyntax> syntaxList) {
    // Do something..
}
````

Set the methods sub-command:
````java
@Subcommand
public void onSubcommandWithoutName(CommandSender sender, String anyArgument /*, other arguments..*/) {
    // Do something..
}
        
@Subcommand("subcommandname")
public void onSubcommandWithName(CommandSender sender /*, other arguments..*/) {
    // Do something..
}
````

Set the method for wrong sender type:
````java
@WrongSender
public void onWrongSender(CommandSender sender) {
    // Do something..
}
````

### Option Annotations

Set a custom permission:
* ``@Access("permission.goes.here")``

Merge the arguments to one concated ``String``:
* ``@MergeText``

Modify the parsing and execution order: (optional)
* ``@Order(99)``

### Create Commands

* Create a new class and annotate with ``@Alias``
````java
@Alias(alias = {"example", "examplecommand"})
public class ExampleCommand {
    
}
````

* Create a method for command without arguments ``/example`` with ``@Default``
````java
@Alias(alias = {"example", "examplecommand"})
public class ExampleCommand {
    
    @Default
    public void onDefault(CommandSender sender) {
        // Do something with the sender...
        sender.sendMessage("You executed /example!");
    }
}
````

* Create a method for showing help messages with ``@OnHelp``
````java
@Alias(alias = {"example", "examplecommand"})
public class ExampleCommand {
    
    @Default
    public void onDefault(CommandSender sender) {
        // Do something with the sender...
        sender.sendMessage("You executed /example!");
    }
    
    @OnHelp
    public void onHelp(CommandSender sender, CommandHelp commandHelp) {
        // The executed command (or alias) without leading "/"
        String command = commandHelp.getLabel();
        
        // The possible syntax, which can be executed
        List<String> commandSyntax = commandHelp.getSyntaxList();
    }
}
````

* Now we can replace ``@Default`` with ``@OnHelp``, just annotate the command with ``@ShowHelpOnDefault``
````java
@Alias(alias = {"example", "examplecommand"})
public class ExampleCommand {
    
    @OnHelp
    public void onHelp(CommandSender sender, CommandHelp commandHelp) {
        // The executed command (or alias) without leading "/"
        String command = commandHelp.getLabel();
        
        // The possible syntax, which can be executed
        List<String> commandSyntax = commandHelp.getSyntaxList();
    }
}
````

* Create a method for errors and show help with errors.
````java
@Alias(alias = {"example", "examplecommand"})
@ShowHelpWithError
public class ExampleCommand {
    
    @OnHelp
    public void onHelp(CommandSender sender, CommandHelp commandHelp) {
        // The executed command (or alias) without leading "/"
        String command = commandHelp.getLabel();
        
        // The possible syntax, which can be executed
        List<String> commandSyntax = commandHelp.getSyntaxList();
    }
    
    @OnError
    public void onError(CommandSender sender, String error) {
        sender.sendMessage("You got an error: " + error);
    }
}
````

* The ``@OnError`` method can also be replaced by ``@OnHelp`` with ``@ShowHelpOnError``
````java
@Alias(alias = {"example", "examplecommand"})
@ShowHelpOnError
public class ExampleCommand {
    
    @OnHelp
    public void onHelp(CommandSender sender, CommandHelp commandHelp) {
        // The executed command (or alias) without leading "/"
        String command = commandHelp.getLabel();
        
        // The possible syntax, which can be executed
        List<String> commandSyntax = commandHelp.getSyntaxList();
    }
}
````


* Create a method for no permissions.
````java
@Alias(alias = {"example", "examplecommand"})
@ShowHelpOnError
public class ExampleCommand {
    
    @OnHelp
    public void onHelp(CommandSender sender, CommandHelp commandHelp) {
        // The executed command (or alias) without leading "/"
        String command = commandHelp.getLabel();
        
        // The possible syntax, which can be executed
        List<String> commandSyntax = commandHelp.getSyntaxList();
    }
    
    @NoPermission
    public void noPermission(CommandSender sender, String permission, String command) {
        sender.sendMessage("You don't have the permission: " + permission);
        sender.sendMessage("You tried to execute: " + command);
    }
}
````
* If you remove the ``@NoPermission``-method no action is happening if the ``Sender`` doesn't have permission.

````java
@Alias(alias = {"example", "examplecommand"})
@ShowHelpOnError
public class ExampleCommand {
    
    @OnHelp
    public void onHelp(CommandSender sender, CommandHelp commandHelp) {
        // The executed command (or alias) without leading "/"
        String command = commandHelp.getLabel();
        
        // The possible syntax, which can be executed
        List<String> commandSyntax = commandHelp.getSyntaxList();
    }
}
````

* Now we can create several sub-commands with and without arguments.
````java
@Alias(alias = {"example", "examplecommand"})
@ShowHelpOnError
public class ExampleCommand {
    
    @OnHelp
    public void onHelp(CommandSender sender, CommandHelp commandHelp) {
        // The executed command (or alias) without leading "/"
        Stirng command = commandHelp.getLabel();
        
        // The possible syntax, which can be executed
        List<String> commandSyntax = commandHelp.getSyntaxList();
    }
    
    // Command: "/example subcommand"
    @Subcommand("subcommand")
    public void onSubCommand(CommandSender sender) {
        sender.sendMessage("You executed onSubCommand");
    }

    // Command: "/example gm <0 | 1 | 2 | 3>"
    // Command: "/example gm <survival | creative | adventure | spectator>"
    // (NOTE: GameMode is parsed through GameModeParser)
    @Subcommand("gm")
    public void onGameMode(Player player, GameMode gameMode) {
        player.sendMessage("You switched to: " + gameMode.name());
    }
    
    // Command: "/example send <Some text with spaces>" 
    @Subcommand("send")
    @MergeText // Concat arguments to text
    public void onSend(CommandSender sender, String text) {
        sender.sendMessage("You send: " + text);
    }
}
````

### Register Commands

If you want to register a command, you do it through the ``CommandEnvironment`` of the desired platform.

````java
Environment environment = AtCommandPlugin.getEnvironment();
environment.registerCommand(new ExampleCommand());
````

**No need to register it through Bukkit, BungeeCord or in their ``plugin.yml`` files.**

### Default Argument Parsers

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

### Create own Parser

The best example of how a ``ParameterParser`` works is the ``BooleanParser``:

````java
// The type of the Parser stands in the brackets <>. 
// In this example it's a Boolean.
public class BooleanParser extends ParameterParser<Boolean> {

    // A static final List of all possible arguments for the (optional) auto-completion
    private static final List<String> COMPLETIONS = new ArrayList<>();

    static {
        COMPLETIONS.addAll(Arrays.asList(
                "yes", "1", "on", "true", "allow",
                "no", "0", "off", "false", "disallow"
        ));
    }

    // In this method the string of the player gets parsed to the desired Type
    @Override
    public Boolean parse(String value) throws ParameterException {
        if(value.equalsIgnoreCase("yes")
                || value.equalsIgnoreCase("1")
                || value.equalsIgnoreCase("on")
                || value.equalsIgnoreCase("true")
                || value.equalsIgnoreCase("allow")) {
            return true;
        }
        if(value.equalsIgnoreCase("no")
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
        if(value == null) {
            return new ArrayList<>(COMPLETIONS);
        } else {
            value = value.toLowerCase(Locale.ROOT);
            List<String> completions = new ArrayList<>();
            // Check if the typed value if present in our subarguments and add them to the list.
            for (String completion : COMPLETIONS) {
                if(completion.startsWith(value) && !completions.contains(completion)) {
                    completions.add(value);
                }
            }
            return completions;
        }
    }

    // Create a user-friendly name for the usages.
    @Override
    public String friendlyName() {
        return "true (yes/on/1/allow) | false (no/off/0/disallow)";
    }
}
````

### Register new Parser

To register a new created Parser simply use:

````java
Environment environment = KloudAPI.getEnvironment();
environment.registerParser(new BooleanParser());
````

You can also override Parser. If you don't don't override and the type already has a Parser, an exception is thrown.

````java
Environment environment = KloudAPI.getEnvironment();
environment.registerParser(new BooleanParser(), true /*Should the parser be overriden? */);
````