package com.ballrop.app.feature.game.shape

data class ShapeNode(
    val gridX: Int,
    val gridY: Int
)

object ShapePatternFactory {

    fun create(shapeCode: Int): List<ShapeNode> {
        return when (shapeCode) {
            10 -> slashShape()
            12 -> plusShape()
            14 -> questionShape()
            15 -> heartShape()
            16 -> zigzagShape()
            17 -> parenthesesShape()
            18 -> snakeShape()
            19 -> ringShape()
            20 -> hashtagShape()
            21 -> dropletShape()
            22 -> stairsShape()
            23 -> goShape()
            24 -> houseShape()
            25 -> runShape()
            26 -> rightTriangleShape()
            27 -> yesShape()
            28 -> wallShape()
            29 -> letterBShape()
            30 -> letterAShape()
            31 -> letterLShape()
            32 -> letterRShape()
            33 -> letterOShape()
            34 -> letterPShape()
            35 -> winShape()
            36 -> letterLShape()
            else -> slashShape()
        }
    }

    private fun slashShape(): List<ShapeNode> {
        return listOf(
            ShapeNode(4, 0),
            ShapeNode(3, 1),
            ShapeNode(2, 2),
            ShapeNode(1, 3),
            ShapeNode(0, 4)
        )
    }

    private fun plusShape(): List<ShapeNode> {
        return listOf(
            ShapeNode(2, 0),
            ShapeNode(2, 1),
            ShapeNode(0, 2),
            ShapeNode(1, 2),
            ShapeNode(2, 2),
            ShapeNode(3, 2),
            ShapeNode(4, 2),
            ShapeNode(2, 3),
            ShapeNode(2, 4)
        )
    }

    private fun questionShape(): List<ShapeNode> {
        return listOf(
            ShapeNode(1, 0),
            ShapeNode(2, 0),
            ShapeNode(3, 0),

            ShapeNode(0, 1),
            ShapeNode(4, 1),

            ShapeNode(3, 2),
            ShapeNode(2, 3),

            ShapeNode(2, 5),
            ShapeNode(2, 7)
        )
    }

    private fun heartShape(): List<ShapeNode> {
        return listOf(
            ShapeNode(1, 0),
            ShapeNode(2, 0),
            ShapeNode(4, 0),
            ShapeNode(5, 0),

            ShapeNode(0, 1),
            ShapeNode(3, 1),
            ShapeNode(6, 1),

            ShapeNode(0, 2),
            ShapeNode(1, 2),
            ShapeNode(2, 2),
            ShapeNode(3, 2),
            ShapeNode(4, 2),
            ShapeNode(5, 2),
            ShapeNode(6, 2),

            ShapeNode(1, 3),
            ShapeNode(2, 3),
            ShapeNode(3, 3),
            ShapeNode(4, 3),
            ShapeNode(5, 3),

            ShapeNode(2, 4),
            ShapeNode(3, 4),
            ShapeNode(4, 4),

            ShapeNode(3, 5)
        )
    }

    private fun zigzagShape(): List<ShapeNode> {
        return listOf(
            ShapeNode(0, 0),
            ShapeNode(1, 1),
            ShapeNode(2, 2),
            ShapeNode(3, 1),
            ShapeNode(4, 0),

            ShapeNode(5, 1),
            ShapeNode(6, 2),
            ShapeNode(7, 1),
            ShapeNode(8, 0),

            ShapeNode(9, 1),
            ShapeNode(10, 2),
            ShapeNode(11, 1),
            ShapeNode(12, 0)
        )
    }

    private fun parenthesesShape(): List<ShapeNode> {
        return listOf(
            ShapeNode(1, 0),
            ShapeNode(0, 1),
            ShapeNode(0, 2),
            ShapeNode(0, 3),
            ShapeNode(1, 4),

            ShapeNode(5, 0),
            ShapeNode(6, 1),
            ShapeNode(6, 2),
            ShapeNode(6, 3),
            ShapeNode(5, 4)
        )
    }

    private fun snakeShape(): List<ShapeNode> {
        return listOf(
            ShapeNode(0, 0),
            ShapeNode(0, 1),
            ShapeNode(0, 2),
            ShapeNode(0, 3),
            ShapeNode(0, 4),
            ShapeNode(0, 5),
            ShapeNode(0, 6)
        )
    }

    private fun ringShape(): List<ShapeNode> {
        val leftRing = listOf(
            ShapeNode(1, 0), ShapeNode(2, 0),
            ShapeNode(0, 1), ShapeNode(3, 1),
            ShapeNode(0, 2), ShapeNode(3, 2),
            ShapeNode(0, 3), ShapeNode(3, 3),
            ShapeNode(1, 4), ShapeNode(2, 4)
        )

        val rightRing = listOf(
            ShapeNode(6, 0), ShapeNode(7, 0),
            ShapeNode(5, 1), ShapeNode(8, 1),
            ShapeNode(5, 2), ShapeNode(8, 2),
            ShapeNode(5, 3), ShapeNode(8, 3),
            ShapeNode(6, 4), ShapeNode(7, 4)
        )

        return leftRing + rightRing
    }

    private fun hashtagShape(): List<ShapeNode> {
        return listOf(
            ShapeNode(1, 0), ShapeNode(1, 1), ShapeNode(1, 2), ShapeNode(1, 3), ShapeNode(1, 4), ShapeNode(1, 5),
            ShapeNode(4, 0), ShapeNode(4, 1), ShapeNode(4, 2), ShapeNode(4, 3), ShapeNode(4, 4), ShapeNode(4, 5),

            ShapeNode(0, 2), ShapeNode(1, 2), ShapeNode(2, 2), ShapeNode(3, 2), ShapeNode(4, 2), ShapeNode(5, 2),
            ShapeNode(0, 4), ShapeNode(1, 4), ShapeNode(2, 4), ShapeNode(3, 4), ShapeNode(4, 4), ShapeNode(5, 4)
        ).distinct()
    }

    private fun dropletShape(): List<ShapeNode> {
        return listOf(
            ShapeNode(3, 0),

            ShapeNode(2, 1), ShapeNode(3, 1), ShapeNode(4, 1),

            ShapeNode(1, 2), ShapeNode(2, 2), ShapeNode(3, 2), ShapeNode(4, 2), ShapeNode(5, 2),

            ShapeNode(1, 3), ShapeNode(2, 3), ShapeNode(3, 3), ShapeNode(4, 3), ShapeNode(5, 3),

            ShapeNode(1, 4), ShapeNode(2, 4), ShapeNode(3, 4), ShapeNode(4, 4), ShapeNode(5, 4),

            ShapeNode(2, 5), ShapeNode(3, 5), ShapeNode(4, 5),

            ShapeNode(2, 6), ShapeNode(3, 6), ShapeNode(4, 6),

            ShapeNode(3, 7)
        )
    }

    private fun stairsShape(): List<ShapeNode> {
        return listOf(
            ShapeNode(0, 6), ShapeNode(1, 6), ShapeNode(2, 6),

            ShapeNode(2, 5), ShapeNode(3, 5), ShapeNode(4, 5),

            ShapeNode(4, 4), ShapeNode(5, 4), ShapeNode(6, 4),

            ShapeNode(6, 3), ShapeNode(7, 3), ShapeNode(8, 3),

            ShapeNode(8, 2), ShapeNode(9, 2), ShapeNode(10, 2)
        )
    }

    private fun goShape(): List<ShapeNode> {
        val g = listOf(
            ShapeNode(0, 0), ShapeNode(1, 0), ShapeNode(2, 0), ShapeNode(3, 0),
            ShapeNode(0, 1),
            ShapeNode(0, 2), ShapeNode(2, 2), ShapeNode(3, 2),
            ShapeNode(0, 3), ShapeNode(3, 3),
            ShapeNode(0, 4), ShapeNode(1, 4), ShapeNode(2, 4), ShapeNode(3, 4)
        )

        val o = listOf(
            ShapeNode(6, 0), ShapeNode(7, 0), ShapeNode(8, 0),
            ShapeNode(6, 1), ShapeNode(8, 1),
            ShapeNode(6, 2), ShapeNode(8, 2),
            ShapeNode(6, 3), ShapeNode(8, 3),
            ShapeNode(6, 4), ShapeNode(7, 4), ShapeNode(8, 4)
        )

        return g + o
    }

    private fun houseShape(): List<ShapeNode> {
        return listOf(
            ShapeNode(3, 0),

            ShapeNode(2, 1), ShapeNode(4, 1),

            ShapeNode(1, 2), ShapeNode(2, 2), ShapeNode(3, 2), ShapeNode(4, 2), ShapeNode(5, 2),

            ShapeNode(1, 3), ShapeNode(5, 3),

            ShapeNode(1, 4), ShapeNode(5, 4),

            ShapeNode(1, 5), ShapeNode(2, 5), ShapeNode(4, 5), ShapeNode(5, 5),

            ShapeNode(1, 6), ShapeNode(2, 6), ShapeNode(4, 6), ShapeNode(5, 6),

            ShapeNode(1, 7), ShapeNode(2, 7), ShapeNode(3, 7), ShapeNode(4, 7), ShapeNode(5, 7),

            ShapeNode(3, 5), ShapeNode(3, 6)
        ).distinct()
    }

    private fun runShape(): List<ShapeNode> {
        val r = listOf(
            ShapeNode(0, 0), ShapeNode(1, 0), ShapeNode(2, 0),
            ShapeNode(0, 1), ShapeNode(2, 1),
            ShapeNode(0, 2), ShapeNode(1, 2), ShapeNode(2, 2),
            ShapeNode(0, 3), ShapeNode(1, 3),
            ShapeNode(0, 4), ShapeNode(2, 4)
        )

        val u = listOf(
            ShapeNode(5, 0), ShapeNode(7, 0),
            ShapeNode(5, 1), ShapeNode(7, 1),
            ShapeNode(5, 2), ShapeNode(7, 2),
            ShapeNode(5, 3), ShapeNode(7, 3),
            ShapeNode(5, 4), ShapeNode(6, 4), ShapeNode(7, 4)
        )

        val n = listOf(
            ShapeNode(10, 0), ShapeNode(13, 0),
            ShapeNode(10, 1), ShapeNode(11, 1), ShapeNode(13, 1),
            ShapeNode(10, 2), ShapeNode(12, 2), ShapeNode(13, 2),
            ShapeNode(10, 3), ShapeNode(13, 3),
            ShapeNode(10, 4), ShapeNode(13, 4)
        )

        return (r + u + n).distinct()
    }

    private fun rightTriangleShape(): List<ShapeNode> {
        return listOf(
            ShapeNode(0, 4),
            ShapeNode(0, 3), ShapeNode(1, 4),
            ShapeNode(0, 2), ShapeNode(1, 3), ShapeNode(2, 4),
            ShapeNode(0, 1), ShapeNode(1, 2), ShapeNode(2, 3), ShapeNode(3, 4),
            ShapeNode(0, 0), ShapeNode(1, 1), ShapeNode(2, 2), ShapeNode(3, 3), ShapeNode(4, 4)
        )
    }

    private fun yesShape(): List<ShapeNode> {
        val y = listOf(
            ShapeNode(0, 0), ShapeNode(2, 0),
            ShapeNode(1, 1),
            ShapeNode(1, 2),
            ShapeNode(1, 3),
            ShapeNode(1, 4)
        )

        val e = listOf(
            ShapeNode(5, 0), ShapeNode(6, 0), ShapeNode(7, 0),
            ShapeNode(5, 1),
            ShapeNode(5, 2), ShapeNode(6, 2), ShapeNode(7, 2),
            ShapeNode(5, 3),
            ShapeNode(5, 4), ShapeNode(6, 4), ShapeNode(7, 4)
        )

        val s = listOf(
            ShapeNode(10, 0), ShapeNode(11, 0), ShapeNode(12, 0),
            ShapeNode(10, 1),
            ShapeNode(10, 2), ShapeNode(11, 2), ShapeNode(12, 2),
            ShapeNode(12, 3),
            ShapeNode(10, 4), ShapeNode(11, 4), ShapeNode(12, 4)
        )

        return y + e + s
    }

    private fun wallShape(): List<ShapeNode> {
        return buildList {
            for (y in 0..3) {
                for (x in 0..3) {
                    add(ShapeNode(x, y))
                }
            }
        }
    }

    private fun letterBShape(): List<ShapeNode> {
        return listOf(
            ShapeNode(0, 0), ShapeNode(1, 0), ShapeNode(2, 0),
            ShapeNode(0, 1), ShapeNode(3, 1),
            ShapeNode(0, 2), ShapeNode(1, 2), ShapeNode(2, 2),
            ShapeNode(0, 3), ShapeNode(3, 3),
            ShapeNode(0, 4), ShapeNode(1, 4), ShapeNode(2, 4),
            ShapeNode(0, 5), ShapeNode(3, 5),
            ShapeNode(0, 6), ShapeNode(1, 6), ShapeNode(2, 6)
        )
    }

    private fun letterAShape(): List<ShapeNode> {
        return listOf(
            ShapeNode(2, 0),

            ShapeNode(1, 1), ShapeNode(3, 1),

            ShapeNode(0, 2), ShapeNode(4, 2),

            ShapeNode(0, 3), ShapeNode(1, 3), ShapeNode(2, 3), ShapeNode(3, 3), ShapeNode(4, 3),

            ShapeNode(0, 4), ShapeNode(4, 4),
            ShapeNode(0, 5), ShapeNode(4, 5),
            ShapeNode(0, 6), ShapeNode(4, 6)
        )
    }

    private fun letterLShape(): List<ShapeNode> {
        return listOf(
            ShapeNode(0, 0),
            ShapeNode(0, 1),
            ShapeNode(0, 2),
            ShapeNode(0, 3),
            ShapeNode(0, 4),
            ShapeNode(0, 5),
            ShapeNode(0, 6),
            ShapeNode(1, 6),
            ShapeNode(2, 6),
            ShapeNode(3, 6)
        )
    }

    private fun letterRShape(): List<ShapeNode> {
        return listOf(
            ShapeNode(0, 0), ShapeNode(1, 0), ShapeNode(2, 0),
            ShapeNode(0, 1), ShapeNode(3, 1),
            ShapeNode(0, 2), ShapeNode(1, 2), ShapeNode(2, 2),
            ShapeNode(0, 3), ShapeNode(2, 3),
            ShapeNode(0, 4), ShapeNode(3, 4),
            ShapeNode(0, 5), ShapeNode(3, 5),
            ShapeNode(0, 6), ShapeNode(4, 6)
        )
    }

    private fun letterOShape(): List<ShapeNode> {
        return listOf(
            ShapeNode(1, 0), ShapeNode(2, 0), ShapeNode(3, 0),
            ShapeNode(0, 1), ShapeNode(4, 1),
            ShapeNode(0, 2), ShapeNode(4, 2),
            ShapeNode(0, 3), ShapeNode(4, 3),
            ShapeNode(0, 4), ShapeNode(4, 4),
            ShapeNode(0, 5), ShapeNode(4, 5),
            ShapeNode(1, 6), ShapeNode(2, 6), ShapeNode(3, 6)
        )
    }

    private fun letterPShape(): List<ShapeNode> {
        return listOf(
            ShapeNode(0, 0), ShapeNode(1, 0), ShapeNode(2, 0),
            ShapeNode(0, 1), ShapeNode(3, 1),
            ShapeNode(0, 2), ShapeNode(3, 2),
            ShapeNode(0, 3), ShapeNode(1, 3), ShapeNode(2, 3),
            ShapeNode(0, 4),
            ShapeNode(0, 5),
            ShapeNode(0, 6)
        )
    }

    private fun winShape(): List<ShapeNode> {
        val w = listOf(
            ShapeNode(0, 0), ShapeNode(0, 1), ShapeNode(0, 2), ShapeNode(0, 3), ShapeNode(0, 4),
            ShapeNode(1, 4),
            ShapeNode(2, 3), ShapeNode(2, 4),
            ShapeNode(3, 4),
            ShapeNode(4, 0), ShapeNode(4, 1), ShapeNode(4, 2), ShapeNode(4, 3), ShapeNode(4, 4)
        )

        val i = listOf(
            ShapeNode(7, 0), ShapeNode(8, 0), ShapeNode(9, 0),
            ShapeNode(8, 1),
            ShapeNode(8, 2),
            ShapeNode(8, 3),
            ShapeNode(7, 4), ShapeNode(8, 4), ShapeNode(9, 4)
        )

        val n = listOf(
            ShapeNode(12, 0), ShapeNode(15, 0),
            ShapeNode(12, 1), ShapeNode(13, 1), ShapeNode(15, 1),
            ShapeNode(12, 2), ShapeNode(14, 2), ShapeNode(15, 2),
            ShapeNode(12, 3), ShapeNode(15, 3),
            ShapeNode(12, 4), ShapeNode(15, 4)
        )

        return (w + i + n).distinct()
    }
}