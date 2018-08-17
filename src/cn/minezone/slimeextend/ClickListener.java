package cn.minezone.slimeextend;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;

public class ClickListener implements Listener {

    private YamlConfiguration config;

    public ClickListener(YamlConfiguration config) {
        this.config = config;
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (e.getClickedInventory() == null || e.getClickedInventory().getTitle() == null || !e.getInventory().getTitle().equals("§a粘液科技-解锁 By.mcard")) {
            return;
        }
        e.setCancelled(true);
        Player p = (Player) e.getWhoClicked();
        if (!e.getClickedInventory().getTitle().equals("§a粘液科技-解锁 By.mcard")) {
            return;
        }
        try {
            String name = ChatColor.stripColor(e.getCurrentItem().getItemMeta().getDisplayName());
            name = name.replace("  未解锁", "");
            name = name.replace("  已解锁", "");
            for (String key : config.getKeys(false)) {
                if (config.getString(key + ".name", key).equals(name)) {
                    Bukkit.dispatchCommand(p, "unlock " + key);
                    return;
                }
            }
        } catch (Exception e1) {

        }


    }

    public void onDrag(InventoryDragEvent e) {
        if (e.getInventory() == null || e.getInventory().getTitle() == null || e.getInventory().getTitle().equals("§a粘液科技-解锁 By.mcard")) {
            e.setCancelled(true);
            return;
        }
    }

}
