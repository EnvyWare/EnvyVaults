package com.envyful.vaults.player;

import com.envyful.api.forge.player.ForgeEnvyPlayer;
import com.envyful.api.forge.player.attribute.AbstractForgeAttribute;
import com.envyful.api.forge.player.util.UtilPlayer;
import com.envyful.api.player.EnvyPlayer;
import com.envyful.api.player.attribute.PlayerAttribute;
import com.envyful.api.player.save.attribute.DataDirectory;
import com.envyful.vaults.EnvyVaults;
import com.envyful.vaults.config.EnvyVaultsConfig;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.UUID;

@DataDirectory("config/players/EnvyVaults/")
public class VaultsAttribute extends AbstractForgeAttribute<EnvyVaults> {

    private List<PlayerVault> vaults;
    private int allowedVaults = 0;

    public VaultsAttribute(EnvyVaults manager, EnvyPlayer<?> parent) {
        super(manager, (ForgeEnvyPlayer) parent);
    }

    public VaultsAttribute(UUID uuid) {
        super(uuid);
    }

    public List<PlayerVault> getVaults() {
        return this.vaults;
    }

    public boolean canAccess(int vaultId) {
        return vaultId <= this.allowedVaults;
    }

    public PlayerVault getVault(int vaultId) {
        for (PlayerVault vault : this.vaults) {
            if (vault.getId() == vaultId) {
                return vault;
            }
        }

        return null;
    }

    public PlayerVault getVault(String name) {
        for (PlayerVault vault : this.vaults) {
            if (vault.getName().equalsIgnoreCase(name)) {
                return vault;
            }
        }

        return null;
    }

    @Override
    public void load() {

    }

    @Override
    public void save() {

    }

    @Override
    public void postLoad() {
        this.vaults = Lists.newArrayList();

        for (EnvyVaultsConfig.VaultGroups vault : this.manager.getConfig().getVaults()) {
            if (UtilPlayer.hasPermission(this.getParent().getParent(), vault.getPermission())) {
                if (vault.getVaultNumber() > this.allowedVaults) {
                    this.allowedVaults = vault.getVaultNumber();
                }
            }
        }

        for (int i = 0; i < this.allowedVaults; i++) {
            if (this.getVault(i) == null) {
                this.vaults.add(new PlayerVault(i));
            }
        }
    }
}
