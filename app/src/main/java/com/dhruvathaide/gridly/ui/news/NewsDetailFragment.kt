package com.dhruvathaide.gridly.ui.news

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment

class NewsDetailFragment : Fragment() {

    companion object {
        private const val ARG_URL = "arg_url"
        
        fun newInstance(url: String): NewsDetailFragment {
            return NewsDetailFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_URL, url)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val url = arguments?.getString(ARG_URL) ?: "https://www.formula1.com"

        return ComposeView(requireContext()).apply {
            setContent {
                NewsDetailScreen(
                    url = url,
                    onBack = { 
                        // Proper method to handle back press in fragment
                        requireActivity().onBackPressedDispatcher.onBackPressed() 
                    }
                )
            }
        }
    }
}
