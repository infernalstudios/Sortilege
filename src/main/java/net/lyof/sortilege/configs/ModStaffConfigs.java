package net.lyof.sortilege.configs;

import com.google.gson.Gson;
import net.lyof.sortilege.Sortilege;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class ModStaffConfigs {
    public static Map<String, StaffInfo> STAFFS = new HashMap<>();
    public static final String DEFAULT_CONFIG = """
{
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

    public static class StaffInfo {
        public StaffInfo(Map<String, ?> dict) {
            this(
                    dict.containsKey("tier") ? (String) dict.get("tier") : "WOOD",
                    dict.containsKey("damage") ?  (int) Math.round((double) dict.get("damage")) : 2,
                    dict.containsKey("pierce") ? (int) Math.round((double) dict.get("pierce")) : 1,
                    dict.containsKey("range") ? (int) Math.round((double) dict.get("range")) : 8,
                    dict.containsKey("durability") ? (int) Math.round((double) dict.get("durability")) : -1,
                    dict.containsKey("cooldown") ? (int) Math.round((double) dict.get("cooldown")) : 15,
                    dict.containsKey("charge_time") ? (int) Math.round((double) dict.get("charge_time")) : 0,
                    dict.containsKey("fire_resistant") && (boolean) dict.get("fire_resistant")
            );
        }

        public StaffInfo(String tier, int dmg, int pierce, int range, int dura, int cooldown, int charge_time, boolean fire_res) {
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
            this.fireRes = fire_res;
        }

        @Override
        public String toString() {
            return "Staff{" +
                    "tier=" + tier +
                    ", damage=" + damage +
                    ", pierce=" + pierce +
                    ", range=" + range +
                    ", durability=" + durability +
                    ", fireRes=" + fireRes +
                    '}';
        }

        public Tier tier;
        public int damage;
        public int pierce;
        public int range;
        public int durability;
        public int cooldown;
        public int charge_time;
        public boolean fireRes;
    }


    public static Map<String, StaffInfo> read() {
        String path = Minecraft.getInstance().gameDirectory.getAbsolutePath();
        while (path.length() > 0 && !path.endsWith("\\"))
            path = path.substring(0, path.length() - 1);
        path += "config\\sortilege-staffs.json";

        // Create config file if it doesn't exist already
        File config = new File(path);
        boolean create = !config.isFile();

        if (create) {
            try {
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


        Gson gson = new Gson();
        String configContent = DEFAULT_CONFIG;
        try {
            configContent = FileUtils.readFileToString(config, StandardCharsets.UTF_8);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        Map<String, Map<String, Map<String, Map>>> json = gson.fromJson(configContent, Map.class);


        Map<String, StaffInfo> result = new HashMap<>();
        for (String id : json.get("staffs").keySet()) {
            result.put(id, new StaffInfo(json.get("staffs").get(id)));
        }

        STAFFS = result;
        return result;
    }
}
