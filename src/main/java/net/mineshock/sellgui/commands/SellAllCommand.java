package net.mineshock.sellgui.commands;

import net.mineshock.sellgui.SellGUIMain;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;


public class SellAllCommand implements CommandExecutor {
    private SellGUIMain main;

    public SellAllCommand(SellGUIMain main) {
        this.main = main;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            return true;
        }
        Player player = (Player) sender;
        if (args.length == 1 && args[0].equalsIgnoreCase("confirm")) {
            PlayerInventory inventory = player.getInventory();
            if (inventory.getItemInOffHand().getType() != Material.AIR) {
                if (getPrice(inventory.getItemInOffHand(), player) != 0.0D) {
                    player.sendMessage(ChatColor.RED + "Please remove all sellable items from your offhand!");
                    return true;
                }
            }
            sellItems(inventory, player);
        } else {
            player.sendMessage(color(main.getLangConfig().getString("sellall-confirm-message")));
        }
        return true;
    }

    public double getPrice(ItemStack itemStack, Player player) {
        double price = 0.0D;
        if(!main.getConfig().getBoolean("sell-all-command-sell-enchanted") && itemStack.getEnchantments().size() > 0){
            return price;
        }
        if (CustomItemsCommand.getPrice(itemStack) != -1.0D) {
            price = CustomItemsCommand.getPrice(itemStack);
        }
        else {
            ArrayList<String> flatBonus = new ArrayList<>();
            if (this.main.getItemPricesConfig().getStringList("flat-enchantment-bonus") != null)
                for (String s : this.main.getItemPricesConfig().getStringList("flat-enchantment-bonus"))
                    flatBonus.add(s);
            ArrayList<String> multiplierBonus = new ArrayList<>();
            if (this.main.getItemPricesConfig().getStringList("multiplier-enchantment-bonus") != null)
                for (String s : this.main.getItemPricesConfig().getStringList("multiplier-enchantment-bonus"))
                    multiplierBonus.add(s);
            if (this.main.hasEssentials() && main.getConfig().getBoolean("use-essentials-price")) {
                if (main.getEssentialsHolder().getEssentials() != null) {
                    return round(main.getEssentialsHolder().getPrice(itemStack).doubleValue(), 3);
                }
            }
            if (itemStack != null && !(itemStack.getType() == Material.AIR) &&
                    this.main.getItemPricesConfig().contains(itemStack.getType().name()))
                price = this.main.getItemPricesConfig().getDouble(itemStack.getType().name());
            if (itemStack != null && itemStack.getItemMeta().hasEnchants()) {
                for (Enchantment enchantment : itemStack.getItemMeta().getEnchants().keySet()) {
                    for (String s : flatBonus) {
                        String[] temp = s.split(":");
                        if (temp[0].equalsIgnoreCase(enchantment.getKey().getKey()) && temp[1]
                                .equalsIgnoreCase(itemStack.getEnchantmentLevel(enchantment) + ""))
                            price += Double.parseDouble(temp[2]);
                    }
                }
                for (Enchantment enchantment : itemStack.getItemMeta().getEnchants().keySet()) {
                    for (String s : multiplierBonus) {
                        String[] temp2 = s.split(":");
                        if (temp2[0].equalsIgnoreCase(enchantment.getKey().getKey()) && temp2[1]
                                .equalsIgnoreCase(itemStack.getEnchantmentLevel(enchantment) + ""))
                            price *= Double.parseDouble(temp2[2]);
                    }
                }
            }
        }

        for(PermissionAttachmentInfo pai : player.getEffectivePermissions()){
            if(pai.getPermission().contains("sellgui.bonus.")){
                if(price != 0){
                    price += Double.parseDouble(pai.getPermission().replaceAll("sellgui.bonus.", ""));
                }
            }else if(pai.getPermission().contains("sellgui.multiplier.")){
                price *= Double.parseDouble(pai.getPermission().replaceAll("sellgui.multiplier.",""));
            }
        }
        return round(price, 3);
    }

    public double getTotal(Inventory inventory, Player player) {
        double total = 0.0D;
        for (ItemStack itemStack : inventory.getContents()) {
            if (itemStack != null)
                total += getPrice(itemStack, player) * itemStack.getAmount();
        }
        return total;
    }

    private final DecimalFormat decimalFormat = new DecimalFormat("#,###");

    public void sellItems(PlayerInventory inventory, Player player) {
        this.main.getEcon().depositPlayer((OfflinePlayer) player, getTotal(inventory, player));
        player.sendMessage(color(this.main.getLangConfig().getString("sold-message").replaceAll("%total%", decimalFormat.format(round(getTotal(inventory, player),3)))));
        for (ItemStack itemStack : inventory) {
            if (itemStack != null && getPrice(itemStack, player) != 0.0D) {
                inventory.removeItem(itemStack);
            }
        }
    }

    public static String color(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}
