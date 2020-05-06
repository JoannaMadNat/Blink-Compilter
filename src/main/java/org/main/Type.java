package org.main;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
@EqualsAndHashCode
public class Type {
    public static final Type string = new Type("string", false);
    public static final Type integer = new Type("int", false);
    public static final Type bool = new Type("bool", false);
    public static final Type nil = new Type("nil", false);
    public static final Type error = new Type("error", false);

    @NonNull private String name;
    @NonNull private boolean isCustom;
}
