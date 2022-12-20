package com.envyful.vaults.command;

import com.envyful.api.command.annotate.Child;
import com.envyful.api.command.annotate.Command;
import com.envyful.api.command.annotate.Permissible;
import com.envyful.api.command.annotate.executor.Argument;
import com.envyful.api.command.annotate.executor.CommandProcessor;
import com.envyful.api.command.annotate.executor.Sender;
import com.envyful.api.forge.player.ForgeEnvyPlayer;
import com.envyful.vaults.EnvyVaults;
import com.envyful.vaults.player.PlayerVault;
import joptsimple.internal.Strings;

@Command(
        value = "rename",
        description = "Renames the vault"
)
@Permissible("envy.vaults.command.rename")
@Child
public class RenameCommand {

    @CommandProcessor
    public void onCommand(@Sender ForgeEnvyPlayer sender, @Argument PlayerVault vault, String[] args) {
        String oldName = vault.getName();
        vault.rename(Strings.join(args, " "));

        for (String s : EnvyVaults.getLocale().getRenamedVaultMessage()) {
            sender.message(s.replace("%old_name%", oldName).replace("%name%", vault.getName()));
        }
    }
}
