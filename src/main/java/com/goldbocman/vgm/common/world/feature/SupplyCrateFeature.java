package com.goldbocman.vgm.common.world.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import com.goldbocman.vgm.common.config.MConfig;
import com.goldbocman.vgm.common.config.SupplyCrateConfig;
import com.goldbocman.vgm.common.registry.ItemReg;
import com.goldbocman.vgm.common.registry.custom.MagazineItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SupplyCrateFeature extends Feature<@org.jetbrains.annotations.NotNull NoneFeatureConfiguration> {

    // 枪械类型权重配置（从配置文件加载）
    private static final List<GunEntry> GUN_POOL = new ArrayList<>();
    
    // 弹药类型权重配置（从配置文件加载）
    private static final List<AmmoEntry> AMMO_POOL = new ArrayList<>();
    
    // 弹匣类型权重配置（从配置文件加载）
    private static final List<MagazineEntry> MAGAZINE_POOL = new ArrayList<>();
    
    // 初始化配置池
    public static void initPools() {
        GUN_POOL.clear();
        AMMO_POOL.clear();
        MAGAZINE_POOL.clear();
        
        // 加载枪械配置
        Map<String, SupplyCrateConfig.GunConfig> gunConfigs = SupplyCrateConfig.getGunConfigs();
        if (ItemReg.MACHINE_GUN != null && gunConfigs.containsKey("machine_gun"))
            GUN_POOL.add(new GunEntry(ItemReg.MACHINE_GUN, gunConfigs.get("machine_gun").weight));
        if (ItemReg.AIR_GUN != null && gunConfigs.containsKey("air_gun"))
            GUN_POOL.add(new GunEntry(ItemReg.AIR_GUN, gunConfigs.get("air_gun").weight));
        if (ItemReg.AUTO_GUN != null && gunConfigs.containsKey("auto_gun"))
            GUN_POOL.add(new GunEntry(ItemReg.AUTO_GUN, gunConfigs.get("auto_gun").weight));
        if (ItemReg.RIFLE != null && gunConfigs.containsKey("rifle")) 
            GUN_POOL.add(new GunEntry(ItemReg.RIFLE, gunConfigs.get("rifle").weight));
        if (ItemReg.SHOTGUN != null && gunConfigs.containsKey("shotgun")) 
            GUN_POOL.add(new GunEntry(ItemReg.SHOTGUN, gunConfigs.get("shotgun").weight));
        if (ItemReg.REVOLVER != null && gunConfigs.containsKey("revolver")) 
            GUN_POOL.add(new GunEntry(ItemReg.REVOLVER, gunConfigs.get("revolver").weight));
        if (ItemReg.NETHERITE_MACHINE_GUN != null && gunConfigs.containsKey("netherite_machine_gun")) 
            GUN_POOL.add(new GunEntry(ItemReg.NETHERITE_MACHINE_GUN, gunConfigs.get("netherite_machine_gun").weight));
        if (ItemReg.NETHERITE_AUTO_GUN != null && gunConfigs.containsKey("netherite_auto_gun")) 
            GUN_POOL.add(new GunEntry(ItemReg.NETHERITE_AUTO_GUN, gunConfigs.get("netherite_auto_gun").weight));
        if (ItemReg.NETHERITE_RIFLE != null && gunConfigs.containsKey("netherite_rifle")) 
            GUN_POOL.add(new GunEntry(ItemReg.NETHERITE_RIFLE, gunConfigs.get("netherite_rifle").weight));
        if (ItemReg.NETHERITE_SHOTGUN != null && gunConfigs.containsKey("netherite_shotgun")) 
            GUN_POOL.add(new GunEntry(ItemReg.NETHERITE_SHOTGUN, gunConfigs.get("netherite_shotgun").weight));
        if (ItemReg.NETHERITE_REVOLVER != null && gunConfigs.containsKey("netherite_revolver")) 
            GUN_POOL.add(new GunEntry(ItemReg.NETHERITE_REVOLVER, gunConfigs.get("netherite_revolver").weight));
        if (ItemReg.FLARE_GUN != null && gunConfigs.containsKey("flare_gun")) 
            GUN_POOL.add(new GunEntry(ItemReg.FLARE_GUN, gunConfigs.get("flare_gun").weight));
        if (ItemReg.NETHERITE_DOUBLE_BARRELS_SHOTGUN != null && gunConfigs.containsKey("netherite_double_barrels_shotgun")) 
            GUN_POOL.add(new GunEntry(ItemReg.NETHERITE_DOUBLE_BARRELS_SHOTGUN, gunConfigs.get("netherite_double_barrels_shotgun").weight));
        if (ItemReg.NETHERITE_LEVER_GUN != null && gunConfigs.containsKey("netherite_lever_gun")) 
            GUN_POOL.add(new GunEntry(ItemReg.NETHERITE_LEVER_GUN, gunConfigs.get("netherite_lever_gun").weight));
        if (ItemReg.SMG != null && gunConfigs.containsKey("smg"))
            GUN_POOL.add(new GunEntry(ItemReg.SMG, gunConfigs.get("smg").weight));
        if (ItemReg.GRAPPLING_HOOK != null && gunConfigs.containsKey("grappling_hook"))
            GUN_POOL.add(new GunEntry(ItemReg.GRAPPLING_HOOK, gunConfigs.get("grappling_hook").weight));
        
        // 加载弹药配置
        Map<String, SupplyCrateConfig.AmmoConfig> ammoConfigs = SupplyCrateConfig.getAmmoConfigs();
        if (ItemReg.EXPLOSIVE_SPEAR != null && ammoConfigs.containsKey("explosive_spear")) {
            SupplyCrateConfig.AmmoConfig config = ammoConfigs.get("explosive_spear");
            AMMO_POOL.add(new AmmoEntry(ItemReg.EXPLOSIVE_SPEAR, config.weight, config.minAmount, config.maxAmount));
        }
        if (ItemReg.BULLET != null && ammoConfigs.containsKey("bullet")) {
            SupplyCrateConfig.AmmoConfig config = ammoConfigs.get("bullet");
            AMMO_POOL.add(new AmmoEntry(ItemReg.BULLET, config.weight, config.minAmount, config.maxAmount));
        }
        if (ItemReg.SHELL != null && ammoConfigs.containsKey("shell")) {
            SupplyCrateConfig.AmmoConfig config = ammoConfigs.get("shell");
            AMMO_POOL.add(new AmmoEntry(ItemReg.SHELL, config.weight, config.minAmount, config.maxAmount));
        }
        if (ItemReg.DRAGONBREATH_SHELL != null && ammoConfigs.containsKey("dragonbreath_shell")) {
            SupplyCrateConfig.AmmoConfig config = ammoConfigs.get("dragonbreath_shell");
            AMMO_POOL.add(new AmmoEntry(ItemReg.DRAGONBREATH_SHELL, config.weight, config.minAmount, config.maxAmount));
        }
        
        // 加载弹匣配置
        Map<String, SupplyCrateConfig.MagazineConfig> magazineConfigs = SupplyCrateConfig.getMagazineConfigs();
        if (ItemReg.MAGAZINE != null && magazineConfigs.containsKey("magazine")) {
            SupplyCrateConfig.MagazineConfig config = magazineConfigs.get("magazine");
            MAGAZINE_POOL.add(new MagazineEntry(ItemReg.MAGAZINE, config.weight, config.capacity));
        }
        if (ItemReg.DRUM_MAGAZINE != null && magazineConfigs.containsKey("drum_magazine")) {
            SupplyCrateConfig.MagazineConfig config = magazineConfigs.get("drum_magazine");
            MAGAZINE_POOL.add(new MagazineEntry(ItemReg.DRUM_MAGAZINE, config.weight, config.capacity));
        }
        if (ItemReg.CLOTHED_MAGAZINE != null && magazineConfigs.containsKey("clothed_magazine")) {
            SupplyCrateConfig.MagazineConfig config = magazineConfigs.get("clothed_magazine");
            MAGAZINE_POOL.add(new MagazineEntry(ItemReg.CLOTHED_MAGAZINE, config.weight, config.capacity));
        }
        if (ItemReg.SHORT_MAGAZINE != null && magazineConfigs.containsKey("short_magazine")) {
            SupplyCrateConfig.MagazineConfig config = magazineConfigs.get("short_magazine");
            MAGAZINE_POOL.add(new MagazineEntry(ItemReg.SHORT_MAGAZINE, config.weight, config.capacity));
        }
    }

    public SupplyCrateFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<@org.jetbrains.annotations.NotNull NoneFeatureConfiguration> context) {
        // 检查是否启用补给箱生成
        if (!MConfig.enableSupplyCrates.get()) {
            return false;
        }

        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();

        // 寻找合适的地表位置
        BlockPos surfacePos = findSuitableSurface(level, origin, random);
        if (surfacePos == null) {
            return false;
        }

        // 检查生成条件
        if (!canPlaceCrate(level, surfacePos)) {
            return false;
        }

        // 放置木桶
        BlockState barrelState = Blocks.BARREL.defaultBlockState()
                .setValue(net.minecraft.world.level.block.BarrelBlock.FACING, Direction.Plane.HORIZONTAL.getRandomDirection(random));
        level.setBlock(surfacePos, barrelState, 2);
        
        // 填充物品
        if (level.getBlockEntity(surfacePos) instanceof BarrelBlockEntity barrelEntity) {
            fillBarrel(barrelEntity, random);
            return true;
        }
        
        return false;
    }
    
    private BlockPos findSuitableSurface(WorldGenLevel level, BlockPos origin, RandomSource random) {
        // 在原点周围随机搜索合适位置
        int searchRadius = 16;
        int maxAttempts = 50;
        
        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            int x = origin.getX() + random.nextInt(searchRadius * 2) - searchRadius;
            int z = origin.getZ() + random.nextInt(searchRadius * 2) - searchRadius;
            
            // 寻找地表
            BlockPos pos = new BlockPos(x, level.getHeight(), z);
            //? if >=1.21.11 {
            while (pos.getY() > level.getMinY()) {
            //?} else {
            /*// LevelHeightAccessor.getMinY() is named getMinBuildHeight() pre-1.21.11.
            while (pos.getY() > level.getMinBuildHeight()) {
            *///?}
                pos = pos.below();
                BlockState state = level.getBlockState(pos);
                if (!state.isAir() && state.blocksMotion()) {
                    // 检查上方是否为空气
                    BlockPos abovePos = pos.above();
                    if (level.getBlockState(abovePos).isAir()) {
                        return abovePos;
                    }
                    break;
                }
            }
        }
        
        return null;
    }
    
    private boolean canPlaceCrate(WorldGenLevel level, BlockPos pos) {
        // 检查位置是否合适
        BlockState state = level.getBlockState(pos);
        if (!state.isAir()) {
            return false;
        }
        
        // 检查下方方块是否为固体
        BlockState belowState = level.getBlockState(pos.below());
        if (!belowState.blocksMotion() || belowState.is(Blocks.BEDROCK)) {
            return false;
        }
        
        // 检查周围是否有足够的空间
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos neighborPos = pos.relative(direction);
            BlockState neighborState = level.getBlockState(neighborPos);
            if (!neighborState.isAir() && neighborState.blocksMotion()) {
                return false;
            }
        }
        
        return true;
    }
    
    private void fillBarrel(BarrelBlockEntity barrelEntity, RandomSource random) {
        List<ItemStack> items = new ArrayList<>();
        
        // 添加枪械 (使用配置中的数量范围)
        int gunCount = MConfig.minGunsPerCrate.get() + random.nextInt(MConfig.maxGunsPerCrate.get() - MConfig.minGunsPerCrate.get() + 1);
        for (int i = 0; i < gunCount; i++) {
            ItemStack gun = getRandomGun(random);
            if (!gun.isEmpty()) {
                items.add(gun);
            }
        }
        
        // 添加弹药 (使用配置中的数量范围)
        int ammoTypes = MConfig.minAmmoTypesPerCrate.get() + random.nextInt(MConfig.maxAmmoTypesPerCrate.get() - MConfig.minAmmoTypesPerCrate.get() + 1);
        for (int i = 0; i < ammoTypes; i++) {
            AmmoEntry ammoEntry = getRandomAmmo(random);
            if (ammoEntry != null) {
                ItemStack ammo = new ItemStack(ammoEntry.item);
                ammo.setCount(ammoEntry.minAmount + random.nextInt(ammoEntry.maxAmount - ammoEntry.minAmount + 1));
                items.add(ammo);
            }
        }
        
        // 添加弹匣 (使用配置中的数量范围)
        int magazineCount = MConfig.minMagazinesPerCrate.get() + random.nextInt(MConfig.maxMagazinesPerCrate.get() - MConfig.minMagazinesPerCrate.get() + 1);
        for (int i = 0; i < magazineCount; i++) {
            MagazineEntry magEntry = getRandomMagazine(random);
            if (magEntry != null) {
                ItemStack magazine = new ItemStack(magEntry.item);
                // 随机填充弹匣 (30%-100%)
                int fillAmount = (int)(magEntry.capacity * (0.3 + random.nextDouble() * 0.7));
                MagazineItem.fillMagazine(magazine, fillAmount);
                items.add(magazine);
            }
        }
        
        // 添加一些随机物品增加趣味性
        if (random.nextInt(3) == 0) {
            items.add(new ItemStack(Items.GUNPOWDER, 1 + random.nextInt(5)));
        }
        if (random.nextInt(4) == 0) {
            items.add(new ItemStack(Items.IRON_INGOT, 1 + random.nextInt(3)));
        }
        
        // 将物品放入箱子
        // 使用1.21.11兼容的方式填充箱子
        for (int i = 0; i < Math.min(items.size(), 27); i++) {
            barrelEntity.setItem(i, items.get(i));
        }
    }
    
    private ItemStack getRandomGun(RandomSource random) {
        int totalWeight = GUN_POOL.stream().mapToInt(entry -> entry.weight).sum();
        int randomWeight = random.nextInt(totalWeight);
        
        int currentWeight = 0;
        for (GunEntry entry : GUN_POOL) {
            currentWeight += entry.weight;
            if (randomWeight < currentWeight) {
                return new ItemStack(entry.item);
            }
        }
        
        return ItemStack.EMPTY;
    }
    
    private AmmoEntry getRandomAmmo(RandomSource random) {
        int totalWeight = AMMO_POOL.stream().mapToInt(entry -> entry.weight).sum();
        int randomWeight = random.nextInt(totalWeight);
        
        int currentWeight = 0;
        for (AmmoEntry entry : AMMO_POOL) {
            currentWeight += entry.weight;
            if (randomWeight < currentWeight) {
                return entry;
            }
        }
        
        return null;
    }
    
    private MagazineEntry getRandomMagazine(RandomSource random) {
        int totalWeight = MAGAZINE_POOL.stream().mapToInt(entry -> entry.weight).sum();
        int randomWeight = random.nextInt(totalWeight);
        
        int currentWeight = 0;
        for (MagazineEntry entry : MAGAZINE_POOL) {
            currentWeight += entry.weight;
            if (randomWeight < currentWeight) {
                return entry;
            }
        }
        
        return null;
    }

    /**
     * @param item 使用Object避免编译时依赖问题
     */ // 数据类定义
        private record GunEntry(net.minecraft.world.item.Item item, int weight) {
    }

    private record AmmoEntry(net.minecraft.world.item.Item item, int weight, int minAmount, int maxAmount) {
    }

    private record MagazineEntry(net.minecraft.world.item.Item item, int weight, int capacity) {
    }
}