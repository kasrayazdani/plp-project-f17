package cop5556fa17;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import static org.junit.Assert.*;

import cop5556fa17.AST.ASTNode;
import cop5556fa17.AST.ASTVisitor;
import cop5556fa17.AST.Declaration_Image;
import cop5556fa17.AST.Declaration_SourceSink;
import cop5556fa17.AST.Declaration_Variable;
import cop5556fa17.AST.Expression;
import cop5556fa17.AST.Expression_FunctionAppWithExprArg;
import cop5556fa17.AST.Expression_IntLit;
import cop5556fa17.AST.Expression_PixelSelector;
import cop5556fa17.AST.Expression_PredefinedName;
import cop5556fa17.AST.Expression_Unary;
import cop5556fa17.AST.Index;
import cop5556fa17.AST.LHS;
import cop5556fa17.AST.Program;
import cop5556fa17.AST.Source_CommandLineParam;
import cop5556fa17.AST.Source_StringLiteral;
import cop5556fa17.AST.Statement_Out;
import cop5556fa17.AST.Statement_Assign;
import cop5556fa17.Parser.SyntaxException;
import cop5556fa17.Scanner.Kind;
import cop5556fa17.Scanner.LexicalException;
import cop5556fa17.Scanner.Token;
import cop5556fa17.TypeCheckVisitor.SemanticException;

import static cop5556fa17.Scanner.Kind.*;

public class TypeCheckTest {

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
	 * Scans, parses, and type checks given input String.
	 * 
	 * Catches, prints, and then rethrows any exceptions that occur.
	 * 
	 * @param input
	 * @throws Exception
	 */
	void typeCheck(String input) throws Exception {
		show(input);
		try {
			Scanner scanner = new Scanner(input).scan();
			ASTNode ast = new Parser(scanner).parse();
			show(ast);
			ASTVisitor v = new TypeCheckVisitor();
			ast.visit(v, null);
		} catch (Exception e) {
			show(e);
			throw e;
		}
	}

	/**
	 * Simple test case with an almost empty program.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testSmallest() throws Exception {
		String input = "n"; //Smallest legal program, only has a name
		show(input); // Display the input
		Scanner scanner = new Scanner(input).scan(); // Create a Scanner and
														// initialize it
		show(scanner); // Display the Scanner
		Parser parser = new Parser(scanner); // Create a parser
		ASTNode ast = parser.parse(); // Parse the program
		TypeCheckVisitor v = new TypeCheckVisitor();
		String name = (String) ast.visit(v, null);
		show("AST for program " + name);
		show(ast);
	}
	
	@Test
	public void test1() throws Exception {
		String input = "prog url facebook = \"http://www.facebook.com\";"
				     + "file exec = \"/usr/share/bin/passwd\";"; 
		typeCheck(input);
		
		input = "prog url facebook = facebook;";
		thrown.expect(SemanticException.class);
		typeCheck(input);
	}
	
	@Test
	public void test2() throws Exception {
		String input = "prog image[40,40] in_img <- \"/home/test.jpg\";\n"
				     + "file path =  \"/home/test2.jpg\";\n"
				     + "image in_img2 <- path;";
		typeCheck(input);
	}

	@Test
	public void test3() throws Exception {
		String input = "prog int height = 10; int width = 10; int junk = (height>=width) ? height : width;";
		typeCheck(input);
	}

	@Test
	public void test4() throws Exception {
		String input = "prog boolean go4kill; boolean alive = true; \n"
				     + "int for = 10; int against = 11; go4kill = (for > against) ? true : false; \n"
				     + "boolean success = go4kill & alive;";
		typeCheck(input);
		
		input = "prog boolean go4kill; int for = 10; "
			  + "int against = 11; kill = (for > against) ? true : false; "
			  + "boolean success = go4Kill & alive;";
		thrown.expect(SemanticException.class);
		typeCheck(input);
	}
	
	@Test
	public void test5() throws Exception {
		String input = "prog int area;\n"
				      + "int height = 50;\n"
				      + "int base = 120;\n"
				      + "area = base*height;\n"
				      + "int angle = cos(120/50);\n"
				      + "boolean isRight = angle==0 ? true : false;";
		typeCheck(input);
		
		input = "prog image [ A*R/Z-DEF_Y , (x++y) ] img2 <- \"img2\";\n";
		typeCheck(input);
		
		input = "prog image [ A*R/Z+DEF_X-DEF_Y , (x==y) ] img2 <- xx;\n";
		thrown.expect(SemanticException.class);
		typeCheck(input);
	}
	
	@Test
	public void test6() throws Exception {
		String input = "prog image output <- \"img2show\";\n"
				     + "output -> SCREEN;\n";
		typeCheck(input);
		
		input = "prog image assign_var <- @ 2++-3;\n"
			  + "image img2 <- \"junk\";\n"
			  + "assign_var = img2;";
		typeCheck(input);
	}
	
	@Test
	public void test7() throws Exception {
		String input = "prog image junk <- junk;";
		thrown.expect(SemanticException.class);
		typeCheck(input);
	}
	
	
	/**
	 * This test should pass with a fully implemented assignment
	 * @throws Exception
	 */
	@Test
	public void testDec1() throws Exception {
		String input = "prog int k = 42;";
		typeCheck(input);

		input = "prog int k = k + 1;";
		thrown.expect(SemanticException.class);
		typeCheck(input);
	}
	 
	 /**
	  * This program does not declare k. The TypeCheckVisitor should
	  * throw a SemanticException in a fully implemented assignment.
	  * @throws Exception
	  */
	 @Test
	 public void testUndec() throws Exception {
	 String input = "prog k = 42;";
	 thrown.expect(SemanticException.class);
	 typeCheck(input);
	 }
}
