package nl.sniffiandros.bren.client.features;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.world.entity.LivingEntity;
import nl.sniffiandros.bren.client.GunAnimationSystem;

/**

 * Implement the FeatureRenderer for gun animation in the new rendering system

 */
@Environment(EnvType.CLIENT)
public class GunAnimationFeatureRenderer<T extends HumanoidRenderState, M extends HumanoidModel<T>> 
        extends RenderLayer<T, M> {
    
    private final Minecraft client;
    
    public GunAnimationFeatureRenderer(RenderLayerParent<T, M> context) {
        super(context);
        this.client = Minecraft.getInstance();
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int i, T entityRenderState, float f, float g) {

    }

    public void render(PoseStack matrices, SubmitNodeCollector commandQueue, int light,
                      T state, float limbAngle, float limbDistance) {
        // In the new rendering system, we need to obtain entity information through other means

        // Obtain the currently rendered entity through the client
        if (client.getCameraEntity() instanceof LivingEntity livingEntity) {
            // Apply weapon animation

            // Note: This may need to be adjusted depending on the specific state type.
        }
    }

    /**
     * Static method, for external calls to apply gun animations
     */
    public static <T extends LivingEntity> void applyGunAnimationToModel(HumanoidModel<?> model, T entity) {
        if (entity != null && model != null) {
            GunAnimationSystem.applyGunAnimation(
                model.leftArm, model.rightArm, model.head, 
                null, entity // Temporarily pass null as state, because the animation system needs to be refactored.
            );
        }
    }

    public Minecraft getClient() {
        return client;
    }
}