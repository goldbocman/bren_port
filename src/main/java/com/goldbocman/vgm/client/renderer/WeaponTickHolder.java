package com.goldbocman.vgm.client.renderer;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;

public class WeaponTickHolder {

    // May use it for something else

    private static int ticks;
    private static int last_ticks;

    public static void tick(Minecraft client) {
        if (!client.isPaused()) {
            last_ticks = ticks;
            ticks = Math.max(0, --ticks);
        }
    }

    public static void setTicks(int t) {
        ticks = t;
    }

    public static float getAnimationTicks(float tickDelta) {
        return Mth.lerp(tickDelta, (float)last_ticks, (float)ticks);
    }

}
