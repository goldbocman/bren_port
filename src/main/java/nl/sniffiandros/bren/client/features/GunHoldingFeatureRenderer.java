package nl.sniffiandros.bren.client.features;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import nl.sniffiandros.bren.common.registry.custom.PoseType;
import nl.sniffiandros.bren.common.registry.custom.types.GunItem;
import org.jetbrains.annotations.NotNull;

@Environment(EnvType.CLIENT)
public class GunHoldingFeatureRenderer<S extends HumanoidRenderState, M extends EntityModel<S>> extends RenderLayer<S, M> {
    public GunHoldingFeatureRenderer(LivingEntityRenderer context) {
        super(context);
    }

    @Override
    public void submit(@NotNull PoseStack poseStack, @NotNull SubmitNodeCollector submitNodeCollector, int i, S entityRenderState, float f, float g) {

    }

    public void render(PoseStack matrices, SubmitNodeCollector queue, int light, S state, float limbAngle, float limbDistance) {
        // Now we can access the mainArm field in BipedEntityRenderState
        double target_degree = state.mainArm == HumanoidArm.LEFT ? 135D : 45D;

        // Use reflection to safely access possible mainHandItem fields
        ItemStack s = getMainHandItem(state);

        if (s != null && !s.isEmpty()) {
            if (s.getItem() instanceof GunItem gunItem) {
                if (gunItem.holdingPose() == PoseType.TWO_ARMS) {
                    // Save matrix state before applying transformation
                    matrices.pushPose();
                    matrices.mulPose(Axis.YN.rotation((float) Math.toRadians(target_degree - 90)));
                    // Restore matrix state
                    matrices.popPose();
                }
            }
        }
    }
    /**
     * Securely obtain main hand items via reflection.
     */
    private ItemStack getMainHandItem(HumanoidRenderState state) {
        try {
            // Try accessing different possible field names
            Class<?> clazz = state.getClass();
            while (clazz != null) {
                try {
                    // Try the mainHandItem field
                    java.lang.reflect.Field mainHandItemField = clazz.getDeclaredField("mainHandItem");
                    mainHandItemField.setAccessible(true);
                    return (ItemStack) mainHandItemField.get(state);
                } catch (NoSuchFieldException e1) {
                    try {
                        // Try the mainHandItemState field
                        java.lang.reflect.Field mainHandItemStateField = clazz.getDeclaredField("mainHandItemState");
                        mainHandItemStateField.setAccessible(true);
                        return (ItemStack) mainHandItemStateField.get(state);
                    } catch (NoSuchFieldException e2) {
                        // Continue checking the parent class
                        clazz = clazz.getSuperclass();
                    }
                }
            }
        } catch (Exception ignored) {
            // If all attempts fail, return an empty ItemStack.
        }
        return ItemStack.EMPTY;
    }
}