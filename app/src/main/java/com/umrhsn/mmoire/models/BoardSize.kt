package com.umrhsn.mmoire.models

import com.umrhsn.mmoire.R

enum class BoardSize(val numCards: Int) {
    SUPER_DUPER_EASY(6),  // 3 * 2
    SUPER_EASY(8),        // 4 * 2
    EASY(12),             // 4 * 3
    MEDIUM(18),           // 6 * 3
    HARD(24),             // 6 * 4
    SUPER_HARD(36),       // 9 * 4
    SUPER_DUPER_HARD(40); // 8 * 5

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

    fun getWidth(): Int = when (this) {
        SUPER_DUPER_EASY -> 2
        SUPER_EASY -> 2
        EASY -> 3
        MEDIUM -> 3
        HARD -> 4
        SUPER_HARD -> 4
        SUPER_DUPER_HARD -> 5
    }

    fun getHeight(): Int = numCards / getWidth()

    fun getNumPairs(): Int = numCards / 2
}
