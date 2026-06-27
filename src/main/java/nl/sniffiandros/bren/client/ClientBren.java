package nl.sniffiandros.bren.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityRenderLayerRegistrationCallback;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import nl.sniffiandros.bren.client.features.GunBackFeatureRenderer;
import nl.sniffiandros.bren.client.features.GunHoldingFeatureRenderer;
import nl.sniffiandros.bren.client.particle.AirRingParticle;
import nl.sniffiandros.bren.client.particle.CasingParticle;
import nl.sniffiandros.bren.client.particle.MuzzleSmokeParticle;
import nl.sniffiandros.bren.client.renderer.BulletRenderer;
import nl.sniffiandros.bren.client.renderer.RecoilSys;
import nl.sniffiandros.bren.client.renderer.WeaponTickHolder;
import nl.sniffiandros.bren.common.Bren;
import nl.sniffiandros.bren.common.config.MConfig;
import nl.sniffiandros.bren.common.registry.ClientNetworkReg;
import nl.sniffiandros.bren.common.registry.KeyBindingReg;
import nl.sniffiandros.bren.common.registry.ParticleReg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientBren implements ClientModInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger("Bren/ClientBren");

    @Override
    public void onInitializeClient() {
        var particleRegistry = ParticleProviderRegistry.getInstance();

        // 使用通配符类型参数
        particleRegistry.register((ParticleType<ParticleOptions>) ParticleReg.MUZZLE_SMOKE_PARTICLE, MuzzleSmokeParticle.Factory::new);
        particleRegistry.register((ParticleType<ParticleOptions>) ParticleReg.AIR_RING_PARTICLE, AirRingParticle.Factory::new);
        particleRegistry.register((ParticleType<ParticleOptions>) ParticleReg.CASING_PARTICLE, CasingParticle.Factory::new);

        // 使用ClientPlayConnectionEvents.INIT事件来延迟注册网络数据包接收器
        ClientPlayConnectionEvents.INIT.register((handler, client) -> {
            ClientNetworkReg.shootPacket();
            ClientNetworkReg.clientShootPacket();
            ClientNetworkReg.shootAnimationPacket();
            ClientNetworkReg.recoilPacket();
            ClientNetworkReg.shootParticlePacket();
        });

        EntityRendererRegistry.register(Bren.BULLET, BulletRenderer::new);
        KeyBindingReg.reg();
        // registerModelPredicates(); // 暂时禁用模型谓词注册 // 暂时禁用模型谓词注册

        // 注册后坐力系统渲染回调
        RecoilSys.registerRenderCallback();

        // HUD 现在通过 Mixin 实现，不需要在这里注册
        if (MConfig.showAmmoGui.get()) {
            LOGGER.info("Ammo GUI is enabled in config");
        } else {
            LOGGER.info("Ammo GUI is disabled in config");
        }

        // TODO there is only:
        //  auto_gun, bullet, machine_gun, magazine, metal_tube, netherite auto_gun + machine + rifle + revolver + shotgun,
        //  shell, short_magazine, revolver, rifle, shotgun,

        LivingEntityRenderLayerRegistrationCallback.EVENT.register((t, r, e, c) -> {
            // Only register weapon-related renderers for humanoid creatures to avoid type conversion errors

            // The t parameter is EntityType; we need to check if it's a humanoid creature type.
            if (isHumanoidEntityType(t)) {
                if (MConfig.renderGunOnBack.get()) {
                    // GunBackFeatureRenderer requires LivingEntityRenderer, not ItemRenderer.
                    e.register(new GunBackFeatureRenderer(r, (net.minecraft.client.renderer.entity.LivingEntityRenderer) r));
                }

                // Only register weapon-holding pose renderers for humanoid creatures
                // Use primitive types to avoid generic type checking issues
                e.register(new GunHoldingFeatureRenderer(r));
            }
        });

        ClientTickEvents.START_CLIENT_TICK.register(WeaponTickHolder::tick);
        ClientTickEvents.START_CLIENT_TICK.register(RecoilSys::tick);
        
        GrenadeClientHandler.register();
    }

    // 添加一个新的方法来处理模型注册
    public static void registerAllModels() {
        // 这里可以添加额外的模型注册逻辑
    }

    // Check if the entity type is a humanoid creature
    private static boolean isHumanoidEntityType(net.minecraft.world.entity.EntityType<?> entityType) {
        var key = net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(entityType);
        return key != null && "minecraft".equals(key.getNamespace()) && "player".equals(key.getPath());
    }
}