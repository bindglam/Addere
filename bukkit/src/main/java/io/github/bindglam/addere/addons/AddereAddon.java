package io.github.bindglam.addere.addons;

import io.github.bindglam.addere.api.addons.AddonInfo;
import io.github.bindglam.addere.api.addons.IAddon;

import java.lang.reflect.Constructor;

public class AddereAddon implements IAddon {
    private final AddonInfo info;
    private final Constructor<?> constructor;
    private final Object instance;

    public AddereAddon(AddonInfo info, Constructor<?> constructor, Object instance) {
        this.info = info;
        this.constructor = constructor;
        this.instance = instance;
    }

    @Override
    public AddonInfo getInfo() {
        return null;
    }

    @Override
    public Constructor<?> getConstructor() {
        return null;
    }

    @Override
    public Object getInstance() {
        return instance;
    }
}
