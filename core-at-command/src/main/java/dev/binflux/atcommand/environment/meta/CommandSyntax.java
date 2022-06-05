package dev.binflux.atcommand.environment.meta;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public record CommandSyntax(String syntax, String permission) {

    @Deprecated
    public String getSyntax() {
        return syntax;
    }

    @Deprecated
    public String getPermission() {
        return permission;
    }

}