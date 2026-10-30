package com.prajwalprince94.expensetracker.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Appllama Benchmark Mobile Design System Shape Scale.
 * Enforces strict shape locking:
 * - Badges: 8.dp
 * - Chips / Tags: 12.dp
 * - Inputs: 16.dp
 * - Primary Actions: 18.dp
 * - Standard Cards: 20.dp
 * - Hero & Container Cards: 24.dp
 * - Modal Bottom Sheets: 28.dp
 * - Action Pills: 50% (CircleShape)
 */
val BadgeShape = RoundedCornerShape(8.dp)
val ChipShape = RoundedCornerShape(12.dp)
val InputShape = RoundedCornerShape(16.dp)
val ButtonShape = RoundedCornerShape(18.dp)
val CardShape = RoundedCornerShape(20.dp)
val LargeCardShape = RoundedCornerShape(24.dp)
val SheetShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
val SquircleShape = RoundedCornerShape(20.dp)
val PillShape = RoundedCornerShape(percent = 50)

val AppllamaShapes = Shapes(
    extraSmall = BadgeShape,
    small = ChipShape,
    medium = InputShape,
    large = CardShape,
    extraLarge = SheetShape
)

// Legacy alias for theme compatibility
val RangeShapes = AppllamaShapes
