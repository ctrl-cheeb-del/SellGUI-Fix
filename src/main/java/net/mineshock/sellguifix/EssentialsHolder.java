package net.mineshock.sellguifix;

import com.earth2me.essentials.Essentials;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;

public class EssentialsHolder {
    private Essentials essentials;

    public EssentialsHolder(){
        this.essentials = (Essentials) Bukkit.getPluginManager().getPlugin("Essentials");
        if(essentials == null  ){
            System.out.println("WEEWOOO");
        }
    }

    public Essentials getEssentials() {
        return essentials;
    }

    public void setEssentials(Essentials essentials) {
        this.essentials = essentials;
    }

    public BigDecimal getPrice(ItemStack itemStack){
        if(essentials.getWorth().getPrice(essentials, itemStack) != null){
            return essentials.getWorth().getPrice(essentials, itemStack);
        }
        return new BigDecimal(0);
    }
}
