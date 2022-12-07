package eu.koboo.atcommand.conditions;

import eu.koboo.atcommand.exceptions.ConditionException;

public class Cond {

    public static void check(boolean conditionStatement, String message) {
        if (!conditionStatement) {
            throw new ConditionException(message);
        }
    }
}
