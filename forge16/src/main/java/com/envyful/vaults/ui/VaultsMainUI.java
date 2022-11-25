package com.envyful.vaults.ui;

import com.envyful.api.forge.chat.UtilChatColour;
import com.envyful.api.forge.config.UtilConfigInterface;
import com.envyful.api.forge.config.UtilConfigItem;
import com.envyful.api.forge.player.ForgeEnvyPlayer;
import com.envyful.api.gui.factory.GuiFactory;
import com.envyful.api.gui.item.Displayable;
import com.envyful.api.gui.pane.Pane;
import com.envyful.vaults.EnvyVaults;
import com.envyful.vaults.config.EnvyVaultsGraphics;
import com.envyful.vaults.player.PlayerVault;
import com.envyful.vaults.player.VaultsAttribute;
import net.minecraft.item.ItemStack;

public class VaultsMainUI {

    public static void open(ForgeEnvyPlayer player, int page) {
        EnvyVaultsGraphics.MainUI config = EnvyVaults.getInstance().getGraphics().getMainUI();
        VaultsAttribute attribute = player.getAttribute(EnvyVaults.class);

        if (page > config.getMaxPage()) {
            page = 1;
        } else if (page <= 0) {
            page = config.getMaxPage();
        }

        Pane pane = GuiFactory.paneBuilder()
                .topLeftY(0)
                .topLeftX(0)
                .height(config.getGuiSettings().getHeight())
                .width(9)
                .build();

        UtilConfigInterface.fillBackground(pane, config.getGuiSettings());

        for (int i = 0; i < config.getVaultPositions().size(); i++) {
            int position = config.getVaultPositions().get(i);
            int posX = position % 9;
            int posY = position / 9;
            int vaultId = ((page - 1) * config.getVaultPositions().size()) + i;

            if (!attribute.canAccess(vaultId)) {
                pane.set(posX, posY, GuiFactory.displayable(UtilConfigItem.fromConfigItem(config.getCannotAccessThisVault())));
                continue;
            }

            PlayerVault playerVault = attribute.getVault(vaultId);

            if (playerVault == null) {
                pane.set(posX, posY, GuiFactory.displayable(UtilConfigItem.fromConfigItem(config.getCannotAccessThisVault())));
                continue;
            }

            ItemStack display = playerVault.getDisplay(config.getDisplay());
            pane.set(posX, posY, GuiFactory.displayableBuilder(display)
                    .clickHandler((envyPlayer, clickType) -> {
                        if (clickType == Displayable.ClickType.LEFT) {
                            playerVault.open(player);
                        } else {
                            VaultEditUI.open(player, playerVault, 1);
                        }
                    })
                    .build());
        }

        int finalPage = page;

        UtilConfigItem.builder()
                .combinedClickHandler(config.getNextPageButton(), (envyPlayer, clickType) -> open(player, finalPage))
                .extendedConfigItem(player, pane, config.getNextPageButton());

        UtilConfigItem.builder()
                .combinedClickHandler(config.getPreviousPageButton(), (envyPlayer, clickType) -> open(player, finalPage))
                .extendedConfigItem(player, pane, config.getPreviousPageButton());

        GuiFactory.guiBuilder()
                .addPane(pane)
                .height(config.getGuiSettings().getHeight())
                .title(UtilChatColour.colour(config.getGuiSettings().getTitle()))
                .setPlayerManager(EnvyVaults.getInstance().getPlayerManager())
                .build().open(player);
    }

}
