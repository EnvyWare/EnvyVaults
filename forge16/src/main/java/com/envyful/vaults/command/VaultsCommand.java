package com.envyful.vaults.command;

import com.envyful.api.command.annotate.Command;
import com.envyful.api.command.annotate.Permissible;
import com.envyful.api.command.annotate.SubCommands;
import com.envyful.api.command.annotate.executor.CommandProcessor;
import com.envyful.api.command.annotate.executor.Sender;
import com.envyful.api.forge.chat.UtilChatColour;
import com.envyful.vaults.EnvyVaults;
import com.envyful.vaults.ui.VaultsMainUI;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.Util;

@Command(
        value = "envyvaults",
        description = "Opens vaults",
        aliases = {
                "vaults",
                "playervaults",
                "pvaults"
        }
)
@Permissible("envy.vaults.command.vaults")
@SubCommands({OpenCommand.class, ReloadCommand.class, RenameCommand.class, RefreshPlayerCommand.class})
public class VaultsCommand {

    @CommandProcessor
    public void onCommand(@Sender ServerPlayerEntity player, String[] args) {
        VaultsMainUI.open(EnvyVaults.getInstance().getPlayerManager().getPlayer(player), 1);

        for (String s : EnvyVaults.getLocale().getDefaultCommand()) {
            player.sendMessage(UtilChatColour.colour(s), Util.NIL_UUID);
        }
    }
}
