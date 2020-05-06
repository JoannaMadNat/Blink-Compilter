package org.main.symbols;

import org.main.Type;

import java.util.ArrayList;
import java.util.Hashtable;

import org.main.util.ArrayUtils;

public class SymbolTable {
    private static ArrayList<Declaration> symbols = new ArrayList<>();
    public static ArrayList<MethodDecl> stdlib = new ArrayList<>();
    private static int latestScope = 0;

    private static SymbolTable symbolTable = new SymbolTable();

    public static Hashtable<String, Type> dotCalls = new Hashtable<>();

    /* A private Constructor prevents any other
     * class from instantiating.
     */
    private SymbolTable() {
        stdlib.add(new MethodDecl("readint", Type.integer)); // readint

        MethodDecl meth = new MethodDecl("printint", Type.integer); //printint
        meth.parameters.add(new VarDecl("num", Type.integer));
        stdlib.add(meth);

        meth = new MethodDecl("getlength", Type.integer); //getlength
        meth.parameters.add(new VarDecl("str", Type.string));
        stdlib.add(meth);

        meth = new MethodDecl("printchar", Type.integer); //printchar
        meth.parameters.add(new VarDecl("ch", Type.integer));
        stdlib.add(meth);

        meth = new MethodDecl("getchar", Type.integer); //getchar
        meth.parameters.add(new VarDecl("str", Type.string));
        meth.parameters.add(new VarDecl("pos", Type.integer));
        stdlib.add(meth);

        meth = new MethodDecl("readchar", Type.integer); //readchar
        stdlib.add(meth);

        meth = new MethodDecl("readstring", Type.string); //readstring
        stdlib.add(meth);

        meth = new MethodDecl("printstring", Type.string); //printstring
        meth.parameters.add(new VarDecl("str", Type.string));
        stdlib.add(meth);

        meth = new MethodDecl("setchar", Type.integer); //setchar
        meth.parameters.add(new VarDecl("str", Type.string));
        meth.parameters.add(new VarDecl("pos", Type.integer));
        meth.parameters.add(new VarDecl("ch", Type.integer));
        stdlib.add(meth);

        symbols.addAll(stdlib);
    }

    /* Static 'instance' method */
    public static SymbolTable getInstance() {
        return symbolTable;
    }

    /* Other methods protected by singleton-ness */
    protected static void demoMethod() {
        System.out.println("demoMethod for singleton");
    }


    public static void addSymbol(Declaration d) {
        d.setLevel(latestScope);
        symbols.add(d);
    }

    public static void beginScope(Declaration d) {
        d.setLevel(latestScope); // this used to be a bug but apparently it's a feature now.
        symbols.add(d);
        latestScope = latestScope + 1;
    }

    public static void endScope() {
        for (int i = 0; i < symbols.size(); i++)
            if (symbols.get(i).getLevel() == latestScope) {
                symbols.remove(i);
                i--; // This is an odd way to dodge the problem, but it works!
            }
        latestScope = latestScope - 1;
    }

    public static <T extends Declaration> T lookup(String name, Class<T> desiredType) {
        for (Declaration symbol : symbols) {
            if (symbol.getClass().equals(desiredType) && symbol.name.equals(name)) {
                return (T) symbol;
            }
        }
        return null;
    }

    public static void addTypeContext(String name, Type value, ClassDecl classContext, MethodDecl funcContext) {
        String prefix = "";
        if (funcContext != null) {
            prefix += "F-";
            if (classContext != null)
                prefix += classContext.getName() + "_";
            prefix += funcContext.getName() + "_";
        }


        dotCalls.put(prefix + name, value);
    }
    // The prefix is to elimininnate chances of there being duplicates. also putting C and F since there can be functions and classes with the same name
    // #thinkingahead

    public static Type getTypeContext(String name, MethodDecl funcContext) {
        String prefix = "";
        if (funcContext != null)
            prefix += "F-" + funcContext.getName() + "_";

        return dotCalls.get(prefix + name);
    }

    public static void deleteRecent() {
        symbols.remove(symbols.size() - 1);
    }
}
