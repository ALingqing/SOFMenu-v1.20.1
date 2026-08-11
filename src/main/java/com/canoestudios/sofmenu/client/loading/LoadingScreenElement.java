package com.canoestudios.sofmenu.client.loading;

import net.minecraft.client.gui.GuiGraphics;

/**
 * A single element rendered by the custom loading screen.
 */
public interface LoadingScreenElement {

    void render(GuiGraphics graphics, int width, int height);

    /**
     * Renders this element with the current loading progress. Elements that do
     * not use progress can keep implementing the original three-argument
     * method.
     */
    default void render(GuiGraphics graphics, int width, int height, int progress) {
        this.render(graphics, width, height);
    }
}
