package net.mineshock.sellguifix;

import net.mineshock.sellguifix.commands.CustomItemsCommand;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.PermissionAttachmentInfo;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;

public class SellGUIAPI {

    private SellGUIMain main;

    public SellGUIAPI(SellGUIMain main) {
        this.main = main;
    }

    /**
     * Returns the double value of the ItemStack
     * with multipliers/bonuses and other price modifications include, Amount is not calculated
     *
     * @param itemStack ItemStack to be evaluated
     * @param player    player Player's who's permissions should be evaluated, nullable
     * @return The double value of the ItemStack provided, ItemStack amount is not evaluated or calculated
     */
    public double getPrice(ItemStack itemStack, @Nullable Player player) {
        double price = 0.0D;
        if (!main.getConfig().getBoolean("sell-all-command-sell-enchanted") && itemStack.getEnchantments().size() > 0) {
            return price;
        }
        if (CustomItemsCommand.getPrice(itemStack) != -1.0D)
            return CustomItemsCommand.getPrice(itemStack);
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
                if (main.getConfig().getBoolean("use-permission-bonuses-on-essentials")) {
                    double temp = round(main.getEssentialsHolder().getPrice(itemStack).doubleValue(), main.getConfig().getInt("places-to-round"));
                    if (player != null) {
                        for (PermissionAttachmentInfo pai : player.getEffectivePermissions()) {
                            if (pai.getPermission().contains("sellgui.bonus.") && pai.getValue()) {
                                if (temp != 0) {
                                    temp += Double.parseDouble(pai.getPermission().replaceAll("sellgui.bonus.", ""));
                                }
                            } else if (pai.getPermission().contains("sellgui.multiplier.") && pai.getValue()) {
                                temp *= Double.parseDouble(pai.getPermission().replaceAll("sellgui.multiplier.", ""));
                            }
                        }
                    }
                    if (main.getConfig().getBoolean("round-places")) {
                        return round(temp, main.getConfig().getInt("places-to-round"));
                    } else {
                        return temp;
                    }
                } else {
                    return round(main.getEssentialsHolder().getPrice(itemStack).doubleValue(), main.getConfig().getInt("places-to-round"));
                }
            }
        }
        if (itemStack != null && !(itemStack.getType() == Material.AIR) && this.main.getItemPricesConfig().contains(itemStack.getType().name())) {
            price = this.main.getItemPricesConfig().getDouble(itemStack.getType().name());
        }
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
        if (player != null) {
            for (PermissionAttachmentInfo pai : player.getEffectivePermissions()) {
                if (pai.getPermission().contains("sellgui.bonus.")) {
                    if (price != 0) {
                        price += Double.parseDouble(pai.getPermission().replaceAll("sellgui.bonus.", ""));
                    }
                } else if (pai.getPermission().contains("sellgui.multiplier.")) {
                    price *= Double.parseDouble(pai.getPermission().replaceAll("sellgui.multiplier.", ""));
                }
            }
        }
        return round(price, 3);
    }

    /**
     * Returns the pure price from the SellGUI Configs
     *
     * @param itemStack ItemStack to be evaluated
     * @return the pure price set in the SellGUI Configs
     */
    public double getPurePrice(ItemStack itemStack) {
        double price = 0;
        if (itemStack != null && !(itemStack.getType() == Material.AIR) && this.main.getItemPricesConfig().contains(itemStack.getType().name())) {
            price = this.main.getItemPricesConfig().getDouble(itemStack.getType().name());
        }
        return price;
    }

    /**
     * Opens SellGUI for player
     *
     * @param player Player who the GUI should be opened to
     */
    public void openSellGUI(Player player){
        main.getSellCommand().getSellGUIS().add(new SellGUI(this.main, player));
    }


    private static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value + "");
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}
