package io.github.bindglam.addere;

import org.apache.commons.io.FileUtils;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.Objects;

public class LangManager {
    private static final File langFolder = new File("plugins/Addere/lang");

    public static YamlConfiguration langConfig;

    public static void init(){
        if(!langFolder.exists())
            langFolder.mkdirs();

        copyLangFiles(langFolder.exists());

        AdderePlugin.INSTANCE.getPlugin().getLogger().info("Language : " + Locale.getDefault(Locale.Category.FORMAT).getLanguage());
        switch (Locale.getDefault(Locale.Category.FORMAT).getLanguage()){
            case "en" -> langConfig = YamlConfiguration.loadConfiguration(new File("plugins/Addere/lang/en_us.yml"));

            default -> {
                AdderePlugin.INSTANCE.getPlugin().getLogger().warning("We couldn't find " + Locale.getDefault().getLanguage() + " lang file!");
                AdderePlugin.INSTANCE.getPlugin().getLogger().warning("We will use en_us.yml!");

                langConfig = YamlConfiguration.loadConfiguration(new File("plugins/Addere/lang/en_us.yml"));
            }
        }
    }

    private static void copyLangFiles(boolean existFileDeleted){
        if(existFileDeleted) {
            File langFile = new File("plugins/Addere/lang/en_us.yml");
            if (langFile.exists())
                langFile.delete();
        }

        try {
            FileUtils.copyInputStreamToFile(Objects.requireNonNull(LangManager.class.getClassLoader().getResourceAsStream("lang/en_us.yml")), new File("plugins/Addere/lang/en_us.yml"));
        } catch (IOException e) {
            throw new RuntimeException("Failed to copy lang file!", e);
        }
    }

    public static @NotNull String get(String key){
        if(langConfig.getString(key) == null)
            init();
        return Objects.requireNonNull(langConfig.getString(key));
    }
}
