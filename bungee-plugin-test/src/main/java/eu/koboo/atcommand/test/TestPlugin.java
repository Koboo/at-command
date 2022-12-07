package eu.koboo.atcommand.test;

import eu.koboo.atcommand.AtCommand;
import net.md_5.bungee.api.plugin.Plugin;

@SuppressWarnings("unused")
public class TestPlugin extends Plugin {

    @Override
    public void onEnable() {
        AtCommand.getEnvironment().registerCommandsIn("eu.koboo.atcommand.test.commands",
                "eu.koboo.atcommand.test.global");
        super.onEnable();
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }
}
