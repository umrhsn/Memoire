package com.umrhsn.mmoire.models

import com.umrhsn.mmoire.R

enum class BoardSize(val numCards: Int) {
    SUPER_DUPER_EASY(6),
    SUPER_EASY(8),
    EASY(12),
    MEDIUM(18),
    HARD(24),
    SUPER_HARD(36),
    SUPER_DUPER_HARD(40);

    companion object {
        fun getByValue(value: Int) = entries.firstOrNull { it.numCards == value } ?: EASY
    }

    fun getNameResId(): Int = when (this) {
        SUPER_DUPER_EASY -> R.string.size_super_duper_easy
        SUPER_EASY -> R.string.size_super_easy
        EASY -> R.string.size_easy
        MEDIUM -> R.string.size_medium
        HARD -> R.string.size_hard
        SUPER_HARD -> R.string.size_super_hard
        SUPER_DUPER_HARD -> R.string.size_super_duper_hard
    }

    fun getPossibleGrids(): List<Pair<Int, Int>> {
        val grids = mutableListOf<Pair<Int, Int>>()
        for (i in 1..numCards) {
            if (numCards % i == 0) {
                grids.add(i to numCards / i)
            }
        }
        return grids
    }

    // This is still used by CreateScreen and others, keep it simple.
    fun getWidth(
        isTwoPlayerMode: Boolean = false,
        isLandscape: Boolean = false,
        isTablet: Boolean = false
    ): Int {
        return if (isLandscape || isTablet) {
            when (this) {
                SUPER_DUPER_EASY -> 3
                SUPER_EASY -> 4
                EASY -> 4
                MEDIUM -> 6
                HARD -> 6
                SUPER_HARD -> 9
                SUPER_DUPER_HARD -> 8
            }
        } else {
            when (this) {
                SUPER_DUPER_EASY -> 2
                SUPER_EASY -> 2
                EASY -> 3
                MEDIUM -> 3
                HARD -> 4
                SUPER_HARD -> 4
                SUPER_DUPER_HARD -> 5
            }
        }
    }

    fun getHeight(
        isTwoPlayerMode: Boolean = false,
        isLandscape: Boolean = false,
        isTablet: Boolean = false
    ): Int {
        return numCards / getWidth(isTwoPlayerMode, isLandscape, isTablet)
    }

    fun getNumPairs(): Int = numCards / 2
}
