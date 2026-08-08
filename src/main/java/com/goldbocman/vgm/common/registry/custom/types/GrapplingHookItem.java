package com.goldbocman.vgm.common.registry.custom.types;

import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import com.goldbocman.vgm.common.utils.GunApiCompat;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 钩索物品类，实现将玩家拉向抓住的地方的功能
 */
public class GrapplingHookItem extends Item {
    private static final Logger LOGGER = LoggerFactory.getLogger(GrapplingHookItem.class);
    
    // 钩索属性
    public static final double HOOK_RANGE =100.0; // 钩索最大射程
    public static final double PULL_SPEED = 0.3;  // 拉取速度
    public static final double MAX_PULL_DISTANCE = 2.0; // 最大拉取距离（距离目标多远停止）
    public static final int COOLDOWN_TICKS = 20; // 冷却时间（刻）
    
    // 气体属性
    public static final int MAX_GAS = 20; // 最大气体使用次数
    public static final int PUMPS_TO_FILL = 10; // 打气次数
    
    // NBT键名
    private static final String HOOKED_POS_KEY = "grappling_hook_hooked_pos";
    private static final String IS_HOOKED_KEY = "grappling_hook_is_hooked";
    private static final String GAS_KEY = "grappling_hook_gas";
    private static final String PUMPS_KEY = "grappling_hook_pumps";
    
    public GrapplingHookItem(Properties settings) {
        super(settings.stacksTo(1));
        LOGGER.info("Creating new GrapplingHookItem instance");
    }
    
    //? if >=1.21.11 {
    @Override
    public @NotNull InteractionResult use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        return bren$use(level, player, stack, hand) ? InteractionResult.SUCCESS : InteractionResult.FAIL;
    }
    //?} else {
    /*@Override
    public net.minecraft.world.@NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        return bren$use(level, player, stack, hand)
                ? net.minecraft.world.InteractionResultHolder.success(stack)
                : net.minecraft.world.InteractionResultHolder.fail(stack);
    }
    *///?}

    // Shared by both use() overloads above - old and new Item.use() return different types
    // (InteractionResult vs InteractionResultHolder<ItemStack>), so the actual logic lives here as a
    // plain success/fail boolean and each version-specific override just wraps it.
    private boolean bren$use(Level level, Player player, ItemStack stack, InteractionHand hand) {
        // 检查冷却时间
        if (GunApiCompat.isOnCooldown(player.getCooldowns(), stack)) {
            return false;
        }

        // 检查是否已经钩住
        if (isHooked(stack)) {
            // 释放钩索
            releaseHook(player, stack);
            return true;
        }

        // 检查是否有气体
        int gas = getGas(stack);
        if (gas <= 0) {
            // 没有气体，进行打气
            int pumps = getPumps(stack) + 1;
            setPumps(stack, pumps);

            // 播放打气声音
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ARMOR_EQUIP_LEATHER, SoundSource.PLAYERS, 0.5F, 1.0F);

            // 检查是否打气完成
            if (pumps >= PUMPS_TO_FILL) {
                setGas(stack, MAX_GAS);
                setPumps(stack, 0);

                // 播放完成声音
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.0F, 1.0F);
            }

            return true;
        }

        // 发射钩索
        return shootHook(level, player, stack, hand);
    }

    private boolean shootHook(Level level, Player player, ItemStack stack, InteractionHand hand) {
        // 计算视线方向
        Vec3 lookVec = player.getLookAngle();
        Vec3 startPos = player.getEyePosition();
        Vec3 endPos = startPos.add(lookVec.scale(HOOK_RANGE));
        
        // 进行射线检测
        BlockHitResult hitResult = level.clip(new net.minecraft.world.level.ClipContext(
            startPos, endPos, 
            net.minecraft.world.level.ClipContext.Block.COLLIDER,
            net.minecraft.world.level.ClipContext.Fluid.NONE,
            player
        ));
        
        if (hitResult.getType() == HitResult.Type.BLOCK) {
            // 钩住方块
            Vec3 hookedVec = hitResult.getLocation();
            
            // 保存钩住的位置
            setHookedPos(stack, hookedVec);
            setIsHooked(stack, true);
            
            // 消耗气体
            int currentGas = getGas(stack);
            setGas(stack, currentGas - 1);
            
            // 播放声音
            level.playSound(null, player.getX(), player.getY(), player.getZ(), 
                SoundEvents.CROSSBOW_SHOOT, SoundSource.PLAYERS, 1.0F, 1.0F);
            
            // 设置冷却时间
            GunApiCompat.addCooldown(player.getCooldowns(), stack, COOLDOWN_TICKS);

            return true;
        }

        // 没有钩住任何东西
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.CROSSBOW_LOADING_MIDDLE, SoundSource.PLAYERS, 0.5F, 1.0F);

        return false;
    }
    
    private static void releaseHook(Player player, ItemStack stack) {
        setIsHooked(stack, false);
        
        // 播放释放声音
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.CROSSBOW_LOADING_START, SoundSource.PLAYERS, 0.5F, 1.0F);
    }
    
    /**
     * 每刻更新钩索的物理效果
     */
    public static void tickHook(Player player, ItemStack stack) {
        if (!isHooked(stack)) return;

        Vec3 hookedPos = getHookedPos(stack);
        if (hookedPos == null) {
            setIsHooked(stack, false);
            return;
        }

        // 计算玩家当前位置和钩住位置的距离
        Vec3 playerPos = player.position();
        Vec3 toHook = hookedPos.subtract(playerPos);
        double distance = toHook.length();

        // 如果距离太近，释放钩索
        if (distance <= MAX_PULL_DISTANCE) {
            releaseHook(player, stack);
            return;
        }

        // 计算拉取方向
        Vec3 pullDirection = toHook.normalize();

        // 应用拉取力 - 增强拉力效果
        double pullStrength = Math.min(PULL_SPEED * 2.0, distance * 0.15); // 距离越远拉力越大
        Vec3 velocity = pullDirection.scale(pullStrength);

        // 使用addVelocity来添加拉力，而不是直接设置速度
        player.addDeltaMovement(velocity);
        
        // 强制更新玩家移动
        player.hurtMarked = true;
        
        // 显示动作栏信息
        displayGasStatus(player, stack);
    }

    // 数据组件存储方法
    private static void setHookedPos(ItemStack stack, Vec3 pos) {
        var customData = stack.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA,
            net.minecraft.world.item.component.CustomData.EMPTY);
        var nbt = customData.copyTag();
        CompoundTag posTag = new CompoundTag();
        posTag.putDouble("x", pos.x);
        posTag.putDouble("y", pos.y);
        posTag.putDouble("z", pos.z);
        nbt.put(HOOKED_POS_KEY, posTag);
        stack.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA,
            net.minecraft.world.item.component.CustomData.of(nbt));
    }

    private static Vec3 getHookedPos(ItemStack stack) {
        var customData = stack.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA);
        if (customData == null) {
            return null;
        }
        var nbt = customData.copyTag();
        if (!nbt.contains(HOOKED_POS_KEY)) {
            return null;
        }
        CompoundTag posTag = GunApiCompat.getCompound(nbt, HOOKED_POS_KEY);
        double x = GunApiCompat.getDouble(posTag, "x");
        double y = GunApiCompat.getDouble(posTag, "y");
        double z = GunApiCompat.getDouble(posTag, "z");
        return new Vec3(x, y, z);
    }

    private static void setIsHooked(ItemStack stack, boolean isHooked) {
        var customData = stack.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA,
            net.minecraft.world.item.component.CustomData.EMPTY);
        var nbt = customData.copyTag();
        nbt.putBoolean(IS_HOOKED_KEY, isHooked);
        stack.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA,
            net.minecraft.world.item.component.CustomData.of(nbt));
    }

    private static boolean isHooked(ItemStack stack) {
        var customData = stack.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA);
        if (customData == null) {
            return false;
        }
        var nbt = customData.copyTag();
        return GunApiCompat.getBoolean(nbt, IS_HOOKED_KEY);
    }
    
    //? if >=1.21.11 {
    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context,
                               net.minecraft.world.item.component.@NotNull TooltipDisplay tooltipComponent,
                               java.util.function.Consumer<Component> tooltipAdder, @NotNull TooltipFlag type) {
        bren$appendHoverText(stack, tooltipAdder::accept);
    }
    //?} else {
    /*@Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context,
                               java.util.@NotNull List<Component> tooltipComponents, @NotNull TooltipFlag type) {
        bren$appendHoverText(stack, tooltipComponents::add);
    }
    *///?}

    private void bren$appendHoverText(ItemStack stack, java.util.function.Consumer<Component> tooltipAdder) {
        tooltipAdder.accept(Component.translatable("item.bren.grappling_hook.desc").withStyle(net.minecraft.ChatFormatting.GRAY));

        // 显示气体信息
        int gas = getGas(stack);
        int pumps = getPumps(stack);

        if (gas > 0) {
            tooltipAdder.accept(Component.translatable("item.bren.grappling_hook.gas", gas, MAX_GAS)
                .withStyle(net.minecraft.ChatFormatting.AQUA));
        } else if (pumps > 0) {
            tooltipAdder.accept(Component.translatable("item.bren.grappling_hook.pumping", pumps, PUMPS_TO_FILL)
                .withStyle(net.minecraft.ChatFormatting.YELLOW));
        } else {
            tooltipAdder.accept(Component.translatable("item.bren.grappling_hook.empty")
                .withStyle(net.minecraft.ChatFormatting.RED));
        }

        if (isHooked(stack)) {
            tooltipAdder.accept(Component.translatable("item.bren.grappling_hook.hooked").withStyle(net.minecraft.ChatFormatting.GREEN));
        }
    }
    
    /**
     * 客户端tick处理方法，用于显示视觉效果和状态
     */
    public static void clientTick() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            return;
        }
        
        Player player = mc.player;
        ItemStack mainHandStack = player.getMainHandItem();
        ItemStack offHandStack = player.getOffhandItem();
        
        if (mainHandStack.getItem() instanceof GrapplingHookItem) {
            renderHookEffects(player, mainHandStack);
            displayGasStatus(player, mainHandStack);
        } else if (offHandStack.getItem() instanceof GrapplingHookItem) {
            renderHookEffects(player, offHandStack);
            displayGasStatus(player, offHandStack);
        }
    }
    
    /**
     * 渲染钩索视觉效果
     */
    private static void renderHookEffects(Player player, ItemStack stack) {
        if (!isHooked(stack)) return;

        Vec3 hookedPos = getHookedPos(stack);
        if (hookedPos == null) return;

        Vec3 playerPos = player.position();
        Vec3 toHook = hookedPos.subtract(playerPos);
        double distance = toHook.length();

        Level level = player.level();
        
        // 钩索线效果 - 沿着钩索路径生成连续的粒子
        int particleCount = (int) Math.min(20, distance / 1.5);
        for (int i = 0; i < particleCount; i++) {
            double progress = (double) i / particleCount;
            Vec3 particlePos = playerPos.add(toHook.scale(progress));
            
            if (i % 3 == 0) {
                level.addParticle(ParticleTypes.ELECTRIC_SPARK,
                        particlePos.x, particlePos.y, particlePos.z, 0, 0, 0);
            } else if (i % 3 == 1) {
                level.addParticle(ParticleTypes.END_ROD,
                        particlePos.x, particlePos.y, particlePos.z, 0, 0, 0);
            } else {
                level.addParticle(ParticleTypes.SMOKE,
                        particlePos.x, particlePos.y, particlePos.z, 0, 0.05, 0);
            }
        }
        
        // 钩住位置锚点效果
        for (int i = 0; i < 5; i++) {
            double offsetX = (Math.random() - 0.5) * 0.3;
            double offsetY = (Math.random() - 0.5) * 0.3;
            double offsetZ = (Math.random() - 0.5) * 0.3;
            level.addParticle(ParticleTypes.FLAME,
                    hookedPos.x + offsetX, hookedPos.y + offsetY, hookedPos.z + offsetZ, 0, 0, 0);
        }
        
        // 玩家拉力效果
        for (int i = 0; i < 3; i++) {
            double offsetX = (Math.random() - 0.5) * 0.2;
            double offsetY = Math.random() * 0.3;
            double offsetZ = (Math.random() - 0.5) * 0.2;
            level.addParticle(ParticleTypes.SWEEP_ATTACK,
                    playerPos.x + offsetX, playerPos.y + offsetY, playerPos.z + offsetZ, 0, 0, 0);
        }
    }
    
    // 气体数据存储方法
    private static void setGas(ItemStack stack, int gas) {
        var customData = stack.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA,
            net.minecraft.world.item.component.CustomData.EMPTY);
        var nbt = customData.copyTag();
        nbt.putInt(GAS_KEY, gas);
        stack.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA,
            net.minecraft.world.item.component.CustomData.of(nbt));
    }
    
    private static int getGas(ItemStack stack) {
        var customData = stack.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA);
        if (customData == null) {
            return 0;
        }
        var nbt = customData.copyTag();
        return GunApiCompat.getInt(nbt, GAS_KEY);
    }
    
    private static void setPumps(ItemStack stack, int pumps) {
        var customData = stack.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA,
            net.minecraft.world.item.component.CustomData.EMPTY);
        var nbt = customData.copyTag();
        nbt.putInt(PUMPS_KEY, pumps);
        stack.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA,
            net.minecraft.world.item.component.CustomData.of(nbt));
    }
    
    private static int getPumps(ItemStack stack) {
        var customData = stack.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA);
        if (customData == null) {
            return 0;
        }
        var nbt = customData.copyTag();
        return GunApiCompat.getInt(nbt, PUMPS_KEY);
    }
    
    /**
     * 显示气体状态
     */
    private static void displayGasStatus(Player player, ItemStack stack) {
        int gas = getGas(stack);
        int pumps = getPumps(stack);
        
        Component message;
        
        if (gas > 0) {
            // 显示气体进度条
            double gasProgress = (double) gas / MAX_GAS;
            String progressBar = createProgressBar(gasProgress, 20);
            message = Component.literal("§bAir: " + progressBar + " " + gas + "/" + MAX_GAS);
        } else if (pumps > 0) {
            // 显示打气进度条
            double pumpProgress = (double) pumps / PUMPS_TO_FILL;
            String progressBar = createProgressBar(pumpProgress, 20);
            message = Component.literal("§ePumping: " + progressBar + " " + pumps + "/" + PUMPS_TO_FILL);
        } else {
            // 气体耗尽提示
            message = Component.literal("§cNeed Pumping");
        }
        
        //? if >=1.21.11 {
        player.sendOverlayMessage(message);
        //?} else {
        /*player.displayClientMessage(message, true);
        *///?}
    }
    
    /**
     * 创建进度条字符串
     */
    private static String createProgressBar(double progress, int totalBars) {
        int filledBars = (int) (progress * totalBars);
        int emptyBars = totalBars - filledBars;
        
        StringBuilder sb = new StringBuilder();
        
        // 使用不同颜色表示进度
        if (progress > 0.5) {
            sb.append("§a"); // 绿色
        } else if (progress > 0.25) {
            sb.append("§e"); // 黄色
        } else {
            sb.append("§c"); // 红色
        }
        
        // 填充部分
        for (int i = 0; i < filledBars; i++) {
            sb.append("█");
        }
        
        // 空部分
        sb.append("§7"); // 灰色
        for (int i = 0; i < emptyBars; i++) {
            sb.append("░");
        }
        
        return sb.toString();
    }
}