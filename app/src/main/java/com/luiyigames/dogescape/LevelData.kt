package com.luiyigames.dogescape

data class EasyLevel(
    val number: Int,
    val title: String,
    val grid: List<String>
)

object EasyLevels {
    val levels = listOf(
        EasyLevel(
            number = 1,
            title = "Paseo al parque",
            grid = listOf(
                "######",
                "#P...#",
                "###..#",
                "#....#",
                "#.###G",
                "######"
            )
        ),
        EasyLevel(
            number = 2,
            title = "Camino del huesito",
            grid = listOf(
                "#######",
                "#P....#",
                "#.###.#",
                "#.....#",
                "###.#.#",
                "#...#G#",
                "#######"
            )
        ),
        EasyLevel(
            number = 3,
            title = "Siesta tranquila",
            grid = listOf(
                "#######",
                "#P..#.#",
                "###...#",
                "#...###",
                "#.###.#",
                "#.....G",
                "#######"
            )
        )
    )
}
