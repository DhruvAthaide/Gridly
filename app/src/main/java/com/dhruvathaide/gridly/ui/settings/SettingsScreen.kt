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
import com.dhruvathaide.gridly.ui.theme.ThemeManager

@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val currentThemeColorHex by ThemeManager.currentThemeColor.collectAsStateWithLifecycle()
    val userName by ThemeManager.userName.collectAsStateWithLifecycle()
    val userDriver by ThemeManager.userDriver.collectAsStateWithLifecycle()
    val isProductionMode by ThemeManager.isProductionMode.collectAsStateWithLifecycle()
    val themeColor = Color(android.graphics.Color.parseColor("#$currentThemeColorHex"))
    
    // Mock States for Toggles
    var spoilersEnabled by remember { mutableStateOf(true) }
    var imperialUnits by remember { mutableStateOf(false) }
    var notificationsEnabled by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF020617)) // Deep Cyberpunk BG
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Header
        Text(
            text = "SETTINGS",
            color = Color.White,
            fontSize = 32.sp,
            fontWeight = FontWeight.Black
        )
        
        // Profile Card (Editable Name, Driver Avatar)
        ProfileCard(themeColor, userName, userDriver) { newName -> 
            ThemeManager.setUserName(context, newName)
        }
        
        // Driver Picker Section
        SectionHeader("CHOOSE YOUR DRIVER")
        DriverPicker(selectedDriver = userDriver) { newDriver ->
            ThemeManager.setUserDriver(context, newDriver)
        }
        
        // Theme Picker Section
        SectionHeader("CHOOSE YOUR TEAM")
        ThemePicker(currentThemeColorHex) { newColor ->
            ThemeManager.setThemeColor(context, newColor)
        }
        
        // Preferences Section
        SectionHeader("PREFERENCES")
        SettingsToggle(
            label = "Hide Spoilers",
            description = "Blur race results until revealed",
            checked = spoilersEnabled,
            onCheckedChange = { spoilersEnabled = it },
            themeColor = themeColor
        )
        SettingsToggle(
            label = "Speed Units",
            description = "Use Imperial (MPH) instead of KPH",
            checked = imperialUnits,
            onCheckedChange = { imperialUnits = it },
            themeColor = themeColor
        )
        SettingsToggle(
            label = "Push Notifications",
            description = "Get notified for race starts",
            checked = notificationsEnabled,
            onCheckedChange = { notificationsEnabled = it },
            themeColor = themeColor
        )
        
        // Data Options
        SectionHeader("DATA SOURCE")
        SettingsToggle(
            label = "Production Data (Beta)",
            description = "Fetch REAL live data (May contain spoilers/empty)",
            checked = isProductionMode,
            onCheckedChange = { ThemeManager.setProductionMode(context, it) },
            themeColor = themeColor
        )
        
        // About Section
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Gridly v1.2.0 (Compose Edition)",
            color = Color.Gray,
            fontSize = 12.sp,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
         Text(
            text = "Made with ❤️ by Dhruv Athaide",
            color = Color.DarkGray,
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
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFF1E1E1E),
                        Color(0xFF1E1E1E).copy(alpha = 0.8f)
                    )
                )
            )
            .border(1.dp, Color(0xFF333333), RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar
        Image(
            painter = painterResource(id = avatarId), 
            contentDescription = "User Avatar",
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .border(2.dp, themeColor, CircleShape)
                .background(Color.Gray), // Fallback bg for transparency
             contentScale = androidx.compose.ui.layout.ContentScale.Crop,
             alignment = Alignment.TopCenter 
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column {
            Text(
                text = "F1 S. LICENSE",
                color = themeColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            
            if (isEditing) {
                OutlinedTextField(
                    value = editedName,
                    onValueChange = { editedName = it },
                    singleLine = true,
                    textStyle = androidx.compose.ui.text.TextStyle(
                         color = Color.White,
                         fontSize = 20.sp,
                         fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = themeColor,
                        unfocusedBorderColor = Color.Gray,
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
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        painter = painterResource(id = R.drawable.ic_settings), // Reuse settings icon for edit for now
                        contentDescription = "Edit",
                        tint = Color.Gray,
                        modifier = Modifier.size(16.dp)
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
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        items(drivers) { driver ->
            // Extract surname for ID/Display (e.g. "Max Verstappen" -> "Verstappen")
            val surname = driver.fullName.substringAfter(" ")
            val isSelected = surname.lowercase() == selectedDriver.lowercase()
            val borderColor = if (isSelected) Color.White else Color.Transparent
            val headshotId = ResourceHelper.getDriverHeadshot(context, surname)
            
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Color(android.graphics.Color.parseColor("#${driver.teamColour}")))
                        .border(if (isSelected) 3.dp else 0.dp, borderColor, CircleShape)
                        .clickable { onDriverSelected(surname.lowercase()) }
                ) {
                     Image(
                        painter = painterResource(id = headshotId),
                        contentDescription = surname,
                        modifier = Modifier.fillMaxSize().offset(y = 4.dp), // Slight offset for headshot
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                        alignment = Alignment.TopCenter
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                if (isSelected) {
                     Text(
                        text = surname.uppercase(),
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
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
        color = Color.Gray,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
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
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.2f))
                        .border(
                            width = if (isSelected) 3.dp else 1.dp,
                            color = if (isSelected) color else Color.Transparent,
                            shape = CircleShape
                        )
                        .clickable { onThemeSelected(team.colorHex) },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = team.logoId),
                        contentDescription = team.name,
                        modifier = Modifier.size(40.dp),
                        contentScale = androidx.compose.ui.layout.ContentScale.Fit
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                if (isSelected) {
                    Text(text = team.name, color = color, fontSize = 10.sp, fontWeight = FontWeight.Bold)
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
            .background(Color(0xFF1E1E1E))
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = description,
                color = Color.Gray,
                fontSize = 12.sp
            )
        }
        
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = themeColor,
                uncheckedThumbColor = Color.Gray,
                uncheckedTrackColor = Color.DarkGray
            )
        )
    }
}
