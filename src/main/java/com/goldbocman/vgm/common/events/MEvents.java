package com.goldbocman.vgm.common.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class MEvents {

    /**
     * An event that is called when a gun is fired.
     *
     * This is fired from {@link com.goldbocman.vgm.common.mixin.PlayerEntityMixin#handleShooting)}
     *
     */
    public static final Event<GunFired> GUN_FIRED_EVENT = EventFactory.createArrayBacked(GunFired.class, callbacks -> (player, stack) -> {
        for (GunFired callback : callbacks) {
            callback.gunFired(player, stack);
        }
    });

    @FunctionalInterface
    public interface GunFired {
        /**
         * Called when a player shoots a gun.
         *
         * @param player the player that fired the gun
         * @param stack the gun item stack
         */
        void gunFired(Player player, ItemStack stack);
    }

    /**
     * An event that is called when a player left-clicks (attacks) with an item.
     *
     * This is fired from {@link com.goldbocman.vgm.common.mixin.PlayerEntityMixin#attack}
     *
     */
    public static final Event<ItemLeftClick> ITEM_LEFT_CLICK_EVENT = EventFactory.createArrayBacked(ItemLeftClick.class, callbacks -> (player, stack) -> {
        for (ItemLeftClick callback : callbacks) {
            callback.onLeftClick(player, stack);
        }
    });

    @FunctionalInterface
    public interface ItemLeftClick {
        /**
         * Called when a player left-clicks with an item.
         *
         * @param player the player that left-clicked
         * @param stack the item stack being held
         */
        void onLeftClick(Player player, ItemStack stack);
    }
}