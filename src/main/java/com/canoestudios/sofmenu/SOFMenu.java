package com.canoestudios.sofmenu;

import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

/**
 * SOF Menu - a client-side menu / loading screen overhaul for the SOF modpack.
 *
 * <p>All functionality is client-side and is wired up through
 * {@link com.canoestudios.sofmenu.client.ClientEventHandler}, which is
 * registered on the Forge (FORGE) event bus.</p>
 */
@Mod(SOFMenu.MODID)
public final class SOFMenu {

    public static final String MODID = "sofmenu";
    public static final String MOD_NAME = "SOF Menu";
    public static final Logger LOGGER = LogUtils.getLogger();

    public SOFMenu() {
        // Mod construction only; client behaviour lives in ClientEventHandler.
    }
}
