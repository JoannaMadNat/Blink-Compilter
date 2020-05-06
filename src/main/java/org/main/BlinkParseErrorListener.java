package org.main;

import org.antlr.v4.runtime.ConsoleErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

public class BlinkParseErrorListener extends ConsoleErrorListener {

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer,
                            Object offendingSymbol,
                            int line,
                            int charPositionInLine,
                            String msg,
                            RecognitionException e)
    {
        ErrorReporter.get().reportError(line, charPositionInLine,
                msg, ErrorReporter.ErrorType.PARSER);
    }
}
