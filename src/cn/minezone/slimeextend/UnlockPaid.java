package cn.minezone.slimeextend;

import com.github.shawhoi.pointcommand.Main;
import com.github.shawhoi.pointcommand.PointCommandAPI;
import net.milkbowl.vault.economy.Economy;
import org.black_ixx.playerpoints.PlayerPoints;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class UnlockPaid extends JavaPlugin {

    private YamlConfiguration config;
    private List<String> tab = new ArrayList<>();
    private PointCommandAPI pointCommandAPI;
    private PlayerPointsAPI playerPointsAPI;
    private Economy economy = null;

    @Override
    public void onEnable() {

        Metrics metrics = new Metrics(this);

        Bukkit.getConsoleSender().sendMessage("§c准备读入粘液科技物品列表");

        saveResource("config.yml", false);

        config = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "config.yml"));

        File dataFolder = new File(getDataFolder().getParentFile(), "Slimefun");

        //Plugin slime = Bukkit.getPluginManager().getPlugin("Slimefun");
        YamlConfiguration c = YamlConfiguration.loadConfiguration(new File(dataFolder, "Items.yml"));
        YamlConfiguration c2 = YamlConfiguration.loadConfiguration(new File(dataFolder, "Researches.yml"));

        for (String itemName : c.getKeys(false)) {
            if (!config.getBoolean(itemName + ".enable", false)) {
                Bukkit.getConsoleSender().sendMessage("§a读入物品：" + itemName);
                config.set(itemName + ".enable", false);
                config.set(itemName + ".name", config.getString(itemName + ".name", itemName));
                config.set(itemName + ".item", config.getLong(itemName + ".item", 0));
                config.set(itemName + ".point", config.getInt(itemName + ".point", 0));
                config.set(itemName + ".coupon", config.getInt(itemName + ".coupon", 0));
                config.set(itemName + ".exp", config.getInt(itemName + ".exp", 0));
                config.set(itemName + ".money", config.getLong(itemName + ".money", 0));
                config.set(itemName + ".required-permissions", null);
            } else {
                tab.add(itemName);
            }
            Bukkit.getConsoleSender().sendMessage("§a设置权限：" + itemName);
            c.set(itemName + ".required-permission", "slime.unlock." + itemName);

        }
        for (String key : c2.getKeys(false)) {
            c2.set(key + ".cost", 0);
        }
        try {
            config.save(new File(getDataFolder(), "config.yml"));
            c.save(new File(dataFolder, "Items.yml"));
            c2.save(new File(dataFolder, "Researches.yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        Bukkit.getConsoleSender().sendMessage("§a完成对粘液科技物品的加载！");

        if (Bukkit.getPluginManager().getPlugin("PointCommand") != null) {
            pointCommandAPI = new PointCommandAPI(Main.class.cast(Bukkit.getPluginManager().getPlugin("PointCommand")));//愚蠢的东西
        }

        if (Bukkit.getPluginManager().getPlugin("PointCommand") != null) {
            playerPointsAPI = PlayerPoints.class.cast(getServer().getPluginManager().getPlugin("PlayerPoints")).getAPI();
        }

        RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economyProvider != null) {
            economy = economyProvider.getProvider();
        }

        Bukkit.getPluginCommand("gui").setExecutor(new GUI(config, tab, pointCommandAPI, playerPointsAPI, economy));
        Bukkit.getPluginManager().registerEvents(new ClickListener(config), this);
    }

    @Override
    public boolean onCommand(CommandSender s, Command cmd, String label, String[] a) {
        if (a.length == 1) {
            if (config.getBoolean(a[0] + ".enable", false)) {
                boolean flag = false;
                for (String per : config.getStringList(a[0] + ".required-permissions")) {
                    if (!s.hasPermission(per)) {
                        s.sendMessage("§c你缺少权限“" + per + "”");
                        flag = true;
                    }
                }
//                if (pointCommandAPI != null) {
//                    if (pointCommandAPI.getPlayerPoint(s.getName()) < config.getInt(a[0] + ".point", 0)) {
//                        s.sendMessage("§c你没有足够的积分，需要" + config.getInt(a[0] + ".point", 0) + "，但你只有" + pointCommandAPI.getPlayerPoint(s.getName()));
//                        flag = true;
//                    }
//                }
                if (playerPointsAPI != null) {
                    if (playerPointsAPI.look(s.getName()) < config.getInt(a[0] + ".coupon", 0)) {
                        s.sendMessage("§c你没有足够的点券，需要" + config.getInt(a[0] + ".point", 0) + "，但你只有" + pointCommandAPI.getPlayerPoint(s.getName()));
                        flag = true;
                    }
                }

                Player p = (Player) s;
                if (p.getLevel() < config.getInt(a[0] + ".exp")) {
                    s.sendMessage("§c你没有足够的等级，需要" + config.getInt(a[0] + ".exp", 0) + "，但你只有" + p.getLevel());
                    flag = true;
                }
                if (economy != null) {
                    if (!economy.has(p, config.getLong(a[0] + ".money", 0))) {
                        s.sendMessage("§c你没有足够的钱，需要" + config.getInt(a[0] + ".money", 0) + "，但你只有" + economy.getBalance(p));
                        flag = true;
                    }
                }

                if (flag) {
                    return true;
                }
                if (s.hasPermission("slime.unlock." + a[0])) {
                    s.sendMessage("§c你已经解锁过该研究，请勿重复解锁");
                    return true;
                }
                p.setLevel(p.getLevel() - config.getInt(a[0] + ".exp", 0));
                //私有插件，已经你们别管就好
//                if (pointCommandAPI != null) {
//                    pointCommandAPI.delPlayerPoint(s.getName(), config.getInt(a[0] + ".point", 0));
//                }
                //点券
                if (playerPointsAPI != null) {
                    playerPointsAPI.take(s.getName(), config.getInt(a[0] + ".coupon", 0));
                }
                if (economy != null) {
                    economy.withdrawPlayer(p, config.getLong(a[0] + ".money", 0));
                }
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "manudelp " + s.getName() + " slime.unlock." + a[0]);
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "manuaddp " + s.getName() + " slime.unlock." + a[0]);
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "sf research " + s.getName() + " " + a[0]);
                return true;
            } else {
                s.sendMessage(ChatColor.RED + "研究不存在或未启用");
                return true;
            }
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return tab;
    }
}
