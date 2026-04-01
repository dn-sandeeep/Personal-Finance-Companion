package com.sandeep.personalfinancecompanion.presentation.goal

import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sandeep.personalfinancecompanion.presentation.home.HomeViewModel

import androidx.compose.runtime.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import com.sandeep.personalfinancecompanion.domain.model.Goal

@Composable
fun GoalScreen(
    viewModel: GoalViewModel = hiltViewModel()
) {
    val colorScheme = MaterialTheme.colorScheme
    val goals by viewModel.goals.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        GoalTypePickerDialog(
            onDismiss = { showDialog = false },
            onGoalSelected = { title, target, icon, color ->
                viewModel.createNewGoal(title, target, icon, color)
                showDialog = false
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        
        // Content Padding
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Active Goals",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Tracking your path to financial equilibrium.",
                style = MaterialTheme.typography.bodyMedium,
                color = colorScheme.onSurfaceVariant,
                fontSize = 13.sp
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            PrimaryObjectiveCard()
            
            Spacer(modifier = Modifier.height(16.dp))
            
            NoSpendChallengeCard()
            
            Spacer(modifier = Modifier.height(16.dp))
            
            goals.forEach { goal ->
                SmallGoalCard(
                    icon = getIconForName(goal.iconName),
                    iconBgColor = Color(android.graphics.Color.parseColor(goal.colorHex)).copy(alpha = 0.2f),
                    iconTintColor = Color(android.graphics.Color.parseColor(goal.colorHex)),
                    title = goal.title,
                    progress = goal.progress,
                    progressColor = Color(android.graphics.Color.parseColor(goal.colorHex)),
                    trackColor = colorScheme.outlineVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            CreateNewGoalButton(onClick = { showDialog = true })
            
            Spacer(modifier = Modifier.height(16.dp))
            
            SavingVelocityCard()
            
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
fun GoalTypePickerDialog(
    onDismiss: () -> Unit,
    onGoalSelected: (String, Double, String, String) -> Unit
) {
    val predefinedGoals = listOf(
        Triple("New Car", 25000.0, "DirectionsCar"),
        Triple("Dream Home", 500000.0, "Home"),
        Triple("New Phone", 1000.0, "Smartphone"),
        Triple("Education", 15000.0, "School"),
        Triple("Retirement", 1000000.0, "TrendingUp")
    )
    val colors = listOf("#F44336", "#2196F3", "#4CAF50", "#FF9800", "#9C27B0")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select a Goal") },
        text = {
            Column {
                predefinedGoals.forEachIndexed { index, (title, target, icon) ->
                    val color = colors[index % colors.size]
                    TextButton(
                        onClick = { onGoalSelected(title, target, icon, color) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = getIconForName(icon),
                                contentDescription = null,
                                tint = Color(android.graphics.Color.parseColor(color))
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(title)
                            Spacer(modifier = Modifier.weight(1f))
                            Text("₹$target", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

fun getIconForName(name: String): ImageVector {
    return when (name) {
        "FlightTakeoff" -> Icons.Default.FlightTakeoff
        "Security" -> Icons.Default.Security
        "DirectionsCar" -> Icons.Default.DirectionsCar
        "Home" -> Icons.Default.Home
        "Smartphone" -> Icons.Default.Smartphone
        "School" -> Icons.Default.School
        "TrendingUp" -> Icons.Default.TrendingUp
        else -> Icons.Default.Star
    }
}

@Composable
private fun PrimaryObjectiveCard() {
    val colorScheme = MaterialTheme.colorScheme
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.primaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Pill
                Box(
                    modifier = Modifier
                        .background(colorScheme.primary.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "PRIMARY OBJECTIVE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.primary,
                        letterSpacing = 1.sp
                    )
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "65%",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = colorScheme.primary
                    )
                    Text(
                        text = "Complete",
                        fontSize = 11.sp,
                        color = colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Save ₹2,000 for a\nnew laptop",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onPrimaryContainer,
                lineHeight = 28.sp
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            LinearProgressIndicator(
                progress = { 0.65f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp)),
                color = colorScheme.primary,
                trackColor = colorScheme.surfaceVariant
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Saved",
                        fontSize = 12.sp,
                        color = colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "₹1,300.00",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface
                    )
                }
                
                // Divider
                Box(
                    modifier = Modifier
                        .height(30.dp)
                        .width(1.dp)
                        .background(colorScheme.outlineVariant)
                )
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Target",
                        fontSize = 12.sp,
                        color = colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "₹2,000.00",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun NoSpendChallengeCard() {
    val colorScheme = MaterialTheme.colorScheme
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.secondary),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(colorScheme.onSecondary.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LocalFireDepartment,
                    contentDescription = "Fire",
                    tint = colorScheme.onSecondary,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Text(
                text = "No Spend Challenge",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onSecondary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Avoiding non-essential purchases this\nweek.",
                fontSize = 12.sp,
                color = colorScheme.onSecondary.copy(alpha = 0.8f),
                lineHeight = 18.sp
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "7-day",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = colorScheme.onSecondary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "STREAK!",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSecondary,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun SmallGoalCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconBgColor: Color,
    iconTintColor: Color,
    title: String,
    progress: Float,
    progressColor: Color,
    trackColor: Color
) {
    val colorScheme = MaterialTheme.colorScheme
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(iconBgColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTintColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = colorScheme.onSurface
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Progress",
                        fontSize = 10.sp,
                        color = colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface
                    )
                }
                
                Box(modifier = Modifier.fillMaxWidth(0.5f)) {
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(18.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = progressColor,
                        trackColor = trackColor
                    )
                }
            }
        }
    }
}

@Composable
private fun CreateNewGoalButton(onClick: () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .drawBehind {
                drawRoundRect(
                    color = colorScheme.outlineVariant,
                    style = Stroke(
                        width = 4f
                    ),
                    cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx())
                )
            }
            .clip(RoundedCornerShape(16.dp))
            .background(Color.Transparent)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .background(colorScheme.onSurface.copy(alpha = 0.8f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add",
                    tint = colorScheme.surface,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Create New Goal",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun SavingVelocityCard() {
    val colorScheme = MaterialTheme.colorScheme
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.ShowChart,
                    contentDescription = null,
                    tint = colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "SAVING VELOCITY",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.primary,
                    letterSpacing = 1.sp
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "On track to reach your\nlaptop goal 2 weeks\nearly.",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onSurface,
                fontSize = 18.sp,
                lineHeight = 24.sp
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "Your reduced spending in 'Dining'\nover the last 7 days has\naccelerated your progress by 12%.",
                fontSize = 13.sp,
                color = colorScheme.onSurfaceVariant,
                lineHeight = 20.sp
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Bar Chart stub
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .background(colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                val heights = listOf(0.4f, 0.5f, 0.35f, 0.7f, 1.0f)
                val colors = listOf(
                    colorScheme.primary.copy(alpha = 0.4f),
                    colorScheme.primary.copy(alpha = 0.5f),
                    colorScheme.primary.copy(alpha = 0.6f),
                    colorScheme.primary.copy(alpha = 0.8f),
                    colorScheme.primary
                )
                
                heights.forEachIndexed { index, fraction ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(fraction)
                            .padding(horizontal = 4.dp)
                            .background(colors[index], RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                    )
                }
            }
        }
    }
}
