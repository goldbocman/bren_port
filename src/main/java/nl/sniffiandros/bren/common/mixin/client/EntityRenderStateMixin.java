package nl.sniffiandros.bren.common.mixin.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import nl.sniffiandros.bren.client.IEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Environment(EnvType.CLIENT)
@Mixin(EntityRenderState.class)
public class EntityRenderStateMixin implements IEntityRenderState {

    @Unique
    private int bren$entityId = -1;

    @Override
    public int bren$getEntityId() {
        return this.bren$entityId;
    }

    @Override
    public void bren$setEntityId(int id) {
        this.bren$entityId = id;
    }
}
