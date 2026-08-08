package com.goldbocman.vgm.client.features.legacy;

//? if <1.21.11 {
/*import com.mojang.blaze3d.vertex.PoseStack;
//? if fabric {
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
//?}
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.LivingEntity;

// Legacy counterpart of GunHoldingFeatureRenderer. The modern file's real @Override is submit(...)
// (empty body) - its render(...) method has a different signature from anything RenderLayer actually
// requires, so it's unreachable dead code. The two-handed CROSSBOW_HOLD pose is already forced
// correctly by LegacyPlayerRendererMixin before HumanoidModel.setupAnim runs (same as the modern
// mixin split), so this layer is a no-op here too - kept only so it exists to register, matching the
// modern registration call in ClientBren without adding behavior nothing currently exercises.
//? if fabric
@Environment(EnvType.CLIENT)
public class LegacyGunHoldingFeatureRenderer<S extends LivingEntity, M extends EntityModel<S>> extends RenderLayer<S, M> {
    public LegacyGunHoldingFeatureRenderer(RenderLayerParent<S, M> context) {
        super(context);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int light, S entity,
                        float limbSwing, float limbSwingAmount, float partialTicks,
                        float ageInTicks, float netHeadYaw, float headPitch) {
    }
}
*///?}
