package com.example.networkanalyzer

import android.content.Context
import android.text.Html
import android.text.Spanned
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

class HtmlArrayAdapter(context: Context, resource: Int, objects: List<Spanned>) :
    ArrayAdapter<Spanned>(context, resource, objects) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getView(position, convertView, parent) as TextView
        view.text = getItem(position)
        return view
    }
}
