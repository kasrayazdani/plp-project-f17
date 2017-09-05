/* *
 * Scanner for the class project in COP5556 Programming Language Principles 
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


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import com.sun.xml.internal.ws.util.xml.XMLReaderComposite.State;

public class Scanner {
	
	@SuppressWarnings("serial")
	public static class LexicalException extends Exception {
		
		int pos;

		public LexicalException(String message, int pos) {
			super(message);
			this.pos = pos;
		}
		
		public int getPos() { return pos; }

	}
	
	@SuppressWarnings("serial")
	public static class InvalidIntLitException extends Exception {
		
		String s;

		public InvalidIntLitException(String message, String s) {
			super(message);
			this.s = s;
		}
		
		public String getString() { return s; }

	}

	public static enum Kind {
		IDENTIFIER, INTEGER_LITERAL, BOOLEAN_LITERAL, STRING_LITERAL, 
		KW_x/* x */, KW_X/* X */, KW_y/* y */, KW_Y/* Y */, KW_r/* r */, KW_R/* R */, KW_a/* a */, 
		KW_A/* A */, KW_Z/* Z */, KW_DEF_X/* DEF_X */, KW_DEF_Y/* DEF_Y */, KW_SCREEN/* SCREEN */, 
		KW_cart_x/* cart_x */, KW_cart_y/* cart_y */, KW_polar_a/* polar_a */, KW_polar_r/* polar_r */, 
		KW_abs/* abs */, KW_sin/* sin */, KW_cos/* cos */, KW_atan/* atan */, KW_log/* log */, 
		KW_image/* image */,  KW_int/* int */, 
		KW_boolean/* boolean */, KW_url/* url */, KW_file/* file */, OP_ASSIGN/* = */, OP_GT/* > */, OP_LT/* < */, 
		OP_EXCL/* ! */, OP_Q/* ? */, OP_COLON/* : */, OP_EQ/* == */, OP_NEQ/* != */, OP_GE/* >= */, OP_LE/* <= */, 
		OP_AND/* & */, OP_OR/* | */, OP_PLUS/* + */, OP_MINUS/* - */, OP_TIMES/* * */, OP_DIV/* / */, OP_MOD/* % */, 
		OP_POWER/* ** */, OP_AT/* @ */, OP_RARROW/* -> */, OP_LARROW/* <- */, LPAREN/* ( */, RPAREN/* ) */, 
		LSQUARE/* [ */, RSQUARE/* ] */, SEMI/* ; */, COMMA/* , */, EOF;
	}
	
	public static enum State {
		START,
		IDENTIFIER,
		DIGIT,
		STRING_LITERAL,
	}
	
	/*
	 * This part of code is generated in python!
	 * keywords = "x | X | y | Y | r | R | a | A | Z | DEF_X | DEF_Y | SCREEN | cart_x | cart_y
	 *  | polar_a | polar_r | abs | sin | cos | atan | log | image | int | boolean | url | file"
	 *  for i in keywords.split(' | '):
	 *  	print("reservedWords.put(\"{}\", Kind.KW_{});".format(i,i))
	 * */
	public static HashMap<String, Kind> reservedWords = new HashMap<>();
	public void populate_reservedWords() {
		reservedWords.put("x", Kind.KW_x);
		reservedWords.put("X", Kind.KW_X);
		reservedWords.put("y", Kind.KW_y);
		reservedWords.put("Y", Kind.KW_Y);
		reservedWords.put("r", Kind.KW_r);
		reservedWords.put("R", Kind.KW_R);
		reservedWords.put("a", Kind.KW_a);
		reservedWords.put("A", Kind.KW_A);
		reservedWords.put("Z", Kind.KW_Z);
		reservedWords.put("DEF_X", Kind.KW_DEF_X);
		reservedWords.put("DEF_Y", Kind.KW_DEF_Y);
		reservedWords.put("SCREEN", Kind.KW_SCREEN);
		reservedWords.put("cart_x", Kind.KW_cart_x);
		reservedWords.put("cart_y", Kind.KW_cart_y);
		reservedWords.put("polar_a", Kind.KW_polar_a);
		reservedWords.put("polar_r", Kind.KW_polar_r);
		reservedWords.put("abs", Kind.KW_abs);
		reservedWords.put("sin", Kind.KW_sin);
		reservedWords.put("cos", Kind.KW_cos);
		reservedWords.put("atan", Kind.KW_atan);
		reservedWords.put("log", Kind.KW_log);
		reservedWords.put("image", Kind.KW_image);
		reservedWords.put("int", Kind.KW_int);
		reservedWords.put("boolean", Kind.KW_boolean);
		reservedWords.put("url", Kind.KW_url);
		reservedWords.put("file", Kind.KW_file);
	}
	
	public static int g_line, g_posInLine;
	public int skipWhiteSpaces(int pos) {
		while (pos < chars.length) {
			if (chars[pos] == '/') {
				if (chars[pos+1] == '/') {
					pos = pos+2;
					while (chars[pos]!='\n')
						pos++;
				}
				else
					break;
			}
			
			/* New Line */
			else if (chars[pos]=='\n') {
				pos++;
				g_line++;
				g_posInLine = 1;
			}
			else if (chars[pos]=='\r') {
				pos = pos++;
			}
			
			/* Space */
			else if (Character.isWhitespace(chars[pos])) {
				pos++;
				g_posInLine++;
			}
			else
				break;
		}
		return pos;
	}
	
	/** Class to represent Tokens. 
	 * 
	 * This is defined as a (non-static) inner class
	 * which means that each Token instance is associated with a specific 
	 * Scanner instance.  We use this when some token methods access the
	 * chars array in the associated Scanner.
	 * 
	 * 
	 * @author Beverly Sanders
	 *
	 */
	public class Token {
		public final Kind kind;
		public final int pos;
		public final int length;
		public final int line;
		public final int pos_in_line;

		public Token(Kind kind, int pos, int length, int line, int pos_in_line) {
			super();
			this.kind = kind;
			this.pos = pos;
			this.length = length;
			this.line = line;
			this.pos_in_line = pos_in_line;
		}

		public String getText() {
			if (kind == Kind.STRING_LITERAL) {
				return chars2String(chars, pos, length);
			}
			else return String.copyValueOf(chars, pos, length);
		}

		/**
		 * To get the text of a StringLiteral, we need to remove the
		 * enclosing " characters and convert escaped characters to
		 * the represented character.  For example the two characters \ t
		 * in the char array should be converted to a single tab character in
		 * the returned String
		 * 
		 * @param chars
		 * @param pos
		 * @param length
		 * @return
		 */
		private String chars2String(char[] chars, int pos, int length) {
			StringBuilder sb = new StringBuilder();
			for (int i = pos + 1; i < pos + length - 1; ++i) {// omit initial and final "
				char ch = chars[i];
				if (ch == '\\') { // handle escape
					i++;
					ch = chars[i];
					switch (ch) {
					case 'b':
						sb.append('\b');
						break;
					case 't':
						sb.append('\t');
						break;
					case 'f':
						sb.append('\f');
						break;
					case 'r':
						sb.append('\r'); //for completeness, line termination chars not allowed in String literals
						break;
					case 'n':
						sb.append('\n'); //for completeness, line termination chars not allowed in String literals
						break;
					case '\"':
						sb.append('\"');
						break;
					case '\'':
						sb.append('\'');
						break;
					case '\\':
						sb.append('\\');
						break;
					default:
						assert false;
						break;
					}
				} else {
					sb.append(ch);
				}
			}
			return sb.toString();
		}

		/**
		 * precondition:  This Token is an INTEGER_LITERAL
		 * 
		 * @returns the integer value represented by the token
		 */
		public int intVal() {
			assert kind == Kind.INTEGER_LITERAL;
			return Integer.valueOf(String.copyValueOf(chars, pos, length));
		}

		public String toString() {
			return "[" + kind + "," + String.copyValueOf(chars, pos, length)  + "," + pos + "," + length + "," + line + ","
					+ pos_in_line + "]";
		}

		/** 
		 * Since we overrode equals, we need to override hashCode.
		 * https://docs.oracle.com/javase/8/docs/api/java/lang/Object.html#equals-java.lang.Object-
		 * 
		 * Both the equals and hashCode method were generated by eclipse
		 * 
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((kind == null) ? 0 : kind.hashCode());
			result = prime * result + length;
			result = prime * result + line;
			result = prime * result + pos;
			result = prime * result + pos_in_line;
			return result;
		}

		/**
		 * Override equals method to return true if other object
		 * is the same class and all fields are equal.
		 * 
		 * Overriding this creates an obligation to override hashCode.
		 * 
		 * Both hashCode and equals were generated by eclipse.
		 * 
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Token other = (Token) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (kind != other.kind)
				return false;
			if (length != other.length)
				return false;
			if (line != other.line)
				return false;
			if (pos != other.pos)
				return false;
			if (pos_in_line != other.pos_in_line)
				return false;
			return true;
		}

		/**
		 * used in equals to get the Scanner object this Token is 
		 * associated with.
		 * @return
		 */
		private Scanner getOuterType() {
			return Scanner.this;
		}

	}

	/** 
	 * Extra character added to the end of the input characters to simplify the
	 * Scanner.  
	 */
	static final char EOFchar = 0;
	
	/**
	 * The list of tokens created by the scan method.
	 */
	final ArrayList<Token> tokens;
	
	/**
	 * An array of characters representing the input.  These are the characters
	 * from the input string plus and additional EOFchar at the end.
	 */
	final char[] chars;  



	
	/**
	 * position of the next token to be returned by a call to nextToken
	 */
	private int nextTokenPos = 0;

	Scanner(String inputString) {
		int numChars = inputString.length();
		this.chars = Arrays.copyOf(inputString.toCharArray(), numChars + 1); // input string terminated with null char
		chars[numChars] = EOFchar;
		tokens = new ArrayList<Token>();
		populate_reservedWords();
		g_line = 1;
		g_posInLine = 1;
	}


	/**
	 * Method to scan the input and create a list of Tokens.
	 * 
	 * If an error is encountered during scanning, throw a LexicalException.
	 * 
	 * @return
	 * @throws LexicalException
	 */
	public Scanner scan() throws LexicalException {
		/* TODO  Replace this with a correct and complete implementation!!! */
		int pos = 0;
		int line = 1;
		int posInLine = 1;
		State state = State.START;
		char ch;
		System.out.println("chars.length = "+chars.length);
		while (pos < chars.length) {
			//System.out.println(pos);
			switch(state) {
			case START:
				pos = skipWhiteSpaces(pos);
				if (pos < chars.length) {
					ch = chars[pos];
					switch(ch) {
					case EOFchar:
						tokens.add(new Token(Kind.EOF, pos, 0, g_line, g_posInLine));
						pos++;
						break;
					
					/* Seperators */
					case ';':
						tokens.add(new Token(Kind.SEMI, pos, 1, g_line, g_posInLine));
						pos++;
						g_posInLine++;
						break;
						
					case '(':
						tokens.add(new Token(Kind.LPAREN, pos, 1, g_line, g_posInLine));
						pos++;
						g_posInLine++;
						break;
						
					case ')':
						tokens.add(new Token(Kind.RPAREN, pos, 1, g_line, g_posInLine));
						pos++;
						g_posInLine++;
						break;
						
					case '[':
						tokens.add(new Token(Kind.LSQUARE, pos, 1, g_line, g_posInLine));
						pos++;
						g_posInLine++;
						break;
						
					case ']':
						tokens.add(new Token(Kind.RSQUARE, pos, 1, g_line, g_posInLine));
						pos++;
						g_posInLine++;
						break;
						
					case ',':
						tokens.add(new Token(Kind.COMMA, pos, 1, g_line, g_posInLine));
						pos++;
						g_posInLine++;
						break;
						
					/* Operators */
					case '=':
						if (chars[pos+1] == '=') { /* == */
							tokens.add(new Token(Kind.OP_EQ, pos, 2, g_line, g_posInLine));
							pos = pos+2;
							g_posInLine = g_posInLine+2;
						}
						else { /* = */
							tokens.add(new Token(Kind.OP_ASSIGN, pos, 1, g_line, g_posInLine));
							pos++;
							g_posInLine++;
							}
						break;
						
					case '>':
						if (chars[pos+1] == '=') { /* >= */
							tokens.add(new Token(Kind.OP_GE, pos, 2, g_line, g_posInLine));
							pos = pos+2;
							g_posInLine = g_posInLine+2;
						}
						else { /* > */
							tokens.add(new Token(Kind.OP_GT, pos, 1, g_line, g_posInLine));
							pos++;
							g_posInLine++;
						}
						break;
						
					case '<':
						if (chars[pos+1] == '=') { /* <= */
							tokens.add(new Token(Kind.OP_LE, pos, 2, g_line, g_posInLine));
							pos = pos+2;
							g_posInLine = g_posInLine+2;
						}
						else if (chars[pos+1] == '-') { /* <- */
							tokens.add(new Token(Kind.OP_LARROW, pos, 2, g_line, g_posInLine));
							pos = pos+2;
							g_posInLine = g_posInLine+2;
						}
						else { /* < */
							tokens.add(new Token(Kind.OP_LT, pos, 1, g_line, g_posInLine));
							pos++;
							g_posInLine++;
						}
						break;
					
					case '!':
						if (chars[pos+1] == '=') { /* != */
							tokens.add(new Token(Kind.OP_NEQ, pos, 2, g_line, g_posInLine));
							pos = pos+2;
							g_posInLine = g_posInLine+2;
						}
						else { /* ! */
							tokens.add(new Token(Kind.OP_EXCL, pos, 1, g_line, g_posInLine));
							pos++;
							g_posInLine++;
						}
						break;
						
					case '?':
						tokens.add(new Token(Kind.OP_Q, pos, 1, g_line, g_posInLine));
						pos++;
						g_posInLine++;
						break;
						
					case ':':
						tokens.add(new Token(Kind.OP_COLON, pos, 1, g_line, g_posInLine));
						pos++;
						g_posInLine++;
						break;
						
					case '&':
						tokens.add(new Token(Kind.OP_AND, pos, 1, g_line, g_posInLine));
						pos++;
						g_posInLine++;
						break;
						
					case '|':
						tokens.add(new Token(Kind.OP_OR, pos, 1, g_line, g_posInLine));
						pos++;
						g_posInLine++;
						break;
						
					case '+':
						tokens.add(new Token(Kind.OP_PLUS, pos, 1, g_line, g_posInLine));
						pos++;
						g_posInLine++;
						break;
						
					case '-':
						if (chars[pos+1] == '>') { /* -> */
							tokens.add(new Token(Kind.OP_RARROW, pos, 2, g_line, g_posInLine));
							pos = pos+2;
							g_posInLine = g_posInLine+2;
						}
						else { /* - */
							tokens.add(new Token(Kind.OP_MINUS, pos, 1, g_line, g_posInLine));
							pos++;
							g_posInLine++;
						}
						break;
						
					case '*':
						if (chars[pos+1] == '*') {/* ** */
							tokens.add(new Token(Kind.OP_POWER, pos, 2, g_line, g_posInLine));
							pos = pos+2;
							g_posInLine = g_posInLine+2;
						}
						else { /* * */
							tokens.add(new Token(Kind.OP_TIMES, pos, 1, g_line, g_posInLine));
							pos++;
							g_posInLine++;
						}
						break;
						
					case '/':
						tokens.add(new Token(Kind.OP_DIV, pos, 1, g_line, g_posInLine));
						pos++;
						g_posInLine++;
						break;
						
					case '%':
						tokens.add(new Token(Kind.OP_MOD, pos, 1, g_line, g_posInLine));
						pos++;
						g_posInLine++;
						break;
					
					case '@':
						tokens.add(new Token(Kind.OP_AT, pos, 1, g_line, g_posInLine));
						pos++;
						g_posInLine++;
						break;
						
					default:
						if (Character.isJavaIdentifierStart(chars[pos])) {
							state = State.IDENTIFIER;
							pos++;
							g_posInLine++;
						}
						else if (Character.isDigit(chars[pos])) {
							if (chars[pos]=='0')
								tokens.add(new Token(Kind.INTEGER_LITERAL, pos, 1, g_line, g_posInLine));
							else
								state = State.DIGIT;
							pos++;
							g_posInLine++;
						}
						else if (chars[pos]=='\"') {
							state = State.STRING_LITERAL;
							pos ++;
							g_posInLine++;
						}
						else
							throw new LexicalException("Unknown Symbol", pos);
					}
				}
				break;
			
			case STRING_LITERAL:
				int strLit_length = 1;
				while (pos<chars.length) {
					if (chars[pos]=='\"') {	
						strLit_length++;
						pos++;
						g_posInLine++;
						break;
					}
					if (chars[pos]==EOFchar)
						throw new LexicalException("Reached End of file! Missing '\"'\n", pos);
					strLit_length++;
					pos++;
					g_posInLine++;
				}
				
				Token string_literal = new Token(Kind.STRING_LITERAL, pos-strLit_length, strLit_length, g_line, g_posInLine-strLit_length);
				tokens.add(string_literal);
				state = State.START;
				break;
				
			case IDENTIFIER:
				int length = 1;
				while (pos<chars.length && Character.isJavaIdentifierPart(chars[pos])) {
					length++;
					pos++;
					g_posInLine++;
				}
				Token token = new Token(Kind.IDENTIFIER, pos-length, length, g_line, g_posInLine-length);
				if (reservedWords.containsKey(token.getText()))
					tokens.add(new Token(reservedWords.get(token.getText()), pos-length, length, g_line, g_posInLine-length));

				else if (token.getText().equals("true") || token.getText().equals("false"))
					tokens.add(new Token(Kind.BOOLEAN_LITERAL, pos-length, length, g_line, g_posInLine-length));
				
				else
					tokens.add(token);
				
				state = State.START;
				break;
			
			case DIGIT:
				int num_digits = 1;
				while (pos<chars.length && Character.isDigit(chars[pos])) {
					num_digits++;
					pos++;
					g_posInLine++;
				}
				Token integer_literal = new Token(Kind.INTEGER_LITERAL, pos-num_digits, num_digits, g_line, g_posInLine-num_digits);
				try {
					Integer.parseInt(integer_literal.getText());
					tokens.add(integer_literal);
				}
				catch (NumberFormatException e) {
					throw new LexicalException("Integer Overflow in line " + g_line + " at column " + (g_posInLine-num_digits) + '\n', (g_posInLine-num_digits));
				}
				state = State.START;
				break;
			
			default:
				assert false;
			}
		}
		return this;

	}


	/**
	 * Returns true if the internal interator has more Tokens
	 * 
	 * @return
	 */
	public boolean hasTokens() {
		return nextTokenPos < tokens.size();
	}

	/**
	 * Returns the next Token and updates the internal iterator so that
	 * the next call to nextToken will return the next token in the list.
	 * 
	 * It is the callers responsibility to ensure that there is another Token.
	 * 
	 * Precondition:  hasTokens()
	 * @return
	 */
	public Token nextToken() {
		return tokens.get(nextTokenPos++);
	}
	
	/**
	 * Returns the next Token, but does not update the internal iterator.
	 * This means that the next call to nextToken or peek will return the
	 * same Token as returned by this methods.
	 * 
	 * It is the callers responsibility to ensure that there is another Token.
	 * 
	 * Precondition:  hasTokens()
	 * 
	 * @return next Token.
	 */
	public Token peek() {
		return tokens.get(nextTokenPos);
	}
	
	
	/**
	 * Resets the internal iterator so that the next call to peek or nextToken
	 * will return the first Token.
	 */
	public void reset() {
		nextTokenPos = 0;
	}

	/**
	 * Returns a String representation of the list of Tokens 
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("Tokens:\n");
		for (int i = 0; i < tokens.size(); i++) {
			sb.append(tokens.get(i)).append('\n');
		}
		return sb.toString();
	}

}
