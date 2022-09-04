package com.envyful.vaults.player;

import com.envyful.api.forge.player.ForgeEnvyPlayer;
import com.envyful.api.forge.player.attribute.AbstractForgeAttribute;
import com.envyful.api.forge.player.util.UtilPlayer;
import com.envyful.api.player.EnvyPlayer;
import com.envyful.api.player.SaveMode;
import com.envyful.api.player.attribute.PlayerAttribute;
import com.envyful.api.player.save.attribute.DataDirectory;
import com.envyful.vaults.EnvyVaults;
import com.envyful.vaults.config.EnvyVaultsConfig;
import com.envyful.vaults.config.Queries;
import com.google.common.collect.Lists;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.realms.RealmsScreen;
import net.minecraftforge.common.util.Constants;

import java.net.ConnectException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@DataDirectory("config/players/EnvyVaults/")
public class VaultsAttribute extends AbstractForgeAttribute<EnvyVaults> {

    private List<PlayerVault> vaults;
    private int allowedVaults = 0;

    public VaultsAttribute(EnvyVaults manager, EnvyPlayer<?> parent) {
        super(manager, (ForgeEnvyPlayer) parent);
    }

    public VaultsAttribute(UUID uuid) {
        super(uuid);
    }

    public List<PlayerVault> getVaults() {
        return this.vaults;
    }

    public boolean canAccess(int vaultId) {
        return vaultId <= this.allowedVaults;
    }

    public PlayerVault getVault(int vaultId) {
        for (PlayerVault vault : this.vaults) {
            if (vault.getId() == vaultId) {
                return vault;
            }
        }

        return null;
    }

    public PlayerVault getVault(String name) {
        for (PlayerVault vault : this.vaults) {
            if (vault.getName().equalsIgnoreCase(name)) {
                return vault;
            }
        }

        return null;
    }

    @Override
    public void load() {
        this.initData();

        try (Connection connection = this.manager.getDatabase().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(Queries.LOAD_PLAYER_DATA)) {
            preparedStatement.setString(1, this.uuid.toString());

            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                int vaultId = resultSet.getInt("vault_id");
                String vaultName = resultSet.getString("vault_name");
                String items = resultSet.getString("items");
                String display = resultSet.getString("display");
                ListNBT itemList = JsonToNBT.parseTag(items).getList("items", Constants.NBT.TAG_COMPOUND);
                ItemStack displayItem = ItemStack.of(JsonToNBT.parseTag(display));
                List<ItemStack> guiItems = new ArrayList<>(54);

                for (INBT inbt : itemList) {
                    CompoundNBT tag = (CompoundNBT) inbt;
                    int slot = tag.getInt("slot");
                    ItemStack item = ItemStack.of((CompoundNBT) tag.get("item"));

                    guiItems.set(slot, item);
                }

                PlayerVault vault = this.getVault(vaultId);

                if (vault == null) {
                    continue;
                }

                vault.load(vaultName, displayItem, guiItems);
            }
        } catch (SQLException | CommandSyntaxException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void save() {
        try (Connection connection = this.manager.getDatabase().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(Queries.UPDATE_DATA)) {
            for (PlayerVault vault : this.vaults) {
                preparedStatement.setString(1, this.uuid.toString());
                vault.save(preparedStatement);
                preparedStatement.addBatch();
            }

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void postLoad() {
        if (this.manager.getConfig().getSaveMode() != SaveMode.JSON) {
            return;
        }

        this.initData();
    }

    private void initData() {
        this.vaults = Lists.newArrayList();

        for (EnvyVaultsConfig.VaultGroups vault : this.manager.getConfig().getVaults()) {
            if (UtilPlayer.hasPermission(this.getParent().getParent(), vault.getPermission())) {
                if (vault.getVaultNumber() > this.allowedVaults) {
                    this.allowedVaults = vault.getVaultNumber();
                }
            }
        }

        for (int i = 0; i < this.allowedVaults; i++) {
            if (this.getVault(i) == null) {
                this.vaults.add(new PlayerVault(i));
            }
        }
    }
}
