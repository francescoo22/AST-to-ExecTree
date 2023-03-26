data class ExecTreeNode(
    var thenChild: ExecTreeNode?,
    var elseChild: ExecTreeNode?,
    val nextExpr: Expr,
    val env: HashMap<String, Expr> = hashMapOf(),
    val Pi: List<Expr>,
    val isCond: Boolean = false
) {
    override fun toString(): String {
        /**
         * function used to pretty print the ExecTreeNode with colors
         * @return a string representing the ExecTreeNode
         */
        val red = "\u001b[31m"
        val blue = "\u001b[34m"
        val magenta = "\u001b[35m"
        val reset = "\u001b[0m"
        return magenta + "next: $nextExpr $reset|$red pi: $Pi $reset|$blue S: $env $reset"
    }

    fun getLeafs() : MutableList<ExecTreeNode> {
        /**
         * function that returns all the leafs of the ExecTreeNode
         * @return a mutable list of ExecTreeNode that are leafs of the current ExecTreeNode
         */
        val leafs :  MutableList<ExecTreeNode> = mutableListOf()
        return if (this.thenChild == null){
            mutableListOf(this)
        } else {
            if (this.isCond && this.elseChild == null){
                // even if this is not a leaf, I need to add it because is an if without else
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
    /**
     * function that creates an ExecTreeNode from an expression and an environment,
     * the function uses recursive pattern matching on the expression to create the ExecTreeNode
     * @param expr the expression to use
     * @param env if the expression uses parameters, they must be in the environment as SymVal
     * @param pi the pi list to use
     * @return the ExecTreeNode created
     */
    when (expr){
        is Block -> {
            // I loop through the block expressions and
            // at each iteration I create a new ExecTreeNode from the leafs of the previous iteration
            // prev is always the previous iteration ExecTreeNode
            var prev : ExecTreeNode? = null
            for (blockExpr in expr.exprs){
                if (prev != null){
                    val leafs = prev.getLeafs()
                    for (leaf in leafs){
                        val newMap = HashMap(leaf.env)
                        if (leaf.nextExpr is Let){
                            // if the next expression is a let, I evaluate the value and add it to the environment
                            val result = evalExpr(leaf.nextExpr.value, leaf.env)
                            newMap[leaf.nextExpr.variable.name] = result
                        }
                        if(leaf.isCond)
                            // if the leaf is a condition without else, I add the negation of the condition to the pi list
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
        printSubtree(root.thenChild, newPrefix)
    }

    if(hasRight){
        println ((if(hasLeft) prefix else "") + "└── " + root.elseChild)
        printSubtree(root.elseChild, "$prefix    ")
    }
}