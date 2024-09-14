package com.ece452.spacexplorer.ui.discover

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.core.content.ContextCompat

// custom adapter for dropdown select
class DropdownAdapter(context: Context, resource: Int, items: Array<String>) : ArrayAdapter<String>(context, resource, items) {
    override fun isEnabled(position: Int): Boolean { return position != 0 }
    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getDropDownView(position, convertView, parent)

        val dropdownItem = view.findViewById<TextView>(android.R.id.text1)

        // set the color of the default selection to different then the others
        if (position == 0) {
            dropdownItem.setTextColor(ContextCompat.getColor(context, android.R.color.darker_gray))
        } else {
            dropdownItem.setTextColor(ContextCompat.getColor(context, android.R.color.black))
        }

        return view
    }
}