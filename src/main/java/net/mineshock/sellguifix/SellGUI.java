package net.mineshock.sellguifix;

import net.mineshock.sellguifix.commands.CustomItemsCommand;
import net.mineshock.sellguifix.commands.SellCommand;
import net.mineshock.sellguifix.listeners.InventoryListeners;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;

import org.apache.commons.lang.WordUtils;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.persistence.PersistentDataType;

public class SellGUI {
    private final SellGUIMain main;

    private final Player player;

    private static ItemStack sellItem;

    private static ItemStack filler;

    private String menuTitle;

    private Inventory menu;

    private ItemStack confirmItem;

    private int sellItemSlot;

    public SellGUI(SellGUIMain main, Player p) {
        this.main = main;
        this.player = p;
        createItems();
        createMenu();
        p.openInventory(this.menu);
    }

    private void createMenu() {
        this.menu = Bukkit.createInventory(null, this.main.getConfig().getInt("menu-size"), color(this.main.getLangConfig().getString("menu-title")));
        addFiller(this.main.getConfig().getString("menu-filler-location"));
        addSellItem();
    }

    public void addSellItem() {
        String s = this.main.getConfig().getString("menu-filler-location");
        String z = this.main.getConfig().getString("sell-item-location");
        if (!s.equalsIgnoreCase("none") && !s.equalsIgnoreCase("round")) {
            if (s.equalsIgnoreCase("bottom")) {
                bottom(z);
            } else if (s.equalsIgnoreCase("left")) {
                left(z);
            } else if (s.equalsIgnoreCase("right")) {
                right(z);
            } else if (s.equalsIgnoreCase("top")) {
                if (z.equalsIgnoreCase("left")) {
                    this.menu.setItem(0, sellItem);
                } else if (z.equalsIgnoreCase("middle")) {
                    this.menu.setItem(4, sellItem);
                } else if (z.equalsIgnoreCase("right")) {
                    this.menu.setItem(8, sellItem);
                }
            }
        } else if (this.main.getConfig().getString("sell-item-side").equalsIgnoreCase("bottom")) {
            bottom(z);
        } else if (this.main.getConfig().getString("sell-item-side").equalsIgnoreCase("top")) {
            top(z);
        } else if (this.main.getConfig().getString("sell-item-side").equalsIgnoreCase("left")) {
            left(z);
        } else if (this.main.getConfig().getString("sell-item-side").equalsIgnoreCase("right")) {
            right(z);
        }
        makeConfirmItem();
    }

    private void right(String z) {
        if (z.equalsIgnoreCase("left")) {
            this.menu.setItem(this.menu.getSize() - 1, null);
            this.menu.setItem(this.menu.getSize() - 1, sellItem);
            this.sellItemSlot = this.menu.getSize() - 1;
        } else if (z.equalsIgnoreCase("middle")) {
            this.menu.setItem(8 + 9 * this.menu.getSize() / 9 / 2, null);
            this.menu.setItem(8 + 9 * this.menu.getSize() / 9 / 2, sellItem);
            this.sellItemSlot = 8 + 9 * this.menu.getSize() / 9 / 2;
        } else if (z.equalsIgnoreCase("right")) {
            this.menu.setItem(8, null);
            this.menu.setItem(8, sellItem);
            this.sellItemSlot = 8;
        }
    }

    private void top(String z) {
        if (z.equalsIgnoreCase("left")) {
            this.menu.setItem(0, null);
            this.menu.setItem(0, sellItem);
            this.sellItemSlot = 0;
        } else if (z.equalsIgnoreCase("middle")) {
            this.menu.setItem(4, null);
            this.menu.setItem(4, sellItem);
            this.sellItemSlot = 4;
        } else if (z.equalsIgnoreCase("right")) {
            this.menu.setItem(8, null);
            this.menu.setItem(8, sellItem);
            this.sellItemSlot = 8;
        }
    }

    private void left(String z) {
        if (z.equalsIgnoreCase("left")) {
            this.menu.setItem(0, null);
            this.menu.setItem(0, sellItem);
            this.sellItemSlot = 0;
        } else if (z.equalsIgnoreCase("middle")) {
            this.menu.setItem(this.menu.getSize() / 9 / 2 * 9, null);
            this.menu.setItem(this.menu.getSize() / 9 / 2 * 9, sellItem);
            this.sellItemSlot = 9 * this.menu.getSize() / 9 / 2;
        } else if (z.equalsIgnoreCase("right")) {
            this.menu.setItem(this.menu.getSize() - 9, null);
            this.menu.setItem(this.menu.getSize() - 9, sellItem);
            this.sellItemSlot = this.menu.getSize() - 9;
        }
    }

    private void bottom(String z) {
        if (z.equalsIgnoreCase("middle")) {
            this.menu.setItem(this.menu.getSize() - 5, null);
            this.menu.setItem(this.menu.getSize() - 5, sellItem);
            this.sellItemSlot = this.menu.getSize() - 5;
        } else if (z.equalsIgnoreCase("left")) {
            this.menu.setItem(this.menu.getSize() - 9, null);
            this.menu.setItem(this.menu.getSize() - 9, sellItem);
            this.sellItemSlot = this.menu.getSize() - 9;
        } else if (z.equalsIgnoreCase("right")) {
            this.menu.setItem(this.menu.getSize() - 1, null);
            this.menu.setItem(this.menu.getSize() - 1, sellItem);
            this.sellItemSlot = this.menu.getSize() - 1;
        }
    }

    private void createItems() {
        if (this.menuTitle != null || sellItem == null || filler == null) {
            this.menuTitle = this.main.getLangConfig().getString("menu-title");
            NamespacedKey key = new NamespacedKey(main, "sellgui-item");
            sellItem = new ItemStack(Material.getMaterial(this.main.getConfig().getString("sell-item")));
            ItemMeta sellItemMeta = sellItem.getItemMeta();
            sellItemMeta.setDisplayName(color(this.main.getLangConfig().getString("sell-item-name")));
            ArrayList<String> lore = new ArrayList<>();
            for (String s : this.main.getLangConfig().getStringList("sell-item-lore"))
                lore.add(color(s));
            if (this.main.getConfig().getBoolean("sell-item-glimmer")) {
                sellItemMeta.addEnchant(Enchantment.ARROW_INFINITE, 1, false);
                sellItemMeta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ENCHANTS});
            }
            sellItemMeta.setLore(lore);
            sellItemMeta.getPersistentDataContainer().set(key, PersistentDataType.BYTE, Byte.valueOf("1"));
            sellItem.setItemMeta(sellItemMeta);
            filler = new ItemStack(Material.valueOf(this.main.getConfig().getString("menu-filler-type")));
            ItemMeta fillerMeta = filler.getItemMeta();
            fillerMeta.setDisplayName(" ");
            fillerMeta.getPersistentDataContainer().set(key, PersistentDataType.BYTE, Byte.valueOf("1"));
            filler.setItemMeta(fillerMeta);
        }
    }

    private void addFiller(String s) {
        if (s.equalsIgnoreCase("bottom")) {
            for (int i = this.menu.getSize() - 9; i < this.menu.getSize(); i++)
                this.menu.setItem(i, filler);
        } else if (s.equalsIgnoreCase("left")) {
            for (int i = 0; i < this.menu.getSize(); i += 9)
                this.menu.setItem(i, filler);
        } else if (s.equalsIgnoreCase("right")) {
            for (int i = 8; i < this.menu.getSize(); i += 9)
                this.menu.setItem(i, filler);
        } else if (s.equalsIgnoreCase("top")) {
            for (int i = 0; i < 9; i++)
                this.menu.setItem(i, filler);
        } else if (s.equalsIgnoreCase("round")) {
            int i;
            for (i = this.menu.getSize() - 9; i < this.menu.getSize(); i++)
                this.menu.setItem(i, filler);
            for (i = 0; i < this.menu.getSize(); i += 9)
                this.menu.setItem(i, filler);
            for (i = 8; i < this.menu.getSize(); i += 9)
                this.menu.setItem(i, filler);
            for (i = 0; i < 9; i++)
                this.menu.setItem(i, filler);
        }
    }

    public void makeConfirmItem() {
        this.confirmItem = new ItemStack(Material.matchMaterial(this.main.getConfig().getString("confirm-item")));
        ItemMeta itemMeta = this.confirmItem.getItemMeta();
        itemMeta.setDisplayName(color(this.main.getLangConfig().getString("confirm-item-name")));
        confirmItem.setItemMeta(itemMeta);
        if (this.main.getConfig().getBoolean("confirm-item-glimmer")) {
            itemMeta.addEnchant(Enchantment.ARROW_DAMAGE, 1, false);
            itemMeta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ENCHANTS});
        }
        itemMeta.setLore(makeLore());
        itemMeta.getPersistentDataContainer().set(new NamespacedKey(main, "sellgui"), PersistentDataType.BYTE, Byte.valueOf("1"));
        this.confirmItem.setItemMeta(itemMeta);
    }

    public ArrayList<String> makeLore() {
        HashMap<ItemStack, Integer> hashMap = new HashMap<>();
        for (ItemStack i : this.getMenu().getContents()) {
            if (i != null && !InventoryListeners.sellGUIItem(i, this.player)) {
                ItemStack tempItemStack = i.clone();
                tempItemStack.setAmount(1);
                if (!hashMap.containsKey(tempItemStack)) {
                    hashMap.put(tempItemStack, i.getAmount());
                } else {
                    int amount = hashMap.get(tempItemStack);
                    hashMap.put(tempItemStack, amount + i.getAmount());
                }
            }
        }
        ArrayList<String> lore = new ArrayList<>();
        for (ItemStack i : hashMap.keySet()) {
            if (main.getConfig().getBoolean("round-places")) {
                lore.add(color(this.main.getLangConfig().getString("item-total-format").replaceAll("%item%", getItemName(i)).replaceAll("%amount%", hashMap.get(i) + "").replaceAll("%price%", "" +
                        getPrice(i)).replaceAll("%total%", (roundString(new BigDecimal(i.getAmount() * getPrice(i)).toPlainString(), main.getConfig().getInt("places-to-round"))) + "")));
            } else {
                lore.add(color(this.main.getLangConfig().getString("item-total-format").replaceAll("%item%", getItemName(i)).replaceAll("%amount%", hashMap.get(i) + "").replaceAll("%price%", "" +
                        getPrice(i)).replaceAll("%total%", (i.getAmount() * getPrice(i)) + "")));
            }

        }
        if (main.getConfig().getBoolean("round-places")) {
            lore.add(color(this.main.getLangConfig().getString("total-format").replaceAll("%total%", "" + roundString(getTotal(this.menu) + "", main.getConfig().getInt("places-to-round")))));
        } else {
            lore.add(color(this.main.getLangConfig().getString("total-format").replaceAll("%total%", "" + getTotal(this.menu))));
        }
        return lore;
    }

    public void setConfirmItem() {
        this.menu.setItem(this.sellItemSlot, null);
        this.menu.setItem(this.sellItemSlot, this.confirmItem);
    }

    public String getItemName(ItemStack itemStack) {
        if (itemStack.hasItemMeta() && itemStack.getItemMeta().hasDisplayName())
            return itemStack.getItemMeta().getDisplayName();
        return WordUtils.capitalizeFully(itemStack.getType().name().replace('_', ' '));
    }

    public double getPrice(ItemStack itemStack) {
        double price = 0.0D;
        if (CustomItemsCommand.getPrice(itemStack) != -1.0D) {
            price = CustomItemsCommand.getPrice(itemStack);
        }
        else {
            if (this.main.getConfig().getBoolean("prevent-custom-item-selling") && itemStack.hasItemMeta()) {
                return 0.00;
            }
            if (this.main.hasEssentials() && main.getConfig().getBoolean("use-essentials-price")) {
                if (main.getEssentialsHolder().getEssentials() != null) {
                    if (main.getConfig().getBoolean("use-permission-bonuses-on-essentials")) {
                        double temp = round(main.getEssentialsHolder().getPrice(itemStack).doubleValue(), main.getConfig().getInt("places-to-round"));
                        for (PermissionAttachmentInfo pai : player.getEffectivePermissions()) {
                            if (pai.getPermission().contains("sellgui.bonus.") && pai.getValue()) {
                                if (temp != 0) {
                                    temp += Double.parseDouble(pai.getPermission().replaceAll("sellgui.bonus.", ""));
                                }
                            } else if (pai.getPermission().contains("sellgui.multiplier.") && pai.getValue()) {
                                temp *= Double.parseDouble(pai.getPermission().replaceAll("sellgui.multiplier.", ""));
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

            ArrayList<String> flatBonus = new ArrayList<>();
            if (this.main.getItemPricesConfig().getStringList("flat-enchantment-bonus") != null)
                for (String s : this.main.getItemPricesConfig().getStringList("flat-enchantment-bonus")) {
                    flatBonus.add(s);
                }
            ArrayList<String> multiplierBonus = new ArrayList<>();
            if (this.main.getItemPricesConfig().getStringList("multiplier-enchantment-bonus") != null)
                for (String s : this.main.getItemPricesConfig().getStringList("multiplier-enchantment-bonus")) {
                    multiplierBonus.add(s);
                }
            if (itemStack != null && !(itemStack.getType().isAir()) &&
                    this.main.getItemPricesConfig().contains(itemStack.getType().name())) {
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
        }


        for (PermissionAttachmentInfo pai : player.getEffectivePermissions()) {
            if (pai.getPermission().contains("sellgui.bonus.") && pai.getValue()) {
                if (price != 0) {
                    price += Double.parseDouble(pai.getPermission().replaceAll("sellgui.bonus.", ""));
                }
            } else if (pai.getPermission().contains("sellgui.multiplier.") && pai.getValue()) {
                price *= Double.parseDouble(pai.getPermission().replaceAll("sellgui.multiplier.", ""));
            }
        }
        if (main.getConfig().getBoolean("round-places")) {
            return round(price, main.getConfig().getInt("places-to-round"));
        } else {
            return price;
        }
    }

    public double getTotal(Inventory inventory) {
        double total = 0.0D;
        for (ItemStack itemStack : inventory.getContents()) {
            if (!InventoryListeners.sellGUIItem(itemStack, this.player) && itemStack != null)
                total += getPrice(itemStack) * itemStack.getAmount();
        }
        return total;
    }

    public void logSell(ItemStack itemStack) {
        if (itemStack == null) {
            return;
        }
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(getMain().getLog(), true));
            Date now = new Date();
            SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            if (itemStack.hasItemMeta() && itemStack.getItemMeta().hasDisplayName()) {
                writer.append(itemStack.getType() + "|" + itemStack.getItemMeta().getDisplayName() + "|" + itemStack.getAmount() + "|" + getPrice(itemStack) + "|" + getPlayer().getName() + "|" + format.format(now) + "\n");
            } else {
                writer.append(itemStack.getType() + "|" + "N\\A" + "|" + itemStack.getAmount() + "|" + getPrice(itemStack) + "|" + getPlayer().getName() + "|" + format.format(now) + "\n");
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private final DecimalFormat decimalFormat = new DecimalFormat("#,###");

    public void sellItems(Inventory inventory) {
        this.main.getEcon().depositPlayer((OfflinePlayer) this.player, getTotal(inventory));
        this.player.sendMessage(color(this.main.getLangConfig().getString("sold-message").replaceAll("%total%", decimalFormat.format(getTotal(inventory)))));
        for (ItemStack itemStack : inventory.getContents()) {
            if (itemStack != null && !InventoryListeners.sellGUIItem(itemStack, this.player)) {
                if (getPrice(itemStack) != 0D) {
                    if (main.getConfig().getBoolean("log-transactions")) {
                        logSell(itemStack);
                    }
                    inventory.remove(itemStack);
                }
            }
        }
        if (this.main.getConfig().getBoolean("close-after-sell")) {
            InventoryListeners.dropItems(getMenu(), this.player);
            this.player.closeInventory();
            SellCommand.getSellGUIs().remove(this);
        } else {
            addSellItem();
        }
    }

    public ItemStack getConfirmItem() {
        return this.confirmItem;
    }

    public Player getPlayer() {
        return this.player;
    }

    public static ItemStack getSellItem() {
        return sellItem;
    }

    public static ItemStack getFiller() {
        return filler;
    }

    public String getMenuTitle() {
        return this.menuTitle;
    }

    public Inventory getMenu() {
        return this.menu;
    }

    public SellGUIMain getMain() {
        return this.main;
    }

    public static String color(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value + "");
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public static double round(String value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public static String roundString(String value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value + "");
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.toPlainString();
    }
}