fun main() {
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

    val fooBarParams = HashMap<String, Expr>()
    fooBarParams["a"] = SymVal("a")
    fooBarParams["b"] = SymVal("b")
    val test = createExecTreeNodes(fooBarAst, fooBarParams)
    val test2 = createExecTreeNodes(prog1, HashMap())
    val test3 = createExecTreeNodes(prog2, HashMap())
    printTree(test)
    printTree(test2)
    printTree(test3)
}