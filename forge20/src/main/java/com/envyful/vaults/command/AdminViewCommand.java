package com.envyful.vaults.command;

import com.envyful.api.command.annotate.Command;
import com.envyful.api.command.annotate.executor.Argument;
import com.envyful.api.command.annotate.executor.CommandProcessor;
import com.envyful.api.command.annotate.executor.Sender;
import com.envyful.api.command.annotate.permission.Permissible;
import com.envyful.api.forge.player.ForgeEnvyPlayer;
import com.envyful.api.forge.player.UsernameFactory;
import com.envyful.vaults.EnvyVaults;
import com.envyful.vaults.player.PlayerVault;
import com.envyful.vaults.player.VaultsAttribute;

import java.util.UUID;

@Command(
        value = {
                "admin",
                "a"
        }
)
@Permissible("envy.vaults.command.admin")
public class AdminViewCommand {

    @CommandProcessor
    public void onCommand(@Sender ForgeEnvyPlayer sender,
                          @Argument String nameOrUUID,
                          @Argument int id) {
        VaultsAttribute playerAttribute = this.getPlayerAttribute(nameOrUUID);

        if (playerAttribute == null) {
            sender.message(EnvyVaults.getLocale().getCannotFindPlayer());
            return;
        }

        PlayerVault vault = playerAttribute.getVault(id - 1);

        if (vault == null) {
            sender.message(EnvyVaults.getLocale().getPlayerDoesntHaveVaultWithid());
            return;
        }

        vault.setAdmin(getUUID(nameOrUUID));
        vault.open(sender);
    }

    private VaultsAttribute getPlayerAttribute(String name) {
        ForgeEnvyPlayer player = EnvyVaults.getInstance().getPlayerManager().getOnlinePlayer(name);

        if (player != null) {
            return player.getAttribute(VaultsAttribute.class);
        }

        UUID uuid = this.getUUID(name);

        if (uuid == null) {
            return null;
        }

        var offlineAttributes = EnvyVaults.getInstance().getPlayerManager().getOfflineAttributes(uuid);

        if (offlineAttributes == null || offlineAttributes.isEmpty()) {
            return null;
        }

        return (VaultsAttribute) offlineAttributes.get(0);
    }

    private UUID getUUID(String nameOrUUID) {
        UUID uuid = this.attemptParseUuid(nameOrUUID);

        if (uuid != null) {
            return uuid;
        }

        return UsernameFactory.getUUID(nameOrUUID);
    }

    private UUID attemptParseUuid(String uuid) {
        try {
            return UUID.fromString(uuid);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
