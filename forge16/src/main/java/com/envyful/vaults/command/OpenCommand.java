package com.envyful.vaults.command;

import com.envyful.api.command.annotate.Child;
import com.envyful.api.command.annotate.Command;
import com.envyful.api.command.annotate.Permissible;
import com.envyful.api.command.annotate.SubCommands;
import com.envyful.api.command.annotate.executor.Argument;
import com.envyful.api.command.annotate.executor.CommandProcessor;
import com.envyful.api.command.annotate.executor.Completable;
import com.envyful.api.command.annotate.executor.Sender;
import com.envyful.api.forge.command.completion.number.IntCompletionData;
import com.envyful.api.forge.command.completion.number.IntegerTabCompleter;
import com.envyful.api.forge.player.ForgeEnvyPlayer;
import com.envyful.vaults.EnvyVaults;
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
    public void onCommand(@Sender ForgeEnvyPlayer sender,
                          @Completable(IntegerTabCompleter.class) @IntCompletionData(min = 1) @Argument PlayerVault vault) {
        vault.open(sender);

        for (String s : EnvyVaults.getLocale().getOpenMessage()) {
            sender.message(s.replace("%vault%", vault.getName()));
        }
    }
}
