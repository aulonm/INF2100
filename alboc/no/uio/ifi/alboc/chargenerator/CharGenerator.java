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
	private static boolean logLine;
	private static boolean isFirstLine;

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
		isFirstLine = true;
		logLine = false;
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
		if ((sourceLine == null) && (curC == 0x03))
			return false;
		return true;
	}

	public static int curLineNum() {
		return (sourceFile == null ? 0 : sourceFile.getLineNumber());
	}

	/*
	 * Leser kildefilen linje for linje, ignorer #-linjer
	 * og sende den resterende kildekoden tegn for tegn videre
	 * i to variabler curC og nextC. Denne metoden klargjor neste tegn
	 */
	public static void readNext() {
		curC = nextC;
		if (!isMoreToRead())
			return;
		/*
		 * Hadde et lite problem naar vi logget. Hvor CharGenerator var alltid
		 * et hakk foran Scanneren, dermed maatte denne if setningen inn i bildet
		 * If den er true saa logger den og gjor logLine til false, og lineNum oker
		 */
		if(logLine == true){
			Log.noteSourceLine(lineNum, sourceLine);
			lineNum++;
			logLine = false;
		}
		/*
		 * Hvis position er storre enn sourceLine sin lengde
		 * saa vil det si at vi skal lese ny linje fordi vi er ferdig
		 * med den forrige
		 */
		if (sourcePos > sourceLine.length() - 1) {
			// Resetter position
			sourcePos = 0;
			try {
				// Leser ny linje
				sourceLine = sourceFile.readLine();
				/*
				 * Helt i begynnelsen av fila, saa mista jeg forste linje
				 * naar jeg satt inn if-setningen over, saa dermed maatte en
				 * kode det litt mer hacky
				 */
				if(isFirstLine == true){
					Log.noteSourceLine(lineNum, sourceLine);
					lineNum++;
					isFirstLine = false;
				}
				// Gj�r den til true s�nn at neste gang readNext blir kjort, saa
				// logges linja
				logLine = true;
				/*
				 * Saa lenge sourceLine ikke er null samtidig som lengden
				 * paa den er 0, saa skal den logge. Dette er for siste
				 * linje vi leser inn fra fila. 
				 */
				while(sourceLine != null && sourceLine.length() == 0){
					Log.noteSourceLine(lineNum, sourceLine);
					lineNum++;
					if(sourceLine == null){
						nextC = 0x03;
						return;
					}
					sourceLine = sourceFile.readLine();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// Checks if that new line i null, eof
			if (sourceLine == null){
				nextC = 0x03;
				return;
			}
			// Hvis det er # i begynnelsen saa kjor hjelpemetode
			// for aa hoppe over de linjene
			if (sourceLine.charAt(0) == '#')
				readNextHelper();
			// M� nullstille nextC
			nextC = ' ';
		}
		/*
		 * Hvis if-setningen ikke slaar til saa skal dette skje
		 * sjekker igjen om char[0] er # eller ikke
		 * else sett nextC til aa bli sourLine.charAt(pos)
		 */
		else{
			if(sourceLine.charAt(0) == '#')
				readNextHelper();
			else{
				nextC = sourceLine.charAt(sourcePos);
				sourcePos++;
			}		
		}
	}
	/*
	 * Bruker denne hjelpemetoden for aa lese neste linje hvis char[0] var #
	 * saa kjores dette rekursivt, saann at readNext blir kalt igjen etter at linja er lest
	 */
	private static void readNextHelper(){
		try {
			sourceLine = sourceFile.readLine();
			logLine = true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		readNext();
	}
}
