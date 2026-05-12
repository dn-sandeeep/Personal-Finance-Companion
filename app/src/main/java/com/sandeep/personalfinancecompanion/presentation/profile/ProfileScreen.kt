package com.sandeep.personalfinancecompanion.presentation.profile

import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sandeep.personalfinancecompanion.R
import com.sandeep.personalfinancecompanion.presentation.profile.components.CurrencySelectionDialog
import com.sandeep.personalfinancecompanion.ui.theme.PrimaryAccent
import androidx.compose.ui.res.stringResource

import com.sandeep.personalfinancecompanion.presentation.profile.components.LanguageSelectionDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.material.icons.filled.Language
import androidx.core.os.LocaleListCompat

@Composable
fun ProfileScreen(
    innerPadding: androidx.compose.foundation.layout.PaddingValues,
    onBack: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val scrollState = rememberScrollState()
    var showCurrencyDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }

    // Permission launcher for Android 13+
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.toggleDailyReminder(true)
        }
    }

    val smsPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (allGranted) {
            viewModel.updateSmsDetectionEnabled(true)
        }
    }

    val context = LocalContext.current
    var csvDataToSave by remember { mutableStateOf<String?>(null) }

    val msgExportSuccess = stringResource(R.string.msg_export_success)
    val msgExportFailed = stringResource(R.string.msg_export_failed)

    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        uri?.let {
            csvDataToSave?.let { data ->
                try {
                    context.contentResolver.openOutputStream(it)?.use { outputStream ->
                        outputStream.write(data.toByteArray())
                    }
                    Toast.makeText(context, msgExportSuccess, Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(context, String.format(msgExportFailed, e.message), Toast.LENGTH_SHORT).show()
                } finally {
                    viewModel.resetExportStatus()
                    csvDataToSave = null
                }
            }
        } ?: viewModel.resetExportStatus()
    }

    LaunchedEffect(state.exportStatus) {
        when (val status = state.exportStatus) {
            is ExportStatus.Success -> {
                csvDataToSave = status.data
                createDocumentLauncher.launch("personal_finance_data.csv")
            }
            is ExportStatus.Error -> {
                Toast.makeText(context, status.message, Toast.LENGTH_LONG).show()
                viewModel.resetExportStatus()
            }
            else -> {}
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
    ) {
        Spacer(modifier = Modifier.height(innerPadding.calculateTopPadding()))
        
        ProfileHeader()

        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = stringResource(R.string.label_notifications_alerts),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            SettingsCard {
                SettingsToggleItem(
                    title = stringResource(R.string.label_daily_reminder),
                    description = stringResource(R.string.desc_daily_reminder),
                    icon = Icons.Default.NotificationsActive,
                    checked = state.dailyReminderEnabled,
                    onCheckedChange = { enabled ->
                        if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            viewModel.toggleDailyReminder(enabled)
                        }
                    }
                )
                
                Divider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)

                SettingsToggleItem(
                    title = stringResource(R.string.label_budget_alerts),
                    description = stringResource(R.string.desc_budget_alerts),
                    icon = Icons.Default.AccountBalanceWallet,
                    checked = state.budgetAlertsEnabled,
                    onCheckedChange = { viewModel.toggleBudgetAlerts(it) }
                )

                Divider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)

                SettingsToggleItem(
                    title = stringResource(R.string.label_goal_progress),
                    description = stringResource(R.string.desc_goal_progress),
                    icon = Icons.Default.Flag,
                    checked = state.goalRemindersEnabled,
                    onCheckedChange = { viewModel.toggleGoalReminders(it) }
                )

                Divider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)

                SettingsToggleItem(
                    title = stringResource(R.string.label_sms_detection),
                    description = stringResource(R.string.desc_sms_detection),
                    icon = if (state.smsDetectionEnabled) Icons.Default.NotificationsActive else Icons.Default.NotificationsNone,
                    checked = state.smsDetectionEnabled,
                    onCheckedChange = { checked ->
                        if (checked) {
                            if (viewModel.isNotificationListenerEnabled(context)) {
                                viewModel.updateSmsDetectionEnabled(true)
                            } else {
                                viewModel.openNotificationListenerSettings(context)
                                // We don't enable yet, wait for user to come back after giving permission
                            }
                        } else {
                            viewModel.updateSmsDetectionEnabled(false)
                        }
                    }
                )

                if (state.smsDetectionEnabled) {
                    Divider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                    SettingsToggleItem(
                        title = stringResource(R.string.label_auto_save_sms),
                        description = stringResource(R.string.desc_auto_save_sms),
                        icon = Icons.Default.AccountBalanceWallet,
                        checked = state.autoSaveSmsTransactions,
                        onCheckedChange = { viewModel.updateAutoSaveSmsTransactions(it) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.label_account_settings),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            SettingsCard {
                SettingsItem(
                    title = stringResource(R.string.label_currency),
                    value = "${state.selectedCurrency.flag} ${state.selectedCurrency.code}",
                    icon = Icons.Default.CurrencyExchange,
                    onClick = { showCurrencyDialog = true }
                )
                Divider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                SettingsItem(
                    title = stringResource(R.string.label_language),
                    value = if (state.selectedLanguage == "hi") "हिन्दी" else "English",
                    icon = Icons.Default.Language,
                    onClick = { showLanguageDialog = true }
                )
                Divider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                SettingsItem(
                    title = stringResource(R.string.label_export_data),
                    value = if (state.exportStatus is ExportStatus.Loading) stringResource(R.string.status_exporting) else stringResource(R.string.label_csv_pdf),
                    icon = Icons.Default.Download,
                    onClick = { viewModel.exportData() },
                    isUnderDevelopment = false
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.label_privacy),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            SettingsCard {
                SettingsToggleItem(
                    title = stringResource(R.string.label_usage_analytics),
                    description = stringResource(R.string.desc_usage_analytics),
                    icon = Icons.Default.QueryStats,
                    checked = state.analyticsEnabled,
                    onCheckedChange = { viewModel.updateAnalyticsEnabled(it) }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
            
            Spacer(modifier = Modifier.height(innerPadding.calculateBottomPadding() + 16.dp))
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(
                    text = stringResource(R.string.label_version),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showCurrencyDialog) {
        CurrencySelectionDialog(
            selectedCurrency = state.selectedCurrency,
            onCurrencySelected = { viewModel.updateCurrency(it) },
            onDismiss = { showCurrencyDialog = false }
        )
    }

    if (showLanguageDialog) {
        LanguageSelectionDialog(
            selectedLanguageCode = state.selectedLanguage,
            onLanguageSelected = { code ->
                viewModel.updateLanguage(code)
                AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(code))
            },
            onDismiss = { showLanguageDialog = false }
        )
    }
}

@Composable
fun ProfileHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(PrimaryAccent, PrimaryAccent.copy(alpha = 0.7f))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "U",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.label_user),
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(R.string.label_free_account),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(content = content)
    }
}

@Composable
fun SettingsToggleItem(
    title: String,
    description: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(PrimaryAccent.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = PrimaryAccent,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = PrimaryAccent,
                uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )
    }
}

@Composable
fun SettingsItem(
    title: String,
    value: String,
    icon: ImageVector,
    onClick: () -> Unit = {},
    isUnderDevelopment: Boolean = false
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(enabled = !isUnderDevelopment, onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (isUnderDevelopment) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface
                )
                if (isUnderDevelopment) {
                    Text(
                        text = stringResource(R.string.label_under_development),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Red,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline.copy(alpha = if (isUnderDevelopment) 0.5f else 1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline.copy(alpha = if (isUnderDevelopment) 0.5f else 1f),
                modifier = Modifier.size(16.dp)
            )
        }

        if (isUnderDevelopment) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.Red.copy(alpha = 0.05f))
            )
        }
    }
}
