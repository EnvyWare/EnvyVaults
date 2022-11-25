package com.envyful.vaults.ui;

import com.envyful.api.config.type.ConfigItem;
import com.envyful.api.forge.chat.UtilChatColour;
import com.envyful.api.forge.config.UtilConfigInterface;
import com.envyful.api.forge.config.UtilConfigItem;
import com.envyful.api.forge.player.ForgeEnvyPlayer;
import com.envyful.api.gui.factory.GuiFactory;
import com.envyful.api.gui.pane.Pane;
import com.envyful.vaults.EnvyVaults;
import com.envyful.vaults.config.EnvyVaultsGraphics;
import com.envyful.vaults.player.PlayerVault;
import com.envyful.vaults.player.VaultsAttribute;

import java.util.List;

public class VaultEditUI {

    public static void open(ForgeEnvyPlayer player, PlayerVault vault, int page) {
        EnvyVaultsGraphics.VaultSettings config = EnvyVaults.getInstance().getGraphics().getSettingsUI();
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

        List<Integer> displayPositions = config.getDisplayPositions();
        List<ConfigItem> showOptions = EnvyVaults.getInstance().getConfig().getShowOptions();

        for (int i = 0; i < displayPositions.size(); i++) {
            int pos = displayPositions.get(i);
            int posX = pos % 9;
            int posY = pos / 9;

            if (showOptions.size() <= i) {
                break;
            }

            ConfigItem configItem = showOptions.get(i);
            pane.set(posX, posY, GuiFactory.displayableBuilder(UtilConfigItem.fromConfigItem(configItem))
                    .clickHandler((envyPlayer, clickType) -> {
                        vault.setDisplay(configItem);
                        VaultsMainUI.open(player, 1);
                    })
                    .build());
        }

        int finalPage = page;

        UtilConfigItem.builder()
                .combinedClickHandler(config.getNextPageButton(), (envyPlayer, clickType) -> open(player, vault, finalPage))
                .extendedConfigItem(player, pane, config.getNextPageButton());

        UtilConfigItem.builder()
                .combinedClickHandler(config.getPreviousPageButton(), (envyPlayer, clickType) -> open(player, vault,finalPage))
                .extendedConfigItem(player, pane, config.getPreviousPageButton());

        UtilConfigItem.builder()
                .singleClick()
                .clickHandler((envyPlayer, clickType) -> VaultsMainUI.open(player, 1))
                .extendedConfigItem(player, pane, config.getBackButton());

        GuiFactory.guiBuilder()
                .addPane(pane)
                .height(config.getGuiSettings().getHeight())
                .title(UtilChatColour.colour(config.getGuiSettings().getTitle()))
                .setPlayerManager(EnvyVaults.getInstance().getPlayerManager())
                .build().open(player);
    }
}
