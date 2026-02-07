package com.dhruvathaide.gridly.ui.standings

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dhruvathaide.gridly.R

class StandingsFragment : Fragment() {

    private lateinit var recycler: RecyclerView
    private lateinit var btnDrivers: Button
    private lateinit var btnConstructors: Button

    // Mock Data
    private val driverStandings = listOf(
        Standing("1", "Max Verstappen", "Red Bull", "3671C6", "575"),
        Standing("2", "Sergio Perez", "Red Bull", "3671C6", "285"),
        Standing("3", "Lewis Hamilton", "Mercedes", "27F4D2", "234"),
        Standing("4", "Fernando Alonso", "Aston Martin", "358C75", "206"),
        Standing("5", "Charles Leclerc", "Ferrari", "F91536", "206"),
        Standing("6", "Lando Norris", "McLaren", "F58020", "205"),
        Standing("7", "Carlos Sainz", "Ferrari", "F91536", "200"),
        Standing("8", "George Russell", "Mercedes", "27F4D2", "175"),
        Standing("9", "Oscar Piastri", "McLaren", "F58020", "97"),
        Standing("10", "Lance Stroll", "Aston Martin", "358C75", "74"),
        Standing("11", "Pierre Gasly", "Alpine", "0090FF", "62"),
        Standing("12", "Esteban Ocon", "Alpine", "0090FF", "58"),
        Standing("13", "Alexander Albon", "Williams", "37BEDD", "27"),
        Standing("14", "Yuki Tsunoda", "AlphaTauri", "5E8FAA", "17"),
        Standing("15", "Valtteri Bottas", "Alfa Romeo", "C92D4B", "10"),
        Standing("16", "Nico Hulkenberg", "Haas", "B6BABD", "9"),
        Standing("17", "Daniel Ricciardo", "AlphaTauri", "5E8FAA", "6"),
        Standing("18", "Guanyu Zhou", "Alfa Romeo", "C92D4B", "6"),
        Standing("19", "Kevin Magnussen", "Haas", "B6BABD", "3"),
        Standing("20", "Logan Sargeant", "Williams", "37BEDD", "1"),
        Standing("21", "Liam Lawson", "AlphaTauri", "5E8FAA", "0"),
        Standing("22", "Colton Herta", "Andretti Global", "FF0000", "0") // New Entry
    )

    private val constructorStandings = listOf(
        Standing("1", "Red Bull Racing", "", "3671C6", "860"),
        Standing("2", "Mercedes", "", "27F4D2", "409"),
        Standing("3", "Ferrari", "", "F91536", "406"),
        Standing("4", "McLaren", "", "F58020", "302"),
        Standing("5", "Aston Martin", "", "358C75", "280"),
        Standing("6", "Alpine", "", "0090FF", "120"),
        Standing("7", "Williams", "", "37BEDD", "28"),
        Standing("8", "AlphaTauri", "", "5E8FAA", "25"),
        Standing("9", "Alfa Romeo", "", "C92D4B", "16"),
        Standing("10", "Haas F1 Team", "", "B6BABD", "12"),
        Standing("11", "Andretti Global", "", "FF0000", "0") // New Team
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_standings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        recycler = view.findViewById(R.id.standingsRecycler)
        btnDrivers = view.findViewById(R.id.btnDrivers)
        btnConstructors = view.findViewById(R.id.btnConstructors)

        recycler.layoutManager = LinearLayoutManager(context)
        
        // Initial State
        updateList(driverStandings)

        btnDrivers.setOnClickListener {
            updateList(driverStandings)
            updateButtons(true)
        }

        btnConstructors.setOnClickListener {
            updateList(constructorStandings)
            updateButtons(false)
        }
    }

    private fun updateButtons(isDrivers: Boolean) {
        if (isDrivers) {
            btnDrivers.backgroundTintList = context?.getColorStateList(android.R.color.holo_red_dark)
            btnDrivers.setTextColor(Color.WHITE)
            btnConstructors.backgroundTintList = context?.getColorStateList(R.color.background_dark) ?: android.content.res.ColorStateList.valueOf(Color.DKGRAY)
            btnConstructors.setTextColor(Color.GRAY)
        } else {
            btnConstructors.backgroundTintList = context?.getColorStateList(android.R.color.holo_red_dark)
            btnConstructors.setTextColor(Color.WHITE)
            btnDrivers.backgroundTintList = context?.getColorStateList(R.color.background_dark) ?: android.content.res.ColorStateList.valueOf(Color.DKGRAY)
            btnDrivers.setTextColor(Color.GRAY)
        }
    }

    private fun updateList(data: List<Standing>) {
        recycler.adapter = StandingsAdapter(data) { standing ->
            // Map mock Standing to DriverDto for the sheet
            val driverDto = com.dhruvathaide.gridly.data.remote.model.DriverDto(
                driverNumber = standing.pos.toIntOrNull() ?: 0, // Mock mapping
                broadcastName = standing.name.split(" ").lastOrNull()?.uppercase() ?: "UNK",
                fullName = standing.name,
                nameAcronym = standing.name.substring(0, 3).uppercase(),
                teamName = standing.team,
                teamColour = standing.colorHex,
                headshotUrl = null,
                countryCode = "UNK"
            )
            
            val sheet = com.dhruvathaide.gridly.ui.common.DriverDetailBottomSheet(driverDto)
            sheet.show(parentFragmentManager, "DriverDetail")
        }
    }

    // Inner classes for simplicity
    data class Standing(val pos: String, val name: String, val team: String, val colorHex: String, val points: String)

    class StandingsAdapter(
        private val data: List<Standing>,
        private val onClick: (Standing) -> Unit
    ) : RecyclerView.Adapter<StandingsAdapter.ViewHolder>() {
    
        class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
            val pos: TextView = v.findViewById(R.id.position)
            val name: TextView = v.findViewById(R.id.driverName)
            val points: TextView = v.findViewById(R.id.points)
            val color: View = v.findViewById(R.id.teamColorStrip)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.item_standing_row, parent, false)
            return ViewHolder(v)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = data[position]
            holder.pos.text = item.pos
            holder.name.text = item.name
            holder.points.text = "${item.points} PTS"
            try {
                holder.color.setBackgroundColor(Color.parseColor("#${item.colorHex}"))
            } catch (e: Exception) { holder.color.setBackgroundColor(Color.GRAY) }
            
            holder.itemView.setOnClickListener { onClick(item) }
        }

        override fun getItemCount() = data.size
    }
}
