package cn.minezone.slimeextend;

import com.github.shawhoi.pointcommand.PointCommandAPI;
import net.milkbowl.vault.economy.Economy;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class GUI implements CommandExecutor {

    private YamlConfiguration config;
    private List<String> items;
    private PointCommandAPI pointCommandAPI;
    private PlayerPointsAPI playerPointsAPI;
    private Economy economy;

    public GUI(YamlConfiguration config, List<String> items, PointCommandAPI pointCommandAPI, PlayerPointsAPI playerPointsAPI, Economy economy) {
        this.config = config;
        this.items = items;
        this.pointCommandAPI = pointCommandAPI;
        this.playerPointsAPI = playerPointsAPI;
        this.economy = economy;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] a) {
        int page = 1;
        if (a.length != 1) {
            sender.sendMessage("§c页数格式错误");
            return true;
        }
        try {
            page = Integer.parseInt(a[0]);
        } catch (NumberFormatException e) {
            sender.sendMessage("§c页数格式错误");
            return true;
        }
        if (page <= 0) {
            page = 1;
        }
        int maxPage = (int) Math.ceil(((double) items.size() / 54));
        if (page > maxPage) {
            sender.sendMessage("§c" + page + "太大了，最大只能为" + maxPage);
            return true;
        }
        Inventory inv = Bukkit.createInventory((Player) sender, 54, "§a粘液科技-解锁 By.mcard");

        int t = (page - 1) * 54;
        for (int i = 0; i <= 54 && (i + t) < items.size(); i++) {
            ItemStack item = new ItemStack(config.getInt(items.get(t + i), 1));
            ItemMeta im = item.getItemMeta();

            String color = "§c";
            String tip = ChatColor.RED + "" + ChatColor.BOLD + "  未解锁";

            if (sender.hasPermission("slime.unlock." + items.get(t + i))) {
                color = "§a";
                tip = ChatColor.GREEN + "" + ChatColor.BOLD + "  已解锁";
            }

            im.setDisplayName(color + config.getString(items.get(t + i) + ".name", items.get(t + i)) + tip);

            List<String> lore = new ArrayList<String>();

//            if (pointCommandAPI != null && config.getInt(items.get(t + i) + ".point", 0) != 0) {
//                if (pointCommandAPI.getPlayerPoint(sender.getName()) < config.getInt(items.get(t + i) + ".point", 0)) {
//                    lore.add("§c需要积分" + config.getInt(items.get(t + i) + ".point"));
//                } else {
//                    lore.add("§a需要积分" + config.getInt(items.get(t + i) + ".point"));
//                }
//            }
            if (playerPointsAPI != null && config.getInt(items.get(t + i) + ".coupon", 0) != 0) {
                if (playerPointsAPI.look(sender.getName()) < config.getInt(items.get(t + i) + ".coupon", 0)) {
                    lore.add("§c需要点券" + config.getInt(items.get(t + i) + ".coupon"));
                } else {
                    lore.add("§a需要点券" + config.getInt(items.get(t + i) + ".coupon"));
                }
            }
            if (economy != null && config.getLong(items.get(t + i) + ".money", 0) != 0) {
                if (!economy.has((Player) sender, config.getLong(items.get(t + i) + ".money", 0))) {
                    lore.add("§c需要钱" + config.getInt(items.get(t + i) + ".money"));
                } else {
                    lore.add("§a需要钱" + config.getInt(items.get(t + i) + ".money"));
                }
            }
            if (config.getInt(items.get(t + i) + ".exp", 0) != 0) {
                if (((Player) sender).getLevel() < config.getInt(items.get(t + i) + ".exp")) {
                    lore.add("§c需要等级" + config.getInt(items.get(t + i) + ".exp"));
                } else {
                    lore.add("§a需要等级" + config.getInt(items.get(t + i) + ".money"));
                }
            }
            im.setLore(lore);
            item.setItemMeta(im);
            inv.setItem(i, item);
            ((Player)sender).openInventory(inv);
        }


        return false;
    }
}
