package com.goldbocman.vgm.common.mixin.client;

//? if >=1.21.11 {
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.state.GameRenderState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import com.goldbocman.vgm.client.HudOverlay;
import com.goldbocman.vgm.common.registry.custom.types.GunItem;
import com.goldbocman.vgm.common.registry.custom.types.GunWithMagItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class MinecraftClientHudMixin {
    @Unique
    private static final Logger LOGGER = LoggerFactory.getLogger("Bren/HudMixin");
    @Unique
    private static final HudOverlay HUD_OVERLAY = new HudOverlay();

    @Shadow
    private GameRenderState gameRenderState;

    @Shadow
    private Minecraft minecraft;

    @Inject(method = "renderLevel", at = @At("TAIL"))
    private void renderHudOverlay(DeltaTracker deltaTracker, CallbackInfo ci) {
        if (minecraft.player == null) {
            return;
        }

        Player player = minecraft.player;
        ItemStack gun = player.getMainHandItem();

        if (!(gun.getItem() instanceof GunItem gunItem)) {
            return;
        }

        int i = gunItem.getContents(gun);
        int max = gunItem.getMaxCapacity(gun);

        if (gunItem instanceof GunWithMagItem && !GunWithMagItem.hasMagazine(gun)) {
            return;
        }

        // 使用 GameRenderState 进行渲染
        if (gameRenderState != null) {
            try {
                // 创建 GuiGraphicsExtractor 实例，需要4个参数
                GuiGraphicsExtractor graphics = new GuiGraphicsExtractor(
                      minecraft,
                        gameRenderState.guiRenderState,
                      0,  // mouseX
                      0   // mouseY
                  );
                HUD_OVERLAY.renderWithTexture(graphics, gameRenderState.guiRenderState, deltaTracker, i, max, gunItem.ammoIconOffset());
            } catch (Exception e) {
                LOGGER.error("Failed to create GuiGraphicsExtractor", e);
            }
        }
    }
}
//?}