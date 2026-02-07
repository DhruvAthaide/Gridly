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
    // Mock Data (2026 Season)
    private val driverStandings = listOf(
        Standing("1", "Lando Norris", "McLaren", "F58020", "25"),
        Standing("2", "Oscar Piastri", "McLaren", "F58020", "18"),
        Standing("3", "George Russell", "Mercedes", "27F4D2", "15"),
        Standing("4", "Kimi Antonelli", "Mercedes", "27F4D2", "12"),
        Standing("5", "Max Verstappen", "Red Bull Racing", "3671C6", "10"),
        Standing("6", "Isack Hadjar", "Red Bull Racing", "3671C6", "8"),
        Standing("7", "Charles Leclerc", "Ferrari", "F91536", "6"),
        Standing("8", "Lewis Hamilton", "Ferrari", "F91536", "4"),
        Standing("9", "Alexander Albon", "Williams", "37BEDD", "2"),
        Standing("10", "Carlos Sainz Jr.", "Williams", "37BEDD", "1"),
        Standing("11", "Liam Lawson", "Racing Bulls", "5E8FAA", "0"),
        Standing("12", "Arvid Lindblad", "Racing Bulls", "5E8FAA", "0"),
        Standing("13", "Fernando Alonso", "Aston Martin", "358C75", "0"),
        Standing("14", "Lance Stroll", "Aston Martin", "358C75", "0"),
        Standing("15", "Oliver Bearman", "Haas", "B6BABD", "0"),
        Standing("16", "Esteban Ocon", "Haas", "B6BABD", "0"),
        Standing("17", "Nico Hülkenberg", "Audi", "C92D4B", "0"),
        Standing("18", "Gabriel Bortoleto", "Audi", "C92D4B", "0"),
        Standing("19", "Pierre Gasly", "Alpine", "0090FF", "0"),
        Standing("20", "Franco Colapinto", "Alpine", "0090FF", "0"),
        Standing("21", "Sergio Pérez", "Cadillac", "FCD116", "0"),
        Standing("22", "Valtteri Bottas", "Cadillac", "FCD116", "0")
    )

    private val constructorStandings = listOf(
        Standing("1", "McLaren", "", "F58020", "43"),
        Standing("2", "Mercedes", "", "27F4D2", "27"),
        Standing("3", "Red Bull Racing", "", "3671C6", "18"),
        Standing("4", "Ferrari", "", "F91536", "10"),
        Standing("5", "Williams", "", "37BEDD", "3"),
        Standing("6", "Racing Bulls", "", "5E8FAA", "0"),
        Standing("7", "Aston Martin", "", "358C75", "0"),
        Standing("8", "Haas", "", "B6BABD", "0"),
        Standing("9", "Audi", "", "C92D4B", "0"),
        Standing("10", "Alpine", "", "0090FF", "0"),
        Standing("11", "Cadillac F1 Team", "", "FCD116", "0")
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
            val teamLogo: android.widget.ImageView = v.findViewById(R.id.teamLogo)
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
            
            // Bind Team Logo
            val logoKey = if (item.team.isNotEmpty()) item.team else item.name
            val teamLogoId = com.dhruvathaide.gridly.ui.common.ResourceHelper.getTeamLogo(holder.itemView.context, logoKey)
            holder.teamLogo.setImageResource(teamLogoId)
            
            // Adjust tint: if it's the fallback trophy, keep it (or tint white). If real logo, no tint.
            if (teamLogoId == R.drawable.ic_trophy) {
                holder.teamLogo.setColorFilter(Color.WHITE)
            } else {
                holder.teamLogo.clearColorFilter()
            }
            
            holder.itemView.setOnClickListener { onClick(item) }
        }

        override fun getItemCount() = data.size
    }
}
