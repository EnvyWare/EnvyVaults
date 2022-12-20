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
@ConfigPath("config/EnvyVaults/locale.yml")
public class EnvyVaultsLocale extends AbstractYamlConfig {

    private List<String> defaultCommand = Lists.newArrayList();

    private List<String> openMessage = Lists.newArrayList(
            "Opened %vault%"
    );

    private List<String> invalidIdMessage = Lists.newArrayList(
            "&c&l(!) &cInvalid vault id!"
    );

    private List<String> notGotThatManyVaultsMessage = Lists.newArrayList(
            "&c&l(!) &cYou do not have that many vaults!"
    );

    private List<String> renamedVaultMessage = Lists.newArrayList(
            "&e&l(!) &eYou've renamed your vault from %old_name% to %name%"
    );

    public EnvyVaultsLocale() {
    }

    public List<String> getDefaultCommand() {
        return this.defaultCommand;
    }

    public List<String> getOpenMessage() {
        return this.openMessage;
    }

    public List<String> getInvalidIdMessage() {
        return this.invalidIdMessage;
    }

    public List<String> getNotGotThatManyVaultsMessage() {
        return this.notGotThatManyVaultsMessage;
    }

    public List<String> getRenamedVaultMessage() {
        return this.renamedVaultMessage;
    }
}
