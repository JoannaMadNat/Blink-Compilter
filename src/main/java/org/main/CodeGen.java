package org.main;

import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.bju.BlinkBaseVisitor;
import org.bju.BlinkParser;
import org.main.instructions.*;
import org.main.instructions.parameters.*;
import org.main.symbols.ClassDecl;
import org.main.symbols.MethodDecl;
import org.main.symbols.SymbolTable;
import org.main.util.InstructionUtils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class CodeGen extends BlinkBaseVisitor<InstructionSet> {
    private BufferedWriter writer = new BufferedWriter(new FileWriter(CommandLineOptions.get().getOutputFile() + ".s"));
    int ifCount = 0;
    int stringCount = 0;
    MethodDecl currentFuncContext = null;
    ClassDecl currentClassContext = null;

    InstructionSet globals = new InstructionSet();
    String prefix = "my_";

    public CodeGen() throws IOException {
        // a solution i am very proud of >.>
        // User thinks they're setting the starting point, but really they're not!!!
        globals.appendInstruction(new StabsInstruction("text"));
        globals.appendInstruction(new StabsInstruction("global", "main"));
        globals.appendInstruction(new LabelInstruction("main"));
        globals.appendInstruction(new InstructionCommand(Instruction.push, Register.rbp));
        globals.appendInstruction(new InstructionCommand(Instruction.move, Register.rsp, Register.rbp));
    }

    void initFile() throws IOException {
        String text =
                ".data\n" +
                        ".comm _stack, 800, 8\n" +
                        "_top_of_stack: .quad 10 # cushion space for any Whiles or Ifs that don't executed\n" +
                        "\n" +
                        ".text\n" +
                        "_myPush:\n" +
                        "    movq    _top_of_stack(%rip), %rcx\n" +
                        "    leaq    _stack(%rip), %rax\n" +
                        "    movq    %r11, (%rax, %rcx, 8)\n" +
                        "    incq    _top_of_stack(%rip)\n" +
                        "    ret\n" +
                        "\n" +
                        "_myPop:\n" +
                        "    decq    _top_of_stack(%rip)\n" +
                        "    movq    _top_of_stack(%rip), %rcx\n" +
                        "    leaq    _stack(%rip), %rax\n" +
                        "    movq    (%rax, %rcx, 8), %rax\n" +
                        "    ret\n" +
                        "# VVVV the rest is code generated... so surreal man\n\n\n";

        writer.write(text);
    }

    void closeMain() {
        globals.appendInstruction(new InstructionCommand(Instruction.call, new Label(prefix + "start")));
        globals.appendInstruction(new InstructionCommand(Instruction.move, Register.rbp, Register.rsp));
        globals.appendInstruction(new InstructionCommand(Instruction.pop, Register.rbp));
        globals.appendInstruction(new InstructionCommand(Instruction.ret));
    }

    @Override
    public InstructionSet
    visitStart(BlinkParser.StartContext ctx) {
        try {
            initFile();

            for (var child : ctx.children) {
                String text = visit(child).toAssembly();
                writer.write(text);
            }

            closeMain();
            writer.write(globals.toAssembly());
            writer.write("\n");
            writer.close();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        return null;
    }

    private InstructionSet skipStep(List<ParseTree> children) {
        InstructionSet text = new InstructionSet();
        for (var child : children)
            text.appendInstructionSet(visit(child));
        return text;
    }

    @Override
    public InstructionSet visitBlink_class(BlinkParser.Blink_classContext ctx) {
        // blink_class: TYPE id=IDENTIFIER L_PAREN args=arguments? R_PAREN parent=inherits? ASSIGNMENT members+=member_declaration+ END;
        InstructionSet instructions = new InstructionSet();
        instructions.appendInstruction(new Comment(ctx.getStart().getLine() + ":visitBlink_class: " + ctx.getText()));

        ClassDecl newClass = SymbolTable.lookup(ctx.id.getText(), ClassDecl.class); // shouldn't return null
        currentClassContext = newClass;
        instructions.appendInstructionSet(InstructionUtils.buildVFT(prefix, newClass)); // build VFT

        for (var member : ctx.members)
            if (member.getClass().equals(BlinkParser.Member_method_declContext.class))
                instructions.appendInstructionSet(visit(member)); // creating member functions seperate from variables

        instructions.appendInstructionSet(InstructionUtils.startMethod("c_" + newClass.getName())); // ************ Constructor starts here VVV

        instructions.appendInstruction(new InstructionCommand(Instruction.call, new Label("_myPop"))); // get the class pointer
        instructions.appendInstruction(new InstructionCommand(Instruction.push, Register.rax)); // push the class pointer to the stack
        instructions.appendInstruction(new InstructionCommand(Instruction.push, new IntegerLiteral("0"))); // second push for keeping shadow space happy :)

        instructions.appendInstruction(new InstructionCommand(Instruction.move, InstructionUtils.getConstructorPointerParam(), Register.r13)); // r13 beacuse the check clobbers r11 and r12

        for (int i = newClass.parameters.size() - 1; i > 0; i--) { // skip variable zero, populate all the parameters
            instructions.appendInstruction(new InstructionCommand(Instruction.call, new Label("_myPop")));
            if(newClass.parameters.get(i).getType().isCustom())
                instructions.appendInstructionSet(InstructionUtils.checkAssignClasstoValue(newClass.parameters.get(i).getType().getName(), Register.rax));
            instructions.appendInstruction(new InstructionCommand(Instruction.move, Register.rax, new OffsetRegister(Register.r13, InstructionUtils.calClassMemberOffset(newClass.parameters.get(i).getOffset()))));
        }

        if (ctx.parent != null) {
            if(ctx.parent.values != null)
                instructions.appendInstructionSet(visit(ctx.parent.values));  // should end with pushes

            instructions.appendInstructionSet(InstructionUtils.pushToMyStack(InstructionUtils.getConstructorPointerParam())); // get the class pointer

            instructions.appendInstruction(new InstructionCommand(Instruction.call, new Label("c_" + newClass.getInheritsFrom().getName())));
        }

        for (var member : ctx.members) // creating member variables
            if (member.getClass().equals(BlinkParser.Member_var_declContext.class))
                instructions.appendInstructionSet(visit(member));

        instructions.appendInstruction(new InstructionCommand(Instruction.move, InstructionUtils.getConstructorPointerParam(), Register.rax));
        instructions.appendInstructionSet(InstructionUtils.endMethod()); // ****************** Constructor ends here
        currentClassContext = null;
        return instructions;
    }

    @Override
    public InstructionSet visitInherit_params(BlinkParser.Inherit_paramsContext ctx) {
        InstructionSet instructions = new InstructionSet();
        instructions.appendInstructionSet(InstructionUtils.getClassVariable(currentClassContext, currentFuncContext, ctx.id.getText()));
        if (ctx.others != null)
            for (var other : ctx.others)
                instructions.appendInstructionSet(InstructionUtils.getClassVariable(currentClassContext, currentFuncContext, other.getText()));

        return instructions;
    }

    @Override
    public InstructionSet visitNew(BlinkParser.NewContext ctx) {
        // NEW typ=type (L_PAREN value=parameters? R_PAREN)? (L_CURLY init=parameters? R_CURLY)? # new;
        InstructionSet instructions = new InstructionSet();
        instructions.appendInstruction(new Comment(ctx.getStart().getLine() + ":visitNew: " + ctx.getText()));
        ClassDecl newClass = SymbolTable.lookup(ctx.typ.typ.getText(), ClassDecl.class);
        int classSize = newClass.getMemSize();

        if (ctx.value != null)
            instructions.appendInstructionSet(visit(ctx.value)); // should end with pushes to the stack

        instructions.appendInstruction(new InstructionCommand(Instruction.move, new IntegerLiteral(String.valueOf(classSize)), Register.rdi)); // # size
        instructions.appendInstruction(new InstructionCommand(Instruction.call, new Label("allocateMem")));
        instructions.appendInstruction(new InstructionCommand(Instruction.move, new IntegerLiteral("VFT" + newClass.getName()), new OffsetRegister(Register.rax, 0))); // Address
        instructions.appendInstructionSet(InstructionUtils.pushToMyStack(Register.rax)); // so that the first param contains the pointer to the class

        instructions.appendInstruction(new InstructionCommand(Instruction.call, new Label("c_" + newClass.getName())));
        instructions.appendInstructionSet(InstructionUtils.pushToMyStack(Register.rax)); //returns pointer
        return instructions;
    }

    @Override // CALL
    public InstructionSet visitFunction(BlinkParser.FunctionContext ctx) {
        //  (expr=pre_dot_expression DOT)? id=IDENTIFIER L_PAREN values=parameters? R_PAREN (others+=otherCall)* # function
        InstructionSet instructions = new InstructionSet();
        instructions.appendInstruction(new Comment(ctx.getStart().getLine() + ":visitFunction: " + ctx.getText()));

        ClassDecl owner = null;
        String functionName = ctx.id.getText();
        MethodDecl callee;

        // figure out which function to call
        if (ctx.expr != null) {
            instructions.appendInstructionSet(visit(ctx.expr)); // ends with pushing the class pointer to the stack
            owner = InstructionUtils.getOwner(ctx.expr.getText(), currentClassContext, currentFuncContext);
            callee = owner.lookupMethod(functionName);
        } else {
            callee = SymbolTable.lookup(functionName, MethodDecl.class);
            functionName = InstructionUtils.getFunctionCallName(functionName, prefix);
        }

        if (ctx.values != null)
            instructions.appendInstructionSet(visit(ctx.values)); // must end with pushing the parameters to teh stack

        instructions.appendInstructionSet(InstructionUtils.handleFunctionCall(functionName, owner, callee.parameters, callee.getParamSS()));

        if (ctx.others != null) {
            String iterName = "";
            if (ctx.expr != null)
                iterName = ctx.expr.getText() + "_";
            iterName += ctx.id.getText();

            for (var other : ctx.others) {
                InstructionSet visitedOther = visit(other); // ends push to stack

                int vSize = visitedOther.getCommands().size();
                functionName = visitedOther.getCommands().get(vSize - 1).getFirstParam().toString(); // get the last command which is a call to the function, extract name
                visitedOther.getCommands().remove(vSize - 1); // remove the call
                owner = InstructionUtils.getOwner(iterName, currentClassContext, currentFuncContext);
                callee = owner.lookupMethod(functionName);
                instructions.appendInstructionSet(visitedOther);
                instructions.appendInstructionSet(InstructionUtils.handleFunctionCall(functionName, owner, callee.parameters, callee.getParamSS()));
                iterName += "_" + functionName;
            }
        }

        return instructions;
    }

    @Override
    public InstructionSet visitOtherCall(BlinkParser.OtherCallContext ctx) {
        //otherCall: DOT other_id=IDENTIFIER L_PAREN other_values=parameters? R_PAREN;
        InstructionSet instructions = new InstructionSet();
        if (ctx.other_values != null)
            instructions.appendInstructionSet(visit(ctx.other_values)); // each one ends with a push to the stack

        instructions.appendInstruction(new InstructionCommand(Instruction.call, new Label(ctx.other_id.getText())));
        return instructions;
    }


    @Override // DECLARATION
    public InstructionSet visitMethod(BlinkParser.MethodContext ctx) {
        //method: id=IDENTIFIER     L_PAREN args=arguments? R_PAREN     typ=declared_type? ASSIGNMENT members+=declaration* value=statement;
        InstructionSet instructions = new InstructionSet();
        instructions.appendInstruction(new Comment(ctx.getStart().getLine() + ":visitMethod: " + ctx.getText()));

        String name = ctx.id.getText();
        MethodDecl method;

        // Figure out which method we're creating
        if (currentClassContext != null) {
            method = currentClassContext.lookupMethod(name);
            name = method.getName();
        } else {
            method = SymbolTable.lookup(name, MethodDecl.class);
            name = prefix + name;
        }

        currentFuncContext = method;
        instructions.appendInstructionSet(InstructionUtils.startMethod(name));

        //set shadowspace for locals
        instructions.appendInstruction(new InstructionCommand(Instruction.sub, new IntegerLiteral(String.valueOf(method.getLocalSS())), Register.rsp)); // "subq    $16, %rsp";

        //save registers
        for (Register reg : InstructionUtils.argRegs)
            instructions.appendInstruction(new InstructionCommand(Instruction.push, reg));

        // add local variables
        for (var member : ctx.members)
            instructions.appendInstructionSet(visit(member));

        // statement
        instructions.appendInstructionSet(visit(ctx.value)); // ends with push
        instructions.appendInstruction(new InstructionCommand(Instruction.call, new Label("_myPop"))); // to make sure the function returns something in rax
        if (ctx.typ != null)
            instructions.appendInstructionSet(InstructionUtils.checkAssignClasstoValue(visit(ctx.typ).getCommands().get(0).toString(), Register.rax));

        instructions.appendInstructionSet(InstructionUtils.endMethod());
        currentFuncContext = null;
        return instructions;
    }


    // ************************** Control Statements
    @Override
    public InstructionSet visitBlink_if(BlinkParser.Blink_ifContext ctx) {
        // blink_if: IF expr=expression THEN true_value=statement false_value=blink_else? END;
        InstructionSet instructions = new InstructionSet();
        instructions.appendInstruction(new Comment(ctx.getStart().getLine() + ":visit_IF: " + ctx.getText()));
        instructions.appendInstructionSet(visit(ctx.expr));
        instructions.appendInstruction(new InstructionCommand(Instruction.call, new Label("_myPop"))); // get result of expr
        instructions.appendInstruction(new InstructionCommand(Instruction.compare, new IntegerLiteral("0"), Register.rax)); // get result of expr
        ifCount++;
        int thisIF = ifCount;
        instructions.appendInstruction(new InstructionCommand(Instruction.jne, new Label("_doif" + thisIF)));
        instructions.appendInstruction(new InstructionCommand(Instruction.jump, new Label("_else" + thisIF)));
        instructions.appendInstruction(new LabelInstruction("_doif" + thisIF));
        instructions.appendInstructionSet(visit(ctx.true_value));
        instructions.appendInstruction(new InstructionCommand(Instruction.jump, new Label("_endif" + thisIF)));
        instructions.appendInstruction(new LabelInstruction("_else" + thisIF));
        if (ctx.false_value != null)
            instructions.appendInstructionSet(visit(ctx.false_value));
        else instructions.appendInstructionSet(InstructionUtils.pushToMyStack(new IntegerLiteral("0")));

        instructions.appendInstruction(new LabelInstruction("_endif" + thisIF));
        return instructions;
    }

    @Override
    public InstructionSet visitLoop(BlinkParser.LoopContext ctx) {
        // loop: LOOP WHILE expr=expression DO value=statement END;
        InstructionSet instructions = new InstructionSet();
        instructions.appendInstruction(new Comment(ctx.getStart().getLine() + ":visit_LOOP: " + ctx.getText()));
        ifCount++;
        int thisIF = ifCount;
        instructions.appendInstruction(new LabelInstruction("_while" + thisIF));
        instructions.appendInstructionSet(visit(ctx.expr));
        instructions.appendInstruction(new InstructionCommand(Instruction.call, new Label("_myPop"))); // get result of expr
        instructions.appendInstruction(new InstructionCommand(Instruction.compare, new IntegerLiteral("0"), Register.rax)); // evaluate result
        instructions.appendInstruction(new InstructionCommand(Instruction.jne, new Label("_whileBody" + thisIF)));
        instructions.appendInstruction(new InstructionCommand(Instruction.jump, new Label("_whileEnd" + thisIF)));
        instructions.appendInstruction(new LabelInstruction("_whileBody" + thisIF));
        instructions.appendInstructionSet(visit(ctx.value));
        instructions.appendInstruction(new InstructionCommand(Instruction.jump, new Label("_while" + thisIF)));
        instructions.appendInstruction(new LabelInstruction("_whileEnd" + thisIF));

        return instructions;
    }

    // ************************** BINARY EXPRESSIONS

    @Override
    public InstructionSet visitConcat(BlinkParser.ConcatContext ctx) {
        InstructionSet instructions = new InstructionSet();
        instructions.appendInstruction(new Comment(ctx.getStart().getLine() + ":visitConcat: " + ctx.getText()));

        instructions.appendInstructionSet(visit(ctx.first));
        if (SymbolTable.getTypeContext("con_" + ctx.first.getText(), currentFuncContext) != Type.string)
            instructions.appendInstructionSet(InstructionUtils.convertInttoString());

        instructions.appendInstructionSet(visit(ctx.rest));
        if (SymbolTable.getTypeContext("con_" + ctx.rest.getText(), currentFuncContext) != Type.string)
            instructions.appendInstructionSet(InstructionUtils.convertInttoString());

        instructions.appendInstruction(new InstructionCommand(Instruction.call, new Label("_myPop")));
        instructions.appendInstruction(new InstructionCommand(Instruction.move, Register.rax, Register.rsi));

        instructions.appendInstruction(new InstructionCommand(Instruction.call, new Label("_myPop")));
        instructions.appendInstruction(new InstructionCommand(Instruction.move, Register.rax, Register.rdi));

        instructions.appendInstruction(new InstructionCommand(Instruction.call, new Label("concatStr"))); // C standard lib
        instructions.appendInstructionSet(InstructionUtils.pushToMyStack(Register.rax));

        return instructions;
    }

    @Override
    public InstructionSet visitAdd(BlinkParser.AddContext ctx) {
        // | first=expression PLUS rest=expression # add
        InstructionSet instructions = new InstructionSet();
        instructions.appendInstruction(new Comment(ctx.getStart().getLine() + ":visitAdd: " + ctx.getText()));
        instructions.appendInstructionSet(visit(ctx.first));
        instructions.appendInstructionSet(visit(ctx.rest));
        instructions.appendInstructionSet(InstructionUtils.handleBinaryExpression(Instruction.add));
        return instructions;
    }

    @Override
    public InstructionSet visitSubtract(BlinkParser.SubtractContext ctx) {
        InstructionSet instructions = new InstructionSet();
        instructions.appendInstruction(new Comment(ctx.getStart().getLine() + ":visitSub: " + ctx.getText()));
        instructions.appendInstructionSet(visit(ctx.first));
        instructions.appendInstructionSet(visit(ctx.rest));
        instructions.appendInstructionSet(InstructionUtils.handleBinaryExpression(Instruction.sub));
        return instructions;
    }


    @Override
    public InstructionSet visitDivide(BlinkParser.DivideContext ctx) {
        InstructionSet instructions = new InstructionSet();
        instructions.appendInstruction(new Comment(ctx.getStart().getLine() + ":visitDiv: " + ctx.getText()));
        instructions.appendInstructionSet(visit(ctx.first));
        instructions.appendInstructionSet(visit(ctx.rest));
        instructions.appendInstructionSet(InstructionUtils.handleBinaryExpression(Instruction.div));
        return instructions;
    }

    @Override
    public InstructionSet visitMultiply(BlinkParser.MultiplyContext ctx) {
        InstructionSet instructions = new InstructionSet();
        instructions.appendInstruction(new Comment(ctx.getStart().getLine() + ":visitMul: " + ctx.getText()));
        instructions.appendInstructionSet(visit(ctx.first));
        instructions.appendInstructionSet(visit(ctx.rest));
        instructions.appendInstructionSet(InstructionUtils.handleBinaryExpression(Instruction.mul));
        return instructions;
    }

    @Override
    public InstructionSet visitNegate(BlinkParser.NegateContext ctx) {
        InstructionSet instructions = new InstructionSet();
        instructions.appendInstruction(new Comment(ctx.getStart().getLine() + ":visitNegate: " + ctx.getText()));
        instructions.appendInstructionSet(visit(ctx.first));
        instructions.appendInstruction(new InstructionCommand(Instruction.call, new Label("_myPop")));
        instructions.appendInstruction(new InstructionCommand(Instruction.neg, Register.rax));
        instructions.appendInstructionSet(InstructionUtils.pushToMyStack(Register.rax));
        return instructions;
    }

    @Override
    public InstructionSet visitGreater(BlinkParser.GreaterContext ctx) {
        InstructionSet instructions = new InstructionSet();
        instructions.appendInstruction(new Comment(ctx.getStart().getLine() + ":visitGreater: " + ctx.getText()));
        instructions.appendInstructionSet(visit(ctx.first));
        instructions.appendInstructionSet(visit(ctx.rest));
        if (SymbolTable.getTypeContext("gr_" + ctx.rest.getText(), currentFuncContext) == Type.string)
            instructions.appendInstructionSet(InstructionUtils.compareTwoStrings());
        ifCount++;
        instructions.appendInstructionSet(InstructionUtils.handleBooleanExpressions(Instruction.jg, ifCount));
        return instructions;
    }

    @Override
    public InstructionSet visitLess_equal(BlinkParser.Less_equalContext ctx) {
        InstructionSet instructions = new InstructionSet();
        instructions.appendInstruction(new Comment(ctx.getStart().getLine() + ":visitLess_Equal: " + ctx.getText()));
        instructions.appendInstructionSet(visit(ctx.first));
        instructions.appendInstructionSet(visit(ctx.rest));
        if (SymbolTable.getTypeContext("lq_" + ctx.rest.getText(), currentFuncContext) == Type.string)
            instructions.appendInstructionSet(InstructionUtils.compareTwoStrings());
        ifCount++;
        instructions.appendInstructionSet(InstructionUtils.handleBooleanExpressions(Instruction.jle, ifCount));
        return instructions;
    }

    @Override
    public InstructionSet visitEqual(BlinkParser.EqualContext ctx) {
        InstructionSet instructions = new InstructionSet();
        instructions.appendInstruction(new Comment(ctx.getStart().getLine() + ":visitEqual: " + ctx.getText()));
        instructions.appendInstructionSet(visit(ctx.first));
        instructions.appendInstructionSet(visit(ctx.rest));
        if (SymbolTable.getTypeContext("eq_" + ctx.rest.getText(), currentFuncContext) == Type.string)
            instructions.appendInstructionSet(InstructionUtils.compareTwoStrings());
        ifCount++;
        instructions.appendInstructionSet(InstructionUtils.handleBooleanExpressions(Instruction.je, ifCount));
        return instructions;
    }

    @Override
    public InstructionSet visitLess(BlinkParser.LessContext ctx) {
        InstructionSet instructions = new InstructionSet();
        instructions.appendInstruction(new Comment(ctx.getStart().getLine() + ":visitLess: " + ctx.getText()));
        instructions.appendInstructionSet(visit(ctx.first));
        instructions.appendInstructionSet(visit(ctx.rest));
        if (SymbolTable.getTypeContext("ls_" + ctx.rest.getText(), currentFuncContext) == Type.string)
            instructions.appendInstructionSet(InstructionUtils.compareTwoStrings());
        ifCount++;
        instructions.appendInstructionSet(InstructionUtils.handleBooleanExpressions(Instruction.jl, ifCount));
        return instructions;
    }

    @Override
    public InstructionSet visitGreater_equal(BlinkParser.Greater_equalContext ctx) {
        InstructionSet instructions = new InstructionSet();
        instructions.appendInstruction(new Comment(ctx.getStart().getLine() + ":visitGreater_Equal: " + ctx.getText()));
        instructions.appendInstructionSet(visit(ctx.first));
        instructions.appendInstructionSet(visit(ctx.rest));
        if (SymbolTable.getTypeContext("gq_" + ctx.rest.getText(), currentFuncContext) == Type.string)
            instructions.appendInstructionSet(InstructionUtils.compareTwoStrings());
        ifCount++;
        instructions.appendInstructionSet(InstructionUtils.handleBooleanExpressions(Instruction.jge, ifCount));
        return instructions;
    }

    @Override
    public InstructionSet visitNot(BlinkParser.NotContext ctx) {
        InstructionSet instructions = new InstructionSet();
        instructions.appendInstruction(new Comment(ctx.getStart().getLine() + ":visitNot: " + ctx.getText()));
        instructions.appendInstructionSet(visit(ctx.first));
        instructions.appendInstruction(new InstructionCommand(Instruction.call, new Label("_myPop")));
        instructions.appendInstruction(new InstructionCommand(Instruction.compare, new IntegerLiteral("0"), Register.rax)); // get result of expr
        ifCount++;
        int thisIF = ifCount;
        instructions.appendInstruction(new InstructionCommand(Instruction.jne, new Label("_doif" + thisIF)));
        instructions.appendInstruction(new InstructionCommand(Instruction.jump, new Label("_else" + thisIF)));
        instructions.appendInstruction(new LabelInstruction("_doif" + thisIF));
        instructions.appendInstructionSet(InstructionUtils.pushToMyStack(new IntegerLiteral("0")));
        instructions.appendInstruction(new InstructionCommand(Instruction.jump, new Label("_endif" + thisIF)));
        instructions.appendInstruction(new LabelInstruction("_else" + thisIF));
        instructions.appendInstructionSet(InstructionUtils.pushToMyStack(new IntegerLiteral("1")));
        instructions.appendInstruction(new LabelInstruction("_endif" + thisIF));
        return instructions;
    }

    @Override
    public InstructionSet visitAnd(BlinkParser.AndContext ctx) {
        InstructionSet instructions = new InstructionSet();
        instructions.appendInstruction(new Comment(ctx.getStart().getLine() + ":visitAnd: " + ctx.getText()));
        instructions.appendInstructionSet(visit(ctx.first));
        instructions.appendInstructionSet(visit(ctx.rest));
        instructions.appendInstructionSet(InstructionUtils.handleBinaryExpression(Instruction.and));
        return instructions;
    }

    @Override
    public InstructionSet visitOr(BlinkParser.OrContext ctx) {
        InstructionSet instructions = new InstructionSet();
        instructions.appendInstruction(new Comment(ctx.getStart().getLine() + ":visitOr: " + ctx.getText()));
        instructions.appendInstructionSet(visit(ctx.first));
        instructions.appendInstructionSet(visit(ctx.rest));
        instructions.appendInstructionSet(InstructionUtils.handleBinaryExpression(Instruction.or));
        return instructions;
    }

    // VVVVVVVVVV Var operations

    @Override
    public InstructionSet visitVariable(BlinkParser.VariableContext ctx) {
        // variable: id=IDENTIFIER  typ=declared_type? ASSIGNMENT value=statement;
        InstructionSet instructions = new InstructionSet();
        instructions.appendInstruction(new Comment(ctx.getStart().getLine() + ":visitVariable: " + ctx.id.getText()));
        InstructionSet visitedResult = visit(ctx.value); //ends with push

        if (currentFuncContext != null)
            instructions.appendInstructionSet(InstructionUtils.createFunctionVariable(visitedResult, ctx.id.getText(), currentFuncContext)); // function
        else if (currentClassContext != null)
            instructions.appendInstructionSet(InstructionUtils.createClassVariable(visitedResult, ctx.id.getText(), currentClassContext)); // member
        else globals.appendInstructionSet(InstructionUtils.createGlobalVariable(visitedResult, ctx.id.getText())); // global

        if (ctx.typ != null)
            instructions.appendInstructionSet(InstructionUtils.checkAssignClasstoValue(visit(ctx.typ).getCommands().get(0).toString(), Register.rax)); // all these end with a pop which is in rax


        return instructions;
    }

    @Override
    public InstructionSet visitReassign(BlinkParser.ReassignContext ctx) {
        // reassign: id=IDENTIFIER ASSIGNMENT value=statement;
        InstructionSet instructions = new InstructionSet();
        instructions.appendInstruction(new Comment(ctx.getStart().getLine() + ":Reassign: " + ctx.getText()));
        instructions.appendInstructionSet(visit(ctx.value)); // should end with a push to the stack
        String name = ctx.id.getText();
        int originalSize = instructions.getSize();

        if (currentFuncContext != null) { // If i'm in a function, check the function vars
            instructions.appendInstructionSet(InstructionUtils.reassignFunctionVariable(currentFuncContext, name));
            if (instructions.getSize() > originalSize) return instructions; // if var is found, return
        }

        if (currentClassContext != null) { // if i'm in a class, check the class vars
            instructions.appendInstructionSet(InstructionUtils.reassignClassVariable(currentClassContext, currentFuncContext, name));
            if (instructions.getSize() > originalSize) return instructions;
        }

        instructions.appendInstructionSet(InstructionUtils.reassignGlobalVariable(name)); //It's definitely a global variable
        return instructions;
    }


    @Override
    public InstructionSet visitId(BlinkParser.IdContext ctx) {
        //  id=IDENTIFIER (br=L_SQUARE values+=expression R_SQUARE)* # id
        InstructionSet instructions = new InstructionSet();
        instructions.appendInstruction(new Comment(ctx.getStart().getLine() + ":visitID: " + ctx.getText()));
        String name = ctx.id.getText();
        int originalSize = instructions.getSize();

        if (currentFuncContext != null) { // If i'm in a function, check the function vars
            instructions.appendInstructionSet(InstructionUtils.getFunctionVariable(currentFuncContext, name));
            if (instructions.getSize() > originalSize) return instructions; // if var is found, return
        }

        if (currentClassContext != null) { // if i'm in a class, check the class vars
            instructions.appendInstructionSet(InstructionUtils.getClassVariable(currentClassContext, currentFuncContext, name));
            if (instructions.getSize() > originalSize) return instructions;
        }

        instructions.appendInstructionSet(InstructionUtils.getGlobalVariable(name)); //It's definitely a global variable
        return instructions;
    }

    // **************************   LEAFES

    @Override
    public InstructionSet visitInt(BlinkParser.IntContext ctx) {
        InstructionSet instructions = new InstructionSet();
        instructions.appendInstruction(new Comment(ctx.getStart().getLine() + ":visitInt: " + ctx.getText()));
        instructions.appendInstructionSet(InstructionUtils.pushToMyStack(new IntegerLiteral(ctx.getText())));
        return instructions;
    }

    @Override
    public InstructionSet visitTrue(BlinkParser.TrueContext ctx) {
        InstructionSet instructions = new InstructionSet();
        instructions.appendInstruction(new Comment(ctx.getStart().getLine() + ":visitTrue: " + ctx.getText()));
        instructions.appendInstructionSet(InstructionUtils.pushToMyStack(new IntegerLiteral("1")));
        return instructions;
    }

    @Override
    public InstructionSet visitFalse(BlinkParser.FalseContext ctx) {
        InstructionSet instructions = new InstructionSet();
        instructions.appendInstruction(new Comment(ctx.getStart().getLine() + ":visitFalse: " + ctx.getText()));
        instructions.appendInstructionSet(InstructionUtils.pushToMyStack(new IntegerLiteral("0")));
        return instructions;
    }

    @Override
    public InstructionSet visitNil(BlinkParser.NilContext ctx) {
        InstructionSet instructions = new InstructionSet();
        instructions.appendInstruction(new Comment(ctx.getStart().getLine() + ":visitNil: " + ctx.getText()));
        instructions.appendInstructionSet(InstructionUtils.pushToMyStack(new IntegerLiteral("0")));
        return instructions;
    }

    @Override
    public InstructionSet visitMe(BlinkParser.MeContext ctx) {
        InstructionSet instructions = new InstructionSet();
        instructions.appendInstruction(new Comment(ctx.getStart().getLine() + ":visitMe: " + ctx.getText()));

        if (currentFuncContext == null) // it's definitely inside a class, so all that's left to check is if we're in the constructor or just a regular function
            instructions.appendInstructionSet(InstructionUtils.pushToMyStack(InstructionUtils.getConstructorPointerParam()));
        else
            instructions.appendInstructionSet(InstructionUtils.getFuncParam(currentFuncContext.getLocalSS(), currentFuncContext.getParamOffset("classPointer")));

        return instructions;
    }

    @Override
    public InstructionSet visitString(BlinkParser.StringContext ctx) {
        InstructionSet instructions = new InstructionSet();
        instructions.appendInstruction(new Comment(ctx.getStart().getLine() + ":visitString: " + ctx.getText()));
        instructions.appendInstruction(new StabsInstruction("data"));
        instructions.appendInstruction(new LabelInstruction("_str" + stringCount));
        instructions.appendInstruction(new StabsInstruction("string", ctx.getText()));
        instructions.appendInstruction(new StabsInstruction("text"));
        instructions.appendInstructionSet(InstructionUtils.customPushToMyStack(Instruction.lea, new VariableReference("_str" + stringCount)));
        stringCount++;
        return instructions;
    }

    @Override
    public InstructionSet visitDeclared_type(BlinkParser.Declared_typeContext ctx) {
        InstructionSet instructions = new InstructionSet();
        instructions.appendInstruction(new LabelInstruction(ctx.typ.getText()));
        return instructions;
    }

    // ******************************   IGNORED
    @Override
    public InstructionSet visitString_type(BlinkParser.String_typeContext ctx) {
        return new InstructionSet();
    }

    @Override
    public InstructionSet visitBool_type(BlinkParser.Bool_typeContext ctx) {
        return new InstructionSet();
    }

    @Override
    public InstructionSet visitCustom_type(BlinkParser.Custom_typeContext ctx) {
        return new InstructionSet();
    }


    @Override
    public InstructionSet visitArguments(BlinkParser.ArgumentsContext ctx) {
        return new InstructionSet();
    }

    @Override
    public InstructionSet visitStatement(BlinkParser.StatementContext ctx) {
        return skipStep(ctx.children);
    }

    @Override
    public InstructionSet visitExpr(BlinkParser.ExprContext ctx) {
        return skipStep(ctx.children);
    }

    @Override
    public InstructionSet visitParen_expr(BlinkParser.Paren_exprContext ctx) {
        return visit(ctx.expr);
    }

    @Override
    public InstructionSet visitPrimary(BlinkParser.PrimaryContext ctx) {
        return skipStep(ctx.children);
    }

    @Override
    public InstructionSet visitVar_decl(BlinkParser.Var_declContext ctx) {
        return skipStep(ctx.children);
    }

    @Override
    public InstructionSet visitMethod_decl(BlinkParser.Method_declContext ctx) {
        return skipStep(ctx.children);
    }

    @Override
    public InstructionSet visitTerminal(TerminalNode node) {
        return new InstructionSet();
    }

    @Override
    public InstructionSet visitMember_var_decl(BlinkParser.Member_var_declContext ctx) {
        return skipStep(ctx.children);
    }

    @Override
    public InstructionSet visitParameters(BlinkParser.ParametersContext ctx) {
        return skipStep(ctx.children);
    }

    @Override
    public InstructionSet visitMember_method_decl(BlinkParser.Member_method_declContext ctx) {
        return skipStep(ctx.children);
    }

    @Override
    public InstructionSet visitType(BlinkParser.TypeContext ctx) {
        return skipStep(ctx.children);
    }

    @Override
    public InstructionSet visitInt_type(BlinkParser.Int_typeContext ctx) {
        return new InstructionSet();
    }

    @Override
    public InstructionSet visitBlink_else(BlinkParser.Blink_elseContext ctx) {
        return visit(ctx.false_value);
    }

    @Override
    public InstructionSet visitArgument(BlinkParser.ArgumentContext ctx) {
        return new InstructionSet();
    }

}

