package net.lyof.sortilege.configs;

import com.google.gson.Gson;
import com.mojang.datafixers.util.Pair;
import net.lyof.sortilege.Sortilege;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ModJsonConfigs {
    public static final ConfigEntry<Double> VERSION = new ConfigEntry<>("TECHNICAL.VERSION_DO_NOT_EDIT", 0d);
    public static final ConfigEntry<Boolean> RELOAD = new ConfigEntry<>("TECHNICAL.FORCE_RESET", false);

    public static Map CONFIG = new TreeMap<>();

    public static List<Pair<String, StaffInfo>> STAFFS = new ArrayList<>();



    public static class StaffInfo {
        public Tier tier;
        public int damage;
        public int pierce;
        public int range;
        public int durability;
        public int cooldown;
        public int charge_time;
        public int xp_cost;
        public boolean fireRes;
        public String dependency;

        public StaffInfo(Map<String, ?> dict) {
            this(
                    dict.containsKey("tier") ? (String) dict.get("tier") : "WOOD",
                    dict.containsKey("damage") ?  (int) Math.round((double) dict.get("damage")) : 2,
                    dict.containsKey("pierce") ? (int) Math.round((double) dict.get("pierce")) : 1,
                    dict.containsKey("range") ? (int) Math.round((double) dict.get("range")) : 8,
                    dict.containsKey("durability") ? (int) Math.round((double) dict.get("durability")) : -1,
                    dict.containsKey("cooldown") ? (int) Math.round((double) dict.get("cooldown")) : 15,
                    dict.containsKey("charge_time") ? (int) Math.round((double) dict.get("charge_time")) : ConfigEntries.StaffsDefaultCharge,
                    dict.containsKey("xp_cost") ? (int) Math.round((double) dict.get("xp_cost")) : ConfigEntries.StaffsDefaultCost,
                    dict.containsKey("fire_resistant") && (boolean) dict.get("fire_resistant"),
                    dict.containsKey("dependency") ? String.valueOf(dict.get("dependency")) : "minecraft"
            );
        }

        public StaffInfo(String tier, int dmg, int pierce, int range, int dura, int cooldown, int charge_time,
                         int xp_cost, boolean fire_res, String dependency) {
            try {
                this.tier = Tiers.valueOf(tier);
            }
            catch (IllegalArgumentException e) {
                this.tier = Tiers.WOOD;
            }
            this.damage = dmg;
            this.pierce = pierce;
            this.range = range;
            this.durability = (dura == -1) ?
                (int) Math.round(this.tier.getUses() * 0.7) : dura;
            this.cooldown = Math.max(cooldown, 0);
            this.charge_time = Math.max(charge_time, 1);
            this.xp_cost = xp_cost;
            this.fireRes = fire_res;
            this.dependency = dependency;
        }

        @Override
        public String toString() {
            return "StaffInfo{" +
                    "tier=" + tier +
                    ", damage=" + damage +
                    ", pierce=" + pierce +
                    ", range=" + range +
                    ", durability=" + durability +
                    ", cooldown=" + cooldown +
                    ", charge_time=" + charge_time +
                    ", xp_cost=" + xp_cost +
                    ", fireRes=" + fireRes +
                    ", dependency=" + dependency +
                    '}';
        }
    }


    public static <T> T get(String path, T fallback) {
        return new ConfigEntry<>(path, fallback).get();
    }

    public static void register() {
        register(false);
    }

    public static void register(boolean force) {
        String path = System.getProperty("user.dir") + File.separator +
                "config" + File.separator + Sortilege.MOD_ID + "-common.json";

        Sortilege.log("Loading Configs for Sortilege");

        // Create config file if it doesn't exist already
        File config = new File(path);
        boolean create = !config.isFile();

        if (create || force) {
            try {
                config.delete();
                config.createNewFile();

                FileWriter writer = new FileWriter(path);
                writer.write(DEFAULT_CONFIG);
                writer.close();

                Sortilege.log("Sortilege Config file created");
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }


        String configContent = DEFAULT_CONFIG;
        try {
            configContent = FileUtils.readFileToString(config, StandardCharsets.UTF_8);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        CONFIG = new Gson().fromJson(configContent, Map.class);
        ConfigEntries.reload();

        if (!force && (RELOAD.get() || VERSION.get() < getVersion())) {
            register(true);
            return;
        }


        List<Pair<String, StaffInfo>> result = new ArrayList<>();
        for (Map<String, Map<String, ?>> staff : ConfigEntries.StaffEntries) {
            String id = String.valueOf(List.of(staff.keySet().toArray()).get(0));
            result.add(new Pair<>(id, new StaffInfo(staff.get(id))));
        }
        STAFFS = result;
    }

    public static double getVersion() {
        String text = DEFAULT_CONFIG;
        int start = 0;

        while (!List.of('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '.').contains(text.charAt(start))) {
            start++;
        }
        int end = start + 1;
        while (List.of('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '.').contains(text.charAt(end))) {
            end++;
        }

        return Double.parseDouble(text.substring(start, end));
    }


    public static final String DEFAULT_CONFIG = """
    {
      "TECHNICAL": {
        "VERSION_DO_NOT_EDIT": 1.3,
        "FORCE_RESET": false
      },

      "witch_hat": {
        "drop_chance": 0.1,
        "xp_bonus": 3
      },
      "enchantments": {
        "enchant_limiter": {
          "default": 3,
          "override_mode": "relative",
          "overrides": {
            "minecraft:golden_shovel": 2,
            "minecraft:golden_pickaxe": 2,
            "minecraft:golden_axe": 2,
            "minecraft:golden_hoe": 2,
            "minecraft:golden_sword": 2,
            "minecraft:golden_helmet": 2,
            "minecraft:golden_chestplate": 2,
            "minecraft:golden_leggings": 2,
            "minecraft:golden_boots": 2,
            "sortilege:golden_staff": 2
          }
        },
        "magic_protection_protection_compatibility": false
      },
      "experience": {
        "increased_enchant_costs": true,
        "costs": [5, 15, 30],
        "level_cap": 100
      },
      "show_coordinates_on_death": true,
      "staffs": {
        "use_hd_particles": false,
        "default_xp_cost": 0,
        "default_charge_time": 1,
        "entries": [
          {
            "wooden_staff": {
              "tier": "WOOD",
              "damage": 3,
              "pierce": 1,
              "range": 6,
              "cooldown": 15
            }
          },
          {
            "stone_staff": {
              "tier": "STONE",
              "damage": 4,
              "pierce": 1,
              "range": 8,
              "cooldown": 20
            }
          },
          {
            "iron_staff": {
              "tier": "IRON",
              "damage": 5,
              "pierce": 1,
              "range": 10,
              "cooldown": 15
            }
          },
          {
            "golden_staff": {
              "tier": "GOLD",
              "damage": 3,
              "pierce": 2,
              "range": 14,
              "cooldown": 10
            }
          },
          {
            "diamond_staff": {
              "tier": "DIAMOND",
              "damage": 5,
              "pierce": 2,
              "range": 12,
              "cooldown": 15
            }
          },
          {
            "netherite_staff": {
              "tier": "NETHERITE",
              "damage": 6,
              "pierce": 3,
              "range": 16,
              "fire_resistant": true,
              "cooldown": 20
            }
          }
        ]
      }
    }""";
}
