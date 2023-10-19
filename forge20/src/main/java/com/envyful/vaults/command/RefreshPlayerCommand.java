package com.envyful.vaults.command;

import com.envyful.api.command.annotate.Command;
import com.envyful.api.command.annotate.executor.Argument;
import com.envyful.api.command.annotate.executor.CommandProcessor;
import com.envyful.api.command.annotate.executor.Sender;
import com.envyful.api.command.annotate.permission.Permissible;
import com.envyful.api.forge.chat.UtilChatColour;
import com.envyful.vaults.EnvyVaults;
import com.envyful.vaults.player.VaultsAttribute;
import net.minecraft.commands.CommandSource;
import net.minecraft.server.level.ServerPlayer;

@Command(
        value = "refresh"
)
@Permissible("envy.vaults.command.refresh")
public class RefreshPlayerCommand {

    @CommandProcessor
    public void onCommand(@Sender CommandSource sender, @Argument ServerPlayer player) {
        var target = EnvyVaults.getInstance().getPlayerManager().getPlayer(player);
        var attribute = target.getAttribute(VaultsAttribute.class);
        attribute.checkAllowedVaults();
        sender.sendSystemMessage(UtilChatColour.colour("Checked player: " + target.getName()));
    }
}
