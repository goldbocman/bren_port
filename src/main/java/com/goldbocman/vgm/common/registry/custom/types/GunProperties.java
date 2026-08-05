package com.goldbocman.vgm.common.registry.custom.types;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.Item;

public class GunProperties {

    public float recoil;
    public float rangedDamage;
    public int fireRate;
    public float speed = 3.5F;
    public int durability = 1;
    public SoundEvent sound;
    public SoundEvent silentSound;

    public GunProperties() {}

    public GunProperties rangedDamage(float damage) {
        this.rangedDamage = damage;
        return this;
    }
    public GunProperties fireRate(int rate) {
        this.fireRate = rate;
        return this;
    }
    public GunProperties recoil(float recoil) {
        this.recoil = recoil;
        return this;
    }
    public GunProperties durability(int durability) {
        this.durability = durability;
        return this;
    }
    public GunProperties shootSound(SoundEvent sound, SoundEvent silent) {
        this.sound = sound;
        this.silentSound = silent;
        return this;
    }

    public Object ammoType(Item shell) {
        return null;
    }
}