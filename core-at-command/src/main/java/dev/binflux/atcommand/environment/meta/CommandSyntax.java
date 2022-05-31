package dev.binflux.atcommand.environment.meta;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class CommandSyntax {

    private final String syntax;
    private final String permission;
}