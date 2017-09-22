package cop5556fa17;



import java.util.Arrays;
import java.util.List;

import cop5556fa17.Scanner.Kind;
import cop5556fa17.Scanner.Token;
import cop5556fa17.SimpleParser.SyntaxException;

import static cop5556fa17.Scanner.Kind.*;

public class SimpleParser {

	@SuppressWarnings("serial")
	public class SyntaxException extends Exception {
		Token t;

		public SyntaxException(Token t, String message) {
			super(message);
			this.t = t;
		}

	}


	Scanner scanner;
	Token t;

	SimpleParser(Scanner scanner) {
		this.scanner = scanner;
		t = scanner.nextToken();
	}
	
	void consume() {
		t = scanner.nextToken();
	}

	/**
	 * Main method called by compiler to parser input.
	 * Checks for EOF
	 * 
	 * @throws SyntaxException
	 */
	public void parse() throws SyntaxException {
		program();
		matchEOF();
	}
	
	/**
	 * Program ::=  IDENTIFIER   ( Declaration SEMI | Statement SEMI )*   
	 * 
	 * Program is start symbol of our grammar.
	 * 
	 * @throws SyntaxException
	 */
	
	final List<Kind> dec_start = Arrays.asList(new Kind[]{KW_int, KW_boolean, KW_image, KW_url, KW_file});
	final List<Kind> stmnt_start =Arrays.asList(new Kind[]{IDENTIFIER});
	
	void program() throws SyntaxException {
		//TODO  implement this
		if (t.kind == IDENTIFIER){
			consume();
			while (t.kind != EOF){
				if (dec_start.contains(t.kind)) {
					declaration();
					match(SEMI);
				}
				else if (stmnt_start.contains(t.kind)) {
					statement();
					match(SEMI);
				}
			}
		}
		else
			throw new UnsupportedOperationException();
	}

	void declaration() throws SyntaxException {
		Kind dec_type = t.kind;
		switch(dec_type) {
		case KW_int:
		case KW_boolean:
			consume();
			var_declaration();
			break;
		
		case KW_image:
			consume();
			img_declaration();
			break;
		
		case KW_url:
		case KW_file:
			consume();
			ss_declaration();
			break;
			
		default:
			throw new UnsupportedOperationException();
		}
	}

	void var_declaration() throws SyntaxException {
		match(IDENTIFIER);
		if (t.kind == SEMI)
			consume();
		else {
			match(OP_ASSIGN);
			expression();
		}
	}
	
	void img_declaration() throws SyntaxException {
		if (t.kind != IDENTIFIER) {
			match(LSQUARE);
			expression();
			match(COMMA);
			expression();
			match(RSQUARE);
		}
		match(IDENTIFIER);
		if (t.kind == SEMI)
			consume();
		else {
			match(OP_LARROW);
			resolve_source();
		}
	}
	
	void ss_declaration() throws SyntaxException {
		match(IDENTIFIER);
		match(OP_ASSIGN);
		resolve_source();
	}
	
	void resolve_source() throws SyntaxException {
		switch(t.kind) {
		case STRING_LITERAL:
			consume();
			break;	
		case OP_AT:
			consume();
			expression();
			break;
		case IDENTIFIER:
			consume();
			break;
		default:
			throw new UnsupportedOperationException();
		}
	}
	
	void statement() throws SyntaxException {
		match(IDENTIFIER);
		switch(t.kind) {
		case LSQUARE:
		case OP_ASSIGN:
			assign_statement();
			break;
		case OP_RARROW:				//ImageOutStatement
			consume();
			resolve_sink();
			break;
		case OP_LARROW:				//ImageInStatement
			consume();
			resolve_sink();
			break;
		default:
			throw new UnsupportedOperationException();
		}
	}
	
	void assign_statement() throws SyntaxException {
		switch(t.kind) {
		case LSQUARE:
			match(LSQUARE);
			lhsSelector();
			match(RSQUARE);
			match(OP_ASSIGN);
			expression();
			break;				
		case OP_ASSIGN:
			consume();
			expression();
			break;
		default:
			throw new UnsupportedOperationException();
		}
	}
	
	void lhsSelector() throws SyntaxException {
		match(LSQUARE);
		switch(t.kind) {
		case KW_x:
			match(KW_x);
			match(COMMA);
			match(KW_y);
			break;
		case KW_r:
			match(KW_r);
			match(COMMA);
			match(KW_A);
		default:
			throw new UnsupportedOperationException();
		}
		match(RSQUARE);
	}
	
	void resolve_sink() {
		switch(t.kind) {
		case IDENTIFIER:
			consume();
			break;
		case KW_SCREEN:
			consume();
			break;
		default:
			throw new UnsupportedOperationException();
		}
	}
	
	/**
	 * Expression ::=  OrExpression  OP_Q  Expression OP_COLON Expression    | OrExpression
	 * 
	 * Our test cases may invoke this routine directly to support incremental development.
	 * 
	 * @throws SyntaxException
	 */
	void expression() throws SyntaxException {
		//TODO implement this.
		throw new UnsupportedOperationException();
	}

	/**
	 * Only for check at end of program. Does not "consume" EOF so no attempt to get
	 * nonexistent next Token.
	 * 
	 * @return
	 * @throws SyntaxException
	 */
	private Token matchEOF() throws SyntaxException {
		if (t.kind == EOF) {
			return t;
		}
		String message =  "Expected EOL at " + t.line + ":" + t.pos_in_line;
		throw new SyntaxException(t, message);
	}
	
	private Token match(Kind kind) throws SyntaxException {
		if (t.kind == kind) {
			consume();
			return t;
		}
		throw new UnsupportedOperationException();
	}
}
