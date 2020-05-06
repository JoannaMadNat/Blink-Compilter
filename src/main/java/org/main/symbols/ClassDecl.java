package org.main.symbols;

import lombok.EqualsAndHashCode;
import org.main.Type;
import lombok.Data;

import java.util.ArrayList;

@Data
@EqualsAndHashCode(callSuper = false)
public class ClassDecl extends Declaration {
    public ArrayList<VarDecl> parameters = new ArrayList<>();
    ClassDecl inheritsFrom;
    ArrayList<VarDecl> locals = new ArrayList<>();
    ArrayList<MethodDecl> methods = new ArrayList<>();
    int heapCushion = 2;
    int VFTCushion = 1;

    int latestVarOffset = heapCushion;      // this offset is based on the heap
    int latestMethodOffset = VFTCushion;    // this offset is based on the VFT

    public ClassDecl(String name) {
        this.name = name;
        this.type = new Type(name, true);
        parameters.add(new VarDecl("classPointer", Type.integer, latestVarOffset));
        latestVarOffset++;
    }

    public void addMember(Declaration member, boolean inherited) {
        if (member.getClass().equals(VarDecl.class))
            addVariable((VarDecl) member, inherited);
        else addMethod((MethodDecl) member, inherited);
    }

    void addVariable(VarDecl member, boolean inherited) {
        String name = member.getName();
        if (inherited)
            name = "inherited";

        VarDecl mem = new VarDecl(name, member.getType());
        mem.offset = latestVarOffset;
        locals.add(mem);
        latestVarOffset++;
    }

    void addMethod(MethodDecl member, boolean inherited) {
        if (!inherited) {
            member.setName(getName() + "_" + member.getName());
            member.insertClassPointer();
        }
        member.setOffset(latestMethodOffset);
        methods.add(member);
        latestMethodOffset++;
    }

    public void addParameter(VarDecl param) {
        param.offset = latestVarOffset;
        parameters.add(param);
        latestVarOffset++;
    }

    public void addParent(ClassDecl parent) {
        inheritsFrom = parent;
        latestMethodOffset = parent.latestMethodOffset;
    }

    public void OverrideFunction(MethodDecl meth) {
        for (int i = 0; i<methods.size();i++)
            if(methods.get(i).getName().endsWith(meth.getName())) {
                meth.setName(this.getName() + "_" + meth.getName());
                meth.setOffset(methods.get(i).getOffset());
                meth.insertClassPointer();
                methods.set(i, meth);
                break;
            }
    }

    // Readonly methods
    public int getMemSize() {
        return (locals.size() + parameters.size() + heapCushion) * 8; // the 2 is the extra two spaces of cushion for inheritance
    }

    public int getMemberVarOffset(String name) {
        for (var param : parameters)
            if (param.getName().equals(name))
                return param.getOffset();
        for (var loc : locals)
            if (loc.getName().equals(name))
                return loc.getOffset();
        return -1;
    }

    public int getMemberMethodOffset(String name) {
        for (var meth : methods)
            if (meth.getName().endsWith(name))
                return meth.getOffset();

        return -1;
    }

    public VarDecl getMemberVar(String name) {
        for (var param : parameters)
            if (param.getName().equals(name))
                return param;
        for (var loc : locals)
            if (loc.getName().equals(name))
                return loc;
        return null;
    }

    public MethodDecl lookupMethod(String name) {
        for (MethodDecl symbol : methods)
            if (symbol.getClass().equals(MethodDecl.class) && symbol.name.equals(this.name + "_" + name))
                return symbol;

        if (inheritsFrom != null)
            for (MethodDecl symbol : methods)
                if (symbol.getClass().equals(MethodDecl.class) && symbol.name.equals(inheritsFrom.name + "_" + name))
                    return symbol;

        return null;
    }
}
