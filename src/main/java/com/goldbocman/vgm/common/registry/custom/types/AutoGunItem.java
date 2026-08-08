package com.goldbocman.vgm.common.registry.custom.types;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import com.goldbocman.vgm.common.utils.GunApiCompat;
import com.goldbocman.vgm.common.utils.GunHelper;

// Simple straight-back kick.
// Reload is a single, slower sequence (not repeated many times a second like firing), so it reuses
// GunItem's shared reload motion (same wobble rifle uses) rather than needing its own variant.
public class AutoGunItem extends GunWithMagItem {
    private static final boolean ANIMATE_RELOAD_IN_THIRD_PERSON = false;

    public AutoGunItem(Properties settings) {
        super(settings);
    }

    public AutoGunItem(Properties settings, TagKey<Item> compatibleMagazines) {
        super(settings, compatibleMagazines);
    }

    @Override
    public boolean applyCustomMatrix(LivingEntity entity, GunHelper.GunStates state, PoseStack matrices, ItemStack stack, float cooldownProgress, boolean leftHanded) {
        if (matrices == null) {
            return false;
        }

        if (state == GunHelper.GunStates.RELOADING) {
            return applyReloadAnimation(entity, matrices, cooldownProgress, leftHanded);
        }

        if (state != GunHelper.GunStates.NORMAL) {
            return false;
        }

        float kick = Math.max(1 - cooldownProgress, 0);
        matrices.translate(0, 0, kick * 0.15F);

        return true;
    }

    // Same reload motion as GunItem.applyCustomMatrix's shared curve
    private boolean applyReloadAnimation(LivingEntity entity, PoseStack matrices, float cooldownProgress, boolean leftHanded) {
        Minecraft client = Minecraft.getInstance();
        boolean isFirstPerson = client.options.getCameraType().isFirstPerson();
        float sinA = (float) Math.sin((cooldownProgress * 2 - 0.5) * Math.PI) * 0.5F + 0.5F;

        if (isFirstPerson) {
            float delta = GunApiCompat.getPartialTick(client);
            double wobble = Math.sin(((float) entity.tickCount + delta) / 2) * sinA * 30;
            float zRotation = (float) ((leftHanded ? -15 : 15) + wobble);
            float xRotation = sinA * 10 * 0.5F;

            matrices.mulPose(Axis.ZP.rotationDegrees(zRotation));
            matrices.mulPose(Axis.XP.rotationDegrees(xRotation));
        } else if (ANIMATE_RELOAD_IN_THIRD_PERSON) {
            float f2 = sinA / 3;
            float yRotation = leftHanded ? 10 : -10;
            float xRotation = f2 * 30 + 45;

            matrices.mulPose(Axis.YP.rotationDegrees(yRotation));
            matrices.mulPose(Axis.XP.rotationDegrees(xRotation));
            matrices.translate(0, -f2 / 4 + 0.25F, f2 / 8 - 0.25F);
        }

        return true;
    }
}
