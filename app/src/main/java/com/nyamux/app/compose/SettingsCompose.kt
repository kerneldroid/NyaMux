package com.nyamux.app.compose

import android.app.Activity
import android.content.Context
import android.os.Environment
import androidx.activity.compose.BackHandler
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.toShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.core.os.LocaleListCompat
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nyamux.R
import com.nyamux.app.models.UserAction
import com.nyamux.shared.activities.ReportActivity
import com.nyamux.shared.android.AndroidUtils
import com.nyamux.shared.android.PackageUtils
import com.nyamux.shared.file.FileUtils
import com.nyamux.shared.interact.ShareUtils
import com.nyamux.shared.logger.Logger
import com.nyamux.shared.models.ReportInfo
import com.nyamux.shared.termux.TermuxConstants
import com.nyamux.shared.termux.TermuxUtils
import com.nyamux.shared.termux.settings.preferences.TermuxAPIAppSharedPreferences
import com.nyamux.shared.termux.settings.preferences.TermuxAppSharedPreferences
import com.nyamux.shared.termux.settings.preferences.TermuxTaskerAppSharedPreferences

enum class SettingsScreen {
    MAIN, TERMUX, DEBUGGING, TERMINAL_IO, TERMINAL_VIEW, UI_CUSTOMIZATION, LANGUAGES, ABOUT
}

/** Shared plaque container shape so grouped tiles keep one corner family. */
private val PlaqueShape = RoundedCornerShape(24.dp)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TermuxSettingsScreen(activity: Activity) {
    val context = LocalContext.current
    
    var currentScreen by remember { mutableStateOf(SettingsScreen.MAIN) }

    val hasTermuxApi = remember { TermuxAPIAppSharedPreferences.build(context, false) != null }
    val hasTermuxTasker = remember { TermuxTaskerAppSharedPreferences.build(context, false) != null }
    
    val showDonate = remember {
        val digest = PackageUtils.getSigningCertificateSHA256DigestForPackage(context)
        digest != null && digest != TermuxConstants.APK_RELEASE_GOOGLE_PLAYSTORE_SIGNING_CERTIFICATE_SHA256_DIGEST
    }

    BackHandler(enabled = currentScreen != SettingsScreen.MAIN) {
        when (currentScreen) {
            else -> currentScreen = SettingsScreen.MAIN
        }
    }

    TermuxTheme {
        Scaffold(
            topBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .height(56.dp)
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { 
                        when (currentScreen) {
                            SettingsScreen.MAIN -> activity.finish()
                            else -> currentScreen = SettingsScreen.MAIN
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    AnimatedContent(
                        targetState = currentScreen,
                        transitionSpec = {
                            fadeIn() + slideInVertically { it / 2 } togetherWith fadeOut() + slideOutVertically { -it / 2 }
                        },
                        label = "title",
                        modifier = Modifier.weight(1f)
                    ) { screen ->
                        Text(
                            text = when (screen) {
                                SettingsScreen.MAIN -> stringResource(R.string.action_open_settings)
                                SettingsScreen.TERMUX -> stringResource(R.string.settings_general_title)
                                SettingsScreen.DEBUGGING -> stringResource(R.string.settings_debugging_title)
                                SettingsScreen.TERMINAL_IO -> stringResource(R.string.settings_terminal_io_title)
                                SettingsScreen.TERMINAL_VIEW -> stringResource(R.string.settings_terminal_view_title)
                                SettingsScreen.UI_CUSTOMIZATION -> stringResource(R.string.settings_ui_customization_title)
                                SettingsScreen.LANGUAGES -> stringResource(R.string.settings_languages_title)
                                SettingsScreen.ABOUT -> stringResource(R.string.about_title)
                            },
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        ) { paddingValues ->
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = {
                    val slideDirection = if (targetState == SettingsScreen.TERMUX) 1 else -1
                    slideInHorizontally(
                        initialOffsetX = { it * slideDirection },
                        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessLow)
                    ) + fadeIn() togetherWith slideOutHorizontally(
                        targetOffsetX = { -it * slideDirection },
                        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessLow)
                    ) + fadeOut()
                },
                modifier = Modifier.padding(paddingValues).fillMaxSize(),
                label = "screen_transition"
            ) { screen ->
                when (screen) {
                    SettingsScreen.MAIN -> {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)
                        ) {
                            item {
                                SettingsActionItem(
                                    title = stringResource(R.string.settings_general_title),
                                    summary = stringResource(R.string.settings_general_summary),
                                    icon = Icons.Rounded.Terminal,
                                    shape = PlaqueShape,
                                    iconShape = MaterialShapes.Cookie12Sided.toShape(),
                                    onClick = { currentScreen = SettingsScreen.TERMUX }
                                )
                            }

                            item {
                                SettingsActionItem(
                                    title = stringResource(R.string.settings_languages_title),
                                    summary = stringResource(R.string.settings_languages_summary),
                                    icon = Icons.Rounded.Language,
                                    shape = PlaqueShape,
                                    iconShape = MaterialShapes.Cookie4Sided.toShape(),
                                    onClick = { currentScreen = SettingsScreen.LANGUAGES }
                                )
                            }
                            
                            if (hasTermuxApi || hasTermuxTasker) {
                                item {
                                    Text(
                                        text = stringResource(R.string.settings_plugins_header),
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                    )
                                }

                                if (hasTermuxApi) {
                                    item {
                                        SettingsActionItem(
                                            title = stringResource(R.string.termux_api_preferences_title),
                                            summary = stringResource(R.string.settings_plugin_api_summary),
                                            icon = Icons.Rounded.Extension,
                                            shape = PlaqueShape,
                                            iconShape = MaterialShapes.Cookie9Sided.toShape(),
                                            onClick = {}
                                        )
                                    }
                                }
                                if (hasTermuxTasker) {
                                    item {
                                        SettingsActionItem(
                                            title = stringResource(R.string.termux_tasker_preferences_title),
                                            summary = stringResource(R.string.settings_plugin_tasker_summary),
                                            icon = Icons.Rounded.Task,
                                            shape = PlaqueShape,
                                            iconShape = MaterialShapes.Pentagon.toShape(),
                                            onClick = {}
                                        )
                                    }
                                }
                            }

                            item {
                                Text(
                                    text = stringResource(R.string.about_title),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                )
                                SettingsActionItem(
                                    title = stringResource(R.string.settings_about_item_title),
                                    summary = stringResource(R.string.settings_about_item_summary),
                                    icon = Icons.Rounded.Info,
                                    shape = if (showDonate) RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 8.dp, bottomEnd = 8.dp) else PlaqueShape,
                                    iconShape = MaterialShapes.Sunny.toShape(),
                                    onClick = { currentScreen = SettingsScreen.ABOUT }
                                )
                            }

                            if (showDonate) {
                                item {
                                    SettingsActionItem(
                                        title = stringResource(R.string.donate_preference_title),
                                        summary = stringResource(R.string.settings_donate_summary),
                                        icon = Icons.Rounded.Favorite,
                                        shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 24.dp, bottomEnd = 24.dp),
                                        iconShape = MaterialShapes.Clover4Leaf.toShape(),
                                        onClick = { ShareUtils.openUrl(context, TermuxConstants.TERMUX_DONATE_URL) }
                                    )
                                }
                            }
                        }
                    }
                    SettingsScreen.TERMUX -> {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)
                        ) {
                            item {
                                SettingsActionItem(
                                    title = stringResource(R.string.settings_debugging_title),
                                    summary = stringResource(R.string.settings_debugging_summary),
                                    icon = Icons.Rounded.BugReport,
                                    shape = PlaqueShape,
                                    iconShape = MaterialShapes.Boom.toShape(),
                                    onClick = { currentScreen = SettingsScreen.DEBUGGING }
                                )
                            }
                            item {
                                SettingsActionItem(
                                    title = stringResource(R.string.settings_terminal_io_title),
                                    summary = stringResource(R.string.settings_terminal_io_summary),
                                    icon = Icons.Rounded.Keyboard,
                                    shape = PlaqueShape,
                                    iconShape = MaterialShapes.Slanted.toShape(),
                                    onClick = { currentScreen = SettingsScreen.TERMINAL_IO }
                                )
                            }
                            item {
                                SettingsActionItem(
                                    title = stringResource(R.string.settings_terminal_view_title),
                                    summary = stringResource(R.string.settings_terminal_view_summary),
                                    icon = Icons.Rounded.Visibility,
                                    shape = PlaqueShape,
                                    iconShape = MaterialShapes.Oval.toShape(),
                                    onClick = { currentScreen = SettingsScreen.TERMINAL_VIEW }
                                )
                            }
                            item {
                                SettingsActionItem(
                                    title = stringResource(R.string.settings_ui_customization_title),
                                    summary = stringResource(R.string.settings_ui_customization_summary),
                                    icon = Icons.Rounded.Palette,
                                    shape = PlaqueShape,
                                    iconShape = MaterialShapes.Clover4Leaf.toShape(),
                                    onClick = { currentScreen = SettingsScreen.UI_CUSTOMIZATION }
                                )
                            }
                        }
                    }
                    SettingsScreen.DEBUGGING -> {
                        DebuggingSettingsScreen(context)
                    }
                    SettingsScreen.TERMINAL_IO -> {
                        TerminalIOSettingsScreen(context)
                    }
                    SettingsScreen.TERMINAL_VIEW -> {
                        TerminalViewSettingsScreen(context)
                    }
                    SettingsScreen.UI_CUSTOMIZATION -> {
                        UICustomizationSettingsScreen(context)
                    }
                    SettingsScreen.LANGUAGES -> {
                        LanguagesSettingsScreen(activity)
                    }
                    SettingsScreen.ABOUT -> {
                        AboutSettingsScreen(context)
                    }
                }
            }
        }
    }
}

@Composable
fun LanguagesSettingsScreen(activity: Activity) {
    val currentTag = AppCompatDelegate.getApplicationLocales().toLanguageTags()
    val selected = remember(currentTag) {
        when {
            currentTag == "enl" -> "enl"
            currentTag.startsWith("ru") -> "ru"
            currentTag.startsWith("zh") -> "zh"
            else -> "en"
        }
    }

    val onLanguageSelected: (String) -> Unit = { tag ->
        when (tag) {
            "enl" -> AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("enl"))
            "ru" -> AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("ru"))
            "zh" -> AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("zh-CN"))
            else -> AppCompatDelegate.setApplicationLocales(LocaleListCompat.getEmptyLocaleList())
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)
    ) {
        item {
            SettingsGroup(title = stringResource(R.string.settings_languages_header)) {
                LanguageOptionRow(
                    title = stringResource(R.string.settings_language_english_title),
                    summary = stringResource(R.string.settings_language_english_summary),
                    selected = selected == "en",
                    onClick = { onLanguageSelected("en") }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                LanguageOptionRow(
                    title = stringResource(R.string.settings_language_enl_title),
                    summary = stringResource(R.string.settings_language_enl_summary),
                    selected = selected == "enl",
                    onClick = { onLanguageSelected("enl") }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                LanguageOptionRow(
                    title = stringResource(R.string.settings_language_russian_title),
                    summary = stringResource(R.string.settings_language_russian_summary),
                    selected = selected == "ru",
                    onClick = { onLanguageSelected("ru") }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                LanguageOptionRow(
                    title = stringResource(R.string.settings_language_chinese_title),
                    summary = stringResource(R.string.settings_language_chinese_summary),
                    selected = selected == "zh",
                    onClick = { onLanguageSelected("zh") }
                )
            }
        }
    }
}

@Composable
fun LanguageOptionRow(
    title: String,
    summary: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = null)
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = summary,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SettingsActionItem(
    title: String,
    summary: String,
    icon: ImageVector,
    shape: RoundedCornerShape,
    iconShape: Shape = RoundedCornerShape(16.dp),
    onClick: () -> Unit
) {
    Surface(
        shape = shape,
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = iconShape,
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                modifier = Modifier.size(52.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = summary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun showAboutReport(context: Context) {
    Thread {
        val title = context.getString(R.string.about_report_title)
        val aboutString = java.lang.StringBuilder()
        aboutString.append(TermuxUtils.getAppInfoMarkdownString(context, TermuxUtils.AppInfoMode.TERMUX_AND_PLUGIN_PACKAGES))
        aboutString.append("\n\n").append(AndroidUtils.getDeviceInfoMarkdownString(context, true))
        aboutString.append("\n\n").append(TermuxUtils.getImportantLinksMarkdownString(context))

        val userActionName = UserAction.ABOUT.actionName

        val reportInfo = ReportInfo(
            userActionName,
            TermuxConstants.TERMUX_APP.TERMUX_SETTINGS_ACTIVITY_NAME, title
        )
        reportInfo.reportString = aboutString.toString()
        reportInfo.setReportSaveFileLabelAndPath(
            userActionName,
            Environment.getExternalStorageDirectory().toString() + "/" +
                    FileUtils.sanitizeFileName(TermuxConstants.TERMUX_APP_NAME + "-" + userActionName + ".log", true, true)
        )

        ReportActivity.startReportActivity(context, reportInfo)
    }.start()
}

@Composable
fun SettingsGroup(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        )
        Card(
            shape = PlaqueShape,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                content = content
            )
        }
    }
}

@Composable
fun SettingSwitchTile(
    title: String,
    summaryOn: String,
    summaryOff: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    ListItem(
        headlineContent = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        supportingContent = {
            Text(
                text = if (checked) summaryOn else summaryOff,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingContent = {
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        },
        colors = ListItemDefaults.colors(
            containerColor = Color.Transparent
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 4.dp)
    )
}

@Composable
fun SettingListTile(
    title: String,
    selectedValueLabel: String,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        supportingContent = {
            Text(
                text = selectedValueLabel,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        colors = ListItemDefaults.colors(
            containerColor = Color.Transparent
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp)
    )
}

@Composable
fun DebuggingSettingsScreen(context: Context) {
    val preferences = remember { TermuxAppSharedPreferences.build(context, true) } ?: return
    
    var logLevel by remember { mutableStateOf(preferences.getLogLevel()) }
    var keyLoggingEnabled by remember { mutableStateOf(preferences.isTerminalViewKeyLoggingEnabled()) }
    var pluginErrorsEnabled by remember { mutableStateOf(preferences.arePluginErrorNotificationsEnabled(false)) }
    var crashReportsEnabled by remember { mutableStateOf(preferences.areCrashReportNotificationsEnabled(false)) }
    
    var showLogLevelDialog by remember { mutableStateOf(false) }
    
    val logLevels = remember { Logger.getLogLevelsArray() }
    val logLevelLabels = remember { Logger.getLogLevelLabelsArray(context, logLevels, true)!! }
    val unknownLogLevelLabel = stringResource(R.string.settings_log_level_unknown)
    val currentLogLevelLabel = remember(logLevel, unknownLogLevelLabel) {
        val index = logLevels.indexOf(logLevel.toString())
        if (index >= 0 && index < logLevelLabels.size) logLevelLabels[index].toString() else unknownLogLevelLabel
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            SettingsGroup(title = stringResource(com.nyamux.R.string.termux_logging_header)) {
                SettingListTile(
                    title = stringResource(com.nyamux.R.string.termux_log_level_title),
                    selectedValueLabel = currentLogLevelLabel,
                    onClick = { showLogLevelDialog = true }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                SettingSwitchTile(
                    title = stringResource(com.nyamux.R.string.termux_terminal_view_key_logging_enabled_title),
                    summaryOn = stringResource(com.nyamux.R.string.termux_terminal_view_key_logging_enabled_on),
                    summaryOff = stringResource(com.nyamux.R.string.termux_terminal_view_key_logging_enabled_off),
                    checked = keyLoggingEnabled,
                    onCheckedChange = { newValue ->
                        preferences.setTerminalViewKeyLoggingEnabled(newValue)
                        keyLoggingEnabled = newValue
                    }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                SettingSwitchTile(
                    title = stringResource(com.nyamux.R.string.termux_plugin_error_notifications_enabled_title),
                    summaryOn = stringResource(com.nyamux.R.string.termux_plugin_error_notifications_enabled_on),
                    summaryOff = stringResource(com.nyamux.R.string.termux_plugin_error_notifications_enabled_off),
                    checked = pluginErrorsEnabled,
                    onCheckedChange = { newValue ->
                        preferences.setPluginErrorNotificationsEnabled(newValue)
                        pluginErrorsEnabled = newValue
                    }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                SettingSwitchTile(
                    title = stringResource(com.nyamux.R.string.termux_crash_report_notifications_enabled_title),
                    summaryOn = stringResource(com.nyamux.R.string.termux_crash_report_notifications_enabled_on),
                    summaryOff = stringResource(com.nyamux.R.string.termux_crash_report_notifications_enabled_off),
                    checked = crashReportsEnabled,
                    onCheckedChange = { newValue ->
                        preferences.setCrashReportNotificationsEnabled(newValue)
                        crashReportsEnabled = newValue
                    }
                )
            }
        }
    }

    if (showLogLevelDialog) {
        AlertDialog(
            onDismissRequest = { showLogLevelDialog = false },
            title = {
                Text(
                    text = stringResource(com.nyamux.R.string.termux_log_level_title),
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    logLevels.forEachIndexed { index, valueStr ->
                        val label = logLevelLabels[index].toString()
                        val isSelected = valueStr.toString() == logLevel.toString()
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val newLevel = Integer.parseInt(valueStr.toString())
                                    preferences.setLogLevel(context, newLevel)
                                    logLevel = newLevel
                                    showLogLevelDialog = false
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = {
                                    val newLevel = Integer.parseInt(valueStr.toString())
                                    preferences.setLogLevel(context, newLevel)
                                    logLevel = newLevel
                                    showLogLevelDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLogLevelDialog = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }
}

@Composable
fun TerminalIOSettingsScreen(context: Context) {
    val preferences = remember { TermuxAppSharedPreferences.build(context, true) } ?: return
    
    var softKeyboardEnabled by remember { mutableStateOf(preferences.isSoftKeyboardEnabled()) }
    var softKeyboardOnlyNoHardware by remember { mutableStateOf(preferences.isSoftKeyboardEnabledOnlyIfNoHardware()) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            SettingsGroup(title = stringResource(com.nyamux.R.string.termux_keyboard_header)) {
                SettingSwitchTile(
                    title = stringResource(com.nyamux.R.string.termux_soft_keyboard_enabled_title),
                    summaryOn = stringResource(com.nyamux.R.string.termux_soft_keyboard_enabled_on),
                    summaryOff = stringResource(com.nyamux.R.string.termux_soft_keyboard_enabled_off),
                    checked = softKeyboardEnabled,
                    onCheckedChange = { newValue ->
                        preferences.setSoftKeyboardEnabled(newValue)
                        softKeyboardEnabled = newValue
                    }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                SettingSwitchTile(
                    title = stringResource(com.nyamux.R.string.termux_soft_keyboard_enabled_only_if_no_hardware_title),
                    summaryOn = stringResource(com.nyamux.R.string.termux_soft_keyboard_enabled_only_if_no_hardware_on),
                    summaryOff = stringResource(com.nyamux.R.string.termux_soft_keyboard_enabled_only_if_no_hardware_off),
                    checked = softKeyboardOnlyNoHardware,
                    onCheckedChange = { newValue ->
                        preferences.setSoftKeyboardEnabledOnlyIfNoHardware(newValue)
                        softKeyboardOnlyNoHardware = newValue
                    }
                )
            }
        }
    }
}

@Composable
fun TerminalViewSettingsScreen(context: Context) {
    val preferences = remember { TermuxAppSharedPreferences.build(context, true) } ?: return
    
    var marginAdjustment by remember { mutableStateOf(preferences.isTerminalMarginAdjustmentEnabled()) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            SettingsGroup(title = stringResource(com.nyamux.R.string.termux_terminal_view_view_header)) {
                SettingSwitchTile(
                    title = stringResource(com.nyamux.R.string.termux_terminal_view_terminal_margin_adjustment_title),
                    summaryOn = stringResource(com.nyamux.R.string.termux_terminal_view_terminal_margin_adjustment_on),
                    summaryOff = stringResource(com.nyamux.R.string.termux_terminal_view_terminal_margin_adjustment_off),
                    checked = marginAdjustment,
                    onCheckedChange = { newValue ->
                        preferences.setTerminalMarginAdjustment(newValue)
                        marginAdjustment = newValue
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AboutSettingsScreen(context: Context) {
    val appInfoErrorText = stringResource(R.string.about_error_app_info)
    val appInfo = remember(appInfoErrorText) {
        try {
            TermuxUtils.getAppInfoMarkdownString(context, TermuxUtils.AppInfoMode.TERMUX_AND_PLUGIN_PACKAGES).orEmpty()
                .replace("### ", "")
                .replace("## ", "")
                .replace("* ", "• ")
        } catch (e: Exception) {
            appInfoErrorText
        }
    }
    
    val deviceInfoErrorText = stringResource(R.string.about_error_device_info)
    val deviceInfo = remember(deviceInfoErrorText) {
        try {
            AndroidUtils.getDeviceInfoMarkdownString(context, true)
                .replace("### ", "")
                .replace("## ", "")
                .replace("* ", "• ")
        } catch (e: Exception) {
            deviceInfoErrorText
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = MaterialShapes.Cookie12Sided.toShape(),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(80.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.Terminal,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.application_name),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                val packageVersion = remember {
                    try {
                        context.packageManager.getPackageInfo(context.packageName, 0).versionName
                    } catch (e: Exception) {
                        "v0.118.0"
                    }
                }
                Text(
                    text = stringResource(R.string.about_version, packageVersion ?: ""),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            SettingsGroup(title = stringResource(R.string.about_diagnostics_header)) {
                Text(
                    text = appInfo,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        item {
            SettingsGroup(title = stringResource(R.string.about_device_header)) {
                Text(
                    text = deviceInfo,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        item {
            SettingsGroup(title = stringResource(R.string.about_community_header)) {
                SettingListTile(
                    title = stringResource(R.string.about_github_title),
                    selectedValueLabel = stringResource(R.string.about_github_summary),
                    onClick = { ShareUtils.openUrl(context, "https://github.com/kerneldroid/NyaMux") }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                SettingListTile(
                    title = stringResource(R.string.about_wiki_title),
                    selectedValueLabel = stringResource(R.string.about_wiki_summary),
                    onClick = { ShareUtils.openUrl(context, "https://wiki.termux.com") }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                SettingListTile(
                    title = stringResource(R.string.about_reddit_title),
                    selectedValueLabel = stringResource(R.string.about_reddit_summary),
                    onClick = { ShareUtils.openUrl(context, "https://www.reddit.com/r/termux") }
                )
            }
        }
    }
}

@Composable
fun UICustomizationSettingsScreen(context: Context) {
    val prefs = remember { TermuxAppSharedPreferences.build(context, true)?.sharedPreferences } ?: return
    
    var colorMode by remember { mutableStateOf(prefs.getString("ui_color_scheme_mode", "monet") ?: "monet") }
    var fontChoice by remember { mutableStateOf(prefs.getString("ui_font_choice", "google_sans_code") ?: "google_sans_code") }
    
    var showFontDialog by remember { mutableStateOf(false) }

    val fontLabels = mapOf(
        "google_sans_code" to stringResource(R.string.settings_font_google_sans_code),
        "system" to stringResource(R.string.settings_font_system),
        "termux_font" to stringResource(R.string.settings_font_termux)
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            SettingsGroup(title = stringResource(R.string.settings_colors_header)) {
                SettingSwitchTile(
                    title = stringResource(R.string.settings_use_termux_colors),
                    summaryOn = stringResource(R.string.settings_use_termux_colors_on),
                    summaryOff = stringResource(R.string.settings_use_termux_colors_off),
                    checked = colorMode == "termux",
                    onCheckedChange = { useTermux ->
                        val newMode = if (useTermux) "termux" else "monet"
                        prefs.edit().putString("ui_color_scheme_mode", newMode).apply()
                        colorMode = newMode
                    }
                )
            }
        }

        item {
            SettingsGroup(title = stringResource(R.string.settings_typography_header)) {
                SettingListTile(
                    title = stringResource(R.string.settings_font_family_title),
                    selectedValueLabel = fontLabels[fontChoice] ?: fontLabels.getValue("google_sans_code"),
                    onClick = { showFontDialog = true }
                )
            }
        }
    }

    if (showFontDialog) {
        AlertDialog(
            onDismissRequest = { showFontDialog = false },
            title = {
                Text(
                    text = stringResource(R.string.settings_font_family_title),
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    fontLabels.forEach { (value, label) ->
                        val isSelected = fontChoice == value
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    prefs.edit().putString("ui_font_choice", value).apply()
                                    fontChoice = value
                                    showFontDialog = false
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = {
                                    prefs.edit().putString("ui_font_choice", value).apply()
                                    fontChoice = value
                                    showFontDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showFontDialog = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }
}