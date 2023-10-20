package net.mineshock.sellgui;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class SellChest {

    private final Block block;
    private final double multiplier;
    private final Player player;
    private final SellGUIMain main;

    public SellChest(Block block, double multiplier, Player player, SellGUIMain main) {
        this.block = block;
        this.multiplier = multiplier;
        this.player = player;
        this.main = main;

    }

    public Block getBlock() { return block; }
    public double getMultiplier() { return multiplier; }
    public Player getPlayer() { return player; }

}
