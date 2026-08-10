package com.goldbocman.vgm.common.mixin.legacy1201;

//? if <1.20.5 {
/*import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import com.goldbocman.vgm.common.registry.custom.types.GunItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Pre-1.20.5 Enchantment.canEnchant(ItemStack) is the sole applicability gate (no separate canApplyAtEnchantingTable method exists yet at this
// version) and just delegates to `this.category.canEnchantItem(stack.getItem())`. Vanilla hardcodes
// Enchantments.FLAMING_ARROWS/PUNCH_ARROWS to EnchantmentCategory.BOW, whose predicate is
// `item instanceof BowItem` - guns never match, so both enchantments silently refuse to apply
// (enchanting table, anvil, /enchant) even though GunItem.getEnchantmentValue()==1 makes guns
// enchantable in general. Unbreaking uses the much broader BREAKABLE category
// (`item.canBeDepleted()`), which every gun trivially satisfies, which is why only Unbreaking worked.
// The data-driven `data/vgm/tags/item/enchantable/{flame,punch}.json` fix used on >=1.21 doesn't
// exist as a mechanism pre-1.20.5, so this version needs its own Java-side override instead.
@Mixin(Enchantment.class)
public class GunEnchantmentMixin {

    @Inject(method = "canEnchant",
            //? if forge && >=1.20.5
            //remap = false,
            at = @At("HEAD"), cancellable = true)
    private void bren$allowGunFlamePunch(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        Enchantment self = (Enchantment) (Object) this;
        if ((self == Enchantments.FLAMING_ARROWS || self == Enchantments.PUNCH_ARROWS)
                && stack.getItem() instanceof GunItem) {
            cir.setReturnValue(true);
        }
    }
}
*///?}
