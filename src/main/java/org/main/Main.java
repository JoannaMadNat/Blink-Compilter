package org.main;

import org.antlr.v4.gui.Trees;
import org.antlr.v4.runtime.*;
import org.apache.commons.cli.*;
import org.apache.commons.io.IOUtils;
import org.bju.BlinkParser;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Main {
    public static void main(String[] args) throws ParseException, IOException, InterruptedException {
        CharStream input = CharStreams.fromStream(IOUtils.toInputStream(getFilesfromArguments(parseCommandLineArgs(args))));

        BlinkLexerImpl lexer = new BlinkLexerImpl(input);                       // Do Lexer
        BlinkParser.StartContext tree = doParser(new CommonTokenStream(lexer)); // Do Parser
        System.out.flush();

        if (ErrorReporter.get().hasErrors()) {
            ErrorReporter.get().printErrors();
            return;
        }

        new SemanticChecker().visit(tree);      // Do Semantic Checker

        if (ErrorReporter.get().hasErrors()) {
            ErrorReporter.get().printErrors();
            return;
        }
        if(CommandLineOptions.get().getDoOptimize())
            new PT_Optimizer().visit(tree);         // Do Parse Tree Optimization

        doCodeGen(tree);                            // Do Code Generation

        ErrorReporter.get().printErrors();
    }

    static BlinkParser.StartContext doParser(CommonTokenStream tokenStream) {
        BlinkParser parser = new BlinkParser(tokenStream);     // Do Parser
        parser.removeErrorListener(ConsoleErrorListener.INSTANCE);
        parser.addErrorListener(new BlinkParseErrorListener());
        BlinkParser.StartContext tree = parser.start();

        if (CommandLineOptions.get().getPrintParserOutput())
            Trees.inspect(tree, parser);

        return tree;
    }

    static String getFilesfromArguments(List<String> files) throws IOException {
        boolean fileNameSet = false;
        String contents = "";
        for (String file : files) {
            var fl = Paths.get(file);
            String fileName = fl.toString();
            if (!fileName.endsWith(".blink")) {
                System.err.println("File must end with .blink");
                return "";
            }

            if (!fileNameSet) {
                CommandLineOptions.get().setOutputFile(fileName.substring(0, fileName.length() - 6));
                fileNameSet = true;
            }

            String fileContents = Files.readString(fl);
            if (!fileContents.endsWith("\n")) {
                fileContents += "\n";
            }
            ErrorReporter.get().addFile(file, fileContents.split("\n").length);
            contents += fileContents;
        }
        return contents;
    }

    static void doCodeGen(BlinkParser.StartContext tree) throws IOException, InterruptedException {
        new CodeGen().visit(tree);

        if (!CommandLineOptions.get().getProduceAssembly()) {
            System.out.println("Compiling...");

            ProcessBuilder builder = new ProcessBuilder("gcc", "-g", "stdlib.o", CommandLineOptions.get().getOutputFile() + ".s", "-o", CommandLineOptions.get().getOutputFile());
            builder.inheritIO().redirectOutput(ProcessBuilder.Redirect.PIPE);
            Process process = builder.start();
            BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String s;
            while ((s = stdError.readLine()) != null) // Print any linking errors
                System.err.println(s);

            System.out.println("completed.");

            if (CommandLineOptions.get().getRunExec())
                RunExecutable();
            if (CommandLineOptions.get().getCleanDirectory())
                cleanDirectory();
        }
    }

    static void cleanDirectory() throws IOException {
        Runtime.getRuntime().exec("rm " + CommandLineOptions.get().getOutputFile() + ".s " + CommandLineOptions.get().getOutputFile());
    }

    static void RunExecutable() throws InterruptedException, IOException {
        Thread.sleep(500);
        System.out.println("Executing...");
        File f = new File("./" + CommandLineOptions.get().getOutputFile());
        if (f.exists() && !f.isDirectory()) {
            ProcessBuilder builder = new ProcessBuilder(f.getPath());
            builder.inheritIO().redirectOutput(ProcessBuilder.Redirect.PIPE);
            Process process = builder.start();

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                reader.lines().forEach(line -> System.out.println(line));
            }

            process.waitFor();
            System.out.println("\nDone.");
        }
    }

    // parse the command line arguments and return all non arg arguments as an ArrayList
    private static List<String> parseCommandLineArgs(String[] args) throws ParseException {
        Options options = new Options();

        // add test option
        options.addOption("ds", "print-lexer-output", false, "Print lexer output");
        options.addOption("dp", "print-parser-output", false, "Print parser output");
        options.addOption("S", "generate-assembly", false, "Generate assembly file");
        options.addOption("r", "run", false, "Run Executable");
        options.addOption("o", "output-file", true, "Set output file name");
        options.addOption("d", "clean", false, "Clean directory");
        options.addOption("z", "no-optimize", false, "Don't optimize tree");

        CommandLineParser commandLineParser = new DefaultParser();
        CommandLine commandLine = commandLineParser.parse(options, args);

        if (commandLine.hasOption("ds"))
            CommandLineOptions.get().setPrintLexerOutput(true);
        if (commandLine.hasOption("dp"))
            CommandLineOptions.get().setPrintParserOutput(true);
        if (commandLine.hasOption("S"))
            CommandLineOptions.get().setProduceAssembly(true);
        if (commandLine.hasOption("o"))
            CommandLineOptions.get().setOutputFile(commandLine.getOptionValue("o"));
        if (commandLine.hasOption("r") && !commandLine.hasOption("S"))
            CommandLineOptions.get().setRunExec(true);
        if (commandLine.hasOption("d"))
            CommandLineOptions.get().setCleanDirectory(true);
        if (commandLine.hasOption("z"))
            CommandLineOptions.get().setDoOptimize(false);

        // all non matched things get returned
        return commandLine.getArgList();
    }
}
