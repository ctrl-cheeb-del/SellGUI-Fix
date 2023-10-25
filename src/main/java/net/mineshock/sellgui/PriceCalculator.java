package net.mineshock.sellgui;

import net.mineshock.sellgui.commands.CustomItemsCommand;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;

public class PriceCalculator {

    private static SellGUIMain main;
    private static PriceCalculator priceCalculator;

    public PriceCalculator(SellGUIMain main) {
        this.main = main;
    }


    public static SellGUIMain getMain() {

        return main;
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    // Gets the price of an item, if Player is online, includes multiplier
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

    // Gets the price of an item, if a Player is not online, does not include multiplier
    public double getPrice(ItemStack itemStack) {
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
        return round(price, 3);
    }



}
