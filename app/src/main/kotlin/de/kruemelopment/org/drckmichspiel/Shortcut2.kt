package de.kruemelopment.org.drckmichspiel

import android.os.Bundle

class Shortcut2 : MainActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        showdialog = false
        intent.putExtra("Shortcut", true)
        super.onCreate(savedInstanceState)
        start()
        checkforAGB()
        taetigerstellen()
    }
}