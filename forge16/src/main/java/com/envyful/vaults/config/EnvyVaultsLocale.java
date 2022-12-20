package com.envyful.vaults.config;

import com.envyful.api.config.data.ConfigPath;
import com.envyful.api.config.yaml.AbstractYamlConfig;
import com.google.common.collect.Lists;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.util.List;

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

    private List<String> cannotFindPlayer = Lists.newArrayList(
            "&c&l(!) &cCannot find that player"
    );

    private List<String> playerDoesntHaveVaultWithid = Lists.newArrayList(
            "&c&l(!) &cPlayer does not have a vault with that id!"
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

    public List<String> getCannotFindPlayer() {
        return this.cannotFindPlayer;
    }

    public List<String> getPlayerDoesntHaveVaultWithid() {
        return this.playerDoesntHaveVaultWithid;
    }
}
