package io.github.bindglam.addere.api;

import io.github.bindglam.addere.api.addons.loader.IAddonLoader;
import org.bukkit.plugin.java.JavaPlugin;

public interface Addere {
    JavaPlugin getPlugin();

    String getServerVersion();

    boolean isFolia();

    IAddonLoader<?> getLoader();
}
