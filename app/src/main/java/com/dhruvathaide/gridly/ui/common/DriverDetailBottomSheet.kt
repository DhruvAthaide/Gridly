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
        view.findViewById<TextView>(R.id.detailTeamName).text = driver.teamName
        view.findViewById<TextView>(R.id.detailNumber).text = "#${driver.driverNumber}"
        
        // Dynamic color
        try {
            view.findViewById<View>(R.id.detailHeaderBackground).setBackgroundColor(android.graphics.Color.parseColor("#${driver.teamColour}"))
        } catch (e: Exception) {}
    }
}
