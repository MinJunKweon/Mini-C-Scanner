package com.minjunkweon;

/**
 * Mini C Scanner에서 사용할 Token의 Model 클래스
 * Created by kweonminjun on 2016. 5. 13..
 */
public class Token {
    /**
     * Mini C Scanner 객체에서 인식한 Token의 Symbol 타입 (대분류)
     */
    public enum SymbolType {
        Operator, IDorKeyword, Digit, NULL
    }

    /**
     * Token의 키워드나 명칭, 연산자를 식별하기 위한 Symbol (소분류)
     */
    public enum TokenSymbol {
        NULL,
        Not, NotEqu, Mod, ModAssign, ID, Number,
        And, LParen, RParen, Mul, MulAssign, Plus,
        Increase, AddAssign, Comma, Minus, Decrease, SubAssign,
        Div, DivAssign, Semicolon, Less, Lesser, Assign,
        Equal, Great, Greater, LBracket, RBracket, EOF,
        Const, Else, If, Int, Return, Void, While,
        LBrace, Or, RBrace
    }

    private TokenSymbol symbol; // Token이 가진 Symbol (Symbol에 상응하는 정수를 추출해낼 수 있다)
    private String val; // 명칭 혹은 숫자일 경우 그 값을 저장
    private String tokenString; // 인식한 토큰의 원시 String

    /**
     * 생성자
     * 각각의 멤버변수들을 NULL로 초기화한다.
     */
    public Token() {
        symbol = TokenSymbol.NULL;
        val = "0";
        tokenString = "NULL";
    }

    /**
     * 키워드나 명칭을 입력받았을 때, 그 String이 키워드인지 명칭인지 구분하는 메소드
     * 키워드라면 어떤 키워드인지 Symbol을 할당
     *
     * @param token - Symbol을 구분할 토큰 String
     * @return 구분된 토큰 Symbol
     */
    private TokenSymbol getIDorKeywordSymbol(String token) {
        switch (token) {
            // Keyword
            case "const":   return TokenSymbol.Const;
            case "else":    return TokenSymbol.Else;
            case "if":      return TokenSymbol.If;
            case "int":     return TokenSymbol.Int;
            case "return":  return TokenSymbol.Return;
            case "void":    return TokenSymbol.Void;
            case "while":   return TokenSymbol.While;

            // ID
            default:
                return TokenSymbol.ID;
        }
    }

    /**
     * 입력받은 토큰 String이 연산자라면 어떤 연산자인지 구분하기 위한 메소드
     *
     * @param token - Symbol을 구분할 토큰 String
     * @return 구분된 토큰 Symbol (연산자)
     */
    private TokenSymbol getOperatorSymbol(String token) {
        switch (token) {
            case "!":   return TokenSymbol.Not;
            case "!=":  return TokenSymbol.NotEqu;
            case "%":   return TokenSymbol.Mod;
            case "%=":  return TokenSymbol.ModAssign;
            case "&&":  return TokenSymbol.And;
            case "(":   return TokenSymbol.LParen;
            case ")":   return TokenSymbol.RParen;
            case "*":   return TokenSymbol.Mul;
            case "*=":  return TokenSymbol.MulAssign;
            case "+":   return TokenSymbol.Plus;
            case "++":  return TokenSymbol.Increase;
            case "+=":  return TokenSymbol.AddAssign;
            case ",":   return TokenSymbol.Comma;
            case "-":   return TokenSymbol.Minus;
            case "--":  return TokenSymbol.Decrease;
            case "-=":  return TokenSymbol.SubAssign;
            case "/":   return TokenSymbol.Div;
            case "/=":  return TokenSymbol.DivAssign;
            case ";":   return TokenSymbol.Semicolon;
            case "<":   return TokenSymbol.Less;
            case "<=":  return TokenSymbol.Lesser;
            case "=":   return TokenSymbol.Assign;
            case "==":  return TokenSymbol.Equal;
            case ">":   return TokenSymbol.Great;
            case ">=":  return TokenSymbol.Greater;
            case "[":   return TokenSymbol.LBracket;
            case "]":   return TokenSymbol.RBracket;
            case "\255": return TokenSymbol.EOF;
            case "{":   return TokenSymbol.LBrace;
            case "||":  return TokenSymbol.Or;
            case "}":   return TokenSymbol.RBrace;
            case "&":
                System.err.print(LexicalError.getErrorMessage(LexicalError.ErrorCode.SingleAmpersand));
                break;
            case "|":
                System.err.print(LexicalError.getErrorMessage(LexicalError.ErrorCode.SingleBar));
                break;
            default: // 인식하지 못한 TokenSymbol
                System.err.print(LexicalError.getErrorMessage(LexicalError.ErrorCode.InvalidChar));
                break;
        }
        return TokenSymbol.NULL;
    }

    /**
     * Token의 Symbol과 value를 설정하는 메소드
     * Mini C Scanner 객체에서 잘라낸 토큰 String을 통해 그 String에 맞는 Symbol로 설정한다
     *
     * @param token - 잘라낸 토큰 String
     * @param type - Mini C Scanner 객체에서 분류한 타입 (키워드나 명칭, 숫자, 연산자로 대분류)
     */
    public void setSymbol(String token, SymbolType type) {
        tokenString = token;
        switch (type) {
            case IDorKeyword:
                symbol = getIDorKeywordSymbol(token);
                if (symbol == TokenSymbol.ID) // 명칭일 경우
                    val = token;
                break;
            case Digit:
                symbol = TokenSymbol.Number;
                val = Integer.toString(parseInt(token));
                break;
            case Operator:
                symbol = getOperatorSymbol(token);
                break;
            case NULL:
            default:
                break;
        }
    }

    /**
     * 16진수, 8진수, 10진수에 상관없이 String을 정수형으로 추출해내는 메소드
     *
     * @param s - 정수로 변환할 String (eg. 0x1F, 047, 14)
     * @return String의 정수
     */
    private int parseInt(String s) {
        int radix = 10; // default 진법은 10진수
        if (s.startsWith("0x")) { // 16진수일 경우
            radix = 16; // 진법을 16진수로 설정
            s = s.substring(2); // prefix인 0x 제거
        } else if (s.startsWith("0") && s.length() > 1) { // 8진수일 경우
            radix = 8; // 진법을 8진수로 설정
        }
        return Integer.parseInt(s, radix); // 위에서 설정한 진법대로 진법 변환
    }

    /**
     * Symbol에 해당하는 토큰 심볼을 숫자로 표현하는 메소드
     *
     * @return 토큰 심볼의 숫자 (-1 : NULL)
     */
    public int getSymbolOrdinal() {
        return symbol.ordinal()-1;  // NULL이 -1이기 때문에 -1 해야한다.
    }

    /**
     * 토큰이 명칭이나 숫자일 경우 그 토큰 값을 얻는 메소드
     *
     * @return 토큰 값 (없을 경우 0 반환)
     */
    public String getSymbolValue() {
        return val;
    }

    /**
     * 출력하기 편하게 하기 위해 정의하는 toString 메소드
     *
     * @return 토큰을 표현하기 위한 String
     */
    public String toString() {
        return tokenString + "\t : (" + getSymbolOrdinal() + ", "+ getSymbolValue() + ")";
    }
}
