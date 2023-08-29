package net.lyof.sortilege.configs;

import com.google.gson.Gson;
import net.lyof.sortilege.Sortilege;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ModJsonConfigs {
    public static final ConfigEntry VERSION = new ConfigEntry("TECHNICAL.VERSION_DO_NOT_EDIT", 0);
    public static final ConfigEntry RELOAD = new ConfigEntry("TECHNICAL.FORCE_RELOAD", false);

    public static Map CONFIG = new HashMap<>();
    public static final String DEFAULT_CONFIG = """
{
  "TECHNICAL": {
    "VERSION_DO_NOT_EDIT": 1,
    "FORCE_RELOAD": false
  },
  
  "enchantments": {
    "enchant_limiter": {
      "default": 3,
      "overrides": {
      }
    }
  },
  "staffs": {
    "wooden_staff": {
      "tier": "WOOD",
      "damage": 3,
      "pierce": 1,
      "range": 6,
      "cooldown": 15
    },
    "stone_staff": {
      "tier": "STONE",
      "damage": 4,
      "pierce": 1,
      "range": 8,
      "cooldown": 20
    },
    "iron_staff": {
      "tier": "IRON",
      "damage": 5,
      "pierce": 1,
      "range": 10,
      "cooldown": 15
    },
    "golden_staff": {
      "tier": "GOLD",
      "damage": 3,
      "pierce": 2,
      "range": 14,
      "cooldown": 10
    },
    "diamond_staff": {
      "tier": "DIAMOND",
      "damage": 5,
      "pierce": 2,
      "range": 12,
      "cooldown": 15
    },
    "netherite_staff": {
      "tier": "NETHERITE",
      "damage": 6,
      "pierce": 3,
      "range": 16,
      "fire_resistant": true,
      "cooldown": 20
    }
  }
}""";

    public static Map<String, StaffInfo> STAFFS = new HashMap<>();

    public static class ConfigEntry {
        public List<String> path;
        @Nullable public Object fallback;

        public ConfigEntry(String path) {
            this(path, null);
        }

        public ConfigEntry(String path, Object fallback) {
            this.path = List.of(path.split("\\."));
            this.fallback = fallback;
        }

        public String get() {
            return this.get(this.fallback);
        }

        public String get(Object fallback) {
            Map next = CONFIG;

            for (String step : this.path) {
                try {
                    next = (Map) next.get(step);
                }
                catch (Exception e) {
                    if (Objects.equals(step, this.path.get(this.path.size() - 1)))
                        return String.valueOf(next.get(step));
                    else
                        return String.valueOf(fallback);
                }
                if (next == null)
                    return String.valueOf(fallback);
            }
            return String.valueOf(next);
        }
    }

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
                    dict.containsKey("charge_time") ? (int) Math.round((double) dict.get("charge_time")) : 1,
                    dict.containsKey("xp_cost") ? (int) Math.round((double) dict.get("xp_cost")) : 0,
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
            this.cooldown = cooldown;
            this.charge_time = charge_time;
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


    public static void register() {
        register(false);
    }

    public static void register(boolean force) {
        String path = Minecraft.getInstance().gameDirectory.getAbsolutePath();
        while (path.length() > 0 && !path.endsWith("\\"))
            path = path.substring(0, path.length() - 1);
        path += "config\\" + Sortilege.MOD_ID + "-common.json";

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

                Sortilege.log("Staff Config file created");
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
        Map<String, Map<String, Map<String, Map>>> json = new Gson().fromJson(configContent, Map.class);
        CONFIG = json;

        if (Boolean.parseBoolean(RELOAD.get()) || Double.parseDouble(VERSION.get()) < getVersion()) {
            register(true);
            return;
        }
        Sortilege.log("RELOADDD " + Boolean.parseBoolean(RELOAD.get()) + " " + RELOAD.get());


        Map<String, StaffInfo> result = new HashMap<>();
        for (String id : json.get("staffs").keySet()) {
            result.put(id, new StaffInfo(json.get("staffs").get(id)));
        }

        STAFFS = result;
    }

    public static double getVersion() {
        String text = DEFAULT_CONFIG;
        int start = 0;

        while (!List.of('0', '1', '2', '3', '4', '5', '6', '7', '8', '9').contains(text.charAt(start))) {
            start++;
        }
        int end = start + 1;
        while (List.of('0', '1', '2', '3', '4', '5', '6', '7', '8', '9').contains(text.charAt(end))) {
            end++;
        }

        return Double.parseDouble(text.substring(start, end));
    }
}
