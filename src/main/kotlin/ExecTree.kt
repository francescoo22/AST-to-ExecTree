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

    // si può fare easy senza mutable
    fun getLeafs() : MutableList<ExecTreeNode> {
        val leafs :  MutableList<ExecTreeNode> = mutableListOf()
        return if (this.thenChild == null){
            mutableListOf(this)
        } else {
            if (this.isCond && this.elseChild == null){
                leafs.add(this)
            }

            leafs.addAll(this.thenChild!!.getLeafs())
            if (this.elseChild != null){
                leafs.addAll(this.elseChild!!.getLeafs())
            }
            leafs

        }
    }
}

fun createExecTreeNodes(expr : Expr, pi : List<Expr> = listOf()) : ExecTreeNode {
    when (expr){
        is Block -> {
            var prev : ExecTreeNode? = null
            for (blockExpr in expr.exprs){
                if (prev != null){
                    val leafs = prev.getLeafs()
                    for (leaf in leafs){
                        if(leaf.isCond)
                            leaf.elseChild = createExecTreeNodes(blockExpr, leaf.Pi + listOf(negation(leaf.nextExpr)))
                        else leaf.thenChild = createExecTreeNodes(blockExpr, leaf.Pi)
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
    if(hasRight && hasLeft) print("├── ")
    if(!hasRight) print("└── ")

    if(hasLeft){
        val printStrand = (hasRight && (root.thenChild?.thenChild != null || root.thenChild?.elseChild != null))
        val newPrefix = prefix + (if (printStrand) "│   " else "    ")
        println(root.thenChild)
        printSubtree(root.thenChild, newPrefix);
    }

    if(hasRight){
        println ((if(hasLeft) prefix else "") + "└── " + root.elseChild)
        printSubtree(root.elseChild, "$prefix    ");
    }
}