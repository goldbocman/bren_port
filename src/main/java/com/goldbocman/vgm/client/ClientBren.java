package com.goldbocman.vgm.client;

//? if fabric {
/*import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
//? if >=1.21.11 {
/^import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityRenderLayerRegistrationCallback;
^///?} else {
// 1.21.1-era fabric-particles-v1 still uses the old ParticleFactoryRegistry name (the newer
// ParticleProviderRegistry didn't exist yet), and fabric-rendering-v1 at this version has no
// generic per-humanoid render-layer registration callback at all - see the onInitializeClient()
// note below for why that's fine here.
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
//?}
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
*///?}
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
//? if >=1.21.11 {
/*import com.goldbocman.vgm.client.features.GunBackFeatureRenderer;
import com.goldbocman.vgm.client.features.GunHoldingFeatureRenderer;
*///?} else {
import com.goldbocman.vgm.client.features.legacy.LegacyGunBackFeatureRenderer;
import com.goldbocman.vgm.client.features.legacy.LegacyGunHoldingFeatureRenderer;
//?}
import com.goldbocman.vgm.client.particle.AirRingParticle;
import com.goldbocman.vgm.client.particle.CasingParticle;
import com.goldbocman.vgm.client.particle.MuzzleSmokeParticle;
//? if >=1.21.11 {
/*import com.goldbocman.vgm.client.renderer.BulletRenderer;
*///?} else {
import com.goldbocman.vgm.client.legacy.LegacyGunHeldModels;
import com.goldbocman.vgm.client.renderer.legacy.LegacyBulletRenderer;
import com.goldbocman.vgm.common.mixin.client.legacy.LegacyItemPropertiesAccessor;
import com.goldbocman.vgm.common.registry.custom.MagazineItem;
import com.goldbocman.vgm.common.registry.custom.types.GunWithMagItem;
import net.minecraft.resources.ResourceLocation;
//?}
//? if <1.21.11
import com.goldbocman.vgm.common.registry.ItemReg;
import com.goldbocman.vgm.client.renderer.RecoilSys;
import com.goldbocman.vgm.client.renderer.WeaponTickHolder;
import com.goldbocman.vgm.common.Bren;
import com.goldbocman.vgm.common.config.MConfig;
import com.goldbocman.vgm.common.registry.ClientNetworkReg;
import com.goldbocman.vgm.common.registry.KeyBindingReg;
import com.goldbocman.vgm.common.registry.ParticleReg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientBren
        //? if fabric
        //implements ClientModInitializer
{
    private static final Logger LOGGER = LoggerFactory.getLogger("Bren/ClientBren");

    //? if fabric
    //@Override
    public void onInitializeClient() {
        //? if fabric {
        /*//? if >=1.21.11 {
        /^var particleRegistry = ParticleProviderRegistry.getInstance();
        ^///?} else {
        var particleRegistry = ParticleFactoryRegistry.getInstance();
        //?}

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
            ClientNetworkReg.gunStatePacket();
        });

        //? if >=1.21.11 {
        /^EntityRendererRegistry.register(Bren.BULLET, BulletRenderer::new);
        ^///?} else {
        EntityRendererRegistry.register(Bren.BULLET, LegacyBulletRenderer::new);
        //?}
        *///?}
        // NeoForge registers particle providers / entity renderer / layers / tick events via the
        // NeoForgeClientModBusEvents / NeoForgeClientGameBusEvents subscribers below instead, and its
        // network payload handlers register once at load time (see NetworkReg), so it doesn't need the
        // Fabric-only calls above.
        KeyBindingReg.reg();
        //? if <1.21.11
        registerLegacyMagazineModelPredicates();
        //? if <1.21.11
        registerLegacyMagazineFullnessModelPredicates();
        //? if <1.21.11 {
        //? if fabric
        //registerLegacyGunHeldModels();
        //?}

        // 注册后坐力系统渲染回调
        RecoilSys.registerRenderCallback();

        // HUD 现在通过 Mixin 实现，不需要在这里注册
        if (MConfig.showAmmoGui.get()) {
            LOGGER.info("Ammo GUI is enabled in config");
        } else {
            LOGGER.info("Ammo GUI is disabled in config");
        }

        // Both GunBackFeatureRenderer and GunHoldingFeatureRenderer are already no-op stubs on every
        // version (confirmed: their render()/submit() overrides are empty) - see LegacyGunBackFeatureRenderer/
        // LegacyGunHoldingFeatureRenderer's own comments. 1.21.1-era fabric-rendering-v1 also has no
        // LivingEntityRenderLayerRegistrationCallback equivalent to register them through at all, so
        // this whole block is skipped for <1.21.11 with no behavior loss.
        //? if fabric {
        /*//? if >=1.21.11 {
        /^LivingEntityRenderLayerRegistrationCallback.EVENT.register((t, r, e, c) -> {
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
        ^///?}
        *///?}

        //? if fabric {
        /*ClientTickEvents.START_CLIENT_TICK.register(WeaponTickHolder::tick);
        ClientTickEvents.START_CLIENT_TICK.register(RecoilSys::tick);
        *///?}

        GrenadeClientHandler.register();
    }

    // 添加一个新的方法来处理模型注册
    public static void registerAllModels() {
        // 这里可以添加额外的模型注册逻辑
    }

    // 1.21.1 predates the Item Model Definition rework, so the vgm:has_magazine state the modern
    // items/<gun>.json select-tree branches on is invisible there - the old-format models/item/<gun>.json
    // "overrides" array needs a real Java-registered ItemProperties predicate of the same name to ever
    // fire (see claude/gun-rendering.md). Reuses GunWithMagItem.hasMagazine(stack), the same source of
    // truth the DataComponent path already uses, so both rendering systems stay consistent.
    //? if <1.21.11 {
    private static void registerLegacyMagazineModelPredicates() {
        var hasMagazine = new net.minecraft.client.renderer.item.ClampedItemPropertyFunction() {
            @Override
            public float unclampedCall(net.minecraft.world.item.ItemStack stack, net.minecraft.client.multiplayer.ClientLevel level, net.minecraft.world.entity.LivingEntity entity, int seed) {
                return GunWithMagItem.hasMagazine(stack) ? 1.0F : 0.0F;
            }
        };
        var id = com.goldbocman.vgm.TemplateModCommon.id(Bren.MODID, "has_magazine");
        for (var item : new net.minecraft.world.item.Item[] { ItemReg.RIFLE, ItemReg.NETHERITE_RIFLE, ItemReg.AUTO_GUN, ItemReg.NETHERITE_AUTO_GUN }) {
            LegacyItemPropertiesAccessor.invokeRegister(item, id, hasMagazine);
        }
    }

    // Same root cause as registerLegacyMagazineModelPredicates above, but for the standalone
    // magazine items' own full/empty texture: models/item/magazine.json and short_magazine.json's
    // "overrides" arrays predicate on a custom "has_ammo" key, which is equally dead on 1.21.1
    // without a real Java-registered ItemProperties predicate. Reuses MagazineItem.isEmpty(stack),
    // the same source of truth the modern vgm:has_ammo DataComponent path already uses.
    private static void registerLegacyMagazineFullnessModelPredicates() {
        var hasAmmo = new net.minecraft.client.renderer.item.ClampedItemPropertyFunction() {
            @Override
            public float unclampedCall(net.minecraft.world.item.ItemStack stack, net.minecraft.client.multiplayer.ClientLevel level, net.minecraft.world.entity.LivingEntity entity, int seed) {
                return !MagazineItem.isEmpty(stack) ? 1.0F : 0.0F;
            }
        };
        var id = com.goldbocman.vgm.TemplateModCommon.id(Bren.MODID, "has_ammo");
        for (var item : new net.minecraft.world.item.Item[] { ItemReg.MAGAZINE, ItemReg.SHORT_MAGAZINE }) {
            LegacyItemPropertiesAccessor.invokeRegister(item, id, hasAmmo);
        }
    }
    //?}

    // Bakes <gun>_held.json/<gun>_with_magazine.json (see LegacyGunHeldModels) as extra models so
    // LegacyItemInHandRendererMixin can retrieve and substitute them for hand display contexts -
    // vanilla only auto-bakes models actually referenced by a registered item/blockstate, and these
    // aren't (models/item/<gun>.json - the item's real registered model - stays on the 16x16 GUI
    // texture, see claude/gun-rendering.md). NeoForge's equivalent is
    // NeoForgeClientModBusEvents.onRegisterAdditionalModels below.
    //? if <1.21.11 {
    //? if fabric {
    /*private static void registerLegacyGunHeldModels() {
        net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin.register(context ->
                context.addModels(LegacyGunHeldModels.allModelIds().toArray(new ResourceLocation[0])));
    }
    *///?}
    //?}

    // Check if the entity type is a humanoid creature
    //? if fabric {
    /*private static boolean isHumanoidEntityType(net.minecraft.world.entity.EntityType<?> entityType) {
        var key = net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(entityType);
        return key != null && "minecraft".equals(key.getNamespace()) && "player".equals(key.getPath());
    }
    *///?}

    //? if neoforge {
    /*//? if >=1.21.9 {
    /^@net.neoforged.fml.common.EventBusSubscriber(modid = Bren.MODID, value = net.neoforged.api.distmarker.Dist.CLIENT)
    ^///?} else {
    @net.neoforged.fml.common.EventBusSubscriber(modid = Bren.MODID, value = net.neoforged.api.distmarker.Dist.CLIENT, bus = net.neoforged.fml.common.EventBusSubscriber.Bus.MOD)
    //?}
    public static class NeoForgeClientModBusEvents {
        @net.neoforged.bus.api.SubscribeEvent
        @SuppressWarnings("unchecked")
        public static void registerParticleProviders(net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent event) {
            event.registerSpriteSet((ParticleType<ParticleOptions>) ParticleReg.MUZZLE_SMOKE_PARTICLE, MuzzleSmokeParticle.Factory::new);
            event.registerSpriteSet((ParticleType<ParticleOptions>) ParticleReg.AIR_RING_PARTICLE, AirRingParticle.Factory::new);
            event.registerSpriteSet((ParticleType<ParticleOptions>) ParticleReg.CASING_PARTICLE, CasingParticle.Factory::new);
        }

        //? if <1.21.11 {
        @net.neoforged.bus.api.SubscribeEvent
        public static void onClientSetup(net.neoforged.fml.event.lifecycle.FMLClientSetupEvent event) {
            registerLegacyMagazineModelPredicates();
            registerLegacyMagazineFullnessModelPredicates();
        }

        // Fabric's equivalent is ClientBren.registerLegacyGunHeldModels() above, called from
        // onInitializeClient() - see LegacyGunHeldModels for what/why. RegisterAdditional fires on
        // every resource reload, not just startup, so this stays correct across F3+T too.
        //
        // Must use the "standalone" variant, not inventory() - ModelEvent.RegisterAdditional.register
        // hard-requires it for side-loaded models (confirmed via javap: throws
        // IllegalArgumentException("Side-loaded models must use the 'standalone' variant") otherwise;
        // there's no dedicated standalone() factory on ModelResourceLocation, so its public
        // (id, variant) constructor is used directly). LegacyGunHeldModels.getBakedModel's NeoForge
        // branch must construct the exact same ModelResourceLocation to retrieve this back out.
        @net.neoforged.bus.api.SubscribeEvent
        public static void onRegisterAdditionalModels(net.neoforged.neoforge.client.event.ModelEvent.RegisterAdditional event) {
            for (ResourceLocation id : LegacyGunHeldModels.allModelIds()) {
                event.register(new net.minecraft.client.resources.model.ModelResourceLocation(id, "standalone"));
            }
        }
        //?}

        @net.neoforged.bus.api.SubscribeEvent
        public static void registerRenderers(net.neoforged.neoforge.client.event.EntityRenderersEvent.RegisterRenderers event) {
            //? if >=1.21.11 {
            /^event.registerEntityRenderer(Bren.BULLET, BulletRenderer::new);
            ^///?} else {
            event.registerEntityRenderer(Bren.BULLET, LegacyBulletRenderer::new);
            //?}
        }

        // GunBackFeatureRenderer/GunHoldingFeatureRenderer are both confirmed no-op stubs (see their
        // own file comments) - on 1.21.1, LivingEntityRenderer.addLayer(...) is also `protected`, not
        // callable from here at all, so this whole subscriber is skipped there with no behavior loss.
        //? if >=1.21.11 {
        /^@net.neoforged.bus.api.SubscribeEvent
        public static void addLayers(net.neoforged.neoforge.client.event.EntityRenderersEvent.AddLayers event) {
            for (net.minecraft.world.entity.player.PlayerModelType skin : event.getSkins()) {
                var renderer = event.<net.minecraft.client.renderer.entity.player.AvatarRenderer<net.minecraft.client.player.AbstractClientPlayer>>getPlayerRenderer(skin);
                if (MConfig.renderGunOnBack.get()) {
                    renderer.addLayer(new GunBackFeatureRenderer(renderer, renderer));
                }
                renderer.addLayer(new GunHoldingFeatureRenderer(renderer));
            }
        }
        ^///?}
    }

    // Game-bus subscriber (ClientTickEvent) - Bus.GAME is the default on every version, no gating needed.
    @net.neoforged.fml.common.EventBusSubscriber(modid = Bren.MODID, value = net.neoforged.api.distmarker.Dist.CLIENT)
    public static class NeoForgeClientGameBusEvents {
        @net.neoforged.bus.api.SubscribeEvent
        public static void onClientTick(net.neoforged.neoforge.client.event.ClientTickEvent.Pre event) {
            WeaponTickHolder.tick(net.minecraft.client.Minecraft.getInstance());
            RecoilSys.tick(net.minecraft.client.Minecraft.getInstance());
        }
    }
    *///?}

    // mirrors NeoForge's
    // <1.21.11 branches unconditionally - no >=1.21.11 AddLayers/BulletRenderer split needed here yet.
    //? if forge {
    @net.minecraftforge.fml.common.Mod.EventBusSubscriber(modid = Bren.MODID, value = net.minecraftforge.api.distmarker.Dist.CLIENT, bus = net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus.MOD)
    public static class ForgeClientModBusEvents {
        @net.minecraftforge.eventbus.api.SubscribeEvent
        @SuppressWarnings("unchecked")
        public static void registerParticleProviders(net.minecraftforge.client.event.RegisterParticleProvidersEvent event) {
            event.registerSpriteSet((ParticleType<ParticleOptions>) ParticleReg.MUZZLE_SMOKE_PARTICLE, MuzzleSmokeParticle.Factory::new);
            event.registerSpriteSet((ParticleType<ParticleOptions>) ParticleReg.AIR_RING_PARTICLE, AirRingParticle.Factory::new);
            event.registerSpriteSet((ParticleType<ParticleOptions>) ParticleReg.CASING_PARTICLE, CasingParticle.Factory::new);
        }

        @net.minecraftforge.eventbus.api.SubscribeEvent
        public static void onClientSetup(net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent event) {
            registerLegacyMagazineModelPredicates();
            registerLegacyMagazineFullnessModelPredicates();
        }

        // Fabric's equivalent is ClientBren.registerLegacyGunHeldModels() above; NeoForge's is
        // NeoForgeClientModBusEvents.onRegisterAdditionalModels - see LegacyGunHeldModels for what/why.
        //
        // >=1.20.5 must use the "standalone" variant, matching NeoForge's own hard requirement for
        // side-loaded models (same underlying vanilla ModelManager mechanism).
        //
        // <1.20.5 (1.20.1) must register the PLAIN ResourceLocation instead - confirmed via javap
        // against the real forge-1.20.1-47.4.22 jar: ModelBakery.loadModel(ResourceLocation) only
        // takes the "inventory"-suffixed item-model path if the location IS a ModelResourceLocation
        // AND its variant literally equals "inventory"; any other variant (e.g. "standalone") falls
        // through to the BLOCK STATE loader instead, which looked for a nonexistent
        // `blockstates/item/<name>.json` and silently produced the missing-model fallback (a
        // pink/black checkerboard on the held item) - this is what the reported bug actually was. A
        // plain (non-MRL) ResourceLocation instead takes loadModel's other branch, which loads
        // `models/<path>.json` directly - exactly the `assets/vgm/models/item/<name>_held.json` files
        // these ids already point at. LegacyGunHeldModels.getBakedModel's forge branch must retrieve
        // with the exact same object shape (plain ResourceLocation, not wrapped in a
        // ModelResourceLocation) since ModelManager's bakedRegistry is a plain hash map and a
        // ModelResourceLocation key hashes differently than a same-namespace/path plain
        // ResourceLocation query.
        @net.minecraftforge.eventbus.api.SubscribeEvent
        public static void onRegisterAdditionalModels(net.minecraftforge.client.event.ModelEvent.RegisterAdditional event) {
            for (ResourceLocation id : LegacyGunHeldModels.allModelIds()) {
                //? if >=1.20.5 {
                /*event.register(new net.minecraft.client.resources.model.ModelResourceLocation(id, "standalone"));
                *///?} else {
                event.register(id);
                //?}
            }
        }

        @net.minecraftforge.eventbus.api.SubscribeEvent
        public static void registerRenderers(net.minecraftforge.client.event.EntityRenderersEvent.RegisterRenderers event) {
            event.registerEntityRenderer(Bren.BULLET, LegacyBulletRenderer::new);
        }

        // GunBackFeatureRenderer/GunHoldingFeatureRenderer registration (AddLayers) is >=1.21.11 only
        // on every other loader too - see NeoForgeClientModBusEvents.addLayers above - so there's
        // nothing to add here for the 1.21.1-only Forge target.

        //? if <1.20.5 {
        // net.minecraftforge.client.gui.overlay.ForgeGui (what Minecraft.gui actually is on Forge
        // at runtime - confirmed via javap: `class ForgeGui extends Gui`) completely overrides
        // render(GuiGraphics, float) without ever calling super.render(...), replacing it with its
        // own overlay dispatch fed by this event instead - confirmed empirically that
        // Legacy1201GuiMixin's injection into the base Gui.render(...) never fires on Forge (zero
        // log output from a temporary diagnostic, even its unconditional first line). This is the
        // same mechanism real Forge gun mods use for their HUD (e.g. MCModderAnchor/TACZ's
        // GunHudOverlay via RegisterGuiOverlaysEvent) - only needed at 1.20.1 since 1.21.1-forge's
        // HUD already works fine via the Mixin (confirmed working in-game, see claude/forge-port.md).
        @net.minecraftforge.eventbus.api.SubscribeEvent
        public static void onRegisterGuiOverlays(net.minecraftforge.client.event.RegisterGuiOverlaysEvent event) {
            event.registerAboveAll("vgm_ammo_hud", (gui, guiGraphics, partialTick, screenWidth, screenHeight) -> {
                net.minecraft.client.Minecraft minecraft = net.minecraft.client.Minecraft.getInstance();
                if (minecraft.player == null) {
                    return;
                }

                net.minecraft.world.entity.player.Player player = minecraft.player;
                net.minecraft.world.item.ItemStack gun = player.getMainHandItem();

                if (!(gun.getItem() instanceof com.goldbocman.vgm.common.registry.custom.types.GunItem gunItem)) {
                    return;
                }

                int i = gunItem.getContents(gun);
                int max = gunItem.getMaxCapacity(gun);

                if (gunItem instanceof com.goldbocman.vgm.common.registry.custom.types.GunWithMagItem
                        && !com.goldbocman.vgm.common.registry.custom.types.GunWithMagItem.hasMagazine(gun)) {
                    return;
                }

                new com.goldbocman.vgm.client.legacy.LegacyHudOverlay().renderWithTexture(guiGraphics, i, max, gunItem.ammoIconOffset());
            });
        }
        //?}
    }

    // Game-bus subscriber (ClientTickEvent) - Bus.FORGE is the default game-side bus on Forge.
    @net.minecraftforge.fml.common.Mod.EventBusSubscriber(modid = Bren.MODID, value = net.minecraftforge.api.distmarker.Dist.CLIENT)
    public static class ForgeClientGameBusEvents {
        @net.minecraftforge.eventbus.api.SubscribeEvent
        //? if >=1.20.5 {
        /*public static void onClientTick(net.minecraftforge.event.TickEvent.ClientTickEvent.Pre event) {
            WeaponTickHolder.tick(net.minecraft.client.Minecraft.getInstance());
            RecoilSys.tick(net.minecraft.client.Minecraft.getInstance());
        }
        *///?} else {
        // 1.20.1's Forge has no ClientTickEvent.Pre/Post split yet - just one class with a
        // Phase field (confirmed via javap against forge-1.20.1-47.4.22) - START mirrors .Pre.
        public static void onClientTick(net.minecraftforge.event.TickEvent.ClientTickEvent event) {
            if (event.phase == net.minecraftforge.event.TickEvent.Phase.START) {
                WeaponTickHolder.tick(net.minecraft.client.Minecraft.getInstance());
                RecoilSys.tick(net.minecraft.client.Minecraft.getInstance());
            }
        }
        //?}
    }
    //?}
}
