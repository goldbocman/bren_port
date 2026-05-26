package nl.sniffiandros.bren.client;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import net.minecraft.resources.Identifier;
import nl.sniffiandros.bren.common.Bren;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HudOverlay {
    private static final Logger LOGGER = LoggerFactory.getLogger("Bren/HudOverlay");
    private static final Identifier BULLET_ICONS = Identifier.fromNamespaceAndPath(Bren.MODID,
            "textures/gui/bullet_icons.png");

    public void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, int currentAmmo, int maxAmmo, int uOffset) {
          int width = graphics.guiWidth();
        int height = graphics.guiHeight();

        int rows = 2;
        int ri = rows * 10;

        for (int n = 0; n < maxAmmo; ++n) {
            int row = n / ri;
            int y1 = height - 30 - (n * 6 - row * ri * 6);
            int x1 = width - 30 - (15 * row);
            int color = n < currentAmmo ? 0xFFFFFFFF : 0xFF666666;
            graphics.fill(x1, y1, x1 + 12, y1 + 12, color);
        }
    }

    public void renderWithTexture(GuiGraphicsExtractor graphics, GuiRenderState guiRenderState, DeltaTracker deltaTracker, int currentAmmo, int maxAmmo, int uOffset) {
        int width = graphics.guiWidth();
        int height = graphics.guiHeight();

        int baseIconSize = 12;
        int screenScale = Math.max(1, width / 800);
        int iconSize = baseIconSize * screenScale;
        int rows = 2;
        int ri = rows * 10;
        int textureWidth = 48;
        int textureHeight = 12;
        float v0 = 0.0f;
        float v1 = (float) baseIconSize / textureHeight;

        for (int n = 0; n < maxAmmo; ++n) {
            int row = n / ri;
            int y1 = n * (6 * screenScale) - row * ri * (6 * screenScale);
            int x1 = (15 * screenScale) * row + (15 * screenScale);
            
            int u1 = n < currentAmmo ? 0 : 24;
            int textureOffset = uOffset + u1;
            
            float u0 = (float) textureOffset / textureWidth;
            float u1_uv = (float) (textureOffset + baseIconSize) / textureWidth;
            
            graphics.blit(BULLET_ICONS, x1, y1, x1 + iconSize, y1 + iconSize, 
                         u0, u1_uv, v0, v1);
        }
    }
}