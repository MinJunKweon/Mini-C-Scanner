package com.minjunkweon;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Token을 가져오기 위한 처리를 담당하는 Scanner 클래스
 */
public class MiniCScanner {
    static public final char EOF = '\255'; // 파일의 끝을 의미하는 EOF 문자
    static public final String SPECIAL_CHARS = "!=%&*+-/<>|"; // 한글자 자체가 토큰이 아닐 수 있는

    private String src;
    private Integer idx;

    /**
     * states
     * 0 : Initial state
     * 1 : Decimal
     * 2 : Octal
     * 3 : Hexadecimal
     * 4 : IDorKeyword
     * 5 : Operator
     */
    private enum State {
        Initial, Decimal, Octal, Hexadecimal, IDorKeyword, Operator
    }

    /**
     *
     * @param filePath
     */
    public MiniCScanner(String filePath) {
        src = parseFile(filePath);
        idx = 0;
    }

    /**
     *
     * @param filePath
     * @return
     */
    private String parseFile(String filePath) {
        String src = "", readedString = "";
        FileReader fileReader = null;
        try {
            fileReader = new FileReader(new File(filePath));
        } catch (IOException e) {
            System.err.print("ERROR!\n" + e.getMessage() + "\n" + e.getStackTrace());
            System.exit(-1);
        }

        BufferedReader reader = new BufferedReader(fileReader);
        try {
            while ((readedString = reader.readLine()) != null)
                src += readedString + "\n";
            src += EOF;   // 파일의 끝을 의미하는 EOF 문자 추가
            reader.close();
        } catch (IOException e) {
            System.err.print("ERROR!\n" + e.getMessage() + "\n" + e.getStackTrace());
            System.exit(-1);
        }
        return src;
    }

    /**
     *
     * @return
     */
    public Token getToken() {
        Token token = new Token();
        Token.SymbolType symType = Token.SymbolType.NULL;
        String tokenString = "";

        State currentState = State.Initial;

        if (exceptComment()) {
            // get ERROR
            System.err.println("ERROR! : incomplete comment");
            System.exit(-1);    // 프로그램 종료
        }

        while (!isEOF(idx)) {
            char c = src.charAt(idx++);
            if (Character.isWhitespace(c)) { // white space (needs trimming)
                if (tokenString.length() != 0)
                    break;
                continue;
            } else if (isSpecialChar(c) || isSingleSpecialToken(c)) {
                if (currentState == State.Initial) { // operator
                    currentState = State.Operator;
                    if (isSingleSpecialToken(c)) { // signle operator ( '(', ')', '{', '}', ',', '[', ']', ';', EOF )
                        tokenString = String.valueOf(c);
                        break;
                    }
                } else if (currentState != State.Operator) { // operator (more than 1 digit)
                    idx--;
                    break;
                }
            } else if (Character.isAlphabetic(c)) { // ID or Keyword
                if (currentState == State.Initial) {
                    currentState = State.IDorKeyword;
                } else if (c == 'x' && currentState == State.Octal) { // Hexadecimal (0x일 경우 인식)
                    currentState = State.Hexadecimal;
                } else if (currentState != State.IDorKeyword) {
                    idx--;
                    break;
                }
            } else if (c == '0' && currentState == State.Octal) // Octal
                currentState = State.Octal;
            else if (Character.isDigit(c) && currentState == State.Initial) // Decimal
                currentState = State.Decimal;
            tokenString += String.valueOf(c);
        }
        symType = getSymbolType(currentState);
        token.setSymbol(tokenString, symType);
        return token;
    }

    /**
     *
     * @param idx
     * @return
     */
    private boolean isEOF(int idx) {
        return idx >= src.length();
    }

    /**
     *
     * @param c
     * @return
     */
    private boolean isSpecialChar(char c) {
        for (int i = 0; i < SPECIAL_CHARS.length(); ++i)
            if (SPECIAL_CHARS.charAt(i) == c)
                return true;
        return false;
    }

    /**
     *
     * @param c
     * @return
     */
    private boolean isSingleSpecialToken(char c) {
        switch (c) {
            case '(': case ')': case ',': case ';':
            case '[': case ']': case '{': case '}':
            case EOF:
                return true;
            default:
                return false;
        }
    }

    /**
     *
     * @param s
     * @return
     */
    private Token.SymbolType getSymbolType(State s) {
        switch (s) {
            case Decimal:
            case Octal:
            case Hexadecimal:
                return Token.SymbolType.Digit;
            case IDorKeyword:
                return Token.SymbolType.IDorKeyword;
            case Operator:
                return Token.SymbolType.Operator;
            case Initial:
            default:
                return Token.SymbolType.NULL;
        }
    }

    /**
     *
     * @return
     */
    private boolean exceptComment() {
        char c;
        // 문자열 trim
        while (!isEOF(idx) && Character.isWhitespace(src.charAt(idx))) idx++;
        if (isEOF(idx)) return false;

        if (src.charAt(idx) == '/') {
            if (isEOF(idx+1)) return true;   // ERROR: 마지막 줄에 세미콜론이 오지않음 (/이 옴)
            else if (src.charAt(idx+1) == '/') {    // Line Comment
                idx += 2;
                while (!isEOF(idx) && src.charAt(idx) != '\n') idx++; // 개행문자 혹은 EOF 문자
                idx += 1;
            } else if (src.charAt(idx+1) == '*') { // Block Comment
                idx += 2;
                while (src.charAt(idx) != '*' && src.charAt(idx+1) != '/') {
                    if (isEOF(idx+1)) return true;
                    idx++;
                }
                idx += 2;
            }
        }
        return false;
    }
}
