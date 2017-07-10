/**
* Author: Spencer Brydges 
* webTrees is the tree structured used for storing the tag/text/arc representation of a webOQL hypertree. All the required operators as per the assignment (tail, head, prime) are also implemented as well
*/
import java.util.*;

class webTrees
{
	public ArrayList<webTrees> children; //Use an arraylist, not a fixed array, as a tag may have an arbitrary number of tags enclosed within it
	public webTrees parent; //Reference to parent of tag
	public String tag; //Holds tag data, i.e, p
	public String text; //Holds text data, i.e., "Spencer Brydges"
	public boolean isExternal; //If true, then this node is an external arc (a link).
	
	/**
	* Primary constructor for initializing a new node. 
	* par = Parent of node being created. If the node is root (<html>), then par is null
	*/
	public webTrees(webTrees par, String tg, String txt, boolean setExt)
	{
		children = new ArrayList<webTrees>();
		parent = par;
		tag = tg;
		text = txt;
		isExternal = setExt;
	}
	
	/**
	* Returns the parent of a node, allowing for backwards tree traversal
	*/
	
	public webTrees getParent()
	{
		return this.parent;
	}
	
	/**
	* Creates a new child node when called by a parent node X
	*/
	
	public void addChild(String tg, String txt, boolean setExt)
	{
		if(setExt)
		{
			String nodeType = "External";
		}
		else
		{
			String nodeType = "Internal";
		}
		children.add(new webTrees(this, tg, txt, setExt));
	}
	
	/**
	* Grab last inserted child of node. Used to traversing forwards in tree
	*/
	
	public webTrees getChild()
	{
		return children.get(children.size()-1);
	}
	
	/**
	* Display contents of tree. Height is used for alignment purposes -- the further down the tree the traversal goes, the more spaces are printed to better align the data
	* Ref must be the root of the tree -- cannot pass an intermediate node
	* Traversal method is effectively preorder
	*/

	public static void print(webTrees ref, int height)
	{
		for(int n = 0; n < height; n++) //Align data depending on height of node
		{
			System.out.print(" ");
			System.out.print(" ");
			System.out.print(" ");
		}
		height++;
		//System.out.println("Called, height = " + height);
		String parent = (ref.parent == null) ? "ROOT" : ref.parent.tag;
		if(ref.isExternal)
		{
			System.out.println("[Label: " + ref.text+ ", URL: " + ref.tag + "]" + "(Parent: " + parent + ")"); //Print the parent tag
		}
		else
		{
			System.out.println("[Tag: " + ref.tag + ", Text: " + ref.text + "]" + "(Parent: " + parent + ")"); //Print the parent tag
		}
		for(int i = 0; i < ref.children.size(); i++) //Iteratively call method for every child of parent
		{
			print(ref.children.get(i), height);
		}
	}
	
	/**
	* The following methods make up 90% of the code for part II of the assignment
	*/
	
	/**
	* Method returns the tree as a result of applying the tail operator
	* This is accomplished by simply performing the following:
	* A) Check if node is root (root will not have a parent). If it is, print it and only i+1, i+2...n of its children, effectively skipping first child
	* B) Iteratively call method in order to grab the children of children
	* Ref = root of tree
	* newTree = tree object containing tail. First initialize it to the root, then pass created children recurisvely to this same method until no more children exist.
	*/
	
	
	public static webTrees tail(webTrees ref, webTrees newTree)
	{
		if(ref.children.size() != 0)
		{
			if(ref.parent == null) //Check that this node is the parent
			{
				newTree = new webTrees(null, ref.tag, ref.text, false); //Initialize new webTree for return, designating document root as root
				for(int i = 1; i < ref.children.size(); i++) //Since this is the parent, obtain the tail by removing first branch...just start at 1 and not 0
				{
					newTree.addChild(ref.children.get(i).tag, ref.children.get(i).text, ref.children.get(i).isExternal); //Add child node
					webTrees.tail(ref.children.get(i), newTree.getChild()); //Repeat this process of adding nodes by visiting the children
				}
			}
			else
			{
				for(int i = 0; i < ref.children.size(); i++) //Since this is not the root, it is OK to add all the children to the tree
				{
					newTree.addChild(ref.children.get(i).tag, ref.children.get(i).text, ref.children.get(i).isExternal);
					webTrees.tail(ref.children.get(i), newTree.getChild()); 
				}
			}
		}
		return newTree;
	}

	/**
	* Much like its counterpart tail, head uses the following logic to achieve its task;
	* A) If ref is root, then initialize newTree to it and recursively add only X children of root
	* B) If ref is not root, then freely add children without constraints, repeating the same code from tail
	*/
	
	public static webTrees head(webTrees ref, webTrees newTree, int x)
	{
		if(ref.parent == null) //If this node is the root, simply print the first x children. Otherwise, print all children to complete simple tree  
		{
			newTree = new webTrees(null, ref.tag, ref.text, false); //Following the same logic from tail, initialize newTree to root and begin to add children all the way to X
			for(int i = 0; i < x; i++)
			{
				newTree.addChild(ref.children.get(i).tag, ref.children.get(i).text, ref.children.get(i).isExternal); //Add child node
				webTrees.head(ref.children.get(i), newTree.getChild(), x); //Repeat this process of adding nodes by visiting the children
			}
		}
		else
		{
			for(int i = 0; i < ref.children.size(); i++)
			{
				newTree.addChild(ref.children.get(i).tag, ref.children.get(i).text, ref.children.get(i).isExternal);
				webTrees.head(ref.children.get(i), newTree.getChild(), x); 
			}
		}
		return newTree;
	}
	
	/**
	* The prime method works a little different -- recursion cannot be used (it becomes complicated with this function).
	* Instead, the method finds the first subtree by repeatedly travelling down nodes to the left of the tree until it reaches a node 
	* whose children are leaves. At that point, the current node simply has to be returned
	*/

	public static webTrees prime(webTrees ref, webTrees newTree)
	{
		boolean doAdd = true;
		
		if(ref.parent == null) //If this node is the root, simply print the first x children. Otherwise, print all children to complete simple tree  
		{
			newTree = new webTrees(null, ref.tag, ref.text, false); //Following the same logic from tail, initialize newTree to root and begin to add children all the way to X
			for(int i = 0; i < 1; i++)
			{
				newTree.addChild(ref.children.get(i).tag, ref.children.get(i).text, ref.children.get(i).isExternal); //Add child node
				webTrees.prime(ref.children.get(i), newTree.getChild()); //Repeat this process of adding nodes by visiting the children
			}
		}
		else
		{
			for(int i = 0; i < ref.children.size(); i++)
			{
				newTree.addChild(ref.children.get(i).tag, ref.children.get(i).text, ref.children.get(i).isExternal);
				webTrees.prime(ref.children.get(i), newTree.getChild()); 
			}
		}
		//for(int i = ref.children.size()-1; i > x-1; i--)
		//{
		//	newTree.children.remove(i);
		//}
		return newTree;
	}

	
}
