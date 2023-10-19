package com.envyful.vaults.command;

import com.envyful.api.command.annotate.Command;
import com.envyful.api.command.annotate.SubCommands;
import com.envyful.api.command.annotate.executor.CommandProcessor;
import com.envyful.api.command.annotate.executor.Sender;
import com.envyful.api.command.annotate.permission.Permissible;
import com.envyful.api.forge.chat.UtilChatColour;
import com.envyful.vaults.EnvyVaults;
import com.envyful.vaults.ui.VaultsMainUI;
import net.minecraft.server.level.ServerPlayer;

@Command(
        value = {
                "envyvaults",
                "vaults",
                "playervaults",
                "pvaults"
        }
)
@Permissible("envy.vaults.command.vaults")
@SubCommands({OpenCommand.class, ReloadCommand.class, RenameCommand.class, RefreshPlayerCommand.class, AdminViewCommand.class})
public class VaultsCommand {

    @CommandProcessor
    public void onCommand(@Sender ServerPlayer player, String[] args) {
        VaultsMainUI.open(EnvyVaults.getInstance().getPlayerManager().getPlayer(player), 1);

        for (String s : EnvyVaults.getLocale().getDefaultCommand()) {
            player.sendSystemMessage(UtilChatColour.colour(s));
        }
    }
}
