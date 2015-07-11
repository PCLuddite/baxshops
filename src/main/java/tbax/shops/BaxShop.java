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
package tbax.shops;

import java.io.Serializable;
import java.util.HashMap;

/**
 * A Shop represents a user's shop and its inventory of items.
 */
public class BaxShop extends Shop implements Serializable {

    private static final long serialVersionUID = 1L;
    
    public BaxShop() {
    }
    
    public int getIndexOfEntry(int id, int damage) {
        for(int index = 0; index < inventory.size(); index++) {
            if (inventory.get(index).itemID == id && inventory.get(index).itemDamage == damage) {
                return index;
            }
        }
        return -1;
    }
    
    /**
     * The flag options set for this shop
     */
    public HashMap<String, Object> flags = new HashMap<>();

    public Object getOption(String flagName) {
        Object o = flags.get(flagName);
        if (o == null) {
            return false;
        }
        return o;
    }
    
    public Object setOption(String flagName, Object option) {
        return flags.put(flagName, option);
    }
}
