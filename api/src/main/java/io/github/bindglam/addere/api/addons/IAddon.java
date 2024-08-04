package io.github.bindglam.addere.api.addons;

import java.lang.reflect.Constructor;

public interface IAddon {
    AddonInfo getInfo();
    Constructor<?> getConstructor();
    Object getInstance();
}
