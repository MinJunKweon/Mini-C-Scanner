package com.minjunkweon;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Token을 가져오기 위한 처리를 담당하는 Scanner 클래스
 */
public class MiniCScanner {
    static public final char EOF = '\255'; // 파일의 끝을 의미하는 EOF 문자 상수
    static public final String SPECIAL_CHARS = "!=%&*+-/<>|"; // 두 글자 이상이 하나의 토큰일 수 있는 특수문자들

    private String src; // Source Code의 전체 내용을 String으로 저장하기 위한 변수
    private Integer idx; // Source Code를 읽을 때 cursor 역할을 하는 변수

    /**
     * Token을 추출해낼 때 어떤 토큰을 인식하고 있는지 나타내기 위한 State
     */
    private enum State {
        Initial, Dec, Oct, Hex, IDorKeyword, Operator, Zero, PreHex
    }

    /**
     * Mini C Scanner 생성자
     * 소스 코드의 파일 경로를 입력받아 src 변수에 String으로 저장하고, 커서를 맨 처음으로 이동
     *
     * @param filePath - 소스 코드의 파일 경로
     */
    public MiniCScanner(String filePath) {
        src = parseFile(filePath);
        idx = 0;
    }

    /**
     * 소스코드 경로를 통해 소스코드 파일을 String으로 읽어 들이는 Method
     *
     * @param filePath - 읽어올 소스코드 경로
     * @return 소스코드 파일의 내용 (String)
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
     * Core Method
     * Source code에서 Token 단위로 String을 나누고 Token 객체를 만들어 반환하는 처리를 한다
     *
     * @return 인식된 토큰 객체 반환
     */
    public Token getToken() {
        Token token = new Token();
        Token.SymbolType symType = Token.SymbolType.NULL; // Symbol Type을 NULL로 설정
        String tokenString = "";

        State state = State.Initial;

        // 현재 커서로부터 Comment 제거
        if (exceptComment()) {
            // Comment를 지우는 도중에 ERROR가 발생했을 경우
            System.err.println("ERROR! : incomplete comment");
            System.exit(-1);    // 프로그램 종료
        }

        while (!isEOF(idx)) { // 소스코드를 전부 읽을때 까지 계속
            char c = src.charAt(idx++); // 커서로부터 글자 하나를 읽고 커서를 한칸 이동

            if (Character.isWhitespace(c)) { // white space (needs trimming)
                if(state != State.Initial) break; // 만약 글자들을 인식하고 있었다면 그대로 종결
                else continue;
            } else if (isSpecialChar(c) || isSingleSpecialToken(c)) { // 특수문자일 경우
                if (state == State.Initial) { // 특수문자를 인식함
                    state = State.Operator;
                    if (isSingleSpecialToken(c)) { // signle operator ( '(', ')', '{', '}', ',', '[', ']', ';', EOF )
                        tokenString = String.valueOf(c);
                        break;
                    }
                } else if (state != State.Operator) {
                    // 다른 문자를 인식하는 도중에 특수문자를 읽었을 경우 while문 탈출
                    --idx; break;
                }
            } else if (state == State.Initial && c == '0') { // Zero를 인식할 경우
                state = State.Zero;
            } else if (Character.isDigit(c)) { // 숫자를 인식한 경우
                if (state == State.Initial) // 아무것도 인식하지 않았을 경우, 10진수로 취급
                    state = State.Dec;
                else if (state == State.Zero) // 숫자 0을 인식하고 있었을 경우, 8진수로 취급
                    state = State.Oct;
                else if (state == State.PreHex) // 0x 까지 인식하고 있었을 경우, 16진수로 취급
                    state = State.Hex;
            } else if (state == State.Zero && c == 'x') { // 0x까지 인식 했을 경우
                state = State.PreHex;
            } else if (Character.isAlphabetic(c) || c == '_') { // underscore 혹은 알파벳을 인식했을 경우
                if (state != State.Initial && state != State.IDorKeyword) {
                    // 명칭 혹은 키워드가 아닌 토큰을 인식하는 중일 경우 while문 탈출
                    --idx; break;
                }
                state = State.IDorKeyword; // 명칭 혹은 키워드 인식
            }

            tokenString += String.valueOf(c); // 토큰 String에 글자 추가
        }
        symType = getSymbolType(state); // 인식한 state로부터 토큰이 어떤 값을 의미하는지 대분류
        token.setSymbol(tokenString, symType); // tokenString과 함께 대분류한 타입을 전달하여 token을 세팅
        return token; // 인식한 token을 반환
    }

    /**
     * 커서가 파일의 끝을 가리키고 있는지 확인하는 Method
     * 즉, 더이상 읽을 문자가 없는지 확인하는 Method
     *
     * @param idx - 확인할 위치
     * @return 더이상 읽을 문자가 없으면 true, 아니면 false
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
            case Dec:
            case Oct:
            case Hex:
            case Zero:
                return Token.SymbolType.Digit;
            case IDorKeyword:
                return Token.SymbolType.IDorKeyword;
            case Operator:
                return Token.SymbolType.Operator;
            // 종결상태가 아닌 State의 경우 NULL Type을 반환 (인식실패)
            case Initial:
            case PreHex:
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
