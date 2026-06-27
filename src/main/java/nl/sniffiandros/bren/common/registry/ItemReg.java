package nl.sniffiandros.bren.common.registry;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import nl.sniffiandros.bren.common.Bren;
import nl.sniffiandros.bren.common.config.MConfig;
import nl.sniffiandros.bren.common.registry.custom.ColorableMagazineItem;
import nl.sniffiandros.bren.common.registry.custom.types.*;
import nl.sniffiandros.bren.common.registry.custom.MagazineItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ItemReg {
    // 添加日志记录器
    private static final Logger LOGGER = LoggerFactory.getLogger(ItemReg.class);

    // Machine Gun
    public static final float MACHINE_GUN_RECOIL = 9f;
    public static final float MACHINE_GUN_DAMAGE = MConfig.machineGunDamage.get();
    public static final float N_MACHINE_GUN_DAMAGE = MConfig.netheriteMachineGunDamage.get();

    // Auto-Gun
    public static final float AUTO_GUN_RECOIL = 12f;
    public static final float AUTO_GUN_DAMAGE = MConfig.autoGunDamage.get();
    public static final float N_AUTO_GUN_DAMAGE = MConfig.netheriteAutoGunDamage.get();

    // Rifle
    public static final float RIFLE_RECOIL = 22f;
    public static final float RIFLE_DAMAGE = MConfig.rifleDamage.get();
    public static final float N_RIFLE_DAMAGE = MConfig.netheriteRifleDamage.get();

    // Shotgun
    public static final float SHOTGUN_RECOIL = 25f;
    public static final float SHOTGUN_DAMAGE = MConfig.shotgunDamage.get();
    public static final float N_SHOTGUN_DAMAGE = MConfig.netheriteShotgunDamage.get();

    // Revolver
    public static final float REVOLVER_RECOIL = 15f;
    public static final float REVOLVER_DAMAGE = MConfig.revolverDamage.get();
    public static final float N_REVOLVER_DAMAGE = MConfig.netheriteRevolverDamage.get();
    public static Item AUTO_PISTOL;

    // 将物品字段声明为null，在reg()方法中初始化
    public static Item MACHINE_GUN;
    public static Item NETHERITE_MACHINE_GUN;
    public static Item AUTO_GUN;
    public static Item NETHERITE_AUTO_GUN;
    public static Item RIFLE;
    public static Item NETHERITE_RIFLE;
    public static Item SHOTGUN;
    public static Item NETHERITE_SHOTGUN;
    public static Item REVOLVER;
    public static Item NETHERITE_REVOLVER;
    public static Item MAGAZINE;
    public static Item DRUM_MAGAZINE;
    public static Item CLOTHED_MAGAZINE;
    public static Item SHORT_MAGAZINE;
    public static Item BULLET;
    public static Item SHELL;
    public static Item AUTO_LOADER_CONTRAPTION;
    public static Item METAL_TUBE;
    public static Item NETHERITE_DOUBLE_BARRELS_SHOTGUN;
    public static Item DRAGONBREATH_SHELL;
    public static Item NETHERITE_LEVER_GUN;
    public static Item FLARE_GUN;
    public static Item FIRE_AXE;
    public static Item EXPLOSIVE_SPEAR;
    public static Item AIR_GUN;
    public static Item SMG;
    public static Item AR_15;
    public static Item GRAPPLING_HOOK;
    public static Item GRENADE;
    public static Item NETHERITE_TACTICAL_AUTO_GUN;
    public static Item NETHERITE_BIG_BORE_REVOLVER;
    private static final float EXPLOSIVE_SPEAR_POWER = 3F;
    private static final float GRENADE_POWER = 4F;
    private static final int GRENADE_FUSE_TIME = 40; // 2 seconds (20 ticks = 1 second)




    // 适配新版本的注册方法，正确设置模型和翻译键
    // 修复注册方法，正确设置RegistryKey
    public static Item register(String path, java.util.function.Function<Item.Properties, Item> factory, Item.Properties settings) {
        final ResourceKey<Item> registryKey = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(Bren.MODID, path));
        // 只设置RegistryKey，不重复设置模型和翻译组件
        settings = settings.setId(registryKey);
        return Registry.register(BuiltInRegistries.ITEM, registryKey, factory.apply(settings));
    }

    // 简化注册方法，直接使用1.21.4+标准模式
    private static Item registerItem(String name, Item.Properties settings) {
        try {
            // 直接使用标准注册模式
            Item registeredItem = register(name, Item::new, settings);
            return registeredItem;
        } catch (Exception e) {
            throw e;
        }
    }

    // 简化自定义物品注册方法
    private static Item registerCustomItem(String name, java.util.function.Function<Item.Properties, Item> factory, Item.Properties settings) {
        try {
            // 直接使用标准注册模式
            return register(name, factory, settings);
        } catch (Exception e) {
            throw e;
        }
    }

    // 简化枪械物品注册方法
    private static Item registerGunItem(String name, java.util.function.Function<Item.Properties, Item> factory, GunProperties properties) {
        LOGGER.info("Registering gun item with properties: {}", name);
        try {
            // 直接使用标准注册模式
            Item registeredItem = register(name, factory, new Item.Properties().stacksTo(1));
            
            // 立即注册枪械属性
            GunItem.registerGunProperties(Identifier.fromNamespaceAndPath(Bren.MODID, name), properties);
            
            return registeredItem;
        } catch (Exception e) {
            throw e;
        }
    }

    public static void reg(){
        LOGGER.info("Starting item registration process");

        try {
            // 第一步：注册所有基础物品 - 使用1.21.4+标准模式
            LOGGER.info("Registering basic items");
            BULLET = registerItem("bullet", new Item.Properties().stacksTo(64));
            SHELL = registerItem("shell", new Item.Properties().stacksTo(64));
            // TODO: BALANCING NEEDED — disabled until ammo types are tuned
            // DRAGONBREATH_SHELL = registerItem("dragonbreath_shell", new Item.Properties().stacksTo(64));
            AUTO_LOADER_CONTRAPTION = registerItem("auto_loader_contraption", new Item.Properties().stacksTo(64));
            METAL_TUBE = registerItem("metal_tube", new Item.Properties().stacksTo(64));

            // 注册弹匣物品 - 使用1.21.4+标准模式
            LOGGER.info("Registering magazine items");
            // TODO: BALANCING NEEDED — high-capacity drum magazine disabled
            // DRUM_MAGAZINE = registerCustomItem("drum_magazine", s -> new MagazineItem(s, 120), new Item.Properties().stacksTo(1));
            MAGAZINE = registerCustomItem("magazine", s -> new MagazineItem(s, 20), new Item.Properties().stacksTo(1));
            // TODO: BALANCING NEEDED — clothed magazine disabled
            // CLOTHED_MAGAZINE = registerCustomItem("clothed_magazine", s -> new ColorableMagazineItem(s, 50), new Item.Properties().stacksTo(1));
            SHORT_MAGAZINE = registerCustomItem("short_magazine", s -> new MagazineItem(s, 6), new Item.Properties().stacksTo(1));
            // TODO: BALANCING NEEDED — melee items disabled
            // FIRE_AXE = registerCustomItem("fire_axe", FireAxeItem::new, new Item.Properties().stacksTo(1));

            // TODO: BALANCING NEEDED — utility items disabled
            // GRAPPLING_HOOK = registerCustomItem("grappling_hook",
            //     nl.sniffiandros.bren.common.registry.custom.types.GrapplingHookItem::new,
            //     new Item.Properties().stacksTo(1));

            // 第二步：注册枪械物品并立即注册属性 - 使用1.21.4+标准模式
            LOGGER.info("Registering gun items with properties");

            // TODO: BALANCING NEEDED — lunge mine disabled until throwable mechanics are tuned
            // EXPLOSIVE_SPEAR = registerCustomItem("lunge_mine",
            //         s -> new ExplosiveSpearItem(VanillaToolMaterials.IRON, EXPLOSIVE_SPEAR_POWER, s),
            //         new Item.Properties().spear(
            //                 VanillaToolMaterials.IRON,
            //                 0.5f, 0.0f, 0.5f, 8.5f, 2.5f, 2.8f, 2.0f, 2.3f, 1.5f
            //         )
            // );

            // TODO: BALANCING NEEDED — machine gun disabled until fire rate and ammo economy are tuned
            // MACHINE_GUN = registerGunItem("machine_gun", MachineGunItem::new,
            //     new GunProperties().rangedDamage(MACHINE_GUN_DAMAGE).fireRate(3).recoil(MACHINE_GUN_RECOIL)
            //         .shootSound(SoundReg.ITEM_MACHINE_GUN_SHOOT, SoundReg.ITEM_MACHINE_GUN_SHOOT_SILENCED));

            // TODO: BALANCING NEEDED — netherite machine gun disabled
            // NETHERITE_MACHINE_GUN = registerGunItem("netherite_machine_gun", MachineGunItem::new,
            //     new GunProperties().rangedDamage(N_MACHINE_GUN_DAMAGE).fireRate(3).recoil(MACHINE_GUN_RECOIL)
            //         .shootSound(SoundReg.ITEM_MACHINE_GUN_SHOOT, SoundReg.ITEM_MACHINE_GUN_SHOOT_SILENCED));

            // 自动枪
            AUTO_GUN = registerGunItem("auto_gun", GunWithMagItem::new,
                new GunProperties().rangedDamage(AUTO_GUN_DAMAGE).fireRate(5).recoil(AUTO_GUN_RECOIL)
                    .shootSound(SoundReg.ITEM_AUTO_GUN_SHOOT, SoundReg.ITEM_AUTO_GUN_SHOOT_SILENCED));

            // TODO: BALANCING NEEDED — SMG disabled until fire rate is differentiated from auto gun
            // SMG = registerGunItem("smg", GunWithMagItem::new,
            //         new GunProperties().rangedDamage(AUTO_GUN_DAMAGE).fireRate(2).recoil(AUTO_GUN_RECOIL)
            //                 .shootSound(SoundReg.ITEM_AUTO_GUN_SHOOT, SoundReg.ITEM_AUTO_GUN_SHOOT_SILENCED));

            NETHERITE_AUTO_GUN = registerGunItem("netherite_auto_gun", GunWithMagItem::new,
                new GunProperties().rangedDamage(N_AUTO_GUN_DAMAGE).fireRate(4).recoil(AUTO_GUN_RECOIL)
                    .shootSound(SoundReg.ITEM_AUTO_GUN_SHOOT, SoundReg.ITEM_AUTO_GUN_SHOOT_SILENCED));

            // TODO: BALANCING NEEDED — tactical auto gun disabled until unique role is defined
            // NETHERITE_TACTICAL_AUTO_GUN = registerGunItem("netherite_tactical_auto_gun", GunWithMagItem::new,
            //         new GunProperties().rangedDamage(N_AUTO_GUN_DAMAGE).fireRate(2).recoil(AUTO_GUN_RECOIL)
            //                 .shootSound(SoundReg.ITEM_AUTO_GUN_SHOOT, SoundReg.ITEM_AUTO_GUN_SHOOT_SILENCED));

            // TODO: BALANCING NEEDED — air gun disabled
            // AIR_GUN = registerGunItem("air_gun", AirGunItem::new,
            //         new GunProperties().rangedDamage(7f).fireRate(6).recoil(MACHINE_GUN_RECOIL)
            //                 .shootSound(SoundReg.ITEM_MACHINE_GUN_SHOOT, SoundReg.ITEM_MACHINE_GUN_SHOOT_SILENCED));

            // 步枪 - 使用SHORT_MAGAZINES标签
            RIFLE = registerGunItem("rifle", s -> new GunWithMagItem(s, TagReg.SHORT_MAGAZINES),
                new GunProperties().rangedDamage(RIFLE_DAMAGE).fireRate(20).recoil(RIFLE_RECOIL)
                    .shootSound(SoundReg.ITEM_RIFLE_SHOOT, SoundReg.ITEM_RIFLE_SHOOT_SILENCED));

            NETHERITE_RIFLE = registerGunItem("netherite_rifle", s -> new GunWithMagItem(s, TagReg.SHORT_MAGAZINES),
                new GunProperties().rangedDamage(N_RIFLE_DAMAGE).fireRate(20).recoil(RIFLE_RECOIL)
                    .shootSound(SoundReg.ITEM_RIFLE_SHOOT, SoundReg.ITEM_RIFLE_SHOOT_SILENCED));

            // TODO: BALANCING NEEDED — lever gun disabled until ammo type is assigned
            // NETHERITE_LEVER_GUN = registerGunItem("netherite_lever_gun", LeverGunItem::new,
            //         new GunProperties().rangedDamage(12F).fireRate(8).recoil(SHOTGUN_RECOIL)
            //                 .shootSound(SoundReg.ITEM_SHOTGUN_SHOOT, null));

            // 霰弹枪
            SHOTGUN = registerGunItem("shotgun", ShotgunItem::new,
                new GunProperties().rangedDamage(SHOTGUN_DAMAGE).fireRate(20).recoil(SHOTGUN_RECOIL)
                    .shootSound(SoundReg.ITEM_SHOTGUN_SHOOT, null));

            NETHERITE_SHOTGUN = registerGunItem("netherite_shotgun", ShotgunItem::new,
                    new GunProperties().rangedDamage(SHOTGUN_DAMAGE).fireRate(20).recoil(SHOTGUN_RECOIL)
                            .shootSound(SoundReg.ITEM_SHOTGUN_SHOOT, null));

            // TODO: BALANCING NEEDED — double-barrel shotgun disabled until burst mechanics are tuned
            // NETHERITE_DOUBLE_BARRELS_SHOTGUN = registerGunItem("netherite_double_barrels_shotgun", DoubleBarrelShotgunItem::new,
            //         new GunProperties().rangedDamage(SHOTGUN_DAMAGE).fireRate(8).recoil(SHOTGUN_RECOIL)
            //                 .shootSound(SoundReg.ITEM_SHOTGUN_SHOOT, null));

            REVOLVER = registerGunItem("revolver", RevolverItem::new,
                new GunProperties().rangedDamage(REVOLVER_DAMAGE).fireRate(15).recoil(REVOLVER_RECOIL)
                    .shootSound(SoundReg.ITEM_REVOLVER_SHOOT, null));

            NETHERITE_REVOLVER = registerGunItem("netherite_revolver", RevolverItem::new,
                new GunProperties().rangedDamage(N_REVOLVER_DAMAGE).fireRate(15).recoil(REVOLVER_RECOIL)
                    .shootSound(SoundReg.ITEM_REVOLVER_SHOOT, null));

            // TODO: BALANCING NEEDED — big bore revolver disabled until high-damage tier is balanced
            // NETHERITE_BIG_BORE_REVOLVER = registerGunItem("netherite_big_bore_revolver", RevolverItem::new,
            //         new GunProperties().rangedDamage(25F).fireRate(6).recoil(3F)
            //                 .shootSound(SoundReg.ITEM_SHOTGUN_SHOOT, null));

            // TODO: BALANCING NEEDED — flare gun disabled until special ammo is implemented
            // FLARE_GUN = registerGunItem("flare_gun", s -> new FlareGunItem(s, TagReg.SHORT_MAGAZINES),
            //         new GunProperties().rangedDamage(REVOLVER_DAMAGE).fireRate(6).recoil(REVOLVER_RECOIL)
            //                 .shootSound(SoundReg.ITEM_REVOLVER_SHOOT, null)
            // );

            // TODO: BALANCING NEEDED — auto pistol disabled until pistol tier is designed
            // AUTO_PISTOL = registerGunItem("auto_pistol", s -> new GunWithMagItem(s, TagReg.SHORT_MAGAZINES),
            //         new GunProperties().rangedDamage(9F).fireRate(4).recoil(RIFLE_RECOIL)
            //                 .shootSound(SoundReg.ITEM_RIFLE_SHOOT, SoundReg.ITEM_RIFLE_SHOOT_SILENCED)
            //                 );

            LOGGER.info("Item registration process completed successfully");
        } catch (Exception e) {
            LOGGER.error("Error during item registration process", e);
            throw e;
        }
    }
}