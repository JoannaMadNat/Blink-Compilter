package org.main.symbols;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.main.Type;

import java.util.ArrayList;

@Data
@EqualsAndHashCode(callSuper = false)
public class MethodDecl extends Declaration {
    public ArrayList<VarDecl> parameters = new ArrayList<>();
    public ArrayList<VarDecl> locals = new ArrayList<>();
    public int offset = -1;

    public int localSS = 0; // shadow space for local parameters
    public int paramSS = 0;

    int paramOffsetCount = -6;
    int localOffsetCount = 0;

    public MethodDecl(String name, Type type) {
        this.name = name;
        this.type = type;
    }

    // a copy constructor for MethodDecls
    public MethodDecl(MethodDecl meth) {
        parameters.addAll(meth.parameters);
        locals.addAll(meth.locals);

        paramOffsetCount = meth.paramOffsetCount;
        localOffsetCount = meth.localOffsetCount;
        offset = meth.offset;
        localSS = meth.localSS;
        paramSS = meth.paramSS;

        name = meth.name;
        type = meth.type;
    }

    public void addLocal(VarDecl newVar) {
        newVar.setOffset(localOffsetCount);
        locals.add(newVar);
        localOffsetCount++;
    }

    public void addParam(VarDecl newVar) {
        newVar.setOffset(paramOffsetCount);
        parameters.add(newVar);
        paramOffsetCount++;
    }

    // sz: nummber of local vars
    public void fillLocalSS() {
        int size = locals.size();
        localSS = size * 8;
        while (localSS % 16 != 0)
            localSS += 8; // add padding
    }

    // sz: desired overflow parameters, should already not include register params
    public void fillParamSS() {
        int size = parameters.size();
        if (size <= 6)
            return;
        paramSS = (size - 6) * 8;
        while (paramSS % 16 != 0)
            paramSS += 8; // add padding
    }

    // The parameter offsets are set AFTER all the parameters have been loaded
    public void populateParameterOffsets() {
        int count = -6;
        for (VarDecl parameter : parameters) {
            parameter.offset = count;
            count++;
        }
    }

    // For when the function is a class member function
    public void insertClassPointer() {
        ArrayList<VarDecl> newList = new ArrayList<>();
        newList.add(new VarDecl("classPointer", Type.integer));
        newList.addAll(parameters);
        parameters = newList;

        populateParameterOffsets(); // params have been changed
        fillParamSS();
    }

    // Readonly methods

    public int getParamOffset(String name) {
        for (VarDecl parameter : parameters)
            if (parameter.name.equals(name))
                return parameter.offset;
        return -23; // -23 because the offset can be -1
    }

    public int getLocalOffset(String name) {
        for (VarDecl local : locals)
            if (local.name.equals(name))
                return local.offset;
        return -23; // -23 just fot uniformity
    }

    public VarDecl getLocal(String name) {
        for (VarDecl local : locals)
            if (local.name.equals(name))
                return local;
        return null;
    }

    public VarDecl getParam(String name) {
        for (VarDecl param : parameters)
            if (param.name.equals(name))
                return param;
        return null;
    }

}
