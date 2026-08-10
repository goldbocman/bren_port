package com.goldbocman.vgm.common.mixin.client;

//? if >=1.21.11 {
import com.mojang.blaze3d.vertex.PoseStack;
//? if fabric {
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
//?}
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.state.ArmedEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import com.goldbocman.vgm.client.IEntityRenderState;
import com.goldbocman.vgm.common.entity.IGunUser;
import com.goldbocman.vgm.common.registry.custom.types.GunItem;
import com.goldbocman.vgm.common.utils.GunHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Third-person gun-in-hand transform for the >=1.21.11 render pipeline. 26.2 ItemInHandRenderer.renderItem - what
// FirstPersonItemRendererMixin hooks - is only ever called from submitHandsWithItems, i.e. it is
// first-person-only on this pipeline. Third-person hand items (self in third-person camera, and
// every remote player) go entirely through ItemInHandLayer.submitArmWithItem instead, which builds
// its own real PoseStack (arm-translate + arm-pose animation) and hands it straight to
// ItemStackRenderState.submit(...) - so injecting right before that call gives GunItem.applyCustomMatrix
// a genuine PoseStack to work with, the same real API both FirstPersonItemRendererMixin and 1.21.1's
// LegacyItemInHandRendererMixin use, instead of reflecting into ItemStackRenderState's private fields.
@SuppressWarnings("ALL")
//? if fabric
@Environment(value = EnvType.CLIENT)
@Mixin(ItemInHandLayer.class)
public abstract class ItemInHandLayerMixin {

    @Inject(
        method = "submitArmWithItem",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/item/ItemStackRenderState;submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;III)V"
        )
    )
    private void bren$applyThirdPersonGunMatrix(
            ArmedEntityRenderState state, ItemStackRenderState itemRenderState, ItemStack stack,
            HumanoidArm arm, PoseStack poseStack, net.minecraft.client.renderer.SubmitNodeCollector collector,
            int light, CallbackInfo ci) {

        if (stack.isEmpty() || !(stack.getItem() instanceof GunItem gunItem)) return;

        Minecraft client = Minecraft.getInstance();
        if (client.level == null) return;

        int entityId = ((IEntityRenderState) state).bren$getEntityId();
        if (entityId == -1) return;

        Entity entity = client.level.getEntity(entityId);
        if (!(entity instanceof Player player) || !(player instanceof IGunUser gunUser)) return;

        float cooldownProgress = player.getCooldowns().getCooldownPercent(stack, 0.0F);
        GunHelper.GunStates gunState = gunUser.bren_1_21_1$getGunState();
        boolean leftHanded = arm == HumanoidArm.LEFT;

        gunItem.applyCustomMatrix(player, gunState, poseStack, stack, cooldownProgress, leftHanded);
    }
}
//?}
