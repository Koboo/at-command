package dev.binflux.atcommand.environment;

import dev.binflux.atcommand.environment.meta.CommandSyntax;

import java.util.List;

public record CommandHelp(String label, List<CommandSyntax> syntaxList) {

}