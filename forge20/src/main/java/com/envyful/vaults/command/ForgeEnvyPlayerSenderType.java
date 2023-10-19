package com.envyful.vaults.command;

import com.envyful.api.command.sender.SenderType;
import com.envyful.api.forge.player.ForgeEnvyPlayer;
import com.envyful.vaults.EnvyVaults;
import net.minecraft.commands.CommandSource;
import net.minecraft.server.level.ServerPlayer;

public class ForgeEnvyPlayerSenderType implements SenderType<CommandSource, ForgeEnvyPlayer> {
    @Override
    public Class<?> getType() {
        return ForgeEnvyPlayer.class;
    }

    @Override
    public boolean isAccepted(CommandSource sender) {
        return sender instanceof ServerPlayer;
    }

    @Override
    public ForgeEnvyPlayer getInstance(CommandSource sender) {
        return EnvyVaults.getInstance().getPlayerManager().getPlayer((ServerPlayer) sender);
    }
}
