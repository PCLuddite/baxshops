package qs.shops;

import java.io.Serializable;

import net.minecraft.server.NBTTagCompound;
import net.minecraft.server.NBTTagList;
import net.minecraft.server.NBTTagString;

import org.bukkit.Material;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

public class ShopBook implements Serializable {
	
	private static final long serialVersionUID = 1L;
	/**
	 * The author of the book
	 */
	private String author;
	/**
	 * The title of the book
	 */
    private String title;
    /**
     * The content of the book as an array of Strings
     */
    private String[] pages;
 
    /**
     * Creates a ShopBook from the given itemstack
     * @param bookItem The Itemstack to create the book from
     */
    public ShopBook(ItemStack bookItem){
        NBTTagCompound bookData = ((CraftItemStack) bookItem).getHandle().tag;
       
        this.author = bookData.getString("author");
        this.title = bookData.getString("title");
               
        NBTTagList nPages = bookData.getList("pages");
 
        String[] sPages = new String[nPages.size()];
        for(int i = 0;i<nPages.size();i++)
        {
            sPages[i] = nPages.get(i).toString();
        }
               
        this.pages = sPages;
    }
 
    /**
     * Creates a ShopBook with the given parameters, to get
     * this book as an ItemStack, you would then call
     * generateItemStack() on the book.
     * @param title The title of the book
     * @param author The author of the book
     * @param pages The content of the book as an array of Strings
     */
    public ShopBook(String title, String author, String[] pages) {
        this.title = title;
        this.author = author;
        this.pages = pages;
    }
    
    /**
     * Check if the given ItemStack is a valid written book
     * @param item The item to check
     * @return True if the ItemStack is a valid written book, false if otherwise
     */
    public static boolean isBook(ItemStack item) {
    	return (item.getType() == Material.WRITTEN_BOOK);
    }
   
    /**
     * Gets the author of the book
     * @return The author of the book
     */
    public String getAuthor()
    {
        return author;
    }
 
    /**
     * Sets the author of the book
     * @param author The new author of the book
     */
    public void setAuthor(String author)
    {
        this.author = author;
    }
   
    /**
     * Gets the title of the book
     * @return The title of the book
     */
    public String getTitle()
    {
        return title;
    }
   
    /**
     * Gets the content of the book as an array of Strings,
     * each string represents one page of the book,
     * IE the seccond index in the book, would be a the
     * second page of the book.
     * @return An array of the book's pages
     */
    public String[] getPages()
    {
        return pages;
    }
 
    /**
     * Generates an ItemStack from the current book
     * @return The new book created as an ItemStack
     */
    public ItemStack generateItemStack(){
        CraftItemStack newbook = new CraftItemStack(Material.WRITTEN_BOOK);
       
        NBTTagCompound newBookData = new NBTTagCompound();
       
        newBookData.setString("author",author);
        newBookData.setString("title",title);
               
        NBTTagList nPages = new NBTTagList();
        for(int i = 0;i<pages.length;i++)
        { 
            nPages.add(new NBTTagString(pages[i],pages[i]));
        }
       
        newBookData.set("pages", nPages);
 
        newbook.getHandle().tag = newBookData;
       
        return (ItemStack) newbook;
    }
}