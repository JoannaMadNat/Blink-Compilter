package org.main.symbols;

import lombok.Data;
import org.main.Type;

@Data
public class VarDecl extends Declaration {
    int offset = -1;
    public VarDecl(String name, Type type) {
        this.name = name;
        this.type = type;
    }

    public VarDecl(String name, Type type, int offset) {
        this.name = name;
        this.type = type;
        this.offset = offset;
    }
}
