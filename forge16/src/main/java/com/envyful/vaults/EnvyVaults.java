package com.envyful.vaults;

import com.envyful.api.concurrency.UtilConcurrency;
import com.envyful.api.concurrency.UtilLogger;
import com.envyful.api.config.yaml.YamlConfigFactory;
import com.envyful.api.database.Database;
import com.envyful.api.database.impl.SimpleHikariDatabase;
import com.envyful.api.forge.command.ForgeCommandFactory;
import com.envyful.api.forge.player.ForgePlayerManager;
import com.envyful.api.player.SaveMode;
import com.envyful.api.player.save.impl.JsonSaveManager;
import com.envyful.vaults.command.VaultsCommand;
import com.envyful.vaults.config.EnvyVaultsConfig;
import com.envyful.vaults.config.EnvyVaultsGraphics;
import com.envyful.vaults.player.VaultsAttribute;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class EnvyVaults {

    public static String MOD_ID = "envyvaults";

    private static EnvyVaults instance;

    private Logger logger = LogManager.getLogger(MOD_ID);

    private ForgePlayerManager playerManager = new ForgePlayerManager();
    private ForgeCommandFactory commandFactory = new ForgeCommandFactory();

    private EnvyVaultsConfig config;
    private EnvyVaultsGraphics graphics;
    private Database database;

    public EnvyVaults() {
        instance = this;
        UtilLogger.setLogger(this.logger);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onServerStarting(FMLServerStartingEvent event) {
        this.reloadConfig();

        if (this.config.getSaveMode() == SaveMode.JSON) {
            this.playerManager.setSaveManager(new JsonSaveManager<>());
        }

        this.playerManager.registerAttribute(this, VaultsAttribute.class);

        if (this.config.getSaveMode() == SaveMode.MYSQL) {
            UtilConcurrency.runAsync(() -> {
                this.database = new SimpleHikariDatabase(this.config.getDatabaseDetails());
            });
        }
    }

    @SubscribeEvent
    public void registerCommands(RegisterCommandsEvent event) {
        this.commandFactory.registerCommand(event.getDispatcher(), new VaultsCommand());
    }

    public void reloadConfig() {
        try {
            this.config = YamlConfigFactory.getInstance(EnvyVaultsConfig.class);
            this.graphics = YamlConfigFactory.getInstance(EnvyVaultsGraphics.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static EnvyVaults getInstance() {
        return instance;
    }

    public Logger getLogger() {
        return this.logger;
    }

    public ForgePlayerManager getPlayerManager() {
        return this.playerManager;
    }

    public ForgeCommandFactory getCommandFactory() {
        return this.commandFactory;
    }

    public EnvyVaultsConfig getConfig() {
        return this.config;
    }

    public EnvyVaultsGraphics getGraphics() {
        return this.graphics;
    }

    public Database getDatabase() {
        return this.database;
    }
}
