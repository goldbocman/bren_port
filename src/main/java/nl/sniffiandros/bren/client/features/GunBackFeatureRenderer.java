package nl.sniffiandros.bren.client.features;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import nl.sniffiandros.bren.common.entity.IGunUser;

public class GunBackFeatureRenderer<T extends net.minecraft.client.renderer.entity.state.HumanoidRenderState, M extends HumanoidModel<T>> extends RenderLayer<T, M> {

    private final LivingEntityRenderer itemRenderer;

    public GunBackFeatureRenderer(RenderLayerParent context, LivingEntityRenderer itemRenderer) {
        super(context);
        this.itemRenderer = itemRenderer;
    }

    // Add a static method for external calls to render the gun on the back.
    public static <T extends LivingEntity> void renderGunOnBack(T entity, PoseStack matrices,
//                                                                MultiBufferSource vertexConsumers,
                                                                int light, LivingEntityRenderer itemRenderer, HumanoidModel<?> model) {
        if (entity instanceof IGunUser gunUser) {
            ItemStack lastGun = gunUser.bren_1_21_1$getLastGun();
            if (!lastGun.isEmpty()) {
                renderGunItem(entity, lastGun, matrices, light, itemRenderer, model);
            }
        }
    }

    // Auxiliary methods for rendering firearms and other items
    private static <T extends LivingEntity> void renderGunItem(T entity, ItemStack stack, PoseStack matrices,
//                                                               MultiBufferSource vertexConsumers,
                                                               int light, LivingEntityRenderer itemRenderer, HumanoidModel<?> model) {
        if (stack.isEmpty()) return;

        matrices.pushPose();

        ModelPart modelPart = model.body;

        // Option 4: Use reflection to retrieve field values ​​(last resort)
        try {
            java.lang.reflect.Field pivotXField = ModelPart.class.getDeclaredField("pivotX");
            pivotXField.setAccessible(true);
            float pivotX = (float) pivotXField.get(modelPart);

            // 对其他字段重复此过程...
            java.lang.reflect.Field pivotYField = ModelPart.class.getDeclaredField("pivotY");
            pivotYField.setAccessible(true);
            float pivotY = (float) pivotYField.get(modelPart);

            java.lang.reflect.Field pivotZField = ModelPart.class.getDeclaredField("pivotZ");
            pivotZField.setAccessible(true);
            float pivotZ = (float) pivotZField.get(modelPart);

            java.lang.reflect.Field pitchField = ModelPart.class.getDeclaredField("pitch");
            pitchField.setAccessible(true);
            float pitch = (float) pitchField.get(modelPart);

            java.lang.reflect.Field yawField = ModelPart.class.getDeclaredField("yaw");
            yawField.setAccessible(true);
            float yaw = (float) yawField.get(modelPart);

            java.lang.reflect.Field rollField = ModelPart.class.getDeclaredField("roll");
            rollField.setAccessible(true);
            float roll = (float) rollField.get(modelPart);

            matrices.translate(pivotX / 16.0F, pivotY / 16.0F, pivotZ / 16.0F);
            matrices.mulPose(Axis.XP.rotation(pitch));
            matrices.mulPose(Axis.YP.rotation(yaw));
            matrices.mulPose(Axis.ZP.rotation(roll));
        } catch (Exception e) {
            // If reflection fails, use the default value.
            matrices.translate(0, 0, 0);
        }

        matrices.translate(0.1F, 0.1F, 0.25F);
        matrices.scale(1.65F, 1.65F, 1.0F);
        matrices.mulPose(Axis.ZP.rotationDegrees(60 + 180));
        matrices.mulPose(Axis.YP.rotationDegrees(-180));

        // Use the correct ItemRenderer.renderItem method signature from Minecraft 1.21.6
        // Revert to a version that does not use the TransformationMode parameter to avoid class not found issues
        //itemRenderer.renderItem(stack, light, OverlayTexture.DEFAULT_UV, matrices, vertexConsumers, (World) entity.getWorld(), 0);
        matrices.popPose();
    }

    public LivingEntityRenderer getItemRenderer() {
        return itemRenderer;
    }

    public void render(PoseStack matrices, SubmitNodeCollector queue, int light, T state, float limbAngle, float limbDistance) {

    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int i, T entityRenderState, float f, float g) {

    }
}