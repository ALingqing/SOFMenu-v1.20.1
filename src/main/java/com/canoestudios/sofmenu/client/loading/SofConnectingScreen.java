package com.canoestudios.sofmenu.client.loading;

import com.canoestudios.sofmenu.SOFMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

import java.lang.reflect.Field;

/**
 * Replaces the vanilla connection screen with the SOF artwork while keeping
 * the real {@link ConnectScreen} ticking so the connection proceeds.
 *
 * <p>Because 1.20.1 keeps {@code ConnectScreen}'s connection state private, the
 * wrapper uses Forge's SRG-aware reflection helper (like the 1.12.2 build
 * did) instead of an Access Transformer.</p>
 */
public class SofConnectingScreen extends SofWrappedScreen {

    private static final String CONNECTION_FIELD_NAME = "connection";
    private static final String PARENT_FIELD_NAME = "screen";

    private static final Field CONNECTION_FIELD = findField(ConnectScreen.class, CONNECTION_FIELD_NAME);
    private static final Field PARENT_FIELD = findField(ConnectScreen.class, PARENT_FIELD_NAME);

    private final ConnectScreen connectScreen;

    public SofConnectingScreen(ConnectScreen wrapped) {
        super(wrapped, Component.literal("SOF Connecting"));
        this.connectScreen = wrapped;
    }

    @Override
    protected void init() {
        super.init();
        this.clearWidgets();
        this.addRenderableWidget(Button.builder(Component.translatable("gui.cancel"), (btn) -> cancel())
                .bounds(this.width / 2 - 100, this.height / 4 + 132, 200, 20)
                .build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        LoadingScreenHandler.getInstance().renderScreen(graphics, this.width, this.height);

        String key = getConnection() == null ? "connect.connecting" : "connect.authorizing";
        graphics.drawCenteredString(this.font, LoadingScreenBackground.getAnimatedLoadingText(),
                this.width / 2, this.height / 2 - 12, 0xFFFFFF);
        graphics.drawCenteredString(this.font, Component.translatable(key),
                this.width / 2, this.height / 2 + 4, 0xFFFFFF);

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) { // GLFW_KEY_ESCAPE
            cancel();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void cancel() {
        Connection connection = getConnection();
        if (connection != null) {
            connection.close(Component.translatable("connect.aborted"));
        }
        this.minecraft.setScreen(getPreviousScreen());
    }

    private Connection getConnection() {
        if (CONNECTION_FIELD == null) {
            return null;
        }
        try {
            Object value = CONNECTION_FIELD.get(this.connectScreen);
            if (value instanceof Connection connection) {
                return connection;
            }
        } catch (IllegalAccessException | ClassCastException ignored) {
            // Fall back to the generic "connecting" state.
        }
        return null;
    }

    private Screen getPreviousScreen() {
        if (PARENT_FIELD != null) {
            try {
                Object value = PARENT_FIELD.get(this.connectScreen);
                if (value instanceof Screen screen) {
                    return screen;
                }
            } catch (IllegalAccessException | ClassCastException ignored) {
                // Fall through to the title screen.
            }
        }
        return new TitleScreen();
    }

    private static Field findField(Class<?> clazz, String officialName) {
        try {
            return ObfuscationReflectionHelper.findField(clazz, officialName);
        } catch (Exception exception) {
            SOFMenu.LOGGER.warn("Unable to resolve field {}.{} - cancel on the connection screen may not work.",
                    clazz.getSimpleName(), officialName, exception);
            return null;
        }
    }
}
