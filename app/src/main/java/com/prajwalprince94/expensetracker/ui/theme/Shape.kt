package com.prajwalprince94.expensetracker.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/** Expressive shape scale inspired by NikhilKain/Range */
val RangeShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(16.dp),
    medium = RoundedCornerShape(22.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(36.dp)
)

val PillShape = RoundedCornerShape(percent = 50)
val CardShape = RoundedCornerShape(26.dp)
val SheetShape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
val SquircleShape = RoundedCornerShape(20.dp)
