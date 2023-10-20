package net.mineshock.sellgui;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.List;


public class SellWand implements Listener {

    private final SellGUIMain main;

    private final NamespacedKey wandKey;

    private final HashMap<Player, Chest> confirmMap = new HashMap<>();

    public SellWand(SellGUIMain main) {
        this.main = main;
        this.wandKey = new NamespacedKey(main, "sellwand");
    }

    public ItemStack create(double multiplier) {
        ItemStack stack = new ItemStack(Material.STICK);
        ItemMeta meta = stack.getItemMeta();

        meta.setDisplayName(ChatColor.GOLD + "§lSell Wand");
        meta.setLore(List.of("",ChatColor.GRAY + "| Multiplier: x" + multiplier, ChatColor.GRAY + "| Right-click a chest to sell its contents"));
        meta.addEnchant(new Glow(main.key), 1, true);

        meta.getPersistentDataContainer().set(this.wandKey, PersistentDataType.DOUBLE, multiplier);

        stack.setItemMeta(meta);
        return stack;
    }

    @EventHandler
    public void onRightClick(PlayerInteractEvent event) {

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) { return; }
        if (event.getPlayer().isSneaking()) { return; }
        if (event.getClickedBlock().getType() != Material.CHEST) { return; }
        if (event.getPlayer().getItemInHand().getType() != Material.STICK) { return; }

        if (!event.getPlayer().getItemInHand().getItemMeta().getPersistentDataContainer().has(this.wandKey, PersistentDataType.DOUBLE)) { return; }

        if (!SuperiorSkyblockAPI.getPlayer(event.getPlayer()).isInsideIsland()) { return; }

        event.setCancelled(true);

        Chest chest = (Chest) event.getClickedBlock().getState();
        Inventory inventory = chest.getInventory();

        double multiplier = event.getPlayer().getItemInHand().getItemMeta().getPersistentDataContainer().get(this.wandKey, PersistentDataType.DOUBLE);

        double total = 0.0D;
        for (ItemStack itemStack : inventory.getContents()) {
            if (itemStack != null)
                total += main.getSellALlCommand().getPrice(itemStack, event.getPlayer()) * itemStack.getAmount();
        }

        double price = multiplier * total;


        if (!confirmMap.containsKey(event.getPlayer())) {
            event.getPlayer().sendMessage(ChatColor.GREEN + "Chest value: $" + price + ". Right-click to confirm sell.");
            confirmMap.put(event.getPlayer(), chest);

        } else {
            Chest storedChest = confirmMap.get(event.getPlayer());

            if (storedChest.equals(chest)) {
                this.main.getEcon().depositPlayer(event.getPlayer(), price);

                for (ItemStack itemStack : inventory) {
                    if (itemStack != null && main.getSellALlCommand().getPrice(itemStack, event.getPlayer()) != 0.0D) {
                        inventory.remove(itemStack);
                    }
                }

                event.getPlayer().sendMessage(ChatColor.GREEN + "Chest contents sold for $" + price);
                confirmMap.remove(event.getPlayer());

            } else {
                event.getPlayer().sendMessage(ChatColor.GREEN + "Chest value: $" + price + ". Right-click to confirm sell.");
                confirmMap.replace(event.getPlayer(), chest);

            }
        }
    }

    @EventHandler
    public void onCraft(PrepareItemCraftEvent event) {
        boolean hasMetaData = false;

        for (ItemStack ingredient : event.getInventory().getMatrix()) {
            if (ingredient == null || ingredient.getItemMeta() == null) { continue; }

            if (ingredient.getItemMeta().getPersistentDataContainer().has(this.wandKey, PersistentDataType.DOUBLE) || ingredient.getItemMeta().getPersistentDataContainer().has(this.wandKey, PersistentDataType.DOUBLE)) {
                hasMetaData = true;
                break;
            }
        }

        if (hasMetaData) {
            event.getInventory().setResult(null);
        }
    }

    @EventHandler
    public void onAnvilPrepare(PrepareAnvilEvent event) {

        ItemStack firstInput = event.getInventory().getItem(0);
        ItemStack secondInput = event.getInventory().getItem(1);

        if (firstInput != null && firstInput.getItemMeta() != null) {
            if (firstInput.getItemMeta().getPersistentDataContainer().has(this.wandKey, PersistentDataType.DOUBLE)) {
                event.setResult(null);
            }
        }

        if (secondInput != null && secondInput.getItemMeta() != null) {
            if (secondInput.getItemMeta().getPersistentDataContainer().has(this.wandKey, PersistentDataType.DOUBLE)) {
                event.setResult(null);
            }
        }

    }

    @EventHandler
    public void onSmelt(FurnaceBurnEvent event) {
        ItemStack fuel = event.getFuel();
        if (fuel.getItemMeta() != null) {
            if (fuel.getItemMeta().getPersistentDataContainer().has(this.wandKey, PersistentDataType.DOUBLE)) {
                event.setCancelled(true);
            }
        }
    }


}
