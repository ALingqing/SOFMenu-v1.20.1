package com.canoestudios.sofmenu.client.loading;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ReceivingLevelScreen;
import net.minecraft.network.chat.Component;

/**
 * Replaces the vanilla "Downloading terrain" screen with the SOF artwork.
 */
public class SofReceivingLevelScreen extends SofWrappedScreen {

    public SofReceivingLevelScreen(ReceivingLevelScreen wrapped) {
        super(wrapped, Component.literal("SOF Receiving Level"));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        LoadingScreenHandler.getInstance().renderScreen(graphics, this.width, this.height);

        graphics.drawCenteredString(this.font, LoadingScreenBackground.getAnimatedLoadingText(),
                this.width / 2, this.height / 2 - 12, 0xFFFFFF);
        graphics.drawCenteredString(this.font, Component.translatable("multiplayer.downloadingTerrain"),
                this.width / 2, this.height / 2 + 4, 0xFFFFFF);
    }
}
