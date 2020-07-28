package org.tbax.baxshops.commands;

import org.tbax.baxshops.BaxEntry;
import org.tbax.baxshops.BaxQuantity;
import org.tbax.baxshops.PlayerUtil;
import org.tbax.bukkit.commands.CmdActor;
import org.tbax.bukkit.commands.CommandArgument;
import org.tbax.bukkit.errors.CommandErrorException;
import org.tbax.bukkit.errors.PrematureAbortException;
import org.tbax.baxshops.Resources;
import org.tbax.bukkit.serialization.StoredPlayer;

import java.util.List;

public final class ShopCmdArg extends CommandArgument
{
    private final ShopCmdActor actor;

    public ShopCmdArg(ShopCmdActor actor, String arg)
    {
        super(arg);
        this.actor = actor;
    }

    public boolean isQty()
    {
        return BaxQuantity.isQuantity(asString());
    }

    public boolean isQtyNotAny()
    {
        return BaxQuantity.isQuantityNotAny(asString());
    }

    public BaxQuantity asShopQty(BaxEntry entry) throws PrematureAbortException
    {
        if (actor.getShop() == null)
            throw new CommandErrorException(Resources.NOT_FOUND_SELECTED);
        if (actor.getShop().hasFlagInfinite() && (BaxQuantity.isAll(asString()) || BaxQuantity.isMost(asString())))
            throw new CommandErrorException("This shop has infinite supplies. You cannot take " + asString().toLowerCase());
        return new BaxQuantity(asString(), actor.getPlayer(), actor.getShop().getItemStackInventory(), entry.toItemStack());
    }

    public BaxQuantity asPlayerQty()
    {
        return new BaxQuantity(asString(), actor.getPlayer(), actor.getInventory(), actor.getItemInHand());
    }

    public BaxEntry asEntry() throws PrematureAbortException
    {
        return asEntry(Resources.NOT_FOUND_SHOPITEM);
    }

    public BaxEntry asEntry(String errMsg) throws PrematureAbortException
    {
        return actor.getShop().getEntryFromString(asString(), errMsg);
    }

    public int asEntryIndex() throws PrematureAbortException
    {
        return asEntryIndex(Resources.NOT_FOUND_SHOPITEM);
    }

    public int asEntryIndex(String errMsg) throws PrematureAbortException
    {
        return actor.getShop().indexOf(asEntry(errMsg));
    }

    public List<BaxEntry> takeFromInventory() throws PrematureAbortException
    {
        return PlayerUtil.takeQtyFromInventory(asPlayerQty(), actor.getShop(), actor.getExcluded());
    }

    public List<BaxEntry> peekFromInventory() throws PrematureAbortException
    {
        return PlayerUtil.peekQtyFromInventory(asPlayerQty(), actor.getShop(), actor.getExcluded());
    }

    public CmdActor getActor()
    {
        return actor;
    }

    @Override
    public StoredPlayer asPlayer() throws PrematureAbortException
    {
        return (StoredPlayer)super.asPlayer();
    }

    @Override
    public StoredPlayer asPlayerSafe() throws PrematureAbortException
    {
        return (StoredPlayer)super.asPlayerSafe();
    }
}
