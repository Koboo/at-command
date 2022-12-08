package eu.koboo.atcommand.parser;

import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * This is a utility class to avoid code duplication related to auto-completion
 * in ParameterParsers.
 */
@UtilityClass
public class ParserUtility {

    /**
     * This method creates an array of arguments using the given command string.
     *
     * @param commandString The command string executed by the sender
     * @return An array of all arguments of the command string.
     */
    public String[] getArguments(String commandString) {
        String label = commandString.split(" ")[0].toLowerCase(Locale.ROOT);
        String[] arguments = new String[]{};
        // If possible, we create the arguments as String[]
        if (commandString.length() > (label.length() + 1)) {
            arguments = commandString.substring(label.length() + 1).split(" ");
        }
        return arguments;
    }

    /**
     * This method is used to simplify auto-completion.
     *
     * @param value           The argument parsed in by ParameterParser#complete.
     * @param itemList        A list of all possible auto-completions.
     * @param stringConverter A Function, to convert any T into a string representation.
     * @param <T>             The generic type of the ParameterParser
     * @return The list of possible auto-completions of the value.
     */
    public <T> List<String> complete(String value, List<T> itemList, Function<T, String> stringConverter) {
        if (value == null) {
            return itemList.stream()
                    .map(stringConverter)
                    .collect(Collectors.toList());
        }
        value = value.toLowerCase(Locale.ROOT);
        List<String> completionList = new ArrayList<>();
        for (T item : itemList) {
            String completion = stringConverter.apply(item).toLowerCase(Locale.ROOT);
            if (!completion.startsWith(value) || completionList.contains(completion)) {
                continue;
            }
            completionList.add(value);
        }
        return completionList;
    }

    /**
     * Same method as above, but takes an array instead of a list.
     * See ParserUtility#complete.
     */
    public <T> List<String> complete(String value, T[] itemArray, Function<T, String> stringConverter) {
        return complete(value, Arrays.asList(itemArray), stringConverter);
    }
}