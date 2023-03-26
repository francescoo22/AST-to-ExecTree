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
    return if (root.thenChild == null){
        mutableListOf(root)
    } else {
        if (root.isCond && root.elseChild == null){
            leafs.add(root)
        }

        leafs.addAll(getLeafs(root.thenChild!!))
        if (root.elseChild != null){
            leafs.addAll(getLeafs(root.elseChild!!))
        }
        leafs

    }
}

// param , pi : List<Expr>
fun createExecTreeNodes(expr : Expr) : ExecTreeNode {
    when (expr){
        is Block -> {
            var prev : ExecTreeNode? = null
            for (blockExpr in expr.exprs){
                if (prev != null){
                    val leafs = getLeafs(prev)
                    for (leaf in leafs){
                        if(leaf.isCond)  leaf.elseChild = createExecTreeNodes(blockExpr)
                        else leaf.thenChild = createExecTreeNodes(blockExpr)
                    }
                } else {
                    prev = createExecTreeNodes(blockExpr)
                }

            }
            return if (prev == null){
                ExecTreeNode(null, null, Block(), listOf(), listOf())
            } else {
                prev
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

fun printTree(root: ExecTreeNode?){
    if (root == null) return

    println(root.nextExpr)
    printSubtree(root, "")
    print("\n")
}

fun printSubtree (root: ExecTreeNode?, prefix: String){
    if(root == null) return

    val hasLeft = root.thenChild != null
    val hasRight = root.elseChild != null
    if (!hasLeft && !hasRight) return

    print(prefix)
    if(hasLeft && hasRight) print("├── ")
    if(!hasLeft && hasRight) print("├── ")

    if(hasRight){
        val printStrand = (hasLeft && hasRight && (root.elseChild?.elseChild != null || root.elseChild?.thenChild != null))
        val newPrefix = prefix + (if (printStrand) "│   " else "    ")
        println(root.elseChild?.nextExpr)
        printSubtree(root.elseChild, newPrefix);
    }

    if(hasLeft){
        println ((if(hasRight) prefix else "") + "└── " + root.thenChild?.nextExpr)
        printSubtree(root.thenChild, prefix + "    ");
    }
}