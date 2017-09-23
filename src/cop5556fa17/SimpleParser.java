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
		if (t.kind != SEMI) {
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
		if (t.kind != SEMI) {
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
		case LSQUARE:				//Lhs
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
			break;
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

	void or_expression() throws SyntaxException {
		and_expression();
		while (t.kind == OP_OR) {
			consume();
			and_expression();
		}
	}
	
	void and_expression() throws SyntaxException {
		eq_expression();
		while(t.kind == OP_AND) {
			consume();
			eq_expression();
		}
	}
	
	void eq_expression() throws SyntaxException {
		rel_expression();
		while(Arrays.asList(new Kind[]{OP_EQ,OP_NEQ}).contains(t.kind)) {
			consume();
			rel_expression();			
		}
	}
	
	void rel_expression() throws SyntaxException {
		add_expression();
		while(Arrays.asList(new Kind[]{OP_LT,OP_GT,OP_LE,OP_GE}).contains(t.kind)) {
			consume();
			add_expression();
		}
	}
	
	void add_expression() throws SyntaxException {
		mult_expression();
		while(Arrays.asList(new Kind[]{OP_PLUS,OP_MINUS}).contains(t.kind)) {
			consume();
			mult_expression();
		}
	}
	
	void mult_expression() throws SyntaxException {
		unary_expression();
		while(Arrays.asList(new Kind[]{OP_TIMES,OP_DIV,OP_MOD}).contains(t.kind)) {
			consume();
			unary_expression();
		}
	}
	
	void unary_expression() throws SyntaxException {
		//TODO .. not LL(1) possibly
	}
	
	void unary_expression_notplusminus() throws SyntaxException {
		if (t.kind == OP_EXCL) {
			consume();
			unary_expression();
		}
		else if (t.kind == IDENTIFIER) {
			identOrPixelSelector_expression();
		}
		else if (Arrays.asList(new Kind[]{KW_x,KW_y,KW_r,KW_a,
										  KW_X,KW_Y,KW_Z,KW_A,KW_R,
										  KW_DEF_X,KW_DEF_Y}).contains(t.kind)) {
			consume();
		}
		else
			primary();
	}
	
	void identOrPixelSelector_expression() throws SyntaxException {
		match(IDENTIFIER);
		if (t.kind == LSQUARE) {
			match(LSQUARE);
			selector();
			match(RSQUARE);
		}
	}
	
	void primary() throws SyntaxException {
		if (t.kind == INTEGER_LITERAL)
			consume();
		else if (t.kind == LPAREN) {
			match(LPAREN);
			expression();
			match(RPAREN);
		}
		else
			function_application();
	}
	
	void selector() throws SyntaxException {
		expression();
		match(COMMA);
		expression();
	}
	
	void function_application() throws SyntaxException {
		if (Arrays.asList(new Kind[]{KW_sin,KW_cos,KW_atan,
									 KW_abs,KW_cart_x,KW_cart_y,
									 KW_polar_a,KW_polar_r}).contains(t.kind)) {
			consume();
			switch(t.kind) {
			case LPAREN:
				match(LPAREN);
				expression();
				match(RPAREN);
				break;
			case LSQUARE:
				match(LSQUARE);
				selector();
				match(RSQUARE);
				break;
			default:
				throw new UnsupportedOperationException();
			}
		}
		else
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
