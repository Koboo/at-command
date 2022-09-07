package dev.binflux.atcommand.environment.meta;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.util.List;

@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Getter
public class CommandMeta {

    List<String> aliasList;
    String rootPermission;
    MethodMeta defaultMeta;
    MethodMeta helpMeta;
    MethodMeta noPermissionMeta;
    MethodMeta errorMeta;
    MethodMeta wrongSenderMeta;
    List<MethodMeta> subCommandMetaList;
    boolean isGlobalCommand;
    boolean showHelpWithError;

    public boolean hasRootPermission() {
        return rootPermission != null;
    }
}