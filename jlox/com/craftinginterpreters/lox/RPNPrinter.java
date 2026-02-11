package com.craftinginterpreters.lox;

class RPNPrinter implements Expr.Visitor<String> {
  String print(Expr expr) {
    return expr.accept(this);
  }

  @Override
  public String visitTernaryExpr(Expr.Ternary expr) {
    return compose_rpn(expr.cond_op.lexeme + expr.br_op,
        expr.cond, expr.br_true, expr.br_true);
  }

  @Override
  public String visitBinaryExpr(Expr.Binary expr) {
    return compose_rpn(expr.operator.lexeme,
        expr.left, expr.right);
  }

  @Override
  public String visitGroupingExpr(Expr.Grouping expr) {
    return expr.expression.accept(this);
  }

  @Override
  public String visitLiteralExpr(Expr.Literal expr) {
    if (expr.value == null)
      return "nil";
    return expr.value.toString();
  }

  @Override
  public String visitUnaryExpr(Expr.Unary expr) {
    switch (expr.operator.lexeme) {
      case "-":
        return compose_rpn("@", expr.right);
      default:
        return compose_rpn(expr.operator.lexeme, expr.right);
    }

  }

  private String compose_rpn(String name, Expr... exprs) {
    StringBuilder builder = new StringBuilder();
    // for (Expr expr : exprs) {
    for (int i = exprs.length - 1; i >= 0; i--) {
      Expr expr = exprs[i];
      builder.append(expr.accept(this));
      builder.append(" ");
    }

    builder.append(name);
    return builder.toString();
  }

  public static void main(String[] args) {
    Expr expression = new Expr.Binary(
        new Expr.Unary(
            new Token(TokenType.MINUS, "-", null, 1),
            new Expr.Literal(123)),
        new Token(TokenType.STAR, "*", null, 1),
        new Expr.Grouping(
            new Expr.Literal(45.67)));

    System.out.println(new RPNPrinter().print(expression));
  }
}