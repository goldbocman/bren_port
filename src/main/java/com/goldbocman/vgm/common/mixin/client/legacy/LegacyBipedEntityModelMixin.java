package com.goldbocman.vgm.common.mixin.client.legacy;

//? if <1.21.11 {
/*//? if fabric {
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
//?}
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import com.goldbocman.vgm.client.legacy.LegacyGunAnimationSystem;
import com.goldbocman.vgm.common.entity.IGunUser;
import com.goldbocman.vgm.common.registry.custom.types.GunItem;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Legacy counterpart of BipedEntityModelMixin. The old HumanoidModel.setupAnim(T entity, float
// limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) receives the
// entity directly, so none of the entity-ID-on-render-state plumbing
// (EntityRenderStateMixin/LivingEntityRendererMixin/IEntityRenderState) that the modern mixin needs
// is required here.
//? if fabric
@Environment(value = EnvType.CLIENT)
@Mixin(HumanoidModel.class)
public abstract class LegacyBipedEntityModelMixin implements ArmedModel, HeadedModel {

    @Shadow public abstract ModelPart getHead();

    @Shadow @Final public ModelPart leftArm;

    @Shadow @Final public ModelPart rightArm;

    @Shadow @Final public ModelPart hat;

    @Inject(method = "setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V", at = @At("TAIL"))
    private void bren$angles(LivingEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks,
                              float netHeadYaw, float headPitch, CallbackInfo ci) {
        if (!(entity instanceof Player player) || !(player instanceof IGunUser)) return;

        ItemStack mainHandItem = player.getMainHandItem();
        if (mainHandItem.isEmpty() || !(mainHandItem.getItem() instanceof GunItem)) return;

        LegacyGunAnimationSystem.applyGunAnimation(
            this.leftArm, this.rightArm, this.getHead(), this.hat,
            player, netHeadYaw, headPitch
        );
    }
}
*///?}
