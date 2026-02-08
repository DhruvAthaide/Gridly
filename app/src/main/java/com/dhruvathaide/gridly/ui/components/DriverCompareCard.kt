package com.dhruvathaide.gridly.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dhruvathaide.gridly.data.remote.model.DriverDto
import com.dhruvathaide.gridly.data.remote.model.TelemetryDto

@Composable
fun DriverCompareCard(
    driver: DriverDto?,
    telemetry: TelemetryDto?,
    interval: String,
    tyre: String,
    tyreLife: Int, // New
    pitStops: Int, // New
    sectors: Triple<String, String, String>, // S1, S2, S3
    modifier: Modifier = Modifier
) {
    // Team Color Parsing
    val teamColorHex = try {
        Color(android.graphics.Color.parseColor("#${driver?.teamColour ?: "FFFFFF"}"))
    } catch (e: Exception) {
        Color.Gray
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF1E1E1E))
            .border(1.dp, Color(0xFF333333), RoundedCornerShape(12.dp))
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            // Team Color Strip
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .fillMaxHeight()
                    .background(teamColorHex)
            )

            Column(
                modifier = Modifier
                    .padding(12.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Header: Name & Speed
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = driver?.nameAcronym ?: "---",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.SansSerif
                    )
                    
                    Text(
                        text = "${telemetry?.speed ?: 0} KPH",
                        color = Color(0xFF00E5FF),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Telemetry Bars (Throttle/Brake)
                Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                    // Throttle
                    Row(
                        modifier = Modifier.fillMaxWidth().height(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .weight((telemetry?.throttle?.toFloat() ?: 0f) / 100f + 0.01f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(2.dp))
                                .background(Color(0xFF00E676)) // Green
                        )
                        Spacer(modifier = Modifier.weight(1f - ((telemetry?.throttle?.toFloat() ?: 0f) / 100f) + 0.01f))
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // Brake
                    Row(
                        modifier = Modifier.fillMaxWidth().height(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .weight((telemetry?.brake?.toFloat() ?: 0f) / 100f + 0.01f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(2.dp))
                                .background(Color(0xFFFF5252)) // Red
                        )
                        Spacer(modifier = Modifier.weight(1f - ((telemetry?.brake?.toFloat() ?: 0f) / 100f) + 0.01f))
                    }
                }

                // Details: Interval & Tyre & DRS
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "GAP",
                            color = Color.Gray,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = interval,
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    // DRS Indicator
                    if ((telemetry?.drs ?: 0) > 8) { // DRS Active (usually 10-14 in APIs)
                         Text(
                            text = "DRS",
                            color = Color(0xFF2979FF), // Blue
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier
                                .background(Color(0xFF2979FF).copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "TYRE",
                            color = Color.Gray,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = tyre,
                            color = try {
                                when {
                                    tyre.contains("SOFT", true) -> Color(0xFFFF5252) // Red
                                    tyre.contains("MED", true) -> Color(0xFFFFD740) // Yellow
                                    tyre.contains("HARD", true) -> Color.White
                                    else -> Color.Green // Inter/Wet
                                }
                            } catch(e: Exception) { Color.White },
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Tyre Life & Pit Stops Row
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                   Text(
                        text = "USED: ${tyreLife}L",
                        color = Color.LightGray,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                     Text(
                        text = "STOP: $pitStops",
                        color = Color.LightGray,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Sector Times (Bottom Strip)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SectorBox(label = "S1", time = sectors.first, color = Color(0xFF00E676)) // Green (Best)
                    SectorBox(label = "S2", time = sectors.second, color = Color(0xFFFFEB3B)) // Yellow (Slow)
                    SectorBox(label = "S3", time = sectors.third, color = Color(0xFFFF4081)) // Purple (PB)
                }
            }
        }
    }
}

@Composable
fun SectorBox(label: String, time: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, color = Color.Gray, fontSize = 8.sp, fontWeight = FontWeight.Bold)
        Text(
            text = time,
            color = color,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}
