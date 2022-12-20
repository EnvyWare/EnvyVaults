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
import com.google.common.collect.Lists;
import com.google.gson.JsonObject;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.play.server.SCloseWindowPacket;
import net.minecraft.network.play.server.SOpenWindowPacket;
import net.minecraft.util.text.StringTextComponent;

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

            PlayerVault.VaultContainer container = new VaultContainer(this, player.getParent());

            UtilForgeConcurrency.runWhenTrue(__ -> player.getParent().containerMenu == player.getParent().containerMenu, () -> {
                player.getParent().containerMenu = container;
                player.getParent().containerCounter = 1;
                player.getParent().connection.send(new SOpenWindowPacket(player.getParent().containerCounter, this.getContainerType(), new StringTextComponent(this.name)));
                player.getParent().refreshContainer(container, container.getItems());
                player.getParent().refreshContainer(player.getParent().inventoryMenu, player.getParent().inventoryMenu.getItems());
            });
        });
    }

    private ContainerType<?> getContainerType() {
        switch(EnvyVaults.getConfig().getVaultHeight()) {
            case 6: default: return ContainerType.GENERIC_9x6;
            case 5: return ContainerType.GENERIC_9x5;
            case 4: return ContainerType.GENERIC_9x4;
            case 3: return ContainerType.GENERIC_9x3;
            case 2: return ContainerType.GENERIC_9x2;
            case 1: return ContainerType.GENERIC_9x1;
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
        preparedStatement.setString(4, this.display.save(new CompoundNBT()).toString());
        preparedStatement.setString(5, this.getItemSave().toString());
    }

    private CompoundNBT getItemSave() {
        CompoundNBT itemsTag = new CompoundNBT();
        ListNBT list = new ListNBT();

        for (int i = 0; i < this.items.size(); i++) {
            CompoundNBT itemTag = new CompoundNBT();
            ItemStack itemStack = this.items.get(i);

            if (itemStack == null) {
                continue;
            }

            itemTag.putInt("slot", i);
            itemTag.put("item", itemStack.save(new CompoundNBT()));
            list.add(itemTag);
        }

        itemsTag.put("items", list);
        return itemsTag;
    }

    public void write(JsonObject object) {
        object.addProperty("id", this.id);
        object.addProperty("name", this.name);
        object.addProperty("display", this.display.save(new CompoundNBT()).toString());
        object.addProperty("items", this.getItemSave().toString());
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
            List<ItemStack> items = Lists.newArrayList();

            for (Slot slot : this.slots) {
                if (slot.container instanceof Inventory) {
                    items.add(slot.getItem());
                }
            }

            this.vault.items = items;

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
        public ItemStack quickMoveStack(PlayerEntity p_82846_1_, int p_82846_2_) {
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
