package eu.koboo.atcommand;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import eu.koboo.atcommand.environment.VelocityCommandEnvironment;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.slf4j.Logger;

@Plugin(id = "PROJECT_NAME", name = "PROJECT_NAME", version = "PROJECT_VERSION",
        authors = {"PLUGIN_AUTHOR"})
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AtCommand {

    @Getter
    private static VelocityCommandEnvironment environment;

    ProxyServer proxyServer;
    Logger logger;

    @Inject
    public AtCommand(ProxyServer proxyServer, Logger logger) {
        this.proxyServer = proxyServer;
        this.logger = logger;
    }

    @Subscribe
    public void onInitialization(ProxyInitializeEvent event) {
        environment = new VelocityCommandEnvironment(this, proxyServer);
        logger.info("Initialized " + VelocityCommandEnvironment.class.getName() + "!");
    }

    @Subscribe
    public void onDisable(ProxyShutdownEvent event) {
        if (environment != null) {
            environment.destroy();
        }
        logger.info("Destroyed " + VelocityCommandEnvironment.class.getName() + "!");
    }
}
