package com.dhruvathaide.gridly.data.local

import android.content.Context
import androidx.core.content.edit

object Prefs {
    private const val PREFS_NAME = "gridly_general_prefs"
    private const val KEY_NEWS_FILTERS = "news_filters"

    fun saveNewsFilters(context: Context, filters: Set<String>) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit {
            putStringSet(KEY_NEWS_FILTERS, filters)
        }
    }

    fun getNewsFilters(context: Context): Set<String> {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getStringSet(KEY_NEWS_FILTERS, emptySet()) ?: emptySet()
    }
}
