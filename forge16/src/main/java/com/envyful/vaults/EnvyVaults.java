package com.envyful.vaults;

import com.envyful.api.concurrency.UtilConcurrency;
import com.envyful.api.concurrency.UtilLogger;
import com.envyful.api.config.yaml.YamlConfigFactory;
import com.envyful.api.database.Database;
import com.envyful.api.database.impl.SimpleHikariDatabase;
import com.envyful.api.forge.command.ForgeCommandFactory;
import com.envyful.api.forge.concurrency.UtilForgeConcurrency;
import com.envyful.api.forge.player.ForgePlayerManager;
import com.envyful.api.player.SaveMode;
import com.envyful.api.player.save.impl.JsonSaveManager;
import com.envyful.vaults.config.EnvyVaultsConfig;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class EnvyVaults {

    public static String MOD_ID = "envyvaults";

    private Logger logger = LogManager.getLogger(MOD_ID);

    private ForgePlayerManager playerManager = new ForgePlayerManager();
    private ForgeCommandFactory commandFactory = new ForgeCommandFactory();

    private EnvyVaultsConfig config;
    private Database database;

    public EnvyVaults() {
        UtilLogger.setLogger(this.logger);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onServerStarting(FMLServerStartingEvent event) {
        this.reloadConfig();

        if (this.config.getSaveMode() == SaveMode.JSON) {
            this.playerManager.setSaveManager(new JsonSaveManager<>());
        }

        if (this.config.getSaveMode() == SaveMode.MYSQL) {
            UtilConcurrency.runAsync(() -> {
                this.database = new SimpleHikariDatabase(this.config.getDatabaseDetails());
            });
        }
    }

    public void reloadConfig() {
        try {
            this.config = YamlConfigFactory.getInstance(EnvyVaultsConfig.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    public Database getDatabase() {
        return this.database;
    }
}
