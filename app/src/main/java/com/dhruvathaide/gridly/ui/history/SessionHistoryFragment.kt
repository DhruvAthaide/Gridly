package com.dhruvathaide.gridly.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.dhruvathaide.gridly.ui.MainViewModel

class SessionHistoryFragment : Fragment() {

    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                com.dhruvathaide.gridly.ui.theme.GridlyTheme {
                    SessionHistoryScreen(
                        viewModel = viewModel,
                        onSessionSelected = { session ->
                            viewModel.selectHistorySession(session)
                            // Navigate to dashboard to view analysis
                            parentFragmentManager.popBackStack()
                        }
                    )
                }
            }
        }
    }
}
