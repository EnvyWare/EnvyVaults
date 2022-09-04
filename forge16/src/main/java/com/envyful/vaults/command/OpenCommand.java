package com.envyful.vaults.command;

import com.envyful.api.command.annotate.Child;
import com.envyful.api.command.annotate.Command;
import com.envyful.api.command.annotate.Permissible;
import com.envyful.api.command.annotate.SubCommands;
import com.envyful.api.command.annotate.executor.Argument;
import com.envyful.api.command.annotate.executor.CommandProcessor;
import com.envyful.api.command.annotate.executor.Sender;
import com.envyful.api.forge.player.ForgeEnvyPlayer;
import com.envyful.vaults.player.PlayerVault;

@Command(
        value = "open",
        description = "Opens the vault",
        aliases = {
                "o"
        }
)
@Permissible("envy.vaults.command.open")
@Child
public class OpenCommand {

    @CommandProcessor
    public void onCommand(@Sender ForgeEnvyPlayer sender, @Argument PlayerVault vault) {
        vault.open(sender);
    }
}
