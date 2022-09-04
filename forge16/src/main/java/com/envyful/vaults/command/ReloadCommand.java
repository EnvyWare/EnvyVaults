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

@Command(
        value = "reload",
        description = "reloads configs"
)
@Permissible("envy.vaults.command.reload")
@Child
public class ReloadCommand {

    @CommandProcessor
    public void onCommand(@Sender ForgeEnvyPlayer sender, String[] args) {
        EnvyVaults.getInstance().reloadConfig();
        sender.message("Reloaded config");
    }
}
