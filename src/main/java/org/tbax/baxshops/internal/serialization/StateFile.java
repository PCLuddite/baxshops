/*
 * Copyright (C) Timothy Baxendale
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
 * USA
 */
package org.tbax.baxshops.internal.serialization;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.tbax.baxshops.Format;
import org.tbax.baxshops.internal.ShopPlugin;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public final class StateFile
{
    public static final String YAML_FILE_PATH = "shops.yml";

    private JavaPlugin plugin;
    private BaxConfig config;

    public StateFile(JavaPlugin plugin)
    {
        this.plugin = plugin;
    }

    /**
     * Saves all shops
     */
    public void writeToDisk(State savedState)
    {
        if (getConfig().getBackupDays() != 0 && !backup()) {
            ShopPlugin.logWarning("Failed to back up BaxShops before saving!");
        }

        if (getConfig().saveDefaults()) {
            resaveConfig();
        }

        FileConfiguration state = new YamlConfiguration();
        state.set("shops", new ArrayList<>(savedState.getShops()));
        state.set("players", new ArrayList<>(savedState.getPlayers()));

        try {
            File dir = plugin.getDataFolder();
            File stateFile = new File(dir, YAML_FILE_PATH);
            if (!dir.exists() && !dir.mkdirs()) {
                plugin.getLogger().severe("Unable to make data folder!");
            }
            else {
                String yaml = state.saveToString();
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(stateFile))) {
                    writer.write("VERSION " + State.STATE_VERSION);
                    writer.newLine();
                    writer.write(yaml);
                }
            }

            if (getConfig().getBackupDays() > 0) {
                if (hasStateChanged()) {
                    deleteOldestBackup();
                }
                else {
                    deleteLatestBackup();
                }
            }
        }
        catch (IOException e) {
            plugin.getLogger().severe("Save failed");
            e.printStackTrace();
        }
    }

    private void resaveConfig()
    {
        if (!config.backup())
            plugin.getLogger().warning("Could not backup config. Configuration may be lost.");
        if (config.getFileConfig().contains("StateVersion"))
            config.getFileConfig().set("StateVersion", null);
        config.save();
    }


    public boolean backup()
    {
        File stateLocation = getFile();
        if (!stateLocation.exists()) {
            plugin.getLogger().warning("Aborting backup: shops.yml not found");
            return false;
        }

        File backupFolder = getBackupFile();
        if (!backupFolder.exists() && !backupFolder.mkdirs()) {
            plugin.getLogger().severe("Unable to create backups folder!");
            return false;
        }

        String backupName = Format.FILE_DATE_FORMAT.format(new Date()) + ".yml";
        File backup = new File(backupFolder, backupName);
        try (InputStream in = new FileInputStream(stateLocation);
             OutputStream out = new FileOutputStream(backup))
        {
            byte[] buf = new byte[1024];
            int i;
            while ((i = in.read(buf)) > 0) {
                out.write(buf, 0, i);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
            ShopPlugin.logSevere("Backup failed!");
            return false;
        }
        return true;
    }

    public void deleteOldestBackup()
    {
        List<File> backups = getBackupFiles();
        int days = getConfig().getBackupDays();

        if (backups == null || days <= 0) {
            return;
        }

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        Date limit = cal.getTime();

        for(File backup : backups) {
            String name = backup.getName().substring(0, backup.getName().lastIndexOf('.'));
            if (limit.after(Format.parseFileDate(name))) {
                if (!backup.delete()) {
                    ShopPlugin.logWarning(String.format("Unable to delete old backup %s", backup.getName()));
                }
            }
        }
    }

    public List<File> getBackupFiles()
    {
        File backupFolder = getBackupFile();
        return Arrays.stream(backupFolder.listFiles((f, name) -> name.endsWith(".yml")))
                .map(f -> f.getName().substring(0, f.getName().lastIndexOf('.')))
                .filter(n -> Format.parseFileDate(n) != null)
                .map(n -> new File(backupFolder, n + ".yml"))
                .sorted(Comparator.comparing(f -> Format.parseFileDate(f.getName())))
                .sorted(Comparator.reverseOrder())
                .collect(Collectors.toList());
    }

    public boolean deleteLatestBackup()
    {
        List<File> backups = getBackupFiles();
        if (backups.isEmpty())
            return false;
        return backups.get(0).delete();
    }

    public boolean hasStateChanged()
    {
        List<File> backups = getBackupFiles();
        if (backups.isEmpty())
            return true;

        File latest = backups.get(0);
        File state = getFile();

        if (latest.length() != state.length())
            return true;

        try (InputStream stream1 = new FileInputStream(latest);
             InputStream stream2 = new FileInputStream(state))
        {
            int b1;
            while ((b1 = stream1.read()) != -1) {
                if (b1 != stream2.read()) {
                    return true;
                }
            }
            return false;
        }
        catch (IOException e) {
            return true;
        }
    }

    public BaxConfig getConfig()
    {
        if (config == null)
            config = new BaxConfig(plugin);
        return config;
    }

    public File getFile()
    {
        return new File(plugin.getDataFolder(), YAML_FILE_PATH);
    }

    public File getBackupFile()
    {
        return new File(plugin.getDataFolder(), "backups");
    }

    public static double readVersion(File file)
    {
        try (Scanner scanner = new Scanner(file)) {
            if (!scanner.next().equals("VERSION")) return 0d;
            return scanner.nextDouble();
        }
        catch (NoSuchElementException | IOException e) {
            return 0d;
        }
    }
}
