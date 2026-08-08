package com.goldbocman.vgm.common.mixin.client.legacy;

//? if <1.21.11 {
/*import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import com.goldbocman.vgm.client.legacy.LegacyHudOverlay;
import com.goldbocman.vgm.common.registry.custom.types.GunItem;
import com.goldbocman.vgm.common.registry.custom.types.GunWithMagItem;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Legacy counterpart of MinecraftClientHudMixin. Gui.render(GuiGraphics, DeltaTracker) is the real
// vanilla HUD render hook pre-1.21.11 - a cleaner injection point than the modern mixin's piggyback
// on GameRenderer.renderLevel (which exists purely because the render-state world doesn't expose a
// GuiGraphics-carrying HUD hook as conveniently).
@Mixin(Gui.class)
public class LegacyGuiMixin {
    @Unique
    private static final LegacyHudOverlay HUD_OVERLAY = new LegacyHudOverlay();

    @Shadow @Final private Minecraft minecraft;

    @Inject(method = "render", at = @At("TAIL"))
    private void bren$renderHudOverlay(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
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

        HUD_OVERLAY.renderWithTexture(guiGraphics, i, max, gunItem.ammoIconOffset());
    }
}
*///?}
