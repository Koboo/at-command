package eu.koboo.atcommand.parser;

import eu.koboo.atcommand.exceptions.ParameterException;

import java.lang.reflect.ParameterizedType;
import java.util.Collections;
import java.util.List;

/**
 * This class is used to parse the object of a method parameter
 *
 * @param <T> The generic type of the parsed object
 */
public abstract class ParameterParser<T> {

    /**
     * This method is ued to parse the object.
     *
     * @param value The argument as string value
     * @return The instance of the object
     * @throws ParameterException is thrown if no object could be parsed.
     */
    public abstract T parse(String value) throws ParameterException;

    /**
     * This method returns a user-friendly name of the object for the usages messages
     *
     * @return The name of the object for the message.
     */
    public String friendlyName() {
        if (getType().isEnum()) {
            StringBuilder enumBuilder = new StringBuilder();
            for (T enumConstant : getType().getEnumConstants()) {
                if (enumConstant instanceof Enum<?>) {
                    Enum<?> anEnum = (Enum<?>) enumConstant;
                    enumBuilder.append(anEnum.name()).append(" | ");
                }
            }
            String enumSyntax = enumBuilder.toString();
            return enumSyntax.substring(0, enumSyntax.length() - 3);
        }
        return getType().getSimpleName();
    }

    /**
     * This method is used to generate auto-completion.
     *
     * @param value The argument which should be auto-completed.
     * @return A list of possible auto-completions for the value.
     */
    public List<String> complete(String value) {
        return Collections.emptyList();
    }

    /**
     * This method is used to get the generic type class of the parser.
     *
     * @return The class of the generic type.
     */
    @SuppressWarnings("all")
    public Class<T> getType() {
        return ((Class<T>) ((ParameterizedType) getClass()
                .getGenericSuperclass()).getActualTypeArguments()[0]);
    }

    /**
     * This method is used to check against the method parameters.
     * This should return any extending or implementing types
     * of the parsed object.
     * E.g. Boolean.class and boolean.class
     *
     * @return An array of the parsable types
     */
    public Class<?>[] getExtraTypes() {
        return new Class[]{};
    }
}