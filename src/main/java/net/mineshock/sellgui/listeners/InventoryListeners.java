package net.mineshock.sellgui.listeners;

import net.mineshock.sellgui.SellGUI;
import net.mineshock.sellgui.SellGUIMain;
import net.mineshock.sellgui.commands.CustomItemsCommand;
import net.mineshock.sellgui.commands.SellCommand;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

public class InventoryListeners implements Listener {
    private static SellGUIMain main;

    public InventoryListeners(SellGUIMain main) {
        this.main = main;
    }

    @EventHandler
    public void inventoryClose(InventoryCloseEvent event) {
        if (SellCommand.isSellGUI(event.getInventory())) {
            dropItems(event.getInventory(), (Player) event.getPlayer());
            SellCommand.getSellGUIs().remove(SellCommand.getSellGUI(event.getInventory()));
        } else if (event.getInventory().equals(CustomItemsCommand.getMenu())) {
            CustomItemsCommand.addToList();
            CustomItemsCommand.saveStuff();
        }
    }

    @EventHandler
    public void inventoryClick(InventoryClickEvent event) {
        if (SellCommand.openSellGUI(((Player) event.getWhoClicked()))) {
            SellGUI sellGUI = SellCommand.getSellGUI(((Player) event.getWhoClicked()).getPlayer());
            Player p = ((Player) event.getWhoClicked());
            if (event.getClickedInventory() != null && event.getClickedInventory().getItem(event.getSlot()) != null) {
                if (event.getClickedInventory().equals(event.getWhoClicked().getOpenInventory().getTopInventory())) {
                    if (event.isShiftClick()) {
                        event.setResult(Event.Result.DENY);
                        event.setCancelled(true);
                        return;
                    }
                }
                ItemStack itemStack = event.getClickedInventory().getItem(event.getSlot());
                if (itemStack.hasItemMeta() && itemStack.getItemMeta().getPersistentDataContainer().has(new NamespacedKey(main, "sellgui"), PersistentDataType.BYTE)){
                    event.setResult(Event.Result.DENY);
                    event.setCancelled(true);
                }
                sellGUI.addSellItem();
                if (itemStack.isSimilar(SellCommand.getSellGUI(p).getConfirmItem())) {
                    sellGUI.sellItems(sellGUI.getMenu());
                    event.setResult(Event.Result.DENY);
                    event.setCancelled(true);
                } else if (itemStack.isSimilar(SellGUI.getFiller())) {
                    event.setResult(Event.Result.DENY);
                    event.setCancelled(true);
                } else if (itemStack.isSimilar(SellGUI.getSellItem())) {
                    SellCommand.getSellGUI(p).makeConfirmItem();
                    SellCommand.getSellGUI(p).setConfirmItem();
                    event.setResult(Event.Result.DENY);
                    event.setCancelled(true);
                }
            }
        } else if (CustomItemsCommand.getMenu().equals(event.getClickedInventory())) {
            if (event.getClickedInventory() != null && event.getClickedInventory().getItem(event.getSlot()) != null && !CustomItemsCommand.clickable(event.getClickedInventory().getItem(event.getSlot()))) {
                if (event.getClickedInventory().getItem(event.getSlot()).isSimilar(CustomItemsCommand.getBack())) {
                    CustomItemsCommand.lastPage();
                } else if (event.getClickedInventory().getItem(event.getSlot()).isSimilar(CustomItemsCommand.getNext())) {
                    CustomItemsCommand.nextPage();
                } else if (event.getClickedInventory().getItem(event.getSlot()).isSimilar(CustomItemsCommand.getDelete())) {
                    CustomItemsCommand.removeItem(event.getSlot() + 9);
                }
                event.setResult(Event.Result.DENY);
                event.setCancelled(true);
            }
        }
    }


    public static void dropItems(Inventory inventory, Player player) {
        for (ItemStack itemStack : inventory.getContents()) {
            if (itemStack != null && !sellGUIItem(itemStack, player)) {
                if (main.getConfig().getBoolean("drop-items-on-close")) {
                    player.getWorld().dropItem(player.getLocation(), itemStack);
                    inventory.remove(itemStack);
                }else{
                    if(player.getInventory().firstEmpty() != -1){
                        player.getInventory().addItem(itemStack);
                        inventory.remove(itemStack);
                    }else{
                        player.getWorld().dropItem(player.getLocation(), itemStack);
                        inventory.remove(itemStack);
                    }
                }
            }
        }
    }

    public static boolean sellGUIItem(ItemStack i, Player player) {
        return i != null && (i.isSimilar(SellGUI.getSellItem()) || i.isSimilar(SellGUI.getFiller()) || i.isSimilar(SellCommand.getSellGUI(player).getConfirmItem()));
    }

}
