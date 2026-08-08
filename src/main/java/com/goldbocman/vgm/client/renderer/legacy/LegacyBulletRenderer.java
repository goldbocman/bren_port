package com.goldbocman.vgm.client.renderer.legacy;

//? if <1.21.11 {
/*import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;
import com.goldbocman.vgm.common.Bren;
import com.goldbocman.vgm.common.entity.BulletEntity;

// Legacy counterpart of BulletRenderer. The modern render-state EntityRenderer<T, S> doesn't require
// a getTextureLocation() override; the old single-type-parameter EntityRenderer<T> does (it's
// abstract), so this wires BULLET_TEXTURE through it directly instead of leaving it dead like the
// modern file's unused getTexture() helper.
public class LegacyBulletRenderer<T extends BulletEntity> extends EntityRenderer<T> {

    public static final Identifier BULLET_TEXTURE = Identifier.fromNamespaceAndPath(Bren.MODID, "textures/entity/bullet.png");

    public LegacyBulletRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public Identifier getTextureLocation(T entity) {
        return BULLET_TEXTURE;
    }

    @Override
    public void render(T entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int light) {
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, light);
    }
}
*///?}
