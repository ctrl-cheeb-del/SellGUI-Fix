package net.mineshock.sellgui;

import net.mineshock.sellgui.commands.CustomItemsCommand;
import net.mineshock.sellgui.commands.SellAllCommand;
import net.mineshock.sellgui.commands.SellCommand;
import net.mineshock.sellgui.listeners.InventoryListeners;
import net.mineshock.sellgui.listeners.SignListener;
import net.mineshock.sellgui.listeners.UpdateWarning;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.sql.Connection;

public class SellGUIMain extends JavaPlugin {

    private static SellGUIMain main;

    private static Economy econ;

    private ConsoleCommandSender console = getServer().getConsoleSender();

    private File itemPrices;
    private FileConfiguration itemPricesConfig;

    private File lang;
    private FileConfiguration langConfig;

    private File customItems;
    private FileConfiguration customItemsConfig;

    private File customMenuItems;
    private FileConfiguration customMenuItemsConfig;

    private boolean useEssentials;

    private EssentialsHolder essentialsHolder;

    private File log;

    private SellCommand sellCommand;
    private SellWand sellWand;
    private PriceCalculator priceCalculator;

    private FileConfiguration logConfiguration;
    public NamespacedKey key = new NamespacedKey(this, getDescription().getName());

    public static Connection dbConnection;

    public void onEnable() {
        main = this;

        registerGlow();
        registerConfig();
        createConfigs();
        createPrices();
        PluginManager manager = getServer().getPluginManager();

        manager.registerEvents(new InventoryListeners(this), this);
        manager.registerEvents(new SignListener(this), this);
        manager.registerEvents(new SellWand(this), this);

        this.sellCommand = new SellCommand(this);
        this.sellWand = new SellWand(this);
        this.priceCalculator = new PriceCalculator(this);
        getCommand("sellgui").setExecutor(sellCommand);
        getCommand("customitems").setExecutor(new CustomItemsCommand(this));
        getCommand("sellall").setExecutor(new SellAllCommand(this));
        setupEconomy();
        this.useEssentials = essentials();
        (new UpdateChecker(this, 55201)).getVersion(version -> {
            if (getDescription().getVersion().equalsIgnoreCase(version)) {
                getLogger().info("Plugin is up to date");
            } else {
                getLogger().info("There is a new update available.");
                getServer().getPluginManager().registerEvents((Listener) new UpdateWarning(this), (Plugin) this);
            }
        });
    }

    public void onDisable() {
    }

    public void registerGlow() {
        try {
            Field f = Enchantment.class.getDeclaredField("acceptingNew");
            f.setAccessible(true);
            f.set(null, true);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        try {
            key = new NamespacedKey(this, getDescription().getName());

            Glow glow = new Glow(key);
            Enchantment.registerEnchantment(glow);
        }
        catch (IllegalArgumentException e){
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    public void saveCustom() {
        try {
            this.customItemsConfig.save(this.customItems);
            //TODO Remove this
            this.customMenuItemsConfig.save(this.customMenuItems);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public File getLog() {
        return log;
    }

    public void saveLog() {
        try {
            this.logConfiguration.save(this.log);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void registerConfig() {
        getConfig().options().copyDefaults(true);
        saveDefaultConfig();
    }

    public void createPrices() {
        for (Material m : Material.values()) {
            if (!this.itemPricesConfig.contains(m.name())) {
                this.itemPricesConfig.set(m.name(), Double.valueOf(0.0D));
            }
        }
        try {
            this.itemPricesConfig.save(this.itemPrices);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Economy getEcon() {
        return econ;
    }

    public void reload() {
        reloadConfig();
        saveDefaultConfig();
        createConfigs();
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null)
            return false;
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null)
            return false;
        econ = (Economy) rsp.getProvider();
        return (econ != null);
    }

    private boolean essentials() {
        if (getServer().getPluginManager().getPlugin("Essentials") == null) {
            getServer().getLogger().warning("Essentials not found, disabling essentials support");
            return false;
        } else {
            essentialsHolder = new EssentialsHolder();
            return true;
        }
    }

    public SellCommand getSellCommand() {
        return sellCommand;
    }

    public SellAllCommand getSellALlCommand() { return new SellAllCommand(this); }

    public SellWand getSellWand() { return sellWand; }

    public PriceCalculator getPriceCalculator() { return priceCalculator; }

    public static SellGUIMain getMain() {
        return main;
    }

    public ConsoleCommandSender getConsole() {
        return this.console;
    }

    public FileConfiguration getItemPricesConfig() {
        return this.itemPricesConfig;
    }

    public FileConfiguration getLangConfig() {
        return this.langConfig;
    }

    public FileConfiguration getCustomItemsConfig() {
        return this.customItemsConfig;
    }

    public void createConfigs() {
        this.itemPrices = new File(getDataFolder(), "item prices.yml");
        if (!this.itemPrices.exists()) {
            this.itemPrices.getParentFile().mkdirs();
            saveResource("item prices.yml", false);
        }
        this.itemPricesConfig = (FileConfiguration) new YamlConfiguration();
        try {
            this.itemPricesConfig.load(this.itemPrices);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
        this.customMenuItems = new File(getDataFolder(), "custom menu items.yml");
        if (!this.customMenuItems.exists()) {
            this.customMenuItems.getParentFile().mkdirs();
            saveResource("custom menu items.yml", false);
        }
        this.customMenuItemsConfig = (FileConfiguration) new YamlConfiguration();
        try {
            this.customMenuItemsConfig.load(this.itemPrices);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
        this.lang = new File(getDataFolder(), "lang.yml");
        if (!this.lang.exists()) {
            this.lang.getParentFile().mkdirs();
            saveResource("lang.yml", false);
        }
        this.langConfig = (FileConfiguration) new YamlConfiguration();
        try {
            this.langConfig.load(this.lang);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
        this.customItems = new File(getDataFolder(), "custom items.yml");
        if (!this.customItems.exists()) {
            this.customItems.getParentFile().mkdirs();
            saveResource("custom items.yml", false);
        }
        this.customItemsConfig = (FileConfiguration) new YamlConfiguration();
        try {
            this.customItemsConfig.load(this.customItems);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
        this.log = new File(getDataFolder(), "log.txt");
        if (!this.log.exists()) {
            this.log.getParentFile().mkdirs();
            saveResource("log.txt", false);
            BufferedWriter writer = null;
            try {
                writer = new BufferedWriter(new FileWriter(getMain().getLog(), true));
                writer.append("Type|Display Name|Amount|Price|Player|Time\n");
                writer.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean hasEssentials() {
        return this.useEssentials;
    }

    public EssentialsHolder getEssentialsHolder() {
        return essentialsHolder;
    }
}
