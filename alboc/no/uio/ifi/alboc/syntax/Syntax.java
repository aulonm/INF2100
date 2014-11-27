package no.uio.ifi.alboc.syntax;

/*
 * module Syntax
 */

import no.uio.ifi.alboc.alboc.AlboC;
import no.uio.ifi.alboc.code.Code;
import no.uio.ifi.alboc.error.Error;
import no.uio.ifi.alboc.log.Log;
import no.uio.ifi.alboc.scanner.Scanner;
import no.uio.ifi.alboc.scanner.Token;
import static no.uio.ifi.alboc.scanner.Token.*;
import no.uio.ifi.alboc.types.*;


/*
 * Creates a syntax tree by parsing an AlboC program; 
 * prints the parse tree (if requested);
 * checks it;
 * generates executable code. 
 */
public class Syntax {
	static DeclList library;
	static Program program;

	public static void init() {
		// -- Must be changed in part 1+2:

        library = new GlobalDeclList();
        library.addDecl(new FuncDecl("exit", Types.intType, "status"));
        library.addDecl(new FuncDecl("getchar", Types.intType, null));
        library.addDecl(new FuncDecl("getint",  Types.intType, null));
        library.addDecl(new FuncDecl("putchar",  Types.intType, "c"));
        library.addDecl(new FuncDecl("putint", Types.intType, "c"));

        Scanner.readNext();
        Scanner.readNext();
	}

	public static void finish() {
		// -- Must be changed in part 1+2:
	}

	public static void checkProgram() {
		program.check(library);
	}

	public static void genCode() {
		program.genCode(null);
	}

	public static void parseProgram() {
		program = Program.parse();
	}

	public static void printProgram() {
		program.printTree();
	}
}

/*
 * Super class for all syntactic units. (This class is not mentioned in the
 * syntax diagrams.)
 */
abstract class SyntaxUnit {
	int lineNum;

	SyntaxUnit() {
		lineNum = Scanner.curLine;
	}

	abstract void check(DeclList curDecls);

	abstract void genCode(FuncDecl curFunc);

	abstract void printTree();

	void error(String message) {
		Error.error(lineNum, message);
	}
}

/*
 * A <program>
 */
class Program extends SyntaxUnit {
	DeclList progDecls;

	@Override
	void check(DeclList curDecls) {
		progDecls.check(curDecls);

		if (!AlboC.noLink) {
			// Check that 'main' has been declared properly:
			// -- Must be changed in part 2:
            FuncDecl main;
            Declaration d = progDecls.findDecl("main", null);
            Log.noteBinding("main", 0, d.lineNum);

            main = (FuncDecl) d;
            if(main.type != Types.intType)
                error("'main' should return type 'int'");

		}
	}

	@Override
	void genCode(FuncDecl curFunc) {
		progDecls.genCode(null);
	}

	static Program parse() {
		Log.enterParser("<program>");

		Program p = new Program();
		p.progDecls = GlobalDeclList.parse();
		if (Scanner.curToken != eofToken) {
            Error.expected("A declaration");
        }
        //System.out.println(Log.ut + "  what  " + Log.inn);
		Log.leaveParser("</program>");

		return p;
	}

	@Override
	void printTree() {
		progDecls.printTree();

	}
}

/*
 * A declaration list. (This class is not mentioned in the syntax diagrams.)
 */

abstract class DeclList extends SyntaxUnit {
	Declaration firstDecl = null;
	DeclList outerScope;


	DeclList() {
		// -- Must be changed in part 1:
	}

	@Override
	void check(DeclList curDecls) {
		outerScope = curDecls;

		Declaration dx = firstDecl;
		while (dx != null) {
			dx.check(this);
			dx = dx.nextDecl;
		}
	}

	@Override
	void printTree() {
		// -- Must be changed in part 1:
        Declaration currDecl = firstDecl;
        while(currDecl != null){
            currDecl.printTree();
            currDecl = currDecl.nextDecl;
        }
	}

    // Lager FIFO liste
    void addDecl(Declaration d) {
		// -- Must be changed in part 1:

        if(firstDecl == null){
            firstDecl = d;
        }
        else{
            Declaration prevDecl = firstDecl;
            while(prevDecl.nextDecl != null){
                if(prevDecl.name.equals(d.name))
                    d.error("name " + d.name + " already declared");
                prevDecl = prevDecl.nextDecl;
            }
            if(prevDecl.name.equals(d.name))
                d.error("name " + d.name + " already declared");
            prevDecl.nextDecl = d;
        }
	}

	int dataSize() {
		Declaration dx = firstDecl;
		int res = 0;

		while (dx != null) {
			res += dx.declSize();
			dx = dx.nextDecl;
		}
		return res;
	}

	Declaration findDecl(String name, SyntaxUnit use) {
		// -- Must be changed in part 2:
        Declaration dx = firstDecl;
        while(dx != null){
            if(dx.name.equals(name) && dx.visible == true){
				System.out.println(dx.name);
                return dx;
            }

            dx = dx.nextDecl;
        }
        if(outerScope != null){
            return outerScope.findDecl(name, use);
        }else{
			if(use != null){
				Error.error("unknown");
			}else{
				Error.error("unknown2");
			}
		}

        Error.error("name " + name + " is unknown");

        return null;
	}
}

/*
 * A list of global declarations. (This class is not mentioned in the syntax
 * diagrams.)
 */
class GlobalDeclList extends DeclList {
	@Override
	void genCode(FuncDecl curFunc) {
		// -- Must be changed in part 2:
		Declaration gdl = firstDecl;
		while(gdl != null){
			gdl.genCode(curFunc);
			gdl = gdl.nextDecl;
		}
	}

	static GlobalDeclList parse() {
		GlobalDeclList gdl = new GlobalDeclList();

		while (Scanner.curToken == intToken) {
			DeclType ts = DeclType.parse();
			gdl.addDecl(Declaration.parse(ts));
		}
		return gdl;
	}
}

/*
 * A list of local declarations. (This class is not mentioned in the syntax
 * diagrams.)
 */
class LocalDeclList extends DeclList {
	@Override
	void genCode(FuncDecl curFunc) {
		// -- Must be changed in part 2:
		if(firstDecl != null) {
			// Declaration ldl = firstDecl;
			// while(ldl != null){
			// 	ldl.genCode(curFunc);
			// 	ldl = ldl.nextDecl;
			// }

			Declaration ldl = firstDecl;
			if(ldl.nextDecl != null) {
				ldl.nextDecl.genCode(curFunc);
			}
			ldl.genCode(curFunc);

		}
	}


	static LocalDeclList parse() {
		// -- Must be changed in part 1:
		LocalDeclList ldl = new LocalDeclList();

        while(Scanner.curToken == intToken){
            DeclType ts = DeclType.parse();
            ldl.addDecl(LocalVarDecl.parse(ts));
        }
        return ldl;
	}
}

/*
 * A list of parameter declarations. (This class is not mentioned in the syntax
 * diagrams.)
 */
class ParamDeclList extends DeclList {
    static int length = 0;

	@Override
	void genCode(FuncDecl curFunc) {
		// -- Must be changed in part 2:'
		if(firstDecl != null) {
			Declaration pdl = firstDecl;
			if(pdl.nextDecl != null) {
				pdl.nextDecl.genCode(curFunc);
			}
			pdl.genCode(curFunc);
		}
		// Declaration pdl = firstDecl;
		// while(pdl != null){
		// 	pdl.genCode(curFunc);
		// 	pdl = pdl.nextDecl;
		// }

	}


	static ParamDeclList parse() {
		// -- Must be changed in part 1:
		ParamDeclList pdl = new ParamDeclList();

        while(Scanner.curToken == intToken){
            DeclType ts = DeclType.parse();
            pdl.addDecl(ParamDecl.parse(ts));
            length++;
        }

        return pdl;
	}

	@Override
	void printTree() {
		Declaration px = firstDecl;
		while (px != null) {
			px.printTree();
			px = px.nextDecl;
			if (px != null)
				Log.wTree(", ");

		}
	}
}

/*
 * A <type>
 */
class DeclType extends SyntaxUnit {
	int numStars = 0;
	Type type;

	@Override
	void check(DeclList curDecls) {
		type = Types.intType;
		for (int i = 1; i <= numStars; ++i)
			type = new PointerType(type);
	}

	@Override
	void genCode(FuncDecl curFunc) {
	}

	static DeclType parse() {
		Log.enterParser("<type>");

		DeclType dt = new DeclType();

		Scanner.skip(intToken);
		while (Scanner.curToken == starToken) {
			++dt.numStars;
			Scanner.readNext();
		}

		Log.leaveParser("</type>");
		return dt;
	}

	@Override
	void printTree() {
		Log.wTree("int");
		for (int i = 1; i <= numStars; ++i)
			Log.wTree("*");
	}
}

/*
 * Any kind of declaration.
 */
abstract class Declaration extends SyntaxUnit {
	String name, assemblerName;
	DeclType typeSpec;
	Type type;
	boolean visible = false;
	Declaration nextDecl = null;
	int offset = 0;

	Declaration(String n) {
		name = n;
	}

	abstract int declSize();

	static Declaration parse(DeclType dt) {
		Declaration d = null;
		if (Scanner.curToken == nameToken && Scanner.nextToken == leftParToken) {
			d = FuncDecl.parse(dt);
		} else if (Scanner.curToken == nameToken) {
			d = GlobalVarDecl.parse(dt);
		} else {
			Error.expected("A declaration name");
		}
		d.typeSpec = dt;
		return d;
	}

	/**
	 * checkWhetherVariable: Utility method to check whether this Declaration is
	 * really a variable. The compiler must check that a name is used properly;
	 * for instance, using a variable name a in "a()" is illegal. This is
	 * handled in the following way:
	 * <ul>
	 * <li>When a name a is found in a setting which implies that should be a
	 * variable, the parser will first search for a's declaration d.
	 * <li>The parser will call d.checkWhetherVariable(this).
	 * <li>Every sub-class of Declaration will implement a checkWhetherVariable.
	 * If the declaration is indeed a variable, checkWhetherVariable will do
	 * nothing, but if it is not, the method will give an error message.
	 * </ul>
	 * Examples
	 * <dl>
	 * <dt>GlobalVarDecl.checkWhetherVariable(...)</dt>
	 * <dd>will do nothing, as everything is all right.</dd>
	 * <dt>FuncDecl.checkWhetherVariable(...)</dt>
	 * <dd>will give an error message.</dd>
	 * </dl>
	 */
	abstract void checkWhetherVariable(SyntaxUnit use);

	/**
	 * checkWhetherFunction: Utility method to check whether this Declaration is
	 * really a function.
	 * 
	 * @param nParamsUsed
	 *            Number of parameters used in the actual call. (The method will
	 *            give an error message if the function was used with too many
	 *            or too few parameters.)
	 * @param use
	 *            From where is the check performed?
	 * @see //checkWhetherVariable
	 */
	abstract void checkWhetherFunction(int nParamsUsed, SyntaxUnit use);
}

/*
 * A <var decl>
 */
abstract class VarDecl extends Declaration {
	boolean isArray = false;
	int numElems = 0;

	VarDecl(String n) {
		super(n);
	}

	@Override
	void check(DeclList curDecls) {
		// -- Must be changed in part 2:
        visible = true;

        typeSpec.check(curDecls);

        if(isArray){
            type = new ArrayType(typeSpec.type, numElems);
        }else {
            type = typeSpec.type;
        }
	}

	@Override
	void printTree() {
        typeSpec.printTree();
        Log.wTree(" " + name);
        Log.wTreeLn(";");
	}

	@Override
	int declSize() {
		return type.size();
	}

	@Override
	void checkWhetherFunction(int nParamsUsed, SyntaxUnit use) {
		use.error(name + " is a variable and no function!");
	}

	@Override
	void checkWhetherVariable(SyntaxUnit use) {
		// OK
	}
}

/*
 * A global <var decl>.
 */
class GlobalVarDecl extends VarDecl {
	GlobalVarDecl(String n) {
		super(n);
		assemblerName = (AlboC.underscoredGlobals() ? "_" : "") + n;
	}

	@Override
	void genCode(FuncDecl curFunc) {
		// -- Must be changed in part 2:
		// set offset

        if(isArray){
            Code.genVar(assemblerName, true, numElems, type.size()/numElems,
                    type + " " + name);
        } else{
            Code.genVar(assemblerName, true, 1, type.size(),
                    type + " " + name);
        }
	}

	static GlobalVarDecl parse(DeclType dt) {
        Log.enterParser("<var decl>");

        // -- Must be changed in part 1:
        GlobalVarDecl gv = new GlobalVarDecl(Scanner.curName);
        gv.typeSpec = dt;
        Scanner.skip(nameToken);
        if(Scanner.curToken == leftBracketToken) {
            // we found an array.
            gv.isArray = true;
            Scanner.skip(leftBracketToken); // skip this leftbracket
            gv.numElems = Scanner.curNum;
            Scanner.skip(numberToken); // skip the array size
            Scanner.skip(rightBracketToken); // skip the next right bracket token
        }
        Scanner.skip(semicolonToken);
        Log.leaveParser("</var decl>");
        return gv;

	}
}

/*
 * A local variable declaration
 */
class LocalVarDecl extends VarDecl {
	LocalVarDecl(String n) {
		super(n);
		
	}

	@Override
	void genCode(FuncDecl curFunc) {
		// -- Must be changed in part 2:

		assemblerName = "-"+curFunc.localOffset+"(%ebp)";
		curFunc.localOffset -= 4;

		}

	static LocalVarDecl parse(DeclType dt) {
		Log.enterParser("<var decl>");
        LocalVarDecl vv = new LocalVarDecl(Scanner.curName);
        vv.typeSpec = dt;
        Scanner.skip(nameToken);
        if(Scanner.curToken == leftBracketToken){
            vv.isArray = true;
            Scanner.skip(leftBracketToken);
            vv.numElems = Scanner.curNum;
            Scanner.skip(numberToken); // skip the array size
            Scanner.skip(rightBracketToken); // skip the next right bracket token
        }
        Scanner.skip(semicolonToken);
        Log.leaveParser("</var decl>");
		// -- Must be changed in part 1:
		return vv;
	}
}

/*
 * A <param decl>
 */
class ParamDecl extends VarDecl {
	ParamDecl(String n) {
		super(n);
	}

	@Override
	void genCode(FuncDecl curFunc) {
		// -- Must be changed in part 2:
		curFunc.paramOffset -= 4;
		assemblerName = curFunc.paramOffset+"(%ebp)";
		
	}



	static ParamDecl parse(DeclType dt) {
		Log.enterParser("<param decl>");

		// -- Must be changed in part 1:
        ParamDecl pd = new ParamDecl(Scanner.curName);
        pd.typeSpec = dt;
        Scanner.skip(nameToken);
        if(Scanner.curToken == commaToken){
            Scanner.skip(commaToken);
        }
        Log.leaveParser("</param decl>");
		return pd;
	}

	@Override
	void printTree() {
		typeSpec.printTree();
		Log.wTree(" " + name);
	}
}

/*
 * A <func decl>
 */
class FuncDecl extends Declaration {
	ParamDeclList funcParams;
    LocalDeclList funcVars;
    StatmList funcBody;
	String exitLabel;
	int paramOffset = 8;
	int localOffset = 0;

	FuncDecl(String n) {
		// Used for user functions:

		super(n);
		assemblerName = (AlboC.underscoredGlobals() ? "_" : "") + n;
		// -- Must be changed in part 1:
	}

    FuncDecl(String n, Type rt, String t){
        super(n);
		visible = true;
        assemblerName = (AlboC.underscoredGlobals() ? "_" : "") + n;
        this.type = rt;
        funcParams = new ParamDeclList();

        if(t != null){
            ParamDecl p = new ParamDecl(t);
            funcParams.addDecl(p);
            funcParams.firstDecl.type = Types.intType;

        }

    }

	@Override
	int declSize() {
		return 0;
	}

	@Override
	void check(DeclList curDecls) {
		// -- Must be changed in part 2:
        visible = true;
        typeSpec.check(curDecls);
        type = typeSpec.type;

        if(funcParams != null) {
            funcParams.check(curDecls);
            funcVars.check(funcParams);
        }
        else{
            funcVars.check(curDecls);
        }
        funcBody.check(funcVars);

    }

	@Override
	void checkWhetherFunction(int nParamsUsed, SyntaxUnit use) {
		// -- Must be changed in part 2:
        if(funcParams != null && funcParams.length != nParamsUsed){
            Error.error("FUCK THIS");
        }
	}

	@Override
	void checkWhetherVariable(SyntaxUnit use) {
		// -- Must be changed in part 2:
        use.error(name + " is a function and no variable!");

	}

	@Override
	void genCode(FuncDecl curFunc) {
		// -- Must be changed in part 2:
		Code.genInstr("", ".globl", assemblerName, "");
		Code.genInstr(assemblerName, "enter", "$"+funcVars.dataSize()+",$0", "Start function " + name);

		if(funcParams != null){
			paramOffset += funcParams.dataSize();
			funcParams.genCode(this);
		}
		localOffset += funcVars.dataSize();
		funcVars.genCode(this);
		funcBody.genCode(this);

		Code.genInstr(".exit$"+name,"","","");
		Code.genInstr("","leave","","");
		Code.genInstr("","ret","","End function " + name);

	}

	static FuncDecl parse(DeclType ts) {
		// -- Must be changed in part 1:
		        /*
        What to do:
        first parse ParamDecList and save to funcParams.
        next we need to parse a local declaration list
        then we need some sort of function body, which would be a statmList i guess.
        then we return the object.
        */
        Log.enterParser("<func decl>");
        FuncDecl fd = new FuncDecl(Scanner.curName); // start with creating a new object
        fd.typeSpec = ts;
        Scanner.skip(nameToken);
        Scanner.skip(leftParToken);
        fd.funcParams = ParamDeclList.parse();
        Scanner.skip(rightParToken);
        Log.enterParser("<func body>");
        Scanner.skip(leftCurlToken);
        fd.funcVars = LocalDeclList.parse();
        fd.funcBody = StatmList.parse();
        Log.leaveParser("</func body>");
        Log.leaveParser("</func delc>");

        return fd;
	}

	@Override
	void printTree() {
		// -- Must be changed in part 1:
        typeSpec.printTree();
        Log.wTree(" " +name);
        Log.wTree("(");
        funcParams.printTree();
        Log.wTree(")");
        Log.wTreeLn("{");
        Log.indentTree();
        funcVars.printTree();
        funcBody.printTree();
        Log.wTreeLn();
        Log.outdentTree();
        Log.wTreeLn("}");
	}
}

/*
 * A <statm list>.
 */
class StatmList extends SyntaxUnit {
	// -- Must be changed in part 1:
    Statement first = null;


	@Override
	void check(DeclList curDecls) {
		// -- Must be changed in part 2:
        if(first != null){
            Statement s = first;
            s.check(curDecls);
            while(s.nextStatm != null){ // sjekker alle statements i lista
                s = s.nextStatm;
                s.check(curDecls);
            }
        }

	}

	@Override
	void genCode(FuncDecl curFunc) {
		// -- Must be changed in part 2:
		if(first != null){
			Statement s = first;
			s.genCode(curFunc);
			while(s.nextStatm != null){
				s = s.nextStatm;
				s.genCode(curFunc);
			}

		}
	}

	static StatmList parse() {
		Log.enterParser("<statm list>");

		StatmList sl = new StatmList();

		while (Scanner.curToken != rightCurlToken) {
			// -- Must be changed in part 1:
            if(sl.first == null){
                sl.first = Statement.parse();
            }
            else{
                Statement lastStatm = null;
                lastStatm = sl.first;
                while(lastStatm.nextStatm != null){
                    lastStatm = lastStatm.nextStatm;
                }
                lastStatm.nextStatm = Statement.parse();
            }
		}
        Scanner.skip(rightCurlToken);
		Log.leaveParser("</statm list>");
		return sl;
	}

	@Override
	void printTree() {
		// -- Must be changed in part 1:
        Statement currStatm = first;
        while(currStatm != null){
            currStatm.printTree();
            currStatm = currStatm.nextStatm;
        }
	}
}

/*
 * A <statement>.
 */
abstract class Statement extends SyntaxUnit {
	Statement nextStatm = null;

	static Statement parse() {
		Log.enterParser("<statement>");

		Statement s = null;
		if (Scanner.curToken == nameToken && Scanner.nextToken == leftParToken) {
			// -- Must be changed in part 1:
            s = CallStatm.parse();
		} else if (Scanner.curToken == nameToken
				|| Scanner.curToken == starToken) {
			// -- Must be changed in part 1:
            s = AssignStatm.parse();
		} else if (Scanner.curToken == forToken) {
			// -- Must be changed in part 1:
            s = ForStatm.parse();
		} else if (Scanner.curToken == ifToken) {
			s = IfStatm.parse();
		} else if (Scanner.curToken == returnToken) {
			// -- Must be changed in part 1:
            s = ReturnStatm.parse();
		} else if (Scanner.curToken == whileToken) {
			s = WhileStatm.parse();
		} else if (Scanner.curToken == semicolonToken) {
			s = EmptyStatm.parse();
		} else {
			Error.expected("A statement");
		}

		Log.leaveParser("</statement>");
		return s;
	}
}


/*
 * An <call-statm>.
 */
class CallStatm extends Statement {
    // -- Must be changed in part 1+2:
    FunctionCall fc;

    @Override
    void check(DeclList curDecls) {
        // -- Must be changed in part 2:
        fc.check(curDecls);
    }

    @Override
    void genCode(FuncDecl curFunc) {
        // -- Must be changed in part 2:
        fc.genCode(curFunc);
    }

    static CallStatm parse() {
        // -- Must be changed in part 1:
        Log.enterParser("<call-statm>");
        CallStatm cs = new CallStatm();
        cs.fc = FunctionCall.parse();
        Scanner.skip(semicolonToken);
        Log.leaveParser("</call-statm>");
        return cs;
    }

    @Override
    void printTree() {
        // -- Must be changed in part 1:
        fc.printTree();
        Log.wTreeLn(";");
    }
}

/*
 * An <empty statm>.
 */
class EmptyStatm extends Statement {
	// -- Must be changed in part 1+2:

	@Override
	void check(DeclList curDecls) {
		// -- Must be changed in part 2:
	}

	@Override
	void genCode(FuncDecl curFunc) {
		// -- Must be changed in part 2:

	}

	static EmptyStatm parse() {
		// -- Must be changed in part 1:
		EmptyStatm es = new EmptyStatm();
        Log.enterParser("<empty-statm>");
        Scanner.skip(semicolonToken);
        Log.leaveParser("</empty-statm>");
        return es;
	}


	@Override
	void printTree() {
		// -- Must be changed in part 1:
        Log.wTreeLn(";");
	}
}

/*
 * A <for-statm>.
 */
class ForStatm extends Statement {
    // -- Must be changed in part 1+2:
    ForControl control;
    StatmList statmlist;

    @Override
    void check(DeclList curDecls) {
        // -- Must be changed in part 2:
        control.check(curDecls);
        statmlist.check(curDecls);
    }

    @Override
    void genCode(FuncDecl curFunc) {
        // -- Must be changed in part 2:
        // Sender med statm list til forControl
        // Saa slipper man control.first.gencode(), naar
        // man kan heller bare skrive first.gencode :P
        control.statmlist = statmlist;
        control.genCode(curFunc);
    }

    static ForStatm parse() {
        // -- Must be changed in part 1:
        Log.enterParser("<for-statm>");

        ForStatm fs = new ForStatm();
        Scanner.skip(forToken);
        Scanner.skip(leftParToken);
        fs.control = ForControl.parse();
        Scanner.skip(rightParToken);
        Scanner.skip(leftCurlToken);
        fs.statmlist = StatmList.parse();
        //Scanner.skip(rightCurlToken);

        Log.leaveParser("</for-statm>");
        return fs;
    }
    @Override
    void printTree() {
        // -- Must be changed in part 1:
        Log.wTree("for (");
        control.printTree();
        Log.wTreeLn(") {");
        Log.indentTree();
        statmlist.printTree();
        Log.outdentTree();
        Log.wTreeLn("}");
    }
}


/*
 * a <for-control>
 */
class ForControl extends Statement {
    // -- Must be changed in part 1+2:
    Expression e;
    Assignment first, second;
    StatmList statmlist = null;


    @Override
    void check(DeclList curDecls) {
        // -- Must be changed in part 2:
        first.check(curDecls);
        second.check(curDecls);
        e.check(curDecls);
    }

    @Override
    void genCode(FuncDecl curFunc) {
        // -- Must be changed in part 2:
		String testLabel = Code.getLocalLabel(), endLabel = Code
				.getLocalLabel();

		Code.genInstr("", "", "", "Start for-statement");
		first.genCode(curFunc);
		Code.genInstr(testLabel, "", "", "");
		e.genCode(curFunc);
		Code.genInstr("", "cmpl", "$0,%eax", "");
		Code.genInstr("", "je", endLabel, "");
		statmlist.genCode(curFunc);
		second.genCode(curFunc);
		Code.genInstr("", "jmp", testLabel, "");
		Code.genInstr(endLabel, "", "", "End for-statement");
    }

    static ForControl parse() {
        // -- Must be changed in part 1:
        ForControl fc = new ForControl();
        // Usikker om dette trengs? Greit aa dobbeltsjekke kanskje?
        if(Scanner.curToken == nameToken
                || Scanner.curToken == starToken){
            fc.first = Assignment.parse();
        }

        Scanner.skip(semicolonToken);
        fc.e = Expression.parse();
        Scanner.skip(semicolonToken);

        if(Scanner.curToken == nameToken
                || Scanner.curToken == starToken){
            fc.second = Assignment.parse();
        }

        return fc;
    }

    @Override
    void printTree() {
        // -- Must be changed in part 1:
        first.printTree();
        Log.wTree(";");
        e.printTree();
        Log.wTree("}");
        second.printTree();
    }
}

/*
 * An <if-statm>.
 */
class IfStatm extends Statement {
	// -- Must be changed in part 1+2:
    Expression e;
    StatmList ifList;
    StatmList elseList;


	@Override
	void check(DeclList curDecls) {
		// -- Must be changed in part 2:
        e.check(curDecls);
        ifList.check(curDecls);
        if(elseList != null){elseList.check(curDecls);}
	}

	@Override
	void genCode(FuncDecl curFunc) {
		// -- Must be changed in part 2:
        String endLabel = Code.getLocalLabel(),
               elseLabel = "";
        if(elseList != null){elseLabel = Code.getLocalLabel();}

        Code.genInstr("", "","", "Start if-statement");
        e.genCode(curFunc);
        Code.genInstr("", "cmpl", "$0,%eax", "");
        Code.genInstr("", "je", (elseList != null) ? elseLabel : endLabel, "");
        ifList.genCode(curFunc);
        if(elseList != null){
            Code.genInstr("", "jmp", endLabel, "");
            Code.genInstr(elseLabel, "", "", "  else-part");
            elseList.genCode(curFunc);
        }
        Code.genInstr(endLabel, "", "", "End if-statement");
	}

	static IfStatm parse() {
		// -- Must be changed in part 1:
		Log.enterParser("<if-statm>");

        IfStatm is = new IfStatm();
        Scanner.skip(ifToken);
        Scanner.skip(leftParToken);
        is.e = Expression.parse();
        Scanner.skip(rightParToken);
        Scanner.skip(leftCurlToken);
        is.ifList = StatmList.parse();
        //Scanner.skip(rightCurlToken);

        if(Scanner.curToken == Token.elseToken){
            Log.enterParser("<else-part>");
            Scanner.skip(elseToken);
            Scanner.skip(leftCurlToken);
            is.elseList = StatmList.parse();
            //Scanner.skip(rightCurlToken);
            Log.leaveParser("</else-part>");
        }
        Log.leaveParser("</if-statm>");
        return is;

	}


	@Override
	void printTree() {
		// -- Must be changed in part 1:
        Log.wTree("if (");
        e.printTree();
        Log.wTreeLn(") {");
        Log.indentTree();
        ifList.printTree();
        Log.wTreeLn("}");
        Log.outdentTree();
        if(elseList != null) {
            Log.wTreeLn("else {");
            Log.indentTree();
            elseList.printTree();
            Log.wTreeLn();
            Log.wTreeLn("}");
            Log.outdentTree();
        }

	}
}

/*
 * A <return-statm>.
 */
class ReturnStatm extends Statement{
    // -- mUST BE CHANGED IN PART 1+2
    Expression e;

    @Override
    void check(DeclList curDecls) {
        e.check(curDecls);
        // -- Must be changed in part 2:

    }

    @Override
    void genCode(FuncDecl curFunc) {
        // -- Must be changed in part 2:
        System.out.println(""+curFunc.assemblerName);
        e.genCode(curFunc);
        Code.genInstr("", "jmp", ".exit$"+curFunc.name, "Return-statement");
    }

    static ReturnStatm parse() {
        // -- Must be changed in part 1:
        Log.enterParser("<return-statm>");

        ReturnStatm rs = new ReturnStatm();
        Scanner.skip(returnToken);
        rs.e = Expression.parse();
        Scanner.skip(semicolonToken);
        Log.leaveParser("</return-statm>");
        return rs;
    }

    @Override
    void printTree() {
        // -- Must be changed in part 1:
        Log.wTree("return ");
        e.printTree();
        Log.wTree(";");
    }
}

/*
 * A <while-statm>.
 */
class WhileStatm extends Statement {
	Expression test;
	StatmList body;

	@Override
	void check(DeclList curDecls) {
		test.check(curDecls);
		body.check(curDecls);

		Log.noteTypeCheck("while (t) ...", test.type, "t", lineNum);
		if (test.type instanceof ValueType) {
			// OK
		} else {
			error("While-test must be a value.");
		}
	}

	@Override
	void genCode(FuncDecl curFunc) {
		String testLabel = Code.getLocalLabel(), endLabel = Code
				.getLocalLabel();

		Code.genInstr(testLabel, "", "", "Start while-statement");
		test.genCode(curFunc);
		Code.genInstr("", "cmpl", "$0,%eax", "");
		Code.genInstr("", "je", endLabel, "");
		body.genCode(curFunc);
		Code.genInstr("", "jmp", testLabel, "");
		Code.genInstr(endLabel, "", "", "End while-statement");
	}

	static WhileStatm parse() {
		Log.enterParser("<while-statm>");

		WhileStatm ws = new WhileStatm();
		Scanner.skip(whileToken);
		Scanner.skip(leftParToken);
		ws.test = Expression.parse();
		Scanner.skip(rightParToken);
		Scanner.skip(leftCurlToken);
		ws.body = StatmList.parse();
		//Scanner.skip(rightCurlToken);

		Log.leaveParser("</while-statm>");
		return ws;
	}

	@Override
	void printTree() {
		Log.wTree("while (");
		test.printTree();
		Log.wTreeLn(") {");
		Log.indentTree();
		body.printTree();
		Log.outdentTree();
		Log.wTreeLn("}");
	}
}

/*
 * An <Lhs-variable>
 */

class LhsVariable extends SyntaxUnit {
	int numStars = 0;
	Variable var;
	Type type;

	@Override
	void check(DeclList curDecls) {
		var.check(curDecls);
		type = var.type;
		for (int i = 1; i <= numStars; ++i) {
			Type e = type.getElemType();
			if (e == null)
				error("Type error in left-hand side variable!");
			type = e;
		}
	}

	@Override
	void genCode(FuncDecl curFunc) {
		var.genAddressCode(curFunc);
		for (int i = 1; i <= numStars; ++i)
			Code.genInstr("", "movl", "(%eax),%eax", "  *");
	}

	static LhsVariable parse() {
		Log.enterParser("<lhs-variable>");

		LhsVariable lhs = new LhsVariable();
		while (Scanner.curToken == starToken) {
			++lhs.numStars;
			Scanner.skip(starToken);
		}
		Scanner.check(nameToken);
		lhs.var = Variable.parse();

		Log.leaveParser("</lhs-variable>");
		return lhs;
	}

	@Override
	void printTree() {
		for (int i = 1; i <= numStars; ++i)
			Log.wTree("*");
		var.printTree();
	}
}
/*
 * An <assign-statm>
 */
class AssignStatm extends Statement{
    // -- mUST BE CHANGED IN PART 1+2
    Assignment a;

    @Override
    void check(DeclList curDecls) {
        // -- Must be changed in part 2:
        a.check(curDecls);
    }

    @Override
    void genCode(FuncDecl curFunc) {
        // -- Must be changed in part 2:
        a.genCode(curFunc);
    }

    static AssignStatm parse() {
        // -- Must be changed in part 1:
        Log.enterParser("<assign-statm>");
        AssignStatm as = new AssignStatm();
        as.a = Assignment.parse();
        Scanner.skip(semicolonToken);
        Log.leaveParser("</assign-statm>");
        return as;
    }

    @Override
    void printTree() {
        // -- Must be changed in part 1:
        a.printTree();
        Log.wTreeLn(";");
    }
}

/*
 * An <assignment>
 */
class Assignment extends Statement{
    // -- mUST BE CHANGED IN PART 1+2
    Expression e;
    LhsVariable lhs;

    @Override
    void check(DeclList curDecls) {
        // -- Must be changed in part 2:
        lhs.check(curDecls);
        e.check(curDecls);
    }

    @Override
    void genCode(FuncDecl curFunc) {
        // -- Must be changed in part 2:
        lhs.genCode(curFunc);
        Code.genInstr("", "pushl", "%eax", "");
        e.genCode(curFunc);
        Code.genInstr("", "popl", "%edx", "");
        Code.genInstr("", "movl", "%eax,(%edx)", "  =");
    }

    static Assignment parse() {
        // -- Must be changed in part 1:
        Log.enterParser("<assignment>");

        Assignment ass = new Assignment();
        ass.lhs = LhsVariable.parse();
        Scanner.skip(assignToken);
        ass.e = Expression.parse();

        Log.leaveParser("</assignment>");
        return ass;
    }

    @Override
    void printTree() {
        // -- Must be changed in part 1:
        lhs.printTree();
        Log.wTree("=");
        e.printTree();
    }
}

/*
 * An <expression list>.
 */

class ExprList extends SyntaxUnit {
	Expression firstExpr = null;
    static int length = 0;

	@Override
	void check(DeclList curDecls) {
		// -- Must be changed in part 2:
        Expression e = firstExpr;

        while(e != null){ // sjekker alle exprs i exprlisten
            e.check(curDecls);
            e = e.nextExpr;
        }
	}

	@Override
	void genCode(FuncDecl curFunc) {
		 //-- Must be changed in part 2:
         Expression curr = firstExpr;
         // while(curr != null){
         //     curr.genCodeRec(curFunc, 0);
         //     curr = curr.nextExpr;
         // }
        if (curr != null) curr.genCodeRec(curFunc, 0);
		/*
        if(firstExpr != null) {
			Expression curr = firstExpr;
			if(curr.nextExpr != null) {
				curr.nextExpr.genCode(curFunc);
            	Code.genInstr("", "pushl", "%eax", "Push parameter #"+(++length));
				curr = curr.nextExpr;
			}
            curr.genCode(curFunc);
            Code.genInstr("", "pushl", "%eax", "Push parameter #"+(++length));
		}*/
	}

	static ExprList parse() {
		Expression lastExpr = null;

		Log.enterParser("<expr list>");

		// -- Must be changed in part 1:
		ExprList el = new ExprList();
        length = 1;
        if(Scanner.curToken != rightParToken) { // makes sure the firstExpr is null if there's no expressions
            while (Scanner.curToken != rightParToken) {
                if (el.firstExpr == null) {
                    el.firstExpr = Expression.parse();
                } else {
                    Expression prevExpr = el.firstExpr;
                    while (prevExpr.nextExpr != null) {
                        prevExpr = prevExpr.nextExpr;
                    }
                    length++;
                    prevExpr.nextExpr = Expression.parse();
                }
                if (Scanner.curToken != rightParToken) {
                    Scanner.skip(commaToken);
                }
            }
        } else {
        	el.firstExpr = null;
        }
        Scanner.skip(rightParToken);
        Log.leaveParser("</expr list>");
        return el;
	}

	@Override
	void printTree() {
		// -- Must be changed in part 1:

        Expression currExpr = firstExpr;
        while(currExpr != null){
            currExpr.printTree();
            if(currExpr.nextExpr != null){
                Log.wTree(", ");
            }
            currExpr = currExpr.nextExpr;
        }
	}
	// -- Must be changed in part 1:
}

/*
 * An <expression>
 */
class Expression extends SyntaxUnit {
	Expression nextExpr = null;
	Term firstTerm, secondTerm = null;
	Operator relOpr = null;
	Type type = null;


	@Override
	void check(DeclList curDecls) {
		// -- Must be changed in part 2:
        firstTerm.check(curDecls);
        if(secondTerm == null) {
            type = firstTerm.type;
        }
        else{
            secondTerm.check(curDecls);
            if(relOpr.oprToken == equalToken || relOpr.oprToken == notEqualToken){
                if(firstTerm.type instanceof ValueType && secondTerm.type instanceof ValueType &&
                        (firstTerm.type.isSameType(secondTerm.type) || firstTerm.type == Types.intType || secondTerm.type == Types.intType)){
                    type = Types.intType;
                }else{
                    Error.error("Type of x was " + firstTerm.type + " and type for y was " + secondTerm.type + ". Both should have been intType");
                }
            }else if(Token.isRelOperator(relOpr.oprToken)){
                if(firstTerm.type == Types.intType && secondTerm.type == Types.intType){
                    type = Types.intType;
                }else{
                    Error.error("Expected both to be intTypes, x was " + firstTerm.type + " and y was " + secondTerm.type);
                }
            }else{
                Error.error("Not a valid expression");
            }
        }




	}

	@Override
	void genCode(FuncDecl curFunc) {
		// -- Must be changed in part 2:
		// Assignment already redcued away pointer values, so we're left with the actual value
        firstTerm.genCode(curFunc);
        if(secondTerm != null){
            Code.genInstr("", "pushl", "%eax", ""); // mellomlagrer first term paa stacken
            secondTerm.genCode(curFunc); // execute second term
            relOpr.genCode(curFunc); // naar denne kjores ligger term2 i eax og term1 paa toppen av stack
        }

	}

	void genCodeRec(FuncDecl curFunc, int paramN){
		if(nextExpr != null){
			nextExpr.genCodeRec(curFunc, paramN++);
		}
		genCode(curFunc);
		paramN++;
		Code.genInstr("", "pushl", "%eax", "Push parameter #"+paramN);

	}

	static Expression parse() {
		Log.enterParser("<expression>");

		Expression e = new Expression();
		e.firstTerm = Term.parse();
		if (Token.isRelOperator(Scanner.curToken)) {
			e.relOpr = RelOpr.parse();
			e.secondTerm = Term.parse();
		}
		Log.leaveParser("</expression>");
		return e;
	}

	@Override
	void printTree() {
		// -- Must be changed in part 1:
        firstTerm.printTree();
        if(relOpr != null){
            relOpr.printTree();
        }
        if(secondTerm != null){
            secondTerm.printTree();
        }
	}
}

/*
 * A <term>
 */
class Term extends SyntaxUnit {
	// -- Must be changed in part 1+2:
    Factor first;
    Term second;
    TermOpr termOpr;
    Type type;

	@Override
	void check(DeclList curDecls) {
		// -- Must be changed in part 2:
        first.check(curDecls);
        if(second == null){
            type = first.type;
        }else{
            second.check(curDecls);
            if(first.type == Types.intType && second.type == Types.intType){
                type = first.type;
            } else{
                Error.error("Not declared as intType, was declared as "  + first.type);
            }
        }
    }

	@Override
	void genCode(FuncDecl curFunc) {
		// -- Must be changed in part 2:
        // if (first != null){			
        first.genCode(curFunc);

        // }
        //second.genCode(curFunc);
        if(second != null) {
            Code.genInstr("", "pushl", "%eax", ""); // mellomlagrer paa stacken
        	second.genCode(curFunc);
            termOpr.genCode(curFunc);
        }
	}

	static Term parse() {
		// -- Must be changed in part 1:
		Log.enterParser("<term>");
        Term t = new Term();
        t.first = Factor.parse();
        if(Token.isTermOperator(Scanner.curToken)) {
            t.termOpr = TermOpr.parse();
            t.second = Term.parse();
        }
        Log.leaveParser("</term>");
        return t;
	}

	@Override
	void printTree() {
		// -- Must be changed in part 1:
        first.printTree();
        if(termOpr != null){
            termOpr.printTree();
        }
        if(second != null){
            second.printTree();
        }
	}
}

/*
 * A <factor>
 */
class Factor extends SyntaxUnit {
    // -- Must be changed in part 1+2:
    FactorOpr factorOpr;
    Primary first;
    Factor second;
    Type type;


    @Override
    void check(DeclList curDecls) {
        // -- Must be changed in part 2:
        first.check(curDecls);
        if(second == null){
            type = first.type;
        }else{
            second.check(curDecls);
            if(first.type == Types.intType && second.type == Types.intType){
                type = Types.intType;
            }else{
                Error.error("Not declared as intType, was declared as "  + first.type);
            }
        }
    }

    @Override
    void genCode(FuncDecl curFunc) {
        // -- Must be changed in part 2:

		first.genCode(curFunc);
        if(second != null) {
        	Code.genInstr("", "pushl", "%eax", ""); // mellomlagrer first paa stacken
       		second.genCode(curFunc);       		
            factorOpr.genCode(curFunc);
        }

    }

    static Factor parse() {
        // -- Must be changed in part 1:
        Log.enterParser("<factor>");
        Factor f = new Factor();
        f.first = Primary.parse();
        if(Token.isFactorOperator(Scanner.curToken)){
            f.factorOpr = FactorOpr.parse();
            f.second = Factor.parse();
        }

        Log.leaveParser("</factor>");
        return f;
    }

    @Override
    void printTree() {
        // -- Must be changed in part 1:
        first.printTree();
        if(factorOpr != null){
            factorOpr.printTree();
        }
        if(second != null){
            second.printTree();
        }
    }
}


/*
 * A <primary>
 */
class Primary extends SyntaxUnit {
    // -- Must be changed in part 1+2:
    Operand first;
    PrefixOpr prefix;
    Type type;



    @Override
    void check(DeclList curDecls) {
        // -- Must be changed in part 2:
        first.check(curDecls);

        if(prefix == null){
            // System.out.println("hei1");
            type = first.type;
        }
        else if(prefix.oprToken == starToken){
            if(first.type instanceof PointerType)
                type = first.type.getElemType();
            else{
                Error.error("Expected pointerType, got " + first.type);
            }
        }else if(prefix.oprToken == subtractToken){
            if(first.type == Types.intType){
                type = Types.intType;
            }else{
                Error.error("Expected intType, got " + first.type);
            }
        }

    }

    @Override
    void genCode(FuncDecl curFunc) {
        // -- Must be changed in part 2:
        first.genCode(curFunc);
		System.out.println(first.lineNum + " " + first);
        if(prefix != null) {
        	// Code.genInstr("", "pushl", "%eax", "");
        	prefix.genCode(curFunc);
        }
        
    }

    static Primary parse() {
        // -- Must be changed in part 1:
        Log.enterParser("<primary>");
        Primary p = new Primary();
        // System.out.println(Scanner.curToken);
        if(Token.isPrefixOperator(Scanner.curToken)){
            p.prefix = PrefixOpr.parse();
        }
        p.first = Operand.parse();
        // System.out.println(p.first.lineNum +"");

        Log.leaveParser("</primary>");
        return p;
    }

    @Override
    void printTree() {
        // -- Must be changed in part 1:
        if(prefix != null){
            prefix.printTree();
        }
        first.printTree();
    }
}



/*
 * An <operator>
 */
abstract class Operator extends SyntaxUnit {
	Operator nextOpr = null;
	Token oprToken;

	@Override
	void check(DeclList curDecls) {
	} // Never needed.
}

/*
 * A <term opr> (+ or -)
 */
class TermOpr extends Operator{


    @Override
    void genCode(FuncDecl curFunc){
        // PART 2
        Code.genInstr("", "movl", "%eax,%ecx", "");
        Code.genInstr("", "popl", "%eax", "");
        if(oprToken == addToken) {Code.genInstr("", "addl", "%ecx,%eax", "Compute +");}
        else if(oprToken == subtractToken){Code.genInstr("", "subl", "%ecx,%eax", "Compute -");}
    }

    static TermOpr parse(){
        // PART 1
        Log.enterParser("<term opr>");
        TermOpr to = new TermOpr();
        to.oprToken = Scanner.curToken;
        Scanner.readNext();
        Log.leaveParser("</term opr>");
        return to;
    }

    @Override
    void printTree(){
        // MUST BE CHANGED IN PART 1
        if(oprToken == addToken){Log.wTree(" + ");}
        else if(oprToken == subtractToken){Log.wTree(" - ");}
    }
}

/*
 * A <factor opr> (* or /)
 */
class FactorOpr extends Operator{


    @Override
    void genCode(FuncDecl curFunc){
        // PART 2
        Code.genInstr("", "movl", "%eax,%ecx", "");
        Code.genInstr("", "popl", "%eax", "");
        if(oprToken == divideToken){
            Code.genInstr("", "cdq", "", "");
            Code.genInstr("", "idivl", "%ecx", "Compute /");
        }
        else if(oprToken == starToken){Code.genInstr("", "imull", "%ecx,%eax", "Compute *");}
    }

    static FactorOpr parse(){
        // PART 1
        Log.enterParser("<factor opr>");
        FactorOpr fo = new FactorOpr();
        fo.oprToken = Scanner.curToken;
        Scanner.readNext();
        Log.leaveParser("</factor opr>");
        return fo;
    }

    @Override
    void printTree(){
        // MUST BE CHANGED IN PART 1
        if(oprToken == starToken){Log.wTree(" * ");}
        else if(oprToken == divideToken){Log.wTree(" / ");}
    }
}

/*
 * A <prefix opr> (- or *)
 */
class PrefixOpr extends Operator{


    @Override
    void genCode(FuncDecl curFunc){
        // PART 2
        if(oprToken == subtractToken){Code.genInstr("", "negl", "%eax", " negative nr");}
        else if(oprToken == starToken){Code.genInstr("", "movl", "(%eax),%eax", "peker");}
    }

    static PrefixOpr parse(){
        // PART 1
        Log.enterParser("<prefix opr>");
        PrefixOpr po = new PrefixOpr();
        po.oprToken = Scanner.curToken;
        Scanner.readNext();
        Log.leaveParser("</prefix opr>");
        return po;
    }

    @Override
    void printTree(){
        // MUST BE CHANGED IN PART 1
        if(oprToken == subtractToken){Log.wTree("-");}
        else if(oprToken == starToken){Log.wTree("*");}
    }
}


/*
 * A <rel opr> (==, !=, <, <=, > or >=).
 */

class RelOpr extends Operator {
	@Override
	void genCode(FuncDecl curFunc) {
		Code.genInstr("", "popl", "%ecx", "");
		Code.genInstr("", "cmpl", "%eax,%ecx", "");
		Code.genInstr("", "movl", "$0,%eax", "");
		switch (oprToken) {
		case equalToken:
			Code.genInstr("", "sete", "%al", "Test ==");
			break;
		case notEqualToken:
			Code.genInstr("", "setne", "%al", "Test !=");
			break;
		case lessToken:
			Code.genInstr("", "setl", "%al", "Test <");
			break;
		case lessEqualToken:
			Code.genInstr("", "setle", "%al", "Test <=");
			break;
		case greaterToken:
			Code.genInstr("", "setg", "%al", "Test >");
			break;
		case greaterEqualToken:
			Code.genInstr("", "setge", "%al", "Test >=");
			break;
		}
	}

	static RelOpr parse() {
		Log.enterParser("<rel opr>");

		RelOpr ro = new RelOpr();
		ro.oprToken = Scanner.curToken;
		Scanner.readNext();

		Log.leaveParser("</rel opr>");
		return ro;
	}

	@Override
	void printTree() {
		String op = "?";
		switch (oprToken) {
		case equalToken:
			op = "==";
			break;
		case notEqualToken:
			op = "!=";
			break;
		case lessToken:
			op = "<";
			break;
		case lessEqualToken:
			op = "<=";
			break;
		case greaterToken:
			op = ">";
			break;
		case greaterEqualToken:
			op = ">=";
			break;
		}
		Log.wTree(" " + op + " ");
	}
}

/*
 * An <operand>
 */
abstract class Operand extends SyntaxUnit {
	Operand nextOperand = null;
	Type type;

	static Operand parse() {
		Log.enterParser("<operand>");

		Operand o = null;
		if (Scanner.curToken == numberToken) {
			o = Number.parse();
		} else if (Scanner.curToken == nameToken
				&& Scanner.nextToken == leftParToken) {
			o = FunctionCall.parse();
		} else if (Scanner.curToken == nameToken) {
			o = Variable.parse();
		} else if (Scanner.curToken == ampToken) {
			o = Address.parse();
		} else if (Scanner.curToken == leftParToken) {
			o = InnerExpr.parse();
		} else {
			Error.expected("An operand");
		}

		Log.leaveParser("</operand>");
		return o;
	}
}

/*
 * A <function call>.
 */
class FunctionCall extends Operand {
	// -- Must be changed in part 1+2:
    String funcName;
    ExprList el;
    FuncDecl declRef = null;



	@Override
	void check(DeclList curDecls) {
		// -- Must be changed in part 2:

        if(el == null){
            Error.error("ExprList is null");
        }

        Declaration d = curDecls.findDecl(funcName, this);
        Log.noteBinding(funcName, lineNum, d.lineNum);
 

        //d.checkWhetherFunction(el.length, this);
        type = d.type;
        declRef = (FuncDecl) d;


       //Check params
        el.check(curDecls);


        // check func
        Declaration declTmp = declRef.funcParams.firstDecl;
        Expression callTmp = el.firstExpr;
        while(declTmp != null && callTmp != null){
            if(declTmp == null || callTmp == null){
                Error.error("FunctionDecl and callDecl parameterlist are not equal length");
            }

            //if(!declTmp.type.isSameType(callTmp.type) || !callTmp.type.isSameType(Types.intType)) {
              //  Error.error("Function call parameter type not equal to function declaration type or int is " + callTmp.type + " " +callTmp.lineNum);
            //}

            declTmp = declTmp.nextDecl;
            callTmp = callTmp.nextExpr;
        }

	}

	@Override
	void genCode(FuncDecl curFunc) {
		// -- Must be changed in part 2:
        el.genCode(curFunc);
        el.length = 0;

        FuncDecl f = declRef;

        Code.genInstr("", "call", f.assemblerName, "Call "+f.name);

        if(f.funcParams.dataSize()>0){
	        Code.genInstr("", "addl", "$"+f.funcParams.dataSize()+",%esp", "Remove parameters");
        }


	}

	static FunctionCall parse() {
		// -- Must be changed in part 1:
		Log.enterParser("<function call>");

        FunctionCall fc = new FunctionCall();
        fc.funcName = Scanner.curName;
        Scanner.skip(nameToken);
        Scanner.skip(leftParToken);
        fc.el = ExprList.parse();

        Log.leaveParser("</function call>");
	    return fc;
    }

	@Override
	void printTree() {
		// -- Must be changed in part 1:
        Log.wTree(funcName);
        Log.wTree("(");
        el.printTree();
        Log.wTree(")");
	}
	// -- Must be changed in part 1+2:
}

/*
 * A <number>.
 */
class Number extends Operand {
	int numVal;

	@Override
	void check(DeclList curDecls) {
		// -- Must be changed in part 2:
        type = Types.intType;

	}

	@Override
	void genCode(FuncDecl curFunc) {
		Code.genInstr("", "movl", "$" + numVal + ",%eax","" + numVal);
	}

	static Number parse() {
		// -- Must be changed in part 1:
        Log.enterParser("<number>");
        Number n = new Number();
        n.numVal = Scanner.curNum;
        Scanner.skip(numberToken);
        Log.leaveParser("</number>");
		return n;
	}

	@Override
	void printTree() {
		Log.wTree("" + numVal);
	}
}

/*
 * A <variable>.
 */

class Variable extends Operand {
	String varName;
	VarDecl declRef = null;
	Expression index = null;

	@Override
	void check(DeclList curDecls) {
		Declaration d = curDecls.findDecl(varName, this);
        Log.noteBinding(varName, lineNum, d.lineNum);

		d.checkWhetherVariable(this);
		declRef = (VarDecl) d;

		if (index == null) {
			type = d.type;
		} else {
			index.check(curDecls);
			Log.noteTypeCheck("a[e]", d.type, "a", index.type, "e", lineNum);
			if (index.type == Types.intType) {
				// OK
			} else {
				error("Only integers may be used as index.");
			}
			if (d.type.mayBeIndexed()) {
				// OK
			} else {
				error("Only arrays and pointers may be indexed.");
			}
			type = d.type.getElemType();
		}
	}

	@Override
	void genCode(FuncDecl curFunc) {
        // -- Must be changed in part  2:

        if (index == null) {
			System.out.println(varName + " " + declRef.name + " " + declRef.assemblerName);
            Code.genInstr("", "movl", declRef.assemblerName + ",%eax", varName);
        } else {
            index.genCode(curFunc);
            if (declRef.type instanceof ArrayType) {
                Code.genInstr("", "leal", declRef.assemblerName + ",%edx",
                        varName + "[...]");
            } else {
                Code.genInstr("", "movl", declRef.assemblerName + ",%edx",
                        varName + "[...]");
            }
            Code.genInstr("", "movl", "(%edx, %eax, 4), %eax", "");
        }
    }



	void genAddressCode(FuncDecl curFunc) {
		// Generate code to load the _address_ of the variable
		// rather than its value.

		if (index == null) {
			Code.genInstr("", "leal", declRef.assemblerName + ",%eax", varName);
		} else {
			index.genCode(curFunc);
			if (declRef.type instanceof ArrayType) {
				Code.genInstr("", "leal", declRef.assemblerName + ",%edx",
						varName + "[...]");
			} else {
				Code.genInstr("", "movl", declRef.assemblerName + ",%edx",
						varName + "[...]");
			}
			Code.genInstr("", "leal", "(%edx,%eax,4),%eax", "");
		}
	}

	static Variable parse() {
		Log.enterParser("<variable>");
		// -- Must be changed in part 1:
        Variable v = new Variable();
        v.varName = Scanner.curName;
        Scanner.skip(nameToken);

        if(Scanner.curToken == leftBracketToken){
            Scanner.skip(leftBracketToken);
            v.index = Expression.parse();
            Scanner.skip(rightBracketToken);
        }
        Log.leaveParser("</variable>");
		return v;
	}

	@Override
	void printTree() {
		// -- Must be changed in part 1:
        Log.wTree(varName);
        if(index != null){
            Log.wTree("[");
            index.printTree();
            Log.wTree("]");
        }


	}
}

/*
 * An <address>.
 */
class Address extends Operand {
	Variable var;

	@Override
	void check(DeclList curDecls) {
		var.check(curDecls);
		type = new PointerType(var.type);
	}

	@Override
	void genCode(FuncDecl curFunc) {
		var.genAddressCode(curFunc);
	}

	static Address parse() {
		Log.enterParser("<address>");

		Address a = new Address();
		Scanner.skip(ampToken);
		a.var = Variable.parse();

		Log.leaveParser("</address>");
		return a;
	}

	@Override
	void printTree() {
		Log.wTree("&");
		var.printTree();
	}
}

/*
 * An <inner expr>.
 */
class InnerExpr extends Operand {
	Expression expr;

	@Override
	void check(DeclList curDecls) {
		expr.check(curDecls);
		type = expr.type;
	}

	@Override
	void genCode(FuncDecl curFunc) {
		expr.genCode(curFunc);
	}

	static InnerExpr parse() {
		Log.enterParser("<inner expr>");

		InnerExpr ie = new InnerExpr();
		Scanner.skip(leftParToken);
		ie.expr = Expression.parse();
		Scanner.skip(rightParToken);

		Log.leaveParser("</inner expr>");
		return ie;
	}

	@Override
	void printTree() {
		Log.wTree("(");
		expr.printTree();
		Log.wTree(")");
	}
}
