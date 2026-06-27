package nl.sniffiandros.bren.common.mixin.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
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
        if (client.player == null) return;

        // Only animate the local player's own model — not other players' models.
        // We store the entity ID in the render state during extractRenderState so we can identify it here.
        if (((IEntityRenderState) state).bren$getEntityId() != client.player.getId()) return;

        Player player = client.player;
        ItemStack mainHandItem = player.getMainHandItem();

        if (!mainHandItem.isEmpty() && mainHandItem.getItem() instanceof GunItem && player instanceof IGunUser) {
            GunAnimationSystem.applyGunAnimation(
                this.leftArm, this.rightArm, this.getHead(), this.hat,
                state, player
            );
        }
    }
}