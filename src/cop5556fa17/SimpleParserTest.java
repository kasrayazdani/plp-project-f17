package cop5556fa17;

import static org.junit.Assert.*;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import cop5556fa17.Scanner.LexicalException;
import cop5556fa17.Scanner.Token;
import cop5556fa17.SimpleParser.SyntaxException;

import static cop5556fa17.Scanner.Kind.*;

public class SimpleParserTest {

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
	 * Simple test case with an empty program.  This test 
	 * expects an SyntaxException because all legal programs must
	 * have at least an identifier
	 *   
	 * @throws LexicalException
	 * @throws SyntaxException 
	 */
	@Test
	public void testEmpty() throws LexicalException, SyntaxException {
		String input = "";  							//The input is the empty string.  This is not legal
		show(input);        							//Display the input 
		Scanner scanner = new Scanner(input).scan();	//Create a Scanner and initialize it
		show(scanner);   								//Display the Scanner
		SimpleParser parser = new SimpleParser(scanner);//Create a parser
		thrown.expect(SyntaxException.class);
		try {
			parser.parse();  							//Parse the program
		} catch (SyntaxException e) {
			show(e);
			throw e;
		}
	}


	/** Another example.  This is a legal program and should pass when 
	 * your parser is implemented.
	 * 
	 * @throws LexicalException
	 * @throws SyntaxException
	 */

	@Test
	public void testDec1() throws LexicalException, SyntaxException {
		String input = "prog int k;";
		show(input);
		Scanner scanner = new Scanner(input).scan();  //Create a Scanner and initialize it
		show(scanner);   //Display the Scanner
		SimpleParser parser = new SimpleParser(scanner);  //
		parser.parse();

		input = "prog image img;";
		show(input);
		scanner = new Scanner(input).scan();
		show(scanner);
		parser = new SimpleParser(scanner);
		parser.parse();

		input = "prog url ss1=src;\n" + 
				"file ss2=\"src\";";
		show(input);
		scanner = new Scanner(input).scan();
		show(scanner);
		parser = new SimpleParser(scanner);
		parser.parse();
	}

	@Test
	public void testDec2_var() throws LexicalException, SyntaxException {
		String input = "prog int k = 1/2 + 2*3 - 3+4;";
		show(input);
		Scanner scanner = new Scanner(input).scan();  	//Create a Scanner and initialize it
		show(scanner);   //Display the Scanner
		SimpleParser parser = new SimpleParser(scanner);
		parser.parse();
		
		input = "prog boolean sh_it = john==dead ? false : real;";
		show(input);
		scanner = new Scanner(input).scan();  			//Create a Scanner and initialize it
		show(scanner);   								//Display the Scanner
		parser = new SimpleParser(scanner);
		parser.parse();
		
		input = "prog boolean faith = cersei==dead ? true : lost;\n" + 
				"int kill_list = ask(sansa);";
		show(input);        							//Display the input 
		scanner = new Scanner(input).scan();			//Create a Scanner and initialize it
		show(scanner);   								//Display the Scanner
		parser = new SimpleParser(scanner);				//Create a parser
		thrown.expect(SyntaxException.class);
		try {
			parser.parse();  							//Parse the program
		} catch (SyntaxException e) {
			show(e);
			throw e;
		}
	}
	
	/**
	 * This example invokes the method for expression directly. 
	 * Effectively, we are viewing Expression as the start
	 * symbol of a sub-language.
	 *  
	 * Although a compiler will always call the parse() method,
	 * invoking others is useful to support incremental development.  
	 * We will only invoke expression directly, but 
	 * following this example with others is recommended.  
	 * 
	 * @throws SyntaxException
	 * @throws LexicalException
	 */
	@Test
	public void expression1() throws SyntaxException, LexicalException {
		String input = "2";
		show(input);
		Scanner scanner = new Scanner(input).scan();  
		show(scanner);   
		SimpleParser parser = new SimpleParser(scanner);  
		parser.expression();  //Call expression directly.  
	}
	
	@Test
	public void expression2() throws SyntaxException, LexicalException {
		String input = "2+++3";
		show(input);
		Scanner scanner = new Scanner(input).scan();  
		show(scanner);   
		SimpleParser parser = new SimpleParser(scanner);  
		parser.expression();  //Call expression directly.  
	}
	
	@Test
	public void expression3() throws SyntaxException, LexicalException {
		String input = "2 > 3 ? 4 : 1";
		show(input);
		Scanner scanner = new Scanner(input).scan();  
		show(scanner);   
		SimpleParser parser = new SimpleParser(scanner);  
		parser.expression();  //Call expression directly.  
	}
	
	@Test
	public void expression4() throws SyntaxException, LexicalException {
		String input = "++++++++++x";
		show(input);
		Scanner scanner = new Scanner(input).scan();  
		show(scanner);   
		SimpleParser parser = new SimpleParser(scanner);  
		parser.expression();  //Call expression directly.  
	}
	
	@Test
	public void expression5() throws SyntaxException, LexicalException {
		String input = "2**3";
		show(input);
		Scanner scanner = new Scanner(input).scan();  
		show(scanner);   
		SimpleParser parser = new SimpleParser(scanner);  
		parser.expression();  //Call expression directly.  
	}
	
	@Test
	public void expression6_funcApp() throws SyntaxException, LexicalException {
		String input = "polar_a (45)";
		show(input);
		Scanner scanner = new Scanner(input).scan();  
		show(scanner);   
		SimpleParser parser = new SimpleParser(scanner);  
		parser.expression();  //Call expression directly.
		
		input = "polar_r [cart_x(90),cart_y(90)]";
		show(input);
		scanner = new Scanner(input).scan();  
		show(scanner);   
		parser = new SimpleParser(scanner);  
		parser.expression();  //Call expression directly.
		
		input = "sin(cart_x(90),cart_y(90))";
		show(input);
		scanner = new Scanner(input).scan();			//Create a Scanner and initialize it
		show(scanner);
		parser = new SimpleParser(scanner);//Create a parser
		thrown.expect(SyntaxException.class);
		try {
			parser.expression();  							//Parse the program
		} catch (SyntaxException e) {
			show(e);
			throw e;
		}
	}
	
	@Test
	public void expression7_primary() throws SyntaxException, LexicalException {
		String input = "true";
		show(input);
		Scanner scanner = new Scanner(input).scan();  
		show(scanner);   
		SimpleParser parser = new SimpleParser(scanner);  
		parser.expression();  //Call expression directly.
		
		input = "( (ned==alive) ? (true) : (false) )";
		show(input);
		scanner = new Scanner(input).scan();  
		show(scanner);   
		parser = new SimpleParser(scanner);  
		parser.expression();  //Call expression directly.
		
		input = "( (tyrion==dead) ? false : what_the_heck? )";
		show(input);
		scanner = new Scanner(input).scan();				//Create a Scanner and initialize it
		show(scanner);
		parser = new SimpleParser(scanner);					//Create a parser
		thrown.expect(SyntaxException.class);
		try {
			parser.expression();  							//Parse the program
		} catch (SyntaxException e) {
			show(e);
			throw e;
		}
	}
}

