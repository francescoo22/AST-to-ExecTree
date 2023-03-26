data class ExecTreeNode(
    var thenChild: ExecTreeNode?,
    var elseChild: ExecTreeNode?,
    val nextExpr: Expr,
    val env: HashMap<String, Expr> = hashMapOf(),
    val Pi: List<Expr>,
    val isCond: Boolean = false
) {
    override fun toString(): String {
        return "next: $nextExpr | pi: $Pi | S: $env"
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

fun createExecTreeNodes(expr : Expr, env: HashMap<String, Expr>, pi : List<Expr> = listOf()) : ExecTreeNode {
    when (expr){
        is Block -> {
            var prev : ExecTreeNode? = null
            for (blockExpr in expr.exprs){
                if (prev != null){
                    val leafs = prev.getLeafs()
                    for (leaf in leafs){
                        val newMap = HashMap(leaf.env)
                        if (leaf.nextExpr is Let){
                            val result = evalExpr(leaf.nextExpr.value, leaf.env)
                            newMap[leaf.nextExpr.variable.name] = result
                        }
                        if(leaf.isCond)
                            leaf.elseChild = createExecTreeNodes(blockExpr, newMap, leaf.Pi + listOf(negation(leaf.nextExpr)))
                        else leaf.thenChild = createExecTreeNodes(blockExpr, newMap, leaf.Pi)
                    }
                } else {
                    prev = createExecTreeNodes(blockExpr, HashMap(env), pi)
                }

            }
            return if (prev == null){
                // empty block
                ExecTreeNode(null, null, Block(), HashMap(env), pi)
            } else {
                prev
            }
        }
        is If -> {
            val thenChild = createExecTreeNodes(expr.thenExpr, HashMap(env), pi + listOf(expr.cond))
            if (expr.elseExpr != null){
                val elseChild = createExecTreeNodes(expr.elseExpr, HashMap(env), pi + listOf(negation(expr.cond)))
                return ExecTreeNode(thenChild, elseChild, expr.cond, HashMap(env), pi, true)
            }
            return ExecTreeNode(thenChild, null, expr.cond, HashMap(env), pi, true)
        }
//        is Let -> {
//            val result = evalExpr(expr.value, HashMap(env))
//            val newMap = HashMap(HashMap(env))
//            newMap[expr.variable.name] = result
//            return ExecTreeNode(null, null, expr, , pi)
//        }
        else -> {
            return ExecTreeNode(null, null, expr, HashMap(env), pi)
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