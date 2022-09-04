package com.envyful.vaults.player;

import com.envyful.api.forge.chat.UtilChatColour;
import com.envyful.api.forge.items.ItemBuilder;
import com.envyful.api.forge.player.ForgeEnvyPlayer;
import com.envyful.vaults.config.EnvyVaultsGraphics;
import com.google.common.collect.Lists;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.play.server.SOpenWindowPacket;
import net.minecraft.util.text.StringTextComponent;

import java.util.List;

public class PlayerVault {

    private int id;
    private String name;
    private List<ItemStack> items;
    private ItemStack display;

    public PlayerVault(int id) {
        this(id, "vault#" + id);
    }

    public PlayerVault(int id, String name) {
        this(id, name, Lists.newArrayList(), new ItemBuilder().type(Items.STONE).build());
    }

    public PlayerVault(int id, String name, List<ItemStack> items, ItemStack display) {
        this.id = id;
        this.name = name;
        this.items = items;
        this.display = display;
    }

    public PlayerVault() {
    }

    public int getId() {
        return this.id;
    }

    public void open(ForgeEnvyPlayer player) {
        VaultContainer container = new VaultContainer(this.items);
        player.getParent().containerMenu = container;
        player.getParent().containerCounter = 1;
        player.getParent().connection.send(new SOpenWindowPacket(player.getParent().containerCounter, ContainerType.GENERIC_9x6, new StringTextComponent(this.name)));
    }

    public ItemStack getDisplay(EnvyVaultsGraphics.VaultDisplay display) {
        ItemBuilder builder = new ItemBuilder(this.display)
                .name(display.getName());

        for (String s : display.getLore()) {
            builder.lore(UtilChatColour.colour(s));
        }

        return builder.build();
    }

    public class VaultContainer extends Container {

        public VaultContainer(List<ItemStack> items) {
            super(ContainerType.GENERIC_9x6, 1);

            for (int i = 0; i < items.size(); i++) {
                this.slots.get(i).set(items.get(i));
            }
        }

        @Override
        public boolean stillValid(PlayerEntity p_75145_1_) {
            return true;
        }

        @Override
        public void removed(PlayerEntity playerIn) {
            super.removed(playerIn);
            this.handleClose(playerIn);
        }

        private void handleClose(PlayerEntity playerIn) {
            System.out.println(this.slots);
        }
    }
}
