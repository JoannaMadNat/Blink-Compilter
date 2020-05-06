package org.main.instructions;

public class Instruction {
    public static Instruction add = new Instruction("addq"),
            sub = new Instruction("subq"),
            push = new Instruction("pushq"),
            pop = new Instruction("popq"),
            call = new Instruction("call"),
            ret = new Instruction("ret"),
            compare = new Instruction("cmpq"),
            move = new Instruction("movq"),
            jg = new Instruction("jg"),
            jge = new Instruction("jge"),
            jl = new Instruction("jl"),
            jle = new Instruction("jle"),
            je = new Instruction("je"),
            jne = new Instruction("jne"),
            jump = new Instruction("jmp"),
            mul = new Instruction("imulq"),
            div = new Instruction("idivq"),
            neg = new Instruction("negq"),
            xor = new Instruction("xorq"),
            or = new Instruction("orq"),
            and = new Instruction("andq"),
            not = new Instruction("notq"),
            cqo = new Instruction("cqo"),
            lea = new Instruction("leaq");


    private String name;

    public Instruction(String name) {
        this.name = name;
    }

    /* getName
     * Arguments:
     *
     * Purpose: returns the name of an instruction
     */
    public String getName() {
        return this.name;
    }

}
