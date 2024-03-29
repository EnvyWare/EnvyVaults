package com.envyful.vaults;

import com.envyful.api.command.sender.SenderTypeFactory;
import com.envyful.api.concurrency.UtilConcurrency;
import com.envyful.api.concurrency.UtilLogger;
import com.envyful.api.config.yaml.YamlConfigFactory;
import com.envyful.api.database.Database;
import com.envyful.api.database.impl.SimpleHikariDatabase;
import com.envyful.api.forge.chat.UtilChatColour;
import com.envyful.api.forge.command.ForgeCommandFactory;
import com.envyful.api.forge.command.parser.ForgeAnnotationCommandParser;
import com.envyful.api.forge.gui.factory.ForgeGuiFactory;
import com.envyful.api.forge.player.ForgeEnvyPlayer;
import com.envyful.api.forge.player.ForgePlayerManager;
import com.envyful.api.forge.player.UsernameFactory;
import com.envyful.api.forge.player.util.UtilPlayer;
import com.envyful.api.gui.factory.GuiFactory;
import com.envyful.api.player.SaveMode;
import com.envyful.api.player.save.impl.JsonSaveManager;
import com.envyful.api.type.UtilParse;
import com.envyful.vaults.command.ForgeEnvyPlayerSenderType;
import com.envyful.vaults.command.VaultsCommand;
import com.envyful.vaults.config.EnvyVaultsConfig;
import com.envyful.vaults.config.EnvyVaultsGraphics;
import com.envyful.vaults.config.EnvyVaultsLocale;
import com.envyful.vaults.config.Queries;
import com.envyful.vaults.player.PlayerVault;
import com.envyful.vaults.player.VaultsAttribute;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@Mod(EnvyVaults.MOD_ID)
public class EnvyVaults {

    public static final String MOD_ID = "envyvaults";

    private static EnvyVaults instance;

    private Logger logger = LogManager.getLogger(MOD_ID);

    private ForgePlayerManager playerManager = new ForgePlayerManager();
    private ForgeCommandFactory commandFactory = new ForgeCommandFactory(ForgeAnnotationCommandParser::new, playerManager);

    private EnvyVaultsConfig config;
    private EnvyVaultsLocale locale;
    private EnvyVaultsGraphics graphics;
    private Database database;

    public EnvyVaults() {
        UsernameFactory.init();
        instance = this;
        UtilLogger.setLogger(this.logger);
        MinecraftForge.EVENT_BUS.register(this);

        GuiFactory.setPlatformFactory(new ForgeGuiFactory());
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        this.reloadConfig();

        if (this.config.getSaveMode() == SaveMode.JSON) {
            this.playerManager.setSaveManager(new JsonSaveManager<>(playerManager));
        }

        this.playerManager.registerAttribute(VaultsAttribute.class);

        if (this.config.getSaveMode() == SaveMode.MYSQL) {
            UtilConcurrency.runAsync(() -> {
                this.database = new SimpleHikariDatabase(this.config.getDatabaseDetails());
                this.createTable();
            });
        }
    }

    private void createTable() {
        try (Connection connection = this.database.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(Queries.CREATE_TABLE)) {
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @SubscribeEvent
    public void registerCommands(RegisterCommandsEvent event) {
        SenderTypeFactory.register(new ForgeEnvyPlayerSenderType());

        this.commandFactory.registerInjector(ServerPlayer.class, (sender, args) -> {
            var player = UtilPlayer.findByName(args[0]);

            if (player == null) {
                sender.sendSystemMessage(UtilChatColour.colour("Cannot find that player!"));
            }

            return player;
        });

        this.commandFactory.registerInjector(PlayerVault.class, (sender, args) -> {
            if (!(sender instanceof ServerPlayer)) {
                return null;
            }

            ForgeEnvyPlayer player = this.playerManager.getPlayer(((ServerPlayer) sender));
            VaultsAttribute attribute = player.getAttribute(VaultsAttribute.class);
            int id = UtilParse.parseInteger(args[0]).orElse(-1) - 1;

            if (id < 0) {
                PlayerVault vault = attribute.getVault(args[0]);

                if (vault != null) {
                    return vault;
                }

                for (String s : this.locale.getInvalidIdMessage()) {
                    sender.sendSystemMessage(UtilChatColour.colour(s));
                }
                return null;
            }

            if (id >= attribute.getVaults().size()) {
                for (String s : this.locale.getNotGotThatManyVaultsMessage()) {
                    sender.sendSystemMessage(UtilChatColour.colour(s));
                }
                return null;
            }

            return attribute.getVault(id);
        });

        this.commandFactory.registerCommand(event.getDispatcher(), this.commandFactory.parseCommand(new VaultsCommand()));
    }

    public void reloadConfig() {
        try {
            this.config = YamlConfigFactory.getInstance(EnvyVaultsConfig.class);
            this.locale = YamlConfigFactory.getInstance(EnvyVaultsLocale.class);
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

    public static EnvyVaultsConfig getConfig() {
        return instance.config;
    }

    public static EnvyVaultsGraphics getGraphics() {
        return instance.graphics;
    }

    public Database getDatabase() {
        return this.database;
    }

    public static EnvyVaultsLocale getLocale() {
        return instance.locale;
    }
}
