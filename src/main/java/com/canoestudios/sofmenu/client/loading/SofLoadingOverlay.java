package com.canoestudios.sofmenu.client.loading;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Overlay;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.resources.ReloadInstance;

import javax.annotation.Nullable;
import java.util.function.Consumer;

/**
 * Replacement for the vanilla {@link net.minecraft.client.gui.screens.LoadingOverlay}
 * that renders the SOF loading artwork instead of the Mojang logo.
 *
 * <p>The vanilla overlay is swapped for this one by
 * {@link com.canoestudios.sofmenu.client.ClientEventHandler} on the client
 * tick. Completion is detected through the shared {@link ReloadInstance} so the
 * overlay still fades out and is dismissed when the resource reload finishes.</p>
 */
public class SofLoadingOverlay extends Overlay {

    private static final long FADE_OUT_MILLIS = 1500L;

    private final Minecraft minecraft;
    @Nullable
    private final ReloadInstance reload;
    @Nullable
    private final Consumer<Void> onFinish;
    private long fadeOutStart = -1L;

    public SofLoadingOverlay(Minecraft minecraft, @Nullable ReloadInstance reload,
            @Nullable Consumer<Void> onFinish) {
        this.minecraft = minecraft;
        this.reload = reload;
        this.onFinish = onFinish;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        long now = Util.getMillis();

        if (this.reload != null && this.reload.isDone() && this.fadeOutStart == -1L) {
            this.fadeOutStart = now;
        }
        if (this.fadeOutStart != -1L) {
            float fade = (float) (now - this.fadeOutStart) / (float) FADE_OUT_MILLIS;
            if (fade >= 1.0F) {
                if (this.onFinish != null) {
                    this.onFinish.accept(null);
                }
                this.minecraft.setOverlay(null);
                return;
            }
        }

        int width = graphics.guiWidth();
        int height = graphics.guiHeight();
        LoadingScreenBackground.render(graphics, width, height, -1);

        graphics.drawCenteredString(this.minecraft.font, LoadingScreenBackground.getAnimatedLoadingText(),
                width / 2, height / 2 - 12, 0xFFFFFF);
        graphics.drawCenteredString(this.minecraft.font, Component.translatable("sofmenu.loading"),
                width / 2, height / 2 + 4, 0xFFFFFF);
    }

    @Override
    public boolean isPauseScreen() {
        return true;
    }
}
