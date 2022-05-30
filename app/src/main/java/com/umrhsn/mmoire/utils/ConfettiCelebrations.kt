package com.umrhsn.mmoire.utils

import android.graphics.Color
import androidx.constraintlayout.widget.ConstraintLayout
import com.github.jinatonic.confetti.CommonConfetti
import com.github.jinatonic.confetti.ConfettiManager

private val confettiColorArray = intArrayOf(Color.BLUE, Color.GREEN, Color.RED)

fun explosionConfettiArray(container: ConstraintLayout): List<ConfettiManager> = listOf(
    explosionConfetti1(container),
    explosionConfetti2(container),
    explosionConfetti3(container),
    explosionConfetti4(container),
    explosionConfetti5(container),
)

fun rainingConfettiLong(container: ConstraintLayout): ConfettiManager =
    CommonConfetti.rainingConfetti(container, confettiColorArray).stream(10000)

fun rainingConfettiShort(container: ConstraintLayout): ConfettiManager =
    CommonConfetti.rainingConfetti(container, confettiColorArray).oneShot()

private fun explosionConfetti1(container: ConstraintLayout): ConfettiManager =
    CommonConfetti.explosion(container, 50, -50, confettiColorArray).stream(10000)

private fun explosionConfetti2(container: ConstraintLayout): ConfettiManager =
    CommonConfetti.explosion(container, 100, -100, confettiColorArray).stream(10000)

private fun explosionConfetti3(container: ConstraintLayout): ConfettiManager =
    CommonConfetti.explosion(container, 180, -180, confettiColorArray).stream(10000)

private fun explosionConfetti4(container: ConstraintLayout): ConfettiManager =
    CommonConfetti.explosion(container, 360, -360, confettiColorArray).stream(10000)

private fun explosionConfetti5(container: ConstraintLayout): ConfettiManager =
    CommonConfetti.explosion(container, 0, 0, confettiColorArray).stream(10000)