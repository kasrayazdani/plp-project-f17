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
}
