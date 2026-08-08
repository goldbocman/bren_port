package com.goldbocman.vgm.client.legacy;

//? if <1.21.11 {
/*import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.Identifier;
import com.goldbocman.vgm.common.Bren;

// Legacy counterpart of HudOverlay. GuiGraphics is the un-split pre-rewrite class (no
// GuiGraphicsExtractor wrapper, no separate GuiRenderState parameter needed), and blit() takes
// pixel-based u/v + explicit texture dimensions rather of the newer two-corner float-UV overload.
public class LegacyHudOverlay {
    private static final Identifier BULLET_ICONS = Identifier.fromNamespaceAndPath(Bren.MODID,
            "textures/gui/bullet_icons.png");

    public void renderWithTexture(GuiGraphics graphics, int currentAmmo, int maxAmmo, int uOffset) {
        int baseIconSize = 12;
        int rows = 2;
        int ri = rows * 10;
        int textureWidth = 72;
        int textureHeight = 12;

        for (int n = 0; n < maxAmmo; ++n) {
            int row = n / ri;
            int y1 = n * 6 - row * ri * 6;
            int x1 = 15 * row + 15;

            int u1 = n < currentAmmo ? 0 : 36;
            int textureOffset = uOffset + u1;

            graphics.blit(BULLET_ICONS, x1, y1, (float) textureOffset, 0.0F,
                    baseIconSize, baseIconSize, textureWidth, textureHeight);
        }
    }
}
*///?}
