package com.envyful.vaults.command;

import com.envyful.api.command.sender.SenderType;
import com.envyful.api.forge.player.ForgeEnvyPlayer;
import com.envyful.vaults.EnvyVaults;
import net.minecraft.command.ICommandSource;
import net.minecraft.entity.player.ServerPlayerEntity;

public class ForgeEnvyPlayerSenderType implements SenderType<ICommandSource, ForgeEnvyPlayer> {
    @Override
    public Class<?> getType() {
        return ForgeEnvyPlayer.class;
    }

    @Override
    public boolean isAccepted(ICommandSource sender) {
        return sender instanceof ServerPlayerEntity;
    }

    @Override
    public ForgeEnvyPlayer getInstance(ICommandSource sender) {
        return EnvyVaults.getInstance().getPlayerManager().getPlayer((ServerPlayerEntity) sender);
    }
}
