package org.main;

import lombok.Getter;
import org.main.util.Pair;
import java.util.ArrayList;
import java.util.List;

@Getter
public class ErrorReporter {
    public enum ErrorType {
        LEXER,
        PARSER,
        SEMANTIC
    };

    private int lexerErrors = 0;
    private int parseErrors = 0;
    private int semanticErrors = 0;
    private List<Pair<String, Integer>> files = new ArrayList<>();

    private static ErrorReporter errorReporter = null;
    public static ErrorReporter get() {
        if (errorReporter == null) {
            errorReporter = new ErrorReporter();
        }
        return errorReporter;
    }

    public void addLexerError() {
        this.lexerErrors++;
    }

    public void addParseError() {
        this.parseErrors++;
    }

    public void addSemanticErrors() {
        this.semanticErrors++;
    }

    public boolean hasErrors() {
        return lexerErrors > 0 || parseErrors > 0 || semanticErrors > 0;
    }

    public void printErrors() {
        System.out.println(ErrorReporter.get().getLexerErrors() + " lexer error(s), " + ErrorReporter.get().getParseErrors() + " syntax error(s), " + ErrorReporter.get().getSemanticErrors() + " semantic error(s)");
    }
    public void addFile(String filename, Integer lineCount) {
        this.files.add(new Pair(filename, lineCount));
    }

    public void reportError(Integer lineNumber, Integer charNumber, String message, ErrorType type) {
        switch (type) {
            case LEXER: addLexerError(); break;
            case PARSER: addParseError(); break;
            case SEMANTIC: addSemanticErrors(); break;
        }
        print(lineNumber, charNumber, message);
    }

    public void print(Integer lineNumber, Integer charNumber, String message) {
        if(lineNumber != null) {
            for (Pair<String, Integer> pair : files) {
                if (lineNumber <= pair.getValue()) {
                    System.out.println(pair.getKey() + ":" + lineNumber + ":" + charNumber +":" + message);
                    break; // added this because it prints the same error for all files
                } else {
                    lineNumber -= pair.getValue();
                    System.out.println(pair.getKey() + ":" + lineNumber + ":" + charNumber +":" + message);
                }
            }
        } else {
            System.out.println("unknown:unknown:" + message);
        }
    }
}
