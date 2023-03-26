data class ExecTreeNode(
    var thenChild: ExecTreeNode?,
    var elseChild: ExecTreeNode?,
    val nextExpr: Expr,
    val S: List<Expr>,
    val Pi: List<Expr>,
    val isCond: Boolean = false
) {
    override fun toString(): String {
        return "next: $nextExpr | pi: $Pi"
    }
}

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

fun createExecTreeNodes(expr : Expr, pi : List<Expr> = listOf()) : ExecTreeNode {
    when (expr){
        is Block -> {
            var prev : ExecTreeNode? = null
            for (blockExpr in expr.exprs){
                if (prev != null){
                    val leafs = getLeafs(prev)
                    for (leaf in leafs){
                        if(leaf.isCond)
                            leaf.elseChild = createExecTreeNodes(blockExpr, pi + listOf(negation(leaf.nextExpr)))
                        else leaf.thenChild = createExecTreeNodes(blockExpr, pi)
                    }
                } else {
                    prev = createExecTreeNodes(blockExpr, pi)
                }

            }
            return if (prev == null){
                ExecTreeNode(null, null, Block(), listOf(), pi)
            } else {
                prev
            }
        }
        is If -> {
            val thenChild = createExecTreeNodes(expr.thenExpr, pi + listOf(expr.cond))
            if (expr.elseExpr != null){
                val elseChild = createExecTreeNodes(expr.elseExpr, pi + listOf(negation(expr.cond)))
                return ExecTreeNode(thenChild, elseChild, expr.cond, listOf(), pi, true)
            }
            return ExecTreeNode(thenChild, null, expr.cond, listOf(), pi, true)
        }
        else -> {
            return ExecTreeNode(null, null, expr, listOf(), pi)
        }
    }
}

fun printTree(root: ExecTreeNode?){
    if (root == null) return

    println(root)
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
        println(root.elseChild)
        printSubtree(root.elseChild, newPrefix);
    }

    if(hasLeft){
        println ((if(hasRight) prefix else "") + "└── " + root.thenChild)
        printSubtree(root.thenChild, prefix + "    ");
    }
}