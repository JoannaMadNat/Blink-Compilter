package org.main.util;

import org.main.ErrorReporter;
import org.main.Type;
import org.main.symbols.ClassDecl;
import org.main.symbols.MethodDecl;
import org.main.symbols.SymbolTable;
import org.main.symbols.VarDecl;

import java.util.ArrayList;

public class SemanticUtils {
    static void compareTypes(String description, Type lhs, Type rhs, Integer line, Integer col) {
        if (lhs != rhs)
            ErrorReporter.get().reportError(line, col, description + ": '" + rhs.getName() + "' does not match '" + lhs.getName() + "'", ErrorReporter.ErrorType.SEMANTIC);
    }

    public static void checkTypes(String description, String allowedTypes, Type lhs, Type rhs, Integer line, Integer col) {
        if (!allowedTypes.contains(lhs.getName())) // hax
            ErrorReporter.get().reportError(line, col,
                    description + " expected '" + allowedTypes + "' got '" + lhs.getName() + "'", ErrorReporter.ErrorType.SEMANTIC);
        if (!allowedTypes.contains(rhs.getName()))
            ErrorReporter.get().reportError(line, col,
                    description + " expected '" + allowedTypes + "' got '" + rhs.getName() + "'", ErrorReporter.ErrorType.SEMANTIC);

        compareTypes(description, lhs, rhs, line, col);
    }

    public static void checkArguments(String name, ArrayList<VarDecl> expected_args, ArrayList<Type> given_args, boolean isClassFunction, int lineNum, int colNum) {
        String errorDescriptionName = "Function";
        int classParam = 0;
        if (isClassFunction) {
            ArrayList<Type> temp = new ArrayList<>();
            temp.add(Type.integer); // add class param
            temp.addAll(given_args);
            given_args = temp; // make given args account for class param

            errorDescriptionName = "Class";
            classParam = 1;
        }
        int expected_argsCount = expected_args.size();
        int given_argsCount = given_args.size();

        if (given_argsCount != expected_argsCount)
            ErrorReporter.get().reportError(lineNum, colNum, errorDescriptionName + " '" + name + "' expects " + (expected_argsCount - classParam) + " parameters, got " + (given_argsCount - classParam), ErrorReporter.ErrorType.SEMANTIC);
        else {
            for (int i = 0; i < expected_argsCount; i++)
                checkInheritanceValid(expected_args.get(i).getType(), given_args.get(i), lineNum, colNum);

        }
    }

    public static boolean checkOverrideFunction(ClassDecl parent, MethodDecl method, int lineNum, int colNum) {
        if (parent == null)
            return false;

        MethodDecl res = null;

        while (parent != null) { // allow child to inherit from any line back
            res = parent.lookupMethod(method.getName());
            if(res != null) break;
            parent = parent.getInheritsFrom();
        }

        if (res == null)
            return false;

        if (method.getType() != res.getType())
            ErrorReporter.get().reportError(lineNum, colNum, "Override of '" + method.getName() + "' should be type '" + res.getType().getName() + "' got '" + method.getType().getName() + "'", ErrorReporter.ErrorType.SEMANTIC);

        if (method.parameters.size() != res.parameters.size() - 1) {
            ErrorReporter.get().reportError(lineNum, colNum, "Override of '" + method.getName() + "' expects " + res.parameters.size() + "arguments, got " + method.parameters.size(), ErrorReporter.ErrorType.SEMANTIC);
            return false;
        }

        for (int i = 1; i < res.parameters.size() - 1; i++)
            if (method.parameters.get(i - 1).getType() != res.parameters.get(i).getType())
                ErrorReporter.get().reportError(lineNum, colNum, "Override type '" + res.parameters.get(i - 1).getType().getName() + "', got '" + method.parameters.get(i).getType().getName() + "'", ErrorReporter.ErrorType.SEMANTIC);

        return true;
    }

    public static void checkInheritanceValid(Type declared_type, Type given_type, int linenum, int charnum) {
        if(!declared_type.isCustom() && !given_type.isCustom()) {
            compareTypes("Mismatched types", declared_type, given_type, linenum, charnum);
            return;
        }
        if(declared_type == given_type || given_type == Type.nil || declared_type == Type.nil)
            return;

        ClassDecl declCLass = SymbolTable.lookup(declared_type.getName(), ClassDecl.class);
        if(declCLass == null) {
            ErrorReporter.get().reportError(linenum, charnum,  "No such class '" + declared_type.getName() + "'", ErrorReporter.ErrorType.SEMANTIC);
            return;
        }
        ClassDecl givenClass = SymbolTable.lookup(given_type.getName(), ClassDecl.class);
        if(givenClass == null) {
            ErrorReporter.get().reportError(linenum, charnum,  "No such class '" + given_type.getName() + "'", ErrorReporter.ErrorType.SEMANTIC);
            return;
        }

        ClassDecl walker = givenClass;
        while(walker != null) {
            if(walker.getType() == declCLass.getType())
                return;
            walker = walker.getInheritsFrom();
        }

        walker = declCLass;
        while(walker != null) {
            if(walker.getType() == walker.getType())
                return;
            walker = walker.getInheritsFrom();
        }

        ErrorReporter.get().reportError(linenum, charnum,  "Cannot convert '" + declared_type.getName() + "' to '" + given_type.getName() + "' without mass murder", ErrorReporter.ErrorType.SEMANTIC);
    }
}
