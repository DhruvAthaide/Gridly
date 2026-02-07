package com.dhruvathaide.gridly.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dhruvathaide.gridly.R
import com.dhruvathaide.gridly.data.remote.model.DriverDto
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class DriverSelectionBottomSheet(
    private val drivers: List<DriverDto>,
    private val onDriversSelected: (DriverDto, DriverDto) -> Unit
) : BottomSheetDialogFragment() {

    private var selectedDriver1: DriverDto? = null
    private var selectedDriver2: DriverDto? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_driver_selection, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val recyclerView = view.findViewById<RecyclerView>(R.id.driverRecyclerView)
        recyclerView.layoutManager = GridLayoutManager(context, 2)
        recyclerView.adapter = DriverAdapter(drivers) { driver ->
            if (selectedDriver1 == null) {
                selectedDriver1 = driver
            } else if (selectedDriver2 == null && driver != selectedDriver1) {
                selectedDriver2 = driver
                // Both selected, notify and dismiss
                onDriversSelected(selectedDriver1!!, selectedDriver2!!)
                dismiss()
            } else {
                // Reset if clicked again
                selectedDriver1 = driver
                selectedDriver2 = null
            }
        }
    }

    // Inner Adapter Class
    class DriverAdapter(
        private val drivers: List<DriverDto>,
        private val onClick: (DriverDto) -> Unit
    ) : RecyclerView.Adapter<DriverAdapter.ViewHolder>() {

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val nameText: TextView = view.findViewById(android.R.id.text1)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(android.R.layout.simple_list_item_1, parent, false) // Simple layout for demo
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val driver = drivers[position]
            holder.nameText.text = "${driver.driverNumber} - ${driver.nameAcronym}"
            holder.nameText.setTextColor(android.graphics.Color.WHITE)
            holder.itemView.setOnClickListener { onClick(driver) }
        }

        override fun getItemCount() = drivers.size
    }
}
