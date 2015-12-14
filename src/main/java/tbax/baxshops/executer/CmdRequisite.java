/* 
 * The MIT License
 *
 * Copyright © 2013-2015 Timothy Baxendale (pcluddite@hotmail.com) and 
 * Copyright © 2012 Nathan Dinsmore and Sam Lazarus.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package tbax.baxshops.executer;

import tbax.baxshops.help.CommandHelp;
import tbax.baxshops.Main;
import tbax.baxshops.Resources;

/**
 * @author Timothy Baxendale (pcluddite@hotmail.com)
 * Declares a set of conditions that a command must meet in order for it to be executed properly
 * When a requirement is not met, the validity of this requisite will be set to false.
 * When the validity of the requisite is set to false, all subsequent requirements will also return false,
 * so only one requirement failing will cause all subsequent requirements to fail.
 */
public final class CmdRequisite
{
    private final ShopCmd cmd;
    private boolean valid = true;
    
    public CmdRequisite(ShopCmd cmd)
    {
        this.cmd = cmd;
    }
    
    /**
     * Returns a value indicating whether all requisites were met
     * @return 
     */
    public boolean isValid()
    {
        return valid;
    }
    
    /**
     * The sender must have at least one of the given permissions
     * @param permissions
     * @return 
     */
    public boolean hasPermissions(String... permissions) 
    {
        if (!valid) return false;
        boolean hasPerms = false;
        for(String perm : permissions) {
            hasPerms = hasPerms || cmd.getSender().hasPermission(perm);
            if (hasPerms) {
                return true;
            }
        }
        Main.sendError(cmd.getSender(), Resources.NO_PERMISSION);
        return valid = false;
    }
    
    /**
     * The sender has a shop currently selected
     * @return 
     */
    public boolean hasSelection()
    {
        if (!valid) return false;
        if (cmd.getSelection() == null) {
            Main.sendError(cmd.getSender(), Resources.NOT_FOUND_SELECTED);
            return valid = false;
        }
        else {
            return true;
        }
    }
    
    /**
     * Ensure a minimum number of arguments
     * @param num
     * @return 
     */
    public boolean hasArgs(int num)
    {
        if (!valid) return false;
        if (cmd.getNumArgs() < num) {
            Main.sendError(cmd.getSender(), String.format("The command /shop %s expects %d arguments.", cmd.getAction(), num));
            return valid = false;
        }
        else {
            return true;
        }
    }
    
    /**
     * Ensure a minimum number of arguments
     * @param num
     * @param help
     * @return 
     */
    public boolean hasArgs(int num, CommandHelp help)
    {
        if (!valid) return false;
        if (cmd.getNumArgs() < num) {
            Main.sendError(cmd.getSender(), help.toUsageString());
            return valid = false;
        }
        else {
            return true;
        }
    }
    
    /**
     * Ensure an exact number of arguments
     * @param num
     * @param help
     * @return 
     */
    public boolean hasExactArgs(int num, CommandHelp help)
    {
        if (!valid) return false;
        if (cmd.getNumArgs() == num) {
            return true;
        }
        else {
            Main.sendError(cmd.getSender(), help.toUsageString());
            return valid = false;
        }
    }
    
    /**
     * Sender has ownership or admin permissions
     * @return 
     */
    public boolean hasOwnership()
    {
        if (!valid) return false;
        if (cmd.getSelection().isOwner || cmd.getSender().hasPermission("shops.admin")) {
            return true;
        }
        else {
            Main.sendError(cmd.getSender(), Resources.NO_PERMISSION);
            return valid = false;
        }
    }
}
