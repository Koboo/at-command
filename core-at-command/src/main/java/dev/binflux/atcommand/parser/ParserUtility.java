package dev.binflux.atcommand.parser;

import java.util.Locale;

public class ParserUtility {

    public static String[] getArguments(String commandString) {
        String label = commandString.split(" ")[0].toLowerCase(Locale.ROOT);
        String[] arguments = new String[]{};
        // If possible, we create the arguments as String[]
        if (commandString.length() > (label.length() + 1)) {
            arguments = commandString.substring(label.length() + 1).split(" ");
        }
        return arguments;
    }
}