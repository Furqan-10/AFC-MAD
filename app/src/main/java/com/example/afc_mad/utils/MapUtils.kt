package com.example.afc_mad.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

object MapUtils {

    private const val BRANCH_LAT = 31.5790
    private const val BRANCH_LNG = 74.3577
    private const val BRANCH_NAME = "Department of Computer Science, UET Lahore"

    fun openBranchLocation(context: Context) {
        // geo:lat,long?q=lat,long(label)
        val uri = Uri.parse("geo:$BRANCH_LAT,$BRANCH_LNG?q=$BRANCH_LAT,$BRANCH_LNG($BRANCH_NAME)")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.setPackage("com.google.android.apps.maps")

        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            // Fallback to browser if Maps app is not installed
            val browserUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=$BRANCH_LAT,$BRANCH_LNG")
            val browserIntent = Intent(Intent.ACTION_VIEW, browserUri)
            context.startActivity(browserIntent)
            Toast.makeText(context, "Opening in browser...", Toast.LENGTH_SHORT).show()
        }
    }

    fun getDirections(context: Context) {
        // google.navigation:q=lat,long
        val uri = Uri.parse("google.navigation:q=$BRANCH_LAT,$BRANCH_LNG")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.setPackage("com.google.android.apps.maps")

        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            // Fallback to browser directions
            val browserUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=$BRANCH_LAT,$BRANCH_LNG")
            val browserIntent = Intent(Intent.ACTION_VIEW, browserUri)
            context.startActivity(browserIntent)
            Toast.makeText(context, "Opening directions in browser...", Toast.LENGTH_SHORT).show()
        }
    }
}
