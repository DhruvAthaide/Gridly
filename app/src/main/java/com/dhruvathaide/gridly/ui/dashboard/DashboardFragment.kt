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
import com.dhruvathaide.gridly.ui.components.TelemetryChartView
import androidx.cardview.widget.CardView
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collect

class DashboardFragment : Fragment() {

    private lateinit var speedChart: TelemetryChartView
    private lateinit var raceControlCard: CardView
    private lateinit var raceControlText: TextView
    private lateinit var overtakePrediction: TextView

    // Use Fragment-ktx to get ViewModel scoped to Activity (shared)
    private val viewModel: MainViewModel by viewModels({ requireActivity() })

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
        speedChart = view.findViewById(R.id.speedChart)
        raceControlCard = view.findViewById(R.id.raceControlCard)
        raceControlText = view.findViewById(R.id.raceControlText)
        overtakePrediction = view.findViewById(R.id.overtakePrediction)
        
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
        // Update Chart
        // Map speed (0-350 assumed max) to 0.0-1.0
        val d1Data = state.driver1Telemetry.map { (it.speed.toFloat() / 360f).coerceIn(0f, 1f) }
        val d2Data = state.driver2Telemetry.map { (it.speed.toFloat() / 360f).coerceIn(0f, 1f) }
        speedChart.setData(d1Data, d2Data)
        
        // Update Race Control
        if (state.raceControlMessage != null) {
            raceControlCard.visibility = View.VISIBLE
            raceControlText.text = state.raceControlMessage
        } else {
            raceControlCard.visibility = View.GONE
        }
        
        // Update Prediction
        overtakePrediction.text = "OVERTAKE: ${state.overtakePrediction}"
        
        // Driver 1 Card (Include Layout)
        state.driver1?.let { d1 ->
            val card = view?.findViewById<View>(R.id.telemetryCard1) ?: return
            
            card.findViewById<TextView>(R.id.driverName)?.text = d1.nameAcronym
            try {
               card.findViewById<View>(R.id.teamColorStrip)?.setBackgroundColor(android.graphics.Color.parseColor("#${d1.teamColour}"))
            } catch (e: Exception) {}
            
            card.findViewById<TextView>(R.id.tyreCompound)?.text = state.d1TyreCompound
            card.findViewById<TextView>(R.id.intervalText)?.text = state.d1Interval
            
            // Latest Telemetry Text
            state.driver1Telemetry.lastOrNull()?.let { t ->
                card.findViewById<TextView>(R.id.speedText)?.text = "${t.speed} KPH"
                card.findViewById<TextView>(R.id.gearText)?.text = "TEST" // Placeholder or mapped
            }
        }

        // Driver 2 Card
        state.driver2?.let { d2 ->
             val card = view?.findViewById<View>(R.id.telemetryCard2) ?: return
            
            card.findViewById<TextView>(R.id.driverName)?.text = d2.nameAcronym
            try {
               card.findViewById<View>(R.id.teamColorStrip)?.setBackgroundColor(android.graphics.Color.parseColor("#${d2.teamColour}"))
            } catch (e: Exception) {}
            
            card.findViewById<TextView>(R.id.tyreCompound)?.text = state.d2TyreCompound
            card.findViewById<TextView>(R.id.intervalText)?.text = state.d2Interval
            
             // Latest Telemetry Text
            state.driver2Telemetry.lastOrNull()?.let { t ->
                card.findViewById<TextView>(R.id.speedText)?.text = "${t.speed} KPH"
            }
        }
    }
}
