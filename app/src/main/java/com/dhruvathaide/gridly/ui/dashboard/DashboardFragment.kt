package com.dhruvathaide.gridly.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.dhruvathaide.gridly.R
import com.dhruvathaide.gridly.ui.MainViewModel
import com.dhruvathaide.gridly.ui.view.TelemetryGraphView
import kotlinx.coroutines.launch

class DashboardFragment : Fragment() {

    private lateinit var telemetryGraph: TelemetryGraphView
    private lateinit var rainText: TextView
    private lateinit var overdueIndicator: TextView
    private lateinit var pitWindowIndicator: TextView
    private lateinit var driver1Label: TextView
    private lateinit var driver2Label: TextView
    private lateinit var headerTitle: TextView

    // Use Fragment-ktx to get ViewModel scoped to Activity (shared) or Fragment
    private val viewModel: MainViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Bind Views
        telemetryGraph = view.findViewById(R.id.telemetryGraph)
        rainText = view.findViewById(R.id.rainText)
        overdueIndicator = view.findViewById(R.id.overtakeIndicator)
        pitWindowIndicator = view.findViewById(R.id.pitWindowIndicator)
        headerTitle = view.findViewById(R.id.headerTitle)
        
        // Find labels for drivers (added ID in layout update step next, assuming existence or finding by text for now)
        // I'll dynamically update the text in the LinearLayout children or use IDs if I add them.
        // For robustness, let's assume I'll add IDs in the next XML update step.
        // For now, finding by tag or just appending.
        
        // Let's set onClick for the header to open driver selector
        headerTitle.setOnClickListener {
            val drivers = viewModel.uiState.value.availableDrivers
            if (drivers.isNotEmpty()) {
                val bottomSheet = DriverSelectionBottomSheet(drivers) { d1, d2 ->
                    viewModel.selectDrivers(d1, d2)
                }
                bottomSheet.show(parentFragmentManager, "DriverSelector")
            }
        }

        // Observer
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    renderState(state)
                }
            }
        }
    }

    private fun renderState(state: com.dhruvathaide.gridly.ui.DashboardUiState) {
        // Update Graph
        telemetryGraph.setData(state.driver1Telemetry, state.driver2Telemetry)
        
        // Update Weather
        rainText.text = String.format("%.1f%%", state.rainProbability)
        
        // Update Indicators
        overdueIndicator.text = "OVERTAKE\n${state.overtakePrediction}"
        pitWindowIndicator.text = "PIT WINDOW\n${state.pitWindowStatus}"
        
        // Update Colors/Pulsing
        if (state.pitWindowStatus == "OPEN") {
             pitWindowIndicator.alpha = if (System.currentTimeMillis() % 1000 < 500) 1.0f else 0.5f 
        } else {
             pitWindowIndicator.alpha = 1.0f
        }

        // Driver 1 Stats
        state.driver1?.let { d1 ->
            view?.findViewById<TextView>(R.id.driver1Name)?.text = d1.nameAcronym
            view?.findViewById<View>(R.id.d1Color)?.setBackgroundColor(android.graphics.Color.parseColor("#${d1.teamColour}"))
            view?.findViewById<TextView>(R.id.d1Compound)?.text = state.d1TyreCompound
            // Mock tyre life (100 - age * 2 or similar logic)
            view?.findViewById<android.widget.ProgressBar>(R.id.d1TyreProgress)?.progress = 88 
            view?.findViewById<TextView>(R.id.d1Interval)?.text = state.d1Interval
        }

        // Driver 2 Stats
        state.driver2?.let { d2 ->
            view?.findViewById<TextView>(R.id.driver2Name)?.text = d2.nameAcronym
            view?.findViewById<View>(R.id.d2Color)?.setBackgroundColor(android.graphics.Color.parseColor("#${d2.teamColour}"))
            view?.findViewById<TextView>(R.id.d2Compound)?.text = state.d2TyreCompound
            view?.findViewById<android.widget.ProgressBar>(R.id.d2TyreProgress)?.progress = 92
            view?.findViewById<TextView>(R.id.d2Interval)?.text = state.d2Interval
        }
    }
}
