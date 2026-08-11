package com.canoestudios.sofmenu.client;

import com.canoestudios.sofmenu.SOFMenu;
import com.canoestudios.sofmenu.client.gui.SofMainMenuScreen;
import com.canoestudios.sofmenu.client.loading.LoadingScreenBackground;
import com.canoestudios.sofmenu.client.loading.SofConnectingScreen;
import com.canoestudios.sofmenu.client.loading.SofLevelLoadingScreen;
import com.canoestudios.sofmenu.client.loading.SofLoadingOverlay;
import com.canoestudios.sofmenu.client.loading.SofReceivingLevelScreen;
import com.canoestudios.sofmenu.client.resources.MenuTextureCache;
import com.canoestudios.sofmenu.client.session.LastSessionStore;
import com.canoestudios.sofmenu.client.window.WindowCustomizer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.LevelLoadingScreen;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.client.gui.screens.Overlay;
import net.minecraft.client.gui.screens.ReceivingLevelScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.server.packs.resources.ReloadInstance;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

import java.lang.reflect.Field;
import java.util.function.Consumer;

/**
 * Central event wiring for the client-side SOF Menu features.
 *
 * <p>Registered on the FORGE event bus so that screen and tick events are
 * visible. The loading overlay cannot be hooked by a screen event (it is not a
 * {@link Screen}), so it is replaced lazily on the client tick.</p>
 */
@Mod.EventBusSubscriber(modid = SOFMenu.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class ClientEventHandler {

    private static boolean windowCustomized;

    private static final Field OVERLAY_FIELD = findField(Minecraft.class, "overlay");
    private static final Field LOADING_OVERLAY_RELOAD_FIELD = findField(LoadingOverlay.class, "reload");
    private static final Field LOADING_OVERLAY_ONFINISH_FIELD = findField(LoadingOverlay.class, "onFinish");

    private ClientEventHandler() {
    }

    @SubscribeEvent
    public static void onScreenOpening(ScreenEvent.Opening event) {
        Screen screen = event.getScreen();
        Minecraft minecraft = Minecraft.getInstance();

        if (screen instanceof ConnectScreen && !(screen instanceof SofConnectingScreen)) {
            event.setNewScreen(new SofConnectingScreen((ConnectScreen) screen));
            return;
        }
        if (screen instanceof ReceivingLevelScreen && !(screen instanceof SofReceivingLevelScreen)) {
            event.setNewScreen(new SofReceivingLevelScreen((ReceivingLevelScreen) screen));
            return;
        }
        if (screen instanceof LevelLoadingScreen && !(screen instanceof SofLevelLoadingScreen)) {
            event.setNewScreen(new SofLevelLoadingScreen((LevelLoadingScreen) screen));
            return;
        }
        if (!(screen instanceof TitleScreen) || screen instanceof SofMainMenuScreen) {
            return;
        }

        // Only replace the title screen on a clean slate (no world / server loaded).
        if (!isCleanTitleState(minecraft)) {
            return;
        }

        MenuTextureCache.schedulePreload();
        event.setNewScreen(new SofMainMenuScreen());
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        replaceLoadingOverlay(minecraft);

        if (!windowCustomized) {
            windowCustomized = true;
            WindowCustomizer.apply(minecraft, SOFMenu.LOGGER);
            MenuTextureCache.schedulePreload();
        }

        MenuTextureCache.tick(minecraft, SOFMenu.LOGGER);
        LastSessionStore.recordCurrentSession(minecraft);
    }

    /**
     * Replaces the vanilla {@link LoadingOverlay} with the SOF artwork. The
     * vanilla overlay is recreated by Minecraft on startup and on resource
     * reloads, so this simply swaps any vanilla instance for our own.
     */
    private static void replaceLoadingOverlay(Minecraft minecraft) {
        Overlay overlay = getOverlay(minecraft);
        if (overlay instanceof LoadingOverlay && !(overlay instanceof SofLoadingOverlay)) {
            ReloadInstance reload = getFieldValue(LOADING_OVERLAY_RELOAD_FIELD, overlay, ReloadInstance.class);
            Consumer<Void> onFinish = getFieldValue(LOADING_OVERLAY_ONFINISH_FIELD, overlay, Consumer.class);
            minecraft.setOverlay(new SofLoadingOverlay(minecraft, reload, onFinish));
        }
    }

    private static Overlay getOverlay(Minecraft minecraft) {
        if (OVERLAY_FIELD == null) {
            return null;
        }
        try {
            return (Overlay) OVERLAY_FIELD.get(minecraft);
        } catch (IllegalAccessException | ClassCastException ignored) {
            return null;
        }
    }

    private static <T> T getFieldValue(Field field, Object target, Class<T> type) {
        if (field == null) {
            return null;
        }
        try {
            Object value = field.get(target);
            return type.isInstance(value) ? type.cast(value) : null;
        } catch (IllegalAccessException | ClassCastException ignored) {
            return null;
        }
    }

    private static Field findField(Class<?> clazz, String officialName) {
        try {
            return ObfuscationReflectionHelper.findField(clazz, officialName);
        } catch (Exception exception) {
            SOFMenu.LOGGER.warn("Unable to resolve field {}.{} - the related feature is disabled.",
                    clazz.getSimpleName(), officialName, exception);
            return null;
        }
    }

    private static boolean isCleanTitleState(Minecraft minecraft) {
        return minecraft.level == null && minecraft.player == null && !minecraft.isSingleplayer();
    }
}
