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
	private static int lineNum;

	public static void init() {
		try {
			sourceFile = new LineNumberReader(new FileReader(AlboC.sourceName));
		} catch (FileNotFoundException e) {
			Error.error("Cannot read " + AlboC.sourceName + "!");
		}
		sourceLine = "";
		sourcePos = 0;
		curC = nextC = ' ';
		lineNum = 1;
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
		if (sourceLine == null)
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
		// If position is bigger than sourceLine-1 then this happens
		System.out.println(curC+ "\t" + sourcePos + "\t" + sourceLine.length());
		if (sourcePos > sourceLine.length() - 1) {
			sourcePos = 0;
			// Reads an entire line
			try {
				sourceLine = sourceFile.readLine();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// Checks if that new line i null, eof
			if (sourceLine == null){
				return;
			}
			Log.noteSourceLine(lineNum, sourceLine);
			//System.out.println(lineNum + "\t " + sourceLine);
			lineNum++;
			// Checks if there are any single-line comments
			
			if (sourceLine.charAt(sourcePos) == '#')
				readNextHelper();
			nextC = ' ';
		}
		// charAt(position) is assigned to nextC
		else{
			if(sourceLine.charAt(sourcePos) == '#')
				readNextHelper();
			else{
				nextC = sourceLine.charAt(sourcePos);
				sourcePos++;
			}		
		}
		
	}
	private static void readNextHelper(){
		try {
			sourceLine = sourceFile.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Log.noteSourceLine(lineNum, sourceLine);
		//System.out.println(lineNum + "\t " + sourceLine);
		lineNum++;
		readNext();
	}
}
