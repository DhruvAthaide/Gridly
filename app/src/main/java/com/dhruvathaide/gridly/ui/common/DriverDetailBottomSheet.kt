package com.dhruvathaide.gridly.ui.common

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.dhruvathaide.gridly.R
import com.dhruvathaide.gridly.data.remote.model.DriverDto
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class DriverDetailBottomSheet(private val driver: DriverDto) : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_driver_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        view.findViewById<TextView>(R.id.detailDriverName).text = driver.fullName

        // Dynamic Headshot
        val headshotId = com.dhruvathaide.gridly.ui.common.ResourceHelper.getDriverHeadshot(
            view.context, 
            driver.broadcastName // e.g., "VERSTAPPEN"
        )
        
        view.findViewById<android.widget.ImageView>(R.id.driverHeadshotImage).apply {
            setImageResource(headshotId)
            // Remove tint if it's a real photo (id is not the trophy icon)
            if (headshotId != R.drawable.ic_trophy) {
                imageTintList = null
            }
        }

        view.findViewById<TextView>(R.id.detailTeamName).text = driver.teamName
        view.findViewById<TextView>(R.id.detailNumber).text = "#${driver.driverNumber}"
        
        // Dynamic Team Logo
        val teamLogoId = com.dhruvathaide.gridly.ui.common.ResourceHelper.getTeamLogo(view.context, driver.teamName)
        view.findViewById<android.widget.ImageView>(R.id.detailTeamLogo).apply {
             setImageResource(teamLogoId)
             if (teamLogoId != R.drawable.ic_trophy) {
                 imageTintList = null // Remove tint for real logo
             }
        }
        
        // Dynamic color
        try {
            view.findViewById<View>(R.id.detailHeaderBackground).setBackgroundColor(android.graphics.Color.parseColor("#${driver.teamColour}"))
        } catch (e: Exception) {}
    }
}
