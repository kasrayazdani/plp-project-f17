package cop5556fa17;

import java.util.ArrayList;
import java.awt.Image;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import cop5556fa17.Scanner.Kind;
import cop5556fa17.TypeUtils.Type;
import cop5556fa17.AST.ASTNode;
import cop5556fa17.AST.ASTVisitor;
import cop5556fa17.AST.Declaration;
import cop5556fa17.AST.Declaration_Image;
import cop5556fa17.AST.Declaration_SourceSink;
import cop5556fa17.AST.Declaration_Variable;
import cop5556fa17.AST.Expression;
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
import cop5556fa17.AST.Source;
import cop5556fa17.AST.Source_CommandLineParam;
import cop5556fa17.AST.Source_Ident;
import cop5556fa17.AST.Source_StringLiteral;
import cop5556fa17.AST.Statement_In;
import cop5556fa17.AST.Statement_Out;
import cop5556fa17.AST.Statement_Assign;
import cop5556fa17.image.ImageResources;
import cop5556fa17.ImageSupport;

public class CodeGenVisitor implements ASTVisitor, Opcodes {

	/**
	 * All methods and variable static.
	 */


	/**
	 * @param DEVEL
	 *            used as parameter to genPrint and genPrintTOS
	 * @param GRADE
	 *            used as parameter to genPrint and genPrintTOS
	 * @param sourceFileName
	 *            name of source file, may be null.
	 */
	public CodeGenVisitor(boolean DEVEL, boolean GRADE, String sourceFileName) {
		super();
		this.DEVEL = DEVEL;
		this.GRADE = GRADE;
		this.sourceFileName = sourceFileName;
	}

	ClassWriter cw;
	String className;
	String classDesc;
	String sourceFileName;

	MethodVisitor mv; // visitor of method currently under construction
	FieldVisitor fv;
	
	/** Indicates whether genPrint and genPrintTOS should generate code. */
	final boolean DEVEL;
	final boolean GRADE;
	


	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		className = program.name;  
		classDesc = "L" + className + ";";
		String sourceFileName = (String) arg;
		cw.visit(52, ACC_PUBLIC + ACC_SUPER, className, null, "java/lang/Object", null);
		cw.visitSource(sourceFileName, null);
		// create main method
		mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);
		// initialize
		mv.visitCode();		
		//add label before first instruction
		Label mainStart = new Label();
		mv.visitLabel(mainStart);		
		// if GRADE, generates code to add string to log
		//CodeGenUtils.genLog(GRADE, mv, "entering main");

		// visit decs and statements to add field to class
		//  and instructions to main method, respectivley
		ArrayList<ASTNode> decsAndStatements = program.decsAndStatements;
		for (ASTNode node : decsAndStatements) {
			node.visit(this, arg);
		}

		//generates code to add string to log
		//CodeGenUtils.genLog(GRADE, mv, "leaving main");
		
		//adds the required (by the JVM) return statement to main
		mv.visitInsn(RETURN);
		
		//adds label at end of code
		Label mainEnd = new Label();
		mv.visitLabel(mainEnd);
		
		//handles parameters and local variables of main. Right now, only args
		mv.visitLocalVariable("args", "[Ljava/lang/String;", null, mainStart, mainEnd, 0);
		
		//For predefined variables : X,Y,x,y,R,A,r,a
		mv.visitLocalVariable("X", "I", null, mainStart, mainEnd, 3);
		mv.visitLocalVariable("Y", "I", null, mainStart, mainEnd, 4);
		mv.visitLocalVariable("x", "I", null, mainStart, mainEnd, 1);
		mv.visitLocalVariable("y", "I", null, mainStart, mainEnd, 2);
		mv.visitLocalVariable("R", "I", null, mainStart, mainEnd, 5);
		mv.visitLocalVariable("A", "I", null, mainStart, mainEnd, 6);
		mv.visitLocalVariable("r", "I", null, mainStart, mainEnd, 7);
		mv.visitLocalVariable("a", "I", null, mainStart, mainEnd, 8);
		
		//Sets max stack size and number of local vars.
		//Because we use ClassWriter.COMPUTE_FRAMES as a parameter in the constructor,
		//asm will calculate this itself and the parameters are ignored.
		//If you have trouble with failures in this routine, it may be useful
		//to temporarily set the parameter in the ClassWriter constructor to 0.
		//The generated classfile will not be correct, but you will at least be
		//able to see what is in it.
		mv.visitMaxs(0, 0);
		
		//terminate construction of main method
		mv.visitEnd();
		
		//terminate class construction
		cw.visitEnd();

		//generate classfile as byte array and return
		return cw.toByteArray();
	}

	@Override
	public Object visitDeclaration_Variable(
			Declaration_Variable declaration_Variable, Object arg) 
			throws Exception {
		// TODO
		String fieldName = declaration_Variable.name;
		String fieldType = (declaration_Variable.firstToken.kind == Kind.KW_boolean) ? "Z" : "I";
		fv = cw.visitField(ACC_STATIC, fieldName, fieldType, null, null);
		fv.visitEnd();

		if (declaration_Variable.e != null) {
			declaration_Variable.e.visit(this, arg);
			mv.visitInsn(DUP);
			mv.visitFieldInsn(PUTSTATIC, className, declaration_Variable.name, fieldType);
		}
		
		return null;
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpression_Binary(Expression_Binary expression_Binary, Object arg) throws Exception {
		// TODO
		Type expression_Binary_type = null;
		switch (expression_Binary.op) {
		
		case OP_EQ:
		case OP_NEQ:
		case OP_GT:
		case OP_GE:
		case OP_LT:
		case OP_LE:
		case OP_AND:
		case OP_OR: {
			expression_Binary_type = Type.BOOLEAN;
			Label startLabel = new Label();
			Label endLabel = new Label();
			expression_Binary.e0.visit(this, arg);
			expression_Binary.e1.visit(this, arg);
			switch (expression_Binary.op) {
			
			case OP_AND:
				mv.visitInsn(IAND);
				break;
				
			case OP_OR:
				mv.visitInsn(IOR);
				break;
				
			case OP_EQ:
			case OP_NEQ:
			case OP_GT:
			case OP_GE:
			case OP_LT:
			case OP_LE: {
				int op = 0;
				switch (expression_Binary.op) {
				case OP_EQ: op = IF_ICMPEQ;
					break;
				case OP_NEQ: op = IF_ICMPNE;
					break;
				case OP_GT: op = IF_ICMPGT;
					break;
				case OP_GE: op = IF_ICMPGE;
					break;
				case OP_LT : op = IF_ICMPLT;
					break;
				case OP_LE : op = IF_ICMPLE;
					break;
				default:
					break;
				}
				mv.visitJumpInsn(op, startLabel);
				mv.visitInsn(ICONST_0);
				mv.visitJumpInsn(GOTO, endLabel);
				mv.visitLabel(startLabel);
				mv.visitInsn(ICONST_1);
				mv.visitLabel(endLabel);
			}break;
				
			default:
				break;
				
			}
		}break;
		
		case OP_PLUS:
		case OP_MINUS:
		case OP_TIMES:
		case OP_DIV:
		case OP_MOD: {
			expression_Binary_type = Type.INTEGER;
			expression_Binary.e0.visit(this, arg);
			expression_Binary.e1.visit(this, arg);
			switch (expression_Binary.op) {
			case OP_PLUS: mv.visitInsn(IADD);
				break;
			case OP_MINUS: mv.visitInsn(ISUB);
				break;
			case OP_TIMES: mv.visitInsn(IMUL);
				break;
			case OP_DIV: mv.visitInsn(IDIV);
				break;
			case OP_MOD: mv.visitInsn(IREM);
			default:
				break;
			}
		}break;
		
		default:
			break;
			
		}
		
		if (expression_Binary_type == null)
			throw new UnsupportedOperationException();
		
		//CodeGenUtils.genLogTOS(GRADE, mv, expression_Binary.getType());
		//CodeGenUtils.genLogTOS(GRADE, mv, expression_Binary_type);
		return null;
	}

	@Override
	public Object visitExpression_Unary(Expression_Unary expression_Unary, Object arg) throws Exception {
		// TODO 
		switch(expression_Unary.op) {
		case OP_EXCL:
			Type type = expression_Unary.getType();
			switch(type) {
			case INTEGER:
				expression_Unary.e.visit(this, arg);
				mv.visitLdcInsn(new Integer(2147483647));
				mv.visitInsn(IXOR);
				break;
			case BOOLEAN:
				expression_Unary.e.visit(this, arg);
				Label t = new Label();
				Label f = new Label();
				mv.visitInsn(ICONST_1);
				mv.visitJumpInsn(IF_ICMPEQ, t);
				mv.visitInsn(ICONST_1);
				mv.visitJumpInsn(GOTO, f);
				mv.visitLabel(t);
				mv.visitInsn(ICONST_0);
				mv.visitLabel(f);
				break;
			}break;
		
		case OP_PLUS:
		case OP_MINUS: {
			switch(expression_Unary.op) {
			case OP_PLUS:
				expression_Unary.e.visit(this, arg);
				break;
			case OP_MINUS:
				expression_Unary.e.visit(this, arg);
				mv.visitInsn(INEG);
				break;
			default:
				break;
			}
		}break;
		
		default:
			break;
			
		}
//		throw new UnsupportedOperationException();
		//CodeGenUtils.genLogTOS(GRADE, mv, expression_Unary.getType());
		return null;
	}

	// generate code to leave the two values on the stack
	@Override
	public Object visitIndex(Index index, Object arg) throws Exception {
		// TODO HW6
		if (!index.isCartesian()) {
			mv.visitVarInsn(ILOAD, 1);
			mv.visitVarInsn(ILOAD, 2);
		}
		else {
			index.e0.visit(this, arg);
			index.e1.visit(this, arg);
		}
		return null;
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpression_PixelSelector(Expression_PixelSelector expression_PixelSelector, Object arg)
			throws Exception {
		// TODO HW6
		mv.visitFieldInsn(GETSTATIC, className, expression_PixelSelector.name, ImageSupport.ImageDesc);
		expression_PixelSelector.index.visit(this, arg);
		mv.visitMethodInsn(Opcodes.INVOKESTATIC, "cop5556fa17/ImageSupport", "getPixel", 
				ImageSupport.getPixelSig, false);
		return null;
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpression_Conditional(Expression_Conditional expression_Conditional, Object arg)
			throws Exception {
		// TODO
		expression_Conditional.condition.visit(this, arg);
		Label t = new Label();
		Label f = new Label();
		mv.visitInsn(ICONST_1);
		mv.visitJumpInsn(IF_ICMPEQ, t);
		expression_Conditional.falseExpression.visit(this, arg);
		mv.visitJumpInsn(GOTO, f);
		mv.visitLabel(t);
		expression_Conditional.trueExpression.visit(this, arg);
		mv.visitLabel(f);
//		throw new UnsupportedOperationException();
//		CodeGenUtils.genLogTOS(GRADE, mv, expression_Conditional.trueExpression.getType());
//		CodeGenUtils.genLogTOS(GRADE, mv, expression_Conditional.getType());
		return null;
	}


	@Override
	public Object visitDeclaration_Image(Declaration_Image declaration_Image, Object arg) throws Exception {
		// TODO HW6
		String fieldName = declaration_Image.name;
		String fieldType = ImageSupport.ImageDesc;
		fv = cw.visitField(ACC_STATIC, fieldName, fieldType, null, null);
		fv.visitEnd();
		if (declaration_Image.source != null) {
			declaration_Image.source.visit(this, arg);
			if (declaration_Image.xSize==null && declaration_Image.ySize==null) {
				mv.visitInsn(ACONST_NULL);
				mv.visitInsn(ACONST_NULL);
			}
			else {
				declaration_Image.xSize.visit(this, arg);
				declaration_Image.ySize.visit(this, arg);
			}
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, "cop5556fa17/ImageSupport", "readImage", 
					ImageSupport.readImageSig, false);
		}
		else {
			//TODO: see description
			if (declaration_Image.xSize != null && declaration_Image.ySize != null) {
				declaration_Image.xSize.visit(this, arg);
				declaration_Image.ySize.visit(this, arg);
			}
			else {
				mv.visitIntInsn(SIPUSH, 256);
				mv.visitIntInsn(SIPUSH, 256);
			}
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, "cop5556fa17/ImageSupport", "makeImage", 
					ImageSupport.makeImageSig, false);
		}
		mv.visitFieldInsn(PUTSTATIC, className, declaration_Image.name, fieldType);
		return null;
		//throw new UnsupportedOperationException();
	}
	
  
	@Override
	public Object visitSource_StringLiteral(Source_StringLiteral source_StringLiteral, Object arg) throws Exception {
		// TODO HW6
		mv.visitLdcInsn(source_StringLiteral.fileOrUrl);
		return null;
		//throw new UnsupportedOperationException();
	}

	

	@Override
	public Object visitSource_CommandLineParam(Source_CommandLineParam source_CommandLineParam, Object arg)
			throws Exception {
		// TODO
		mv.visitVarInsn(ALOAD, 0);
		source_CommandLineParam.paramNum.visit(this, arg);
		mv.visitInsn(AALOAD);
		return null;
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitSource_Ident(Source_Ident source_Ident, Object arg) throws Exception {
		// TODO HW6
		mv.visitFieldInsn(GETSTATIC, className, source_Ident.name, "Ljava/lang/String;");
		return null;
		//throw new UnsupportedOperationException();
	}


	@Override
	public Object visitDeclaration_SourceSink(Declaration_SourceSink declaration_SourceSink, Object arg)
			throws Exception {
		// TODO HW6
		String fieldName = declaration_SourceSink.name;
		String fieldType = "Ljava/lang/String;";
		fv = cw.visitField(ACC_STATIC, fieldName, fieldType, null, null);
		fv.visitEnd();
		if (declaration_SourceSink.source != null) {
			declaration_SourceSink.source.visit(this, arg);
			mv.visitFieldInsn(PUTSTATIC, className, fieldName, fieldType);
		}
		return null;
		//throw new UnsupportedOperationException();
	}
	


	@Override
	public Object visitExpression_IntLit(Expression_IntLit expression_IntLit, Object arg) throws Exception {
		// TODO
		mv.visitLdcInsn(expression_IntLit.value);
		//throw new UnsupportedOperationException();
		//CodeGenUtils.genLogTOS(GRADE, mv, Type.INTEGER);
		return null;
	}

	@Override
	public Object visitExpression_FunctionAppWithExprArg(
			Expression_FunctionAppWithExprArg expression_FunctionAppWithExprArg, Object arg) throws Exception {
		// TODO HW6
		expression_FunctionAppWithExprArg.arg.visit(this, arg);
		switch (expression_FunctionAppWithExprArg.function) {
		case KW_abs:
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, "cop5556fa17/RuntimeFunctions", "abs", 
					RuntimeFunctions.absSig, false);
			break;
		case KW_log:
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, "cop5556fa17/RuntimeFunctions", "log", 
					RuntimeFunctions.logSig, false);
			break;
		default:
			throw new UnsupportedOperationException();
		}
		return null;
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpression_FunctionAppWithIndexArg(
			Expression_FunctionAppWithIndexArg expression_FunctionAppWithIndexArg, Object arg) throws Exception {
		// TODO HW6
		expression_FunctionAppWithIndexArg.arg.e0.visit(this, arg);
		expression_FunctionAppWithIndexArg.arg.e1.visit(this, arg);
		switch(expression_FunctionAppWithIndexArg.function) {
		case KW_cart_x:
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, "cop5556fa17/RuntimeFunctions", "cart_x", 
					RuntimeFunctions.cart_xSig, false);
			break;
		case KW_cart_y:
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, "cop5556fa17/RuntimeFunctions", "cart_y", 
					RuntimeFunctions.cart_ySig, false);
			break;
		case KW_polar_a:
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, "cop5556fa17/RuntimeFunctions", "polar_a", 
					RuntimeFunctions.polar_aSig, false);
			break;
		case KW_polar_r:
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, "cop5556fa17/RuntimeFunctions", "polar_r", 
					RuntimeFunctions.polar_rSig, false);
			break;
		default:
			throw new UnsupportedOperationException();
		}
		return null;
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpression_PredefinedName(Expression_PredefinedName expression_PredefinedName, Object arg)
			throws Exception {
		// TODO HW6
		switch(expression_PredefinedName.kind) {
		case KW_X:
			mv.visitVarInsn(ILOAD, 3);
			break;
		case KW_Y:
			mv.visitVarInsn(ILOAD, 4);
			break;
		case KW_x:
			mv.visitVarInsn(ILOAD, 1);
			break;
		case KW_y:
			mv.visitVarInsn(ILOAD, 2);
			break;
		case KW_R:
			mv.visitVarInsn(ILOAD, 5);
			break;
		case KW_A:
			mv.visitVarInsn(ILOAD, 6);
			break;
		case KW_r:
			mv.visitVarInsn(ILOAD, 1);
			break;
		case KW_a:
			mv.visitVarInsn(ILOAD, 2);
			break;
		case KW_DEF_X:
		case KW_DEF_Y:
			mv.visitLdcInsn(256);
			break;
		case KW_Z:
			mv.visitLdcInsn(16777215);
			break;
		default:
			break;
		}
		return null;
		//throw new UnsupportedOperationException();
	}

	/** For Integers and booleans, the only "sink"is the screen, so generate code to print to console.
	 * For Images, load the Image onto the stack and visit the Sink which will generate the code to handle the image.
	 */
	@Override
	public Object visitStatement_Out(Statement_Out statement_Out, Object arg) throws Exception {
		// TODO in HW5:  only INTEGER and BOOLEAN
		// TODO HW6 remaining cases
		Type dec_type = TypeUtils.getType(statement_Out.getDec().firstToken);
		if (dec_type == Type.BOOLEAN || dec_type == Type.INTEGER) {
			CodeGenUtils.genLogTOS(GRADE, mv, dec_type);
			return null;
		}
		if (dec_type == Type.IMAGE) {
			//Load the image
			mv.visitFieldInsn(GETSTATIC, className, statement_Out.name, ImageSupport.ImageDesc);
			CodeGenUtils.genLogTOS(GRADE, mv, dec_type);
			statement_Out.sink.visit(this, arg);
			return null;
		}
		throw new UnsupportedOperationException();
	}

	/**
	 * Visit source to load rhs, which will be a String, onto the stack
	 * 
	 *  In HW5, you only need to handle INTEGER and BOOLEAN
	 *  Use java.lang.Integer.parseInt or java.lang.Boolean.parseBoolean 
	 *  to convert String to actual type. 
	 *  
	 *  TODO HW6 remaining types
	 */
	@Override
	public Object visitStatement_In(Statement_In statement_In, Object arg) throws Exception {
		// TODO (see comment )
		Type type = TypeUtils.getType(statement_In.getDec().firstToken);
		statement_In.source.visit(this, arg);
		switch(type) {
		case INTEGER:
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "parseInt", "(Ljava/lang/String;)I", false);
			break;
		case BOOLEAN:
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "parseBoolean", "(Ljava/lang/String;)Z", false);
			break;
		case IMAGE:
			mv.visitInsn(ACONST_NULL);
			mv.visitInsn(ACONST_NULL);
			mv.visitMethodInsn(INVOKESTATIC, "cop5556fa17/ImageSupport", "readImage", 
					ImageSupport.readImageSig, false);
			if (((Declaration_Image)statement_In.getDec()).xSize!=null) {
				mv.visitFieldInsn(GETSTATIC, className, statement_In.name, "Ljava/awt/image/BufferedImage;");
				mv.visitMethodInsn(Opcodes.INVOKESTATIC, "cop5556fa17/ImageSupport", "getX", 
						ImageSupport.getXSig, false);
	
				mv.visitFieldInsn(GETSTATIC, className, statement_In.name, "Ljava/awt/image/BufferedImage;");
				mv.visitMethodInsn(Opcodes.INVOKESTATIC, "cop5556fa17/ImageSupport", "getY", 
						ImageSupport.getYSig, false);
				
				mv.visitMethodInsn(INVOKESTATIC, "cop5556fa17/ImageSupport", "resize", 
						"(Ljava/awt/image/BufferedImage;II)Ljava/awt/image/BufferedImage;", false);
			}
		default:
			break;
		}
		//CodeGenUtils.genPrint(DEVEL, mv, "\n statement_In: "+statement_In.name+"=");
		String intORboolORImage = (type==Type.INTEGER || type==Type.BOOLEAN) 
				                  ? ((type==Type.INTEGER) ? "I" : "Z") 
				                  : ImageSupport.ImageDesc;
		mv.visitInsn(DUP);
		mv.visitFieldInsn(PUTSTATIC, className, statement_In.name, intORboolORImage);
		//CodeGenUtils.genPrintTOS(GRADE, mv, type);
		return null;
	}

	
	/**
	 * In HW5, only handle INTEGER and BOOLEAN types.
	 */
	@Override
	public Object visitStatement_Assign(Statement_Assign statement_Assign, Object arg) throws Exception {
		// TODO Auto-generated method stub
		Type type = TypeUtils.getType(statement_Assign.lhs.getDeclaration().firstToken);
		if (type == Type.INTEGER || type == Type.BOOLEAN) {
			statement_Assign.e.visit(this, arg);
			statement_Assign.lhs.visit(this, arg);
			statement_Assign.setCartesian(statement_Assign.lhs.isCartesian());
			return null;
		}
		if (type == Type.IMAGE) {	
			
			mv.visitFieldInsn(GETSTATIC, className, statement_Assign.lhs.name, "Ljava/awt/image/BufferedImage;");
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, "cop5556fa17/ImageSupport", "getX", ImageSupport.getXSig, false);
			mv.visitVarInsn(ISTORE, 3);
			
			mv.visitFieldInsn(GETSTATIC, className, statement_Assign.lhs.name, "Ljava/awt/image/BufferedImage;");
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, "cop5556fa17/ImageSupport", "getY", ImageSupport.getYSig, false);
			mv.visitVarInsn(ISTORE, 4);
			
			// Looping
			mv.visitInsn(ICONST_0);
			mv.visitVarInsn(ISTORE, 1);
			Label l1 = new Label();
			mv.visitLabel(l1);
			Label l2 = new Label();
			mv.visitJumpInsn(GOTO, l2);
			Label l3 = new Label();
			mv.visitLabel(l3);
			mv.visitFrame(Opcodes.F_APPEND,1, new Object[] {Opcodes.INTEGER}, 0, null);
			mv.visitInsn(ICONST_0);
			mv.visitVarInsn(ISTORE, 2);
			Label l4 = new Label();
			mv.visitLabel(l4);
			Label l5 = new Label();
			mv.visitJumpInsn(GOTO, l5);
			Label l6 = new Label();
			mv.visitLabel(l6);
			mv.visitFrame(Opcodes.F_APPEND,1, new Object[] {Opcodes.INTEGER}, 0, null);
			
			// visit lhs
			statement_Assign.e.visit(this, arg);
			statement_Assign.lhs.visit(this, arg);
			
			Label l7 = new Label();
			mv.visitLabel(l7);
			mv.visitIincInsn(2, 1);
			mv.visitLabel(l5);
			mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
			
			mv.visitVarInsn(ILOAD, 2); //y
			mv.visitVarInsn(ILOAD, 4); //Y
//			mv.visitFieldInsn(GETSTATIC, className, statement_Assign.lhs.name, "Ljava/awt/image/BufferedImage;");
//			mv.visitMethodInsn(Opcodes.INVOKESTATIC, "cop5556fa17/ImageSupport", "getY", ImageSupport.getYSig, false);
			mv.visitJumpInsn(IF_ICMPLT, l6);
			
			Label l8 = new Label();
			mv.visitLabel(l8);
			mv.visitIincInsn(1, 1);
			mv.visitLabel(l2);
			mv.visitFrame(Opcodes.F_CHOP,1, null, 0, null);
			
			mv.visitVarInsn(ILOAD, 1); //x
			mv.visitVarInsn(ILOAD, 3); //X
//			mv.visitFieldInsn(GETSTATIC, className, statement_Assign.lhs.name, "Ljava/awt/image/BufferedImage;");
//			mv.visitMethodInsn(Opcodes.INVOKESTATIC, "cop5556fa17/ImageSupport", "getX", 	ImageSupport.getXSig, false);
			mv.visitJumpInsn(IF_ICMPLT, l3);

			return null;
		}
		throw new UnsupportedOperationException();
	}

	/**
	 * In HW5, only handle INTEGER and BOOLEAN types.
	 */
	@Override
	public Object visitLHS(LHS lhs, Object arg) throws Exception {
		//TODO  (see comment)
		Type type = TypeUtils.getType(lhs.getDeclaration().firstToken);
		if (type == Type.INTEGER || type == Type.BOOLEAN) {
			mv.visitInsn(DUP);
			String intORbool = type==Type.INTEGER ? "I" : "Z";
			mv.visitFieldInsn(PUTSTATIC, className, lhs.name, intORbool);
			return null;
		}
		else if (type == Type.IMAGE) {
			mv.visitFieldInsn(GETSTATIC, className, lhs.name, ImageSupport.ImageDesc);
			//mv.visitVarInsn(ISTORE, 3); //x
			//mv.visitVarInsn(ISTORE, 4); //y
			lhs.index.visit(this, arg);
			mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "setPixel", 
					ImageSupport.setPixelSig, false);
			return null;
		}
		throw new UnsupportedOperationException();
	}
	

	@Override
	public Object visitSink_SCREEN(Sink_SCREEN sink_SCREEN, Object arg) throws Exception {
		//TODO HW6
		mv.visitMethodInsn(Opcodes.INVOKESTATIC, "cop5556fa17/ImageSupport", "makeFrame", 
				ImageSupport.makeFrameSig, false);
		mv.visitInsn(POP);
		return null;
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitSink_Ident(Sink_Ident sink_Ident, Object arg) throws Exception {
		//TODO HW6
		mv.visitFieldInsn(GETSTATIC, className, sink_Ident.name, ImageSupport.ImageDesc);
		mv.visitLdcInsn(ImageResources.imageFile2);
		mv.visitMethodInsn(Opcodes.INVOKESTATIC, "cop5556fa17/ImageSupport", "write", 
				ImageSupport.writeSig, false);
		return null;
		//throw new UnsupportedOperationException();
	}

	@Override
	public Object visitExpression_BooleanLit(Expression_BooleanLit expression_BooleanLit, Object arg) throws Exception {
		//TODO
		mv.visitLdcInsn(expression_BooleanLit.value);
		//throw new UnsupportedOperationException();
		//CodeGenUtils.genLogTOS(GRADE, mv, Type.BOOLEAN);
		return null;
	}

	@Override
	public Object visitExpression_Ident(Expression_Ident expression_Ident,
			Object arg) throws Exception {
		//TODO
		String fieldType = (expression_Ident.getType()==Type.INTEGER) ? "I" : "Z";
		mv.visitFieldInsn(GETSTATIC, className, expression_Ident.name, fieldType);
//		throw new UnsupportedOperationException();
		//CodeGenUtils.genLogTOS(GRADE, mv, expression_Ident.getType());
		return null;
	}


}
