import java.io.*;
import java.util.*;

/**
 * A program that will add documentation to any SRL source file.
 * @author Cohen Adair
 * @since 24 March 2013
 *
 */
public class SRLAddDoc {
	
	public static Scanner in = new Scanner(System.in);
	public static String _author = "", _date = "", _editor = "", _in = "", _out = "";
	public static int lineCounter = 0;
	
	public static void main(String[] args) throws Exception {
		
		System.out.print("Enter non-documented file (ex. C:/text.simba): ");
		_in = in.nextLine();
		BufferedReader inF = new BufferedReader(new FileReader(_in));
		
		System.out.print("Save documented file as (ex. C:/newtext.simba): ");
		_out = in.nextLine();
		BufferedWriter outF = new BufferedWriter(new FileWriter(_out));
		
		System.out.print("Enter an author: ");
		_author = in.nextLine();
		
		System.out.print("Enter a modification date (ex. 24 March 2013): ");
		_date = in.nextLine();
		
		System.out.print("Enter an editor's name: ");
		_editor = in.nextLine();
		
		/*
		_in = "C:/in.simba";
		_out = "C:/out.simba";
		BufferedReader inF = new BufferedReader(new FileReader(_in));
		BufferedWriter outF = new BufferedWriter(new FileWriter("C:/out.simba"));
		*/
		String[] keywords = {"function", "procedure", "type", "var", "const"};		
		String line = null;
		String s = "";
		
		String shortName = new String(_out.substring(_out.indexOf('/') + 1, _out.indexOf('.')));
		
		String[] commentChars = new String[2];
		commentChars[0] = "";
		commentChars[1] = "";
		
		// script header
		writeNewLine(outF, 
			"(*"                                       +"\n"+
			shortName								   +"\n"+
			replicateStr("=", shortName.length())      +"\n"+"\n"+
					
			"Description of file here."                +"\n"+
			"*)"									   +"\n");
		
		while ((line = inF.readLine()) != null) {
			//System.out.println(line);
			lineCounter++;
			
			// skip blank lines
			if (line.length() <= 0)
				continue;
			
			// skip lines that don't have keywords
			if (line.charAt(0) == ' ') 
				continue;
			
			// write any lines that contain compiler directives/includes
			if (line.substring(0, 2).compareTo("{$") == 0) {
				writeNewLine(outF, line);
				continue;
			}
			
			s = "";
			
			// gets the first word on the line
			for (int i = 0; (i < line.length()); i++) {				
				if ((line.charAt(i) == ' ') || (i == (line.length() - 1))) {
					s = line.substring(0, i + ((i == (line.length() - 1)) ? 1 : 0));
					//System.out.println(s);
					break;
				}
			}
			
			int ind = stringInArray(s, keywords);						
			if (ind != -1) {
				if ((keywords[ind].compareTo("var") == 0) || 
					(keywords[ind].compareTo("const") == 0) ||
					(keywords[ind].compareTo("type") == 0)) 
					commentChars = getVarConstCommentChars(lineCounter);
				else
					commentChars = getCommentChars(line);
				
				outF.newLine();
				writeNewLine(outF, getDocText(keywords[ind], commentChars, line));
				writeNewLine(outF, line);
				insertCode(inF, outF, keywords[ind]);
				
				ind = -1;
			}			
		}
		
		inF.close();
		outF.close();
		
		System.out.println("Documentation successfully added.");
	}
	
	/**
	 * Writes the string to the output file and goes to the next line.
	 * @param outF The output file.
	 * @param str The string to be written to outF.
	 * @throws Exception
	 */
	public static void writeNewLine(BufferedWriter outF, String str) throws Exception {
		outF.write(str);
		outF.newLine();
	}
	
	/**
	 * Returns the index in "a" if the String "s" is found.  Returns -1 if not found.
	 * @param s The String to look for.
	 * @param a The array to look in for s.
	 * @return The index in "a" that "s" is found. 
	 */
	public static int stringInArray(String s, String[] a) {		
		
		for (int i = 0; (i < a.length); i++) {
			if (s.compareTo(a[i]) == 0)
				return i;
		}
		
		return -1;
	}
	
	/**
	 * Inserts the actual code text into the output file.
	 * @param inF The file that's being read.
	 * @param outF The file being written to.
	 * @param keyword "var", "function", "procedure", "type", "const".
	 * @throws Exception
	 */
	public static void insertCode(BufferedReader inF, BufferedWriter outF, String keyword) throws Exception {
		
		String line = inF.readLine(); lineCounter++;
		String endTxt = "";
	
		loop: 
		while (true) {

			switch (keyword) {
				case "const": case "var":
					if (line.length() == 0)
						break loop;
					
					writeNewLine(outF, line);
					break;
					
				case "type": case "function": case "procedure":
					if (keyword.compareTo("type") == 0)
						endTxt = "  end;";
					else
						endTxt = "end;";
					
					if (line.length() != 0) {
						if (line.compareTo(endTxt) == 0) {
							writeNewLine(outF, line);
							break loop;
						}
					}
					
					writeNewLine(outF, line);
					break;
					
				default:
					System.out.println("Invalid keyword in getText()");
					break;
			}
			
			lineCounter++;
			if ((line = inF.readLine()) == null) 
				break;
		}
	}
	
	/**
	 * Gets the default documentation text depending on the keyword.
	 * @param textType The type of text to get (function, var, etc.).
	 * @param commentChars The char to start the comment.
	 * @param currLine The current line's text.
	 * @return The default documentation text.
	 */
	public static String getDocText(String textType, String[] commentChars, String currLine) {
		String txt = "";
		
		switch (textType.toLowerCase()) {
			case "function": case "procedure":
				String name = getName(currLine);
				
				txt = commentChars[0]+"*"            +"\n"+
						name            +"\n"+
						replicateStr("~", name.length())         	    +"\n"+"\n"+

						".. code-block:: pascal"    					+"\n"+"\n"+

						"    "+currLine                    				+"\n"+"\n"+

						"Description of the function here." 			+"\n"+"\n"+

						".. note::"                        				+"\n"+"\n"+

						"    - by " + _author                             +"\n"+
						"    - Last Updated: " + _date + " by " + _editor +"\n"+"\n"+

						"Example:"                                      +"\n"+"\n"+

						".. code-block:: pascal"                        +"\n"+"\n"+

						"    Example code here."                        +"\n"+  
						"*"+commentChars[1];
				break;
				
			case "type":
				txt = commentChars[0]+"*"                                 +"\n"+
						"type NAME"                        +"\n"+
						"~~~~~~~~~"                        +"\n"+"\n"+
					
					  	"Description of type here."        +"\n"+
					  	"*"+commentChars[0];
				break;
				
			case "var":
				txt = commentChars[0]+"*"                  +"\n"+
						"var NAME"                         +"\n"+
						"~~~~~~~~"                         +"\n"+"\n"+
					
					  	"Description of variable(s) here." +"\n"+
					  	"*"+commentChars[1];
				break;
				
			case "const":
				txt = commentChars[0]+"*"                  +"\n"+
						"const NAME"                       +"\n"+
						"~~~~~~~~~~"                       +"\n"+"\n"+
					
					  	"Description of constants here."   +"\n"+
					  	"*"+commentChars[1];
				break;
				
			default: 
				txt = "null";
				break;
		}
		
		return txt;
	}
	
	/**
	 * Gets either the internal or external comment characters.
	 * @param line The text line to check for an _.
	 * @return "(" and ")" for external; "{" and "}" for internal.
	 */
	public static String[] getCommentChars(String line) {
		
		String[] res = new String[2];
		res[0] = "(";
		res[1] = ")";
		
		for (int i = 0; (i < line.length()); i++) {
			
			char cChar = line.charAt(i);
		
			if ((cChar == ' ') || (cChar == '.')) {
			
				if (line.charAt(i + 1) == '_') {
					res[0] = "{";
					res[1] = "}";
					break;
				}
			}
		}
		
		return res;				
	}
	
	/**
	 * Gets either the internal or external comment characters for a variable or constant.
	 * @param linesToSkip skip this many lines to get to the one that needs to be read.
	 * @return "(" and ")" for external; "{" and "}" for internal.
	 */
	public static String[] getVarConstCommentChars(int linesToSkip) throws Exception {
		
		BufferedReader newInF = new BufferedReader(new FileReader(_in));
		String[] res = new String[2];
		res[0] = "(";
		res[1] = ")";
		
		// skip lines that have already been read
		for (int i = 0; i < linesToSkip; i++)
			newInF.readLine();
		
		String line = new String(newInF.readLine());
		//System.out.println(line);
		
		// loops through the line until the first character is found
		// if the first character is an _ it will set new comment characters
		// otherwise it will break out of the loop and use the default
		for (int j = 0; j < line.length(); j++) {
			
			char c = line.charAt(j);
			
			// blank space
			if (c == ' ')		
				continue;
			
			if (c == '_') {
				res[0] = "{";
				res[1] = "}";
				break;
			} else
				break;
		}
		
		newInF.close();
		return res;			
	}
	
	/**
	 * Gets the name of a function.
	 * @param line The line of code to extract the name from.
	 * @return The function's or procedure's name.
	 */
	public static String getName(String line) {
		
		if (line.length() <= 0)
			return "";
		
		int s = 0, e = 0;
		
		for (int i = 0; (i < line.length()); i++) {
			
			char cChar = line.charAt(i);
		
			if (cChar == ' ') // if a space the name starts
				s = (i + 1);
			else
				if ((cChar == '(') || (cChar == ';') || (cChar == ':')) {
					e = i;
					break;
				}
		}
		
		return line.substring(s, e);
	}
	
	/**
	 * Repeats a string.
	 * @param str The string to be repeated.
	 * @param times The number of times the string is repeated.
	 * @return The repeated string.
	 */
	public static String replicateStr(String str, int times) {
		
		String txt = "";
		
		for (int i = 0; (i < times); i++)
			txt += str;
		
		return txt;
	}
}
