package io.github.bindglam.addere;

import io.github.bindglam.addere.addons.AddereAddon;
import io.github.bindglam.addere.api.Addere;
import io.github.bindglam.addere.api.addons.loader.IAddonLoader;
import io.github.bindglam.addere.api.items.CustomItem;
import io.github.bindglam.addere.api.utils.IEnrollmentManager;
import io.github.bindglam.addere.items.CustomItemManager;
import io.github.bindglam.addere.loader.AddereAddonLoader;
import io.github.bindglam.addere.pack.PackGenerator;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.TimeUnit;

public class AdderePlugin extends JavaPlugin implements Addere {
    public static Addere INSTANCE;

    private final AddereAddonLoader loader = new AddereAddonLoader();
    private final CustomItemManager itemManager = new CustomItemManager();

    @Override
    public void onEnable() {
        INSTANCE = this;

        if(!isFolia()){
            getLogger().warning("---------------------");
            getLogger().warning("You are not in Folia!");
            getLogger().warning("We can't support you!");
            getLogger().warning("---------------------");
            getLogger().info("Server Info : NOT Folia( " + getServerVersion() + " )");
        } else {
            getLogger().info("Server Info : Folia( " + getServerVersion() + " )");
        }
        getLogger().info("Addere initializing...");

        loader.loadAll();

        saveDefaultConfig();

        Bukkit.getAsyncScheduler().runDelayed(this, (task) -> {
            PackGenerator.generate();
        }, 2L, TimeUnit.SECONDS);
    }

    @Override
    public void onDisable() {
        saveConfig();
    }

    @Override
    public JavaPlugin getPlugin() {
        return this;
    }

    @Override
    public String getServerVersion() {
        return getServer().getMinecraftVersion();
    }

    @Override
    public boolean isFolia() {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    @Override
    public AddereAddonLoader getLoader() {
        return loader;
    }

    @Override
    public CustomItemManager getCustomItemManager() {
        return itemManager;
    }
}
