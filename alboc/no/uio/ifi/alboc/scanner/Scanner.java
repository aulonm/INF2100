package no.uio.ifi.alboc.scanner;

/*
 * module Scanner
 */

import no.uio.ifi.alboc.chargenerator.CharGenerator;
import no.uio.ifi.alboc.error.Error;
import no.uio.ifi.alboc.log.Log;
import static no.uio.ifi.alboc.scanner.Token.*;

/*
 * Module for forming characters into tokens.
 */
public class Scanner {
	public static Token curToken, nextToken;
	public static String curName, nextName;
	public static int curNum, nextNum;
	public static int curLine, nextLine;
	private static boolean testSpacesAndComments;
	
	public static void init() {
		// -- Must be changed in part 0:	
		// nothing to be done.
	}

	public static void finish() {
		// -- Must be changed in part 0:
		// nothing to be done.
	}

	public static void readNext() {
		curToken = nextToken;
		curName = nextName;
		curNum = nextNum;
		curLine = nextLine;
		nextToken = null;
		testSpacesAndComments = true;
		
		
		while(nextToken == null) {
			nextLine = CharGenerator.curLineNum();
			
			/*
				remove tabs, spaces and comment blocks. 
				Done in a while loop in case there are several blocks of these in a row. 

			*/
			while(testSpacesAndComments){ 
				removeTrash();
			}
			// test if we're at the end, if so set end of file.
			if (!CharGenerator.isMoreToRead()) {
				nextToken = eofToken;
			}
			//first we pick up the simple ones, and create appropriate tokens:
			else if(CharGenerator.curC == '+') {
				nextToken = addToken;
			} else if(CharGenerator.curC == '&') {
				nextToken = ampToken;
			} else if(CharGenerator.curC == ',') {
				nextToken = commaToken;
			} else if(CharGenerator.curC == '[') {
				nextToken = leftBracketToken;
			} else if(CharGenerator.curC == '(') {
				nextToken = leftParToken;
			} else if(CharGenerator.curC == '{') {
				nextToken = leftCurlToken;
			} else if(CharGenerator.curC == ']') {
				nextToken = rightBracketToken;
			} else if(CharGenerator.curC == ')') {
				nextToken = rightParToken;
			} else if(CharGenerator.curC == '}') {
				nextToken = rightCurlToken;
			} else if(CharGenerator.curC == ';') {
				nextToken = semicolonToken;
			} else if(CharGenerator.curC == '*') {
				nextToken = starToken;
			} else if(CharGenerator.curC == '-') {
				nextToken = subtractToken;
			} else if(CharGenerator.curC == '/') {
				nextToken = divideToken;
			}
			// we get a bit more advanced
			// first we check if there's a quotation mark indicating a char.
			// If that's the case, convert the char value to an int, and read past quotation marks. 
			else if (CharGenerator.curC == '\'') {
				CharGenerator.readNext();
				nextToken = numberToken; // read past first quotation mark
				nextNum = (int) CharGenerator.curC;
				CharGenerator.readNext(); // read past second quotation mark
			
			// check for an int, though represented as a char/string.
			} else if((isNumber(CharGenerator.curC)) == true) {
				String num = ""+CharGenerator.curC; // initial number
				while((isNumber(CharGenerator.nextC)) == true) {
					num += CharGenerator.nextC; // append the values to the string
					CharGenerator.readNext(); // increment by one
				}
				nextToken = numberToken; // set the token
				nextNum = Integer.parseInt(num); // convert string to an integer

			// check for not equals sign. Has to match both, if not it's an invalid token
			} else if(CharGenerator.curC == '!' && CharGenerator.nextC == '=') {
				nextToken = notEqualToken;
				CharGenerator.readNext(); // since we use two symbols to form token, do an extra readNext
			// check for assignment equals (=) or boolean equals (==)
			} else if(CharGenerator.curC == '=') {
				if(CharGenerator.nextC == '=') {
					nextToken = equalToken; // boolean equal
					CharGenerator.readNext(); // we two chars for token, do an extra readNext
				} else {
					nextToken = assignToken;
				}
			} else if(CharGenerator.curC == '>') { // greater than
				// check if next one is equals
				if(CharGenerator.nextC == '=') {
					nextToken = greaterEqualToken; // if it is, make it a greaten than or equals
					CharGenerator.readNext(); // we two chars for token, do an extra readNext
				} else {
					nextToken = greaterToken;
				}
			} else if(CharGenerator.curC == '<') { 	// less than
				// check if next one is equals
				if(CharGenerator.nextC == '=') {
					nextToken = lessEqualToken; // if it is, make it a less than or equals
					CharGenerator.readNext(); // we use both chars in token, need to increment the chargenerator
				} else {
					nextToken = lessToken;
				}

			// nameToken
			} else if(isLetterAZ(CharGenerator.curC) == true) {
				String name = "" + CharGenerator.curC; // initial String
				while(isLetterAZ(CharGenerator.nextC) == true || isNumber(CharGenerator.nextC) == true) { 
					// note: second or later symbol can be both char or digit
					name += CharGenerator.nextC; // generate complete string
					CharGenerator.readNext(); // increment by one
				}

				// now check if it's a name or something specific. If so create appropriate token
				if(name.equals("int")) nextToken = intToken;
				else if(name.equals("for")) nextToken = forToken;
				else if(name.equals("else")) nextToken = elseToken;
				else if(name.equals("return")) nextToken = returnToken;
				else if(name.equals("while")) nextToken = whileToken;
				else if(name.equals("if")) nextToken = ifToken;
				else {
					// if not it's a nameToken
					nextToken = nameToken;
					nextName = name;
				}

				// done checking the tokens
			} else { // catch illegal tokens
				Error.error(nextLine, "Illegal symbol: '" + ((int) CharGenerator.curC)
						+ "'!");
			}
		}
		Log.noteToken(); // call the Log
		CharGenerator.readNext(); // increment CharGenerator
	}

	private static void removeTrash() {
		testSpacesAndComments = false; // initially set testSpacesAndComments to false. 
		// If this method finds something it is set to true, so we run through the complete test again
		
		// remove any tabs or spaces
		while(CharGenerator.curC == ' '  || CharGenerator.curC == 0x09) {
			testSpacesAndComments = true;
			CharGenerator.readNext();
		}

		// check for comment blocks
		if(CharGenerator.curC == '/' && CharGenerator.nextC == '*') {
			testSpacesAndComments = true;
			CharGenerator.readNext();
			CharGenerator.readNext(); // read past these two

			while(! (CharGenerator.curC == '*' && CharGenerator.nextC == '/')) { // while NOT we hit */ keep skipping
				CharGenerator.readNext();
			}
			// do two readNext to remove the */ from curC and nextC
			CharGenerator.readNext();
			CharGenerator.readNext();
		}
	}
	
	// checks if in legal number range
	private static boolean isNumber(char c) {
		char[] legalNumbers = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
		for (int i = 0 ; i < legalNumbers.length ; i++) if (c == legalNumbers[i]) return true;
		return false;
	}

	// check if in legal char range for name
    private static boolean isLetterAZ(char c) {
    	// check if the character passed in the parameters is a legal letter, small or big.
    	char[] legalCharacters = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 
    									  'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
    									  'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
    									  'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '_'};
    	for (int i = 0 ; i < legalCharacters.length ; i++) if (c == legalCharacters[i]) return true;
    	return false;
    }

    // no error handling. We just create tokens at this stage, wether the code is structured correctly is not yet relevant.
	public static void check(Token t) {
		if (curToken != t)
			Error.expected("A " + t);
	}

	public static void check(Token t1, Token t2) {
		if (curToken != t1 && curToken != t2)
			Error.expected("A " + t1 + " or a " + t2);
	}

	public static void skip(Token t) {
		check(t);
		readNext();
	}

	public static void skip(Token t1, Token t2) {
		check(t1, t2);
		readNext();
	}
}
