//? if <1.21.11 {
/*package com.goldbocman.vgm.common.mixin.client.legacy;

import net.minecraft.client.renderer.item.ClampedItemPropertyFunction;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

// ItemProperties.register(Item, Identifier, ClampedItemPropertyFunction) is private on 1.21.1 -
// pre-1.21.4 item models resolve their "overrides" predicates only through this method, and this
// codebase's custom vgm:has_magazine predicate needs a real registration to ever fire on 1.21.1 (see
// claude/gun-rendering.md). Exposed via @Invoker instead of the vgm.ct Access Widener since that's
// only wired into build.fabric.gradle.kts, not NeoForge's - this works identically on both loaders.
@Mixin(ItemProperties.class)
public interface LegacyItemPropertiesAccessor {
    @Invoker("register")
    static void invokeRegister(Item item, Identifier id, ClampedItemPropertyFunction function) {
        throw new AssertionError();
    }
}
*///?}
