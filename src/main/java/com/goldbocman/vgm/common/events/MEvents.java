package com.goldbocman.vgm.common.events;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class MEvents {

    /**
     * A minimal, loader-agnostic stand-in for Fabric API's array-backed {@code Event<T>}: keeps a list of
     * listeners and exposes a single {@code T} proxy (built once via {@code invokerFactory}) that fans out to
     * all of them.
     */
    public static class SimpleEvent<T> {
        private final List<T> listeners = new ArrayList<>();
        private final Function<List<T>, T> invokerFactory;
        private T invoker;

        public SimpleEvent(Function<List<T>, T> invokerFactory) {
            this.invokerFactory = invokerFactory;
            this.invoker = invokerFactory.apply(listeners);
        }

        public void register(T listener) {
            listeners.add(listener);
            invoker = invokerFactory.apply(listeners);
        }

        public T invoker() {
            return invoker;
        }
    }

    /**
     * An event that is called when a gun is fired.
     *
     * This is fired from {@link com.goldbocman.vgm.common.mixin.PlayerEntityMixin#handleShooting)}
     *
     */
    public static final SimpleEvent<GunFired> GUN_FIRED_EVENT = new SimpleEvent<>(callbacks -> (player, stack) -> {
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
    public static final SimpleEvent<ItemLeftClick> ITEM_LEFT_CLICK_EVENT = new SimpleEvent<>(callbacks -> (player, stack) -> {
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
