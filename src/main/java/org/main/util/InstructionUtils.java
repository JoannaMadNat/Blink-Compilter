// this is the file to contain all the (mostly) ugly code that clutters up the main CodeGen file.

package org.main.util;

import org.main.Type;
import org.main.instructions.*;
import org.main.instructions.parameters.*;
import org.main.symbols.ClassDecl;
import org.main.symbols.MethodDecl;
import org.main.symbols.SymbolTable;
import org.main.symbols.VarDecl;

import java.util.ArrayList;


public class InstructionUtils {
    public static Register[] argRegs = {Register.rdi, Register.rsi, Register.rdx, Register.rcx, Register.r8, Register.r9};

    static int calOuterParamOffset(int varOffset) {
        return 16 + (varOffset * 8); //skipping rbp and ret
    }

    static int getFuncParameterOffset(int offset, int localSS) {
        if (offset < 0) {
            return localSS + (offset * 8);
        }
        return 0;
    }

    static int calFuncLocalOffset(int varOffset) {
        return ((8 * varOffset) + 8) * -1;
    }

    static int calFuncSavedRegOffset(int localSS, int varOffset) {
        return (localSS + 8 + ((6 + varOffset) * 8)) * -1;
    }

    // VVV Public functions
    public static int calClassMemberOffset(int offset) {
        return offset * 8;
    }

    public static OffsetRegister getConstructorPointerParam() {
        return new OffsetRegister(Register.rbp, -8);
    }

    public static String getFunctionCallName(String id, String prefix) {
        boolean isLib = false;
        for (MethodDecl libfunc : SymbolTable.stdlib)
            if (libfunc.getName().equals(id)) {
                isLib = true;
                break;
            }
        if (!isLib)
            return prefix + id;
        return id;
    }

    public static InstructionSet buildVFT(String prefix, ClassDecl newClass) {
        InstructionSet instructions = new InstructionSet();
        instructions.appendInstruction(new StabsInstruction("data"));

        instructions.appendInstruction(new LabelInstruction("VFT" + newClass.getName()));
        String parent = "0";
        if (newClass.getInheritsFrom() != null)
            parent = "VFT" + newClass.getInheritsFrom().getName();
        instructions.appendInstruction(new StabsInstruction("quad", parent));

        for (var method : newClass.getMethods())
            instructions.appendInstruction(new StabsInstruction("quad", method.getName()));
        instructions.appendInstruction(new StabsInstruction("text"));

        return instructions;
    }

    public static InstructionSet checkAssignClasstoValue(String type, Register value) {
        InstructionSet instructions = new InstructionSet();
        if (type.equals(Type.bool.getName()) || type.equals(Type.integer.getName()) || type.equals(Type.string.getName()) || type.equals(Type.nil.getName()))
            return instructions;

        instructions.appendInstruction(new InstructionCommand(Instruction.move, new IntegerLiteral("VFT" + type), Register.rdi)); // parameter 1
        instructions.appendInstruction(new InstructionCommand(Instruction.move, value, Register.rsi));  // parameter 2

        instructions.appendInstruction(new InstructionCommand(Instruction.move, value, Register.r12));          // save value
        instructions.appendInstruction(new InstructionCommand(Instruction.call, new Label("checkAssign")));
        instructions.appendInstruction(new InstructionCommand(Instruction.move, Register.r12, value));   // restore value
        return instructions;
    }

    public static InstructionSet convertInttoString() {
        InstructionSet instructions = new InstructionSet();
        instructions.appendInstruction(new InstructionCommand(Instruction.call, new Label("_myPop")));
        instructions.appendInstruction(new InstructionCommand(Instruction.move, Register.rax, Register.rdi));
        instructions.appendInstruction(new InstructionCommand(Instruction.call, new Label("itos")));
        instructions.appendInstructionSet(pushToMyStack(Register.rax));
        return instructions;
    }

    public static InstructionSet compareTwoStrings() {
        InstructionSet instructions = new InstructionSet();
        instructions.appendInstruction(new InstructionCommand(Instruction.call, new Label("_myPop")));
        instructions.appendInstruction(new InstructionCommand(Instruction.move, Register.rax, Register.rsi));
        instructions.appendInstruction(new InstructionCommand(Instruction.call, new Label("_myPop")));
        instructions.appendInstruction(new InstructionCommand(Instruction.move, Register.rax, Register.rdi));

        instructions.appendInstruction(new InstructionCommand(Instruction.call, new Label("compString")));
        instructions.appendInstructionSet(pushToMyStack(Register.rax));
        instructions.appendInstructionSet(pushToMyStack(new IntegerLiteral("0"))); // it has to return 2 values
        return instructions;
    }

    public static InstructionSet pushToMyStack(Parameter argument) {
        InstructionSet instructions = new InstructionSet();
        instructions.appendInstructionSet(customPushToMyStack(Instruction.move, argument));
        return instructions;
    }

    public static InstructionSet customPushToMyStack(Instruction inst, Parameter argument) {
        InstructionSet instructions = new InstructionSet();
        instructions.appendInstruction(new InstructionCommand(inst, argument, Register.r11));
        instructions.appendInstruction(new InstructionCommand(Instruction.call, new Label("_myPush")));
        return instructions;
    }

    public static InstructionSet startMethod(String name) {
        InstructionSet instructions = new InstructionSet();
        instructions.appendInstruction(new StabsInstruction("global", name));
        instructions.appendInstruction(new LabelInstruction(name));
        instructions.appendInstruction(new InstructionCommand(Instruction.push, Register.rbp));
        instructions.appendInstruction(new InstructionCommand(Instruction.move, Register.rsp, Register.rbp));
        return instructions;
    }

    public static InstructionSet endMethod() {
        InstructionSet instructions = new InstructionSet();
        instructions.appendInstruction(new InstructionCommand(Instruction.move, Register.rbp, Register.rsp));
        instructions.appendInstruction(new InstructionCommand(Instruction.pop, Register.rbp));
        instructions.appendInstruction(new InstructionCommand(Instruction.ret));
        return instructions;
    }

    public static InstructionSet getFuncParam(int localSS, int offset) {
        InstructionSet instructions = new InstructionSet();
        Parameter result;

        if (offset < 0)
            result = new OffsetRegister(Register.rbp, calFuncSavedRegOffset(localSS, offset));
        else
            result = new OffsetRegister(Register.rbp, calOuterParamOffset(offset));

        instructions.appendInstructionSet(pushToMyStack(result));
        return instructions;
    }

    public static ClassDecl getOwner(String className, ClassDecl currentClassContext, MethodDecl currentFuncContext) {
        if (className.equals("me")) return currentClassContext;

        ClassDecl owner;
        VarDecl variable = SymbolTable.lookup(className, VarDecl.class); // global variable
        Type dotType;
        if (variable != null) dotType = variable.getType();
        else dotType = SymbolTable.getTypeContext(className, currentFuncContext);
        owner = SymbolTable.lookup(dotType.getName(), ClassDecl.class);
        return owner;
    }

    public static InstructionSet reassignFuncLocal(VarDecl vary) {
        InstructionSet instructions = new InstructionSet();
        instructions.appendInstruction(new InstructionCommand(Instruction.call, new Label("_myPop")));
        if(vary.getType().isCustom())
            instructions.appendInstructionSet(checkAssignClasstoValue(vary.getType().getName(), Register.rax));

        instructions.appendInstruction(new InstructionCommand(Instruction.move, Register.rax, new OffsetRegister(Register.rbp, calFuncLocalOffset(vary.getOffset()))));
        instructions.appendInstructionSet(pushToMyStack(Register.rax));
        return instructions;
    }

    public static InstructionSet reassignFuncParam(int localSS, VarDecl vary) {
        InstructionSet instructions = new InstructionSet();
        instructions.appendInstruction(new InstructionCommand(Instruction.call, new Label("_myPop")));
        if(vary.getType().isCustom())
            instructions.appendInstructionSet(checkAssignClasstoValue(vary.getType().getName(), Register.rax));

        if (vary.getOffset() < 0)
            instructions.appendInstruction(new InstructionCommand(Instruction.move, Register.rax, new OffsetRegister(Register.rbp, calFuncSavedRegOffset(localSS, vary.getOffset()))));
         else
            instructions.appendInstruction(new InstructionCommand(Instruction.move, Register.rax, new OffsetRegister(Register.rbp, calOuterParamOffset(vary.getOffset()))));

        instructions.appendInstructionSet(pushToMyStack(Register.rax));
        return instructions;
    }

    public static InstructionSet handleFunctionCall(String name, ClassDecl owner, ArrayList<VarDecl> parameters, int calleeParamSS) {
        InstructionSet instructions = new InstructionSet();
        int paramSZ = parameters.size();
        int paramCounter = calleeParamSS / 8;
        int outerParamSZ = paramSZ - argRegs.length;
        int SS = 0;
        if(outerParamSZ >0)
            SS = paramCounter - outerParamSZ;

        if (paramSZ > 0) {
            // load parameters into the registers and stack
            for (int i = paramSZ + SS; i > 0; i--) {
                if (i <= argRegs.length) {
                    instructions.appendInstruction(new InstructionCommand(Instruction.call, new Label("_myPop")));
                    instructions.appendInstructionSet(checkAssignClasstoValue(parameters.get(i-1).getType().getName(), Register.rax));
                    instructions.appendInstruction(new InstructionCommand(Instruction.move, Register.rax, argRegs[i - 1]));
                } else {
                    if (paramCounter > outerParamSZ)
                        instructions.appendInstruction(new InstructionCommand(Instruction.move, new IntegerLiteral("0"), Register.rax));
                    else {
                        instructions.appendInstruction(new InstructionCommand(Instruction.call, new Label("_myPop")));
                        instructions.appendInstructionSet(checkAssignClasstoValue(parameters.get(i-1).getType().getName(), Register.rax));
                    }
                    instructions.appendInstruction(new InstructionCommand(Instruction.push, Register.rax));
                    paramCounter--;
                }
            }
        }

        if (owner == null)
            instructions.appendInstruction(new InstructionCommand(Instruction.call, new Label(name))); // call it by name
        else {
            instructions.appendInstruction(new InstructionCommand(Instruction.call, new Label("nullCheck"))); //rdi should already be in place
            instructions.appendInstruction(new InstructionCommand(Instruction.move, new OffsetRegister(Register.rax, 0), Register.r12));
            instructions.appendInstruction(new InstructionCommand(Instruction.call, new OffsetRegister(Register.r12, owner.getMemberMethodOffset(name) * 8)));
        }

        //push the return value to the stack
        instructions.appendInstructionSet(pushToMyStack(Register.rax));

        if (paramSZ > argRegs.length) // remove shadowspace for parameters
            instructions.appendInstruction(new InstructionCommand(Instruction.add, new IntegerLiteral(String.valueOf(calleeParamSS)), Register.rsp)); // "subq    $16, %rsp";
        return instructions;
    }

    public static InstructionSet handleBinaryExpression(Instruction cmd) {
        InstructionSet instructions = new InstructionSet();

        instructions.appendInstruction(new InstructionCommand(Instruction.call, new Label("_myPop")));
        instructions.appendInstruction(new InstructionCommand(Instruction.move, Register.rax, Register.rbx));   // Put first in %rbx
        instructions.appendInstruction(new InstructionCommand(Instruction.call, new Label("_myPop")));          // Put rest in %rax

        if (cmd == Instruction.mul)
            instructions.appendInstruction(new InstructionCommand(Instruction.mul, Register.rbx));
        else if (cmd == Instruction.div) {
            instructions.appendInstruction(new InstructionCommand(Instruction.cqo)); // sign extends rax to rdx
            instructions.appendInstruction(new InstructionCommand(Instruction.div, Register.rbx));
        } else
            instructions.appendInstruction(new InstructionCommand(cmd, Register.rbx, Register.rax));

        instructions.appendInstructionSet(pushToMyStack(Register.rax));
        return instructions;
    }

    public static InstructionSet handleBooleanExpressions(Instruction cmd, int thisIF) {
        InstructionSet instructions = new InstructionSet();
        instructions.appendInstruction(new InstructionCommand(Instruction.call, new Label("_myPop")));
        instructions.appendInstruction(new InstructionCommand(Instruction.move, Register.rax, Register.rbx));   // Put first in %rbx
        instructions.appendInstruction(new InstructionCommand(Instruction.call, new Label("_myPop")));          // Put rest in %rax

        instructions.appendInstruction(new InstructionCommand(Instruction.compare, Register.rbx, Register.rax));

        instructions.appendInstruction(new InstructionCommand(cmd, new Label("_doif" + thisIF)));
        instructions.appendInstruction(new InstructionCommand(Instruction.jump, new Label("_else" + thisIF)));

        instructions.appendInstruction(new LabelInstruction("_doif" + thisIF)); // do if
        instructions.appendInstructionSet(pushToMyStack(new IntegerLiteral("1")));
        instructions.appendInstruction(new InstructionCommand(Instruction.jump, new Label("_endif" + thisIF)));

        instructions.appendInstruction(new LabelInstruction("_else" + thisIF)); // do else
        instructions.appendInstructionSet(pushToMyStack(new IntegerLiteral("0")));

        instructions.appendInstruction(new LabelInstruction("_endif" + thisIF)); // do end
        return instructions;
    }

    // *********************   Manage variable creation
    public static InstructionSet createGlobalVariable(InstructionSet visitedResult, String name) {
        InstructionSet instructions = new InstructionSet();
        instructions.appendInstruction(new StabsInstruction("data"));
        instructions.appendInstruction(new StabsInstruction("comm", "_" + name + ", 8, 8"));
        instructions.appendInstruction(new StabsInstruction("text"));
        instructions.appendInstructionSet(visitedResult);
        instructions.appendInstruction(new InstructionCommand(Instruction.call, new Label("_myPop")));
        instructions.appendInstruction(new InstructionCommand(Instruction.move, Register.rax, new VariableReference("_" + name)));
        return instructions;
    }

    public static InstructionSet createFunctionVariable(InstructionSet visitedResult, String name, MethodDecl currentFuncContext) {
        InstructionSet instructions = new InstructionSet();
        instructions.appendInstructionSet(visitedResult); // needs to end with a push to the stack
        instructions.appendInstruction(new InstructionCommand(Instruction.call, new Label("_myPop")));
        instructions.appendInstruction(new InstructionCommand(Instruction.move, Register.rax, new OffsetRegister(Register.rbp, calFuncLocalOffset(currentFuncContext.getLocalOffset(name)))));
        return instructions;
    }

    public static InstructionSet createClassVariable(InstructionSet visitedResult, String name, ClassDecl currentClassContext) {
        InstructionSet instructions = new InstructionSet();
        instructions.appendInstructionSet(visitedResult); // needs to end with a push to the stack
        instructions.appendInstruction(new InstructionCommand(Instruction.move, getConstructorPointerParam(), Register.rdi));
        instructions.appendInstruction(new InstructionCommand(Instruction.call, new Label("nullCheck")));
        instructions.appendInstruction(new InstructionCommand(Instruction.move, Register.rax, Register.r12)); // chose r12 just cause.. it's a placeholder

        instructions.appendInstruction(new InstructionCommand(Instruction.call, new Label("_myPop")));
        instructions.appendInstruction(new InstructionCommand(Instruction.move, Register.rax, new OffsetRegister(Register.r12, calClassMemberOffset(currentClassContext.getMemberVarOffset(name)))));
        return instructions;
    }

    // ******************* Manage getting IDs
    public static InstructionSet getFunctionVariable(MethodDecl currentFuncContext, String name) {
        // Check if var is a parameter
        InstructionSet instructions = new InstructionSet();
        int offset = currentFuncContext.getParamOffset(name); // check if var is function parameter
        if (offset != -23) {
            instructions.appendInstructionSet(getFuncParam(currentFuncContext.getLocalSS(), offset));
            return instructions;
        }

        offset = currentFuncContext.getLocalOffset(name); //Check if var is a function local
        if (offset != -23) {
            instructions.appendInstructionSet(pushToMyStack(new OffsetRegister(Register.rbp, calFuncLocalOffset(offset))));
            return instructions;
        }
        return instructions; // returns an empty list if not found
    }

    public static InstructionSet getClassVariable(ClassDecl currentClassContext, MethodDecl currentFuncContext, String name) {
        InstructionSet instructions = new InstructionSet();
        int offset = currentClassContext.getMemberVarOffset(name);
        if (offset == -1)
            return instructions;

        MethodDecl stub = currentFuncContext;
        if (currentFuncContext == null) {
            stub = new MethodDecl(currentClassContext.getName(), currentClassContext.getType());
            stub.insertClassPointer();
        }

        instructions.appendInstructionSet(getFuncParam(stub.getLocalSS(), stub.getParamOffset("classPointer")));
        instructions.appendInstruction(new InstructionCommand(Instruction.call, new Label("_myPop")));
        instructions.appendInstruction(new InstructionCommand(Instruction.move, Register.rax, Register.rdi));
        instructions.appendInstruction(new InstructionCommand(Instruction.call, new Label("nullCheck")));
        instructions.appendInstructionSet(pushToMyStack(new OffsetRegister(Register.rax, calClassMemberOffset(offset))));
        return instructions;
    }

    public static InstructionSet getGlobalVariable(String name) {
        InstructionSet instructions = new InstructionSet();
        instructions.appendInstructionSet(InstructionUtils.pushToMyStack(new VariableReference("_" + name)));
        return instructions;
    }

    // ******************** Manage Reassign Variables

    public static InstructionSet reassignGlobalVariable(String name) {
        InstructionSet instructions = new InstructionSet();
        instructions.appendInstruction(new InstructionCommand(Instruction.call, new Label("_myPop")));
        VarDecl vary = SymbolTable.lookup(name, VarDecl.class);
        if(vary.getType().isCustom())
            instructions.appendInstructionSet(checkAssignClasstoValue(vary.getType().getName(), Register.rax));

        instructions.appendInstruction(new InstructionCommand(Instruction.move, Register.rax, new VariableReference("_" + name)));   // Put first in %rbx
        instructions.appendInstructionSet(pushToMyStack(Register.rax));
        return instructions;
    }

    public static InstructionSet reassignFunctionVariable(MethodDecl currentFuncContext, String name) {
        InstructionSet instructions = new InstructionSet();
        VarDecl vary = currentFuncContext.getLocal(name);
        if (vary != null) {
            instructions.appendInstructionSet(reassignFuncLocal(vary)); // local
            return instructions;
        }

        vary = currentFuncContext.getParam(name);
        if (vary !=null) {
            instructions.appendInstructionSet(reassignFuncParam(currentFuncContext.getLocalSS(), vary)); // parameter
            return instructions;
        }
        return instructions;
    }

    public static InstructionSet reassignClassVariable(ClassDecl currentClassContext, MethodDecl currentFuncContext, String name) {
        InstructionSet instructions = new InstructionSet();
        VarDecl vary = currentClassContext.getMemberVar(name);
        if (vary == null)
            return instructions;

        MethodDecl stub = currentFuncContext;
        if (currentFuncContext == null) {
            // If this is the class constructor, you access the class pointer through the first parameter
            // this only happens in the constructor. basically treat the constructor as a function
            stub = new MethodDecl(currentClassContext.getName(), currentClassContext.getType());
            stub.insertClassPointer();
        }

        if(vary.getType().isCustom()) {
            instructions.appendInstruction(new InstructionCommand(Instruction.call, new Label("_myPop")));
            instructions.appendInstructionSet(checkAssignClasstoValue(vary.getType().getName(), Register.rax));
            instructions.appendInstructionSet(pushToMyStack(Register.rax));
        }

        instructions.appendInstructionSet(getFuncParam(stub.getLocalSS(), stub.getParamOffset("classPointer")));
        instructions.appendInstruction(new InstructionCommand(Instruction.call, new Label("_myPop")));
        instructions.appendInstruction(new InstructionCommand(Instruction.move, Register.rax, Register.rdi));
        instructions.appendInstruction(new InstructionCommand(Instruction.call, new Label("nullCheck")));
        instructions.appendInstruction(new InstructionCommand(Instruction.move, Register.rax, Register.r12));

        instructions.appendInstruction(new InstructionCommand(Instruction.call, new Label("_myPop")));
        instructions.appendInstruction(new InstructionCommand(Instruction.move, Register.rax, new OffsetRegister(Register.r12, calClassMemberOffset(vary.getOffset()))));
        instructions.appendInstructionSet(pushToMyStack(Register.rax));
        return instructions;
    }
}
