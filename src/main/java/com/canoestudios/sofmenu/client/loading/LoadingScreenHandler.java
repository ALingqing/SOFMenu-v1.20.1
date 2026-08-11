package com.canoestudios.sofmenu.client.loading;

import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.List;

/**
 * Owns the elements drawn by SOFMenu's loading screens.
 *
 * <p>The default element renders the bundled loading artwork. Additional
 * elements can be added without changing the replacement renderers.</p>
 */
public final class LoadingScreenHandler {

    private static LoadingScreenHandler instance;

    private final List<LoadingScreenElement> elements = new ArrayList<>();
    private boolean setup;

    private LoadingScreenHandler() {
    }

    public static void load() {
        instance = new LoadingScreenHandler();
    }

    public static LoadingScreenHandler getInstance() {
        if (instance == null) {
            load();
        }
        return instance;
    }

    /** Adds a custom element after the default background. */
    public void addElement(LoadingScreenElement element) {
        if (element != null) {
            this.ensureSetup();
            this.elements.add(element);
        }
    }

    /** Removes all configured elements, including the default background. */
    public void clearElements() {
        this.elements.clear();
        this.setup = true;
    }

    public void renderScreen(GuiGraphics graphics, int width, int height) {
        this.renderScreen(graphics, width, height, -1);
    }

    public void renderScreen(GuiGraphics graphics, int width, int height, int progress) {
        this.ensureSetup();
        for (LoadingScreenElement element : this.elements) {
            element.render(graphics, width, height, progress);
        }
    }

    private void ensureSetup() {
        if (this.setup) {
            return;
        }

        this.setup = true;
        this.elements.add(new LoadingScreenElement() {
            @Override
            public void render(GuiGraphics graphics, int width, int height) {
                LoadingScreenBackground.render(graphics, width, height, -1);
            }

            @Override
            public void render(GuiGraphics graphics, int width, int height, int progress) {
                LoadingScreenBackground.render(graphics, width, height, progress);
            }
        });
    }
}
