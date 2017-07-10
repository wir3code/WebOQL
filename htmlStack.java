/**
* Author: Spencer Brydges
* htmlStack is a simple stack implementation for keeping track of tags. When an opening tag is encountered, is it pushed onto the stack. Conversely, when a closing tag is encountered,
* the last element added is popped. Hence, the document must be well-formed in order for the parser to work. For instance, <someTag><b></someTag></b> would lead to an incorrect parse.
* Since this is just for an assignment, there is no validator...checking if a closing tag matches an opening tag is just extra work that is not needed for this program to run.
*/

import java.util.*;

class htmlStack
{
	private String [] tags; //Array-based stack holding html tags encountered
	private int pointer; //Keep track of stack location
	
	public htmlStack()
	{
		tags = new String[200]; //200 is a relatively safe number...most HTML files shouldn't exceed 200 consecutive tags 
	}
	
	public void push(String tag)
	{
		tags[pointer] = tag; //Place the next encountered tag on the stack
		pointer++;
	}
	
	public void pop()
	{
		if(pointer != 0)
		{
			pointer--;
			String tmp = tags[pointer];
			tags[pointer] = "";
		}
	}
	
	public boolean isEmpty() //If the stack is empty, then there is no matching text for encountered tags in a given region
	{
		return (pointer == 0) ? true : false;
	}

	public int getSize() //The size of the stack determines how many spaces will be printed (for alignment/neatness purposes)
	{
		return pointer;
	}
}
