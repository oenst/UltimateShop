package cn.superiormc.ultimateshop.database;

import cn.superiormc.ultimateshop.UltimateShop;
import cn.superiormc.ultimateshop.cache.ServerCache;
import cn.superiormc.ultimateshop.managers.CacheManager;
import cn.superiormc.ultimateshop.managers.ErrorManager;
import cn.superiormc.ultimateshop.objects.buttons.ObjectItem;
import cn.superiormc.ultimateshop.objects.caches.ObjectUseTimesCache;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class YamlDatabase {

    public static void checkData(ServerCache cache) {
        File dir = new File(UltimateShop.instance.getDataFolder() + "/datas");
        if (!dir.exists()) {
            dir.mkdir();
        }
        File file = null;
        if (!cache.server) {
            file = new File(dir, cache.player.getUniqueId() + ".yml");
            if (!file.exists()) {
                YamlConfiguration config = new YamlConfiguration();
                Map<String, Object> data = new HashMap<>();
                try {
                    data.put("playerName", cache.player.getName());
                    for (String key : data.keySet()) {
                        config.set(key, data.get(key));
                    }
                    config.save(file);
                } catch (IOException e) {
                    ErrorManager.errorManager.sendErrorMessage("§x§9§8§F§B§9§8[UltimateShop] §cError: " +
                            "Can not create new data file: " + cache.player.getUniqueId() + ".yml!");
                }
            }
        } else {
            // 新建文件
            file = new File(dir, "global.yml");
            if (!file.exists()) {
                YamlConfiguration config = new YamlConfiguration();
                Map<String, Object> data = new HashMap<>();
                try {
                    data.put("playerName", "global");
                    for (String key : data.keySet()) {
                        config.set(key, data.get(key));
                    }
                    config.save(file);
                } catch (IOException e) {
                    ErrorManager.errorManager.sendErrorMessage("§x§9§8§F§B§9§8[UltimateShop] §cError: " +
                            "Can not create new data file: global.yml!");
                }
            }
        }
        // 次数储存系统
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection useTimeSection = config.getConfigurationSection("useTimes");
        if (useTimeSection != null) {
            for (String shopID : useTimeSection.getKeys(false)) {
                ConfigurationSection tempVal3 = useTimeSection.getConfigurationSection(shopID);
                for (String productID : tempVal3.getKeys(false)) {
                    ConfigurationSection tempVal4 = tempVal3.getConfigurationSection(productID);
                    int buyUseTimes = tempVal4.getInt("buyUseTimes", 0);
                    int sellUseTimes = tempVal4.getInt("sellUseTimes", 0);
                    String lastPurchaseTime = tempVal4.getString("lastBuyTime", null);
                    String lastSellTime = tempVal4.getString("lastSellTime", null);
                    String cooldownPurchaseTime = tempVal4.getString("cooldownBuyTime", null);
                    String cooldownSellTime = tempVal4.getString("cooldownSellTime", null);
                    cache.setUseTimesCache(shopID, productID,
                            buyUseTimes, sellUseTimes,
                            lastPurchaseTime, lastSellTime,
                            cooldownPurchaseTime, cooldownSellTime);
                }
            }
        }
    }

    public static void updateData(ServerCache cache, boolean quitServer) {
        boolean needDelete = false;
        File dir = new File(UltimateShop.instance.getDataFolder()+"/datas");
        if (!dir.exists()) {
            dir.mkdir();
        }
        File file = null;
        Map<String, Object> data = new HashMap<>();
        if (cache.server) {
            data.put("playerName", "global");
            cache = ServerCache.serverCache;
            file = new File(dir, "global.yml");
            if (file.exists()){
                needDelete = true;
            }
        }
        else {
            data.put("playerName", cache.player);
            file = new File(dir, cache.player.getUniqueId() + ".yml");
            if (file.exists()){
                file.delete();
            }
        }
        YamlConfiguration config = new YamlConfiguration();
        // 储存购买次数
        ConfigurationSection useTimesSection = config.createSection("useTimes");
        Map<ObjectItem, ObjectUseTimesCache> tempVal1 = cache.getUseTimesCache();
        for (ObjectItem tempVal4 : tempVal1.keySet()) {
            data.clear();
            ConfigurationSection tempVal5 = useTimesSection.getConfigurationSection(tempVal4.getShop());
            if (tempVal5 == null) {
                tempVal5 = useTimesSection.createSection(tempVal4.getShop());
            }
            ConfigurationSection tempVal6 = tempVal5.getConfigurationSection(tempVal4.getProduct());
            if (tempVal1.get(tempVal4).getBuyUseTimes() != 0) {
                data.put("buyUseTimes", tempVal1.get(tempVal4).getBuyUseTimes());
            }
            if (tempVal1.get(tempVal4).getSellUseTimes() != 0) {
                data.put("sellUseTimes", tempVal1.get(tempVal4).getSellUseTimes());
            }
            if (tempVal1.get(tempVal4).getBuyRefreshTime() != null) {
                data.put("lastBuyTime", tempVal1.get(tempVal4).getLastBuyTime());
            }
            if (tempVal1.get(tempVal4).getSellRefreshTime() != null) {
                data.put("lastSellTime", tempVal1.get(tempVal4).getLastSellTime());
            }
            if (!cache.server && tempVal1.get(tempVal4).getCooldownBuyTime() != null) {
                data.put("cooldownBuyTime", tempVal1.get(tempVal4).getCooldownBuyTime());
            }
            if (!cache.server && tempVal1.get(tempVal4).getCooldownSellTime() != null) {
                data.put("cooldownSellTime", tempVal1.get(tempVal4).getCooldownSellTime());
            }
            for (String key : data.keySet()) {
                if (tempVal6 == null) {
                    tempVal6 = tempVal5.createSection(tempVal4.getProduct());
                }
                tempVal6.set(key, data.get(key));
            }
        }
        if (quitServer) {
            CacheManager.cacheManager.removePlayerCache(cache.player);
        }
        try {
            if (needDelete) {
                file.delete();
            }
            config.save(file);
        } catch (IOException e) {
            ErrorManager.errorManager.sendErrorMessage("§x§9§8§F§B§9§8[UltimateShop] §cError: " +
                    "Can not save data file: " + file.getName() + "!");
        }
    }

}
