package io.github.bindglam.test;

import io.github.bindglam.addere.api.Addere;
import io.github.bindglam.addere.api.addons.AddonInfo;

public class TestAddon {
    @AddonInfo(name = "test-addon")
    public TestAddon(Addere addere){
        addere.getPlugin().getLogger().info("Test Addon is initialized!");
    }
}
