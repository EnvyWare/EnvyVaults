package com.envyful.vaults.player;

import com.envyful.api.forge.chat.UtilChatColour;
import com.envyful.api.forge.concurrency.UtilForgeConcurrency;
import com.envyful.api.forge.items.ItemBuilder;
import com.envyful.api.forge.player.ForgeEnvyPlayer;
import com.envyful.vaults.config.EnvyVaultsGraphics;
import com.google.common.collect.Lists;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.play.server.SCloseWindowPacket;
import net.minecraft.network.play.server.SOpenWindowPacket;
import net.minecraft.util.text.StringTextComponent;

import java.util.List;

public class PlayerVault {

    private int id;
    private String name;
    private List<ItemStack> items;
    private ItemStack display;

    public PlayerVault(int id) {
        this(id, "vault#" + (id + 1));
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
        UtilForgeConcurrency.runSync(() -> {
            player.getParent().closeContainer();

            PlayerVault.VaultContainer container = new VaultContainer(this, player.getParent());

            UtilForgeConcurrency.runWhenTrue(__ -> player.getParent().containerMenu == player.getParent().containerMenu, () -> {
                player.getParent().containerMenu = container;
                player.getParent().containerCounter = 1;
                player.getParent().connection.send(new SOpenWindowPacket(player.getParent().containerCounter, ContainerType.GENERIC_9x6, new StringTextComponent(this.name)));
                player.getParent().refreshContainer(container, container.getItems());
                player.getParent().refreshContainer(player.getParent().inventoryMenu, player.getParent().inventoryMenu.getItems());
            });
        });
    }

    public ItemStack getDisplay(EnvyVaultsGraphics.VaultDisplay display) {
        ItemBuilder builder = new ItemBuilder(this.display)
                .name(display.getName().replace("%vault_name%", this.name));

        for (String s : display.getLore()) {
            builder.lore(UtilChatColour.colour(s.replace("%vault_name%", this.name)));
        }

        return builder.build();
    }

    public static class VaultContainer extends Container {

        private PlayerVault vault;

        public VaultContainer(PlayerVault vault, ServerPlayerEntity player) {
            super(ContainerType.GENERIC_9x6, 1);

            this.vault = vault;
            Inventory inventory = new Inventory(6 * 9);

            for (int i = 0; i < vault.items.size(); i++) {
                if (i >= 54) {
                    break;
                }

                inventory.setItem(i, vault.items.get(i));
            }

            int i = 2 * 18;

            for(int j = 0; j < 6; ++j) {
                for(int k = 0; k < 9; ++k) {
                    this.addSlot(new Slot(inventory, k + j * 9, 8 + k * 18, 18 + j * 18));
                }
            }

            for(int l = 0; l < 3; ++l) {
                for(int j1 = 0; j1 < 9; ++j1) {
                    this.addSlot(new Slot(player.inventory, j1 + l * 9 + 9, 8 + j1 * 18, 103 + l * 18 + i));
                }
            }

            for(int i1 = 0; i1 < 9; ++i1) {
                this.addSlot(new Slot(player.inventory, i1, 8 + i1 * 18, 161 + i));
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
            int windowId = playerIn.containerMenu.containerId;
            SCloseWindowPacket closeWindowServer = new SCloseWindowPacket(windowId);

            ((ServerPlayerEntity) playerIn).connection.send(closeWindowServer);
            ((ServerPlayerEntity) playerIn).containerCounter = 0;
            ((ServerPlayerEntity) playerIn).refreshContainer(playerIn.containerMenu, playerIn.containerMenu.getItems());
            vault.items = Lists.newArrayList(this.getItems());
        }
    }
}
