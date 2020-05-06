package org.main.instructions.parameters;

public class VariableReference extends Parameter{
    private String name;

    public VariableReference(String name) {
        this.name = name;
    }

    /* toString
     * Arguments:
     *
     * returns the register and offset as a formatted string
     */
    public String toString() {
        return this.name + "(" + Register.rip + ")";
    }
}
