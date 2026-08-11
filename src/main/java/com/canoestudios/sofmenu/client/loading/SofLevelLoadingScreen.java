package com.canoestudios.sofmenu.client.loading;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.LevelLoadingScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.server.ProgressMeter;
import net.minecraft.server.integrated.IntegratedServer;

import javax.annotation.Nullable;

/**
 * Replaces the vanilla world-loading screen (single player) with the SOF
 * artwork. The progress is read from the wrapped screen's integrated server.
 */
public class SofLevelLoadingScreen extends SofWrappedScreen {

    public SofLevelLoadingScreen(LevelLoadingScreen wrapped) {
        super(wrapped, Component.literal("SOF Level Loading"));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int progress = getProgress();
        LoadingScreenHandler.getInstance().renderScreen(graphics, this.width, this.height, progress);

        int textY = this.height / 2 - 20;
        graphics.drawCenteredString(this.font, LoadingScreenBackground.getAnimatedLoadingText(),
                this.width / 2, textY, 0xFFFFFF);
        graphics.drawCenteredString(this.font, Component.translatable("menu.loadingLevel"),
                this.width / 2, textY + 16, 0xFFFFFF);
        String progressText = progress < 0 ? "" : progress + "%";
        if (!progressText.isEmpty()) {
            graphics.drawCenteredString(this.font, progressText, this.width / 2, textY + 32, 0xFFFFFF);
        }
    }

    private int getProgress() {
        IntegratedServer server = getServer();
        if (server != null) {
            ProgressMeter progress = server.getProgress();
            if (progress != null) {
                return progress.getProgress();
            }
        }
        return -1;
    }

    @Nullable
    private IntegratedServer getServer() {
        Minecraft minecraft = Minecraft.getInstance();
        return minecraft.getSingleplayerServer();
    }
}
