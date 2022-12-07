package eu.koboo.atcommand.test;

import eu.koboo.atcommand.AtCommand;
import org.bukkit.plugin.java.JavaPlugin;

@SuppressWarnings("unused")
public class TestPlugin extends JavaPlugin {

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
