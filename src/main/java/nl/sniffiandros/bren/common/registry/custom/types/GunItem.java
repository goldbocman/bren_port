package nl.sniffiandros.bren.common.registry.custom.types;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import nl.sniffiandros.bren.common.Bren;
import nl.sniffiandros.bren.common.entity.IGunUser;
import nl.sniffiandros.bren.common.registry.ParticleReg;
import nl.sniffiandros.bren.common.registry.custom.PoseType;
import nl.sniffiandros.bren.common.utils.GunHelper;
import nl.sniffiandros.bren.common.utils.GunUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class GunItem extends Item {
    // 添加日志记录器
    private static final Logger LOGGER = LoggerFactory.getLogger(GunItem.class);

    // 使用静态映射来存储枪械属性，避免在构造函数中访问注册表
    private static final Map<Identifier, GunProperties> GUN_PROPERTIES_MAP = new HashMap<>();

    // 修改构造函数，接收Item.Settings参数
    public GunItem(Item.Properties settings) {
        super(settings.stacksTo(1));
        LOGGER.info("Creating new GunItem instance with custom settings");
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, Item.@NotNull TooltipContext context, net.minecraft.world.item.component.@NotNull TooltipDisplay tooltipComponent, java.util.function.Consumer<Component> tooltipAdder, @NotNull TooltipFlag type) {
        ChatFormatting formatting = ChatFormatting.AQUA;

        tooltipAdder.accept(Component.translatable(String.format("desc.%s.item.magazine.content",Bren.MODID))
                .append(Component.literal(" " + getContents(stack) + "/" + getMaxCapacity(stack))).withStyle(formatting));

        Component ammoDesc = getAmmoDescription();
        if (ammoDesc != null) {
            tooltipAdder.accept(ammoDesc);
        }

        GunProperties properties = getGunProperties(stack);
        if (properties != null) {
            tooltipAdder.accept(Component.literal("§cDamage: " + properties.rangedDamage).withStyle(ChatFormatting.GRAY));
            tooltipAdder.accept(Component.literal("§eFire Rate: " + properties.fireRate).withStyle(ChatFormatting.GRAY));
            tooltipAdder.accept(Component.literal("§aRecoil: " + properties.recoil).withStyle(ChatFormatting.GRAY));
        }

        super.appendHoverText(stack, context, tooltipComponent, tooltipAdder, type);
    }

    protected Component getAmmoDescription() {
        return null;
    }

    // 静态方法用于注册枪械属性
    public static void registerGunProperties(Identifier gunId, GunProperties properties) {
        LOGGER.info("Registering gun properties for: {}", gunId);
        GUN_PROPERTIES_MAP.put(gunId, properties);
    }

    // 通过物品ID获取枪械属性
    public GunProperties getGunProperties(ItemStack stack) {
        Identifier itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return GUN_PROPERTIES_MAP.get(itemId);
    }

    // 移除错误的getAttributeModifiers方法，使用组件系统自动处理属性
    // 属性现在通过PlayerEntityMixin中的手动应用方式处理

    // 获取射击声音
    public SoundEvent getShootSound(ItemStack stack) {
        GunProperties properties = getGunProperties(stack);
        return properties != null ? properties.sound : null;
    }

    // 获取子弹速度
    public float getBulletSpeed(ItemStack stack) {
        GunProperties properties = getGunProperties(stack);
        return properties != null ? properties.speed : 3.5F;
    }


    // 重写isItemBarVisible方法，使耐久条可见
    @Override
    public boolean isBarVisible(ItemStack stack) {
        // 只有当枪械有弹药容量时才显示耐久条
        return getMaxCapacity(stack) > 0;
    }

    // 重写getItemBarStep方法，控制耐久条的长度
    @Override
    public int getBarWidth(ItemStack stack) {
        int maxCapacity = getMaxCapacity(stack);
        if (maxCapacity <= 0) {
            return 0;
        }
        
        int contents = getContents(stack);
        // 计算耐久条的步骤（13是耐久条的最大步骤数）
        return Math.round((float) contents / maxCapacity * 13);
    }

    // 重写getItemBarColor方法，设置耐久条的颜色
    @Override
    public int getBarColor(ItemStack stack) {
        int maxCapacity = getMaxCapacity(stack);
        if (maxCapacity <= 0) {
            return 0xFFAE00; // 红色
        }
        
        int contents = getContents(stack);
        float ratio = (float) contents / maxCapacity;
        
        // 根据弹药比例改变颜色
        if (ratio > 0.5) {
            // 绿色到黄色的渐变
            return 0xFFAE00; // 绿色
        } else if (ratio > 0.25) {
            // 黄色
            return 0xFFAE00; // 黄色
        } else {
            // 红色
            return 0xFFAE00; // 红色
        }
    }

    public boolean applyCustomMatrix(LivingEntity entity, GunHelper.GunStates state, PoseStack matrices, ItemStack stack, float cooldownProgress, boolean leftHanded) {
        return false;
    }


    public boolean hasGUIModel() {
        return true;
    }

    public boolean ejectCasing() {
        return true;
    }

    public boolean renderOnBack() {
        return true;
    }

    public PoseType holdingPose() {
        return PoseType.TWO_ARMS;
    }

    public int getMaxCapacity(ItemStack stack) {
        return 0; // 修复：直接返回int值，而不是Optional.of(0)
    }

    public void onReload(Player player) {
    }

    public void reloadTick(ItemStack stack, Level world, Player player, IGunUser gunUser) {
    }

    @Override
    public InteractionResult use(Level world, Player user, InteractionHand hand) {
        ItemStack stack = user.getItemInHand(hand);
    
        // 右键检查：只有主手右键才触发
        if (hand == InteractionHand.OFF_HAND) {
            return InteractionResult.PASS;
        }
    
        if (user instanceof IGunUser gunUser) {
            // 移除setCurrentHand调用以避免触发挥动画
            // user.setCurrentHand(hand);
            
            // 子弹检查和冷却时间检查
            ItemCooldowns cooldownManager = user.getCooldowns();
            if (cooldownManager.isOnCooldown(stack) || this.isEmpty(stack)) {
                return InteractionResult.FAIL;
            }
            
            // 状态检查：只有在正常状态下才射击
            if (gunUser.bren_1_21_1$getGunState().equals(GunHelper.GunStates.NORMAL)) {
                // 完整射击逻辑
                int fireRate = GunUtils.fire(user);
                
                // 设置冷却时间
                if (fireRate > 0) {
                    cooldownManager.addCooldown(BuiltInRegistries.ITEM.getKey(stack.getItem()), fireRate);
                }
                
                // 关键修复：在Minecraft 1.21.4中，需要更明确地阻止原版动画
                // 返回FAIL而不是CONSUME，因为CONSUME在某些情况下仍可能触发挥动画
                return InteractionResult.FAIL;
            }
        }

    return InteractionResult.PASS;
}

    public boolean isEmpty(ItemStack stack) {
        return getContents(stack) <= 0;
    }

    public int getContents(ItemStack stack) {
        return 0;
    }

    public void useBullet(ItemStack stack) {
    }

    public static void shotParticles(Level world, Vec3 origin, Vec3 direction, RandomSource random) {
        for (int i = 0; i != 8; ++i) {
            double t = Math.pow(random.nextFloat(), 1.5);
            Vec3 p = origin.add(direction.scale(0.8 + t));
            Vec3 v = direction.scale(0.2 * (1 - t));
            world.addParticle((ParticleOptions) ParticleReg.MUZZLE_SMOKE_PARTICLE, p.x, p.y, p.z, v.x, v.y, v.z);
        }
    }

    public static void ejectCasingParticle(Level world, Vec3 origin, Vec3 direction, RandomSource random) {
        Vec3 rotated = direction.yRot((float) (-Math.PI / 2));
        Vec3 p = origin.add(direction.scale(.3f)).add(rotated.scale(.2));
        Vec3 v = rotated.scale(.15f).add(0, .5f + world.getRandom().nextFloat() * .1f, 0);
        world.addParticle((ParticleOptions) ParticleReg.CASING_PARTICLE, p.x, p.y, p.z, v.x, v.y, v.z);
    }

    public int bulletLifespan() {
        return 35;
    }

    public float spread() {
        return 0.0f;
    }

    public int bulletAmount() {
        return 1;
    }

    public float bulletSpeed(ItemStack stack) {
        return getBulletSpeed(stack);
    }

    public int reloadSpeed() {
        return 20;
    }
    
    /**
     * 检查此枪械是否支持瞄准功能
     */
    public boolean supportsAiming() {
        return true; // 默认所有枪械都支持瞄准
    }
    
    /**
     * 获取瞄准缩放倍数
     */
    public float getAimZoomLevel() {
        // Rifle有更高的缩放倍数
        Identifier itemId = BuiltInRegistries.ITEM.getKey(this);
        if (itemId != null && itemId.getPath().toLowerCase().contains("rifle")) {
            return 4.0f; // 4倍缩放
        }
        return 2.0f; // 其他枪械2倍缩放
    }
    
    /**
     * 获取瞄准时的FOV缩放
     */
    public float getAimFOVModifier() {
        return 1.0f / getAimZoomLevel();
    }
}