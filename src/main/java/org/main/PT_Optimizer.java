package org.main;

import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.antlr.v4.runtime.tree.TerminalNodeImpl;
import org.bju.BlinkBaseVisitor;
import org.bju.BlinkParser;
import java.util.ArrayList;
import java.util.List;

// this was done out of pure curiosity to see if I can actually literally prune a parse tree.. apparently I can, yay for digital gardening! =D
//  for obvious reasons, variables aren't supported since their value changes at runtime
public class PT_Optimizer extends BlinkBaseVisitor<Boolean> {
    private Boolean skipStep(List<ParseTree> children) {
        boolean res = true;
        for (var child : children)
            res &= visit(child);
        return res;
    }

    private ArrayList<ParseTree> replaceMe(BlinkParser.ExpressionContext ctx, String newVal) { // We're all the saaAAAaame ~~~
        // first, make a child that will be in charge of replacing my existence here
        ArrayList<ParseTree> children = new ArrayList<>();

        var newMe = new BlinkParser.IntContext(new BlinkParser.ExpressionContext());
        newMe.addChild(new TerminalNodeImpl(new CommonToken(2, newVal))); // The type means integer. "I'm you, but better."
        newMe.start = ctx.start;
        newMe.parent = ctx.parent;
        newMe.exception = ctx.exception;
        newMe.invokingState = ctx.invokingState;

        for (int i = 0; i < ctx.getParent().getChildCount(); i++) {
            if (ctx.getParent().getChild(i) == ctx)
                children.add(newMe); // Make a new list of children that contain the new child instead of the old one
            else children.add(ctx.getParent().getChild(i));
        }
        return children;
    }

    // Optimize these VVV (i.e. Frankenstein the heck out of this tree)
    @Override
    public Boolean visitAdd(BlinkParser.AddContext ctx) {
        boolean first = visit(ctx.first);
        boolean second = visit(ctx.rest);
        ctx.first = (BlinkParser.ExpressionContext) ctx.getChild(0); // in case the tree was changed during the visit
        ctx.rest = (BlinkParser.ExpressionContext) ctx.getChild(2);

        if (first && second)
            ctx.getParent().children = replaceMe(ctx, Integer.toString(Integer.parseInt(ctx.first.getText()) + Integer.parseInt(ctx.rest.getText())));

        return first && second;
    }

    @Override
    public Boolean visitSubtract(BlinkParser.SubtractContext ctx) {
        boolean first = visit(ctx.first);
        boolean second = visit(ctx.rest);
        ctx.first = (BlinkParser.ExpressionContext) ctx.getChild(0); // in case the tree was changed during the visit
        ctx.rest = (BlinkParser.ExpressionContext) ctx.getChild(2);

        if (first && second)
            ctx.getParent().children = replaceMe(ctx, Integer.toString(Integer.parseInt(ctx.first.getText()) - Integer.parseInt(ctx.rest.getText())));

        return first && second;
    }

    @Override
    public Boolean visitDivide(BlinkParser.DivideContext ctx) {
        boolean first = visit(ctx.first);
        boolean second = visit(ctx.rest);
        ctx.first = (BlinkParser.ExpressionContext) ctx.getChild(0); // in case the tree was changed during the visit
        ctx.rest = (BlinkParser.ExpressionContext) ctx.getChild(2);

        if (first && second)
            ctx.getParent().children = replaceMe(ctx, Integer.toString(Integer.parseInt(ctx.first.getText()) / Integer.parseInt(ctx.rest.getText())));

        return first && second;
    }

    @Override
    public Boolean visitMultiply(BlinkParser.MultiplyContext ctx) {
        boolean first = visit(ctx.first);
        boolean second = visit(ctx.rest);
        ctx.first = (BlinkParser.ExpressionContext) ctx.getChild(0); // in case the tree was changed during the visit
        ctx.rest = (BlinkParser.ExpressionContext) ctx.getChild(2);

        if (first && second)
            ctx.getParent().children = replaceMe(ctx, Integer.toString(Integer.parseInt(ctx.first.getText()) * Integer.parseInt(ctx.rest.getText())));

        return first && second;
    }

    @Override
    public Boolean visitNegate(BlinkParser.NegateContext ctx) {
        boolean first = visit(ctx.first);
        ctx.first = (BlinkParser.ExpressionContext) ctx.getChild(1); // in case the tree was changed during the visit

        if (first)
            ctx.getParent().children = replaceMe(ctx, Integer.toString(Integer.parseInt(ctx.first.getText()) * -1));

        return first;
    }

    @Override
    public Boolean visitNot(BlinkParser.NotContext ctx) {
        boolean first = visit(ctx.first);
        ctx.first = (BlinkParser.ExpressionContext) ctx.getChild(1);

        if (first)
            ctx.getParent().children = replaceMe(ctx, Integer.toString(ctx.first.getText().equals("true") ? 0 : 1));

        return first;
    }

    @Override
    public Boolean visitOr(BlinkParser.OrContext ctx) {
        boolean first = visit(ctx.first);
        boolean second = visit(ctx.rest);
        ctx.first = (BlinkParser.ExpressionContext) ctx.getChild(0); // in case the tree was changed during the visit
        ctx.rest = (BlinkParser.ExpressionContext) ctx.getChild(2);

        if (first && second)                                            // ex: if first is true or rest is true, return 1
            ctx.getParent().children = replaceMe(ctx, Integer.toString(ctx.first.getText().equals("true") || ctx.rest.getText().equals("true") ? 1 : 0));

        return first && second;
    }

    @Override
    public Boolean visitAnd(BlinkParser.AndContext ctx) {
        boolean first = visit(ctx.first);
        boolean second = visit(ctx.rest);
        ctx.first = (BlinkParser.ExpressionContext) ctx.getChild(0); // in case the tree was changed during the visit
        ctx.rest = (BlinkParser.ExpressionContext) ctx.getChild(2);

        if (first && second)
            ctx.getParent().children = replaceMe(ctx, Integer.toString(ctx.first.getText().equals("true") && ctx.rest.getText().equals("true") ? 1 : 0));

        return first && second;
    }

    @Override
    public Boolean visitLess(BlinkParser.LessContext ctx) {
        boolean first = visit(ctx.first);
        boolean second = visit(ctx.rest);
        ctx.first = (BlinkParser.ExpressionContext) ctx.getChild(0); // in case the tree was changed during the visit
        ctx.rest = (BlinkParser.ExpressionContext) ctx.getChild(2);

        if (first && second) {
            String firstText = ctx.first.getText();
            String restText = ctx.rest.getText();
            if(firstText.startsWith("\"") || restText.startsWith("\""))
                return false;

            ctx.getParent().children = replaceMe(ctx, Integer.toString(Integer.parseInt(firstText) < Integer.parseInt(restText) ? 1 : 0));
        }
        return first && second;
    }

    @Override
    public Boolean visitGreater_equal(BlinkParser.Greater_equalContext ctx) {
        boolean first = visit(ctx.first);
        boolean second = visit(ctx.rest);
        ctx.first = (BlinkParser.ExpressionContext) ctx.getChild(0); // in case the tree was changed during the visit
        ctx.rest = (BlinkParser.ExpressionContext) ctx.getChild(2);

        if (first && second) {
            String firstText = ctx.first.getText();
            String restText = ctx.rest.getText();
            if(firstText.startsWith("\"") || restText.startsWith("\""))
                return false;

            ctx.getParent().children = replaceMe(ctx, Integer.toString(Integer.parseInt(firstText) >= Integer.parseInt(restText) ? 1 : 0));
        }
        return first && second;
    }

    @Override
    public Boolean visitGreater(BlinkParser.GreaterContext ctx) {
        boolean first = visit(ctx.first);
        boolean second = visit(ctx.rest);
        ctx.first = (BlinkParser.ExpressionContext) ctx.getChild(0); // in case the tree was changed during the visit
        ctx.rest = (BlinkParser.ExpressionContext) ctx.getChild(2);

        if (first && second) {
            String firstText = ctx.first.getText();
            String restText = ctx.rest.getText();
            if(firstText.startsWith("\"") || restText.startsWith("\""))
                return false;

            ctx.getParent().children = replaceMe(ctx, Integer.toString(Integer.parseInt(firstText) > Integer.parseInt(restText) ? 1 : 0));
        }
        return first && second;
    }

    @Override
    public Boolean visitLess_equal(BlinkParser.Less_equalContext ctx) {
        boolean first = visit(ctx.first);
        boolean second = visit(ctx.rest);
        ctx.first = (BlinkParser.ExpressionContext) ctx.getChild(0); // in case the tree was changed during the visit
        ctx.rest = (BlinkParser.ExpressionContext) ctx.getChild(2);

        if (first && second) {
            String firstText = ctx.first.getText();
            String restText = ctx.rest.getText();
            if(firstText.startsWith("\"") || restText.startsWith("\""))
                return false;

            ctx.getParent().children = replaceMe(ctx, Integer.toString(Integer.parseInt(firstText) <= Integer.parseInt(restText) ? 1 : 0));
        }

        return first && second;
    }

    @Override
    public Boolean visitEqual(BlinkParser.EqualContext ctx) {
        boolean first = visit(ctx.first);
        boolean second = visit(ctx.rest);
        ctx.first = (BlinkParser.ExpressionContext) ctx.getChild(0); // in case the tree was changed during the visit
        ctx.rest = (BlinkParser.ExpressionContext) ctx.getChild(2);

        if (first && second) {
            String firstText = ctx.first.getText();
            String restText = ctx.rest.getText();
            if(firstText.startsWith("\"") || restText.startsWith("\""))
                return false;

            int f = firstText.equals("true")? 1 : firstText.equals("false")? 0 : Integer.parseInt(firstText);
            int r = restText.equals("true")? 1 : restText.equals("false")? 0 : Integer.parseInt(restText);

            ctx.getParent().children = replaceMe(ctx, Integer.toString(f == r ? 1 : 0));
        }

        return first && second;
    }

    @Override
    public Boolean visitParen_expr(BlinkParser.Paren_exprContext ctx) {
        boolean first = visit(ctx.expr);
        ctx.expr = (BlinkParser.StatementContext) ctx.getChild(1);

        if(first) {
            String text = ctx.expr.getText();
            if(text.startsWith("\""))
                return false;

            ArrayList<ParseTree> children = new ArrayList<>();

            var newMe = new BlinkParser.IntContext(new BlinkParser.ExpressionContext());
            newMe.addChild(new TerminalNodeImpl(new CommonToken(2, text))); // The type means integer. "I'm you, but better."
            newMe.start = ctx.start;
            newMe.parent = ctx.parent;
            newMe.exception = ctx.exception;
            newMe.invokingState = ctx.invokingState;

            for (int i = 0; i < ctx.getParent().getChildCount(); i++) {
                if (ctx.getParent().getChild(i) == ctx)
                    children.add(newMe); // Make a new list of children that contain the new child instead of the old one
                else children.add(ctx.getParent().getChild(i));
            }

            ctx.getParent().children = children;
        }

        return first;
    }

    @Override
    public Boolean visitConcat(BlinkParser.ConcatContext ctx) { // concat is thpecial
        boolean first = visit(ctx.first);
        boolean second = visit(ctx.rest);
        ctx.first = (BlinkParser.ExpressionContext) ctx.getChild(0); // in case the tree was changed during the visit
        ctx.rest = (BlinkParser.ExpressionContext) ctx.getChild(2);

        if(first && second) {
            String firstText = ctx.first.getText();
            if(firstText.startsWith("\""))
                firstText = firstText.substring(1, firstText.length() - 1);
            String restText = ctx.rest.getText();
            if(restText.startsWith("\""))
                restText = restText.substring(1, restText.length() - 1);

            ArrayList<ParseTree> children = new ArrayList<>();
            var newMe = new BlinkParser.StringContext(new BlinkParser.ExpressionContext());
            newMe.addChild(new TerminalNodeImpl(new CommonToken(3, '"' + firstText + restText + '"'))); // The type means integer. "I'm you, but better."
            newMe.start = ctx.start;
            newMe.parent = ctx.parent;
            newMe.exception = ctx.exception;
            newMe.invokingState = ctx.invokingState;

            for (int i = 0; i < ctx.getParent().getChildCount(); i++) {
                if (ctx.getParent().getChild(i) == ctx)
                    children.add(newMe); // Make a new list of children that contain the new child instead of the old one
                else children.add(ctx.getParent().getChild(i));
            }

            ctx.getParent().children = children;
        }
        return first && second;
    }

    // Leaf Nodes VVV

    @Override
    public Boolean visitTrue(BlinkParser.TrueContext ctx) {
        return true;
    }

    @Override
    public Boolean visitInt(BlinkParser.IntContext ctx) {
        return true;
    }

    @Override
    public Boolean visitFalse(BlinkParser.FalseContext ctx) {
        return true;
    }

    @Override
    public Boolean visitString(BlinkParser.StringContext ctx) {
        return true;
    }

    // return false if the value cannot be determined at semantic time
    @Override
    public Boolean visitNil(BlinkParser.NilContext ctx) {
        return false;
    }

    @Override
    public Boolean visitMe(BlinkParser.MeContext ctx) {
        return false;
    }

    @Override
    public Boolean visitId(BlinkParser.IdContext ctx) {
        return false;
    }

    @Override
    public Boolean visitCustom_type(BlinkParser.Custom_typeContext ctx) {
        return false;
    }

    // Ignore this ones VVV
    @Override
    public Boolean visitTerminal(TerminalNode node) {
        return false;
    }

    @Override
    public Boolean visitDeclared_type(BlinkParser.Declared_typeContext ctx) {
        return false;
    }

    @Override
    public Boolean visitNew(BlinkParser.NewContext ctx) {
        return skipStep(ctx.children);
    }

    @Override
    public Boolean visitOtherCall(BlinkParser.OtherCallContext ctx) {
        return skipStep(ctx.children);
    }


    @Override
    public Boolean visitFunction(BlinkParser.FunctionContext ctx) {
        return skipStep(ctx.children);
    }

    @Override
    public Boolean visitExpr(BlinkParser.ExprContext ctx) {
        return skipStep(ctx.children);
    }

    @Override
    public Boolean visitStatement(BlinkParser.StatementContext ctx) {
        return skipStep(ctx.children);
    }

    @Override
    public Boolean visitBlink_if(BlinkParser.Blink_ifContext ctx) {
        return skipStep(ctx.children);
    }

    @Override
    public Boolean visitBlink_else(BlinkParser.Blink_elseContext ctx) {
        return skipStep(ctx.children);
    }

    @Override
    public Boolean visitLoop(BlinkParser.LoopContext ctx) {
        return skipStep(ctx.children);
    }

    @Override
    public Boolean visitReassign(BlinkParser.ReassignContext ctx) {
        return skipStep(ctx.children);
    }

    @Override
    public Boolean visitParameters(BlinkParser.ParametersContext ctx) {
        return skipStep(ctx.children);
    }

    @Override
    public Boolean visitStart(BlinkParser.StartContext ctx) {
        return super.visitStart(ctx);
    }

    @Override
    public Boolean visitPrimary(BlinkParser.PrimaryContext ctx) {
        return skipStep(ctx.children);
    }

    @Override
    public Boolean visitBlink_class(BlinkParser.Blink_classContext ctx) {
        return skipStep(ctx.children);
    }

    @Override
    public Boolean visitInherits(BlinkParser.InheritsContext ctx) {
        return skipStep(ctx.children);
    }

    @Override
    public Boolean visitInherit_params(BlinkParser.Inherit_paramsContext ctx) {
        return skipStep(ctx.children);
    }

    @Override
    public Boolean visitVar_decl(BlinkParser.Var_declContext ctx) {
        return skipStep(ctx.children);
    }

    @Override
    public Boolean visitMethod_decl(BlinkParser.Method_declContext ctx) {
        return skipStep(ctx.children);
    }

    @Override
    public Boolean visitMember_var_decl(BlinkParser.Member_var_declContext ctx) {
        return skipStep(ctx.children);
    }

    @Override
    public Boolean visitMember_method_decl(BlinkParser.Member_method_declContext ctx) {
        return skipStep(ctx.children);
    }

    @Override
    public Boolean visitVariable(BlinkParser.VariableContext ctx) {
        return skipStep(ctx.children);
    }

    @Override
    public Boolean visitMethod(BlinkParser.MethodContext ctx) {
        return skipStep(ctx.children);
    }

    @Override
    public Boolean visitArguments(BlinkParser.ArgumentsContext ctx) {
        return skipStep(ctx.children);
    }

    @Override
    public Boolean visitArgument(BlinkParser.ArgumentContext ctx) {
        return skipStep(ctx.children);
    }
}
