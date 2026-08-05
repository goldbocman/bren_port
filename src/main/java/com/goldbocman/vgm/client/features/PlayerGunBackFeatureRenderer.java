package com.goldbocman.vgm.client.features;

import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;

public class PlayerGunBackFeatureRenderer extends GunBackFeatureRenderer<AvatarRenderState, PlayerModel> {

    public PlayerGunBackFeatureRenderer(RenderLayerParent<AvatarRenderState, PlayerModel> context, LivingEntityRenderer itemRenderer) {
        super(context, itemRenderer);
    }
//
//    @Override
//    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, PlayerEntityRenderState state, float limbAngle, float limbDistance, float tickDelta) {
//        super.render(matrices, vertexConsumers, light, state, limbAngle, limbDistance, tickDelta);
//    }
}