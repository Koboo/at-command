package eu.koboo.atcommand.parser.types;

import eu.koboo.atcommand.exceptions.ParameterException;
import eu.koboo.atcommand.parser.ParameterParser;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateParser extends ParameterParser<Date> {

    private static final String[] datePatternArray = {"dd/MM/yyyy", "dd-MM-yyyy", "dd.MM.yyyy"};

    @Override
    public Date parse(String value) throws ParameterException {
        Date date = null;
        for (String datePattern : datePatternArray) {
            try {
                SimpleDateFormat format = new SimpleDateFormat(datePattern);
                date = format.parse(value);
            } catch (ParseException e) {
                // ignoring, we try every pattern
            }
            if (date != null) {
                break;
            }
        }
        if (date == null) {
            throw new ParameterException(value + " is not a valid date!");
        }
        return date;
    }

    @Override
    public String friendlyName() {
        return "Date";
    }
}
