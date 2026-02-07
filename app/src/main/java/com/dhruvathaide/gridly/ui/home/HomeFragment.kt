package com.dhruvathaide.gridly.ui.home

import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.dhruvathaide.gridly.R
import java.util.concurrent.TimeUnit

class HomeFragment : Fragment() {

    private lateinit var countdownText: TextView
    private var timer: CountDownTimer? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        countdownText = view.findViewById(R.id.countdownText)

        // Mock Setup for schedule
        setupScheduleRow(view.findViewById(R.id.row_fp1), "Practice 1", "FRI 13:30")
        setupScheduleRow(view.findViewById(R.id.row_fp2), "Practice 2", "FRI 17:00")
        setupScheduleRow(view.findViewById(R.id.row_quali), "Qualifying", "SAT 16:00")
        setupScheduleRow(view.findViewById(R.id.row_race), "Race", "SUN 15:00")

        // Start 2 hour countdown
        startCountdown(2 * 60 * 60 * 1000 + 14 * 60 * 1000)
    }

    private fun setupScheduleRow(view: View, name: String, time: String) {
        view.findViewById<TextView>(R.id.sessionName).text = name
        view.findViewById<TextView>(R.id.sessionTime).text = time
    }

    private fun startCountdown(durationMillis: Long) {
        timer?.cancel()
        timer = object : CountDownTimer(durationMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val hours = TimeUnit.MILLISECONDS.toHours(millisUntilFinished)
                val minutes = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) % 60
                val seconds = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % 60
                
                countdownText.text = String.format("%02d:%02d:%02d", hours, minutes, seconds)
            }

            override fun onFinish() {
                countdownText.text = "LIVE NOW"
                countdownText.setTextColor(android.graphics.Color.RED)
            }
        }.start()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        timer?.cancel()
    }
}
