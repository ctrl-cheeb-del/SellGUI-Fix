package net.mineshock.sellgui;

import net.mineshock.sellgui.commands.CustomItemsCommand;
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
    private static SellGUIAPI instance;

    public SellGUIAPI(SellGUIMain main) {
        this.main = main;
        instance = this;
    }

    public PriceCalculator getPriceCalculator() { return main.getPriceCalculator(); }

    public static SellGUIAPI getInstance() { return instance; }

}
