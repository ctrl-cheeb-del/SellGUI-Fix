package net.mineshock.sellguifix;

import net.mineshock.sellguifix.commands.CustomItemsCommand;
import net.mineshock.sellguifix.commands.SellAllCommand;
import net.mineshock.sellguifix.commands.SellCommand;
import net.mineshock.sellguifix.listeners.InventoryListeners;
import net.mineshock.sellguifix.listeners.SignListener;
import net.mineshock.sellguifix.listeners.UpdateWarning;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Material;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class SellGUIMain extends JavaPlugin {
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

    private FileConfiguration logConfiguration;

    public void onEnable() {
        registerConfig();
        createConfigs();
        createPrices();
        getServer().getPluginManager().registerEvents((Listener) new InventoryListeners(this), (Plugin) this);
        getServer().getPluginManager().registerEvents((Listener) new SignListener(this), (Plugin) this);
        this.sellCommand = new SellCommand(this);
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
        //customMenuItemsConfig;
    }

    public void onDisable() {
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

    public SellGUIMain getMain() {
        return this;
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
