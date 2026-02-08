package com.dhruvathaide.gridly.ui.circuit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dhruvathaide.gridly.ui.MainViewModel

class CircuitFragment : Fragment() {
    
    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                val state = viewModel.uiState.collectAsStateWithLifecycle()
                CircuitScreen(session = state.value.activeSession)
            }
        }
    }
}
