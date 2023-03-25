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

data class ExecTreeNode(
    // if children.size == 2 -> the first is the then branch and the second is the else
//    val children: MutableList<ExecTreeNode>,
    var thenChild: ExecTreeNode?,
    var elseChild: ExecTreeNode?,
    val nextExpr: Expr,
    val S: List<Expr>,
    val Pi: List<Expr>,
    val isCond: Boolean = false
)

// si può fare easy senza mutable
fun getLeafs(root : ExecTreeNode) : MutableList<ExecTreeNode> {
    val leafs :  MutableList<ExecTreeNode> = mutableListOf()
    if (root.thenChild == null){
        return mutableListOf(root)
    } else {
        if (root.isCond && root.elseChild == null){
            leafs.add(root)
        }

        leafs.addAll(getLeafs(root.thenChild!!))
        if (root.elseChild != null){
            leafs.addAll(getLeafs(root.elseChild!!))
        }
        return leafs

    }
//    if (root.children.isEmpty()){
//        return mutableListOf(root)
//    }
//    for (child in root.children){
//        // special case for conditions without else branch
//        if(child.children.size == 1 && child.isCond){
//            leafs.add(root)
//        }
//        leafs.addAll(getLeafs(child))
//    }
//    return leafs
}

fun createExecTreeNodes(expr : Expr) : ExecTreeNode {
    when (expr){
        is Block -> {
            var prev : ExecTreeNode? = null
            for (blockExpr in expr.exprs){
                if (prev != null){
                    val leafs = getLeafs(prev)
                    for (leaf in leafs){
                        if(leaf.isCond){
                            leaf.elseChild = createExecTreeNodes(blockExpr)
                        } else {
                            leaf.thenChild = createExecTreeNodes(blockExpr)
                        }
                    }
                } else {
                    prev = createExecTreeNodes(blockExpr)
                }

            }
            if (prev == null){
                return ExecTreeNode(null, null, Block(), listOf(), listOf())
            } else {
                return prev
            }
        }
        is If -> {
            val thenChild = createExecTreeNodes(expr.thenExpr)
            if (expr.elseExpr != null){
                val elseChild = createExecTreeNodes(expr.elseExpr)
                return ExecTreeNode(thenChild, elseChild, expr.cond, listOf(), listOf(), true)
            }
            return ExecTreeNode(thenChild, null, expr.cond, listOf(), listOf(), true)
        }
        else -> {
            return ExecTreeNode(null, null, expr, listOf(), listOf())
        }
    }
}

fun prettyPrintExecTree(node : ExecTreeNode , indent : String = "") {
    println(indent + node.nextExpr.toString())
    if(node.thenChild != null) prettyPrintExecTree(node.thenChild!!, indent + "  ")
    if(node.elseChild != null) prettyPrintExecTree(node.elseChild!!, indent + "  ")
}
