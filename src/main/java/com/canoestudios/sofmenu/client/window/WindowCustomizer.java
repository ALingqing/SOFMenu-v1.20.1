package com.canoestudios.sofmenu.client.window;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.system.MemoryUtil;
import org.slf4j.Logger;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * Sets the GLFW window title and icon to the SOF branding.
 */
public final class WindowCustomizer {

    private static final String WINDOW_TITLE = "Survival - Origin & Future BY CanoeStudio";
    private static final ResourceLocation ICON_16 = new ResourceLocation("sofmenu", "textures/menu/window/icon16x16.png");
    private static final ResourceLocation ICON_32 = new ResourceLocation("sofmenu", "textures/menu/window/icon32x32.png");

    private WindowCustomizer() {
    }

    public static void apply(Minecraft minecraft, Logger logger) {
        try {
            long window = minecraft.getWindow().getWindow();
            GLFW.glfwSetWindowTitle(window, WINDOW_TITLE);

            GLFWImage.Buffer icons = GLFWImage.malloc(2);
            try {
                icons.put(0, loadIcon(minecraft, ICON_16));
                icons.put(1, loadIcon(minecraft, ICON_32));
                GLFW.glfwSetWindowIcon(window, icons);
            } finally {
                icons.free();
            }
        } catch (Exception exception) {
            logger.warn("Unable to apply SOF Menu window icon.", exception);
        }
    }

    private static GLFWImage loadIcon(Minecraft minecraft, ResourceLocation location) throws Exception {
        try (InputStream inputStream = minecraft.getResourceManager().open(location)) {
            BufferedImage image = ImageIO.read(inputStream);
            int[] pixels = new int[image.getWidth() * image.getHeight()];
            image.getRGB(0, 0, image.getWidth(), image.getHeight(), pixels, 0, image.getWidth());

            ByteBuffer buffer = MemoryUtil.memAlloc(pixels.length * 4);
            for (int pixel : pixels) {
                buffer.put((byte) ((pixel >> 16) & 0xFF));
                buffer.put((byte) ((pixel >> 8) & 0xFF));
                buffer.put((byte) (pixel & 0xFF));
                buffer.put((byte) ((pixel >> 24) & 0xFF));
            }
            buffer.flip();

            GLFWImage icon = GLFWImage.create();
            icon.set(image.getWidth(), image.getHeight(), buffer);
            return icon;
        }
    }
}
