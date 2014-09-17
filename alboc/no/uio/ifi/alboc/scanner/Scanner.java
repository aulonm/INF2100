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
		curName = nextName;
		curNum = nextNum;
		curLine = nextLine;

		nextToken = null;
		while (nextToken == null) {
			nextLine = CharGenerator.curLineNum();

			if (!CharGenerator.isMoreToRead()) {
				nextToken = eofToken;
			} else
				// burde vi sette curToken = nextToken, og s[ assigne nextToken kanskje? hmmm.
				
				// check if space
				if(CharGenerator.curC == ' ') {
					// skip it somehow
				}
				//first we pick up the simple ones:
				else if(CharGenerator.curC == '+') {
					curToken = nextToken;
					nextToken = addToken;
					readNextHelper();
				} else if(CharGenerator.curC == '&') {
					curToken = nextToken;
					nextToken = ampToken;
					readNextHelper();
				} else if(CharGenerator.curC == ',') {
					curToken = nextToken;
					nextToken = commaToken;
					readNextHelper();
				} else if(CharGenerator.curC == '[') {
					curToken = nextToken;
					nextToken = leftBrackToken;
					readNextHelper();
				} else if(CharGenerator.curC == '(') {
					curToken = nextToken;
					nextToken = leftParToken;
					readNextHelper();
				} else if(CharGenerator.curC == '{') {
					curToken = nextToken;
					nextToken = leftCurlToken;
					readNextHelper();
				} else if(CharGenerator.curC == ']') {
					curToken = nextToken;
					nextToken = rightBrackToken;
					readNextHelper();
				} else if(CharGenerator.curC == ')') {
					curToken = nextToken;
					nextToken = rightParToken;
					readNextHelper();
				} else if(CharGenerator.curC == '}') {
					curToken = nextToken;
					nextToken = rightCurlToken;
					readNextHelper();
				} else if(CharGenerator.curC == ';') {
					curToken = nextToken;
					nextToken = semiColonToken;
					readNextHelper();
				} else if(CharGenerator.curC == '*') {
					curToken = nextToken;
					nextToken = starToken;
					readNextHelper();
				// we get a bit more advanced
				} else if(isNumber(charGenerator.curC) == true) {
					// do number logic
				} else if(CharGenerator.curC == '=') {
					// check if next one is equals as well
					curToken = nextToken;
					if(CharGenerator.nextC == '=') {
						nextToken = equalToken; // boolean equal
					} else {
						nextToken = assignToken;
					}
					readNextHelper();
				} else if(CharGenerator.curC == '>') {
					// check if next one is equals
					curToken = nextToken;
					if(CharGenerator.nextC == '=') {
						nextToken = greaterEqualToken; // boolean equal
					} else {
						nextToken = greaterToken;
					}
					readNextHelper();
				} else if(CharGenerator.curC == '<') {
					// check if next one is equals
					curToken = nextToken;
					if(CharGenerator.nextC == '=') {
						nextToken = lessEqualToken; // boolean equal
					} else {
						nextToken = lessToken;
					}
					readNextHelper();
				} else if(isLetterAZ(CharGenerator.curC) || CharGenerator.curC == '_') {
					// it's a word, an int, an if, and else or 
				}
				
			// -- Must be changed in part 0:
			{
				Error.error(nextLine, "Illegal symbol: '" + CharGenerator.curC
						+ "'!");
			}
		}
		Log.noteToken();
	}
	
	private void readNextHelper() {
		// this method does the steps common for all happy cases of readNext
	}
	
	private boolean isNumber(char c) {
		private char[] legalNumbers = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
		for (int i = 0 ; i < legalNumbers ; i++) if (c == legalNumbers[i]) return true;
		return false
	}

    private static boolean isLetterAZ(char c) {
    	// check if the character passed in the parameters is a legal letter, small or big.
    	private char[] legalCharacters = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 
    									  'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
    									  'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
    									  'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};
    	for (int i = 0 ; i < legalCharacters ; i++) if (c == legalCharacters[i]) return true;
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
