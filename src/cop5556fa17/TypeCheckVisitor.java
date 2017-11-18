package cop5556fa17;

import cop5556fa17.Scanner.Kind;
import cop5556fa17.Scanner.Token;
import cop5556fa17.TypeUtils.Type;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

import cop5556fa17.AST.ASTNode;
import cop5556fa17.AST.ASTVisitor;
import cop5556fa17.AST.Declaration;
import cop5556fa17.AST.Declaration_Image;
import cop5556fa17.AST.Declaration_SourceSink;
import cop5556fa17.AST.Declaration_Variable;
import cop5556fa17.AST.Expression_Binary;
import cop5556fa17.AST.Expression_BooleanLit;
import cop5556fa17.AST.Expression_Conditional;
import cop5556fa17.AST.Expression_FunctionAppWithExprArg;
import cop5556fa17.AST.Expression_FunctionAppWithIndexArg;
import cop5556fa17.AST.Expression_Ident;
import cop5556fa17.AST.Expression_IntLit;
import cop5556fa17.AST.Expression_PixelSelector;
import cop5556fa17.AST.Expression_PredefinedName;
import cop5556fa17.AST.Expression_Unary;
import cop5556fa17.AST.Index;
import cop5556fa17.AST.LHS;
import cop5556fa17.AST.Program;
import cop5556fa17.AST.Sink_Ident;
import cop5556fa17.AST.Sink_SCREEN;
import cop5556fa17.AST.Source_CommandLineParam;
import cop5556fa17.AST.Source_Ident;
import cop5556fa17.AST.Source_StringLiteral;
import cop5556fa17.AST.Statement_Assign;
import cop5556fa17.AST.Statement_In;
import cop5556fa17.AST.Statement_Out;

public class TypeCheckVisitor implements ASTVisitor {
	

	@SuppressWarnings("serial")
	public static class SemanticException extends Exception {
		Token t;

		public SemanticException(Token t, String message) {
			super("line " + t.line + " pos " + t.pos_in_line + ": "+  message);
			this.t = t;
		}

	}		
		
	SymbolTable symbolTable = new SymbolTable();
	
	/**
	 * The program name is only used for naming the class.  It does not rule out
	 * variables with the same name.  It is returned for convenience.
	 * 
	 * @throws Exception 
	 */
	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		for (ASTNode node: program.decsAndStatements) {
			node.visit(this, arg);
		}
		return program.name;
	}

	@Override
	public Object visitDeclaration_Variable(
			Declaration_Variable declaration_Variable, Object arg)
			throws Exception {
		if (symbolTable.lookupType(declaration_Variable.name)==null) {
			Type type = TypeUtils.getType(declaration_Variable.firstToken);
			if (declaration_Variable.e != null) {
				if (TypeUtils.getType(declaration_Variable.firstToken)!=declaration_Variable.e.visit(this, arg))
					throw new SemanticException(declaration_Variable.e.firstToken, "Type mismatch.\n");
			}
			symbolTable.insert(declaration_Variable.name, declaration_Variable);
			return type;
		}
		else
			throw new SemanticException(declaration_Variable.firstToken, "Symbol already present.\n");
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpression_Binary(Expression_Binary expression_Binary,
			Object arg) throws Exception {
		Type type = null;
		Type e0_type = (Type) expression_Binary.e0.visit(this, arg);
		Type e1_type = (Type) expression_Binary.e1.visit(this, arg);
		if (e0_type != e1_type)
			throw new SemanticException(expression_Binary.firstToken, "Return type of e0 and e1 not same");
		switch (expression_Binary.op) {
		case OP_EQ:
		case OP_NEQ:
			type = Type.BOOLEAN;
			break;
			
		case OP_GE:
		case OP_GT:
		case OP_LT:
		case OP_LE:
			if (e0_type == Type.INTEGER)
				type = Type.BOOLEAN;
			break;
			
		case OP_AND:
		case OP_OR:
			if (e0_type == Type.INTEGER || e0_type == Type.BOOLEAN)
				type = e0_type;
			break;
			
		case OP_DIV:
		case OP_MINUS:
		case OP_MOD:
		case OP_PLUS:
		case OP_POWER:
		case OP_TIMES:
			if (e0_type == Type.INTEGER)
				type = Type.INTEGER;
			break;
		
		default:
			type = null;
			break;
		}
		if (type != null)
			return type;
		else
			throw new SemanticException(expression_Binary.firstToken, "Return type null.\n");
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpression_Unary(Expression_Unary expression_Unary,
			Object arg) throws Exception {
		if (Arrays.asList(Kind.OP_EXCL,Kind.OP_PLUS,Kind.OP_MINUS).contains(expression_Unary.op)) {
			Type type = (Type) expression_Unary.e.visit(this, arg);
			switch(expression_Unary.op) {
			case OP_EXCL:
				if (!(type == Type.BOOLEAN || type == Type.INTEGER))
					throw new SemanticException(expression_Unary.e.firstToken, "Return type not integer or bool.\n");
				break;
				
			case OP_PLUS:
			case OP_MINUS:
				if (!(type==Type.INTEGER))
					throw new SemanticException(expression_Unary.e.firstToken, "Return type not integer.\n");
				break;				
			}
			expression_Unary.setType(type);
			return type;
		}
		else
			throw new SemanticException(expression_Unary.firstToken, "Unidentified unary op.\n");
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitIndex(Index index, Object arg) throws Exception {
		if (index.e0.visit(this, arg) == Type.INTEGER) {
			if (index.e1.visit(this, arg) == Type.INTEGER) {
				index.setCartesian(!(index.e0.firstToken.kind == Kind.KW_r && index.e1.firstToken.kind == Kind.KW_A));
				return Type.NONE;
			}
			else
				throw new SemanticException(index.e1.firstToken, "Return type not integer.\n");
		}
		else
			throw new SemanticException(index.e0.firstToken, "Return type not integer.\n");
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpression_PixelSelector(
			Expression_PixelSelector expression_PixelSelector, Object arg)
			throws Exception {
		Type type = TypeUtils.getType(symbolTable.lookupType(expression_PixelSelector.name).firstToken);
		if (type == Type.IMAGE)
			return Type.INTEGER;
		else if (expression_PixelSelector.index == null)
				return type;
		else
			throw new SemanticException(expression_PixelSelector.index.firstToken, "Index not null.\n");	
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpression_Conditional(
			Expression_Conditional expression_Conditional, Object arg)
			throws Exception {
		Type cond_type = (Type) expression_Conditional.condition.visit(this, arg);
		if (cond_type == Type.BOOLEAN) {
			Type type = (Type) expression_Conditional.trueExpression.visit(this, arg); 
			if (type == expression_Conditional.falseExpression.visit(this, arg)) {
				expression_Conditional.setType(type);
				return type;
			}
			else
				throw new SemanticException(expression_Conditional.falseExpression.firstToken, "Return type not same as trueExpression.\n");
		}
		else
			throw new SemanticException(expression_Conditional.condition.firstToken, "Return type not bool.\n");
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitDeclaration_Image(Declaration_Image declaration_Image,
			Object arg) throws Exception {
		if (symbolTable.lookupType(declaration_Image.name) == null) {
			Type source_type;
			if (declaration_Image.source != null)
				source_type = (Type) declaration_Image.source.visit(this, arg);
			
			symbolTable.insert(declaration_Image.name, declaration_Image);
			
			if (declaration_Image.xSize != null || declaration_Image.ySize != null) {
				if (!(declaration_Image.xSize!=null && declaration_Image.ySize !=null &&
						(Type) declaration_Image.xSize.visit(this, arg)==Type.INTEGER && 
						(Type) declaration_Image.ySize.visit(this, arg)==Type.INTEGER))
					throw new SemanticException(declaration_Image.firstToken, "Retrun type of xSize OR ySize not integer.\n");
			}
			return Type.IMAGE;
		}
		else
			throw new SemanticException(declaration_Image.firstToken, "Symbol already present.\n");
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitSource_StringLiteral(
			Source_StringLiteral source_StringLiteral, Object arg)
			throws Exception {
		Type type;
		try {
			URL url = new URL(source_StringLiteral.fileOrUrl);
			type = Type.URL;
		}
		catch (java.net.MalformedURLException e) {
			type = Type.FILE;
		}
		return type;
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitSource_CommandLineParam(
			Source_CommandLineParam source_CommandLineParam, Object arg)
			throws Exception {
		if ((Type) source_CommandLineParam.paramNum.visit(this, arg) == Type.INTEGER)
			return Type.INTEGER;
		else
			throw new SemanticException(source_CommandLineParam.firstToken, "Return type not integer.\n");
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitSource_Ident(Source_Ident source_Ident, Object arg)
			throws Exception {
		if (symbolTable.lookupType(source_Ident.name)==null)
			throw new SemanticException(source_Ident.firstToken, source_Ident.name + " not declared.\n");
		Type type = TypeUtils.getType(symbolTable.lookupType(source_Ident.name).firstToken);
		if (type == Type.URL || type == Type.FILE)
			return type;
		else
			throw new SemanticException(source_Ident.firstToken, "Return type not url or file.\n");
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitDeclaration_SourceSink(
			Declaration_SourceSink declaration_SourceSink, Object arg)
			throws Exception {
		if (symbolTable.lookupType(declaration_SourceSink.name) == null) {
			Type type = (Type) declaration_SourceSink.source.visit(this, arg);
			if (TypeUtils.getType(declaration_SourceSink.firstToken) != type)
				throw new SemanticException(declaration_SourceSink.source.firstToken, "Source rerurn type mismatch.\n");
			symbolTable.insert(declaration_SourceSink.name,declaration_SourceSink);
			return type;
		}
		else
			throw new SemanticException(declaration_SourceSink.firstToken, "Symbol already present.\n");
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpression_IntLit(Expression_IntLit expression_IntLit,
			Object arg) throws Exception {
		return Type.INTEGER;
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpression_FunctionAppWithExprArg(
			Expression_FunctionAppWithExprArg expression_FunctionAppWithExprArg,
			Object arg) throws Exception {
		if (!(expression_FunctionAppWithExprArg.arg.visit(this, arg) == Type.INTEGER))
			throw new SemanticException(expression_FunctionAppWithExprArg.firstToken, "Return type not integer");
		return Type.INTEGER;
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpression_FunctionAppWithIndexArg(
			Expression_FunctionAppWithIndexArg expression_FunctionAppWithIndexArg,
			Object arg) throws Exception {
		return Type.INTEGER;
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpression_PredefinedName(
			Expression_PredefinedName expression_PredefinedName, Object arg)
			throws Exception {
		return Type.INTEGER;
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitStatement_Out(Statement_Out statement_Out, Object arg)
			throws Exception {
		Declaration dec = symbolTable.lookupType(statement_Out.name);
		if (dec != null) {
			statement_Out.setDec(dec);
			Type dec_type =  TypeUtils.getType(statement_Out.getDec().firstToken);
			Type sink_type = (Type) statement_Out.sink.visit(this, arg);
			if (!(((dec_type == Type.INTEGER || dec_type == Type.BOOLEAN) && sink_type == Type.SCREEN) || 
					(dec_type == Type.IMAGE && (sink_type == Type.FILE || sink_type == Type.SCREEN))))
				throw new SemanticException(statement_Out.sink.firstToken, "Return type not consistent.\n");
			return Type.NONE;
		}
		else
			throw new SemanticException(statement_Out.firstToken, "Invalid Statement_Out.\n");
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitStatement_In(Statement_In statement_In, Object arg)
			throws Exception {
		Declaration dec = symbolTable.lookupType(statement_In.name);
		if (dec != null) {
			statement_In.setDec(dec);
			//if (TypeUtils.getType(statement_In.getDec().firstToken) != statement_In.source.visit(this, arg))
			//	throw new SemanticException(statement_In.source.firstToken, "Return type not consistent.\n");
			return Type.NONE;
		}
		else
			throw new SemanticException(statement_In.firstToken, "Invalid Statement_In.\n");
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitStatement_Assign(Statement_Assign statement_Assign,
			Object arg) throws Exception {
		Type e_type = (Type) statement_Assign.e.visit(this, arg);
		Type lhs_type = (Type) statement_Assign.lhs.visit(this, arg);
		if (lhs_type != e_type)
			throw new SemanticException(statement_Assign.e.firstToken, "Return type not consistent.\n");
		statement_Assign.setCartesian(statement_Assign.lhs.isCartesian());
		return Type.NONE;
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitLHS(LHS lhs, Object arg) throws Exception {
		Declaration dec = symbolTable.lookupType(lhs.name);
		if (dec != null) {
			lhs.setDeclaration(dec);
			if (lhs.index != null)
				lhs.setCartesian(lhs.index.isCartesian());
			else
				lhs.setCartesian(false);
			return TypeUtils.getType(lhs.getDeclaration().firstToken);
		}
		else
			throw new SemanticException(lhs.firstToken, lhs.name + " not declared.\n");
		//throw new UnsupportedOperationException(); 
	}

	@Override
	public Object visitSink_SCREEN(Sink_SCREEN sink_SCREEN, Object arg)
			throws Exception {
		return Type.SCREEN;
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitSink_Ident(Sink_Ident sink_Ident, Object arg)
			throws Exception {
		if (TypeUtils.getType(symbolTable.lookupType(sink_Ident.name).firstToken) == Type.FILE)
			return Type.FILE;
		else
			throw new SemanticException(sink_Ident.firstToken, "Return type not file.\n");
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpression_BooleanLit(
			Expression_BooleanLit expression_BooleanLit, Object arg)
			throws Exception {
		return Type.BOOLEAN;
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpression_Ident(Expression_Ident expression_Ident,
			Object arg) throws Exception {
		if (symbolTable.lookupType(expression_Ident.name)==null) 
			throw new SemanticException(expression_Ident.firstToken, expression_Ident.name + " not declared.\n");
		expression_Ident.setType(TypeUtils.getType(symbolTable.lookupType(expression_Ident.name).firstToken));
		return expression_Ident.getType();
		//throw new UnsupportedOperationException();
	}

}
