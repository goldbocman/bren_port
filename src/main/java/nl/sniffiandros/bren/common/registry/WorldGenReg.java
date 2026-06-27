package nl.sniffiandros.bren.common.registry;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import nl.sniffiandros.bren.common.Bren;
import nl.sniffiandros.bren.common.world.feature.SupplyCrateFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorldGenReg {
    private static final Logger LOGGER = LoggerFactory.getLogger(WorldGenReg.class);
    
    // 特征注册
    public static final ResourceKey<Feature<?>> SUPPLY_CRATE_FEATURE_KEY = 
        ResourceKey.create(Registries.FEATURE, net.minecraft.resources.Identifier.fromNamespaceAndPath(Bren.MODID, "supply_crate"));
    
    public static Feature<@org.jetbrains.annotations.NotNull NoneFeatureConfiguration> SUPPLY_CRATE_FEATURE;
    
    // 放置修饰器注册 - 使用数据包中定义的放置特征
    public static final ResourceKey<PlacedFeature> SUPPLY_CRATE_PLACED_KEY = 
        ResourceKey.create(Registries.PLACED_FEATURE, net.minecraft.resources.Identifier.fromNamespaceAndPath(Bren.MODID, "supply_crate"));

    public static void registerFeatures() {
        LOGGER.info("Registering world generation features");
        
        try {
            // 注册补给桶特征
            SUPPLY_CRATE_FEATURE = new SupplyCrateFeature(NoneFeatureConfiguration.CODEC);
            Registry.register(BuiltInRegistries.FEATURE, SUPPLY_CRATE_FEATURE_KEY, SUPPLY_CRATE_FEATURE);
            
            // 初始化补给箱配置池
            SupplyCrateFeature.initPools();
            
            LOGGER.info("§a[WorldGen] Successfully registered supply crate feature: {}", SUPPLY_CRATE_FEATURE_KEY);
        } catch (Exception e) {
            LOGGER.error("Failed to register world generation features", e);
            throw e;
        }
    }
    
    public static void registerPlacements() {
        LOGGER.info("Registering world generation placements");
        
        try {
            // 注意：配置特征和放置特征通过数据包JSON文件注册
            // 这里不需要额外的代码注册
            
            LOGGER.info("Successfully registered supply crate placements");
        } catch (Exception e) {
            LOGGER.error("Failed to register world generation placements", e);
            throw e;
        }
    }
    
    public static void addBiomeModifications() {
        // TODO: BALANCING NEEDED — gun-trader structures (gunpowder_town, abandoned_factory) are disabled
        //  until loot tables, villager trades, and spawn rates are tuned. To re-enable, move
        //  src/main/resources/data/bren/worldgen/structure_set/disabled/*.json back to structure_set/.
        //
        // TODO: BALANCING NEEDED — supply crate spawning is disabled until gun drop rates,
        //  loot pool weights, and ammo quantities are tuned. To re-enable, uncomment the
        //  BiomeModifications.addFeature call below. Loot pools are defined in
        //  SupplyCrateFeature.initPools() and weights come from SupplyCrateConfig.
        //
        // BiomeModifications.addFeature(
        //     BiomeSelectors.tag(BiomeTags.IS_OVERWORLD),
        //     GenerationStep.Decoration.SURFACE_STRUCTURES,
        //     SUPPLY_CRATE_PLACED_KEY
        // );
    }
    
    // 静态初始化方法
    public static void init() {
        registerFeatures();
        registerPlacements();
        addBiomeModifications();
    }
}