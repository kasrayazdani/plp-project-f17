/**
 * /**
 * JUunit tests for the Scanner for the class project in COP5556 Programming Language Principles 
 * at the University of Florida, Fall 2017.
 * 
 * This software is solely for the educational benefit of students 
 * enrolled in the course during the Fall 2017 semester.  
 * 
 * This software, and any software derived from it,  may not be shared with others or posted to public web sites,
 * either during the course or afterwards.
 * 
 *  @Beverly A. Sanders, 2017
 */

package cop5556fa17;

import static org.junit.Assert.*;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

//import com.sun.beans.util.Cache.Kind;

import cop5556fa17.Scanner.LexicalException;
import cop5556fa17.Scanner.Token;

import static cop5556fa17.Scanner.Kind.*;

public class ScannerTest {

	//set Junit to be able to catch exceptions
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	
	//To make it easy to print objects and turn this output on and off
	static final boolean doPrint = true;
	private void show(Object input) {
		if (doPrint) {
			System.out.println(input.toString());
		}
	}

	/**
	 *Retrieves the next token and checks that it is an EOF token. 
	 *Also checks that this was the last token.
	 *
	 * @param scanner
	 * @return the Token that was retrieved
	 */
	
	Token checkNextIsEOF(Scanner scanner) {
		Scanner.Token token = scanner.nextToken();
		assertEquals(Scanner.Kind.EOF, token.kind);
		assertFalse(scanner.hasTokens());
		return token;
	}


	/**
	 * Retrieves the next token and checks that its kind, position, length, line, and position in line
	 * match the given parameters.
	 * 
	 * @param scanner
	 * @param kind
	 * @param pos
	 * @param length
	 * @param line
	 * @param pos_in_line
	 * @return  the Token that was retrieved
	 */
	Token checkNext(Scanner scanner, Scanner.Kind kind, int pos, int length, int line, int pos_in_line) {
		Token t = scanner.nextToken();
		assertEquals(scanner.new Token(kind, pos, length, line, pos_in_line), t);
		return t;
	}

	/**
	 * Retrieves the next token and checks that its kind and length match the given
	 * parameters.  The position, line, and position in line are ignored.
	 * 
	 * @param scanner
	 * @param kind
	 * @param length
	 * @return  the Token that was retrieved
	 */
	Token check(Scanner scanner, Scanner.Kind kind, int length) {
		Token t = scanner.nextToken();
		assertEquals(kind, t.kind);
		assertEquals(length, t.length);
		return t;
	}

	/**
	 * Simple test case with a (legal) empty program
	 *   
	 * @throws LexicalException
	 */
	@Test
	public void testEmpty() throws LexicalException {
		String input = "";  							//The input is the empty string.  This is legal
		show(input);        							//Display the input 
		Scanner scanner = new Scanner(input).scan();  	//Create a Scanner and initialize it
		show(scanner);   								//Display the Scanner
		checkNextIsEOF(scanner);  						//Check that the only token is the EOF token.
	}
	
	/**
	 * Test illustrating how to put a new line in the input program and how to
	 * check content of tokens.
	 * 
	 * Because we are using a Java String literal for input, we use \n for the
	 * end of line character. (We should also be able to handle \n, \r, and \r\n
	 * properly.)
	 * 
	 * Note that if we were reading the input from a file, as we will want to do 
	 * later, the end of line character would be inserted by the text editor.
	 * Showing the input will let you check your input is what you think it is.
	 * 
	 * @throws LexicalException
	 */
	@Test
	public void testSemi() throws LexicalException {
		String input = ";;\n;;";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, SEMI, 0, 1, 1, 1);
		checkNext(scanner, SEMI, 1, 1, 1, 2);
		checkNext(scanner, SEMI, 3, 1, 2, 1);
		checkNext(scanner, SEMI, 4, 1, 2, 2);
		checkNextIsEOF(scanner);
	}
	
	@Test
	public void testSeperators() throws LexicalException {
		String input = "[(),()];\n();";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, LSQUARE,	0, 1, 1, 1);
		checkNext(scanner, LPAREN,	1, 1, 1, 2);
		checkNext(scanner, RPAREN,	2, 1, 1, 3);
		checkNext(scanner, COMMA,	3, 1, 1, 4);
		checkNext(scanner, LPAREN,	4, 1, 1, 5);
		checkNext(scanner, RPAREN,	5, 1, 1, 6);
		checkNext(scanner, RSQUARE,	6, 1, 1, 7);
		checkNext(scanner, SEMI,	7, 1, 1, 8);
		checkNext(scanner, LPAREN,	9, 1, 2, 1);
		checkNext(scanner, RPAREN,	10, 1, 2, 2);
		checkNext(scanner, SEMI,	11, 1, 2, 3);
		checkNextIsEOF(scanner);
	}
	
	@Test
	public void testOperators() throws LexicalException {
		String input = "(+)*(-)==?:;";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, LPAREN,		0, 1, 1, 1);
		checkNext(scanner, OP_PLUS,		1, 1, 1, 2);
		checkNext(scanner, RPAREN,		2, 1, 1, 3);
		checkNext(scanner, OP_TIMES,	3, 1, 1, 4);
		checkNext(scanner, LPAREN,		4, 1, 1, 5);
		checkNext(scanner, OP_MINUS,	5, 1, 1, 6);
		checkNext(scanner, RPAREN,		6, 1, 1, 7);
		checkNext(scanner, OP_EQ,		7, 2, 1, 8);
		checkNext(scanner, OP_Q,		9, 1, 1, 10);
		checkNext(scanner, OP_COLON,	10, 1, 1, 11);
		checkNext(scanner, SEMI,		11, 1, 1, 12);
		checkNextIsEOF(scanner);
		
		input = ">= | <= ? / : ** & %;\n" + 
				"> -> !=! < <-\n" + 
				'@';
		scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, OP_GE,		0, 2, 1, 1);
		checkNext(scanner, OP_OR,		3, 1, 1, 4);
		checkNext(scanner, OP_LE,		5, 2, 1, 6);
		checkNext(scanner, OP_Q,		8, 1, 1, 9);
		checkNext(scanner, OP_DIV,		10, 1, 1, 11);
		checkNext(scanner, OP_COLON,	12, 1, 1, 13);
		checkNext(scanner, OP_POWER,	14, 2, 1, 15);
		checkNext(scanner, OP_AND,		17, 1, 1, 18);
		checkNext(scanner, OP_MOD,		19, 1, 1, 20);
		checkNext(scanner, SEMI,		20, 1, 1, 21);
		checkNext(scanner, OP_GT,		22, 1, 2, 1);
		checkNext(scanner, OP_RARROW,	24, 2, 2, 3);
		checkNext(scanner, OP_NEQ,		27, 2, 2, 6);
		checkNext(scanner, OP_EXCL,		29, 1, 2, 8);
		checkNext(scanner, OP_LT,		31, 1, 2, 10);
		checkNext(scanner, OP_LARROW,	33, 2, 2, 12);
		checkNext(scanner, OP_AT,		36, 1, 3, 1);
		checkNextIsEOF(scanner);
	}
	
	@Test
	public void testIntegerLiteral() throws LexicalException {
		String input = "101";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, INTEGER_LITERAL, 0, 3, 1, 1);
		checkNextIsEOF(scanner);
		
		input = "101;\n" + 
				"0010;";
		scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, INTEGER_LITERAL, 0, 3, 1, 1);
		checkNext(scanner, SEMI, 			3, 1, 1, 4);
		checkNext(scanner, INTEGER_LITERAL, 5, 1, 2, 1);
		checkNext(scanner, INTEGER_LITERAL, 6, 1, 2, 2);
		checkNext(scanner, INTEGER_LITERAL, 7, 2, 2, 3);
		checkNext(scanner, SEMI, 			9, 1, 2, 5);
		checkNextIsEOF(scanner);
	}
	
	@Test
	public void testIntegerLiteralOverflow() throws LexicalException {
		String input = "9999999999999999999999999";
		show(input);
		thrown.expect(LexicalException.class);
		try {
			Scanner scanner = new Scanner(input).scan();
			show(scanner);
		} catch (LexicalException e){
			show(e);
			assertEquals(1, e.getPos());
			throw e;
		}
	}
	
	@Test
	public void testReservedWords() throws LexicalException {
		String input = "DEF_X=5;\n" + 
					   "DEF_Y=6;";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, KW_DEF_X,		0, 5, 1, 1);
		checkNext(scanner, OP_ASSIGN,		5, 1, 1, 6);
		checkNext(scanner, INTEGER_LITERAL,	6, 1, 1, 7);
		checkNext(scanner, SEMI,			7, 1, 1, 8);
		checkNext(scanner, KW_DEF_Y,		9, 5, 2, 1);
		checkNext(scanner, OP_ASSIGN,		14, 1, 2, 6);
		checkNext(scanner, INTEGER_LITERAL,	15, 1, 2, 7);
		checkNext(scanner, SEMI,			16, 1, 2, 8);
		checkNextIsEOF(scanner);	
	} 
	
	@Test
	public void testIdentifier() throws LexicalException {
		String input = "int p=DEF_X;\n" + 
					   "int q=DEF_Y;\n" + 
					   "int sum <- (p+q);";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, KW_int,		0, 3, 1, 1);
		checkNext(scanner, IDENTIFIER,	4, 1, 1, 5);
		checkNext(scanner, OP_ASSIGN,	5, 1, 1, 6);
		checkNext(scanner, KW_DEF_X,	6, 5, 1, 7);
		checkNext(scanner, SEMI,		11, 1, 1, 12);
		checkNext(scanner, KW_int,		13, 3, 2, 1);
		checkNext(scanner, IDENTIFIER, 	17, 1, 2, 5);
		checkNext(scanner, OP_ASSIGN, 	18, 1, 2, 6);
		checkNext(scanner, KW_DEF_Y, 	19, 5, 2, 7);
		checkNext(scanner, SEMI, 		24, 1, 2, 12);
		checkNext(scanner, KW_int, 		26, 3, 3, 1);
		checkNext(scanner, IDENTIFIER, 	30, 3, 3, 5);
		checkNext(scanner, OP_LARROW, 	34, 2, 3, 9);
		checkNext(scanner, LPAREN, 		37, 1, 3, 12);
		checkNext(scanner, IDENTIFIER, 	38, 1, 3, 13);
		checkNext(scanner, OP_PLUS, 	39, 1, 3, 14);
		checkNext(scanner, IDENTIFIER, 	40, 1, 3, 15);
		checkNext(scanner, RPAREN, 		41, 1, 3, 16);
		checkNext(scanner, SEMI, 		42, 1, 3, 17);
		checkNextIsEOF(scanner);
	}
	
	@Test
	public void testBooleanLiteral() throws LexicalException {
		String input = "if x==5:\n" + 
					   "  flag=true;\n" + 
					   "flag=false;";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, IDENTIFIER, 		0, 2, 1, 1);
		checkNext(scanner, KW_x, 			3, 1, 1, 4);
		checkNext(scanner, OP_EQ, 			4, 2, 1, 5);
		checkNext(scanner, INTEGER_LITERAL,	6, 1, 1, 7);
		checkNext(scanner, OP_COLON, 		7, 1, 1, 8);
		checkNext(scanner, IDENTIFIER, 		11, 4, 2, 3);
		checkNext(scanner, OP_ASSIGN, 		15, 1, 2, 7);
		checkNext(scanner, BOOLEAN_LITERAL,	16, 4, 2, 8);
		checkNext(scanner, SEMI, 			20, 1, 2, 12);
		checkNext(scanner, IDENTIFIER, 		22, 4, 3, 1);
		checkNext(scanner, OP_ASSIGN, 		26, 1, 3, 5);
		checkNext(scanner, BOOLEAN_LITERAL,	27, 5, 3, 6);
		checkNext(scanner, SEMI, 			32, 1, 3, 11);
		checkNextIsEOF(scanner);		
	}
	
	@Test
	public void testComment() throws LexicalException {
		String input = "//This is a comment\n" + 
					   "p = 5+4; //Junk\n";
		
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, IDENTIFIER,		20, 1, 2, 1);
		checkNext(scanner, OP_ASSIGN, 		22, 1, 2, 3);
		checkNext(scanner, INTEGER_LITERAL,	24, 1, 2, 5);
		checkNext(scanner, OP_PLUS, 		25, 1, 2, 6);
		checkNext(scanner, INTEGER_LITERAL, 26, 1, 2, 7);
		checkNext(scanner, SEMI,			27, 1, 2, 8);
		checkNextIsEOF(scanner);
	}
	
	@Test
	public void testStringLiteral() throws LexicalException {
		String input = "\"Junk\\n\";\n";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, STRING_LITERAL, 	0, 8, 1, 1);
		checkNext(scanner, SEMI, 			8, 1, 1, 9);
		checkNextIsEOF(scanner);
	}
	
	@Test
	public void testStringLiteral2() throws LexicalException {
		String input ="\"Junk is \r\n Junk\"";
		show(input);
		thrown.expect(LexicalException.class);
		try {
			new Scanner(input).scan();
		} catch (LexicalException e) {
			show(e);
			assertEquals(10,e.getPos());
			throw e;
		}
	}
	
	@Test
	public void testStringLiteral3() throws LexicalException {
		String input = "\"abc\\\"a\"";
		show(input);
		Scanner scanner = new Scanner(input).scan();
		show(scanner);
		checkNext(scanner, STRING_LITERAL, 0, 8, 1, 1);
		checkNextIsEOF(scanner);
		
		input = "\"\t\t\"";
		show(input);
		scanner = new Scanner(input).scan();
		show(scanner);
		checkNext(scanner, STRING_LITERAL, 0, 4, 1, 1);
		checkNextIsEOF(scanner);
	}
	
	@Test
	public void failStringLiteral() throws LexicalException {
		String input = "\" greet\\ings\"";
		show(input);
		thrown.expect(LexicalException.class);  //Tell JUnit to expect a LexicalException
		try {
			new Scanner(input).scan();
		} catch (LexicalException e) {  //
			show(e);
			assertEquals(9,e.getPos());
			throw e;
		}
	}
	
	/**
	 * This example shows how to test that your scanner is behaving when the
	 * input is illegal.  In this case, we are giving it a String literal
	 * that is missing the closing ".  
	 * 
	 * Note that the outer pair of quotation marks delineate the String literal
	 * in this test program that provides the input to our Scanner.  The quotation
	 * mark that is actually included in the input must be escaped, \".
	 * 
	 * The example shows catching the exception that is thrown by the scanner,
	 * looking at it, and checking its contents before rethrowing it.  If caught
	 * but not rethrown, then JUnit won't get the exception and the test will fail.  
	 * 
	 * The test will work without putting the try-catch block around 
	 * new Scanner(input).scan(); but then you won't be able to check 
	 * or display the thrown exception.
	 * 
	 * @throws LexicalException
	 */
	@Test
	public void failUnclosedStringLiteral() throws LexicalException {
		String input = "\" greetings  ";
		show(input);
		thrown.expect(LexicalException.class);  //Tell JUnit to expect a LexicalException
		try {
			new Scanner(input).scan();
		} catch (LexicalException e) {  //
			show(e);
			assertEquals(13,e.getPos());
			throw e;
		}
	}
}
