package com.envyful.vaults.config;

import com.envyful.api.config.data.ConfigPath;
import com.envyful.api.config.type.ConfigItem;
import com.envyful.api.config.type.SQLDatabaseDetails;
import com.envyful.api.config.yaml.AbstractYamlConfig;
import com.envyful.api.player.SaveMode;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.util.List;
import java.util.Map;

@ConfigSerializable
@ConfigPath("config/EnvyVaults/config.yml")
public class EnvyVaultsConfig extends AbstractYamlConfig {

    private SaveMode saveMode = SaveMode.JSON;

    private SQLDatabaseDetails databaseDetails = new SQLDatabaseDetails(
            "example", "0.0.0.0", 3306, "username", "password", "database"
    );

    private Map<String, VaultGroups> vaultGroups = ImmutableMap.of(
            "one", new VaultGroups("example", "com.envyware.example", 3)
    );

    private Map<String, ConfigItem> showOptions = ImmutableMap.of(
            "one", new ConfigItem("minecraft:diamond", 1, "this isn't even important", Lists.newArrayList()),
            "two", new ConfigItem("pixelmon:pixelmon_sprite", 1, "", Lists.newArrayList(), Maps.newHashMap(),
                    ImmutableMap.of(
                            "ndex", new ConfigItem.NBTValue("short", "1"),
                            "form", new ConfigItem.NBTValue("string", ""),
                            "gender", new ConfigItem.NBTValue("byte", "0"),
                            "palette", new ConfigItem.NBTValue("string", "none")
                    ))
    );

    private int vaultHeight = 6;

    private String defaultVaultName = "Vault #%id%";

    private ConfigItem defaultDisplayItem = ConfigItem.builder()
            .type("minecraft:stone")
            .amount(1)
            .build();

    public EnvyVaultsConfig() {
        super();
    }

    public SaveMode getSaveMode() {
        return this.saveMode;
    }

    public SQLDatabaseDetails getDatabaseDetails() {
        return this.databaseDetails;
    }

    public List<VaultGroups> getVaults() {
        return Lists.newArrayList(this.vaultGroups.values());
    }

    public List<ConfigItem> getShowOptions() {
        return Lists.newArrayList(this.showOptions.values());
    }

    public String getDefaultVaultName() {
        return this.defaultVaultName;
    }

    public ConfigItem getDefaultDisplayItem() {
        return this.defaultDisplayItem;
    }

    public int getVaultHeight() {
        return this.vaultHeight;
    }

    @ConfigSerializable
    public static class VaultGroups {

        private String groupId;
        private String permission;
        private int vaultNumber;

        public VaultGroups(String groupId, String permission, int vaultNumber) {
            this.groupId = groupId;
            this.permission = permission;
            this.vaultNumber = vaultNumber;
        }

        public VaultGroups() {
        }

        public String getGroupId() {
            return this.groupId;
        }

        public String getPermission() {
            return this.permission;
        }

        public int getVaultNumber() {
            return this.vaultNumber;
        }
    }
}
