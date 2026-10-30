package com.prajwalprince94.expensetracker.ui.components

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.prajwalprince94.expensetracker.ui.theme.BadgeShape
import com.prajwalprince94.expensetracker.ui.theme.ButtonShape
import com.prajwalprince94.expensetracker.ui.theme.CardShape
import com.prajwalprince94.expensetracker.ui.theme.ChipShape
import com.prajwalprince94.expensetracker.ui.theme.InputShape
import com.prajwalprince94.expensetracker.ui.theme.LargeCardShape
import com.prajwalprince94.expensetracker.ui.theme.PillShape

/**
 * Appllama benchmark adaptive shimmer modifier.
 * Creates an animated linear gradient matching theme container tones.
 */
fun Modifier.shimmerSkeleton(
    shape: Shape = CardShape,
    customBase: Color? = null,
    customHighlight: Color? = null
): Modifier = composed {
    val base = customBase ?: MaterialTheme.colorScheme.surfaceContainerHigh
    val highlight = customHighlight ?: MaterialTheme.colorScheme.surfaceContainerHighest

    val transition = rememberInfiniteTransition(label = "skeletonShimmer")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "skeletonProgress"
    )

    clip(shape).drawWithCache {
        val width = size.width.coerceAtLeast(1f)
        val travel = width * 2f
        val start = -width + travel * progress
        val brush = Brush.linearGradient(
            colors = listOf(base, highlight, base),
            start = Offset(start, 0f),
            end = Offset(start + width, size.height)
        )
        onDrawBehind {
            drawRect(brush)
        }
    }
}

/**
 * Reusable shimmer skeleton box matching Appllama design system shape scales.
 */
@Composable
fun ShimmerSkeletonBox(
    modifier: Modifier = Modifier,
    shape: Shape = CardShape
) {
    Box(modifier = modifier.shimmerSkeleton(shape = shape))
}

/**
 * Skeleton placeholder that strictly mirrors [HomeScreen] layout to prevent layout shifts.
 */
@Composable
fun HomeScreenSkeleton(modifier: Modifier = Modifier) {
    val horizontalPadding = responsiveHorizontalPadding()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .fadingEdges(top = 8.dp, bottom = 80.dp)
            .padding(horizontal = horizontalPadding),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ShimmerSkeletonBox(modifier = Modifier.size(width = 110.dp, height = 14.dp), shape = BadgeShape)
                    ShimmerSkeletonBox(modifier = Modifier.size(width = 170.dp, height = 28.dp), shape = ChipShape)
                }
                ShimmerSkeletonBox(modifier = Modifier.size(44.dp), shape = CircleShape)
            }
        }

        // Hero Net Balance Card Skeleton
        item {
            ShimmerSkeletonBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                shape = LargeCardShape
            )
        }

        // Quick Actions Row Skeleton
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                repeat(4) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        ShimmerSkeletonBox(modifier = Modifier.size(54.dp), shape = CircleShape)
                        ShimmerSkeletonBox(modifier = Modifier.size(width = 46.dp, height = 10.dp), shape = BadgeShape)
                    }
                }
            }
        }

        // Spending Trend Chart Skeleton
        item {
            ShimmerSkeletonBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp),
                shape = CardShape
            )
        }

        // Recent Transactions Section Header Skeleton
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ShimmerSkeletonBox(modifier = Modifier.size(width = 140.dp, height = 20.dp), shape = BadgeShape)
                ShimmerSkeletonBox(modifier = Modifier.size(width = 60.dp, height = 14.dp), shape = BadgeShape)
            }
        }

        // Recent Transaction Rows
        items(4) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(68.dp)
                    .clip(CardShape)
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ShimmerSkeletonBox(modifier = Modifier.size(40.dp), shape = CircleShape)
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    ShimmerSkeletonBox(modifier = Modifier.size(width = 120.dp, height = 14.dp), shape = BadgeShape)
                    ShimmerSkeletonBox(modifier = Modifier.size(width = 80.dp, height = 10.dp), shape = BadgeShape)
                }
                ShimmerSkeletonBox(modifier = Modifier.size(width = 70.dp, height = 18.dp), shape = BadgeShape)
            }
        }
    }
}

/**
 * Skeleton placeholder that strictly mirrors [TransactionsScreen] layout.
 */
@Composable
fun TransactionsScreenSkeleton(modifier: Modifier = Modifier) {
    val horizontalPadding = responsiveHorizontalPadding()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = horizontalPadding)
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        ShimmerSkeletonBox(modifier = Modifier.size(width = 160.dp, height = 32.dp), shape = ChipShape)
        Spacer(modifier = Modifier.height(14.dp))
        ShimmerSkeletonBox(modifier = Modifier.fillMaxWidth().height(52.dp), shape = InputShape)
        Spacer(modifier = Modifier.height(14.dp))

        // Filter chips row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            repeat(4) {
                ShimmerSkeletonBox(modifier = Modifier.size(width = 76.dp, height = 36.dp), shape = PillShape)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Transaction rows
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .fadingEdges(top = 8.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(6) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(68.dp)
                        .clip(CardShape)
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ShimmerSkeletonBox(modifier = Modifier.size(40.dp), shape = CircleShape)
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        ShimmerSkeletonBox(modifier = Modifier.size(width = 130.dp, height = 14.dp), shape = BadgeShape)
                        ShimmerSkeletonBox(modifier = Modifier.size(width = 85.dp, height = 10.dp), shape = BadgeShape)
                    }
                    ShimmerSkeletonBox(modifier = Modifier.size(width = 70.dp, height = 18.dp), shape = BadgeShape)
                }
            }
        }
    }
}

/**
 * Skeleton placeholder that strictly mirrors [BudgetsScreen] layout.
 */
@Composable
fun BudgetsScreenSkeleton(modifier: Modifier = Modifier) {
    val horizontalPadding = responsiveHorizontalPadding()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .fadingEdges(top = 8.dp, bottom = 80.dp)
            .padding(horizontal = horizontalPadding),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ShimmerSkeletonBox(modifier = Modifier.size(width = 100.dp, height = 14.dp), shape = BadgeShape)
                    ShimmerSkeletonBox(modifier = Modifier.size(width = 180.dp, height = 28.dp), shape = ChipShape)
                }
                ShimmerSkeletonBox(modifier = Modifier.size(44.dp), shape = CircleShape)
            }
        }

        // Monthly Summary Card Skeleton
        item {
            ShimmerSkeletonBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                shape = LargeCardShape
            )
        }

        // Category Header Skeleton
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ShimmerSkeletonBox(modifier = Modifier.size(width = 150.dp, height = 20.dp), shape = BadgeShape)
                ShimmerSkeletonBox(modifier = Modifier.size(width = 60.dp, height = 14.dp), shape = BadgeShape)
            }
        }

        // Category Budget Cards
        items(4) {
            ShimmerSkeletonBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(115.dp),
                shape = CardShape
            )
        }
    }
}
