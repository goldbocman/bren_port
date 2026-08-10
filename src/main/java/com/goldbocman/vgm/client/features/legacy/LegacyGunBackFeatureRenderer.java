package com.goldbocman.vgm.client.features.legacy;

//? if <1.21.11 {
/*import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.LivingEntity;

// Legacy counterpart of GunBackFeatureRenderer. The modern file's render/submit overrides are both
// empty (its renderGunOnBack/renderGunItem static helpers are unused dead code, never called from
// anywhere), so this ports the same not-yet-implemented state without carrying
// over the unused reflection-based ModelPart pivot extraction, matching this repo's convention of not
// porting confirmed-dead code as if it were load-bearing.
public class LegacyGunBackFeatureRenderer<T extends LivingEntity, M extends HumanoidModel<T>> extends RenderLayer<T, M> {

    private final LivingEntityRenderer<T, M> itemRenderer;

    public LegacyGunBackFeatureRenderer(RenderLayerParent<T, M> context, LivingEntityRenderer<T, M> itemRenderer) {
        super(context);
        this.itemRenderer = itemRenderer;
    }

    public LivingEntityRenderer<T, M> getItemRenderer() {
        return itemRenderer;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int light, T entity,
                        float limbSwing, float limbSwingAmount, float partialTicks,
                        float ageInTicks, float netHeadYaw, float headPitch) {
    }
}
*///?}
