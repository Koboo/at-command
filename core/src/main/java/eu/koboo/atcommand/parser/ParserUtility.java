package eu.koboo.atcommand.parser;

import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.stream.Collectors;

@UtilityClass
public class ParserUtility {

    public String[] getArguments(String commandString) {
        String label = commandString.split(" ")[0].toLowerCase(Locale.ROOT);
        String[] arguments = new String[]{};
        // If possible, we create the arguments as String[]
        if (commandString.length() > (label.length() + 1)) {
            arguments = commandString.substring(label.length() + 1).split(" ");
        }
        return arguments;
    }

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

    public <T> List<String> complete(String value, T[] itemArray, Function<T, String> stringConverter) {
        return complete(value, Arrays.asList(itemArray), stringConverter);
    }
}