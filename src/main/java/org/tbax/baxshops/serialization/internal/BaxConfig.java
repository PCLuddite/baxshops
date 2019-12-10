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
import org.tbax.baxshops.internal.Resources;
import org.tbax.baxshops.internal.versioning.LegacyConfigUtil;

import java.io.File;
import java.util.*;

@SuppressWarnings({"FieldCanBeLocal", "unused"})
public final class BaxConfig
{
    public static final boolean DEFAULT_LOG_NOTES = false;
    public static final int DEFAULT_BACKUP_DAYS = 1;
    public static final int DEFAULT_BACKUP_INTERVAL = 60; // in minutes
    public static final UUID DEFAULT_DUMMY_ID = UUID.fromString("326a36ea-b465-3192-a4f7-c313f347edc9");
    public static final String DEFAULT_DUMMY_NAME = "world";
    public static final UUID DEFAULT_ERROR_ID = UUID.fromString("3d748006-ddc3-4f1b-a7c9-01fab68d0797");
    public static final String DEFAULT_ERROR_NAME = Resources.ERROR_INLINE;
    public static final boolean DEFAULT_DEPOSIT_DUMMY = false;

    private final JavaPlugin plugin;

    public BaxConfig(JavaPlugin plugin)
    {
        this.plugin = plugin;
    }

    public FileConfiguration getFileConfig()
    {
        return plugin.getConfig();
    }

    public int getBackupDays()
    {
        return getFileConfig().getInt("Backup.Days", DEFAULT_BACKUP_DAYS);
    }

    public void setBackupDays(int days)
    {
        getFileConfig().set("Backup.Days", days);
    }

    public int getBackupInterval()
    {
        return getFileConfig().getInt("Backup.Interval", DEFAULT_BACKUP_INTERVAL);
    }

    public void setBackupInterval(int minutes)
    {
        getFileConfig().set("Backup.Interval", minutes);
    }

    public boolean isLogNotes()
    {
        return getFileConfig().getBoolean("LogNotes", DEFAULT_LOG_NOTES);
    }

    public void setLogNotes(boolean logNotes)
    {
        getFileConfig().set("LogNotes", logNotes);
    }

    public UUID getErrorId()
    {
        try {
            return UUID.fromString(getFileConfig().getString("SpecialPlayers.Error.uuid", DEFAULT_ERROR_ID.toString()));
        }
        catch (IllegalArgumentException e) {
            return DEFAULT_ERROR_ID;
        }
    }

    public String getErrorName()
    {
        return getFileConfig().getString("SpecialPlayers.Error.name", DEFAULT_ERROR_NAME);
    }

    public void setErrorName(String name)
    {
        getFileConfig().set("SpecialPlayers.Error.name", name);
    }


    public void setErrorId(UUID uuid)
    {
        getFileConfig().set("SpecialPlayers.Error.uuid", uuid.toString());
    }

    public UUID getDummyId()
    {
        try {
            return UUID.fromString(getFileConfig().getString("SpecialPlayers.Dummy.uuid", DEFAULT_DUMMY_ID.toString()));
        }
        catch (IllegalArgumentException e) {
            return DEFAULT_DUMMY_ID;
        }
    }

    public void setDummyName(String name)
    {
        getFileConfig().set("SpecialPlayers.Dummy.name", name);
    }


    public void setDummyId(UUID uuid)
    {
        getFileConfig().set("SpecialPlayers.Dummy.uuid", uuid.toString());
    }

    public String getDummyName()
    {
        return getFileConfig().getString("SpecialPlayers.Dummy.name", DEFAULT_DUMMY_NAME);
    }

    public boolean hasDepositDummy()
    {
        return getFileConfig().getBoolean("SpecialPlayers.Dummy.RealDeposit", DEFAULT_DEPOSIT_DUMMY);
    }

    public void setHasDepositDummy(boolean depositDummy)
    {
        getFileConfig().set("SpecialPlayers.Dummy.RealDeposit", depositDummy);
    }

    public boolean backup()
    {
        File configFile = new File(plugin.getDataFolder(), "config.yml");
        File backupFile = new File(plugin.getDataFolder(), "config.bak");

        if (backupFile.exists()) {
            int i = 0;
            do {
                backupFile = new File(plugin.getDataFolder(), "config.bak" + i++);
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
