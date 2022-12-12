package com.envyful.vaults;

import com.envyful.api.command.sender.SenderTypeFactory;
import com.envyful.api.concurrency.UtilConcurrency;
import com.envyful.api.concurrency.UtilLogger;
import com.envyful.api.config.yaml.YamlConfigFactory;
import com.envyful.api.database.Database;
import com.envyful.api.database.impl.SimpleHikariDatabase;
import com.envyful.api.forge.command.ForgeCommandFactory;
import com.envyful.api.forge.gui.factory.ForgeGuiFactory;
import com.envyful.api.forge.player.ForgeEnvyPlayer;
import com.envyful.api.forge.player.ForgePlayerManager;
import com.envyful.api.forge.player.util.UtilPlayer;
import com.envyful.api.gui.factory.GuiFactory;
import com.envyful.api.player.SaveMode;
import com.envyful.api.player.save.impl.JsonSaveManager;
import com.envyful.api.type.UtilParse;
import com.envyful.vaults.command.ForgeEnvyPlayerSenderType;
import com.envyful.vaults.command.VaultsCommand;
import com.envyful.vaults.config.EnvyVaultsConfig;
import com.envyful.vaults.config.EnvyVaultsGraphics;
import com.envyful.vaults.player.PlayerVault;
import com.envyful.vaults.player.VaultsAttribute;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.Util;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

@Mod(EnvyVaults.MOD_ID)
public class EnvyVaults {

    public static final String MOD_ID = "envyvaults";

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

        GuiFactory.setPlatformFactory(new ForgeGuiFactory());
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
        SenderTypeFactory.register(new ForgeEnvyPlayerSenderType());

        this.commandFactory.registerInjector(ServerPlayerEntity.class, (sender, args) -> {
            ServerPlayerEntity player = UtilPlayer.findByName(args[0]);

            if (player == null) {
                sender.sendMessage(new StringTextComponent("Cannot find that player!"), Util.NIL_UUID);
            }

            return player;
        });

        this.commandFactory.registerInjector(PlayerVault.class, (sender, args) -> {
            if (!(sender instanceof ServerPlayerEntity)) {
                return null;
            }

            ForgeEnvyPlayer player = this.playerManager.getPlayer(((ServerPlayerEntity) sender));
            VaultsAttribute attribute = player.getAttribute(EnvyVaults.class);
            int id = UtilParse.parseInteger(args[0]).orElse(-1) - 1;

            if (id < 0) {
                PlayerVault vault = attribute.getVault(args[0]);

                if (vault != null) {
                    return vault;
                }

                //TODO: error message
                return null;
            }

            if (id >= attribute.getVaults().size()) {
                //TOOD: error message
                return null;
            }

            return attribute.getVault(id);
        });

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

    public static EnvyVaultsConfig getConfig() {
        return instance.config;
    }

    public static EnvyVaultsGraphics getGraphics() {
        return instance.graphics;
    }

    public Database getDatabase() {
        return this.database;
    }
}
