sealed class Expr

class Block(vararg val exprs: Expr) : Expr(){
    override fun toString(): String = exprs.fold("") { a, b -> "$a $b" }
}

data class Const(val value: Int) : Expr()
data class Var(val name: String) : Expr()
data class Let(val variable: Var, val value: Expr) : Expr()
data class Eq(val left: Expr, val right: Expr) : Expr()
data class NEq(val left: Expr, val right: Expr) : Expr()
data class If(val cond: Expr, val thenExpr: Expr, val elseExpr: Expr? = null) : Expr()
data class Plus(val left: Expr, val right: Expr) : Expr()
data class Minus(val left: Expr, val right: Expr) : Expr()
data class Mul(val left: Expr, val right: Expr) : Expr()

data class SymVal(val name: String) : Expr()

fun negation (expr: Expr): Expr{
    return when(expr){
        is Eq -> NEq(expr.left, expr.right)
        is NEq -> Eq(expr.left, expr.right)
        else -> throw Exception("Expected Eq or NEq expression but got $expr")
    }
}
