package com.dhruvathaide.gridly.ui.settings

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dhruvathaide.gridly.R
import com.dhruvathaide.gridly.data.MockDataProvider
import com.dhruvathaide.gridly.ui.common.ResourceHelper
import com.dhruvathaide.gridly.ui.theme.*

@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val currentThemeColorHex by ThemeManager.currentThemeColor.collectAsStateWithLifecycle()
    val userName by ThemeManager.userName.collectAsStateWithLifecycle()
    val userDriver by ThemeManager.userDriver.collectAsStateWithLifecycle()
    val themeColor = Color(android.graphics.Color.parseColor("#$currentThemeColorHex"))

    val prefs = remember { context.getSharedPreferences("gridly_prefs", android.content.Context.MODE_PRIVATE) }
    var spoilersEnabled by remember { mutableStateOf(prefs.getBoolean("spoilers_enabled", true)) }
    var imperialUnits by remember { mutableStateOf(prefs.getBoolean("imperial_units", false)) }
    var notificationsEnabled by remember { mutableStateOf(prefs.getBoolean("notifications_enabled", true)) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkAsphalt)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Header
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(22.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(F1Red)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "SETTINGS",
                color = TextPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
        }

        // Profile Card
        ProfileCard(themeColor, userName, userDriver) { newName ->
            ThemeManager.setUserName(context, newName)
        }

        // Driver Picker
        SectionHeader("CHOOSE YOUR DRIVER")
        DriverPicker(selectedDriver = userDriver) { newDriver ->
            ThemeManager.setUserDriver(context, newDriver)
        }

        // Theme Picker
        SectionHeader("CHOOSE YOUR TEAM")
        ThemePicker(currentThemeColorHex) { newColor ->
            ThemeManager.setThemeColor(context, newColor)
        }

        // Preferences
        SectionHeader("PREFERENCES")
        SettingsToggle(
            label = "Hide Spoilers",
            description = "Blur race results until revealed",
            checked = spoilersEnabled,
            onCheckedChange = { spoilersEnabled = it; prefs.edit().putBoolean("spoilers_enabled", it).apply() },
            themeColor = themeColor
        )
        SettingsToggle(
            label = "Speed Units",
            description = "Use Imperial (MPH) instead of KPH",
            checked = imperialUnits,
            onCheckedChange = { imperialUnits = it; prefs.edit().putBoolean("imperial_units", it).apply() },
            themeColor = themeColor
        )
        SettingsToggle(
            label = "Push Notifications",
            description = "Get notified for race starts",
            checked = notificationsEnabled,
            onCheckedChange = { notificationsEnabled = it; prefs.edit().putBoolean("notifications_enabled", it).apply() },
            themeColor = themeColor
        )

        // About
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Gridly v1.0.0",
            color = TextTertiary,
            fontSize = 12.sp,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        Text(
            text = "Made with love by Dhruv Athaide",
            color = TextDisabled,
            fontSize = 12.sp,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(48.dp))
    }
}

@Composable
fun ProfileCard(themeColor: Color, userName: String, userDriver: String, onNameChange: (String) -> Unit) {
    var isEditing by remember { mutableStateOf(false) }
    var editedName by remember { mutableStateOf(userName) }
    val context = LocalContext.current
    val avatarId = ResourceHelper.getDriverHeadshot(context, userDriver)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(CarbonFiber)
            .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = avatarId),
            contentDescription = "User Avatar",
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .border(2.dp, themeColor, CircleShape)
                .background(SurfaceHighlight),
            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
            alignment = Alignment.TopCenter
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(
                text = "F1 S. LICENSE",
                color = themeColor,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            if (isEditing) {
                OutlinedTextField(
                    value = editedName,
                    onValueChange = { editedName = it },
                    singleLine = true,
                    textStyle = androidx.compose.ui.text.TextStyle(
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = themeColor,
                        unfocusedBorderColor = TextTertiary,
                        cursorColor = themeColor,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    ),
                    trailingIcon = {
                        IconButton(onClick = {
                            onNameChange(editedName)
                            isEditing = false
                        }) {
                            Icon(painterResource(id = R.drawable.ic_flag_checkered), contentDescription = "Save", tint = themeColor)
                        }
                    }
                )
            } else {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { isEditing = true }) {
                    Text(
                        text = userName,
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        painter = painterResource(id = R.drawable.ic_settings),
                        contentDescription = "Edit",
                        tint = TextTertiary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun DriverPicker(selectedDriver: String, onDriverSelected: (String) -> Unit) {
    val context = LocalContext.current
    val drivers = remember { MockDataProvider.getDrivers() }

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        items(drivers) { driver ->
            val surname = driver.fullName.substringAfter(" ")
            val isSelected = surname.lowercase() == selectedDriver.lowercase()
            val borderColor = if (isSelected) TextPrimary else Color.Transparent
            val headshotId = ResourceHelper.getDriverHeadshot(context, surname)

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(Color(android.graphics.Color.parseColor("#${driver.teamColour}")))
                        .border(if (isSelected) 2.dp else 0.dp, borderColor, CircleShape)
                        .clickable { onDriverSelected(surname.lowercase()) }
                ) {
                    Image(
                        painter = painterResource(id = headshotId),
                        contentDescription = surname,
                        modifier = Modifier.fillMaxSize().offset(y = 4.dp),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                        alignment = Alignment.TopCenter
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                if (isSelected) {
                    Text(
                        text = surname.uppercase(),
                        color = TextPrimary,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        color = TextTertiary,
        style = MaterialTheme.typography.labelMedium,
        modifier = Modifier.padding(start = 4.dp)
    )
}

@Composable
fun ThemePicker(currentHex: String, onThemeSelected: (String) -> Unit) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        items(ThemeManager.teams) { team ->
            val isSelected = team.colorHex == currentHex
            val color = Color(android.graphics.Color.parseColor("#${team.colorHex}"))

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.15f))
                        .border(
                            width = if (isSelected) 2.dp else 0.dp,
                            color = if (isSelected) color else Color.Transparent,
                            shape = CircleShape
                        )
                        .clickable { onThemeSelected(team.colorHex) },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = team.logoId),
                        contentDescription = team.name,
                        modifier = Modifier.size(36.dp),
                        contentScale = androidx.compose.ui.layout.ContentScale.Fit
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                if (isSelected) {
                    Text(text = team.name, color = color, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun SettingsToggle(
    label: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    themeColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CarbonFiber)
            .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
            .padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                color = TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = description,
                color = TextTertiary,
                fontSize = 12.sp
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = themeColor,
                uncheckedThumbColor = TextTertiary,
                uncheckedTrackColor = SurfaceHighlight
            )
        )
    }
}
