package com.goldbocman.vgm.client.renderer;

//? if >=1.21.11 {
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import com.goldbocman.vgm.common.Bren;
import com.goldbocman.vgm.common.entity.BulletEntity;

public class BulletRenderer<T extends BulletEntity> extends EntityRenderer<T, net.minecraft.client.renderer.entity.state.@org.jetbrains.annotations.NotNull EntityRenderState> {

    public static final Identifier BULLET_TEXTURE = Identifier.fromNamespaceAndPath(Bren.MODID, "textures/entity/bullet.png");
    public static final Identifier SHELL_TEXTURE = Identifier.fromNamespaceAndPath(Bren.MODID, "textures/entity/shell.png"); // 新增霰弹纹理

    public BulletRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void submit(net.minecraft.client.renderer.entity.state.EntityRenderState renderState, PoseStack matrices, 
                      SubmitNodeCollector commandQueue, CameraRenderState cameraRenderState) {
        // 在新的渲染系统中，我们暂时使用父类的渲染逻辑
        super.submit(renderState, matrices, commandQueue, cameraRenderState);
    }

    public Identifier getTexture(BulletEntity entity) {
        // 新增：根据子弹类型返回不同纹理
        // 这里可以根据子弹的某些属性来判断，暂时先返回默认纹理
        // 后续可以扩展BulletEntity以支持子弹类型字段
        return BULLET_TEXTURE;
    }

    @Override
    public net.minecraft.client.renderer.entity.state.EntityRenderState createRenderState() {
        return new net.minecraft.client.renderer.entity.state.EntityRenderState();
    }
    
    public void updateRenderState(T entity, net.minecraft.client.renderer.entity.state.EntityRenderState renderState, float tickDelta) {
        super.extractRenderState(entity, renderState, tickDelta);
        // 在这里更新renderState中的数据
    }
}
//?}