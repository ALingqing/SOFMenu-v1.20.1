package com.canoestudios.sofmenu.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.MathHelper;

/**
 * A button rendered entirely from a normal + hover texture, with a delayed
 * fade-in appearance used by the SOF main menu.
 */
public class TexturedMenuButton extends Button {

    private final ResourceLocation normalTexture;
    private final ResourceLocation hoverTexture;
    private final long screenOpenedAt;
    private final float delaySeconds;
    private boolean wasHovered;
    private boolean sofHovered;
    private Component hoverLabel;

    public TexturedMenuButton(int x, int y, int width, int height, Component message,
            ResourceLocation normalTexture, ResourceLocation hoverTexture, long screenOpenedAt,
            float delaySeconds, OnPress onPress) {
        super(x, y, width, height, message, onPress, DEFAULT_NARRATION);
        this.normalTexture = normalTexture;
        this.hoverTexture = hoverTexture;
        this.screenOpenedAt = screenOpenedAt;
        this.delaySeconds = delaySeconds;
    }

    public TexturedMenuButton setHoverLabel(Component hoverLabel) {
        this.hoverLabel = hoverLabel;
        return this;
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        float alpha = getAppearanceAlpha();
        this.active = alpha >= 1.0F;
        this.sofHovered = mouseX >= this.getX() && mouseY >= this.getY()
                && mouseX < this.getX() + this.width && mouseY < this.getY() + this.height;

        if (this.sofHovered && !this.wasHovered && alpha >= 1.0F) {
            Minecraft.getInstance().getSoundManager()
                    .play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        }
        this.wasHovered = this.sofHovered;

        graphics.setColor(1.0F, 1.0F, 1.0F, alpha);
        graphics.blit(this.sofHovered ? this.hoverTexture : this.normalTexture,
                this.getX(), this.getY(), 0, 0, this.width, this.height, this.width, this.height);
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);

        Component label = this.sofHovered && this.hoverLabel != null ? this.hoverLabel : this.getMessage();
        if (label != null && !label.getString().isEmpty()) {
            int color = this.sofHovered ? 0xFFFFA0 : 0xF2F2F2;
            int alphaBits = MathHelper.clamp((int) (alpha * 255.0F), 0, 255) << 24;
            graphics.drawCenteredString(Minecraft.getInstance().font, label, this.getX() + this.width / 2,
                    this.getY() + (this.height - 8) / 2, alphaBits | color);
        }
    }

    private float getAppearanceAlpha() {
        if (this.delaySeconds <= 0.0F) {
            return 1.0F;
        }
        float seconds = (System.currentTimeMillis() - this.screenOpenedAt) / 1000.0F;
        return MathHelper.clamp((seconds - this.delaySeconds) * 1.0F, 0.0F, 1.0F);
    }
}
