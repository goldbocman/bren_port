package com.goldbocman.vgm.common.datagen;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.predicates.DataComponentMatchers;
import net.minecraft.advancements.predicates.DamageSourcePredicate;
import net.minecraft.advancements.predicates.EnchantmentPredicate;
import net.minecraft.advancements.predicates.ItemPredicate;
import net.minecraft.advancements.predicates.LocationPredicate;
import net.minecraft.advancements.predicates.MinMaxBounds;
import net.minecraft.advancements.predicates.TagPredicate;
import net.minecraft.advancements.predicates.entity.EntityPredicate;
import net.minecraft.advancements.triggers.CriteriaTriggers;
import net.minecraft.advancements.triggers.Criterion;
import net.minecraft.advancements.triggers.EnchantedItemTrigger;
import net.minecraft.advancements.triggers.InventoryChangeTrigger;
import net.minecraft.advancements.triggers.KilledTrigger;
import net.minecraft.advancements.triggers.PlayerTrigger;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.predicates.DataComponentPredicates;
import net.minecraft.core.component.predicates.EnchantmentsPredicate;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.advancements.AdvancementSubProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import com.goldbocman.vgm.common.Bren;
import com.goldbocman.vgm.common.registry.ItemReg;
import com.goldbocman.vgm.common.registry.TagReg;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class VgmAdvancementProvider implements AdvancementSubProvider {
    @Override
    public void generate(HolderLookup.Provider registries, Consumer<AdvancementHolder> saver) {
        HolderGetter<Item> items = registries.lookupOrThrow(Registries.ITEM);
        HolderGetter<Enchantment> enchantments = registries.lookupOrThrow(Registries.ENCHANTMENT);
        HolderGetter<EntityType<?>> entityTypes = registries.lookupOrThrow(Registries.ENTITY_TYPE);

        AdvancementHolder root = Advancement.Builder.advancement()
                .display(ItemReg.AUTO_PISTOL,
                        Component.translatable("advancements.vgm.adventure.root.title"),
                        Component.translatable("advancements.vgm.adventure.root.description"),
                        Identifier.withDefaultNamespace("gui/advancements/backgrounds/adventure"),
                        AdvancementType.TASK, false, false, false)
                .addCriterion("in_world", PlayerTrigger.TriggerInstance.located(LocationPredicate.Builder.location()))
                .sendsTelemetryEvent()
                .save(saver, Bren.MODID + ":adventure/root");

        AdvancementHolder autoLoaderContraption = Advancement.Builder.advancement()
                .parent(root)
                .display(ItemReg.AUTO_LOADER_CONTRAPTION,
                        Component.translatable("advancements.vgm.adventure.auto_loader_contraption.title"),
                        Component.translatable("advancements.vgm.adventure.auto_loader_contraption.description"),
                        null, AdvancementType.TASK, true, true, false)
                .addCriterion("auto_loader_contraption", InventoryChangeTrigger.TriggerInstance.hasItems(ItemReg.AUTO_LOADER_CONTRAPTION))
                .sendsTelemetryEvent()
                .save(saver, Bren.MODID + ":adventure/auto_loader_contraption");

        gunAdvancement(saver, autoLoaderContraption, "auto_gun", ItemReg.AUTO_GUN, ItemReg.NETHERITE_AUTO_GUN);
        gunAdvancement(saver, autoLoaderContraption, "rifle", ItemReg.RIFLE, ItemReg.NETHERITE_RIFLE);
        gunAdvancement(saver, autoLoaderContraption, "shotgun", ItemReg.SHOTGUN, ItemReg.NETHERITE_SHOTGUN);
        gunAdvancement(saver, autoLoaderContraption, "revolver", ItemReg.REVOLVER, ItemReg.NETHERITE_REVOLVER);

        AdvancementHolder armsDealer = Advancement.Builder.advancement()
                .parent(root)
                .display(ItemReg.AUTO_GUN,
                        Component.translatable("advancements.vgm.adventure.arms_dealer.title"),
                        Component.translatable("advancements.vgm.adventure.arms_dealer.description"),
                        null, AdvancementType.GOAL, true, true, false)
                .addCriterion("auto_gun", InventoryChangeTrigger.TriggerInstance.hasItems(ItemReg.AUTO_GUN))
                .addCriterion("rifle", InventoryChangeTrigger.TriggerInstance.hasItems(ItemReg.RIFLE))
                .addCriterion("shotgun", InventoryChangeTrigger.TriggerInstance.hasItems(ItemReg.SHOTGUN))
                .addCriterion("revolver", InventoryChangeTrigger.TriggerInstance.hasItems(ItemReg.REVOLVER))
                .requirements(AdvancementRequirements.Strategy.AND)
                .sendsTelemetryEvent()
                .save(saver, Bren.MODID + ":adventure/arms_dealer");

        Advancement.Builder.advancement()
                .parent(armsDealer)
                .display(Items.NETHERITE_INGOT,
                        Component.translatable("advancements.vgm.adventure.debris_armed.title"),
                        Component.translatable("advancements.vgm.adventure.debris_armed.description"),
                        null, AdvancementType.CHALLENGE, true, true, false)
                .addCriterion("netherite_auto_gun", InventoryChangeTrigger.TriggerInstance.hasItems(ItemReg.NETHERITE_AUTO_GUN))
                .addCriterion("netherite_rifle", InventoryChangeTrigger.TriggerInstance.hasItems(ItemReg.NETHERITE_RIFLE))
                .addCriterion("netherite_shotgun", InventoryChangeTrigger.TriggerInstance.hasItems(ItemReg.NETHERITE_SHOTGUN))
                .addCriterion("netherite_revolver", InventoryChangeTrigger.TriggerInstance.hasItems(ItemReg.NETHERITE_REVOLVER))
                .requirements(AdvancementRequirements.Strategy.AND)
                .sendsTelemetryEvent()
                .save(saver, Bren.MODID + ":adventure/debris_armed");

        AdvancementHolder aTouchOfMagic = Advancement.Builder.advancement()
                .parent(root)
                .display(Items.ENCHANTED_BOOK,
                        Component.translatable("advancements.vgm.adventure.a_touch_of_magic.title"),
                        Component.translatable("advancements.vgm.adventure.a_touch_of_magic.description"),
                        null, AdvancementType.TASK, true, true, false)
                .addCriterion("flame", enchantedWeapon(items, enchantments, Enchantments.FLAME, 1))
                .addCriterion("punch", enchantedWeapon(items, enchantments, Enchantments.PUNCH, 1))
                .addCriterion("unbreaking", enchantedWeapon(items, enchantments, Enchantments.UNBREAKING, 1))
                .requirements(AdvancementRequirements.Strategy.OR)
                .sendsTelemetryEvent()
                .save(saver, Bren.MODID + ":adventure/a_touch_of_magic");

        ItemStackTemplate glintShotgunIcon = new ItemStackTemplate(ItemReg.NETHERITE_SHOTGUN,
                DataComponentPatch.builder().set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true).build());

        Advancement.Builder.advancement()
                .parent(aTouchOfMagic)
                .display(glintShotgunIcon,
                        Component.translatable("advancements.vgm.adventure.never_let_you_down.title"),
                        Component.translatable("advancements.vgm.adventure.never_let_you_down.description"),
                        null, AdvancementType.CHALLENGE, true, true, false)
                .addCriterion("maxed_out", maxedOutNetheriteGun(items, enchantments))
                .sendsTelemetryEvent()
                .save(saver, Bren.MODID + ":adventure/never_let_you_down");

        Advancement.Builder.advancement()
                .parent(root)
                .display(ItemReg.BULLET,
                        Component.translatable("advancements.vgm.adventure.warcrimes.title"),
                        Component.translatable("advancements.vgm.adventure.warcrimes.description"),
                        null, AdvancementType.TASK, true, true, false)
                .addCriterion("killed_villager", killedVillagerWithBullet(entityTypes))
                .sendsTelemetryEvent()
                .save(saver, Bren.MODID + ":adventure/warcrimes");
    }

    private static void gunAdvancement(Consumer<AdvancementHolder> saver, AdvancementHolder parent, String name, Item base, Item netherite) {
        Advancement.Builder.advancement()
                .parent(parent)
                .display(base,
                        Component.translatable("advancements.vgm.adventure." + name + ".title"),
                        Component.translatable("advancements.vgm.adventure." + name + ".description"),
                        null, AdvancementType.TASK, true, true, false)
                .addCriterion(name, InventoryChangeTrigger.TriggerInstance.hasItems(base, netherite))
                .sendsTelemetryEvent()
                .save(saver, Bren.MODID + ":adventure/" + name);
    }

    private static Criterion<EnchantedItemTrigger.TriggerInstance> enchantedWeapon(
            HolderGetter<Item> items, HolderGetter<Enchantment> enchantments, ResourceKey<Enchantment> enchantment, int minLevel) {
        ItemPredicate predicate = ItemPredicate.Builder.item()
                .of(items, TagReg.WEAPONS)
                .withComponents(DataComponentMatchers.Builder.components()
                        .partial(DataComponentPredicates.ENCHANTMENTS,
                                EnchantmentsPredicate.enchantments(List.of(
                                        new EnchantmentPredicate(enchantments.getOrThrow(enchantment), MinMaxBounds.Ints.atLeast(minLevel)))))
                        .build())
                .build();
        return CriteriaTriggers.ENCHANTED_ITEM.createCriterion(
                new EnchantedItemTrigger.TriggerInstance(Optional.empty(), Optional.of(predicate), MinMaxBounds.Ints.ANY));
    }

    private static Criterion<EnchantedItemTrigger.TriggerInstance> maxedOutNetheriteGun(
            HolderGetter<Item> items, HolderGetter<Enchantment> enchantments) {
        ItemPredicate predicate = ItemPredicate.Builder.item()
                .of(items, ItemReg.NETHERITE_AUTO_GUN, ItemReg.NETHERITE_RIFLE, ItemReg.NETHERITE_SHOTGUN, ItemReg.NETHERITE_REVOLVER)
                .withComponents(DataComponentMatchers.Builder.components()
                        .partial(DataComponentPredicates.ENCHANTMENTS,
                                EnchantmentsPredicate.enchantments(List.of(
                                        new EnchantmentPredicate(enchantments.getOrThrow(Enchantments.FLAME), MinMaxBounds.Ints.atLeast(1)),
                                        new EnchantmentPredicate(enchantments.getOrThrow(Enchantments.PUNCH), MinMaxBounds.Ints.atLeast(2)),
                                        new EnchantmentPredicate(enchantments.getOrThrow(Enchantments.UNBREAKING), MinMaxBounds.Ints.atLeast(3)))))
                        .build())
                .build();
        return CriteriaTriggers.ENCHANTED_ITEM.createCriterion(
                new EnchantedItemTrigger.TriggerInstance(Optional.empty(), Optional.of(predicate), MinMaxBounds.Ints.ANY));
    }

    private static Criterion<KilledTrigger.TriggerInstance> killedVillagerWithBullet(HolderGetter<EntityType<?>> entityTypes) {
        return KilledTrigger.TriggerInstance.playerKilledEntity(
                EntityPredicate.Builder.entity().of(entityTypes, EntityTypes.VILLAGER),
                DamageSourcePredicate.Builder.damageType().tag(TagPredicate.is(TagReg.IS_BULLET)));
    }
}
