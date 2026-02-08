package com.dhruvathaide.gridly.ui.standings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.compose.ui.platform.ComposeView
import com.dhruvathaide.gridly.data.MockDataProvider
import androidx.fragment.app.activityViewModels
import com.dhruvathaide.gridly.ui.MainViewModel

class StandingsFragment : Fragment() {

    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                StandingsScreen(
                    viewModel = viewModel,
                    onDriverClick = { standing ->
                        // existing click logic
                        val sheet = com.dhruvathaide.gridly.ui.common.DriverDetailBottomSheet(standing.driver)
                        sheet.show(parentFragmentManager, "DriverDetail")
                    }
                )
            }
        }
    }
}
