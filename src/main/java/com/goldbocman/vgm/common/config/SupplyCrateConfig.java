package com.goldbocman.vgm.common.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.goldbocman.vgm.common.Bren;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 补给箱物品配置类
 * 支持动态配置补给箱中各种物品的权重和数量范围
 */
public class SupplyCrateConfig {
    private static final File file = new File("config/bren_supply_crate.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    // 默认配置
    private static final Map<String, GunConfig> DEFAULT_GUN_CONFIGS = new LinkedHashMap<>();
    private static final Map<String, AmmoConfig> DEFAULT_AMMO_CONFIGS = new LinkedHashMap<>();
    private static final Map<String, MagazineConfig> DEFAULT_MAGAZINE_CONFIGS = new LinkedHashMap<>();

    static {
        // 初始化默认枪械配置
        DEFAULT_GUN_CONFIGS.put("machine_gun", new GunConfig("Machine Gun", 8));
        DEFAULT_GUN_CONFIGS.put("air_gun", new GunConfig("Air Gun", 10));
        DEFAULT_GUN_CONFIGS.put("auto_gun", new GunConfig("Auto Gun", 8));
        DEFAULT_GUN_CONFIGS.put("rifle", new GunConfig("Rifle", 10));
        DEFAULT_GUN_CONFIGS.put("shotgun", new GunConfig("Shotgun", 15));
        DEFAULT_GUN_CONFIGS.put("revolver", new GunConfig("Revolver", 12));
        DEFAULT_GUN_CONFIGS.put("netherite_machine_gun", new GunConfig("Netherite Machine Gun", 3));
        DEFAULT_GUN_CONFIGS.put("netherite_auto_gun", new GunConfig("Netherite Auto Gun", 3));
        DEFAULT_GUN_CONFIGS.put("netherite_rifle", new GunConfig("Netherite Rifle", 4));
        DEFAULT_GUN_CONFIGS.put("netherite_shotgun", new GunConfig("Netherite Shotgun", 17));
        DEFAULT_GUN_CONFIGS.put("netherite_revolver", new GunConfig("Netherite Revolver", 2));
        DEFAULT_GUN_CONFIGS.put("flare_gun", new GunConfig("Flare Gun", 15));
        DEFAULT_GUN_CONFIGS.put("netherite_double_barrels_shotgun", new GunConfig("Netherite Double Barrels Shotgun", 1));
        DEFAULT_GUN_CONFIGS.put("netherite_lever_gun", new GunConfig("Netherite Lever Gun", 1));
        DEFAULT_GUN_CONFIGS.put("smg", new GunConfig("SMG", 16));
        DEFAULT_GUN_CONFIGS.put("grappling_hook", new GunConfig("Grappling Hook", 5));

        // 初始化默认弹药配置
        DEFAULT_AMMO_CONFIGS.put("explosive_spear", new AmmoConfig("Explosive Spear", 4, 8, 32));
        DEFAULT_AMMO_CONFIGS.put("bullet", new AmmoConfig("Bullet", 15, 16, 64));
        DEFAULT_AMMO_CONFIGS.put("shell", new AmmoConfig("Shell", 16, 8, 32));
        DEFAULT_AMMO_CONFIGS.put("dragonbreath_shell", new AmmoConfig("Dragonbreath Shell", 6, 1, 8));

        // 初始化默认弹匣配置
        DEFAULT_MAGAZINE_CONFIGS.put("magazine", new MagazineConfig("Magazine", 12, 30));
        DEFAULT_MAGAZINE_CONFIGS.put("drum_magazine", new MagazineConfig("Drum Magazine", 4, 120));
        DEFAULT_MAGAZINE_CONFIGS.put("clothed_magazine", new MagazineConfig("Clothed Magazine", 6, 50));
        DEFAULT_MAGAZINE_CONFIGS.put("short_magazine", new MagazineConfig("Short Magazine", 8, 15));
    }

    // 当前配置
    private static Map<String, GunConfig> gunConfigs = new LinkedHashMap<>(DEFAULT_GUN_CONFIGS);
    private static Map<String, AmmoConfig> ammoConfigs = new LinkedHashMap<>(DEFAULT_AMMO_CONFIGS);
    private static Map<String, MagazineConfig> magazineConfigs = new LinkedHashMap<>(DEFAULT_MAGAZINE_CONFIGS);

    public static void init() {
        if (!file.exists()) {
            save();
        } else {
            load();
        }
    }

    public static void save() {
        if (!file.getParentFile().isDirectory() && !file.getParentFile().mkdirs()) {
            Bren.LOGGER.error("Failed to create supply crate config directory");
            return;
        }

        try (FileWriter fileWriter = new FileWriter(file)) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("name", "Supply Crate Configuration for the Bren mod");
            jsonObject.addProperty("_comment", "Modify the weights to change spawn probabilities. Higher weight = higher chance");

            // 枪械配置
            JsonObject gunsJson = new JsonObject();
            for (Map.Entry<String, GunConfig> entry : gunConfigs.entrySet()) {
                gunsJson.add(entry.getKey(), entry.getValue().toJson());
            }
            jsonObject.add("guns", gunsJson);

            // 弹药配置
            JsonObject ammoJson = new JsonObject();
            for (Map.Entry<String, AmmoConfig> entry : ammoConfigs.entrySet()) {
                ammoJson.add(entry.getKey(), entry.getValue().toJson());
            }
            jsonObject.add("ammo", ammoJson);

            // 弹匣配置
            JsonObject magazinesJson = new JsonObject();
            for (Map.Entry<String, MagazineConfig> entry : magazineConfigs.entrySet()) {
                magazinesJson.add(entry.getKey(), entry.getValue().toJson());
            }
            jsonObject.add("magazines", magazinesJson);

            GSON.toJson(jsonObject, fileWriter);
        } catch (IOException e) {
            Bren.LOGGER.error("Failed to save supply crate config", e);
        }
    }

    public static void load() {
        if (!file.exists()) {
            return;
        }

        try (FileReader fileReader = new FileReader(file)) {
            JsonObject jsonObject = GSON.fromJson(fileReader, JsonObject.class);

            // 加载枪械配置
            if (jsonObject.has("guns")) {
                JsonObject gunsJson = jsonObject.getAsJsonObject("guns");
                for (Map.Entry<String, JsonElement> entry : gunsJson.entrySet()) {
                    if (gunConfigs.containsKey(entry.getKey())) {
                        gunConfigs.get(entry.getKey()).fromJson(entry.getValue().getAsJsonObject());
                    }
                }
            }

            // 加载弹药配置
            if (jsonObject.has("ammo")) {
                JsonObject ammoJson = jsonObject.getAsJsonObject("ammo");
                for (Map.Entry<String, JsonElement> entry : ammoJson.entrySet()) {
                    if (ammoConfigs.containsKey(entry.getKey())) {
                        ammoConfigs.get(entry.getKey()).fromJson(entry.getValue().getAsJsonObject());
                    }
                }
            }

            // 加载弹匣配置
            if (jsonObject.has("magazines")) {
                JsonObject magazinesJson = jsonObject.getAsJsonObject("magazines");
                for (Map.Entry<String, JsonElement> entry : magazinesJson.entrySet()) {
                    if (magazineConfigs.containsKey(entry.getKey())) {
                        magazineConfigs.get(entry.getKey()).fromJson(entry.getValue().getAsJsonObject());
                    }
                }
            }

        } catch (IOException e) {
            Bren.LOGGER.error("Failed to read supply crate config", e);
        }
    }

    // 获取配置方法
    public static Map<String, GunConfig> getGunConfigs() {
        return Collections.unmodifiableMap(gunConfigs);
    }

    public static Map<String, AmmoConfig> getAmmoConfigs() {
        return Collections.unmodifiableMap(ammoConfigs);
    }

    public static Map<String, MagazineConfig> getMagazineConfigs() {
        return Collections.unmodifiableMap(magazineConfigs);
    }

    // 配置类定义
    public static class GunConfig {
        public String name;
        public int weight;

        public GunConfig(String name, int weight) {
            this.name = name;
            this.weight = weight;
        }

        public JsonObject toJson() {
            JsonObject json = new JsonObject();
            json.addProperty("name", name);
            json.addProperty("weight", weight);
            return json;
        }

        public void fromJson(JsonObject json) {
            if (json.has("weight")) {
                this.weight = json.get("weight").getAsInt();
            }
        }
    }

    public static class AmmoConfig {
        public String name;
        public int weight;
        public int minAmount;
        public int maxAmount;

        public AmmoConfig(String name, int weight, int minAmount, int maxAmount) {
            this.name = name;
            this.weight = weight;
            this.minAmount = minAmount;
            this.maxAmount = maxAmount;
        }

        public JsonObject toJson() {
            JsonObject json = new JsonObject();
            json.addProperty("name", name);
            json.addProperty("weight", weight);
            json.addProperty("minAmount", minAmount);
            json.addProperty("maxAmount", maxAmount);
            return json;
        }

        public void fromJson(JsonObject json) {
            if (json.has("weight")) {
                this.weight = json.get("weight").getAsInt();
            }
            if (json.has("minAmount")) {
                this.minAmount = json.get("minAmount").getAsInt();
            }
            if (json.has("maxAmount")) {
                this.maxAmount = json.get("maxAmount").getAsInt();
            }
        }
    }

    public static class MagazineConfig {
        public String name;
        public int weight;
        public int capacity;

        public MagazineConfig(String name, int weight, int capacity) {
            this.name = name;
            this.weight = weight;
            this.capacity = capacity;
        }

        public JsonObject toJson() {
            JsonObject json = new JsonObject();
            json.addProperty("name", name);
            json.addProperty("weight", weight);
            json.addProperty("capacity", capacity);
            return json;
        }

        public void fromJson(JsonObject json) {
            if (json.has("weight")) {
                this.weight = json.get("weight").getAsInt();
            }
            if (json.has("capacity")) {
                this.capacity = json.get("capacity").getAsInt();
            }
        }
    }
}