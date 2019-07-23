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
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.jetbrains.annotations.NotNull;
import tbax.baxshops.ShopPlugin;
import tbax.baxshops.serialization.states.State_00300;

import java.io.File;
import java.util.*;

@SuppressWarnings({"FieldCanBeLocal", "unused"})
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
    private final String[] DEFAULT_DEATH_TAX_DEATHS = new String[] { "FALL", "DROWNING", "LAVA", "CONTACT", "FIRE", "FIRE_TICK", "SUFFOCATION" };
    private final int DEFAULT_DEATH_TAX_FOOD_LEVEL = 8;

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

    public List<EntityDamageEvent.DamageCause> getStupidDeaths()
    {
        List<?> deaths = getFileConfig().getList("DeathTax.Deaths", Arrays.asList(DEFAULT_DEATH_TAX_DEATHS));
        List<EntityDamageEvent.DamageCause> causes = new ArrayList<>(deaths.size());
        for (Object death : deaths) {
            if (!(death instanceof String)) {
                plugin.getLogger().warning(death + " cannot be converted to a damage cause");
            }
            else {
                try {
                    causes.add(EntityDamageEvent.DamageCause.valueOf((String) death));
                }
                catch (IllegalArgumentException e) {
                    plugin.getLogger().warning(death + " is not a damage cause");
                }
            }
        }
        return causes;
    }

    public int getStupidMinimumFoodLevel()
    {
        return getFileConfig().getInt("DeathTax.FoodLevel", DEFAULT_DEATH_TAX_FOOD_LEVEL);
    }

    public void setStupidMinimumFoodLevel(int foodLevel)
    {
        getFileConfig().set("DeathTax.FoodLevel", foodLevel);
    }

    public boolean isStupidDeath(PlayerDeathEvent e)
    {
        if (getStupidMinimumFoodLevel() > -1 && e.getEntity().getFoodLevel() <= getStupidMinimumFoodLevel())
            return true;
        return getStupidDeaths().contains(e.getEntity().getLastDamageCause().getCause());
    }

    public double getStateVersion()
    {
        return getFileConfig().getDouble("StateVersion", State_00300.VERSION); // 3.0 was the last version not to have version in config
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
            if (!getFileConfig().contains(key, true)) {
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
