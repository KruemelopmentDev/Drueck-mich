package de.kruemelopment.org.drckmichspiel

import android.os.Bundle
import android.widget.Toast
import java.io.File

class Shortcut1 : MainActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        showdialog = false
        intent.putExtra("Shortcut", true)
        super.onCreate(savedInstanceState)
        start()
        checkforAGB()
        spielen()
    }

    private fun spielen() {
        val sp = getSharedPreferences("dms", 0)
        val tt = sp.getString("default", "")
        if (!tt.isNullOrEmpty()) {
            val myFile = File(getExternalFilesDir(null), "$tt.drmch")
            if (!myFile.exists()) {
                val spv = getSharedPreferences("dms", 0)
                val edev = spv.edit()
                edev.putString("default", "")
                edev.apply()
                Toast.makeText(
                    this@Shortcut1,
                    "$tt existiert leider nicht mehr, bitte wähle ein anderes aus!",
                    Toast.LENGTH_SHORT
                ).show()
            } else drueckmich1()
        } else Toast.makeText(
            this@Shortcut1,
            "Mach erstmal ein Drück mich oder wähle eins aus!",
            Toast.LENGTH_SHORT
        ).show()
    }
}
