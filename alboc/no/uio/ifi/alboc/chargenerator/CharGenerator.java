package no.uio.ifi.alboc.chargenerator;

/*
 * module CharGenerator
 */

import java.io.*;

import no.uio.ifi.alboc.alboc.AlboC;
import no.uio.ifi.alboc.error.Error;
import no.uio.ifi.alboc.log.Log;

/*
 * Module for reading single characters.
 */
public class CharGenerator {
	public static char curC, nextC;

	private static LineNumberReader sourceFile = null;
	private static String sourceLine;
	private static int sourcePos;
	private static int sourceLength;

	public static void init() {
		try {
			sourceFile = new LineNumberReader(new FileReader(AlboC.sourceName));
		} catch (FileNotFoundException e) {
			Error.error("Cannot read " + AlboC.sourceName + "!");
		}
		sourceLine = "";
		sourcePos = 0;
		curC = nextC = ' ';
		readNext();
		readNext();
	}

	public static void finish() {
		if (sourceFile != null) {
			try {
				sourceFile.close();
			} catch (IOException e) {
				Error.error("Could not close source file!");
			}
		}
	}

	public static boolean isMoreToRead() {
		if(sourceLine == null)
			return false;
		return true;
	}

	public static int curLineNum() {
		return (sourceFile == null ? 0 : sourceFile.getLineNumber());
	}

	public static void readNext() {
		curC = nextC;
		
		if (!isMoreToRead())
			return;
		try {
			if (sourcePos > sourceLine.length()-1){
				nextC = sourceLine.charAt(sourcePos);
				sourcePos++;
				sourceLine = sourceFile.readLine();
				if(sourceLine == null)
					return;
				if(sourceLine.charAt(0) == '#')
					sourceLine = sourceFile.readLine();
				sourcePos = 0;
				
			}
			nextC = sourceLine.charAt(sourcePos);
			sourcePos++;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
