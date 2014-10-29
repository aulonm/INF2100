package no.uio.ifi.alboc.scanner;

/*
 * class Token
 */

/*
 * The different kinds of tokens read by Scanner.
 */
public enum Token { 
    addToken, ampToken, assignToken, 
    commaToken, 
    divideToken,
    elseToken, eofToken, equalToken, 
    forToken, 
    greaterEqualToken, greaterToken, 
    ifToken, intToken, 
    leftBracketToken, leftCurlToken, leftParToken, lessEqualToken, lessToken, 
    nameToken, notEqualToken, numberToken, 
    returnToken, rightBracketToken, rightCurlToken, rightParToken, 
    semicolonToken, starToken, subtractToken, 
    whileToken;

	public static boolean isFactorOperator(Token t) {
		// -- Must be changed in part 0:
        return (t == starToken || t == divideToken);
	}

	public static boolean isTermOperator(Token t) {
		// -- Must be changed in part 0:
        return (t == addToken || t == subtractToken);
	}

	public static boolean isPrefixOperator(Token t) {
		// -- Must be changed in part 0:
		return (t == subtractToken || t == starToken);
	}

	public static boolean isRelOperator(Token t) {
		// -- Must be changed in part 0:
		return (t == equalToken || t == notEqualToken || t == lessToken
                || t == lessEqualToken || t == greaterToken || t == greaterEqualToken);
	}

	public static boolean isOperand(Token t) {
		// -- Must be changed in part 0:
		return (t == numberToken || t == nameToken || t == leftParToken
                || t == ampToken);
	}
}
