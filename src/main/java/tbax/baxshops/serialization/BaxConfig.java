/*
 * Copyright (C) 2013-2019 Timothy Baxendale
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
package tbax.baxshops.serialization;

import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import tbax.baxshops.ShopPlugin;

import java.util.UUID;

@SuppressWarnings("FieldCanBeLocal")
public final class BaxConfig
{
    private final int DEFAULT_BACKUPS = 15;
    private final boolean DEFAULT_LOG_NOTES = false;
    private final double DEFAULT_XP_CONVERT = 4.00;
    private final boolean DEFAULT_DEATH_TAX_ENABLED = false;
    private final String DEFAULT_DEATH_TAX_GOES_TO = StoredPlayer.DUMMY_UUID.toString();
    private final double DEFAULT_DEATH_TAX_PERCENT = 0.04;
    private final double DEFAULT_DEATH_TAX_MINIMUM = 100.00;
    private final double DEFAULT_DEATH_TAX_MAXIMUM = -1;

    private final ShopPlugin plugin;

    public BaxConfig(ShopPlugin plugin)
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

    public double getXpConvert()
    {
        return getFileConfig().getDouble("XPConvert", DEFAULT_XP_CONVERT);
    }

    public void setXpConvert(double xpConvert)
    {
        getFileConfig().set("XPConvert", xpConvert);
    }

    public boolean isDeathTaxEnabled()
    {
        return getFileConfig().getBoolean("DeathTax.Enabled", DEFAULT_DEATH_TAX_ENABLED);
    }

    public void setDeathTaxEnabled(boolean deathTaxEnabled)
    {
        getFileConfig().set("DeathTax.Enabled", deathTaxEnabled);
    }

    public UUID getDeathTaxGoesTo()
    {
        try {
            return UUID.fromString(getFileConfig().getString("DeathTax.GoesTo", DEFAULT_DEATH_TAX_GOES_TO));
        }
        catch (IllegalArgumentException e) {
            return UUID.fromString(DEFAULT_DEATH_TAX_GOES_TO);
        }
    }

    public void setDeathTaxGoesTo(@NotNull UUID deathTaxGoesTo)
    {
        getFileConfig().set("DeathTax.GoesTo", deathTaxGoesTo.toString());
    }

    public double getDeathTaxPercentage()
    {
        return getFileConfig().getDouble("DeathTax.Percentage", DEFAULT_DEATH_TAX_PERCENT);
    }

    public void setDeathTaxPercentage(double deathTaxPercentage)
    {
        getFileConfig().set("DeathTax.Percentage", deathTaxPercentage);
    }

    public double getDeathTaxMinimum()
    {
        return getFileConfig().getDouble("DeathTax.Minimum", DEFAULT_DEATH_TAX_MINIMUM);
    }

    public void setDeathTaxMinimum(double deathTaxMinimum)
    {
        getFileConfig().set("DeathTax.Minimum", deathTaxMinimum);
    }

    public double getDeathTaxMaximum()
    {
        return getFileConfig().getDouble("DeathTax.Maximum", DEFAULT_DEATH_TAX_MAXIMUM);
    }

    public void setDeathTaxMaximum(double deathTaxMaximum)
    {
        getFileConfig().set("DeathTax.Maximum", deathTaxMaximum);
    }

    public void save()
    {
        plugin.saveConfig();
    }
}
