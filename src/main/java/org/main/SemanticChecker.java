package org.main;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bju.BlinkBaseVisitor;
import org.bju.BlinkParser;
import org.main.symbols.*;
import org.main.util.SemanticUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@EqualsAndHashCode(callSuper = false)
public class SemanticChecker extends BlinkBaseVisitor {
    MethodDecl currentFuncContext = null;
    ClassDecl currentClassContext = null;


    @Override
    public Object visitStart(BlinkParser.StartContext ctx) {
        var res = super.visitStart(ctx);

        MethodDecl start = SymbolTable.lookup("start", MethodDecl.class); // check for start method
        if (start == null)
            ErrorReporter.get().reportError(0, 0,
                    "Could not find 'start' function anywhere. FIX THIS!!", ErrorReporter.ErrorType.SEMANTIC);
        else if (start.parameters.size() != 0)
            ErrorReporter.get().reportError(0, 0,
                    "'start' function doesn't take any arguments, ya dingus!", ErrorReporter.ErrorType.SEMANTIC);
        return res;
    }

    @Override
    public ClassDecl visitInherits(BlinkParser.InheritsContext ctx) {
        // inherits: INHERITS FROM id=IDENTIFIER L_PAREN values=parameters? R_PAREN;
        ClassDecl expClass = SymbolTable.lookup(ctx.id.getText(), ClassDecl.class);
        if (expClass == null) {
            ErrorReporter.get().reportError(ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine(), "Class with name '" + ctx.id.getText() + "' already exists", ErrorReporter.ErrorType.SEMANTIC);
            return new ClassDecl("error");
        }

        if (ctx.values != null) {
            ArrayList<Type> given_params = (ArrayList<Type>) visit(ctx.values);
            SemanticUtils.checkArguments(expClass.getName(),  expClass.parameters, given_params, true, ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine());
        }

        return expClass;
    }

    @Override
    public ArrayList<Type> visitInherit_params(BlinkParser.Inherit_paramsContext ctx) {
        // inherit_params: id=IDENTIFIER (COMMA others+=IDENTIFIER)*;
        ArrayList<Type> params = new ArrayList<>();
        VarDecl exp = SymbolTable.lookup(ctx.id.getText(), VarDecl.class);
        if (exp == null) {
            ErrorReporter.get().reportError(ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine(), "no variable with name '" + ctx.id.getText() + "' exists", ErrorReporter.ErrorType.SEMANTIC);
            return params;
        }

        params.add(exp.getType());
        if (ctx.others != null) {
            for (var other : ctx.others) {
                exp = SymbolTable.lookup(other.getText(), VarDecl.class);

                if (exp == null) {
                    ErrorReporter.get().reportError(ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine(), "no variable with name '" + ctx.id.getText() + "' exists", ErrorReporter.ErrorType.SEMANTIC);
                    return params;
                }

                params.add(exp.getType());
            }
        }
        return params;
    }

    @Override
    public Type visitBlink_class(BlinkParser.Blink_classContext ctx) {
        // blink_class: TYPE id=IDENTIFIER L_PAREN args=arguments? R_PAREN parent=inherits? ASSIGNMENT members+=member_declaration+ END;

        String name = ctx.id.getText();
        if (SymbolTable.lookup(name, ClassDecl.class) != null)
            ErrorReporter.get().reportError(ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine(), "Class with name '" + name + "' already exists", ErrorReporter.ErrorType.SEMANTIC);

        ClassDecl newClass = new ClassDecl(name);
        currentClassContext = newClass;
        SymbolTable.beginScope(newClass); //beginning context of class

        ArrayList<VarDecl> parameters = new ArrayList<>();
        if (ctx.args != null)
            parameters = (ArrayList<VarDecl>) visit(ctx.args);

        if (ctx.parent != null) {
            ClassDecl parent = (ClassDecl) visit(ctx.parent);
            for (int i = 1; i < parent.getParameters().size(); i++)  // skip the class pointer of the parent
                newClass.addMember(parent.getParameters().get(i), true);


            for (var local : parent.getLocals())
                newClass.addMember(local, true);

            for (var meth : parent.getMethods()) {
                MethodDecl parentMethod = new MethodDecl(meth); // copying the method because the child wants freedom to alter it
                newClass.addMember(parentMethod, true);
            }
            newClass.addParent(parent);
        }

        for (var param : parameters)
            newClass.addParameter(param);

        for (var member : ctx.members) {
            var mem = (Declaration) visit(member);
            if (member.getClass().equals(BlinkParser.Member_method_declContext.class)) {
                SymbolTable.deleteRecent();
                boolean isOverride = SemanticUtils.checkOverrideFunction(newClass.getInheritsFrom(), (MethodDecl) mem, ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine());
                if (isOverride) {
                    newClass.OverrideFunction((MethodDecl) mem);
                    continue;
                }
            }
            newClass.addMember(mem, false);
        }

        SymbolTable.endScope();
        currentClassContext = null;
        return newClass.getType();
    }

    @Override
    public Type visitFunction(BlinkParser.FunctionContext ctx) {
        //          | (expr=pre_dot_expression DOT)? id=IDENTIFIER L_PAREN values=parameters? R_PAREN (others+=otherCall)* # function
        String idMethodName = ctx.id.getText();
        MethodDecl meth;
        String predotText = "";

        ArrayList<Type> params = new ArrayList<>();
        if (ctx.values != null)
            params = (ArrayList<Type>) visit(ctx.values);

        if (ctx.expr != null) {
            Type exprType = (Type) visit(ctx.expr);
            ClassDecl exprClass = SymbolTable.lookup(exprType.getName(), ClassDecl.class);
            if (exprClass == null) {
                ErrorReporter.get().reportError(ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine(), "no class with name '" + exprType.getName() + "' exists", ErrorReporter.ErrorType.SEMANTIC);
                return Type.error;
            }
            meth = exprClass.lookupMethod(idMethodName);
            if (meth == null) {
                ErrorReporter.get().reportError(ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine(), "no method with name '" + idMethodName + "' exists", ErrorReporter.ErrorType.SEMANTIC);
                return Type.error;
            }

            predotText += ctx.expr.getText();
            SymbolTable.addTypeContext(predotText, exprType, currentClassContext, currentFuncContext);
            predotText += "_" + idMethodName;
            SemanticUtils.checkArguments(meth.getName(), meth.parameters, params, true, ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine());

        } else {
            // Two different execution paths for a dot call and a non dot call
            meth = SymbolTable.lookup(idMethodName, MethodDecl.class);
            if (meth == null) {
                ErrorReporter.get().reportError(ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine(), "no method with name '" + idMethodName + "' exists", ErrorReporter.ErrorType.SEMANTIC);
                return Type.error;
            }
            predotText += meth.getName();
            SemanticUtils.checkArguments(meth.getName(), meth.parameters, params, false, ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine());
        }


        Type cTyp = meth.getType();

        if (ctx.others != null) {
            for (var other : ctx.others) {
                SymbolTable.addTypeContext(predotText, cTyp, currentClassContext, currentFuncContext); // if there are others, it means the function call itself returns a callable type

                ClassDecl expClass = SymbolTable.lookup(cTyp.getName(), ClassDecl.class);
                if (expClass == null) {
                    ErrorReporter.get().reportError(ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine(), "no class with name '" + cTyp.getName() + "' exists", ErrorReporter.ErrorType.SEMANTIC);
                    return Type.error;
                }

                MethodDecl check = (MethodDecl) visit(other);
                MethodDecl original = expClass.lookupMethod(check.getName());
                if (original == null) {
                    ErrorReporter.get().reportError(ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine(), "no method with name '" + check.getName() + "' exists", ErrorReporter.ErrorType.SEMANTIC);
                    return Type.error;
                }

                params = new ArrayList<>();
                for (var par : check.parameters)
                    params.add(par.getType());
                SemanticUtils.checkArguments(original.getName(), original.parameters, params, true, ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine());

                predotText += "_" + check.getName(); // reached here
                cTyp = original.getType();
            }
        }
        return cTyp;
    }

    @Override
    public MethodDecl visitOtherCall(BlinkParser.OtherCallContext ctx) {
        // otherCall: DOT other_id=IDENTIFIER L_PAREN other_values=parameters? R_PAREN;
        MethodDecl meth = new MethodDecl(ctx.other_id.getText(), Type.nil);

        if (ctx.other_values != null) {
            ArrayList<Type> params = (ArrayList<Type>) visit(ctx.other_values);
            for (var param : params)
                meth.parameters.add(new VarDecl("stub", param));
        }

        return meth;
    }
// VVVVVVVVVVVVVVVVVVV DECLARATIONS

    @Override
    public VarDecl visitVariable(BlinkParser.VariableContext ctx) {
        // variable: id=IDENTIFIER typ=declared_type? ASSIGNMENT value=expression;

        VarDecl newVar = new VarDecl(ctx.id.getText(), (Type) visit(ctx.value));
        if (ctx.typ != null) {
            Type var_Type = (Type) visit(ctx.typ);
            SemanticUtils.checkInheritanceValid(var_Type, newVar.getType(), ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine());

            newVar.setType(var_Type);
        }
        if (SymbolTable.lookup(newVar.getName(), VarDecl.class) != null)
            ErrorReporter.get().reportError(ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine(), "Variable with name '" + newVar.getName() + "' already exists", ErrorReporter.ErrorType.SEMANTIC);

        SymbolTable.addSymbol(newVar);
        return newVar;
    }

    @Override
    public MethodDecl visitMethod(BlinkParser.MethodContext ctx) {
        // id=IDENTIFIER L_PAREN args=arguments? R_PAREN typ=declared_type? ASSIGNMENT members+=declaration* value=statement;
        String name = ctx.id.getText();
        if (SymbolTable.lookup(name, MethodDecl.class) != null)
            ErrorReporter.get().reportError(ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine(), "Method with name '" + name + "' already exists", ErrorReporter.ErrorType.SEMANTIC);

        MethodDecl newMethod = new MethodDecl(name, Type.error); // nil as placeholder
        currentFuncContext = newMethod;

        if (ctx.typ != null)
            newMethod.setType((Type) visit(ctx.typ));

        SymbolTable.beginScope(newMethod);

        // Populate parameters
        if (ctx.args != null) {
            ArrayList<VarDecl> params = (ArrayList<VarDecl>) visit(ctx.args);
            for (var param : params)
                newMethod.addParam(param);
            newMethod.fillParamSS();
        }

        if (ctx.members != null) {
            for (var member : ctx.members)
                newMethod.addLocal((VarDecl) visit(member));
            newMethod.fillLocalSS();
        }

        Type val = (Type) visit(ctx.value);
        if (newMethod.getType() == Type.error)
            newMethod.setType(val);
        else
            SemanticUtils.checkInheritanceValid(newMethod.getType(), val, ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine());

        SymbolTable.endScope();
        currentFuncContext = null;
        return newMethod;
    }

    // LISTS VVVVVVVVVVVVVVVVVVVVVV

    @Override
    public ArrayList<VarDecl> visitArguments(BlinkParser.ArgumentsContext ctx) {
        ArrayList<VarDecl> params = new ArrayList<>();
        params.add((VarDecl) visit(ctx.first));

        for (var arg : ctx.last)
            params.add((VarDecl) visit(arg));

        return params;
    }

    @Override
    public ArrayList<Type> visitParameters(BlinkParser.ParametersContext ctx) {
        ArrayList<Type> types = new ArrayList<>();
        types.add((Type) visit(ctx.first));

        for (var arg : ctx.last)
            types.add((Type) visit(arg));

        return types;
    }

    @Override
    public VarDecl visitArgument(BlinkParser.ArgumentContext ctx) {
        Type type = (Type) visit(ctx.typ);
        String name = ctx.id.getText();
        if (SymbolTable.lookup(name, VarDecl.class) != null)
            ErrorReporter.get().reportError(ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine(), "Variable with name '" + name + "' already exists", ErrorReporter.ErrorType.SEMANTIC);

        SymbolTable.addSymbol(new VarDecl(name, type));

        return new VarDecl(name, type);
    }

    // STATEMENTS VVVVVVVVVVVVVVVV

    @Override
    public Type visitBlink_if(BlinkParser.Blink_ifContext ctx) {
        // blink_if: IF expr=expression THEN true_value=statement false_value=blink_else? END;

        Type type = (Type) visit(ctx.expr);
        if (type != Type.bool)
            ErrorReporter.get().reportError(ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine(),
                    "Expected 'bool' for if, got '" + type.getName() + "'", ErrorReporter.ErrorType.SEMANTIC);

        Type true_result = (Type) visit(ctx.true_value);
        if (ctx.false_value != null) {
            Type false_result = (Type) visit(ctx.false_value);
            SemanticUtils.checkInheritanceValid(true_result, false_result, ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine()); // make both ends of if statement return same type
        }

        return true_result;
    }

    @Override
    public Type visitLoop(BlinkParser.LoopContext ctx) {
        // loop: LOOP WHILE expr=expression DO value=statement END;
        Type type = (Type) visit(ctx.expr);
        if (type != Type.bool)
            ErrorReporter.get().reportError(ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine(),
                    "expected 'bool' for while, got '" + type.getName() + "'", ErrorReporter.ErrorType.SEMANTIC);

        return (Type) visit(ctx.value);
    }


    @Override
    public Object visitReassign(BlinkParser.ReassignContext ctx) {
        Type value_Type = (Type) visit(ctx.value);
        String name = ctx.id.getText();

        //Check if symbol already exists
        VarDecl vary = SymbolTable.lookup(name, VarDecl.class);
        if (vary == null)
            ErrorReporter.get().reportError(ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine(), "No such variable with name '" + name + "'", ErrorReporter.ErrorType.SEMANTIC);
        else {
            SemanticUtils.checkInheritanceValid(vary.getType(), value_Type, ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine());
            value_Type = vary.getType();
        }
        return value_Type;
    }

    // VV Those binary operations
    @Override
    public Object visitParen_expr(BlinkParser.Paren_exprContext ctx) {
        return visit(ctx.expr);
    }

    @Override
    public Type visitAdd(BlinkParser.AddContext ctx) {
        Type type_first = (Type) visit(ctx.first);
        Type type_second = (Type) visit(ctx.rest);

        SemanticUtils.checkTypes("Add", "int", type_first, type_second,  ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine());
        return type_first;
    }

    @Override
    public Type visitSubtract(BlinkParser.SubtractContext ctx) {
        Type type_first = (Type) visit(ctx.first);
        Type type_second = (Type) visit(ctx.rest);

        SemanticUtils.checkTypes("Subtract", "int", type_first, type_second,  ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine());
        return type_first;
    }

    @Override
    public Type visitDivide(BlinkParser.DivideContext ctx) {
        Type type_first = (Type) visit(ctx.first);
        Type type_second = (Type) visit(ctx.rest);

        SemanticUtils.checkTypes("Divide", "int", type_first, type_second,  ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine());
        return type_first;
    }

    @Override
    public Type visitMultiply(BlinkParser.MultiplyContext ctx) {
        Type type_first = (Type) visit(ctx.first);
        Type type_second = (Type) visit(ctx.rest);

        SemanticUtils.checkTypes("Multiply", "int", type_first, type_second,  ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine());
        return type_first;
    }

    @Override
    public Type visitConcat(BlinkParser.ConcatContext ctx) {
        Type type_first = (Type) visit(ctx.first);
        SymbolTable.addTypeContext("con_" + ctx.first.getText(), type_first, currentClassContext, currentFuncContext);
        Type type_second = (Type) visit(ctx.rest);
        SymbolTable.addTypeContext("con_" + ctx.rest.getText(), type_second, currentClassContext, currentFuncContext);

        if (type_first != Type.string && type_first != Type.bool && type_first != Type.integer )
            ErrorReporter.get().reportError(ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine(),
                    "Concat expected 'int, string, bool' got '" + type_first.getName() + "'", ErrorReporter.ErrorType.SEMANTIC);
        if (type_second != Type.string && type_second != Type.bool && type_second != Type.integer )
            ErrorReporter.get().reportError(ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine(),
                    "Concat expected 'int, string, bool' got '" + type_second.getName() + "'", ErrorReporter.ErrorType.SEMANTIC);
        return Type.string;
    }

    @Override
    public Type visitAnd(BlinkParser.AndContext ctx) {
        Type type_first = (Type) visit(ctx.first);
        Type type_second = (Type) visit(ctx.rest);

        SemanticUtils.checkTypes("AND", "bool", type_first, type_second,  ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine());

        return type_first;
    }

    @Override
    public Type visitOr(BlinkParser.OrContext ctx) {
        Type type_first = (Type) visit(ctx.first);
        Type type_second = (Type) visit(ctx.rest);

        SemanticUtils.checkTypes("OR", "bool", type_first, type_second,  ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine());
        return type_first;
    }

    @Override
    public Type visitNot(BlinkParser.NotContext ctx) {
        Type type_first = (Type) visit(ctx.first);

        if (type_first != Type.bool)
            ErrorReporter.get().reportError(ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine(),
                    "Not expression expected 'bool' got '" + type_first.getName() + "'", ErrorReporter.ErrorType.SEMANTIC);
        return type_first;
    }

    @Override
    public Type visitNegate(BlinkParser.NegateContext ctx) {
        Type type_first = (Type) visit(ctx.first);
        if (type_first != Type.integer)
            ErrorReporter.get().reportError(ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine(),
                    "Negate expression expected 'int' got '" + type_first.getName() + "'", ErrorReporter.ErrorType.SEMANTIC);
        return type_first;
    }


    // VVV Logic operations
    @Override
    public Type visitGreater(BlinkParser.GreaterContext ctx) {
        Type type_first = (Type) visit(ctx.first);
        Type type_second = (Type) visit(ctx.rest);
        SymbolTable.addTypeContext("gr_" + ctx.rest.getText(), type_second, currentClassContext, currentFuncContext);

        SemanticUtils.checkTypes("Greater Than", "string int", type_first, type_second,  ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine());
        return Type.bool;
    }

    @Override
    public Type visitLess_equal(BlinkParser.Less_equalContext ctx) {
        Type type_first = (Type) visit(ctx.first);
        Type type_second = (Type) visit(ctx.rest);
        SymbolTable.addTypeContext("lq_" + ctx.rest.getText(), type_second, currentClassContext, currentFuncContext);

        SemanticUtils.checkTypes("Less Than or Equal", "string int", type_first, type_second,  ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine());
        return Type.bool;
    }

    @Override
    public Type visitLess(BlinkParser.LessContext ctx) {
        Type type_first = (Type) visit(ctx.first);
        Type type_second = (Type) visit(ctx.rest);
        SymbolTable.addTypeContext("ls_" + ctx.rest.getText(), type_second, currentClassContext, currentFuncContext);

        SemanticUtils.checkTypes("Less Than", "string int", type_first, type_second,  ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine());
        return Type.bool;
    }

    @Override
    public Type visitGreater_equal(BlinkParser.Greater_equalContext ctx) {
        Type type_first = (Type) visit(ctx.first);
        Type type_second = (Type) visit(ctx.rest);
        SymbolTable.addTypeContext("gq_" + ctx.rest.getText(), type_second, currentClassContext, currentFuncContext);

        SemanticUtils.checkTypes("Greater Than or Equal", "string int", type_first, type_second,  ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine());
        return Type.bool;
    }

    @Override
    public Type visitEqual(BlinkParser.EqualContext ctx) {
        Type type_first = (Type) visit(ctx.first);
        Type type_second = (Type) visit(ctx.rest);
        SymbolTable.addTypeContext("eq_" + ctx.rest.getText(), type_second, currentClassContext, currentFuncContext);

        if((type_first.isCustom() && type_second == Type.nil) || (type_second.isCustom() && type_first == Type.nil))
            return Type.bool;

        SemanticUtils.checkTypes("Equal", "string bool int nil", type_first, type_second,  ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine());

        return Type.bool;
    }


    @Override
    protected Object aggregateResult(Object aggregate, Object nextResult) {
        if (aggregate == null) {
            return nextResult;
        } else {
            if (aggregate instanceof List) {
                if (nextResult != null) {
                    ((List<Object>) aggregate).add(nextResult);
                }
                return aggregate;
            } else {
                List<Object> objects = new ArrayList<Object>();
                objects.add(aggregate);
                objects.add(nextResult);
                objects = objects.stream().filter(x -> x != null).collect(Collectors.toList());
                if (objects.size() == 1) {
                    return objects.get(0);
                } else {
                    return objects;
                }
            }
        }
    }

    // VVVVVVVVVVVVVVVV PREDOR EXPRS
    @Override
    public Type visitMe(BlinkParser.MeContext ctx) {
        if (currentClassContext == null) {
            ErrorReporter.get().reportError(ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine(),
                    "No class in context to reference Me", ErrorReporter.ErrorType.SEMANTIC);
            return Type.error;
        }
        return currentClassContext.getType();
    }

    @Override
    public Type visitNew(BlinkParser.NewContext ctx) {
        // NEW typ=type (L_PAREN value=parameters? R_PAREN)? (L_CURLY init=expressions? R_CURLY)? # new
        Type type = (Type) visit(ctx.typ);
        ClassDecl newClass = SymbolTable.lookup(type.getName(), ClassDecl.class);
        if (newClass == null) {
            ErrorReporter.get().reportError(ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine(),
                    "No such type '" + type.getName() + "'", ErrorReporter.ErrorType.SEMANTIC);
            return Type.error;
        }

        ArrayList<Type> given_args = new ArrayList<>(), values;
        if (ctx.value != null)
            given_args = (ArrayList<Type>) visit(ctx.value);

        SemanticUtils.checkArguments(newClass.getName(), newClass.parameters, given_args, true, ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine());

        if (ctx.init != null) {
            ErrorReporter.get().reportError(ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine(),
                    "feature unsupported - Arrays", ErrorReporter.ErrorType.SEMANTIC);
            given_args.remove(0); // remove class pointer
            int argSize = given_args.size();
            values = (ArrayList<Type>) visit(ctx.value);
            if (argSize != values.size()) {
                ErrorReporter.get().reportError(ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine(),
                        "Argument size mismatch, requested " + argSize + ", got " + values.size() + " values", ErrorReporter.ErrorType.SEMANTIC);
                return type;
            }

            for (int i = 1; i < argSize; i++)
                if (given_args.get(i) != values.get(i))
                    ErrorReporter.get().reportError(ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine(),
                            "Type mismatch, class requested " + newClass.parameters.get(i).getType().getName() + ", got " + values.get(i - 1).getName() + " values", ErrorReporter.ErrorType.SEMANTIC);
        }
        return type;
    }

    @Override
    public Type visitId(BlinkParser.IdContext ctx) {
        // id=IDENTIFIER (L_SQUARE values+=expression R_SQUARE)* # id
        if (ctx.br != null) {
            ErrorReporter.get().reportError(ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine(),
                    "feature unsupported - Arrays", ErrorReporter.ErrorType.SEMANTIC);
            if (ctx.values != null)
                for (var val : ctx.values)
                    visit(val);
        }

        VarDecl result = SymbolTable.lookup(ctx.id.getText(), VarDecl.class);
        if (result == null) {
            ErrorReporter.get().reportError(ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine(),
                    "Variable with name '" + ctx.id.getText() + "' does not exist in the current context", ErrorReporter.ErrorType.SEMANTIC);
            return Type.error;
        }
        return result.getType();
    }

    // VVV All the leafs

    @Override
    public Type visitString_type(BlinkParser.String_typeContext ctx) {
        return Type.string;
    }

    @Override
    public Type visitString(BlinkParser.StringContext ctx) {
        return Type.string;
    }

    @Override
    public Type visitCustom_type(BlinkParser.Custom_typeContext ctx) {
        String name = ctx.id.getText();
        ClassDecl customClass = SymbolTable.lookup(name, ClassDecl.class);
        if (customClass == null) {
            ErrorReporter.get().reportError(ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine(),
                    "Type with name '" + name + "' does not exist in the current context", ErrorReporter.ErrorType.SEMANTIC);
            return Type.error;
        }

        return customClass.getType();
    }


    @Override
    public Type visitType(BlinkParser.TypeContext ctx) {
        // typ=types (br=L_SQUARE sizes+=expression? R_SQUARE)*;
        return (Type) visit(ctx.typ);
    }

    @Override
    public Type visitInt(BlinkParser.IntContext ctx) {
        return Type.integer;
    }

    @Override
    public Type visitFalse(BlinkParser.FalseContext ctx) {
        return Type.bool;
    }

    @Override
    public Type visitTrue(BlinkParser.TrueContext ctx) {
        return Type.bool;
    }

    @Override
    public Type visitNil(BlinkParser.NilContext ctx) {
        return Type.nil;
    }

    @Override
    public Type visitInt_type(BlinkParser.Int_typeContext ctx) {
        return Type.integer;
    }

    @Override
    public Type visitBool_type(BlinkParser.Bool_typeContext ctx) {
        return Type.bool;
    }

}
