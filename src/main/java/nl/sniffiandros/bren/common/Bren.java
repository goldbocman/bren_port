package nl.sniffiandros.bren.common;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import nl.sniffiandros.bren.common.config.MConfig;
import nl.sniffiandros.bren.common.entity.BulletEntity;
import nl.sniffiandros.bren.common.registry.*;
import nl.sniffiandros.bren.common.registry.custom.MagazineItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Bren implements ModInitializer {
	public static final String MODID = "bren";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);
	public static final int UNIVERSAL_AMMO_COLOR = 0xFFAE00;

	public static final ResourceKey<CreativeModeTab> BREN_TAB = ResourceKey.create(Registries.CREATIVE_MODE_TAB, Identifier.fromNamespaceAndPath(MODID, "bren_tab"));

	public static final EntityType<@org.jetbrains.annotations.NotNull BulletEntity> BULLET = Registry.register(BuiltInRegistries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(MODID,
			"bullet"), EntityType.Builder.<BulletEntity>of(
					(type, level) -> new BulletEntity(type, level), MobCategory.MISC).clientTrackingRange(10)
					.sized(0.35F, 0.35F).noSave().build(
							net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(MODID, "bullet"))));

	@Override
	public void onInitialize() {
		// 在1.21.4中，必须先注册属性，然后再注册物品
		// 属性注册必须在物品注册之前

		ModContainer container = FabricLoader.getInstance().getModContainer(MODID)
				.orElseThrow(RuntimeException::new);

		// 注册内置资源包
		// 第一个参数: 资源包的 ID (唯一标识)
		// 第二个参数: 模组容器
		// 第三个参数: 资源包激活类型 (ALWAYS_ENABLED 强制启用, DEFAULT_ENABLED 默认启用但允许关闭)
		boolean added2 = ResourceManagerHelper.registerBuiltinResourcePack(
				Identifier.fromNamespaceAndPath(MODID, "bren_3d_resources_2"), // 资源包ID
				container,
				ResourcePackActivationType.NORMAL  // 玩家可以手动关闭
		);
		AttributeReg.reg();
		// 注册数据组件类型（必须在物品注册之前）
		DataComponentReg.register();

		AttributeReg.reg();

		// 然后注册其他内容
		ItemReg.reg();
		SoundReg.reg();
		ParticleReg.reg();

		MConfig.init();



		// 初始化世界生成
		WorldGenReg.init();

		// 移除CommonLifecycleEvents中的属性注册调用

		NetworkReg.registerAllPackets();

		// 初始化数据包注册
		DataPackReg.init();

		// 注册钩索处理器
//		nl.sniffiandros.bren.common.events.GrapplingHookHandler.register();

		// 注册手雷处理器
//		nl.sniffiandros.bren.common.events.GrenadeHandler.register();

		// 注册自定义创意标签页
		Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, BREN_TAB, FabricCreativeModeTab.builder()
			.title(Component.translatable("itemGroup." + MODID + ".bren_tab"))
			.icon(() -> new ItemStack(ItemReg.NETHERITE_AUTO_GUN))
			.displayItems((context, output) -> {
				// 添加所有武器
				output.accept(ItemReg.MACHINE_GUN);
				output.accept(ItemReg.AUTO_GUN);
				output.accept(ItemReg.SHOTGUN);
				output.accept(ItemReg.RIFLE);
				output.accept(ItemReg.REVOLVER);
				output.accept(ItemReg.NETHERITE_MACHINE_GUN);
				output.accept(ItemReg.NETHERITE_AUTO_GUN);
				output.accept(ItemReg.NETHERITE_TACTICAL_AUTO_GUN);
				output.accept(ItemReg.NETHERITE_SHOTGUN);
				output.accept(ItemReg.NETHERITE_DOUBLE_BARRELS_SHOTGUN);
				output.accept(ItemReg.NETHERITE_RIFLE);
				output.accept(ItemReg.NETHERITE_LEVER_GUN);
				output.accept(ItemReg.NETHERITE_REVOLVER);
				output.accept(ItemReg.AUTO_PISTOL);
				output.accept(ItemReg.FLARE_GUN);
			output.accept(ItemReg.FIRE_AXE);
			output.accept(ItemReg.EXPLOSIVE_SPEAR);
			output.accept(ItemReg.AIR_GUN);
			output.accept(ItemReg.SMG);
			output.accept(ItemReg.GRAPPLING_HOOK);

				// 添加弹药
				output.accept(ItemReg.BULLET);
				output.accept(ItemReg.SHELL);
				output.accept(ItemReg.DRAGONBREATH_SHELL);


				// 添加配件
				output.accept(ItemReg.MAGAZINE);
				output.accept(ItemReg.DRUM_MAGAZINE);
				output.accept(ItemReg.CLOTHED_MAGAZINE);
				output.accept(ItemReg.SHORT_MAGAZINE);

				// 添加材料
				output.accept(ItemReg.AUTO_LOADER_CONTRAPTION);
				output.accept(ItemReg.METAL_TUBE);
			})
			.build()
		);

		LOGGER.info("BAM! {} is done loading!", MODID);
	}

	private static ItemStack mag(MagazineItem m) {
		ItemStack mag = m.getDefaultInstance();
		MagazineItem.fillMagazine(mag, MagazineItem.getMaxCapacity(mag));
		return mag;
	}

	public static ItemStack getMagazineFromPlayer(Player player, TagKey<Item> magTag) {
		// 添加空值检查
		if (magTag == null) {
			Bren.LOGGER.warn("getMagazineFromPlayer called with null magTag for player {}", 
				player.getName().getString());
			return ItemStack.EMPTY;
		}
		
		Container inventory = player.getInventory();
		ItemStack fullestMag = ItemStack.EMPTY;
		ItemStack emptyMag = ItemStack.EMPTY; // 用于存储空弹匣
		
		// 添加空值检查

        Bren.LOGGER.debug("Searching for magazine for player {} with tag: {}",
			player.getName().getString(), magTag.location());
	
		// 首先尝试直接物品匹配（绕过标签系统）
		Item[] compatibleItems = getCompatibleMagazineItems(magTag);
		if (compatibleItems.length == 0) {
			Bren.LOGGER.warn("No compatible magazine items found for tag: {}", magTag.location());
			return ItemStack.EMPTY;
		}
	
		// 检查副手
		if (!player.getOffhandItem().isEmpty() && isCompatibleMagazine(player.getOffhandItem(), compatibleItems)) {
			int contents = MagazineItem.getContents(player.getOffhandItem());
			Bren.LOGGER.debug("Found magazine in offhand for player {}: item={}, contents={}", 
				player.getName().getString(), player.getOffhandItem().getItem(), contents);
			// 优先选择有子弹的弹匣，如果没有则选择空弹匣
			if (!MagazineItem.isEmpty(player.getOffhandItem())) {
				return player.getOffhandItem();
			} else {
				emptyMag = player.getOffhandItem();
			}
		} else if (!player.getOffhandItem().isEmpty()) {
			Bren.LOGGER.debug("Offhand item for player {} is not matching: {}", 
				player.getName().getString(), player.getOffhandItem().getItem());
		}
		
		int foundCount = 0;
		for(int i = 0; i < inventory.getContainerSize(); ++i) {
			ItemStack itemStack = inventory.getItem(i);
			
			// 添加空值检查
			if (itemStack.isEmpty()) {
				continue;
			}
			
			// 直接检查物品类型，绕过标签系统
			boolean matches = isCompatibleMagazine(itemStack, compatibleItems);
			
			if (matches) {
				foundCount++;
				int contents = MagazineItem.getContents(itemStack);
				Bren.LOGGER.debug("Found matching magazine for player {} at slot {}: item={}, contents={}", 
					player.getName().getString(), i, itemStack.getItem(), contents);
				
				// 修复：优先选择有子弹的弹匣，而不是容量最大的
				if (!MagazineItem.isEmpty(itemStack) && MagazineItem.getContents(itemStack) > 0) {
					if (fullestMag.isEmpty() || MagazineItem.getContents(itemStack) > MagazineItem.getContents(fullestMag)) {
						fullestMag = itemStack;
						Bren.LOGGER.debug("Updated fullestMag for player {}: item={}, contents={}", 
							player.getName().getString(), fullestMag.getItem(), contents);
					}
				} else if (emptyMag.isEmpty()) {
					// 保存第一个找到的空弹匣
					emptyMag = itemStack;
					Bren.LOGGER.debug("Found empty magazine for player {}: item={}", 
						player.getName().getString(), emptyMag.getItem());
				}
			}
		}
		
		// 如果找到了有子弹的弹匣，优先返回它
		if (!fullestMag.isEmpty()) {
			int finalContents = MagazineItem.getContents(fullestMag);
			Bren.LOGGER.info("Selected loaded magazine for player {}: item={}, contents={}, total found={}", 
				player.getName().getString(), fullestMag.getItem(), finalContents, foundCount);
			return fullestMag;
		}
		
		// 如果没有找到有子弹的弹匣，但找到了空弹匣，返回空弹匣
		if (!emptyMag.isEmpty()) {
			Bren.LOGGER.info("Selected empty magazine for player {}: item={}, total found={}", 
				player.getName().getString(), emptyMag.getItem(), foundCount);
			return emptyMag;
		}
		
		Bren.LOGGER.info("No suitable magazine found for player {} with tag: {}, total checked={}", 
			player.getName().getString(), magTag.location(), foundCount);
		return ItemStack.EMPTY;
	}

	// 新增辅助方法：根据标签获取兼容的弹匣物品
	private static Item[] getCompatibleMagazineItems(TagKey<Item> magTag) {
		if (magTag.location().getPath().equals("magazines/medium_magazines")) {
			return new Item[]{ItemReg.MAGAZINE, ItemReg.CLOTHED_MAGAZINE, ItemReg.DRUM_MAGAZINE};
		} else if (magTag.location().getPath().equals("magazines/short_magazines")) {
			return new Item[]{ItemReg.SHORT_MAGAZINE};
		} else {
			Bren.LOGGER.warn("Unknown magazine tag: {}", magTag.location().getPath());
			return new Item[0];
		}
	}

	// 新增辅助方法：检查物品是否为兼容的弹匣
	private static boolean isCompatibleMagazine(ItemStack itemStack, Item[] compatibleItems) {
		for (Item compatibleItem : compatibleItems) {
			if (itemStack.is(compatibleItem)) {
				return true;
			}
		}
		return false;
	}

	public static ItemStack getItemFromPlayer(Player player, Item item) {
		Container inventory = player.getInventory();

		Bren.LOGGER.debug("Searching for item for player {}: {}", 
			player.getName().getString(), item.toString());

		if (player.getOffhandItem().is(item)) {
			Bren.LOGGER.debug("Found item in offhand for player {}: {}", 
				player.getName().getString(), item);
			return player.getOffhandItem();
		}
		
		for(int i = 0; i < inventory.getContainerSize(); ++i) {
			ItemStack itemStack = inventory.getItem(i);
			if (itemStack.is(item)) {
				Bren.LOGGER.debug("Found item for player {} at slot {}: {}", 
					player.getName().getString(), i, item);
				return itemStack;
			}
		}
		
		Bren.LOGGER.info("Item not found for player {}: {}", 
			player.getName().getString(), item);
		return ItemStack.EMPTY;
	}
}