package com.androidengineers.agent_quickstart_android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.androidengineers.agent_quickstart_android.model.NavigationTab
import com.androidengineers.agent_quickstart_android.ui.theme.VoxCyan
import com.androidengineers.agent_quickstart_android.ui.theme.VoxIndigo
import com.androidengineers.agent_quickstart_android.ui.theme.VoxNavyBorder
import com.androidengineers.agent_quickstart_android.ui.theme.VoxNavyCard
import com.androidengineers.agent_quickstart_android.ui.theme.VoxNavyDark
import com.androidengineers.agent_quickstart_android.ui.theme.VoxTextMuted
import com.androidengineers.agent_quickstart_android.ui.theme.VoxTextPrimary

@Composable
fun VoxPilotBottomBar(
    currentTab: NavigationTab,
    onTabSelected: (NavigationTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(VoxNavyDark)
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(VoxNavyCard)
                .padding(vertical = 6.dp, horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BottomBarItem(
                icon = Icons.Default.Mic,
                label = "Home",
                selected = currentTab == NavigationTab.HOME,
                onClick = { onTabSelected(NavigationTab.HOME) },
            )
            BottomBarItem(
                icon = Icons.Default.Description,
                label = "Notes",
                selected = currentTab == NavigationTab.NOTES,
                onClick = { onTabSelected(NavigationTab.NOTES) },
            )
            BottomBarItem(
                icon = Icons.Default.History,
                label = "History",
                selected = currentTab == NavigationTab.HISTORY,
                onClick = { onTabSelected(NavigationTab.HISTORY) },
            )
            BottomBarItem(
                icon = Icons.Default.Settings,
                label = "Settings",
                selected = currentTab == NavigationTab.SETTINGS,
                onClick = { onTabSelected(NavigationTab.SETTINGS) },
            )
        }
    }
}

@Composable
private fun BottomBarItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val activeColor = VoxCyan
    val inactiveColor = VoxTextMuted

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (selected) activeColor else inactiveColor,
            modifier = Modifier.size(22.dp),
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) VoxTextPrimary else inactiveColor,
        )
    }
}
