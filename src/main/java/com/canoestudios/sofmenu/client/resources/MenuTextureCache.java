package com.canoestudios.sofmenu.client.resources;

import com.canoestudios.sofmenu.client.gui.SofMainMenuScreen;
import com.canoestudios.sofmenu.client.loading.LoadingScreenBackground;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

/**
 * Preloads the SOF menu and loading textures a couple at a time on the client
 * tick so the screens never hitch while decoding them.
 */
public final class MenuTextureCache {

    private static final Queue<ResourceLocation> PRELOAD_QUEUE = new ArrayDeque<>();
    private static final Set<ResourceLocation> QUEUED_TEXTURES = new HashSet<>();
    private static final Set<ResourceLocation> LOADED_TEXTURES = new HashSet<>();

    private MenuTextureCache() {
    }

    public static void schedulePreload() {
        for (ResourceLocation texture : SofMainMenuScreen.getPreloadTextures()) {
            if (!LOADED_TEXTURES.contains(texture) && QUEUED_TEXTURES.add(texture)) {
                PRELOAD_QUEUE.offer(texture);
            }
        }
        for (ResourceLocation texture : LoadingScreenBackground.getPreloadTextures()) {
            if (!LOADED_TEXTURES.contains(texture) && QUEUED_TEXTURES.add(texture)) {
                PRELOAD_QUEUE.offer(texture);
            }
        }
    }

    public static void tick(Minecraft minecraft, Logger logger) {
        if (minecraft == null || minecraft.getTextureManager() == null) {
            return;
        }

        schedulePreload();
        LoadingScreenBackground.preloadNextBlurredFrame();
        ResourceLocation texture = PRELOAD_QUEUE.poll();
        if (texture == null) {
            return;
        }

        QUEUED_TEXTURES.remove(texture);
        TextureManager textureManager = minecraft.getTextureManager();
        if (textureManager.getTexture(texture) != null) {
            LOADED_TEXTURES.add(texture);
            return;
        }

        try {
            textureManager.register(texture, new SimpleTexture(texture));
            LOADED_TEXTURES.add(texture);
        } catch (RuntimeException exception) {
            logger.warn("Unable to preload SOF Menu texture {}.", texture, exception);
        }
    }
}
