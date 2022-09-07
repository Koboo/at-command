package dev.binflux.atcommand.environment;

import dev.binflux.atcommand.environment.meta.CommandSyntax;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import javax.swing.text.html.HTMLDocument;
import java.util.List;

@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Getter
public class CommandHelp {

    String label;
    List<CommandSyntax> syntaxList;
}