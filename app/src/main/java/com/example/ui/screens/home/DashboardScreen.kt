package com.example.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.MadrasaViewModel
import com.example.ui.screens.ScreenTab
import com.example.util.BanglaFormatHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: MadrasaViewModel,
    onNavigateTab: (ScreenTab) -> Unit
) {
    val settings by viewModel.settings.collectAsState()
    val totalStudents by viewModel.totalStudentCount.collectAsState()
    val totalTeachers by viewModel.totalTeacherCount.collectAsState()
    val jamats by viewModel.jamats.collectAsState()
    val exams by viewModel.exams.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("dashboard_screen")
    ) {
        // Madrasa Header Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "بِسْمِ اللهِ الرَّحْمٰنِ الرَّحِيْمِ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = settings?.madrasaName ?: "মাদ্রাসা ম্যানেজমেন্ট",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                if (!settings?.madrasaAddress.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = settings?.madrasaAddress ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primaryContainer
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    color = Color.White.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        text = "অফলাইন সংস্করণ | সম্পূর্ণ সুরক্ষিত",
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White
                    )
                }
            }
        }

        // Quick Stats Row
        Text(
            text = "সারসংক্ষেপ",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatCard(
                title = "মোট ছাত্র",
                count = BanglaFormatHelper.toBangla(totalStudents),
                icon = Icons.Default.School,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f),
                onClick = { onNavigateTab(ScreenTab.STUDENTS) }
            )
            StatCard(
                title = "মোট শিক্ষক",
                count = BanglaFormatHelper.toBangla(totalTeachers),
                icon = Icons.Default.Person,
                color = Color(0xFF2E7D32),
                modifier = Modifier.weight(1f),
                onClick = { onNavigateTab(ScreenTab.TEACHERS) }
            )
            StatCard(
                title = "মোট জামাত",
                count = BanglaFormatHelper.toBangla(jamats.size),
                icon = Icons.Default.Class,
                color = Color(0xFFC67D00),
                modifier = Modifier.weight(1f),
                onClick = { onNavigateTab(ScreenTab.JAMATS) }
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Recent Exams Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 18.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Assignment,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "পরীক্ষার তথ্য",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    TextButton(onClick = { onNavigateTab(ScreenTab.EXAMS) }) {
                        Text("সকল পরীক্ষা")
                    }
                }
                if (exams.isEmpty()) {
                    Text(
                        text = "কোন পরীক্ষা তৈরি করা হয়নি",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    exams.take(3).forEach { exam ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surface
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(12.dp)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = exam.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "সাল: ${exam.hijriYear} ${if (exam.examDate.isNotBlank()) " | তারিখ: ${exam.examDate}" else ""}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                FilledTonalButton(
                                    onClick = { onNavigateTab(ScreenTab.RESULTS) },
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text("ফলাফল", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Quick Action Buttons
        Text(
            text = "দ্রুত কাজের মেনু",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 10.dp)
        )

        val quickActions = listOf(
            Triple("ছাত্র তালিকা", Icons.Default.PersonAdd, ScreenTab.STUDENTS),
            Triple("নম্বর এন্ট্রি ও ফলাফল", Icons.Default.Grade, ScreenTab.RESULTS),
            Triple("প্রশ্নপত্র তৈরি", Icons.Default.Description, ScreenTab.QUESTIONS),
            Triple("সনদ ও প্রশংসাপত্র", Icons.Default.CardMembership, ScreenTab.DOCUMENTS),
            Triple("বিষয় ও সিলেবাস", Icons.Default.MenuBook, ScreenTab.JAMATS),
            Triple("ব্যাকআপ ও সেটিংস", Icons.Default.Settings, ScreenTab.SETTINGS)
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            for (i in quickActions.indices step 2) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val item1 = quickActions[i]
                    QuickActionButton(
                        title = item1.first,
                        icon = item1.second,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateTab(item1.third) }
                    )
                    if (i + 1 < quickActions.size) {
                        val item2 = quickActions[i + 1]
                        QuickActionButton(
                            title = item2.first,
                            icon = item2.second,
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigateTab(item2.third) }
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun StatCard(
    title: String,
    count: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = count,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun QuickActionButton(
    title: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    OutlinedCard(
        onClick = onClick,
        modifier = modifier.height(64.dp),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(26.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
