package com.goldbocman.vgm.common.mixin.client.legacy1201;

//? if <1.20.5 {
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

// Legacy1201 counterpart of LegacyGuiMixin - Gui.render(GuiGraphics, DeltaTracker) doesn't exist yet
// at 1.20.1 (DeltaTracker itself is a 1.20.5+ class, confirmed via javap); the real vanilla hook
// there is render(GuiGraphics, float partialTick) instead. Otherwise identical logic to
// LegacyGuiMixin.
@Mixin(Gui.class)
public class Legacy1201GuiMixin {
    @Unique
    private static final LegacyHudOverlay HUD_OVERLAY = new LegacyHudOverlay();

    @Shadow @Final private Minecraft minecraft;

    @Inject(method = "render", at = @At("TAIL"))
    private void bren$renderHudOverlay(GuiGraphics guiGraphics, float partialTick, CallbackInfo ci) {
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
//?}
