sealed class Expr

class Block(vararg val exprs: Expr) : Expr(){
    override fun toString(): String = exprs.fold("") { a, b -> "$a $b" }
}

data class Const(val value: Int) : Expr(){
    override fun toString(): String = value.toString()
}
data class Var(val name: String) : Expr(){
    override fun toString(): String = name
}
data class Let(val variable: Var, val value: Expr) : Expr(){
    override fun toString(): String = "let $variable = $value"
}
data class Eq(val left: Expr, val right: Expr) : Expr(){
    override fun toString(): String = "$left == $right"
}
data class NEq(val left: Expr, val right: Expr) : Expr(){
    override fun toString(): String = "$left != $right"
}
data class If(val cond: Expr, val thenExpr: Expr, val elseExpr: Expr? = null) : Expr(){
    override fun toString(): String = "if $cond then $thenExpr else $elseExpr"
}
data class Plus(val left: Expr, val right: Expr) : Expr(){
    override fun toString(): String = "$left + $right"
}
data class Minus(val left: Expr, val right: Expr) : Expr(){
    override fun toString(): String = "$left - $right"
}
data class Mul(val left: Expr, val right: Expr) : Expr(){
    override fun toString(): String = "$left * $right"
}

data class SymVal(val name: String) : Expr(){
    override fun toString(): String = name
}

fun negation (expr: Expr): Expr{
    /**
     * function that given a condition expression returns its negation
     * @param expr must be a condition expression
     * @return the negation of the given expression
     * @throws Exception if the given expression is not a condition expression
     */
    return when(expr){
        is Eq -> NEq(expr.left, expr.right)
        is NEq -> Eq(expr.left, expr.right)
        else -> throw Exception("Expected Eq or NEq expression but got $expr")
    }
}

fun evalExpr(expr: Expr, env: HashMap<String, Expr>): Expr{
    /**
     * function that evaluates an expression given an environment
     * @param expr the expression to evaluate
     * @param env the environment to use
     * @return a Const expression if the expression can be evaluated (there are no symbolic variables), the original expression otherwise
     */
    when(expr){
        is Var -> {
            return env[expr.name]!!
        }
        is Plus -> {
            val left = evalExpr(expr.left, env)
            val right = evalExpr(expr.right, env)
            return if (left is Const && right is Const){
                Const(left.value + right.value)
            } else {
                Plus(left, right)
            }
        }
        is Minus -> {
            val left = evalExpr(expr.left, env)
            val right = evalExpr(expr.right, env)
            return if (left is Const && right is Const){
                Const(left.value - right.value)
            } else {
                Minus(left, right)
            }
        }
        is Mul -> {
            val left = evalExpr(expr.left, env)
            val right = evalExpr(expr.right, env)
            return if (left is Const && right is Const){
                Const(left.value * right.value)
            } else {
                Mul(left, right)
            }
        }
        else -> return expr
    }
}