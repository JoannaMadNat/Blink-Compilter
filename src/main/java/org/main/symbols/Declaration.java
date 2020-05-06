package org.main.symbols;
import org.main.Type;
import lombok.Data;

import java.util.ArrayList;

@Data
public class Declaration {
    int level = 0;
    String name;
    Type type;

    public void setLevel(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }
}
