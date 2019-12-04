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
package org.tbax.baxshops.serialization.internal;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.tbax.baxshops.internal.versioning.LegacyConfigUtil;

import java.io.File;
import java.util.*;

@SuppressWarnings({"FieldCanBeLocal", "unused"})
public final class BaxConfig
{
    private final int DEFAULT_BACKUPS = 15;
    private final boolean DEFAULT_LOG_NOTES = false;

    private final JavaPlugin plugin;

    public BaxConfig(JavaPlugin plugin)
    {
        this.plugin = plugin;
    }

    public FileConfiguration getFileConfig()
    {
        return plugin.getConfig();
    }

    public int getBackups()
    {
        return getFileConfig().getInt("Backups", DEFAULT_BACKUPS);
    }

    public void setBackups(int backups)
    {
        getFileConfig().set("Backups", backups);
    }

    public boolean isLogNotes()
    {
        return getFileConfig().getBoolean("LogNotes", DEFAULT_LOG_NOTES);
    }

    public void setLogNotes(boolean logNotes)
    {
        getFileConfig().set("LogNotes", logNotes);
    }

    public boolean backup()
    {
        File configFile = new File(plugin.getDataFolder(), "config.yml");
        File backupFile = new File(plugin.getDataFolder(), "CONFIG.BAK");

        if (backupFile.exists()) {
            int i = 0;
            do {
                backupFile = new File(plugin.getDataFolder(), "CONFIG.BAK" + i++);
            }
            while(backupFile.exists());
        }

        return configFile.renameTo(backupFile);
    }

    public boolean saveDefaults()
    {
        boolean changed = false;
        Set<String> keys = getFileConfig().getDefaults().getKeys(true);
        for (String key : keys) {
            if (!LegacyConfigUtil.configContains(getFileConfig(), key, true)) {
                getFileConfig().set(key, getFileConfig().getDefaults().get(key));
                changed = true;
            }
        }
        return changed;
    }

    public void save()
    {
        plugin.saveConfig();
    }
}
