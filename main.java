/**
 * Author: Spencer Brydges
 * COSC 4806 Assignment - Convert HTML files to webOQL trees
 * Summary: In order to accomplish (part 1) of the assignment, I have constructed a rough algorithm that performs the following:
 * 1) Reads through the HTML file and removes all newlines and multiple spaces (in order to make parsing it far easier...)
 * 2) Read through the data tag-by-tag. First, it finds the initial "<" with respect to the current position. Then, 
 * it finds the immediate '>' character, clearing attributes in the process. If the tag is <a>, the program uses character positioning and substring matching once again
 * in order to pull HREF data (the URL) and the link text (the "label") between '>' and '<'. 
 * 3) If a standard tag (i.e., <h3>) is encountered, the program looks ahead in the file in order to find the first text match and associate it with
 * the initial tag and every tag following it
 *
 * IMPORTANT: The following assumptions are made
 * A) All tags other than <br>, <hr>, and <img> have closing tags in the document. For instance, <li> without a closing tag (as in the assignment document) is rejected
 */

import java.util.*;
import java.io.*;

class WebOQLConverter
{
	
	/**
	* parseContent performs 90% of the task required for part 1 of the assignment. 
	* Arguments: content = data read in from file, tree = reference to tree object (initially null), root = reference to root object (initially null)
	*/
	
	public static webTrees parseContent(String content, webTrees tree, webTrees root)
	{
		content = content.replace("\n", "").replace("\r", ""); //Let's make the document easier to parse by eliminating newlines and long whitespaces
		content = content.replaceAll("\\s+", " "); //Need to keep at least one space when necessary, otherwise document will be rendered useless
		System.out.println("Parsing the following received file contents: ");
		System.out.println(content + "\n\n");
		int posS = 0; //Keep track of the opening tag position
		int posF = 0; //Keep track of the ending tag position
		String tag = "";//Holds extracted tags (with attributes removed)
		String treeText = ""; //Holds Text in between tags
		boolean findText = true;
		int foundPos = 0; //Holds position of first text encountered
		htmlStack stack = new htmlStack(); //Keep track of elements 		
		
		for(int x = 0; x < content.length(); x++) //This is NOT character-by-character, as X will increment across tags
		{
			//Begin by finding the first tag
			posS = content.indexOf("<", posF); 
			posF = content.indexOf(">", posS);
			
			x = posF; //We do not care about the attributes and other characters in between. If it is a link, another function will extract it. Continue
			
			tag = content.substring(posS, posF+1); //Extract tag
			tag = clearAttributes(tag); //Remove attributes so we only have the tag

			if(tag.equals("<a>")) //Need to extract link from HREF
			{
				String label = extractLink(content.substring(posS, posF+1)); //Use original positions obtained in order to grab attributes
				int tmpStart = content.indexOf(">", posF); //Find text enclosed by <a>*</a>
				int tmpEnd = content.indexOf("<", tmpStart);
				String text = content.substring(tmpStart+1, tmpEnd);
				tree.addChild(label, text, true);
				continue;
			} 
			else if(tag.equals("<!DOCTYPE>") ||  tag.equals("<hr>") || tag.equals("<img>"))
			{
				continue; //Do nothing -- just designate HTML as root and move on
			}
			else if(tag.equals("<html>")) //Designate as root node -- do not go searching for texts, no need at this point
			{
				//System.out.println("[Tag: html, Text: ROOT NODE]");
				root = tree = new webTrees(null, tag, "Root", false); //Set as root of document
				continue;
			}
			
			if(!tag.contains("/")) 
			{
				int tposS = x;
				int tposF = x;
				String tmpTag = "";
				if(!tag.contains("br>") && !tag.contains("img") && !tag.contains("hr"))
				{
					stack.push(tag);
				}
				if(x > foundPos) //If we are at a position ahead of last found text, then check if there is more text ahead to append to labels
				{
					findText = true;
					treeText = "";
				}
				if(findText) //Now we must look ahead in the file for text, and append it to the tags that lead up to it accordingly
				{
					int tPosS = x;
					int tPosF = x;
					for(int j = x; j < content.length(); j++)
					{						
						tposS = content.indexOf(">", tposF);
						tposF = content.indexOf("<", tposS);

						if(tposS < 0 || tposF < 0) //Tag was not found...end of document must have been matched
                                                {
                                                        break;
                                                }
						//If only a space or null exists between tags, then there is no text here. Keep searching
						if(content.substring(tposS+1, tposF).equals(" ") || content.substring(tposS+1, tposF).equals("")) 
						{
							continue;
						}
						//If a link is encountered, do not assign the text. The text belonging to a link is only associated with said link
						if(content.substring(tposF+1, tposF+3).equals("/a"))
						{
							j = tposF+4; //Skip ahead to end of link
							continue;
						}
						
						j = tposF;
						
						//if(stack.getSize() == 0)
						//{		
							treeText = content.substring(tposS+1, tposF); //Since text exists and it is not a link, assign it to node
							findText = false; //Look ahead no further...text was found
							//break;
						//}
						foundPos = j;
						break;
					}
				}
				
				if(tag.equals("<br>") || tag.equals("</ br>"))
				{
					tree.addChild(tag, treeText, false);
				}
				else
				{
					tree.addChild(tag, treeText, false);
					tree = tree.getChild();
					System.out.println("Stack size = " + stack.getSize() + " after pushing " + tag);
				}
			}
			else
			{
				if(!tag.equals("</a>"))
				{
					if(tree.parent != null)
					{
						tree = tree.parent;
					}	
					stack.pop();
					System.out.println("Stack size = " + stack.getSize() + " after popping " + tag);
				}
			}
			
		}
		return root; //Return reference to root node so that caller method may use tree operators on it
	}
	
	/**
	* Simple method for clearing attributes from tag.
	* It achieves this by parsing the tag line-by-line, adding each character to a string. The minute a space is encountered, it immediately stops processing as attributes must follow
	*/
	
	public static String clearAttributes(String tag)
	{
		String cleanTag = "";
		for(int j = 0; j < tag.length(); j++) //Go through tag until " " is encountered
		{
			if(tag.substring(j, j+1).equals(" ")) //Attributes follow, i.e., <a[space]href="..."...>
			{
				break;
			}
			cleanTag += tag.substring(j, j+1); //Add character to string, building tag gradually
		}
		if(!cleanTag.substring(cleanTag.length()-1).equals(">")) //A tag with attributes would not have ">" appended to it on account of the loop breaking early...
		{
			cleanTag += ">";
		}
		return cleanTag;
	}
	
	/**
	* Simple method for extracting links from <a> tags
	* This is achieved by finding the index of href, then the indexes of ", and returning the substring (text between " and ")
	*/
	
	public static String extractLink(String ahref)
	{
		String link = "";
		int hrefPos = ahref.indexOf("href"); //Find initial position. Must be href since other attributes may occur before href, i.e., <a style='somestyle' href='...>
		int posS = ahref.indexOf("\"", hrefPos); //Since we have the position of href, the next " comes immediately after it
		int posF = ahref.indexOf("\"", posS+1); //Find the last " in href="..."
		link = ahref.substring(posS, posF+1); //Extract text between quotes and return
		return link;
	}

	
	public static void main(String [] args)
	{
		webTrees tree = null;
		webTrees root = null; //Keep a reference to the root of the tree
		Scanner in = new Scanner(System.in);
		String filename = "";
		System.out.print("Enter file to parse: ");
		filename = in.nextLine();
		BufferedReader fr = null;
		try
		{
			fr = new BufferedReader(new FileReader(filename));
		}
		catch(FileNotFoundException ex)
		{
			System.out.println("Error -- file not found");
		}
		
		String currentLine = "";
		String readIn = "";
		
		try
		{
			while((currentLine = fr.readLine()) != null)
			{
				if(currentLine.length() < 1) continue;
				while(!(currentLine.substring(currentLine.length() - 1).equals(">"))) //Ensure that every line finishes with a closing tag. Makes it easier to parse
				{
					String tmp = fr.readLine();
					if(tmp == null)
					{
						break;
					}
					currentLine += tmp;
				}
				readIn += currentLine + "\n";
			}
			
		//Now, read through the contents line-by-line and extract tags/texts/links in the process...
			
		root = parseContent(readIn, tree, root);
		System.out.println("\n\nOk, now all the nodes should be in the tree. Double check by calling print(): ");
                webTrees.print(root, 0);

		int choice = -1;
		webTrees operationTree = root;
			
		Scanner s = new Scanner(System.in);
			
		while(choice != 4)
		{
			System.out.println("\n\nYou can now perform the following functions: ");
			System.out.print("0) Use original tree\n1) Apply tail (!) operator to current tree\n2) Apply head (& [number]) operator to current tree\n3) Apply prime (') operator to current tree\n4) Quit\n");
			System.out.print("Enter choice: ");
			choice = s.nextInt();
			if(choice == 0)
			{
				System.out.println("Reassigning to original tree...: \n");
				operationTree = root; //Reassign original tree 
				webTrees.print(operationTree, 0);
			}
			else if(choice == 1)
			{
				System.out.println("Applying ! operator: \n");
				operationTree = webTrees.tail(operationTree, operationTree);
				webTrees.print(operationTree, 0);
			}
			else if(choice == 2)
			{
				int x = 0;
				while(x < 1 || x > 99)
				{
					System.out.print("Enter value for X (1-99): ");
					x = s.nextInt();
				}
				System.out.println("Applying & ["+x+"] operator: \n");
				operationTree = webTrees.head(operationTree, operationTree, x);
				webTrees.print(operationTree, 0);
			}
			else if(choice == 3)
			{
				System.out.println("Applying ' operator: \n");
				operationTree = webTrees.prime(operationTree, operationTree);
				webTrees.print(operationTree, 0);
			}
			
		}

		}
		catch(IOException e)
		{
			System.out.println("An error has occurred -- " + e);
		}
		try {fr.close();} catch(IOException e) {}
	}
}
