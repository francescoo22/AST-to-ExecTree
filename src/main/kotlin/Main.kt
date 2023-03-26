fun main() {
    /**
     * There are 3 AST program examples which are converted to ExecTreeNodes and printed.
     * One of them is the example from the paper.
     * Note that for the FooBar example, the parameters a and b are passed as a map to the createExecTreeNodes function.
     */
    val fooBarAst = Block(
        Let(Var("x"), Const(1)),
        Let(Var("y"), Const(0)),
        If( NEq(Var("a"), Const(0)),
            Block(
                Let(Var("y"), Plus(Const(3), Var("x"))),
                If( Eq(Var("b"), Const(0)),
                    Let(Var("x"), Mul(Const(2), Plus(Var("a"), Var("b")))),
                )
            )
        ),
        Minus(Var("x"), Var("y"))
    )

    val prog1 = Block(
        Let (Var("a"), Const(1)),
        Let (Var("b"), Const(2)),
        If ( Eq(Var("a"), Var("b")),
            Let (Var("a"), Const(2)),
            Let (Var("b"), Const(3))
        ),
        Minus(Var("a"), Var("b"))
    )

    val prog2 = Block(
        Let (Var("a"), Const(1)),
        Let (Var("b"), Const(2)),
        Let (Var("x"), Plus(Var("a"), Var("b"))),
        Let (Var("y"), Minus(Var("x"), Const(5))),
        Minus(Var("a"), Var("b"))
    )

    // FooBar example
    val fooBarParams = HashMap<String, Expr>()
    fooBarParams["a"] = SymVal("a")
    fooBarParams["b"] = SymVal("b")
    val test = createExecTreeNodes(fooBarAst, fooBarParams)
    println("FooBar example:")
    printTree(test)

    // Prog1 example
    val test2 = createExecTreeNodes(prog1, HashMap())
    println("Prog1 example:")
    printTree(test2)

    // Prog2 example
    val test3 = createExecTreeNodes(prog2, HashMap())
    println("Prog2 example:")
    printTree(test3)
}