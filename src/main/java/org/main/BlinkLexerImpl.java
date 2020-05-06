package org.main;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.bju.BlinkLexer;
import org.main.util.ArrayUtils;

public class BlinkLexerImpl extends BlinkLexer {
    public BlinkLexerImpl(CharStream input) {
        super(input);
    }

    private int[] ignoredTokens = new int[] { BlinkLexerImpl.COMMENT, BlinkLexerImpl.WHITESPACE, BlinkLexerImpl.NEWLINE, BlinkLexerImpl.EOF };
    private int[] keywordTokens = new int[] {
        BlinkLexerImpl.TYPE,
        BlinkLexerImpl.INHERITS,
        BlinkLexerImpl.FROM,
        BlinkLexerImpl.END,
        BlinkLexerImpl.LET,
        BlinkLexerImpl.MEMBER,
        BlinkLexerImpl.INT_TYPE,
        BlinkLexerImpl.STRING_TYPE,
        BlinkLexerImpl.BOOL_TYPE,
        BlinkLexerImpl.IF,
        BlinkLexerImpl.THEN,
        BlinkLexerImpl.ELSE,
        BlinkLexerImpl.LOOP,
        BlinkLexerImpl.WHILE,
        BlinkLexerImpl.DO,
        BlinkLexerImpl.TRUE,
        BlinkLexerImpl.FALSE,
        BlinkLexerImpl.NIL,
        BlinkLexerImpl.ME,
        BlinkLexerImpl.NEW
    };
    private int[] operatorTokens = new int[] {
        BlinkLexerImpl.ASSIGNMENT,
        BlinkLexerImpl.OR,
        BlinkLexerImpl.AND,
        BlinkLexerImpl.GREATER_EQUAL,
        BlinkLexerImpl.GREATER,
        BlinkLexerImpl.LESS_EQUAL,
        BlinkLexerImpl.LESS,
        BlinkLexerImpl.EQUAL,
        BlinkLexerImpl.CONCAT,
        BlinkLexerImpl.PLUS,
        BlinkLexerImpl.SUBTRACT,
        BlinkLexerImpl.MULTIPLY,
        BlinkLexerImpl.DIVIDE,
        BlinkLexerImpl.NEGATE,
        BlinkLexerImpl.NOT
    };
    private int[] errorTokens = new int[] {
        BlinkLexerImpl.UNTERMINATED,
        BlinkLexerImpl.BAD_ESCAPE,
        BlinkLexerImpl.OTHER_CHARACTER
    };

    private boolean shouldIgnore(Token t) {
        return ArrayUtils.arrayContains(ignoredTokens, t.getType());
    }

    private boolean isKeyword(Token t) {
        return ArrayUtils.arrayContains(keywordTokens, t.getType());
    }

    private boolean isOperator(Token t) {
        return ArrayUtils.arrayContains(operatorTokens, t.getType());
    }

    private boolean isError(Token t) {
        return ArrayUtils.arrayContains(errorTokens, t.getType());
    }

    @Override
    public Token nextToken() {
        Token t = super.nextToken();
        if(CommandLineOptions.get().getPrintLexerOutput()) {
            if(!shouldIgnore(t)) {
                if(isKeyword(t)) {
                    ErrorReporter.get().print(t.getLine(), t.getCharPositionInLine(),"keyword:" + t.getText());
                } else if (t.getType() == BlinkLexerImpl.IDENTIFIER) {
                    ErrorReporter.get().print(t.getLine(), t.getCharPositionInLine(),"identifier:" + t.getText());
                } else if (t.getType() == BlinkLexerImpl.STRING_LITERAL) {
                    ErrorReporter.get().print(t.getLine(), t.getCharPositionInLine(),"string lit:" + t.getText());
                } else if (isOperator(t)) {
                    ErrorReporter.get().print(t.getLine(), t.getCharPositionInLine(),"operator:" + t.getText());
                } else if (!isError(t)) {
                    ErrorReporter.get().print(t.getLine(),t.getCharPositionInLine(), t.getText());
                }
            }
        }
        // errors are always printed
        if (t.getType() == BlinkLexerImpl.UNTERMINATED) {
            ErrorReporter.get().reportError(t.getLine(), t.getCharPositionInLine(), "Unterminated string:" + t.getText(), ErrorReporter.ErrorType.LEXER);
        } else if (t.getType() == BlinkLexerImpl.BAD_ESCAPE) {
            ErrorReporter.get().reportError(t.getLine(), t.getCharPositionInLine(),"Illegal string:" + t.getText(), ErrorReporter.ErrorType.LEXER);
        } else if (t.getType() == BlinkLexerImpl.OTHER_CHARACTER) {
            ErrorReporter.get().reportError(t.getLine(), t.getCharPositionInLine(),"Unrecognized char:" + t.getText(), ErrorReporter.ErrorType.LEXER);
        }
        return t;
    }
}
