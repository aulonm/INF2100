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

	public static void init() {
		// -- Must be changed in part 0:	
	}

	public static void finish() {
		// -- Must be changed in part 0:
	}

	public static void readNext() {
		curToken = nextToken;
		System.out.println("" + curToken);
		curName = nextName;
		curNum = nextNum;
		curLine = nextLine;
		nextToken = null;
			
		// check if space
		while(CharGenerator.curC == ' ') {
			CharGenerator.readNext();
			
		}

		// check for comment blocks
		if(CharGenerator.curC == '/' && CharGenerator.nextC == '*') {
			CharGenerator.readNext();
			CharGenerator.readNext(); // read past these two

			while(! (CharGenerator.curC == '*' && CharGenerator.nextC == '/')) { // while NOT we hit */ keep skipping
				CharGenerator.readNext();
			}
		}
			
		if (!CharGenerator.isMoreToRead()) {
			nextToken = eofToken;
		}
		//first we pick up the simple ones:
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
		// we get a bit more advanced
		} else if((isNumber(CharGenerator.curC)) == true) {
			String num = ""+CharGenerator.curC; // initial number


			while((isNumber(CharGenerator.nextC)) == true) {
				num += CharGenerator.nextC;
				CharGenerator.readNext(); // increment by one
			}
			nextToken = numberToken;
			nextNum = Integer.parseInt(num); // convert string representation of the int to an integer


		} else if(CharGenerator.curC == '=') {
			// check if next one is equals as well
			if(CharGenerator.nextC == '=') {
				nextToken = equalToken; // boolean equal
				CharGenerator.readNext(); // we use both chars in token, need to increment the chargenerator
			} else {
				nextToken = assignToken;
			}
			
		} else if(CharGenerator.curC == '>') {
			// check if next one is equals
			if(CharGenerator.nextC == '=') {
				nextToken = greaterEqualToken; // boolean equal
				CharGenerator.readNext(); // we use both chars in token, need to increment the chargenerator
			} else {
				nextToken = greaterToken;
			}
		} else if(CharGenerator.curC == '<') {
			// check if next one is equals
			if(CharGenerator.nextC == '=') {
				nextToken = lessEqualToken; // boolean equal
				CharGenerator.readNext(); // we use both chars in token, need to increment the chargenerator
			} else {
				nextToken = lessToken;
			}
			
		} else if(isLetterAZ(CharGenerator.curC) == true) {
			//System.out.println("Gaar inn");
			String name = "" + CharGenerator.curC;
			while(isLetterAZ(CharGenerator.nextC) == true) {
				name += CharGenerator.nextC; // generate complete string
				CharGenerator.readNext(); // increment by one
			}
			//System.out.println("" + name);
			// now check if it's a name or something specific.
			if(name.equals("int")){
				nextToken = intToken;
			}
			else if(name.equals("for")) nextToken = forToken;
			else if(name.equals("else")) nextToken = elseToken;
			else if(name.equals("return")) nextToken = returnToken;
			else if(name.equals("while")) nextToken = whileToken;
			else {
				nextToken = nameToken;
				nextName = name;
			}
			// done checking the tokens
		} else { // catch illegal tokens
			Error.error(nextLine, "Illegal symbol: '" + CharGenerator.curC
					+ "'!");
		}
		System.out.println("\t" + nextToken);
		Log.noteToken();
		CharGenerator.readNext();
		
	}
	
	private static boolean isNumber(char c) {
		char[] legalNumbers = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
		for (int i = 0 ; i < legalNumbers.length ; i++) if (c == legalNumbers[i]) return true;
		return false;
	}

    private static boolean isLetterAZ(char c) {
    	// check if the character passed in the parameters is a legal letter, small or big.
    	char[] legalCharacters = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 
    									  'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
    									  'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
    									  'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '_'};
    	for (int i = 0 ; i < legalCharacters.length ; i++) if (c == legalCharacters[i]) return true;
    	return false;
    }

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
