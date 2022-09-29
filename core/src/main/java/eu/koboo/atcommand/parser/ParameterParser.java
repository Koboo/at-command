package eu.koboo.atcommand.parser;

import eu.koboo.atcommand.exceptions.ParameterException;

import java.lang.reflect.ParameterizedType;
import java.util.Collections;
import java.util.List;

public abstract class ParameterParser<T> {

    public abstract T parse(String value) throws ParameterException;

    public String friendlyName() {
        if(getType().isEnum()) {
            StringBuilder enumBuilder = new StringBuilder();
            for (T enumConstant : getType().getEnumConstants()) {
                if(enumConstant instanceof Enum<?>) {
                    Enum<?> anEnum = (Enum<?>) enumConstant;
                    enumBuilder.append(anEnum.name()).append(" | ");
                }
            }
            String enumSyntax = enumBuilder.toString();
            return enumSyntax.substring(0, enumSyntax.length() - 3);
        }
        return getType().getSimpleName();
    }

    public List<String> complete(String value) {
        return Collections.emptyList();
    }

    @SuppressWarnings("all")
    public Class<T> getType(){
        return ((Class<T>) ((ParameterizedType) getClass()
                .getGenericSuperclass()).getActualTypeArguments()[0]);
    }

    public Class<?>[] getExtraTypes() {
        return null;
    }
}