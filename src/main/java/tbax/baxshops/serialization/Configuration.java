/** +++====+++
 *
 *  Copyright (c) Timothy Baxendale
 *
 *  +++====+++
**/
package tbax.baxshops.serialization;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class Configuration
{
    private int backups = 15;
    private boolean logNotes = false;
    private double xpConvert = 4.00;
    private boolean deathTaxEnabled = false;
    private String deathTaxGoesTo = StoredPlayer.DUMMY_UUID.toString();
    private double deathTaxPercentage = 0.04;
    private double deathTaxMinimum = 100.00;
    private double deathTaxMaximum = -1;


    public int getBackups()
    {
        return backups;
    }

    public void setBackups(int backups)
    {
        this.backups = backups;
    }

    public boolean isLogNotes()
    {
        return logNotes;
    }

    public void setLogNotes(boolean logNotes)
    {
        this.logNotes = logNotes;
    }

    public double getXpConvert()
    {
        return xpConvert;
    }

    public void setXpConvert(double xpConvert)
    {
        this.xpConvert = xpConvert;
    }

    public boolean isDeathTaxEnabled()
    {
        return deathTaxEnabled;
    }

    public void setDeathTaxEnabled(boolean deathTaxEnabled)
    {
        this.deathTaxEnabled = deathTaxEnabled;
    }

    public String getDeathTaxGoesTo()
    {
        return deathTaxGoesTo;
    }

    public void setDeathTaxGoesTo(String deathTaxGoesTo)
    {
        this.deathTaxGoesTo = deathTaxGoesTo;
    }

    public double getDeathTaxPercentage()
    {
        return deathTaxPercentage;
    }

    public @NotNull UUID getDeathTaxGoesToId()
    {
        try {
            return UUID.fromString(deathTaxGoesTo);
        }
        catch (IllegalArgumentException e) {
            return StoredPlayer.DUMMY_UUID;
        }
    }

    public void setDeathTaxPercentage(double deathTaxPercentage)
    {
        this.deathTaxPercentage = deathTaxPercentage;
    }

    public double getDeathTaxMinimum()
    {
        return deathTaxMinimum;
    }

    public void setDeathTaxMinimum(double deathTaxMinimum)
    {
        this.deathTaxMinimum = deathTaxMinimum;
    }

    public double getDeathTaxMaximum()
    {
        return deathTaxMaximum;
    }

    public void setDeathTaxMaximum(double deathTaxMaximum)
    {
        this.deathTaxMaximum = deathTaxMaximum;
    }
}
