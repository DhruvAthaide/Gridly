package com.dhruvathaide.gridly.ui.standings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.compose.ui.platform.ComposeView
import com.dhruvathaide.gridly.data.MockDataProvider
import com.dhruvathaide.gridly.ui.common.DriverDetailBottomSheet

class StandingsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                StandingsScreen(
                    onDriverClick = { standing ->
                        val sheet = DriverDetailBottomSheet(standing.driver)
                        sheet.show(parentFragmentManager, "DriverDetail")
                    }
                )
            }
        }
    }
}
