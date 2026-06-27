package nl.sniffiandros.bren.common.mixin.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import nl.sniffiandros.bren.client.GunAnimationSystem;
import nl.sniffiandros.bren.client.IEntityRenderState;
import nl.sniffiandros.bren.common.entity.IGunUser;
import nl.sniffiandros.bren.common.registry.custom.types.GunItem;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(value= EnvType.CLIENT)
@Mixin(HumanoidModel.class)
public abstract class BipedEntityModelMixin implements ArmedModel, HeadedModel {

    @Shadow public abstract ModelPart getHead();

    @Shadow @Final public ModelPart leftArm;

    @Shadow @Final public ModelPart rightArm;

    @Shadow @Final public ModelPart hat;

    @Inject(at = @At("RETURN"), method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/HumanoidRenderState;)V")
    private void angles(HumanoidRenderState state, CallbackInfo info) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.level == null) return;

        int entityId = ((IEntityRenderState) state).bren$getEntityId();
        if (entityId == -1) return;

        // Look up the actual entity that owns this render state.
        // For the local player, full gun state is available.
        // For remote players, rotation and inventory are vanilla-synced, so the holding
        // pose and cooldown-based firing animation work correctly out of the box.
        // gunState stays NORMAL for remote players until reload networking is added.
        Entity entity = client.level.getEntity(entityId);
        if (!(entity instanceof Player player) || !(player instanceof IGunUser)) return;

        ItemStack mainHandItem = player.getMainHandItem();
        if (mainHandItem.isEmpty() || !(mainHandItem.getItem() instanceof GunItem)) return;

        GunAnimationSystem.applyGunAnimation(
            this.leftArm, this.rightArm, this.getHead(), this.hat,
            state, player
        );
    }
}