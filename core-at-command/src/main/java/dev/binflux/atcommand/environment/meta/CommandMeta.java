package dev.binflux.atcommand.environment.meta;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class CommandMeta {

    private final List<String> aliasList;
    private final String rootPermission;
    private final MethodMeta defaultMeta;
    private final MethodMeta helpMeta;
    private final MethodMeta noPermissionMeta;
    private final MethodMeta errorMeta;
    private final MethodMeta wrongSenderMeta;
    private final List<MethodMeta> subCommandMetaList;
    private final boolean isGlobalCommand;
    private final boolean showHelpOnDefault;
    private final boolean showHelpOnError;
    private final boolean showHelpWithError;

    public boolean hasRootPermission() {
        return rootPermission != null;
    }
}