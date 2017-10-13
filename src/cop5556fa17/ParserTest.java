package cop5556fa17;

import static org.junit.Assert.*;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import cop5556fa17.Scanner.LexicalException;
import cop5556fa17.AST.*;

import cop5556fa17.Parser.SyntaxException;

import static cop5556fa17.Scanner.Kind.*;

public class ParserTest {

	// set Junit to be able to catch exceptions
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	// To make it easy to print objects and turn this output on and off
	static final boolean doPrint = true;
	private void show(Object input) {
		if (doPrint) {
			System.out.println(input.toString());
		}
	}

	/**
	 * Simple test case with an empty program. This test expects an exception
	 * because all legal programs must have at least an identifier
	 * 
	 * @throws LexicalException
	 * @throws SyntaxException
	 */
	@Test
	public void testEmpty() throws LexicalException, SyntaxException {
		String input = ""; // The input is the empty string. Parsing should fail
		show(input); // Display the input
		Scanner scanner = new Scanner(input).scan(); // Create a Scanner and
														// initialize it
		show(scanner); // Display the tokens
		Parser parser = new Parser(scanner); //Create a parser
		thrown.expect(SyntaxException.class);
		try {
			ASTNode ast = parser.parse();; //Parse the program, which should throw an exception
		} catch (SyntaxException e) {
			show(e);  //catch the exception and show it
			throw e;  //rethrow for Junit
		}
	}


	@Test
	public void testNameOnly() throws LexicalException, SyntaxException {
		String input = "prog";  //Legal program with only a name
		show(input);            //display input
		Scanner scanner = new Scanner(input).scan();   //Create scanner and create token list
		show(scanner);    //display the tokens
		Parser parser = new Parser(scanner);   //create parser
		Program ast = parser.parse();          //parse program and get AST
		show(ast);                             //Display the AST
		assertEquals(ast.name, "prog");        //Check the name field in the Program object
		assertTrue(ast.decsAndStatements.isEmpty());   //Check the decsAndStatements list in the Program object.  It should be empty.
	}

	@Test
	public void testDec1() throws LexicalException, SyntaxException {
		String input = "prog int k;\n"
					 + "int l = 5;";
		show(input);
		Scanner scanner = new Scanner(input).scan(); 
		show(scanner); 
		Parser parser = new Parser(scanner);
		Program ast = parser.parse();
		show(ast);
		assertEquals(ast.name, "prog"); 
		//This should have one Declaration_Variable object, which is at position 0 in the decsAndStatements list
		Declaration_Variable dec1 = (Declaration_Variable) ast.decsAndStatements.get(0);  
		assertEquals(KW_int, dec1.type.kind);
		assertEquals("k", dec1.name);
		assertNull(dec1.e);
		Declaration_Variable dec2 = (Declaration_Variable) ast.decsAndStatements.get(1);
		assertEquals(KW_int, dec2.type.kind);
		assertEquals("l", dec2.name);
		assertEquals("Expression_IntLit",dec2.e.getClass().getSimpleName());
		assertEquals(INTEGER_LITERAL, ((Expression_IntLit)dec2.e).firstToken.kind);
		assertEquals(5, ((Expression_IntLit)dec2.e).value);
	}

	@Test
	public void testDec2() throws LexicalException, SyntaxException {
		String input = "prog image [ A*R/Z-DEF_Y , (ned==alive) ] img2 <- \"img2\";\n";
		show(input);
		Scanner scanner = new Scanner(input).scan(); 
		show(scanner); 
		Parser parser = new Parser(scanner);
		Program ast = parser.parse();
		show(ast);
		assertEquals(ast.name, "prog");
		Declaration_Image dec = (Declaration_Image) ast.decsAndStatements.get(0);
		Expression_Binary xSize = (Expression_Binary)dec.xSize;
		assertEquals(OP_MINUS,xSize.op);
		Expression_Binary ySize = (Expression_Binary)dec.ySize;
		assertEquals(OP_EQ,ySize.op);
		
		input = "prog image [ A*R/Z+DEF_X-DEF_Y , (ned==alive) ] img2 <- x;\n";
		show(input);
		scanner = new Scanner(input).scan(); 
		show(scanner);
		parser = new Parser(scanner); //Create a parser
		thrown.expect(SyntaxException.class);
		try {
			ast = parser.parse();; //Parse the program, which should throw an exception
		} catch (SyntaxException e) {
			show(e);  //catch the exception and show it
			throw e;  //rethrow for Junit
		}
	}
	
	@Test
	public void testDec3() throws LexicalException, SyntaxException {
		String input = "prog image [ A*R/Z+DEF_X-DEF_Y , (ned=alive) ] img2 = x;\n";
		show(input);
		Scanner scanner = new Scanner(input).scan(); 
		show(scanner);
		Parser parser = new Parser(scanner); //Create a parser
		thrown.expect(SyntaxException.class);
		try {
			Program ast = parser.parse();; //Parse the program, which should throw an exception
		} catch (SyntaxException e) {
			show(e);  //catch the exception and show it
			throw e;  //rethrow for Junit
		}
	}
	
	@Test
	public void testDec4() throws LexicalException, SyntaxException {
		String input = "prog image [ A*R/Z+DEF_X-DEF_Y , (ned==alive) ] img2 <- x+y;\n";
		show(input);        							//Display the input 
		Scanner scanner = new Scanner(input).scan();			//Create a Scanner and initialize it
		show(scanner);   								//Display the Scanner
		Parser parser = new Parser(scanner);
		thrown.expect(SyntaxException.class);
		try {
			Program ast = parser.parse();; //Parse the program, which should throw an exception
		} catch (SyntaxException e) {
			show(e);  //catch the exception and show it
			throw e;  //rethrow for Junit
		}
	}
	
	@Test
	public void testDec5() throws LexicalException, SyntaxException {
		String input = "prog url facebook = \"www.facebook.com\";\n"
				     + "file assignment = \"/home/vishal/plp.txt\";\n";
		show(input);
		Scanner scanner = new Scanner(input).scan(); 
		show(scanner); 
		Parser parser = new Parser(scanner);
		Program ast = parser.parse();
		show(ast);
		assertEquals(ast.name, "prog");
		Declaration_SourceSink ss1 = (Declaration_SourceSink) ast.decsAndStatements.get(0);
		assertEquals(KW_url,ss1.type);
		assertEquals("facebook", ss1.name);
		assertEquals("www.facebook.com", ((Source_StringLiteral)ss1.source).fileOrUrl);
		Declaration_SourceSink ss2 = (Declaration_SourceSink) ast.decsAndStatements.get(1);
		assertEquals(KW_file,ss2.type);
		assertEquals("assignment", ss2.name);
		assertEquals("/home/vishal/plp.txt", ((Source_StringLiteral)ss2.source).fileOrUrl);
		
		input = "prog file email = str1+str2;";
		show(input);
		scanner = new Scanner(input).scan(); 
		show(scanner);
		parser = new Parser(scanner); //Create a parser
		thrown.expect(SyntaxException.class);
		try {
			ast = parser.parse();; //Parse the program, which should throw an exception
		} catch (SyntaxException e) {
			show(e);  //catch the exception and show it
			throw e;  //rethrow for Junit
		}
	}
	
	@Test
	public void testStmnt1_imgOut() throws LexicalException, SyntaxException {
		String input = "prog output -> img2show;\n"
					 + "output -> SCREEN;\n";
		show(input);
		Scanner scanner = new Scanner(input).scan(); 
		show(scanner); 
		Parser parser = new Parser(scanner);
		Program ast = parser.parse();
		show(ast);
		assertEquals(ast.name, "prog");
		Statement_Out line1 = (Statement_Out) ast.decsAndStatements.get(0);
		assertEquals("output", line1.name);
		assertEquals("img2show", ((Sink_Ident) line1.sink).name);
		Statement_Out line2 = (Statement_Out) ast.decsAndStatements.get(1);
		assertEquals("output", line2.name);
		assertEquals(KW_SCREEN, ((Sink_SCREEN) line2.sink).kind);
		
		input = "prog output -> image;";
		show(input);
		scanner = new Scanner(input).scan(); 
		show(scanner);
		parser = new Parser(scanner); //Create a parser
		thrown.expect(SyntaxException.class);
		try {
			ast = parser.parse();; //Parse the program, which should throw an exception
		} catch (SyntaxException e) {
			show(e);  //catch the exception and show it
			throw e;  //rethrow for Junit
		}
	}
	
	@Test
	public void testStmnt2_imgIn() throws LexicalException, SyntaxException {
		String input = "prog input_img <- \"in_img\";\n"
					 + "input_img <- junk;\n"
					 + "input_img <- @ x+y | r-a;\n";
		show(input);
		Scanner scanner = new Scanner(input).scan(); 
		show(scanner); 
		Parser parser = new Parser(scanner);
		Program ast = parser.parse();
		show(ast);
		assertEquals(ast.name, "prog");
		Statement_In _1 = (Statement_In) ast.decsAndStatements.get(0);
		assertEquals("input_img",_1.name);
		assertEquals("in_img", ((Source_StringLiteral)_1.source).fileOrUrl);
		Statement_In _2 = (Statement_In) ast.decsAndStatements.get(1);
		assertEquals("junk", ((Source_Ident)_2.source).name);
		Statement_In _3 = (Statement_In) ast.decsAndStatements.get(2);
		Source_CommandLineParam s = (Source_CommandLineParam)_3.source;
		Expression_Binary expr = (Expression_Binary)s.paramNum;
		assertEquals(OP_OR,expr.op);
		Expression_Binary e0 = (Expression_Binary)expr.e0;
		assertEquals(OP_PLUS,e0.op);
		Expression_Binary e1 = (Expression_Binary)expr.e1;
		assertEquals(OP_MINUS,e1.op);
		
		input = "prog input <- @image;";
		show(input);
		scanner = new Scanner(input).scan(); 
		show(scanner);
		parser = new Parser(scanner); //Create a parser
		thrown.expect(SyntaxException.class);
		try {
			ast = parser.parse();; //Parse the program, which should throw an exception
		} catch (SyntaxException e) {
			show(e);  //catch the exception and show it
			throw e;  //rethrow for Junit
		}
	}
	
	@Test
	public void testStmnt3_assign() throws LexicalException, SyntaxException {
		String input = "prog assign_var [[x,y]] = junk;";
		show(input);
		Scanner scanner = new Scanner(input).scan(); 
		show(scanner); 
		Parser parser = new Parser(scanner);
		Program ast = parser.parse();
		show(ast);
		assertEquals(ast.name, "prog");
		Statement_Assign stmnt = (Statement_Assign) ast.decsAndStatements.get(0);
		LHS lhs = stmnt.lhs;
		assertEquals("assign_var", lhs.name.toString());
		Index index = lhs.index;
		assertEquals(KW_x, index.firstToken.kind);
		assertEquals(KW_x, ((Expression_PredefinedName) index.e0).kind);
		assertEquals(KW_y, ((Expression_PredefinedName) index.e1).kind);
		
		input = "prog lhs [x,y] = matrix[xx,yy];";
		show(input);
		scanner = new Scanner(input).scan(); 
		show(scanner);
		parser = new Parser(scanner); //Create a parser
		thrown.expect(SyntaxException.class);
		try {
			ast = parser.parse();; //Parse the program, which should throw an exception
		} catch (SyntaxException e) {
			show(e);  //catch the exception and show it
			throw e;  //rethrow for Junit
		}
	}
	
	@Test
	public void testStmnt4_assign2() throws LexicalException, SyntaxException {
		String input = "prog lhs [[x,A]] = matrix[xx,yy];";
		show(input);        							//Display the input 
		Scanner scanner = new Scanner(input).scan();			//Create a Scanner and initialize it
		show(scanner);
		Parser parser = new Parser(scanner);
		thrown.expect(SyntaxException.class);
		try {
			Program ast = parser.parse();; //Parse the program, which should throw an exception
		} catch (SyntaxException e) {
			show(e);  //catch the exception and show it
			throw e;  //rethrow for Junit
		}
	}
	
	@Test
	public void testStmnt5_assign3() throws LexicalException, SyntaxException {
		String input = "prog lhs [[r,A]] <- matrix[xx,yy];";
		show(input);        							//Display the input 
		Scanner scanner = new Scanner(input).scan();			//Create a Scanner and initialize it
		show(scanner);
		Parser parser = new Parser(scanner);
		thrown.expect(SyntaxException.class);
		try {
			Program ast = parser.parse();; //Parse the program, which should throw an exception
		} catch (SyntaxException e) {
			show(e);  //catch the exception and show it
			throw e;  //rethrow for Junit
		}
	}
	
	@Test
	public void testStmnt6_assign4() throws LexicalException, SyntaxException {
		String input = "prog lhs [ [x,y] ];";
		show(input);        							//Display the input 
		Scanner scanner = new Scanner(input).scan();			//Create a Scanner and initialize it
		show(scanner);
		Parser parser = new Parser(scanner);
		thrown.expect(SyntaxException.class);
		try {
			Program ast = parser.parse();; //Parse the program, which should throw an exception
		} catch (SyntaxException e) {
			show(e);  //catch the exception and show it
			throw e;  //rethrow for Junit
		}
	}
	
	@Test
	public void testStmnt7_assign5() throws LexicalException, SyntaxException {
		String input = "prog lhs = x | @;";
		show(input);
		Scanner scanner = new Scanner(input).scan();				//Create a Scanner and initialize it
		show(scanner);
		Parser parser = new Parser(scanner);
		thrown.expect(SyntaxException.class);
		try {
			Program ast = parser.parse();; //Parse the program, which should throw an exception
		} catch (SyntaxException e) {
			show(e);  //catch the exception and show it
			throw e;  //rethrow for Junit
		}
	}
	
	@Test
	public void testExpr1() throws LexicalException, SyntaxException {
		String input = "2++-3";
		show(input);
		Scanner scanner = new Scanner(input).scan();  
		show(scanner);
		Parser parser = new Parser(scanner);
		Expression_Binary expr = (Expression_Binary)parser.expression();
		show(expr);
		assertEquals(OP_PLUS, expr.op);
		Expression_Unary e1 = (Expression_Unary) expr.e1;
		assertEquals(OP_PLUS, e1.op);
		Expression_Unary e = (Expression_Unary) e1.e;
		assertEquals(OP_MINUS,e.op);
		assertEquals(3, ((Expression_IntLit)e.e).value);
	}
	
	@Test
	public void testExpr2() throws SyntaxException, LexicalException {
		String input = "2 > 3 ? 4 : 1";
		show(input);
		Scanner scanner = new Scanner(input).scan();  
		show(scanner);
		Parser parser = new Parser(scanner);
		Expression_Conditional expr = (Expression_Conditional) parser.expression();
		show(expr);
		Expression_Binary condition = (Expression_Binary)expr.condition;
		assertEquals(2,((Expression_IntLit)condition.e0).value);
		assertEquals(OP_GT,condition.op);
		assertEquals(3,((Expression_IntLit)condition.e1).value);
		Expression_IntLit trueExpr = (Expression_IntLit) expr.trueExpression;
		assertEquals(4,trueExpr.value);
		Expression_IntLit falseExpr = (Expression_IntLit) expr.falseExpression;
		assertEquals(1,falseExpr.value);
	}
	
	@Test
	public void testExpr3_funcApp() throws SyntaxException, LexicalException {
		String input = "polar_a (45)";
		show(input);
		Scanner scanner = new Scanner(input).scan();  
		show(scanner);
		Parser parser = new Parser(scanner);
		Expression_FunctionAppWithExprArg expr1 = (Expression_FunctionAppWithExprArg) parser.expression();
		show(expr1);
		assertEquals(KW_polar_a, expr1.function);
		assertEquals(45, ((Expression_IntLit)expr1.arg).value);
		
		input = "polar_r [cart_x(90),cart_y(90)]";
		show(input);
		scanner = new Scanner(input).scan();  
		show(scanner); 
		parser = new Parser(scanner);
		Expression_FunctionAppWithIndexArg expr2 = (Expression_FunctionAppWithIndexArg) parser.expression();
		show(expr2);
		assertEquals(KW_polar_r, expr2.function);
		Index index = expr2.arg;
		assertEquals(KW_cart_x,((Expression_FunctionAppWithExprArg)index.e0).function);
		assertEquals(KW_cart_y,((Expression_FunctionAppWithExprArg)index.e1).function);
		
		input = "sin(cart_x(90),cart_y(90))";
		show(input);
		scanner = new Scanner(input).scan();			//Create a Scanner and initialize it
		show(scanner);
		parser = new Parser(scanner);
		thrown.expect(SyntaxException.class);
		try {
			Expression expr = parser.expression(); //Parse the program, which should throw an exception
		} catch (SyntaxException e) {
			show(e);  //catch the exception and show it
			throw e;  //rethrow for Junit
		}
	}
	
	@Test
	public void testExpr4_primary() throws SyntaxException, LexicalException {
		String input = "true";
		show(input);
		Scanner scanner = new Scanner(input).scan();  
		show(scanner);
		Parser parser = new Parser(scanner);
		Expression_BooleanLit expr1 = (Expression_BooleanLit) parser.expression();
		show(expr1);
		assertEquals(true, expr1.value);
		
		input = "( (ned==alive) ? (true) : (false) )";
		show(input);
		scanner = new Scanner(input).scan();  
		show(scanner);
		parser = new Parser(scanner);
		Expression_Conditional expr2 = (Expression_Conditional) parser.expression();
		show(expr2);
		Expression_BooleanLit trueExpr = (Expression_BooleanLit) expr2.trueExpression;
		assertEquals(true,trueExpr.value);
		Expression_BooleanLit falseExpr = (Expression_BooleanLit) expr2.falseExpression;
		assertEquals(false,falseExpr.value);
		
		input = "(cos[ atan(90) , atan(45) ])";
		show(input);
		scanner = new Scanner(input).scan();  
		show(scanner);
		parser = new Parser(scanner);
		Expression_FunctionAppWithIndexArg expr3 = (Expression_FunctionAppWithIndexArg) parser.expression();
		show(expr3);
		assertEquals(KW_cos,expr3.function);
		Index index = expr3.arg;
		assertEquals(KW_atan,((Expression_FunctionAppWithExprArg)index.e0).function);
		assertEquals(KW_atan,((Expression_FunctionAppWithExprArg)index.e1).function);
		
		input = "( (tyrion==dead) ? false : what_the_heck? )";
		show(input);
		scanner = new Scanner(input).scan();				//Create a Scanner and initialize it
		show(scanner);
		parser = new Parser(scanner);
		thrown.expect(SyntaxException.class);
		try {
			Expression expr = parser.expression(); //Parse the program, which should throw an exception
		} catch (SyntaxException e) {
			show(e);  //catch the exception and show it
			throw e;  //rethrow for Junit
		}
	}
	
	@Test
	public void testExpr5_primary() throws LexicalException, SyntaxException {
		String input = "( sin( cos[ atan(abs(cord_x==45 ? 45 : 90)) , "
				+ "polar_a[ atan(abs(cord_y==90 ? 90 : 45)), ] ] ) )";
		show(input);
		Scanner scanner = new Scanner(input).scan();				//Create a Scanner and initialize it
		show(scanner);
		Parser parser = new Parser(scanner);
		thrown.expect(SyntaxException.class);
		try {
			Expression expr = parser.expression();; //Parse the program, which should throw an exception
		} catch (SyntaxException e) {
			show(e);  //catch the exception and show it
			throw e;  //rethrow for Junit
		}
	}
	
	@Test
	public void testExpr6_orNand() throws LexicalException, SyntaxException {
		String input = "!x | +y & -Z";
		show(input);
		Scanner scanner = new Scanner(input).scan();				//Create a Scanner and initialize it
		show(scanner);
		Parser parser = new Parser(scanner);
		Expression_Binary expr1 = (Expression_Binary) parser.expression();
		show(expr1);
		Expression_Unary e0 = (Expression_Unary) expr1.e0;
		assertEquals(OP_EXCL,e0.op);
		Expression_Binary e1 = (Expression_Binary) expr1.e1;
		assertEquals(OP_AND,e1.op);
		
		input = "junk1 || junk2";
		show(input);
		scanner = new Scanner(input).scan();				//Create a Scanner and initialize it
		show(scanner);
		parser = new Parser(scanner);
		thrown.expect(SyntaxException.class);
		try {
			Expression expr = parser.expression();; //Parse the program, which should throw an exception
		} catch (SyntaxException e) {
			show(e);  //catch the exception and show it
			throw e;  //rethrow for Junit
		}
	}
	
	@Test
	public void testExpr7_orNand() throws LexicalException, SyntaxException {
		String input = "junk0 | junk1 && junk2";
		show(input);
		Scanner scanner = new Scanner(input).scan();				//Create a Scanner and initialize it
		show(scanner);
		Parser parser = new Parser(scanner);
		thrown.expect(SyntaxException.class);
		try {
			Expression expr = parser.expression();; //Parse the program, which should throw an exception
		} catch (SyntaxException e) {
			show(e);  //catch the exception and show it
			throw e;  //rethrow for Junit
		}
	}
	
	@Test
	public void testExpr8_eq() throws LexicalException, SyntaxException {
		String input = "junk0 | junk1 & junk2===junk3";
		show(input);
		Scanner scanner = new Scanner(input).scan();				//Create a Scanner and initialize it
		show(scanner);
		Parser parser = new Parser(scanner);
		thrown.expect(SyntaxException.class);
		try {
			Expression expr = parser.expression();; //Parse the program, which should throw an exception
		} catch (SyntaxException e) {
			show(e);  //catch the exception and show it
			throw e;  //rethrow for Junit
		}
	}
	
	@Test
	public void testExpr9_eq() throws LexicalException, SyntaxException {
		String input = "junk0 | junk1 & junk2!==junk3 ";
		show(input);
		Scanner scanner = new Scanner(input).scan();				//Create a Scanner and initialize it
		show(scanner);
		Parser parser = new Parser(scanner);
		thrown.expect(SyntaxException.class);
		try {
			Expression expr = parser.expression();; //Parse the program, which should throw an exception
		} catch (SyntaxException e) {
			show(e);  //catch the exception and show it
			throw e;  //rethrow for Junit
		}
	}
	
	@Test
	public void testExpr10_eq() throws LexicalException, SyntaxException {
		String input = "junk0 | junk1 & junk2!=!=junk3 ";
		show(input);
		Scanner scanner = new Scanner(input).scan();				//Create a Scanner and initialize it
		show(scanner);
		Parser parser = new Parser(scanner);
		thrown.expect(SyntaxException.class);
		try {
			Expression expr = parser.expression();; //Parse the program, which should throw an exception
		} catch (SyntaxException e) {
			show(e);  //catch the exception and show it
			throw e;  //rethrow for Junit
		}
	}
	
	@Test
	public void testExpr11_eq() throws LexicalException, SyntaxException {
		String input = "junk0 | junk1 & junk2====junk3 ";
		show(input);
		Scanner scanner = new Scanner(input).scan();				//Create a Scanner and initialize it
		show(scanner);
		Parser parser = new Parser(scanner);
		thrown.expect(SyntaxException.class);
		try {
			Expression expr = parser.expression();; //Parse the program, which should throw an exception
		} catch (SyntaxException e) {
			show(e);  //catch the exception and show it
			throw e;  //rethrow for Junit
		}
	}
	
	@Test
	public void testExpr12_eq() throws LexicalException, SyntaxException {
		String input = "junk0 | junk1 & junk2!===junk3 ";
		show(input);
		Scanner scanner = new Scanner(input).scan();				//Create a Scanner and initialize it
		show(scanner);
		Parser parser = new Parser(scanner);
		thrown.expect(SyntaxException.class);
		try {
			Expression expr = parser.expression();; //Parse the program, which should throw an exception
		} catch (SyntaxException e) {
			show(e);  //catch the exception and show it
			throw e;  //rethrow for Junit
		}
	}
	
	@Test
	public void testExpr13_eq() throws LexicalException, SyntaxException {
		String input = "x | @";
		show(input);
		Scanner scanner = new Scanner(input).scan();				//Create a Scanner and initialize it
		show(scanner);
		Parser parser = new Parser(scanner);
		thrown.expect(SyntaxException.class);
		try {
			Expression expr = parser.expression();; //Parse the program, which should throw an exception
		} catch (SyntaxException e) {
			show(e);  //catch the exception and show it
			throw e;  //rethrow for Junit
		}
	}
}
