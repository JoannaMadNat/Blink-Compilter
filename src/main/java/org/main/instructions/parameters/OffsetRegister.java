package org.main.instructions.parameters;

public class OffsetRegister extends Parameter {
	private Register register;
	private int offset;
	
	public OffsetRegister(Register register, int offset) {
		this.register = register;
		this.offset = offset;
	}
	
	/* toString
	 * Arguments:
	 *   
	 * returns the register and offset as a formatted string
	 */
	public String toString() {
		String res = "";
		if(offset != 0)
			res+=this.offset;
		res += "(" + this.register.toString() + ")";
		return res;
	}
}
