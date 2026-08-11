package com.canoestudios.sofmenu.client.gui;

import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;

import com.canoestudios.sofmenu.util.MathHelper;

import java.util.List;

/**
 * SOF credits screen: a glass panel with the active / former developer lists.
 */
public class SofCreditsScreen extends Screen {

    private static final ResourceLocation[] BACKGROUNDS = new ResourceLocation[] {
            SofMainMenuScreen.texture("menu/backgrounds/image_01.png"),
            SofMainMenuScreen.texture("menu/backgrounds/image_02.png"),
            SofMainMenuScreen.texture("menu/backgrounds/image_03.png"),
            SofMainMenuScreen.texture("menu/backgrounds/image_04.png")
    };
    private static final ResourceLocation BUTTON_SHORT = SofMainMenuScreen.texture("menu/buttons/shortbutton_normal.png");
    private static final ResourceLocation BUTTON_SHORT_HOVER = SofMainMenuScreen.texture("menu/buttons/shortbutton_hover.png");
    private static final int BUTTON_BACK = 3000;
    private static final String[] ACTIVE_CREDITS = new String[] {
            "dwm.fm.credits.active.lonelyxiya",
            "dwm.fm.credits.active.dreamsource",
            "dwm.fm.credits.active.alingqing",
            "dwm.fm.credits.active.lonelywjx",
            "dwm.fm.credits.active.taiyangxina",
            "dwm.fm.credits.active.origins_eternity"
    };
    private static final String[] FORMER_CREDITS = new String[] {
            "dwm.fm.credits.former.lonelyyunzer",
            "dwm.fm.credits.former.gezi_sama",
            "dwm.fm.credits.former.tuijin",
            "dwm.fm.credits.former.ffei",
            "dwm.fm.credits.former.mczph"
    };

    private final Screen parent;
    private final long screenOpenedAt;
    private final long backgroundStartedAt;
    private Layout layout;

    public SofCreditsScreen(Screen parent) {
        super(Component.literal("SOF Credits"));
        this.parent = parent;
        this.screenOpenedAt = Util.getMillis();
        this.backgroundStartedAt = this.screenOpenedAt;
    }

    @Override
    protected void init() {
        this.layout = Layout.create(this.width, this.height);
        this.clearWidgets();
        this.addRenderableWidget(new TexturedMenuButton(this.layout.backButtonX, this.layout.backButtonY,
                this.layout.backButtonWidth, this.layout.backButtonHeight, Component.literal("<"),
                BUTTON_SHORT, BUTTON_SHORT_HOVER, this.screenOpenedAt, 0.15F, (btn) -> actionPerformed(BUTTON_BACK)));
    }

    private void actionPerformed(int id) {
        if (id == BUTTON_BACK) {
            this.minecraft.setScreen(this.parent);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) { // GLFW_KEY_ESCAPE
            this.minecraft.setScreen(this.parent);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (this.layout == null) {
            this.layout = Layout.create(this.width, this.height);
        }

        drawSlideshowBackground(graphics);
        graphics.fill(0, 0, this.width, this.height, 0x78000000);
        float alpha = getIntroAlpha(0.0F);
        drawGlassPanels(graphics, alpha);
        drawCredits(graphics, getIntroAlpha(0.12F));

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private float getIntroAlpha(float delaySeconds) {
        float seconds = (Util.getMillis() - this.screenOpenedAt) / 1000.0F;
        return MathHelper.clamp((seconds - delaySeconds) * 1.35F, 0.0F, 1.0F);
    }

    private void drawSlideshowBackground(GuiGraphics graphics) {
        long elapsed = Util.getMillis() - this.backgroundStartedAt;
        float seconds = elapsed / 1000.0F;
        float slideDuration = 7.0F;
        int slide = MathHelper.floor(seconds / slideDuration) % BACKGROUNDS.length;
        float progress = (seconds % slideDuration) / slideDuration;
        float fade = MathHelper.clamp((progress - 0.82F) / 0.18F, 0.0F, 1.0F);

        SofMainMenuScreen.drawTexturedQuad(graphics, BACKGROUNDS[slide], 0, 0, this.width, this.height, 1.0F);
        if (fade > 0.0F) {
            SofMainMenuScreen.drawTexturedQuad(graphics, BACKGROUNDS[(slide + 1) % BACKGROUNDS.length], 0, 0,
                    this.width, this.height, fade);
        }
    }

    private void drawCredits(GuiGraphics graphics, float alpha) {
        int alphaBits = MathHelper.clamp((int) (alpha * 255.0F), 0, 255) << 24;
        int activeEndY = drawColumn(graphics, Component.translatable("dwm.fm.credits.active"), ACTIVE_CREDITS,
                this.layout.leftX, this.layout.columnY, this.layout.columnWidth, alphaBits);
        int formerY = this.layout.stacked ? activeEndY + this.layout.sectionGap : this.layout.columnY;
        int formerX = this.layout.stacked ? this.layout.leftX : this.layout.rightX;
        drawColumn(graphics, Component.translatable("dwm.fm.credits.former"), FORMER_CREDITS, formerX, formerY,
                this.layout.columnWidth, alphaBits);
    }

    private void drawGlassPanels(GuiGraphics graphics, float alpha) {
        int glassAlpha = MathHelper.clamp((int) (alpha * 115.0F), 0, 115) << 24;
        int softAlpha = MathHelper.clamp((int) (alpha * 48.0F), 0, 48) << 24;
        int lineAlpha = MathHelper.clamp((int) (alpha * 135.0F), 0, 135) << 24;

        graphics.fill(this.layout.panelX - 3, this.layout.panelY - 3, this.layout.panelX + this.layout.panelWidth + 3,
                this.layout.panelY + this.layout.panelHeight + 3, softAlpha | 0xFFFFFF);
        graphics.fill(this.layout.panelX, this.layout.panelY, this.layout.panelX + this.layout.panelWidth,
                this.layout.panelY + this.layout.panelHeight, glassAlpha | 0xFFFFFF);
        graphics.fillGradient(this.layout.panelX, this.layout.panelY, this.layout.panelX + this.layout.panelWidth,
                this.layout.panelY + Math.max(12, this.layout.panelHeight / 5), (softAlpha | 0xFFFFFF),
                0x00FFFFFF);
        graphics.fill(this.layout.panelX, this.layout.panelY, this.layout.panelX + this.layout.panelWidth,
                this.layout.panelY + 1, lineAlpha | 0xFFFFFF);
        graphics.fill(this.layout.panelX, this.layout.panelY + this.layout.panelHeight - 1,
                this.layout.panelX + this.layout.panelWidth, this.layout.panelY + this.layout.panelHeight,
                lineAlpha | 0xFFFFFF);
        if (!this.layout.stacked) {
            graphics.fill(this.layout.dividerX, this.layout.panelY + this.layout.panelPadding,
                    this.layout.dividerX + 1, this.layout.panelY + this.layout.panelHeight - this.layout.panelPadding,
                    (MathHelper.clamp((int) (alpha * 70.0F), 0, 70) << 24) | 0xFFFFFF);
        }
    }

    private int drawColumn(GuiGraphics graphics, Component title, String[] entries, int x, int y, int width,
            int alphaBits) {
        int cursorY = y;
        drawScaledString(graphics, title, x, cursorY, alphaBits | 0x4A3822, this.layout.headerScale);
        cursorY += scaledLineHeight(this.layout.headerScale) + this.layout.titleGap;

        for (String entry : entries) {
            cursorY = drawWrappedString(graphics, Component.translatable(entry), x, cursorY, width,
                    alphaBits | 0x2A2118, this.layout.bodyScale);
            cursorY += this.layout.entryGap;
        }

        return cursorY;
    }

    private int drawWrappedString(GuiGraphics graphics, Component text, int x, int y, int width, int color,
            float scale) {
        int scaledWidth = Math.max(20, Math.round(width / scale));
        List<FormattedCharSequence> lines = this.font.split(text, scaledWidth);
        int cursorY = y;
        for (FormattedCharSequence line : lines) {
            drawScaledString(graphics, line, x, cursorY, color, scale);
            cursorY += scaledLineHeight(scale);
        }
        return cursorY;
    }

    private void drawScaledString(GuiGraphics graphics, Component text, int x, int y, int color, float scale) {
        graphics.pose().pushPose();
        graphics.pose().scale(scale, scale, 1.0F);
        graphics.drawString(this.font, text, Math.round(x / scale), Math.round(y / scale), color);
        graphics.pose().popPose();
    }

    private void drawScaledString(GuiGraphics graphics, FormattedCharSequence text, int x, int y, int color,
            float scale) {
        graphics.pose().pushPose();
        graphics.pose().scale(scale, scale, 1.0F);
        graphics.drawString(this.font, text, Math.round(x / scale), Math.round(y / scale), color);
        graphics.pose().popPose();
    }

    private static int scaledLineHeight(float scale) {
        return Math.max(6, Math.round(9.0F * scale));
    }

    private static class Layout {
        final int panelX;
        final int panelY;
        final int panelWidth;
        final int panelHeight;
        final int panelPadding;
        final int dividerX;
        final int leftX;
        final int rightX;
        final int columnY;
        final int columnWidth;
        final int sectionGap;
        final int titleGap;
        final int entryGap;
        final float headerScale;
        final float bodyScale;
        final boolean stacked;
        final int backButtonX;
        final int backButtonY;
        final int backButtonWidth;
        final int backButtonHeight;

        private Layout(int panelX, int panelY, int panelWidth, int panelHeight, int panelPadding, int dividerX,
                int leftX, int rightX, int columnY, int columnWidth, int sectionGap, int titleGap, int entryGap,
                float headerScale, float bodyScale, boolean stacked, int backButtonX, int backButtonY,
                int backButtonWidth, int backButtonHeight) {
            this.panelX = panelX;
            this.panelY = panelY;
            this.panelWidth = panelWidth;
            this.panelHeight = panelHeight;
            this.panelPadding = panelPadding;
            this.dividerX = dividerX;
            this.leftX = leftX;
            this.rightX = rightX;
            this.columnY = columnY;
            this.columnWidth = columnWidth;
            this.sectionGap = sectionGap;
            this.titleGap = titleGap;
            this.entryGap = entryGap;
            this.headerScale = headerScale;
            this.bodyScale = bodyScale;
            this.stacked = stacked;
            this.backButtonX = backButtonX;
            this.backButtonY = backButtonY;
            this.backButtonWidth = backButtonWidth;
            this.backButtonHeight = backButtonHeight;
        }

        static Layout create(int screenWidth, int screenHeight) {
            int margin = MathHelper.clamp(Math.round(Math.min(screenWidth, screenHeight) * 0.040F), 8, 26);
            int maxPanelWidth = Math.max(120, screenWidth - margin * 2);
            int maxPanelHeight = Math.max(110, screenHeight - margin * 2);
            int minPanelWidth = Math.min(320, maxPanelWidth);
            int minPanelHeight = Math.min(190, maxPanelHeight);
            int panelWidth = MathHelper.clamp(Math.round(screenWidth * 0.88F), minPanelWidth, maxPanelWidth);
            int panelHeight = MathHelper.clamp(Math.round(screenHeight * 0.82F), minPanelHeight, maxPanelHeight);
            int panelX = (screenWidth - panelWidth) / 2;
            int panelY = (screenHeight - panelHeight) / 2;
            int panelPadding = MathHelper.clamp(Math.round(panelWidth * 0.045F), 10, 30);
            boolean stacked = panelWidth < 520;
            int columnGap = stacked ? 0 : MathHelper.clamp(Math.round(panelWidth * 0.055F), 16, 42);
            int columnWidth = stacked ? panelWidth - panelPadding * 2
                    : Math.max(130, (panelWidth - panelPadding * 2 - columnGap) / 2);
            int leftX = panelX + panelPadding;
            int rightX = stacked ? leftX : leftX + columnWidth + columnGap;
            int dividerX = leftX + columnWidth + columnGap / 2;
            int columnY = panelY + panelPadding;
            float bodyScale = chooseBodyScale(screenWidth, screenHeight, panelWidth);
            float headerScale = chooseHeaderScale(screenWidth, screenHeight, panelWidth);
            int sectionGap = MathHelper.clamp(Math.round(panelHeight * 0.030F), 8, 18);
            int titleGap = MathHelper.clamp(Math.round(5.0F * bodyScale), 4, 8);
            int entryGap = MathHelper.clamp(Math.round(4.0F * bodyScale), 3, 7);
            int backButtonWidth = MathHelper.clamp(Math.round(panelWidth * 0.080F), 36, 60);
            int backButtonHeight = MathHelper.clamp(Math.round(panelHeight * 0.065F), 18, 26);
            int backButtonX = panelX + panelPadding;
            int backButtonY = panelY + panelHeight - panelPadding - backButtonHeight;

            return new Layout(panelX, panelY, panelWidth, panelHeight, panelPadding, dividerX, leftX, rightX,
                    columnY, columnWidth, sectionGap, titleGap, entryGap, headerScale, bodyScale, stacked,
                    backButtonX, backButtonY, backButtonWidth, backButtonHeight);
        }

        private static float chooseBodyScale(int screenWidth, int screenHeight, int panelWidth) {
            int shortSide = Math.min(screenWidth, screenHeight);
            if (panelWidth >= 980 && shortSide >= 680) {
                return 1.00F;
            }
            if (panelWidth >= 760 && shortSide >= 520) {
                return 0.92F;
            }
            if (panelWidth >= 560 && shortSide >= 400) {
                return 0.82F;
            }
            if (panelWidth >= 430) {
                return 0.76F;
            }
            return 0.72F;
        }

        private static float chooseHeaderScale(int screenWidth, int screenHeight, int panelWidth) {
            int shortSide = Math.min(screenWidth, screenHeight);
            if (panelWidth >= 980 && shortSide >= 680) {
                return 1.25F;
            }
            if (panelWidth >= 760 && shortSide >= 520) {
                return 1.15F;
            }
            if (panelWidth >= 560 && shortSide >= 400) {
                return 1.02F;
            }
            if (panelWidth >= 430) {
                return 0.92F;
            }
            return 0.86F;
        }
    }
}
