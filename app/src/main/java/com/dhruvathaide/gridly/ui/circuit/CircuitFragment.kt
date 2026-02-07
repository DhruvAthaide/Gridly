package com.dhruvathaide.gridly.ui.circuit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.dhruvathaide.gridly.R

class CircuitFragment : Fragment() {
    // Static view for now, could be dynamic later
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_circuit, container, false)
    }
}
