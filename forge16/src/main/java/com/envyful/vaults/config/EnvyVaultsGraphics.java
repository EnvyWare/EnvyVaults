
package com.envyful.vaults.config;

import com.envyful.api.config.data.ConfigPath;
import com.envyful.api.config.type.ConfigInterface;
import com.envyful.api.config.type.ConfigItem;
import com.envyful.api.config.type.ExtendedConfigItem;
import com.envyful.api.config.type.SQLDatabaseDetails;
import com.envyful.api.config.yaml.AbstractYamlConfig;
import com.envyful.api.player.SaveMode;
import com.envyful.api.type.Pair;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.util.List;

@ConfigSerializable
@ConfigPath("config/EnvyVaults/guis.yml")
public class EnvyVaultsGraphics extends AbstractYamlConfig {

    private MainUI mainUI = new MainUI();

    public EnvyVaultsGraphics() {
        super();
    }

    public MainUI getMainUI() {
        return this.mainUI;
    }

    @ConfigSerializable
    public static class MainUI {

        private ConfigInterface guiSettings = new ConfigInterface(
                "EnvyVaults", 6, ConfigInterface.FillType.BLOCK.name(), ImmutableMap.of(
                "one", new ConfigItem("minecraft:black_stained_glass_pane", 1, "", Lists.newArrayList())
        )
        );

        private List<Integer> vaultPositions = Lists.newArrayList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13);

        private ExtendedConfigItem nextPageButton = ExtendedConfigItem.builder()
                .type("minecraft:stone")
                .name("NEXT PAGE")
                .positions(Pair.of(2, 2))
                .build();
        private ExtendedConfigItem previousPageButton = ExtendedConfigItem.builder()
                .type("minecraft:stone")
                .name("PREVIOUS PAGE")
                .positions(Pair.of(3, 2))
                .build();

        private ConfigItem cannotAccessThisVault = new ConfigItem(
                "minecraft:barrier", 1, "&cMore perms", Lists.newArrayList()
        );

        private VaultDisplay display;

        public MainUI() {
        }

        public ConfigInterface getGuiSettings() {
            return this.guiSettings;
        }

        public List<Integer> getVaultPositions() {
            return this.vaultPositions;
        }

        public ExtendedConfigItem getNextPageButton() {
            return this.nextPageButton;
        }

        public ExtendedConfigItem getPreviousPageButton() {
            return this.previousPageButton;
        }

        public ConfigItem getCannotAccessThisVault() {
            return this.cannotAccessThisVault;
        }

        public VaultDisplay getDisplay() {
            return this.display;
        }
    }

    @ConfigSerializable
    public static class VaultDisplay {

        private String name;
        private List<String> lore;

        public VaultDisplay(String name, List<String> lore) {
            this.name = name;
            this.lore = lore;
        }

        public VaultDisplay() {
        }

        public String getName() {
            return this.name;
        }

        public List<String> getLore() {
            return this.lore;
        }
    }
}
