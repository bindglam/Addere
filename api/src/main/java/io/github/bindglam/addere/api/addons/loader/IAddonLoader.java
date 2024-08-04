package io.github.bindglam.addere.api.addons.loader;

import io.github.bindglam.addere.api.addons.AddonInfo;
import io.github.bindglam.addere.api.addons.IAddon;

import java.io.File;

public interface IAddonLoader<T extends IAddon> {
    T load(File addonFile);
    void unload(T addon);
}
