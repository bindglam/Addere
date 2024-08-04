package io.github.bindglam.addere;

import io.github.bindglam.addere.api.Addere;
import io.github.bindglam.addere.api.addons.loader.IAddonLoader;
import io.github.bindglam.addere.loader.AddereAddonLoader;
import org.bukkit.plugin.java.JavaPlugin;

public class AdderePlugin extends JavaPlugin implements Addere {
    public static Addere INSTANCE;

    private final AddereAddonLoader loader = new AddereAddonLoader();

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
    public IAddonLoader getLoader() {
        return loader;
    }
}
