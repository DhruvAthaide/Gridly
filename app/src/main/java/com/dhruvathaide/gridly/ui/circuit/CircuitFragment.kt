package com.dhruvathaide.gridly.ui.circuit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.dhruvathaide.gridly.R

import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.dhruvathaide.gridly.ui.MainViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class CircuitFragment : Fragment() {
    
    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_circuit, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collectLatest { state ->
                state.activeSession?.let { session ->
                    bindSessionData(view, session)
                }
            }
        }
    }
    
    private fun bindSessionData(view: View, session: com.dhruvathaide.gridly.data.remote.model.SessionDto) {
        view.findViewById<TextView>(R.id.trackName).text = "CIRCUIT DE ${session.location.uppercase()}"
        view.findViewById<TextView>(R.id.trackLocation).text = "${session.location}, ${session.countryName}"
        
        // Track Map
        val mapId = com.dhruvathaide.gridly.ui.common.ResourceHelper.getTrackMap(
            view.context, 
            session.countryName, 
            session.location
        )
        val imgView = view.findViewById<android.widget.ImageView>(R.id.trackMapImage)
        imgView.setImageResource(mapId)
        
        // Remove tint if real map found (fallback is trophy which might need tint, but placeholder is usually white)
        // Adjust logic: if it's the specific track asset, NO TINT.
        if (mapId != R.drawable.ic_track_placeholder && mapId != R.drawable.ic_trophy) {
            imgView.imageTintList = null
        } else {
             imgView.setColorFilter(android.graphics.Color.WHITE)
        }
        
        // Mock Stats based on circuit (Real API doesn't always have length/laps in SessionDto)
        // We could map these if we had a database. For now, keep hardcoded or generic?
        // Let's at least pretend.
        // If it's not Monaco, randomize/hide specific record text to avoid confusion?
        // Or leave the "Monaco" defaults as placeholders if we can't get real data.
        // Actually, for "God Tier", showing wrong data (Monaco stats for Bahrain) is bad.
        // I will hide the record holder text if it's not Monaco.
        
        if (!session.location.contains("Monaco", ignoreCase = true)) {
             view.findViewById<TextView>(R.id.trackLength).text = "--- km"
             view.findViewById<TextView>(R.id.trackLaps).text = "--"
             view.findViewById<TextView>(R.id.trackRecordTime).text = "--:--.---"
             view.findViewById<TextView>(R.id.trackRecordHolder).text = ""
        }
    }
}
