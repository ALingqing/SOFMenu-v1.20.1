package com.canoestudios.sofmenu.client.session;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.client.multiplayer.ServerAddress;
import net.minecraft.client.multiplayer.ServerData;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Records the last server the player joined so the SOF menu can offer to
 * rejoin it.
 */
public final class LastSessionStore {

    private static final String KEY_IS_SERVER = "is_server";
    private static final String KEY_TARGET = "target";
    private static String cachedTarget = "";
    private static boolean cachedServer;
    private static boolean loaded;

    private LastSessionStore() {
    }

    public static void recordCurrentSession(Minecraft minecraft) {
        if (minecraft.level == null) {
            return;
        }

        if (minecraft.isSingleplayer() && minecraft.getSingleplayerServer() != null) {
            record(minecraft, minecraft.getSingleplayerServer().getWorldData().getLevelName(), false);
        } else if (minecraft.getCurrentServer() != null) {
            record(minecraft, minecraft.getCurrentServer().ip, true);
        }
    }

    public static void joinLastSession(Screen parent) {
        Minecraft minecraft = Minecraft.getInstance();
        load(minecraft);

        if (cachedTarget.isEmpty() || !isCleanTitleState(minecraft)) {
            minecraft.setScreen(new SelectWorldScreen(parent));
            return;
        }

        if (cachedServer) {
            ServerData serverData = new ServerData(cachedTarget, cachedTarget, false);
            ConnectScreen.startConnecting(parent, minecraft, ServerAddress.parseString(cachedTarget), serverData);
        } else {
            minecraft.setScreen(new SelectWorldScreen(parent));
        }
    }

    private static void record(Minecraft minecraft, String target, boolean server) {
        if (target == null || target.isEmpty()) {
            return;
        }

        load(minecraft);
        if (cachedServer == server && target.equals(cachedTarget)) {
            return;
        }

        cachedTarget = target;
        cachedServer = server;
        save(minecraft);
    }

    private static void load(Minecraft minecraft) {
        if (loaded) {
            return;
        }

        loaded = true;
        File file = getStoreFile(minecraft);
        if (!file.isFile()) {
            return;
        }

        Properties properties = new Properties();
        try (FileInputStream inputStream = new FileInputStream(file)) {
            properties.load(inputStream);
            cachedTarget = properties.getProperty(KEY_TARGET, "");
            cachedServer = Boolean.parseBoolean(properties.getProperty(KEY_IS_SERVER, "false"));
        } catch (IOException ignored) {
            cachedTarget = "";
            cachedServer = false;
        }
    }

    private static void save(Minecraft minecraft) {
        File file = getStoreFile(minecraft);
        File parent = file.getParentFile();
        if (parent != null && !parent.isDirectory()) {
            parent.mkdirs();
        }

        Properties properties = new Properties();
        properties.setProperty(KEY_TARGET, cachedTarget);
        properties.setProperty(KEY_IS_SERVER, Boolean.toString(cachedServer));
        try (FileOutputStream outputStream = new FileOutputStream(file)) {
            properties.store(outputStream, "SOF Menu last session");
        } catch (IOException ignored) {
        }
    }

    private static File getStoreFile(Minecraft minecraft) {
        return new File(new File(minecraft.gameDirectory, "mods/sofmenu"), "last_session.properties");
    }

    private static boolean isCleanTitleState(Minecraft minecraft) {
        return minecraft.level == null && minecraft.player == null && !minecraft.isSingleplayer();
    }
}
