package net.minecraft.util;

/**
 * Compatibility shim for the 1.12.2-era {@code MathHelper} name.
 *
 * <p>Minecraft 1.20.1 renamed this utility to {@link Mth}. This class keeps
 * the ported SOF Menu code (and any legacy call sites) compiling unchanged by
 * delegating the used helpers to {@link Mth}.</p>
 */
@SuppressWarnings("unused")
public final class MathHelper {

    private MathHelper() {
    }

    public static int clamp(int value, int min, int max) {
        return Mth.clamp(value, min, max);
    }

    public static float clamp(float value, float min, float max) {
        return Mth.clamp(value, min, max);
    }

    public static double clamp(double value, double min, double max) {
        return Mth.clamp(value, min, max);
    }

    public static int floor(float value) {
        return Mth.floor(value);
    }

    public static int floor(double value) {
        return Mth.floor(value);
    }

    public static int ceil(float value) {
        return Mth.ceil(value);
    }

    public static int ceil(double value) {
        return Mth.ceil(value);
    }
}
