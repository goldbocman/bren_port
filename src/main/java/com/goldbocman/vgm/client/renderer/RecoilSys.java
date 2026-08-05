package com.goldbocman.vgm.client.renderer;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

public class RecoilSys {

    private static float cameraRecoil = 0;
    private static float sideRecoil = 0;
    private static float recoil = 0;
    private static int cameraRecoilProgress = 0;
    private static int lastCameraRecoilProgress = 0;
    
    // 添加一个常量来控制枪口上抬的额外角度
    private static final float MUZZLE_LIFT_MULTIPLIER = 1.2f; // 增加20%的上抬角度

    public static void shotEvent(Player player, float cam_recoil) {
        cameraRecoil = cam_recoil;
        sideRecoil = (player.getRandom().nextFloat() - .5F) / 2;
        cameraRecoilProgress = 2;
        recoil = 0;
    }

    public static void render(Minecraft client, float tickDelta) {
        Player player = client.player;

        if (player == null) { return;}

        // 使用传入的tickDelta参数
        float progress = Mth.lerp(tickDelta, (float)lastCameraRecoilProgress, (float)cameraRecoilProgress);

        float pitch = player.getXRot();
        float yaw = player.getYRot();

        // 使用传入的tickDelta参数，并增加枪口上抬角度
        recoil = progress * cameraRecoil * tickDelta * MUZZLE_LIFT_MULTIPLIER;

        player.setXRot(pitch - (Float.isNaN(recoil) ? .0F : recoil));
        player.setYRot(yaw - (Float.isNaN(recoil * sideRecoil) ? .0F : recoil * sideRecoil));
        // 在Minecraft 1.21.6中，prevPitch字段已被移除，游戏引擎会自动处理视角平滑过渡
        // 移除对prevPitch的直接访问：player.prevPitch = pitch;
    }

    public static void tick(Minecraft client) {
        lastCameraRecoilProgress = cameraRecoilProgress;
        cameraRecoilProgress = Math.max(0, --cameraRecoilProgress);
        
        // 在tick中应用后坐力效果
        render(client, 1.0f);
    }

    public static void registerRenderCallback() {
        // 不需要注册HUD回调，后坐力效果在tick中应用
    }
}