package com.canoestudios.sofmenu.client.loading;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.opengl.GL11C;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Draws the supplied loading-screen artwork.
 *
 * <p>The numbered images are shown as a looped background animation with a
 * soft blurred cross-fade between slides.</p>
 */
public final class LoadingScreenBackground {

    private static final int FRAME_COUNT = 34;
    /** How long a frame remains sharp before the next slide starts. */
    private static final int FRAME_HOLD_MILLIS = 5000;
    /** Duration of the blurry cross-fade between two slides. */
    private static final int TRANSITION_MILLIS = 1400;
    private static final int DOT_ANIMATION_MILLIS = 350;
    private static final int BLURRED_TEXTURE_WIDTH = 160;
    private static final int BLURRED_TEXTURE_HEIGHT = 90;

    private static final ResourceLocation[] BACKGROUND_FRAMES = createBackgroundFrames();
    private static final List<ResourceLocation> PRELOAD_TEXTURES = createPreloadTextures();
    private static final Map<Integer, ResourceLocation> BLURRED_TEXTURES = new HashMap<>();
    private static final boolean[] BLUR_GENERATION_FAILED = new boolean[FRAME_COUNT];
    private static int nextBlurredFrameToPreload;

    private LoadingScreenBackground() {
    }

    public static void render(GuiGraphics graphics, int width, int height, int progress) {
        long slideDuration = FRAME_HOLD_MILLIS + TRANSITION_MILLIS;
        long animationTime = Util.getMillis();
        int frame = (int) ((animationTime / slideDuration) % FRAME_COUNT);
        long slideTime = animationTime % slideDuration;

        if (slideTime < FRAME_HOLD_MILLIS) {
            drawTexture(graphics, BACKGROUND_FRAMES[frame], 0, 0, width, height, 1.0F);
        } else {
            int nextFrame = (frame + 1) % FRAME_COUNT;
            float transition = (float) (slideTime - FRAME_HOLD_MILLIS) / (float) TRANSITION_MILLIS;
            transition = smoothStep(transition);
            float blurAmount = (float) Math.sin(Math.PI * transition);

            ResourceLocation currentBlur = getBlurredTexture(frame);
            ResourceLocation nextBlur = getBlurredTexture(nextFrame);

            // The two sharp images are cross-faded, while their blurred copies
            // take over in the middle of the transition.
            drawTexture(graphics, BACKGROUND_FRAMES[frame], 0, 0, width, height, 1.0F - transition);
            drawTexture(graphics, currentBlur == null ? BACKGROUND_FRAMES[frame] : currentBlur,
                    0, 0, width, height, (1.0F - transition) * blurAmount);
            drawTexture(graphics, BACKGROUND_FRAMES[nextFrame], 0, 0, width, height, transition);
            drawTexture(graphics, nextBlur == null ? BACKGROUND_FRAMES[nextFrame] : nextBlur,
                    0, 0, width, height, transition * blurAmount);
        }
    }

    public static List<ResourceLocation> getPreloadTextures() {
        return PRELOAD_TEXTURES;
    }

    public static String getAnimatedLoadingText() {
        int dots = (int) ((Util.getMillis() / DOT_ANIMATION_MILLIS) % 4);
        StringBuilder text = new StringBuilder("Loading");
        for (int i = 0; i < dots; ++i) {
            text.append('.');
        }
        return text.toString();
    }

    /**
     * Builds one low-resolution blurred copy per client tick so that the
     * transition never stalls the loading screen.
     */
    public static void preloadNextBlurredFrame() {
        int frame = nextBlurredFrameToPreload;
        nextBlurredFrameToPreload = (nextBlurredFrameToPreload + 1) % FRAME_COUNT;
        if (!BLURRED_TEXTURES.containsKey(frame) && !BLUR_GENERATION_FAILED[frame]) {
            createBlurredTexture(frame);
        }
    }

    private static ResourceLocation getBlurredTexture(int frame) {
        ResourceLocation texture = BLURRED_TEXTURES.get(frame);
        if (texture == null && !BLUR_GENERATION_FAILED[frame]) {
            texture = createBlurredTexture(frame);
        }
        return texture;
    }

    private static ResourceLocation createBlurredTexture(int frame) {
        Minecraft minecraft = Minecraft.getInstance();
        try (InputStream inputStream = minecraft.getResourceManager().open(BACKGROUND_FRAMES[frame])) {
            BufferedImage source = ImageIO.read(inputStream);
            if (source == null) {
                throw new IOException("Image data is empty");
            }

            BufferedImage reduced = new BufferedImage(BLURRED_TEXTURE_WIDTH, BLURRED_TEXTURE_HEIGHT,
                    BufferedImage.TYPE_INT_ARGB);
            Graphics2D graphics = reduced.createGraphics();
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
            graphics.drawImage(source, 0, 0, BLURRED_TEXTURE_WIDTH, BLURRED_TEXTURE_HEIGHT, null);
            graphics.dispose();

            // A small Gaussian kernel plus the downsample makes the blur soft
            // without requiring a GLSL shader.
            float[] gaussian = new float[] {
                    1.0F, 4.0F, 6.0F, 4.0F, 1.0F,
                    4.0F, 16.0F, 24.0F, 16.0F, 4.0F,
                    6.0F, 24.0F, 36.0F, 24.0F, 6.0F,
                    4.0F, 16.0F, 24.0F, 16.0F, 4.0F,
                    1.0F, 4.0F, 6.0F, 4.0F, 1.0F
            };
            for (int i = 0; i < gaussian.length; ++i) {
                gaussian[i] /= 256.0F;
            }
            BufferedImage blurred = new BufferedImage(BLURRED_TEXTURE_WIDTH, BLURRED_TEXTURE_HEIGHT,
                    BufferedImage.TYPE_INT_ARGB);
            new ConvolveOp(new Kernel(5, 5, gaussian), ConvolveOp.EDGE_NO_OP, null).filter(reduced, blurred);

            NativeImage nativeImage = toNativeImage(blurred);
            DynamicTexture dynamicTexture = new DynamicTexture(nativeImage);
            TextureManager textureManager = minecraft.getTextureManager();
            ResourceLocation location = textureManager.register("sofmenu_loading_blur_" + frame, dynamicTexture);

            // DynamicTexture defaults to nearest filtering; linear filtering is
            // important when the small blurred copy is enlarged to full screen.
            RenderSystem.bindTexture(dynamicTexture.getId());
            RenderSystem.texParameter(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_MIN_FILTER, GL11C.GL_LINEAR);
            RenderSystem.texParameter(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_MAG_FILTER, GL11C.GL_LINEAR);

            BLURRED_TEXTURES.put(frame, location);
            return location;
        } catch (IOException | RuntimeException exception) {
            BLUR_GENERATION_FAILED[frame] = true;
            return null;
        }
    }

    private static NativeImage toNativeImage(BufferedImage image) {
        NativeImage nativeImage = new NativeImage(image.getWidth(), image.getHeight(), false);
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int argb = image.getRGB(x, y);
                // BufferedImage uses ARGB, NativeImage expects ABGR packing.
                int abgr = (argb & 0xFF00FF00) | ((argb >> 16) & 0xFF) | ((argb & 0xFF) << 16);
                nativeImage.setPixelRGBA(x, y, abgr);
            }
        }
        return nativeImage;
    }

    private static float smoothStep(float value) {
        value = Math.max(0.0F, Math.min(1.0F, value));
        return value * value * (3.0F - 2.0F * value);
    }

    private static ResourceLocation[] createBackgroundFrames() {
        ResourceLocation[] frames = new ResourceLocation[FRAME_COUNT];
        for (int i = 0; i < FRAME_COUNT; ++i) {
            frames[i] = new ResourceLocation("sofmenu",
                    "textures/loading/backgrounds/" + (i + 1) + ".png");
        }
        return frames;
    }

    private static List<ResourceLocation> createPreloadTextures() {
        List<ResourceLocation> textures = new ArrayList<>(FRAME_COUNT);
        Collections.addAll(textures, BACKGROUND_FRAMES);
        return Collections.unmodifiableList(textures);
    }

    private static void drawTexture(GuiGraphics graphics, ResourceLocation texture, int x, int y, int width,
            int height, float alpha) {
        graphics.setColor(1.0F, 1.0F, 1.0F, alpha);
        graphics.blit(texture, x, y, 0, 0, width, height, width, height);
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
    }
}
