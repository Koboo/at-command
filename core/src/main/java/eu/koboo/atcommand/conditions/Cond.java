package eu.koboo.atcommand.conditions;

import eu.koboo.atcommand.exceptions.ConditionException;
import lombok.experimental.UtilityClass;

/**
 * This is a utility class to check specific conditions and
 * throw a ConditionException if condition is not true.
 */
@UtilityClass
public class Cond {

    /**
     * @param conditionStatement The boolean which is checked.
     * @param message The message of the failed condition.
     */
    public void check(boolean conditionStatement, String message) {
        if (!conditionStatement) {
            throw new ConditionException(message);
        }
    }
}
