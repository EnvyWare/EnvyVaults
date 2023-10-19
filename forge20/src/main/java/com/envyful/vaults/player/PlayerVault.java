package com.envyful.vaults.player;

import com.envyful.api.concurrency.UtilConcurrency;
import com.envyful.api.config.type.ConfigItem;
import com.envyful.api.forge.chat.UtilChatColour;
import com.envyful.api.forge.concurrency.UtilForgeConcurrency;
import com.envyful.api.forge.config.UtilConfigItem;
import com.envyful.api.forge.items.ItemBuilder;
import com.envyful.api.forge.player.ForgeEnvyPlayer;
import com.envyful.vaults.EnvyVaults;
import com.envyful.vaults.config.EnvyVaultsGraphics;
import com.envyful.vaults.config.Queries;
import com.envyful.vaults.ui.VaultsMainUI;
import com.google.common.collect.Lists;
import com.google.gson.JsonObject;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundContainerClosePacket;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class PlayerVault {

    private int id;
    private String name;
    private List<ItemStack> items;
    private ItemStack display;
    private boolean admin = false;
    private UUID owner = null;

    public PlayerVault(int id) {
        this(id, EnvyVaults.getConfig().getDefaultVaultName().replace("%id%", String.valueOf(id + 1)));
    }

    public PlayerVault(int id, String name) {
        this(id, name, Lists.newArrayList(), UtilConfigItem.fromConfigItem(EnvyVaults.getConfig().getDefaultDisplayItem()));
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

    public String getName() {
        return this.name;
    }

    public void open(ForgeEnvyPlayer player) {
        UtilForgeConcurrency.runSync(() -> {
            player.getParent().closeContainer();
            MenuType<?> containerType = this.getContainerType();

            PlayerVault.VaultContainer container = new VaultContainer(this, containerType, EnvyVaults.getConfig().getVaultHeight(), player.getParent());

            UtilForgeConcurrency.runWhenTrue(__ -> player.getParent().containerMenu == player.getParent().containerMenu, () -> {
                player.getParent().containerMenu = container;
                player.getParent().containerCounter = 1;
                player.getParent().connection.send(new ClientboundOpenScreenPacket(player.getParent().containerCounter, containerType, Component.literal(this.name)));
                player.getParent().containerMenu.broadcastChanges();
            });
        });
    }

    private MenuType<?> getContainerType() {
        switch(EnvyVaults.getConfig().getVaultHeight()) {
            case 6: default: return MenuType.GENERIC_9x6;
            case 5: return MenuType.GENERIC_9x5;
            case 4: return MenuType.GENERIC_9x4;
            case 3: return MenuType.GENERIC_9x3;
            case 2: return MenuType.GENERIC_9x2;
            case 1: return MenuType.GENERIC_9x1;
        }

    }

    public void setAdmin(UUID owner) {
        this.admin = true;
        this.owner = owner;
    }

    public ItemStack getDisplay(EnvyVaultsGraphics.VaultDisplay display) {
        ItemBuilder builder = new ItemBuilder(this.display)
                .name(display.getName().replace("%vault_name%", this.name));

        for (String s : display.getLore()) {
            builder.lore(UtilChatColour.colour(s.replace("%vault_name%", this.name)));
        }

        return builder.build();
    }

    public void setDisplay(ConfigItem item) {
        this.display = UtilConfigItem.fromConfigItem(item);
    }

    public void rename(String name) {
        this.name = name;
    }

    public void load(String name, ItemStack display, List<ItemStack> items) {
        this.name = name;
        this.display = display;
        this.items = items;
    }

    public void save(PreparedStatement preparedStatement) throws SQLException {
        preparedStatement.setInt(2, this.id);
        preparedStatement.setString(3, this.name);
        preparedStatement.setString(4, this.getItemSave().toString());
        preparedStatement.setString(5, this.display.save(new CompoundTag()).toString());
    }

    private CompoundTag getItemSave() {
        var itemsTag = new CompoundTag();
        var list = new ListTag();

        for (int i = 0; i < this.items.size(); i++) {
            var itemTag = new CompoundTag();
            ItemStack itemStack = this.items.get(i);

            if (itemStack == null) {
                continue;
            }

            itemTag.putInt("slot", i);
            itemTag.put("item", itemStack.save(new CompoundTag()));
            list.add(itemTag);
        }

        itemsTag.put("items", list);
        return itemsTag;
    }

    public void write(JsonObject object) {
        object.addProperty("id", this.id);
        object.addProperty("name", this.name);
        object.addProperty("display", this.display.save(new CompoundTag()).toString());
        object.addProperty("items", this.getItemSave().toString());
    }

    public static class VaultContainer extends AbstractContainerMenu {

        private PlayerVault vault;

        public VaultContainer(PlayerVault vault, MenuType<?> containerType, int height, ServerPlayer player) {
            super(containerType, 1);

            this.vault = vault;
            Container inventory = new SimpleContainer(6 * 9);

            for (int i = 0; i < vault.items.size(); i++) {
                if (i >= 54) {
                    break;
                }

                inventory.setItem(i, vault.items.get(i));
            }

            int i = 2 * 18;

            for(int j = 0; j < height; ++j) {
                for(int k = 0; k < 9; ++k) {
                    this.addSlot(new Slot(inventory, k + j * 9, 8 + k * 18, 18 + j * 18));
                }
            }

            for(int l = 0; l < 3; ++l) {
                for(int j1 = 0; j1 < 9; ++j1) {
                    this.addSlot(new Slot(player.getInventory(), j1 + l * 9 + 9, 8 + j1 * 18, 103 + l * 18 + i));
                }
            }

            for(int i1 = 0; i1 < 9; ++i1) {
                this.addSlot(new Slot(player.getInventory(), i1, 8 + i1 * 18, 161 + i));
            }
        }

        @Override
        public boolean stillValid(Player p_75145_1_) {
            return true;
        }

        @Override
        public void removed(Player playerIn) {
            super.removed(playerIn);
            this.handleClose(playerIn);
        }

        private void handleClose(Player playerIn) {
            int windowId = playerIn.containerMenu.containerId;
            ClientboundContainerClosePacket closeWindowServer = new ClientboundContainerClosePacket(windowId);

            ((ServerPlayer) playerIn).connection.send(closeWindowServer);
            ((ServerPlayer) playerIn).containerCounter = 0;
            playerIn.containerMenu.broadcastChanges();
            playerIn.containerMenu = playerIn.inventoryMenu;

            List<ItemStack> items = Lists.newArrayList();

            for (Slot slot : this.slots) {
                if (slot.container instanceof Inventory) {
                    items.add(slot.getItem());
                }
            }

            this.vault.items = items;

            VaultsMainUI.open(EnvyVaults.getInstance().getPlayerManager().getPlayer((ServerPlayer) playerIn), 1);

            if (this.vault.admin) {
                UtilConcurrency.runAsync(() -> {
                    try (Connection connection = EnvyVaults.getInstance().getDatabase().getConnection();
                         PreparedStatement preparedStatement = connection.prepareStatement(Queries.UPDATE_DATA)) {
                        preparedStatement.setString(1, this.vault.owner.toString());
                        vault.save(preparedStatement);
                        preparedStatement.executeUpdate();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                });
            }
        }

        @Override
        public ItemStack quickMoveStack(Player p_82846_1_, int p_82846_2_) {
            ItemStack itemstack = ItemStack.EMPTY;
            Slot slot = this.slots.get(p_82846_2_);
            if (slot != null && slot.hasItem()) {
                ItemStack itemstack1 = slot.getItem();
                itemstack = itemstack1.copy();
                if (p_82846_2_ < 6 * 9) {
                    if (!this.moveItemStackTo(itemstack1, 6 * 9, this.slots.size(), true)) {
                        return ItemStack.EMPTY;
                    }
                } else if (!this.moveItemStackTo(itemstack1, 0, 6 * 9, false)) {
                    return ItemStack.EMPTY;
                }

                if (itemstack1.isEmpty()) {
                    slot.set(ItemStack.EMPTY);
                } else {
                    slot.setChanged();
                }
            }

            return itemstack;
        }
    }
}
