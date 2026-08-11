package com.canoestudios.sofmenu.client.loading;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * Base class for SOF loading screens that wrap a vanilla screen. Lifecycle
 * calls are forwarded to the wrapped screen so the underlying operation
 * (connection, terrain download, world load) keeps progressing, while the
 * rendering is replaced with the SOF loading artwork.
 */
public abstract class SofWrappedScreen extends Screen {

    protected final Screen wrapped;

    protected SofWrappedScreen(Screen wrapped, Component title) {
        super(title);
        this.wrapped = wrapped;
    }

    /**
     * The wrapped screen is replaced by the event before the game has a chance
     * to call {@link Screen#init(Minecraft, int, int)} on it, so it is
     * initialized here to keep its internal state (minecraft, width, height)
     * consistent while it keeps ticking in the background.
     */
    @Override
    protected void init() {
        this.wrapped.init(this.minecraft, this.width, this.height);
    }

    @Override
    public void tick() {
        this.wrapped.tick();
    }

    @Override
    public void removed() {
        this.wrapped.removed();
    }

    @Override
    public void onClose() {
        this.wrapped.onClose();
    }

    @Override
    public boolean isPauseScreen() {
        return this.wrapped.isPauseScreen();
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return this.wrapped.shouldCloseOnEsc();
    }
}
