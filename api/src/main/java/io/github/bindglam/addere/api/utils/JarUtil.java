package io.github.bindglam.addere.api.utils;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class JarUtil {
    public static final char JAR_SEPARATOR = '/';

    public static List<String> getClassNamesInJar(File jarFile) throws IOException {
        List<String> classNames = new ArrayList<>();
        ZipInputStream zip = new ZipInputStream(new FileInputStream(jarFile));
        for (ZipEntry entry = zip.getNextEntry(); entry != null; entry = zip.getNextEntry()) {
            if (!entry.isDirectory() && entry.getName().endsWith(".class")) {
                String className = entry.getName().replace('/', '.'); // including ".class"
                classNames.add(className.substring(0, className.length() - ".class".length()));
            }
        }
        return classNames;
    }

    public static void copyFolderFromJar(Class<?> clazz, String folderName, File destFolder, CopyOption option) throws IOException {
        copyFolderFromJar(clazz, folderName, destFolder, option, null);
    }

    public static void copyFolderFromJar(Class<?> clazz, String folderName, File destFolder, CopyOption option, PathTrimmer trimmer) throws IOException {
        if (!destFolder.exists())
            destFolder.mkdirs();

        byte[] buffer = new byte[1024];

        File fullPath = null;
        String path = clazz.getProtectionDomain().getCodeSource().getLocation().getPath();
        if (trimmer != null)
            path = trimmer.trim(path);
        try {
            if (!path.startsWith("file"))
                path = "file://" + path;

            fullPath = new File(new URI(path));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        ZipInputStream zis = new ZipInputStream(new FileInputStream(Objects.requireNonNull(fullPath)));

        ZipEntry entry;
        while ((entry = zis.getNextEntry()) != null) {
            if (!entry.getName().startsWith(folderName + JAR_SEPARATOR))
                continue;

            String fileName = entry.getName();

            if (fileName.charAt(fileName.length() - 1) == JAR_SEPARATOR) {
                File file = new File(destFolder + File.separator + fileName);
                if (file.isFile()) {
                    file.delete();
                }
                file.mkdirs();
                continue;
            }

            File file = new File(destFolder + File.separator + fileName);
            if (option == CopyOption.COPY_IF_NOT_EXIST && file.exists())
                continue;

            if (!file.getParentFile().exists())
                file.getParentFile().mkdirs();

            if (!file.exists())
                file.createNewFile();
            FileOutputStream fos = new FileOutputStream(file);

            int len;
            while ((len = zis.read(buffer)) > 0) {
                fos.write(buffer, 0, len);
            }
            fos.close();
        }

        zis.closeEntry();
        zis.close();
    }

    public enum CopyOption {
        COPY_IF_NOT_EXIST, REPLACE_IF_EXIST;
    }

    @FunctionalInterface
    public interface PathTrimmer {
        String trim(String original);
    }
}
