package edu.binghamton.cs571;

import edu.binghamton.cs571.Token.Kind;

public class UglyRegexpParser {

	Token _lookahead;
	Scanner _scanner;

	UglyRegexpParser(Scanner scanner) {
		_scanner = scanner;
		_lookahead = _scanner.nextToken();
	}


	/** parse a sequence of lines containing ugly-regexp's; for each
	 *  ugly regexp print out the corresponding standard regexp.
	 *  If there is an error, print diagnostic and continue with
	 *  next line.
	 */
	public void parse() {
		while (_lookahead.kind != Token.Kind.EOF) {
			try {
				String out = uglyRegexp();
				if (check(Token.Kind.NL)) System.out.println(out);
				match(Token.Kind.NL);
			}
			catch (ParseException e) {
				System.err.println(e.getMessage());
				while (_lookahead.kind != Token.Kind.NL) {
					_lookahead = _scanner.nextToken();
				}
				_lookahead = _scanner.nextToken();	
			}
		}
	}

	/** Return standard syntax regexp corresponding to ugly-regexp
	 *  read from _scanner.
	 */
	//IMPLEMENT THIS FUNCTION and any necessary functions it may call.
	private String uglyRegexp() {
		String valuesoFar= null;
		valuesoFar=expr(valuesoFar); 	

		return valuesoFar; 
	}
	/** expr
	 *      : term exprRest
	 *      ;
	 * 
	 * @param valuesoFar
	 * @return
	 */
	private String expr(String valuesoFar) {
		valuesoFar= term(valuesoFar);
		valuesoFar= exprRest(valuesoFar);  
		return valuesoFar;
	}

	/**exprRest
	 * 		 : "." term exprRest
	 *		 ; 
	 * @param valuesoFar
	 * @return
	 */
	private String exprRest(String valuesoFar){
		if(check(_lookahead.kind,new String(".") )){
			match(_lookahead.kind, new String("."));
			String value = "("+valuesoFar+ term(null)+")";
			value= exprRest(value);
			return value;
		}else{
			return valuesoFar;
		}	
	}

	/**    term
	 *   	   :  simple termRest 
	 *         ;     
	 * @param valuesoFar
	 * @return
	 */

	private String term(String valuesoFar) {
		// TODO Auto-generated method stub
		valuesoFar= simple(valuesoFar);	
		valuesoFar=termRest(valuesoFar);
		return valuesoFar;
	}	

	/** termRest
	 * 		 : "+" simple termRest
	 * 
	 * 
	 * @param valuesoFar
	 * @return
	 */

	private String termRest(String valuesoFar) {
		if(check(_lookahead.kind, new String("+"))){
			match(_lookahead.kind, new String("+"));	
			valuesoFar="("+ valuesoFar+"|"+simple(null)+")";
			valuesoFar=termRest(valuesoFar); 		
		}
		return valuesoFar;
	}	

	/**  simple 
	 *         : "*" simple
	 *         |  "(" expr ")"
	 *         |  chars "(" idList ")"
	 *         ;  
	 * @param valuesoFar
	 * @return
	 */
	private String simple(String valuesoFar) {
		if(check(_lookahead.kind, new String("*"))){
			match(_lookahead.kind, new String("*"));
			valuesoFar=simple(valuesoFar)+"*";
		}
		else if(check(_lookahead.kind, new String("("))){
			match(_lookahead.kind, new String("("));
			valuesoFar="("+expr(valuesoFar);
			match(_lookahead.kind, new String(")"));
			valuesoFar=valuesoFar+")";  
		}
		else if(check(Token.Kind.CHARS )){
			match(Token.Kind.CHARS);
			match(_lookahead.kind, new String("("));
			valuesoFar= idList(valuesoFar);
			match(_lookahead.kind, new String(")"));
			valuesoFar="["+valuesoFar+"]";
		}
		else{
			String expected = (_lookahead.lexeme == null) ? _lookahead.kind.toString() : _lookahead.lexeme;
			String message = String.format("%s: syntax error at '%s', %s",
					_lookahead.coords, expected, (String)(_lookahead.coords.colN==0 ? " regex should start with '*' or  '(' or 'chars'":
					" expecting '*' or  '(' or 'chars'  as next character'")

			);
			throw new ParseException(message);

		}
		return valuesoFar;
	}

	/** idList
	 *        : ID idListTail;
	 *        ; 
	 * 
	 * @param valuesoFar
	 * @return
	 */
	private String idList(String valuesoFar){
		if(_lookahead.kind==Token.Kind.CHAR){

			String char1 = _lookahead.lexeme;
			match(Token.Kind.CHAR);

			if(valuesoFar!=null){
				valuesoFar=valuesoFar+quote(char1);
			}
			else{
				valuesoFar=quote(char1);
			}
			return idlistTail(valuesoFar);
		}
		else{
			return valuesoFar;	
		}

	}
	/**idListTail
	 *          : "," ID idListTail
	 *          ;     
	 * 
	 * @param valuesoFar
	 * @return
	 */
	private String idlistTail(String valuesoFar) {
		if(check(_lookahead.kind,new String(","))){
			match(_lookahead.kind, new String(","));
			String value= _lookahead.lexeme;
			match(Kind.CHAR);
			valuesoFar=valuesoFar+quote(value);
			valuesoFar= idlistTail(valuesoFar);

		}else{
			return valuesoFar;
		}
		// TODO Auto-generated method stub
		return valuesoFar;
	}


	/** Return s with first char escaped using a '\' if it is
	 * non-alphanumeric.
	 */
	private static String quote(String s) {

		return (Character.isLetterOrDigit(s.charAt(0))) ? s : "\\" + s;
	}
	
	/** Return true if _lookahead.kind is equal to kind. */
	private boolean check(Token.Kind kind) {
		return check(kind, null);
	}

	/** Return true if lookahead kind and lexeme are equal to
	 *  corresponding args.  Note that if lexeme is null, then it is not
	 *  used in the match.
	 */
	private boolean check(Token.Kind kind, String lexeme) {
		return (_lookahead.kind == kind &&
				(lexeme == null || _lookahead.lexeme.equals(lexeme)));
	}

	/** If lookahead kind is equal to kind, then set lookahead to next
	 *  token; else throw a ParseException.
	 */
	private void match(Token.Kind kind) {
		match(kind, null);
	}

	/** If lookahead kind and lexeme are not equal to corresponding
	 *  args, then set lookahead to next token; else throw a
	 *  ParseException.  Note that if lexeme is null, then it is
	 *  not used in the match.
	 */
	private void match(Token.Kind kind, String lexeme) {
		if (check(kind, lexeme)) {
			_lookahead = _scanner.nextToken();
		}
		else {
			String expected = (lexeme == null) ? kind.toString() : lexeme;
			String message = String.format("%s: syntax error at '%s', expecting '%s'",
					_lookahead.coords, _lookahead.lexeme,
					expected);
			throw new ParseException(message);
		}
	}

	private static class ParseException extends RuntimeException {
		ParseException(String message) {
			super(message);
		}
	}


	/** main program: parses and translates ugly-regexp's contained in
	 *  the file specified by it's single command-line argument.
	 */
	public static void main(String[] args) {
		if (args.length != 1) {
			System.err.format("usage: java %s chars" +
					" FILENAME\n",
					UglyRegexpParser.class.getName());
			System.exit(1);
		}
		Scanner scanner =
			("-".equals(args[0])) ? new Scanner() : new Scanner(args[0]);
			(new UglyRegexpParser(scanner)).parse();
	}


}
