package com.envyful.vaults.command;

import com.envyful.api.command.annotate.Child;
import com.envyful.api.command.annotate.Command;
import com.envyful.api.command.annotate.Permissible;
import com.envyful.api.command.annotate.executor.Argument;
import com.envyful.api.command.annotate.executor.CommandProcessor;
import com.envyful.api.command.annotate.executor.Sender;
import com.envyful.api.forge.player.ForgeEnvyPlayer;
import com.envyful.vaults.EnvyVaults;
import com.envyful.vaults.player.VaultsAttribute;
import net.minecraft.command.ICommandSource;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.Util;
import net.minecraft.util.text.StringTextComponent;

import java.lang.annotation.Target;

@Command(
        value = "refresh",
        description = "Re-checks a player's vault limit"
)
@Permissible("envy.vaults.command.refresh")
@Child
public class RefreshPlayerCommand {

    @CommandProcessor
    public void onCommand(@Sender ICommandSource sender, @Argument ServerPlayerEntity player) {
        ForgeEnvyPlayer target = EnvyVaults.getInstance().getPlayerManager().getPlayer(player);
        VaultsAttribute attribute = target.getAttribute(EnvyVaults.class);
        attribute.checkAllowedVaults();
        sender.sendMessage(new StringTextComponent("Checked player: " + target.getName()), Util.NIL_UUID);
    }
}
