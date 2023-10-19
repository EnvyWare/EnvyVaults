package com.envyful.vaults.player;

import com.envyful.api.forge.player.ForgeEnvyPlayer;
import com.envyful.api.forge.player.ForgePlayerManager;
import com.envyful.api.forge.player.attribute.ManagedForgeAttribute;
import com.envyful.api.forge.player.util.UtilPlayer;
import com.envyful.api.player.save.attribute.DataDirectory;
import com.envyful.api.player.save.attribute.TypeAdapter;
import com.envyful.vaults.EnvyVaults;
import com.envyful.vaults.config.EnvyVaultsConfig;
import com.envyful.vaults.config.Queries;
import com.google.common.collect.Lists;
import com.google.gson.*;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.world.item.ItemStack;

import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@DataDirectory("config/players/EnvyVaults/")
@TypeAdapter(VaultsAttribute.TypeAdapter.class)
public class VaultsAttribute extends ManagedForgeAttribute<EnvyVaults> {

    private List<PlayerVault> vaults = Lists.newArrayList();
    private int allowedVaults = 0;

    public VaultsAttribute(ForgePlayerManager playerManager) {
        super(EnvyVaults.getInstance(), playerManager);
    }

    public VaultsAttribute() {
        super(EnvyVaults.getInstance(), EnvyVaults.getInstance().getPlayerManager());
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
    public void setParent(ForgeEnvyPlayer parent) {
        super.setParent(parent);

        this.initData();
    }

    @Override
    public void load() {
        this.initData();

        try (Connection connection = this.manager.getDatabase().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(Queries.LOAD_PLAYER_DATA)) {
            preparedStatement.setString(1, this.parent.getUuid().toString());

            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                int vaultId = resultSet.getInt("vault_id");
                String vaultName = resultSet.getString("vault_name");
                String items = resultSet.getString("items");
                String display = resultSet.getString("display");
                ListTag itemList = TagParser.parseTag(items).getList("items", Tag.TAG_COMPOUND);
                ItemStack displayItem = ItemStack.of(TagParser.parseTag(display));
                List<ItemStack> guiItems = Lists.newArrayList();

                for (Tag inbt : itemList) {
                    var tag = (CompoundTag) inbt;
                    int slot = tag.getInt("slot");
                    ItemStack item = ItemStack.of(tag.getCompound("item"));
                    guiItems.add(ItemStack.EMPTY);
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
            boolean hasData = false;

            for (PlayerVault vault : this.vaults) {
                preparedStatement.setString(1, this.parent.getUuid().toString());
                vault.save(preparedStatement);
                hasData = true;
                preparedStatement.addBatch();
            }

            if (hasData) {
                preparedStatement.executeBatch();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void initData() {
        if (this.vaults == null) {
            this.vaults = Lists.newArrayList();
        }

        this.checkAllowedVaults();

        for (int i = 0; i < this.allowedVaults; i++) {
            if (this.getVault(i) == null) {
                this.vaults.add(new PlayerVault(i));
            }
        }
    }

    public void checkAllowedVaults() {
        for (EnvyVaultsConfig.VaultGroups vault : this.manager.getConfig().getVaults()) {
            if (UtilPlayer.hasPermission(this.parent.getParent(), vault.getPermission())) {
                if (vault.getVaultNumber() > this.allowedVaults) {
                    this.allowedVaults = vault.getVaultNumber();
                }
            }
        }

        for (int i = 99; i > 0; i--) {
            if (UtilPlayer.hasPermission(this.parent.getParent(), "vault.extra." + i)) {
                this.allowedVaults += i;
                break;
            }
        }
    }

    public static class TypeAdapter implements JsonDeserializer<VaultsAttribute>, JsonSerializer<VaultsAttribute> {
        @Override
        public VaultsAttribute deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject asJsonObject = json.getAsJsonObject();
            UUID uuid = UUID.fromString(asJsonObject.get("uuid").getAsString());
            VaultsAttribute attribute = new VaultsAttribute(EnvyVaults.getInstance().getPlayerManager());

//            attribute.setParent(EnvyVaults.getInstance().getPlayerManager().getPlayer(uuid));

            JsonArray vaults = asJsonObject.get("vaults").getAsJsonArray();
            List<PlayerVault> loadedVaults = new ArrayList<>(54);

            for (JsonElement vault : vaults) {
                try {
                    JsonObject vaultObject = vault.getAsJsonObject();
                    int vaultId = vaultObject.get("id").getAsInt();
                    String name = vaultObject.get("name").getAsString();
                    ItemStack display = ItemStack.of(TagParser.parseTag(vaultObject.get("display").getAsString()));
                    List<ItemStack> guiItems = Lists.newArrayList();
                    CompoundTag loadedNBT = TagParser.parseTag(vaultObject.get("items").getAsString());

                    for (var itemElement : loadedNBT.getList("items", Tag.TAG_COMPOUND)) {
                        var itemObject = (CompoundTag) itemElement;
                        int slot = itemObject.getInt("slot");
                        ItemStack item = ItemStack.of(itemObject.getCompound("item"));
                        guiItems.add(slot, item);
                    }

                    PlayerVault loadedVault = new PlayerVault(vaultId, name, guiItems, display);
                    loadedVaults.add(loadedVault);
                } catch (CommandSyntaxException e) {
                    EnvyVaults.getInstance().getLogger().catching(e);
                }
            }

            attribute.vaults = loadedVaults;
            return attribute;
        }

        @Override
        public JsonElement serialize(VaultsAttribute src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject object = new JsonObject();
            JsonArray vaults = new JsonArray();

            for (PlayerVault vault : src.vaults) {
                JsonObject vaultJson = new JsonObject();
                vault.write(vaultJson);
                vaults.add(vaultJson);
            }

            object.addProperty("uuid", src.id.toString());
            object.add("vaults", vaults);

            return object;
        }
    }
}
