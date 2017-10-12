package cop5556fa17;



import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cop5556fa17.Scanner.Kind;
import cop5556fa17.Scanner.Token;
import cop5556fa17.SimpleParser.SyntaxException;

import static cop5556fa17.Scanner.Kind.*;
import cop5556fa17.AST.*;

public class Parser {

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

	Parser(Scanner scanner) {
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
	public Program parse() throws SyntaxException {
		Program ast = program();
		matchEOF();
		return ast;
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
	
	Program program() throws SyntaxException {
		//TODO  implement this
		Program pg = null;
		ArrayList<ASTNode> decsAndStatements = new ArrayList<ASTNode>();
		Token ft = t;
		match(IDENTIFIER);
		while (t.kind != EOF){
			if (dec_start.contains(t.kind)) {
				decsAndStatements.add(declaration());
				match(SEMI);
			}
			else if (stmnt_start.contains(t.kind)) {
				decsAndStatements.add(statement());
				match(SEMI);
			}
			else {
				String message = t.kind + " at " + t.line + ":" + t.pos_in_line + "\n";
				throw new SyntaxException(t, message);
			}
		}
		pg = new Program(ft, ft, decsAndStatements);
		return pg;
	}

	Declaration declaration() throws SyntaxException {
		Declaration dec = null;
		Kind dec_type = t.kind;
		switch(dec_type) {
		case KW_int:
		case KW_boolean:
			//consume();
			dec = var_declaration();
			break;
		
		case KW_image:
			consume();
			dec = img_declaration();
			break;
		
		case KW_url:
		case KW_file:
			//consume();
			dec = ss_declaration();
			break;
			
		default:
			String message = t.kind + " at " + t.line + ":" + t.pos_in_line + "\n";
			throw new SyntaxException(t, message);
		}
		return dec;
	}

	Declaration_Variable var_declaration() throws SyntaxException {
		Declaration_Variable dec_var;
		Expression e = null;
		Token ft = t;
		Token type = t;
		consume();
		Token name = t;
		match(IDENTIFIER);
		if (t.kind != SEMI) {
			match(OP_ASSIGN);
			e = expression();
		}
		dec_var = new Declaration_Variable(ft, type, name, e);
		return dec_var;
	}
	
	Declaration_Image img_declaration() throws SyntaxException {
		Declaration_Image dec_img;
		Token ft = t;
		Expression xSize = null;
		Expression ySize = null;
		if (t.kind != IDENTIFIER) {
			match(LSQUARE);
			xSize = expression();
			match(COMMA);
			ySize = expression();
			match(RSQUARE);
		}
		Token name = t; 
		match(IDENTIFIER);
		Source source = null;
		if (t.kind != SEMI) {
			match(OP_LARROW);
			source = resolve_source();
		}
		dec_img = new Declaration_Image(ft, xSize, ySize, name, source);
		return dec_img;
	}
	
	Declaration_SourceSink ss_declaration() throws SyntaxException {
		Declaration_SourceSink dec_ss;
		Token ft = t;
		Token type = ft;
		consume();
		Token name = t;
		match(IDENTIFIER);
		match(OP_ASSIGN);
		Source source = resolve_source();
		dec_ss = new Declaration_SourceSink(ft, type, name, source);
		return dec_ss;
	}
	
	Source resolve_source() throws SyntaxException {
		Source source = null;
		Token ft = t;
		switch(t.kind) {
		case STRING_LITERAL:
			String fileOrUrl = ft.getText();
			consume();
			source = new Source_StringLiteral(ft,fileOrUrl);
			break;	
		case OP_AT:
			consume();
			Expression e = expression();
			source = new Source_CommandLineParam(ft, e);
			break;
		case IDENTIFIER:
			consume();
			Token name = ft;
			source = new Source_Ident(ft, name);
			break;
		default:
			String message = t.kind + " at " + t.line + ":" + t.pos_in_line + "\n";
			throw new SyntaxException(t, message);
		}
		return source;
	}
	
	Statement statement() throws SyntaxException {
		Statement statement = null;
		Token ft = t;
		Token name = t;
		Expression e = null;
		match(IDENTIFIER);
		switch(t.kind) {
		case LSQUARE:
		case OP_ASSIGN:				//AssignStatement
			Index index = null;
			if (t.kind==LSQUARE) {
				match(LSQUARE);
				index = lhsSelector();
				match(RSQUARE);
			}
			LHS lhs = new LHS(ft, name, index);
			match(OP_ASSIGN);
			e = expression();
			statement = new Statement_Assign(ft, lhs, e);
			break;
		case OP_RARROW:				//ImageOutStatement
			consume();
			statement = new Statement_Out(ft, name, resolve_sink());
			break;
		case OP_LARROW:				//ImageInStatement
			consume();
			statement = new Statement_In(ft, name, resolve_source());
			break;
		default:
			String message = t.kind + " at " + t.line + ":" + t.pos_in_line + "\n";
			throw new SyntaxException(t, message);
		}
		return statement;
	}
	
	/*void assign_statement() throws SyntaxException {
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
			String message = t.kind + " at " + t.line + ":" + t.pos_in_line + "\n";
			throw new SyntaxException(t, message);
		}
	}*/
	
	Index lhsSelector() throws SyntaxException {
		match(LSQUARE);
		Index index = null;
		Token index_ft = t;
		Expression e0 = null;
		Expression e1 = null;
		Kind kind;
		Token ft;
		switch(t.kind) {
		case KW_x:
			ft = t; kind = t.kind;
			match(KW_x);
			e0 = new Expression_PredefinedName(ft, kind);
			match(COMMA);
			ft = t; kind = t.kind;
			match(KW_y);
			e1 = new Expression_PredefinedName(ft, kind);
			break;
		case KW_r:
			ft = t; kind = t.kind;
			match(KW_r);
			e0 = new Expression_PredefinedName(t, kind);
			match(COMMA);
			ft = t; kind = t.kind;
			match(KW_A);
			e1 = new Expression_PredefinedName(t, kind);
			break;
		default:
			String message = t.kind + " at " + t.line + ":" + t.pos_in_line + "\n";
			throw new SyntaxException(t, message);
		}
		match(RSQUARE);
		index = new Index(index_ft, e0, e1);
		return index;
	}
	
	Sink resolve_sink() throws SyntaxException {
		Sink sink = null;
		Token ft = t;
		switch(t.kind) {
		case IDENTIFIER:
			Token name_ident = t;
			consume();
			sink = new Sink_Ident(ft, name_ident);
			break;
		case KW_SCREEN:
			consume();
			sink = new Sink_SCREEN(ft);
			break;
		default:
			String message = t.kind + " at " + t.line + ":" + t.pos_in_line + "\n";
			throw new SyntaxException(t, message);
		}
		return sink;
	}
	
	/**
	 * Expression ::=  OrExpression  OP_Q  Expression OP_COLON Expression    | OrExpression
	 * 
	 * Our test cases may invoke this routine directly to support incremental development.
	 * 
	 * @throws SyntaxException
	 */
	Expression expression() throws SyntaxException {
		//TODO implement this.
		Expression e = null;
		Token ft = t;
		Expression condition = null;
		Expression trueExpression = null;
		Expression falseExpression = null;
		condition = or_expression();
		if (t.kind==OP_Q) {
			match(OP_Q);
			trueExpression = expression();
			match(OP_COLON);
			falseExpression = expression();
		}
		if (trueExpression != null)
			e =  new Expression_Conditional(ft, condition, trueExpression, falseExpression);
		else
			e = condition;
		return e;
		//throw new UnsupportedOperationException();
	}

	Expression or_expression() throws SyntaxException {
		Expression or_expr = null;
		Token ft = t;
		or_expr = and_expression();
		while (t.kind == OP_OR) {
			Token op = t;
			consume();
			Expression e1 = and_expression();
			or_expr = new Expression_Binary(ft, or_expr, op, e1);
		}
		return or_expr;
	}
	
	Expression and_expression() throws SyntaxException {
		Expression and_expr = null;
		Token ft = t;
		and_expr = eq_expression();
		while(t.kind == OP_AND) {
			Token op = t;
			consume();
			Expression e1 = eq_expression();
			and_expr = new Expression_Binary(ft, and_expr, op, e1);
		}
		return and_expr;
	}
	
	Expression eq_expression() throws SyntaxException {
		Expression eq_expr = null;
		Token ft = t;
		eq_expr = rel_expression();
		while(Arrays.asList(new Kind[]{OP_EQ,OP_NEQ}).contains(t.kind)) {
			Token op = t;
			consume();
			Expression e1 = rel_expression();
			eq_expr = new Expression_Binary(ft, eq_expr, op, e1);
		}
		return eq_expr;
	}
	
	Expression rel_expression() throws SyntaxException {
		Expression rel_expr = null;
		Token ft = t;
		rel_expr = add_expression();
		while(Arrays.asList(new Kind[]{OP_LT,OP_GT,OP_LE,OP_GE}).contains(t.kind)) {
			Token op = t;
			consume();
			Expression e1 = add_expression();
			rel_expr = new Expression_Binary(ft, rel_expr, op, e1);
		}
		return rel_expr;
	}
	
	Expression add_expression() throws SyntaxException {
		Expression add_expr = null;
		Token ft = t;
		add_expr = mult_expression();
		while(Arrays.asList(new Kind[]{OP_PLUS,OP_MINUS}).contains(t.kind)) {
			Token op = t;
			consume();
			Expression e1 = mult_expression();
			add_expr = new Expression_Binary(ft, add_expr, op, e1);
		}
		return add_expr;
	}
	
	Expression mult_expression() throws SyntaxException {
		Expression mult_expr = null;
		Token ft = t;
		mult_expr = unary_expression();
		while(Arrays.asList(new Kind[]{OP_TIMES,OP_DIV,OP_MOD}).contains(t.kind)) {
			Token op = t;
			consume();
			Expression e1 = unary_expression();
			mult_expr = new Expression_Binary(ft, mult_expr, op, e1);
		}
		return mult_expr;
	}
	
	Expression unary_expression() throws SyntaxException {
		//TODO .. not LL(1) possibly
		Expression unary_expr = null;
		if (t.kind==OP_PLUS || t.kind==OP_MINUS) {
			Token ft = t;
			Token op = t;
			consume();
			Expression e = unary_expression();
			unary_expr = new Expression_Unary(ft, op, e);
		}
		else
			unary_expr = unary_expression_notplusminus();
		return unary_expr;
	}
	
	Expression unary_expression_notplusminus() throws SyntaxException {
		Expression e = null;
		if (t.kind == OP_EXCL) {
			Token ft = t;
			Token op = t;
			consume();
			e = new Expression_Unary(ft, op, unary_expression());
		}
		else if (t.kind == IDENTIFIER) {
			e = identOrPixelSelector_expression();
		}
		else if (Arrays.asList(new Kind[]{KW_x,KW_y,KW_r,KW_a,
										  KW_X,KW_Y,KW_Z,KW_A,KW_R,
										  KW_DEF_X,KW_DEF_Y}).contains(t.kind)) {
			Token ft = t;
			consume();
			e = new Expression_PredefinedName(ft, ft.kind);
		}
		else
			e = primary();
		return e;
	}
	
	Expression identOrPixelSelector_expression() throws SyntaxException {
		Expression e = null;
		Token ft = t;
		Token ident = t;
		Token name = t;
		Index index = null;
		match(IDENTIFIER);
		if (t.kind == LSQUARE) {
			match(LSQUARE);
			index = selector();
			match(RSQUARE);
		}
		if (index != null)
			e = new Expression_PixelSelector(ft, name, index);
		else
			e = new Expression_Ident(ft, ident);
		return e;
	}
	
	Expression primary() throws SyntaxException {
		Expression e = null;
		Token ft = t;
		if (t.kind == INTEGER_LITERAL) {
			consume();
			e = new Expression_IntLit(ft, ft.intVal());
		}
		else if (t.kind == LPAREN) {
			match(LPAREN);
			e = expression();
			match(RPAREN);
		}
		else if (t.kind == BOOLEAN_LITERAL) {
			consume();
			e = new Expression_BooleanLit(ft, ft.toString()=="true" ? true : false);
		}
		else
			e = function_application();
		return e;
	}
	
	Index selector() throws SyntaxException {
		Index index = null;
		Token ft = t;
		Expression e0 = expression();
		match(COMMA);
		Expression e1 = expression();
		index = new Index(ft, e0, e1);
		return index;
	}
	
	Expression function_application() throws SyntaxException {
		Expression e = null;
		Token ft = t;
		Kind func;
		if (Arrays.asList(new Kind[]{KW_sin,KW_cos,KW_atan,
									 KW_abs,KW_cart_x,KW_cart_y,
									 KW_polar_a,KW_polar_r}).contains(t.kind)) {
			func = t.kind;
			consume();
			switch(t.kind) {
			case LPAREN:
				match(LPAREN);
				Expression expr_arg = expression();
				match(RPAREN);
				e = new Expression_FunctionAppWithExprArg(ft, func, expr_arg);
				break;
			case LSQUARE:
				match(LSQUARE);
				Index index_arg = selector();
				match(RSQUARE);
				e = new Expression_FunctionAppWithIndexArg(ft, func, index_arg);
				break;
			default:
				String message = t.kind + " at " + t.line + ":" + t.pos_in_line + "\n";
				throw new SyntaxException(t, message);
			}
		}
		else {
			String message = t.kind + " at " + t.line + ":" + t.pos_in_line + "\n";
			throw new SyntaxException(t, message);
		}
		return e;
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
		String message =  "Expected EOL at " + t.line + ":" + t.pos_in_line + "\n";
		throw new SyntaxException(t, message);
	}
	
	private Token match(Kind kind) throws SyntaxException {
		if (t.kind == kind) {
			consume();
			return t;
		}
		String message = "Expected " + kind + " at " + t.line + ":" + t.pos_in_line + "\n";
		throw new SyntaxException(t, message);
	}
}
