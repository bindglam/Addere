package io.github.bindglam.addere.loader;

import io.github.bindglam.addere.AdderePlugin;
import io.github.bindglam.addere.addons.AddereAddon;
import io.github.bindglam.addere.api.Addere;
import io.github.bindglam.addere.api.addons.AddonInfo;
import io.github.bindglam.addere.api.addons.loader.IAddonLoader;
import io.github.bindglam.addere.api.utils.JarUtil;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

public class AddereAddonLoader implements IAddonLoader<AddereAddon> {
    private static final File addonsFolder = new File("plugins/Addere/addons");

    private final CopyOnWriteArrayList<AddereAddon> addons = new CopyOnWriteArrayList<>();

    public void loadAll(){
        if(!addonsFolder.exists())
            addonsFolder.mkdirs();

        for(File addonFile : Objects.requireNonNull(addonsFolder.listFiles())){
            Bukkit.getAsyncScheduler().runNow(AdderePlugin.INSTANCE.getPlugin(), (task) -> load(addonFile));
        }
    }

    @Override
    public AddereAddon load(File addonFile) {
        List<String> classNames;
        URLClassLoader child;
        try {
            classNames = JarUtil.getClassNamesInJar(addonFile);
            child = new URLClassLoader(
                    new URL[] { addonFile.toURI().toURL() },
                    this.getClass().getClassLoader()
            );
        } catch (IOException e) {
            throw new RuntimeException("Failed to load " + addonFile.getName() + "!", e);
        }

        boolean didCopyResources = false;
        for(String className : classNames){
            Class<?> clazz;
            try {
                clazz = Class.forName(className, true, child);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("Failed to load addon's class!", e);
            }

            if(!didCopyResources) {
                didCopyResources = true;
                try {
                    JarUtil.copyFolderFromJar(clazz, "assets", new File("plugins/Addere/resourcepack"), JarUtil.CopyOption.REPLACE_IF_EXIST);
                } catch (IOException e) {
                    throw new RuntimeException("Failed to copy addon's resources!", e);
                }
            }

            for(Constructor<?> constructor : clazz.getDeclaredConstructors()){
                if(constructor.isAnnotationPresent(AddonInfo.class)){
                    AddonInfo addonInfo = constructor.getDeclaredAnnotation(AddonInfo.class);

                    AdderePlugin.INSTANCE.getPlugin().getLogger().info(addonInfo.name() + " loading...");

                    Object instance;
                    try {
                        if(Arrays.stream(constructor.getParameterTypes()).toList().contains(Addere.class)) {
                            instance = constructor.newInstance(AdderePlugin.INSTANCE);
                        } else {
                            instance = constructor.newInstance();
                        }
                    } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                        throw new RuntimeException("Failed to create addon's instance!", e);
                    }
                    AddereAddon addon = new AddereAddon(addonInfo, constructor, instance);
                    addons.add(addon);
                    return addon;
                }
            }
        }
        return null;
    }

    @Override
    public void unload(AddereAddon addon) {
        addons.remove(addon);
    }

    public List<AddereAddon> getAddons(){
        return addons;
    }
}
