package com.goldbocman.vgm.common.mixin.client;

//? if >=1.21.11 {
//? if fabric {
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
//?}
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

//? if fabric
@Environment(value= EnvType.CLIENT)
@Mixin(GuiGraphicsExtractor.class)
public abstract class DrawContextMixin {

    @Shadow public abstract void fill(int x1, int y1, int x2, int y2, int color);

    // 修复：在 Minecraft 26.1 中，renderItem 方法可能已被移除或重命名
    // 暂时注释掉所有注入点，避免游戏崩溃
    /*
    @Inject(at = @At("HEAD"), method = "renderItem")
    private void editGuiItem(ItemStack stack, int x, int y, CallbackInfo ci) {
        if (!stack.isEmpty()) {
            if (stack.getItem() instanceof GunItem gunItem) {
                if (showBar(stack)) {
                    int m = gunItem.getMaxCapacity(stack);
                    int i = m > 0 ? Math.round(((float) gunItem.getContents(stack) / m) * 13) : 0;
                    int j = Bren.UNIVERSAL_AMMO_COLOR;
                    int k = x + 2;
                    int l = y + 11;
                    // 修复：使用正确的颜色计算
                    this.fill(k, l, k + 13, l + 2, 0xFF000000); // 黑色背景
                    this.fill(k, l, k + i, l + 1, j); // 弹药条颜色
                }
            }
        }
    }

    private static boolean showBar(ItemStack stack) {
        if (stack.getItem() instanceof GunWithMagItem) {
            return GunWithMagItem.hasMagazine(stack);
        } else if (stack.getItem() instanceof GunItem gunItem) {
            // 修复：对于普通枪械，总是显示弹药条，无论是否有弹药
            return gunItem.getMaxCapacity(stack) > 0;
        }
        return true;
    }
    */
}
//?}