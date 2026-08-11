package com.canoestudios.sofmenu.client.loading;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.LevelLoadingScreen;
import net.minecraft.network.chat.Component;

/**
 * Replaces the vanilla world-loading screen (single player) with the SOF
 * artwork.
 */
public class SofLevelLoadingScreen extends SofWrappedScreen {

    public SofLevelLoadingScreen(LevelLoadingScreen wrapped) {
        super(wrapped, Component.literal("SOF Level Loading"));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        LoadingScreenHandler.getInstance().renderScreen(graphics, this.width, this.height);

        int textY = this.height / 2 - 20;
        graphics.drawCenteredString(this.font, LoadingScreenBackground.getAnimatedLoadingText(),
                this.width / 2, textY, 0xFFFFFF);
        graphics.drawCenteredString(this.font, Component.translatable("menu.loadingLevel"),
                this.width / 2, textY + 16, 0xFFFFFF);
    }
}
