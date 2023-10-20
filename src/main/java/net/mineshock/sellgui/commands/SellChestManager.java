package net.mineshock.sellgui.commands;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import net.mineshock.sellgui.Glow;
import net.mineshock.sellgui.SQLManager;
import net.mineshock.sellgui.SellChest;
import net.mineshock.sellgui.SellGUIMain;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class SellChestManager implements Listener {

    private final SellGUIMain main;
    private final NamespacedKey sellChestKey;
    private final int interval = 5;
    public final HashMap<Location, SellChest> sellChests = new HashMap<>();

    public SellChestManager(SellGUIMain main) {
        this.main = main;
        this.sellChestKey = new NamespacedKey(main, "sellChest");
    }

    public boolean isSellChest(Block block) { return sellChests.containsKey(block.getLocation()); }

    public SellChest getSellChest(@NotNull Block block) { return sellChests.get(block.getLocation()); }

    public ItemStack create(double multiplier) {
        System.out.println(sellChests);

        ItemStack stack = new ItemStack(Material.CHEST);
        ItemMeta meta = stack.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + "§lSell Chest");
        meta.setLore(List.of("",ChatColor.GRAY + "| Multiplier: x" + multiplier, ChatColor.GRAY + "| Auto-sells its contents every " + interval + " seconds"));
        meta.addEnchant(new Glow(main.key), 1, true);

        meta.getPersistentDataContainer().set(this.sellChestKey, PersistentDataType.DOUBLE, multiplier);

        stack.setItemMeta(meta);
        return stack;
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        ItemStack stack = event.getItemInHand();

        if (stack.getType() != Material.CHEST) { return; }
        if (stack.getItemMeta() == null) { return; }
        if (!stack.getItemMeta().getPersistentDataContainer().has(this.sellChestKey, PersistentDataType.DOUBLE)) { return; }
        double multiplier = stack.getItemMeta().getPersistentDataContainer().get(this.sellChestKey, PersistentDataType.DOUBLE);

        if (!SuperiorSkyblockAPI.getPlayer(event.getPlayer()).isInsideIsland()) {
            if (!SuperiorSkyblockAPI.getPlayer(event.getPlayer()).hasBypassModeEnabled()) {
                if (event.getPlayer().getWorld() == Bukkit.getWorld("SuperiorWorld")) {
                    event.getPlayer().sendMessage(ChatColor.RED + "You can only place sell chests on your own island");
                }
                event.setCancelled(true);
                return;
            }
        }

        Location location = event.getBlock().getLocation();
        SellChest sellChest = new SellChest(event.getBlock(), multiplier, event.getPlayer(), main);

        sellChests.put(location, sellChest);
        SQLManager.SQLEdit("INSERT INTO sell_chests (player_id, x, y, z, world, multiplier) VALUES (?, ?, ?, ?, ?, ?)", event.getPlayer().getUniqueId().toString(), location.getBlockX(), location.getBlockY(), location.getBlockZ(), location.getWorld().getName(), multiplier);
    }

    @EventHandler
    public void onBlockBreak(@NotNull BlockBreakEvent event) {

        Block block = event.getBlock();

        if (!isSellChest(block)) {
            return;
        }

        if (!SuperiorSkyblockAPI.getPlayer(event.getPlayer()).isInsideIsland()) {
            if (!SuperiorSkyblockAPI.getPlayer(event.getPlayer()).hasBypassModeEnabled()) {
                if (event.getPlayer().getWorld() == Bukkit.getWorld("SuperiorWorld")) {
                    event.getPlayer().sendMessage("You may only break generators on your own island!");
                }
                event.setCancelled(true);
                return;
            }
        }

        SellChest sellChest = getSellChest(block);

        if (sellChest == null) {
            // wtf?
            return;
        }

        ItemStack stack = this.create(sellChest.getMultiplier());

        World world = block.getWorld();

        event.setDropItems(false);
        world.dropItemNaturally(block.getLocation(), stack);



        this.sellChests.remove(block.getLocation());

        int x = block.getX();
        int y = block.getY();
        int z = block.getZ();

        SQLManager.SQLEdit("DELETE FROM sell_chests WHERE x = ? AND y = ? AND z = ?", x, y, z);
    }

    public void loadSellChests() {
        try (PreparedStatement preparedStatement = SellGUIMain.dbConnection.prepareStatement("SELECT * FROM sell_chests")) {
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                int x = resultSet.getInt("x");
                int y = resultSet.getInt("y");
                int z = resultSet.getInt("z");
                World world = Bukkit.getWorld(resultSet.getString("world"));
                Location location = new Location(world, x, y, z);

                Block block = location.getBlock();
                double multiplier = resultSet.getDouble("multiplier");
                Player player = Bukkit.getPlayer(UUID.fromString(resultSet.getString("player_id")));

                sellChests.put(location, new SellChest(block, multiplier, player, main));
                System.out.println(sellChests);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


}
