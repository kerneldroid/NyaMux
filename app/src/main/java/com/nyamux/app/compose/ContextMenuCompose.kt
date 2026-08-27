package com.nyamux.app.compose

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.Dialog
import com.nyamux.R
import com.nyamux.app.TermuxActivity
import com.nyamux.app.activities.HelpActivity
import com.nyamux.app.activities.SettingsActivity

private val OverlaySheetShape = RoundedCornerShape(28.dp)
private val ActionGroupShape = RoundedCornerShape(16.dp)

class ContextMenuStateHolder {
    var isVisible by mutableStateOf(false)
    var pid by mutableStateOf(0)
    var isSessionRunning by mutableStateOf(false)
    var selectedText by mutableStateOf("")
    var isAutoFillEnabled by mutableStateOf(false)
    var isKeepScreenOn by mutableStateOf(false)
}

@Composable
fun ContextMenuOverlay(
    activity: TermuxActivity,
    stateHolder: ContextMenuStateHolder
) {
    if (!stateHolder.isVisible) return

    Dialog(onDismissRequest = { stateHolder.isVisible = false }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp)
                .wrapContentHeight(),
            shape = OverlaySheetShape,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.ctx_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 16.dp, start = 8.dp)
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Sharing & clipboard
                    item {
                        ActionGroupCard {
                            ContextMenuItem(
                                icon = Icons.Rounded.Link,
                                label = stringResource(R.string.action_select_url),
                                onClick = {
                                    stateHolder.isVisible = false
                                    activity.mTermuxTerminalViewClient?.showUrlSelection()
                                }
                            )
                            ContextMenuItem(
                                icon = Icons.Rounded.Share,
                                label = stringResource(R.string.action_share_transcript),
                                onClick = {
                                    stateHolder.isVisible = false
                                    activity.mTermuxTerminalViewClient?.shareSessionTranscript()
                                }
                            )
                            if (stateHolder.selectedText.isNotEmpty()) {
                                ContextMenuItem(
                                    icon = Icons.Rounded.ContentCopy,
                                    label = stringResource(R.string.action_share_selected_text),
                                    onClick = {
                                        stateHolder.isVisible = false
                                        activity.mTermuxTerminalViewClient?.shareSelectedText()
                                    }
                                )
                            }
                        }
                    }

                    // Autofill
                    if (stateHolder.isAutoFillEnabled) {
                        item {
                            ActionGroupCard {
                                ContextMenuItem(
                                    icon = Icons.Rounded.Person,
                                    label = stringResource(R.string.action_autofill_username),
                                    onClick = {
                                        stateHolder.isVisible = false
                                        activity.terminalView?.requestAutoFillUsername()
                                    }
                                )
                                ContextMenuItem(
                                    icon = Icons.Rounded.VpnKey,
                                    label = stringResource(R.string.action_autofill_password),
                                    onClick = {
                                        stateHolder.isVisible = false
                                        activity.terminalView?.requestAutoFillPassword()
                                    }
                                )
                            }
                        }
                    }

                    // Terminal control
                    item {
                        ActionGroupCard {
                            ContextMenuItem(
                                icon = Icons.Rounded.RestartAlt,
                                label = stringResource(R.string.ctx_reset_terminal),
                                onClick = {
                                    stateHolder.isVisible = false
                                    activity.onResetTerminalSession(activity.currentSession)
                                }
                            )
                            ContextMenuItem(
                                icon = Icons.Rounded.Cancel,
                                label = if (stateHolder.pid > 0) stringResource(R.string.action_kill_process, stateHolder.pid) else stringResource(R.string.ctx_kill_process),
                                enabled = stateHolder.isSessionRunning,
                                onClick = {
                                    stateHolder.isVisible = false
                                    activity.showKillSessionDialog(activity.currentSession)
                                }
                            )
                            ContextMenuItem(
                                icon = Icons.Rounded.LightMode,
                                label = stringResource(R.string.action_toggle_keep_screen_on),
                                onClick = {
                                    activity.toggleKeepScreenOn()
                                    stateHolder.isKeepScreenOn = !stateHolder.isKeepScreenOn
                                },
                                trailingContent = {
                                    Switch(
                                        checked = stateHolder.isKeepScreenOn,
                                        onCheckedChange = {
                                            activity.toggleKeepScreenOn()
                                            stateHolder.isKeepScreenOn = it
                                        }
                                    )
                                }
                            )
                        }
                    }

                    // Configuration & support
                    item {
                        ActionGroupCard {
                            ContextMenuItem(
                                icon = Icons.Rounded.Help,
                                label = stringResource(R.string.action_open_help),
                                onClick = {
                                    stateHolder.isVisible = false
                                    activity.startActivity(Intent(activity, HelpActivity::class.java))
                                }
                            )
                            ContextMenuItem(
                                icon = Icons.Rounded.Settings,
                                label = stringResource(R.string.action_open_settings),
                                onClick = {
                                    stateHolder.isVisible = false
                                    activity.startActivity(Intent(activity, SettingsActivity::class.java))
                                }
                            )
                            ContextMenuItem(
                                icon = Icons.Rounded.BugReport,
                                label = stringResource(R.string.action_report_issue),
                                onClick = {
                                    stateHolder.isVisible = false
                                    activity.mTermuxTerminalViewClient?.reportIssueFromTranscript()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ActionGroupCard(
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = ActionGroupShape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            content = content
        )
    }
}

@Composable
fun ContextMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    trailingContent: @Composable (() -> Unit)? = null
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f),
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                )
            }
            if (trailingContent != null) {
                trailingContent()
            }
        }
    }
}
