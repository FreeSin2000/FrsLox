package com.craftinginterpreters.lox;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

class Resolver implements Expr.Visitor<Void>, Stmt.Visitor<Void> {
    private final Interpreter interpreter;
    private final Stack<Map<String, Variable>> scopes = new Stack<>();
    private FunctionType currentFunction = FunctionType.NONE;
    private LoopType currentLoop = LoopType.NONE;

    Resolver(Interpreter interpreter) {
        this.interpreter = interpreter;
    }

    private enum FunctionType {
        NONE,
        FUNCTION,
        STATIC_METHOD,
        INITIALIZER,
        GETTER,
        METHOD
    }

    private enum ClassType {
        NONE,
        CLASS
    }

    private ClassType currentClass = ClassType.NONE;

    private enum LoopType {
        NONE,
        LOOP
    }

    private enum VarType {
        FUN,
        VAR,
        PARAM,
        CLASS,
        THIS
    }

    private enum AccessType {
        READ,
        WRITE,
        THIS
    }

    private class Variable {
        final Token name;
        boolean ready;
        boolean used;
        VarType type;

        Variable(Token name, VarType type) {
            this.name = name;
            this.type = type;
            this.ready = false;
            this.used = false;
        }
    }

    void resolve(List<Stmt> statements) {
        for (Stmt statement : statements) {
            resolve(statement);
        }
    }

    private void beginScope() {
        scopes.push(new HashMap<String, Variable>());
    }

    private void endScope() {
        checkLocalUsed();
        scopes.pop();
    }

    private void checkLocalUsed() {
        if (scopes.isEmpty())
            return;
        for (Map.Entry<String, Variable> entry : scopes.peek().entrySet()) {
            Variable curVar = entry.getValue();
            if (curVar.used)
                continue;
            if (curVar.type == VarType.PARAM || curVar.type == VarType.THIS)
                continue;

            String message = switch (curVar.type) {
                case VAR -> "Local variable '" + entry.getKey() + "' is never used.";
                case FUN -> "Local function '" + entry.getKey() + "' is never called.";
                case CLASS -> "Local class '" + entry.getKey() + "' is never used.";
                // case PARAM -> "Function parameter '" + entry.getKey() + "' is never used.";
                default -> "Unused entity.";
            };
            Lox.error(curVar.name, message);
        }
    }

    private void declare(Token name, VarType type) {
        if (scopes.isEmpty())
            return;

        Map<String, Variable> scope = scopes.peek();
        if (scope.containsKey(name.lexeme)) {
            Lox.error(name,
                    "Already a variable with this name in this scope.");
        }

        scope.put(name.lexeme, new Variable(name, type));
    }

    private void define(Token name) {
        if (scopes.isEmpty())
            return;
        scopes.peek().get(name.lexeme).ready = true;
    }

    private void resolveLocal(Expr expr, Token name, AccessType type) {
        for (int i = scopes.size() - 1; i >= 0; i--) {
            if (scopes.get(i).containsKey(name.lexeme)) {
                interpreter.resolve(expr, scopes.size() - 1 - i);
                if (type == AccessType.READ) {
                    scopes.get(i).get(name.lexeme).used = true;
                }
                return;
            }
        }
    }

    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        beginScope();
        resolve(stmt.statements);
        endScope();
        return null;
    }

    @Override
    public Void visitClassStmt(Stmt.Class stmt) {
        ClassType enclosingClass = currentClass;
        currentClass = ClassType.CLASS;
        declare(stmt.name, VarType.CLASS);
        define(stmt.name);

        beginScope();
        scopes.peek().put("this", new Variable(new Token(TokenType.THIS, "this", null, stmt.name.line), VarType.THIS));

        for (Stmt.Function method : stmt.methods) {
            FunctionType declaration = FunctionType.METHOD;
            if (method.name.lexeme.equals("init")) {
                if (method.isGetter)
                    Lox.error(method.name, "Getters cannot be named 'init'.");
                declaration = FunctionType.INITIALIZER;
            }
            resolveFunction(method, declaration);
        }

        endScope();

        for (Stmt.Function method : stmt.staticMethods) {
            FunctionType declaration = FunctionType.STATIC_METHOD;
            if (method.name.lexeme.equals("init")) {
                Lox.error(method.name, "Static methods cannot be named 'init'.");
            }
            resolveFunction(method, declaration);
        }
        currentClass = enclosingClass;
        return null;
    }

    @Override
    public Void visitIfStmt(Stmt.If stmt) {
        resolve(stmt.condition);
        resolve(stmt.thenBranch);
        if (stmt.elseBranch != null)
            resolve(stmt.elseBranch);
        return null;
    }

    @Override
    public Void visitPrintStmt(Stmt.Print stmt) {
        resolve(stmt.expression);
        return null;
    }

    @Override
    public Void visitBreakStmt(Stmt.Break stmt) {
        if (currentLoop == LoopType.NONE) {
            Lox.error(stmt.keyword, "Can't break from none-loop code.");
        }
        return null;
    }

    @Override
    public Void visitReturnStmt(Stmt.Return stmt) {
        if (currentFunction == FunctionType.NONE) {
            Lox.error(stmt.keyword, "Can't return from top-level code.");
        }
        if (stmt.value != null) {
            if (currentFunction == FunctionType.INITIALIZER) {
                Lox.error(stmt.keyword,
                        "Can't return a value from an initializer.");
            }
            resolve(stmt.value);
        }

        return null;
    }

    @Override
    public Void visitWhileStmt(Stmt.While stmt) {
        resolve(stmt.condition);
        LoopType enclosingLoop = currentLoop;
        currentLoop = LoopType.LOOP;
        resolve(stmt.body);
        currentLoop = enclosingLoop;
        return null;
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        resolve(stmt.expression);
        return null;
    }

    @Override
    public Void visitFunctionStmt(Stmt.Function stmt) {
        declare(stmt.name, VarType.FUN);
        define(stmt.name);

        resolveFunction(stmt, FunctionType.FUNCTION);
        return null;
    }

    @Override
    public Void visitVarStmt(Stmt.Var stmt) {
        declare(stmt.name, VarType.VAR);
        if (stmt.initializer != null) {
            resolve(stmt.initializer);
        }
        define(stmt.name);
        return null;
    }

    @Override
    public Void visitAssignExpr(Expr.Assign expr) {
        resolve(expr.value);
        resolveLocal(expr, expr.name, AccessType.WRITE);
        return null;
    }

    @Override
    public Void visitTernaryExpr(Expr.Ternary expr) {
        resolve(expr.cond);
        resolve(expr.br_true);
        resolve(expr.br_false);
        return null;
    }

    @Override
    public Void visitLambdaExpr(Expr.Lambda expr) {
        resolveFunction(new Stmt.Function(null, expr.params, expr.body, false), FunctionType.FUNCTION);
        return null;
    }

    @Override
    public Void visitBinaryExpr(Expr.Binary expr) {
        resolve(expr.left);
        resolve(expr.right);
        return null;
    }

    @Override
    public Void visitCallExpr(Expr.Call expr) {
        resolve(expr.callee);

        for (Expr argument : expr.arguments) {
            resolve(argument);
        }

        return null;
    }

    @Override
    public Void visitGetExpr(Expr.Get expr) {
        resolve(expr.object);
        return null;
    }

    @Override
    public Void visitGroupingExpr(Expr.Grouping expr) {
        resolve(expr.expression);
        return null;
    }

    @Override
    public Void visitLiteralExpr(Expr.Literal expr) {
        return null;
    }

    @Override
    public Void visitLogicalExpr(Expr.Logical expr) {
        resolve(expr.left);
        resolve(expr.right);
        return null;
    }

    @Override
    public Void visitSetExpr(Expr.Set expr) {
        resolve(expr.value);
        resolve(expr.object);
        return null;
    }

    @Override
    public Void visitThisExpr(Expr.This expr) {
        if (currentClass == ClassType.NONE) {
            Lox.error(expr.keyword,
                    "Can't use 'this' outside of a class.");
            return null;
        }
        if (currentFunction == FunctionType.STATIC_METHOD) {
            Lox.error(expr.keyword, "Cannot use 'this' in a static method.");
        }
        resolveLocal(expr, expr.keyword, AccessType.THIS);
        return null;
    }

    @Override
    public Void visitUnaryExpr(Expr.Unary expr) {
        resolve(expr.right);
        return null;
    }

    @Override
    public Void visitVariableExpr(Expr.Variable expr) {
        if (!scopes.isEmpty() &&
                scopes.peek().get(expr.name.lexeme) != null &&
                scopes.peek().get(expr.name.lexeme).ready == Boolean.FALSE) {
            Lox.error(expr.name,
                    "Can't read local variable in its own initializer.");
        }
        resolveLocal(expr, expr.name, AccessType.READ);
        return null;
    }

    private void resolve(Stmt stmt) {
        stmt.accept(this);
    }

    private void resolve(Expr expr) {
        expr.accept(this);
    }

    private void resolveFunction(
            Stmt.Function function, FunctionType type) {
        FunctionType enclosingFunction = currentFunction;
        currentFunction = type;
        LoopType enclosingLoop = currentLoop;
        currentLoop = LoopType.NONE;

        beginScope();
        for (Token param : function.params) {
            declare(param, VarType.PARAM);
            define(param);
        }
        resolve(function.body);
        endScope();
        currentFunction = enclosingFunction;
        currentLoop = enclosingLoop;
    }

}