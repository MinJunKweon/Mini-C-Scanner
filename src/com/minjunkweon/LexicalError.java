package com.minjunkweon;

/**
 * Lexical Error를 처리하는 클래스
 * Created by kweonminjun on 2016. 5. 13..
 */
public class LexicalError {
    public enum ErrorCode {
        CannotOpenFile, AboveIDLimit, SingleAmpersand, SingleBar, InvalidChar, InvalidComment
    }

    public static String getErrorMessage(ErrorCode code) {
        String msg;
        msg = "Lexical Error(code: " + code.ordinal() + ")\n";
        switch (code) {
            case CannotOpenFile:
                msg += "cannot open the file. please check the file path.";
                break;
            case AboveIDLimit:
                msg += "an identifier length must be less than 12.";
                break;
            case SingleAmpersand:
                msg += "next character must be &.";
                break;
            case SingleBar:
                msg += "next character must be |.";
                break;
            case InvalidChar:
                msg += "invalid character!!!";
                break;
            case InvalidComment:
                msg += "invalid block comment!!!";
                break;
            default:
                msg += "Unknown Error";
                break;
        }
        return msg;
    }
}
