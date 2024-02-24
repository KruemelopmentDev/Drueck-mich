package de.kruemelopment.org.drckmichspiel

import android.app.Activity
import android.app.Dialog
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.InsetDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.OpenableColumns
import android.text.Editable
import android.text.Html
import android.text.TextWatcher
import android.text.method.LinkMovementMethod
import android.view.KeyEvent
import android.view.View
import android.widget.AdapterView.OnItemClickListener
import android.widget.AdapterView.OnItemLongClickListener
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ListView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.AppCompatImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import androidx.core.text.HtmlCompat
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.FileWriter
import java.io.IOException
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Collections
import java.util.Locale

open class MainActivity : Activity() {
    private var names:Array<String?> = arrayOfNulls(32)
    private var actions:Array<String?> = arrayOfNulls(4)

    private var geschlecht = false
    private var tat: String? = null
    private var save: String? = null
    private var titel: String? = null
    private var defaul: String? = null
    private var zahl = 0
    private var symbol = 0
    private var name = ""
    private var layout = 0
    private var ui = 0
    private var imagepath: File? = null
    private var delete: String? = null
    private var tt: Boolean? = null
    private var liste: ArrayList<String?>? = null
    private var dateipfad: String? = ""
    private var sharefile: File? = null
    private var loeschen = false
    private var hackchenbild: InsetDrawable? = null
    private var insetpixels = 0
    private var inset = false
    var showdialog = true
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            setFullScreen()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        setFullScreen()
        checkforAGB()
        handleIntent(intent)
    }

    fun start() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.decorView.setOnApplyWindowInsetsListener { _, insets ->
                if (insets.displayCutout != null) {
                    insetpixels = insets.displayCutout!!.boundingRects[0].width()
                    inset = true
                }
                insets
            }
        }
        ui = 0
        setContentView(R.layout.activity_main)
        layout = 0
        val dr = ResourcesCompat.getDrawable(resources, R.drawable.hacken, theme)!!
        val bitmap = (dr as BitmapDrawable).bitmap
        val d: Drawable = BitmapDrawable(resources, Bitmap.createScaledBitmap(bitmap, 100, 100, true))
        hackchenbild = InsetDrawable(d, 0, -40, 0, 0)
        val dm = findViewById<Button>(R.id.button)
        val ndm = findViewById<Button>(R.id.button2)
        ndm.setOnClickListener {
            layout = 1
            ui = 0
            taetigerstellen()
        }
        val dmv = findViewById<Button>(R.id.button4)
        dm.setOnClickListener {
            val sp = getSharedPreferences("dms", 0)
            val tt = sp.getString("default", "")
            if (tt != null) {
                if (tt.isNotEmpty()) {
                    val myFile = File(getExternalFilesDir(null), "$tt.drmch")
                    if (!myFile.exists()) {
                        val spv = getSharedPreferences("dms", 0)
                        val edev = spv.edit()
                        edev.putString("default", "")
                        edev.apply()
                        Toast.makeText(
                            this@MainActivity,
                            "$tt existiert leider nicht mehr, bitte wähle ein anderes aus!",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else drueckmich1()
                } else Toast.makeText(
                    this@MainActivity,
                    getString(R.string.create_select_dm),
                    Toast.LENGTH_SHORT
                ).show()
            } else Toast.makeText(
                this@MainActivity,
                getString(R.string.create_select_dm),
                Toast.LENGTH_SHORT
            ).show()
        }
        dmv.setOnClickListener {
            val folder = File(getExternalFilesDir(null).toString() + "")
            val children = folder.list()
            liste = ArrayList()
            assert(children != null)
            if (children!!.isNotEmpty()) {
                for (aChildren in children) {
                    if (aChildren.contains("drmch")) liste!!.add(aChildren.replace(".drmch", ""))
                }
            }
            if (liste!!.isNotEmpty()) {
                liste?.let { it1 -> Collections.sort(it1, java.lang.String.CASE_INSENSITIVE_ORDER) }
                setContentView(R.layout.verwaltung)
                layout = 3
                val adapter = ArrayAdapter(this@MainActivity, R.layout.listeitem, liste!!)
                val listView = findViewById<ListView>(R.id.listview)
                listView.adapter = adapter
                listView.onItemClickListener = OnItemClickListener { _, _, i, _ ->
                    defaul = adapter.getItem(i)
                    val sp8 = getSharedPreferences("dms", 0)
                    val ed = sp8.edit()
                    ed.putString("default", defaul)
                    ed.apply()
                    Toast.makeText(this@MainActivity, "$defaul ist ausgewählt", Toast.LENGTH_SHORT)
                        .show()
                    start()
                }
                listView.onItemLongClickListener =
                    OnItemLongClickListener { _, _, i, _ ->
                        delete = adapter.getItem(i)
                        val dialog = Dialog(this@MainActivity, R.style.AppDialog2)
                        dialog.setContentView(R.layout.buttons)
                        val btn = dialog.findViewById<Button>(R.id.oben)
                        val btn2 = dialog.findViewById<Button>(R.id.button19)
                        val btn4 = dialog.findViewById<Button>(R.id.button25)
                        val aBuffer = StringBuilder()
                        try {
                            val myFile = File(getExternalFilesDir(null), "$delete.drmch")
                            val fIn = FileInputStream(myFile)
                            val myReader = BufferedReader(InputStreamReader(fIn))
                            var aDataRow: String?
                            while (myReader.readLine().also { aDataRow = it } != null) {
                                aBuffer.append(aDataRow)
                            }
                            myReader.close()
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                        val tatig = aBuffer.toString().split("&teiler%".toRegex())
                            .dropLastWhile { it.isEmpty() }
                            .toTypedArray()
                        if (tatig[36] == "true") btn.setTextColor(ContextCompat.getColor(this,android.R.color.darker_gray))
                        btn.setOnClickListener {
                            if (tatig[36] == "false") {
                                for ((j, _) in actions.withIndex()){
                                    actions[j]=tatig[j]
                                }
                                for ((j,_) in names.withIndex()){
                                    names[j]=tatig[j+4]
                                }
                                ui = 1
                                titel = delete
                                dialog.dismiss()
                                taetigerstellen()
                            } else Toast.makeText(
                                this@MainActivity,
                                getString(R.string.received_dm_no_edit),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        btn2.setOnClickListener {
                            dialog.dismiss()
                            val alertdo = Dialog(this@MainActivity, R.style.AppDialog)
                            alertdo.setContentView(R.layout.alertdia)
                            alertdo.setCancelable(true)
                            val tv1 = alertdo.findViewById<TextView>(R.id.textView)
                            tv1.text =
                                "Bist du dir sicher,dass du das Drück mich:„$delete“ löschen willst?"
                            val tv3 = alertdo.findViewById<TextView>(R.id.textView3)
                            val tv2 = alertdo.findViewById<TextView>(R.id.textView2)
                            val tv4 = alertdo.findViewById<TextView>(R.id.textView4)
                            tv4.text = ""
                            tv2.text = getString(R.string.yes)
                            tv3.text = getString(R.string.no)
                            alertdo.show()
                            tv3.setOnClickListener { alertdo.dismiss() }
                            tv2.setOnClickListener {
                                alertdo.dismiss()
                                val myFile = File(getExternalFilesDir(null), "$delete.drmch")
                                if (myFile.exists()) {
                                    myFile.delete()
                                }
                                liste!!.remove(delete)
                                if (liste!!.isEmpty()) {
                                    start()
                                } else {
                                    adapter.notifyDataSetChanged()
                                }
                                val widgetIDs = AppWidgetManager.getInstance(this@MainActivity)
                                    .getAppWidgetIds(
                                        ComponentName(
                                            this@MainActivity,
                                            WigetListe::class.java
                                        )
                                    )
                                for (id in widgetIDs) AppWidgetManager.getInstance(this@MainActivity)
                                    .notifyAppWidgetViewDataChanged(id, R.id.widgetlistview)
                            }
                        }
                        btn4.setOnClickListener {
                            val sp = getSharedPreferences("dms", 0)
                            val tt = sp.getString("von", "Unbekannt")
                            var schreiben = aBuffer.toString()
                            if (aBuffer.toString().substring(aBuffer.length - 5, aBuffer.length)
                                    .contains("false")
                            ) {
                                schreiben = aBuffer.substring(0, aBuffer.length - 5) + "true"
                                schreiben = "$schreiben&teiler%$delete von $tt"
                                sharefile = File(getExternalFilesDir(null), "$delete von $tt.drmch")
                                loeschen = true
                            } else {
                                sharefile = File(getExternalFilesDir(null), "$delete.drmch")
                                loeschen = false
                            }
                            try {
                                sharefile!!.createNewFile()
                                val writer = BufferedWriter(FileWriter(sharefile, true /*append*/))
                                writer.write(schreiben)
                                writer.close()
                                val uri = FileProvider.getUriForFile(
                                    this@MainActivity,
                                    BuildConfig.APPLICATION_ID + ".provider",
                                    sharefile!!
                                )
                                val intentShareFile = Intent(Intent.ACTION_SEND)
                                intentShareFile.setType("application/drmch")
                                intentShareFile.putExtra(Intent.EXTRA_STREAM, uri)
                                startActivity(
                                    Intent.createChooser(
                                        intentShareFile,
                                        getString(R.string.share_via)
                                    )
                                )
                            } catch (e: Exception) {
                                Toast.makeText(
                                    this@MainActivity,
                                    getString(R.string.something_gone_wrong),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                        val constraintLayout = dialog.findViewById<ConstraintLayout>(R.id.buttons)
                        constraintLayout.setOnClickListener { dialog.dismiss() }
                        dialog.show()
                        true
                    }
            } else {
                start()
                Toast.makeText(
                    this@MainActivity,
                    getString(R.string.create_dm),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        val info = findViewById<AppCompatImageView>(R.id.button23)
        info.setOnClickListener {
            val dialog3 = Dialog(this@MainActivity, R.style.AppDialog2)
            dialog3.setContentView(R.layout.buttons2)
            val btn = dialog3.findViewById<Button>(R.id.oben)
            val btn2 = dialog3.findViewById<Button>(R.id.unten)
            val btn3 = dialog3.findViewById<Button>(R.id.button24)
            btn.setOnClickListener {
                dialog3.dismiss()
                val uri = Uri.parse("https://www.kruemelopment-dev.de/nutzungsbedingungen")
                val intent = Intent(Intent.ACTION_VIEW, uri)
                startActivity(intent)
            }
            btn2.setOnClickListener {
                dialog3.dismiss()
                val uri = Uri.parse("https://www.kruemelopment-dev.de/datenschutzerklaerung")
                val intent = Intent(Intent.ACTION_VIEW, uri)
                startActivity(intent)
            }
            btn3.setOnClickListener {
                val intent = Intent(Intent.ACTION_SENDTO)
                intent.setData(Uri.parse("mailto:kontakt@kruemelopment-dev.de"))
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
            val relativeLayout = dialog3.findViewById<RelativeLayout>(R.id.buttons2)
            relativeLayout.setOnClickListener { dialog3.dismiss() }
            dialog3.show()
        }
        val settings = findViewById<AppCompatImageView>(R.id.button123)
        settings.setOnClickListener { setting() }
    }

    fun drueckmich1() {
        setContentView(R.layout.such1)
        layout = 5
        val mann = findViewById<ImageView>(R.id.button3)
        val frau = findViewById<ImageView>(R.id.button5)
        mann.setOnClickListener {
            geschlecht = true
            drueckmich2()
        }
        frau.setOnClickListener {
            geschlecht = false
            drueckmich2()
        }
    }

    private fun drueckmich2() {
        setContentView(R.layout.tatigkeit)
        layout = 6
        val tt: String
        if (dateipfad!!.isEmpty()) {
            val sp = getSharedPreferences("dms", 0)
            tt = sp.getString("default", "")!!
            if (tt.isEmpty()) {
                Toast.makeText(this, getString(R.string.no_dm_selected), Toast.LENGTH_SHORT)
                    .show()
                start()
            }
        } else {
            tt = dateipfad!!.replace(".drmch", "")
        }
        val aBuffer = StringBuilder()
        val myFile = File(getExternalFilesDir(null), "$tt.drmch")
        if (myFile.exists()) {
            try {
                val fIn = FileInputStream(myFile)
                val myReader = BufferedReader(InputStreamReader(fIn))
                var aDataRow: String?
                while (myReader.readLine().also { aDataRow = it } != null) {
                    aBuffer.append(aDataRow)
                }
                myReader.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            val tatig =
                aBuffer.toString().split("&teiler%".toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray()
            val tat1 = findViewById<Button>(R.id.button6)
            val tat2 = findViewById<Button>(R.id.button7)
            val tat3 = findViewById<Button>(R.id.button8)
            val tat4 = findViewById<Button>(R.id.button9)
            tat1.text = tatig[0]
            tat2.text = tatig[1]
            tat3.text = tatig[2]
            tat4.text = tatig[3]
            tat1.setOnClickListener {
                tat = tat1.text.toString()
                drueckmich3()
            }
            tat2.setOnClickListener {
                tat = tat2.text.toString()
                drueckmich3()
            }
            tat3.setOnClickListener {
                tat = tat3.text.toString()
                drueckmich3()
            }
            tat4.setOnClickListener {
                tat = tat4.text.toString()
                drueckmich3()
            }
        } else {
            Toast.makeText(
                this@MainActivity,
                getString(R.string.file_not_exists),
                Toast.LENGTH_SHORT
            ).show()
            val sp8 = getSharedPreferences("dms", 0)
            val ed = sp8.edit()
            ed.putString("default", "")
            ed.apply()
            start()
        }
    }

    private fun drueckmich3() {
        setContentView(R.layout.suchname)
        layout = 7
        val outMetrics = resources.displayMetrics
        val density = outMetrics.density
        val dpWidth = outMetrics.widthPixels / density
        val dpHeight = outMetrics.heightPixels / density
        var setwidth = ((dpHeight / 2 - 30) * density).toInt()
        val berechn = (dpWidth - (dpHeight / 2 - 30) * 4) / 4
        var margin = (berechn * density).toInt() //rechtsmargin
        var margintop = (20 * density).toInt()
        var marginleft = (berechn / 2 * density).toInt()
        if (berechn < 0) {
            setwidth = ((dpWidth - 80) / 4 * density).toInt()
            margin = (20 * density).toInt() //rechtsmargin
            margintop = ((dpHeight - (dpWidth - 80) / 2) / 2 * density).toInt()
            marginleft = (10 * density).toInt()
        }
        val z1 = findViewById<AppCompatImageView>(R.id.button11)
        val z2 = findViewById<AppCompatImageView>(R.id.button13)
        val z3 = findViewById<AppCompatImageView>(R.id.button16)
        val z4 = findViewById<AppCompatImageView>(R.id.button17)
        val kreis = findViewById<AppCompatImageView>(R.id.button18)
        val rechteck = findViewById<AppCompatImageView>(R.id.button20)
        val viereck = findViewById<AppCompatImageView>(R.id.button21)
        val smiley = findViewById<AppCompatImageView>(R.id.button22)
        var params = RelativeLayout.LayoutParams(setwidth, setwidth)
        params.setMargins(marginleft, margintop * 2 / 3, margin, margintop / 3)
        z1.layoutParams = params
        params = RelativeLayout.LayoutParams(setwidth, setwidth)
        params.addRule(RelativeLayout.RIGHT_OF, R.id.button11)
        params.setMargins(0, margintop * 2 / 3, margin, margintop / 3)
        z2.layoutParams = params
        params = RelativeLayout.LayoutParams(setwidth, setwidth)
        params.setMargins(0, margintop * 2 / 3, margin, margintop / 3)
        params.addRule(RelativeLayout.RIGHT_OF, R.id.button13)
        z3.layoutParams = params
        params = RelativeLayout.LayoutParams(setwidth, setwidth)
        params.addRule(RelativeLayout.RIGHT_OF, R.id.button16)
        params.setMargins(0, margintop * 2 / 3, marginleft, margintop / 3)
        z4.layoutParams = params
        params = RelativeLayout.LayoutParams(setwidth, setwidth)
        params.setMargins(marginleft, margintop / 3, margin, margintop * 2 / 3)
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
        kreis.layoutParams = params
        params = RelativeLayout.LayoutParams(setwidth, setwidth)
        params.addRule(RelativeLayout.RIGHT_OF, R.id.button18)
        params.setMargins(0, margintop / 3, margin, margintop * 2 / 3)
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
        rechteck.layoutParams = params
        params = RelativeLayout.LayoutParams(setwidth, setwidth)
        params.addRule(RelativeLayout.RIGHT_OF, R.id.button20)
        params.setMargins(0, margintop / 3, margin, margintop * 2 / 3)
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
        viereck.layoutParams = params
        params = RelativeLayout.LayoutParams(setwidth, setwidth)
        params.addRule(RelativeLayout.RIGHT_OF, R.id.button21)
        params.setMargins(0, margintop / 3, marginleft, margintop * 2 / 3)
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
        smiley.layoutParams = params
        val hackenoben = findViewById<AppCompatImageView>(R.id.imageView2)
        val hackenunten = findViewById<AppCompatImageView>(R.id.imageView3)
        hackenoben.visibility = View.INVISIBLE
        hackenunten.visibility = View.INVISIBLE
        z1.setOnClickListener {
            resultClickProcess(1,hackenoben,R.id.button11,false)
        }
        z2.setOnClickListener {
            resultClickProcess(2,hackenoben,R.id.button13,false)
        }
        z3.setOnClickListener {
            resultClickProcess(3,hackenoben,R.id.button16,false)
        }
        z4.setOnClickListener {
            resultClickProcess(4,hackenoben,R.id.button17,false)
        }
        kreis.setOnClickListener {
            resultClickProcess(1,hackenunten,R.id.button18,true)
        }
        rechteck.setOnClickListener {
            resultClickProcess(2,hackenunten,R.id.button20,true)
        }
        viereck.setOnClickListener {
            resultClickProcess(3,hackenunten,R.id.button21,true)
        }
        smiley.setOnClickListener {
            resultClickProcess(4,hackenunten,R.id.button22,true)
        }
    }

    private fun drueckmich(layout:Int,nameIndex:Int){
        setContentView(R.layout.ndmm1)
        this.layout = layout
        val ed1 = findViewById<EditText>(R.id.editText6)
        val ed2 = findViewById<EditText>(R.id.editText7)
        val ed3 = findViewById<EditText>(R.id.editText9)
        val ed4 = findViewById<EditText>(R.id.editText27)
        val ed5 = findViewById<EditText>(R.id.editText42)
        val ed6 = findViewById<EditText>(R.id.editText43)
        val ed7 = findViewById<EditText>(R.id.editText44)
        val ed8 = findViewById<EditText>(R.id.editText45)
        ed1.setText(names[nameIndex])
        ed2.setText(names[nameIndex+1])
        ed3.setText(names[nameIndex+2])
        ed4.setText(names[nameIndex+3])
        ed5.setText(names[nameIndex+16])
        ed6.setText(names[nameIndex+17])
        ed7.setText(names[nameIndex+18])
        ed8.setText(names[nameIndex+19])
        val btn = findViewById<Button>(R.id.button14)
        btn.setOnClickListener {
            names[nameIndex] = ed1.text.toString()
            names[nameIndex+1] = ed2.text.toString()
            names[nameIndex+2] = ed3.text.toString()
            names[nameIndex+3] = ed4.text.toString()
            names[nameIndex+16] = ed5.text.toString()
            names[nameIndex+17] = ed6.text.toString()
            names[nameIndex+18] = ed7.text.toString()
            names[nameIndex+19] = ed8.text.toString()
            if ( names[nameIndex]!!.isNotEmpty() && names[nameIndex+1]!!.isNotEmpty() && names[nameIndex+2]!!.isNotEmpty() && names[nameIndex+3]!!.isNotEmpty() && names[nameIndex+16]!!.isNotEmpty() && names[nameIndex+17]!!.isNotEmpty() && names[nameIndex+18]!!.isNotEmpty() && names[nameIndex+19]!!.isNotEmpty()) {
                if(layout==13) auswahl5()
                else drueckmich(layout+1,nameIndex+4)
            } else Toast.makeText(
                this@MainActivity,
                getString(R.string.fill_all_fields),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun namenausloten() {
        val aBuffer = StringBuilder()
        try {
            val tt: String
            if (dateipfad!!.isEmpty()) {
                val sp = getSharedPreferences("dms", 0)
                tt = sp.getString("default", "")!!
            } else {
                tt = dateipfad!!.replace(".drmch", "")
                dateipfad = ""
            }
            val myFile = File(getExternalFilesDir(null), "$tt.drmch")
            val fIn = FileInputStream(myFile)
            val myReader = BufferedReader(InputStreamReader(fIn))
            var aDataRow: String?
            while (myReader.readLine().also { aDataRow = it } != null) {
                aBuffer.append(aDataRow)
            }
            myReader.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        val tatig = aBuffer.toString().split("&teiler%".toRegex()).dropLastWhile { it.isEmpty() }
            .toTypedArray()
        name = if (geschlecht){
            tatig[(symbol)*4+(zahl-1)]
        }
        else{
            tatig[(symbol)*4+(zahl-1)+16]
        }
        ende()
    }

    private fun ende() {
        val dialog = Dialog(this, R.style.AppDialog)
        dialog.setContentView(R.layout.alertdia)
        dialog.setCancelable(true)
        val tv1 = dialog.findViewById<TextView>(R.id.textView)
        tv1.text = "Du hast $tat mit $name gewählt"
        val tv3 = dialog.findViewById<TextView>(R.id.textView3)
        val tv2 = dialog.findViewById<TextView>(R.id.textView2)
        val tv4 = dialog.findViewById<TextView>(R.id.textView4)
        dialog.show()
        tv2.setOnClickListener{
            backToStart(dialog)
        }
        tv3.setOnClickListener{
            backToStart(dialog)
        }
        tv4.setOnClickListener {
            takescreenshot(dialog)
            backToStart(dialog)
        }
        dialog.setOnCancelListener {
            backToStart(dialog)
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            when (layout) {
                0 -> {
                    finish()
                }
                10 -> {
                    taetigerstellen()
                }
                1, 3, 5, 15 -> {
                    titel = ""
                    actions.forEachIndexed { i, _ ->
                        actions[i] = null
                    }
                    names.forEachIndexed { i, _ ->
                        names[i] = null
                    }
                    start()
                    layout = 0
                }
                6 -> {
                    drueckmich1()
                    layout = 5
                }
                7 -> {
                    drueckmich2()
                    zahl = 0
                    symbol = 0
                    layout = 6
                }
                11, 12, 13 -> {
                    val ed1 = findViewById<EditText>(R.id.editText6)
                    val ed2 = findViewById<EditText>(R.id.editText7)
                    val ed3 = findViewById<EditText>(R.id.editText9)
                    val ed4 = findViewById<EditText>(R.id.editText27)
                    val ed5 = findViewById<EditText>(R.id.editText42)
                    val ed6 = findViewById<EditText>(R.id.editText43)
                    val ed7 = findViewById<EditText>(R.id.editText44)
                    val ed8 = findViewById<EditText>(R.id.editText45)
                    var startIndex=4;
                    if (layout==12) startIndex=8
                    else if (layout==13) startIndex=12
                    names[startIndex] = ed1.text.toString()
                    names[startIndex+1] = ed2.text.toString()
                    names[startIndex+2] = ed3.text.toString()
                    names[startIndex+3] = ed4.text.toString()
                    names[startIndex+16] = ed5.text.toString()
                    names[startIndex+17] = ed6.text.toString()
                    names[startIndex+18] = ed7.text.toString()
                    names[startIndex+19] = ed8.text.toString()
                    drueckmich(layout-1,startIndex-4)
                }
                20, 16 -> {
                    start()
                }
            }
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun auswahl5() {
        if (ui == 1) {
            save=actions.joinToString("&teiler%")+"&teiler%"+names.joinToString("&teiler%")+"&teiler%false"
            val delte = File(getExternalFilesDir(null), "$delete.drmch")
            if (delte.exists()) delte.delete()
            var testFile = File(getExternalFilesDir(null), "$titel.drmch")
            var inter = 2
            var tite = titel
            while (testFile.exists()) {
                tite = "$titel($inter)"
                testFile = File(getExternalFilesDir(null), "$tite.drmch")
                inter += 1
            }
            try {
                testFile.createNewFile()
                val writer = BufferedWriter(FileWriter(testFile, true /*append*/))
                writer.write(save)
                writer.close()
                Toast.makeText(this@MainActivity,
                    getString(R.string.changes_saved), Toast.LENGTH_SHORT)
                    .show()
                val widgetIDs = AppWidgetManager.getInstance(this@MainActivity)
                    .getAppWidgetIds(ComponentName(this@MainActivity, WigetListe::class.java))
                for (id in widgetIDs) AppWidgetManager.getInstance(this@MainActivity)
                    .notifyAppWidgetViewDataChanged(id, R.id.widgetlistview)
                val sp8 = getSharedPreferences("dms", 0)
                val ed = sp8.edit()
                ed.putString("default", tite)
                ed.apply()
                titel = ""
                actions.forEachIndexed { i, _ ->
                    actions[i] = null
                }
                names.forEachIndexed { i, _ ->
                    names[i] = null
                }
                start()
                layout = 0
            } catch (e: Exception) {
                Toast.makeText(
                    this@MainActivity,
                    getString(R.string.cant_edit_dm),
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            save=actions.joinToString("&teiler%")+"&teiler%"+names.joinToString("&teiler%")+"&teiler%false"
            var testFile = File(getExternalFilesDir(null), "$titel.drmch")
            try {
                var inter = 2
                var tite = titel
                while (testFile.exists()) {
                    tite = "$titel($inter)"
                    testFile = File(getExternalFilesDir(null), "$tite.drmch")
                    inter += 1
                }
                testFile.createNewFile()
                val writer = BufferedWriter(FileWriter(testFile, true /*append*/))
                writer.write(save)
                writer.close()
                Toast.makeText(
                    this@MainActivity,
                    getString(R.string.dm_saved),
                    Toast.LENGTH_SHORT
                ).show()
                val widgetIDs = AppWidgetManager.getInstance(this@MainActivity)
                    .getAppWidgetIds(ComponentName(this@MainActivity, WigetListe::class.java))
                for (id in widgetIDs) AppWidgetManager.getInstance(this@MainActivity)
                    .notifyAppWidgetViewDataChanged(id, R.id.widgetlistview)
                val sp8 = getSharedPreferences("dms", 0)
                val ed = sp8.edit()
                ed.putString("default", tite)
                ed.apply()
                actions.forEachIndexed { i, _ ->
                    actions[i] = null
                }
                names.forEachIndexed { i, _ ->
                    names[i] = null
                }
                start()
                layout = 0
            } catch (e: Exception) {
                Toast.makeText(
                    this@MainActivity,
                    getString(R.string.cant_save_dm),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun takescreenshot(alertDialog: Dialog) {
        val mainView = this.window.decorView.findViewById<View>(android.R.id.content)
        val bitmap = getBitmapFromView(mainView)
        if (alertDialog.window == null) return
        val dialog = alertDialog.window!!.decorView.rootView
        val loc = IntArray(2)
        mainView.getLocationOnScreen(loc)
        val loc2 = IntArray(2)
        dialog.getLocationOnScreen(loc2)
        val bitmap1 = getBitmapFromView(dialog)
        val canvas = Canvas(bitmap)
        canvas.drawBitmap(
            bitmap1,
            (loc2[0] - loc[0]).toFloat(),
            (loc2[1] - loc[1]).toFloat(),
            Paint()
        )
        val c = Calendar.getInstance()
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.GERMANY)
        val tit = sdf.format(c.time)
        imagepath = File(getExternalFilesDir(null), "$tit.png")
        try {
            imagepath!!.createNewFile()
            val fil = FileOutputStream(imagepath)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fil)
            fil.flush()
            fil.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        val uri = FileProvider.getUriForFile(
            this@MainActivity,
            BuildConfig.APPLICATION_ID + ".provider",
            imagepath!!
        )
        val sharingintent = Intent(Intent.ACTION_SEND)
        sharingintent.setType("image/*")
        sharingintent.putExtra(Intent.EXTRA_STREAM, uri)
        startActivity(Intent.createChooser(sharingintent, "Teilen über..."))
    }

    fun taetigerstellen() {
        layout = 15
        setContentView(R.layout.ndm)
        val edt = findViewById<EditText>(R.id.editText)
        val edt2 = findViewById<EditText>(R.id.editText2)
        val edt3 = findViewById<EditText>(R.id.editText3)
        val edt4 = findViewById<EditText>(R.id.editText4)
        val edt5 = findViewById<EditText>(R.id.editText5)
        edt5.setText(titel)
        edt5.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                if (edt5.length() > 20) {
                    edt5.setText(edt5.text.toString().substring(0, 20))
                    Toast.makeText(
                        this@MainActivity,
                        getString(R.string.char_limit_20),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun afterTextChanged(editable: Editable) {}
        })
        edt.setText(actions[0])
        edt2.setText(actions[1])
        edt3.setText(actions[2])
        edt4.setText(actions[3])
        val btn = findViewById<Button>(R.id.button10)
        btn.setOnClickListener {
            if (edt.text.toString().isNotEmpty() && edt2.text.toString().isNotEmpty() && edt3.text.toString()
                    .isNotEmpty() && edt4.text.toString().isNotEmpty() && edt5.text.toString()
                    .isNotEmpty()
            ) {
                actions[0] = edt.text.toString()
                actions[1] = edt2.text.toString()
                actions[2] = edt3.text.toString()
                actions[3] = edt4.text.toString()
                titel = edt5.text.toString()
                drueckmich(10,0)
            } else Toast.makeText(
                this@MainActivity,
                getString(R.string.fill_all_fields),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun bewertung() {
        val sp = getSharedPreferences("dms", 0)
        tt = sp.getBoolean("bewert", false)
        if (!tt!!) {
            val alertdo = Dialog(this@MainActivity, R.style.AppDialog)
            alertdo.setContentView(R.layout.alertdia)
            alertdo.setCancelable(true)
            val tv1 = alertdo.findViewById<TextView>(R.id.textView)
            tv1.text = getString(R.string.rate_dm_playstore)
            val tv3 = alertdo.findViewById<TextView>(R.id.textView3)
            val tv2 = alertdo.findViewById<TextView>(R.id.textView2)
            val tv4 = alertdo.findViewById<TextView>(R.id.textView4)
            tv4.text = getString(R.string.never)
            tv2.text = getString(R.string.later)
            tv3.text = getString(R.string.letsgo)
            alertdo.show()
            tv4.setOnClickListener {
                alertdo.dismiss()
                tt = true
                val sp3 = getSharedPreferences("dms", 0)
                val ede = sp3.edit()
                ede.putBoolean("bewert", tt!!)
                ede.apply()
            }
            tv2.setOnClickListener { alertdo.dismiss() }
            tv3.setOnClickListener {
                alertdo.dismiss()
                val uri =
                    Uri.parse("https://play.google.com/store/apps/details?id=de.kruemelopment.org.drckmichspiel")
                val intent = Intent(Intent.ACTION_VIEW, uri)
                startActivity(intent)
                tt = true
                val sp3 = getSharedPreferences("dms", 0)
                val ede = sp3.edit()
                ede.putBoolean("bewert", tt!!)
                ede.apply()
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handlelink(intent)
    }

    private fun handlelink(intent: Intent) {
        val appLinkAction = intent.action
        if (Intent.ACTION_VIEW == appLinkAction) {
            try {
                if (intent.data == null) {
                    Toast.makeText(
                        this@MainActivity,
                        getString(R.string.save_failed),
                        Toast.LENGTH_SHORT
                    ).show()
                    return
                }
                if (!getFileName(intent.data).contains("drmch")) {
                    Toast.makeText(
                        this@MainActivity,
                        getString(R.string.no_dm_error),
                        Toast.LENGTH_SHORT
                    ).show()
                    return
                }
                val cr = contentResolver
                val `is` = cr.openInputStream(intent.data!!) ?: return
                val buf = StringBuilder()
                val reader = BufferedReader(InputStreamReader(`is`))
                var str: String?
                while (reader.readLine().also { str = it } != null) {
                    buf.append(str).append("\n")
                }
                `is`.close()
                val memo = buf.toString()
                var file = File(
                    getExternalFilesDir(null),
                    memo.split("&teiler%".toRegex()).dropLastWhile { it.isEmpty() }
                        .toTypedArray()[37] + ".drmch")
                var dtat = 2
                while (file.exists()) {
                    file = File(
                        getExternalFilesDir(null),
                        file.name.replace(".drmch", "") + "(" + dtat + ")" + ".drmch"
                    )
                    dtat += 1
                }
                file.createNewFile()
                val writer = BufferedWriter(FileWriter(file, true /*append*/))
                writer.write(memo)
                writer.close()
                Toast.makeText(this@MainActivity, getString(R.string.dm_saved), Toast.LENGTH_SHORT)
                    .show()
                val widgetIDs = AppWidgetManager.getInstance(this)
                    .getAppWidgetIds(ComponentName(this, WigetListe::class.java))
                for (id in widgetIDs) AppWidgetManager.getInstance(this)
                    .notifyAppWidgetViewDataChanged(id, R.id.widgetlistview)
                val i = getIntent()
                i.setAction(null)
                setIntent(i)
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity,
                    getString(R.string.save_failed), Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun setting() {
        setContentView(R.layout.settings)
        layout = 16
        val editText = findViewById<EditText>(R.id.editText8)
        val sp = getSharedPreferences("dms", 0)
        val name = sp.getString("von", "Unbekannt")
        editText.setText(name)
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                if (editText.length() > 20) {
                    editText.setText(editText.text.toString().substring(0, 20))
                    Toast.makeText(
                        this@MainActivity,
                        getString(R.string.char_limit_20),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun afterTextChanged(editable: Editable) {
                val sp3 = getSharedPreferences("dms", 0)
                val ede = sp3.edit()
                ede.putString("von", editText.text.toString())
                ede.apply()
            }
        })
        val button = findViewById<Button>(R.id.button12)
        button.setOnClickListener {
            val chooseFile = Intent(Intent.ACTION_GET_CONTENT)
            chooseFile.addCategory(Intent.CATEGORY_OPENABLE)
            chooseFile.setType("*/*")
            startActivityForResult(
                Intent.createChooser(chooseFile, getString(R.string.select_dm)),
                20
            )
        }
        val info = findViewById<ImageView>(R.id.imageView)
        info.setOnClickListener {
            val alertdo = Dialog(this@MainActivity, R.style.AppDialog)
            alertdo.setContentView(R.layout.alertdia)
            alertdo.setCancelable(true)
            val tv1 = alertdo.findViewById<TextView>(R.id.textView)
            tv1.text = getString(R.string.share_explanation)
            val tv3 = alertdo.findViewById<TextView>(R.id.textView3)
            val tv2 = alertdo.findViewById<TextView>(R.id.textView2)
            val tv4 = alertdo.findViewById<TextView>(R.id.textView4)
            tv4.text = ""
            tv2.text = getString(R.string.okay)
            tv3.text = ""
            alertdo.show()
            tv2.setOnClickListener { alertdo.dismiss() }
        }
    }

    public override fun onResume() {
        try {
            if (loeschen) sharefile!!.delete()
        } catch (ignored: Exception) {
        }
        setFullScreen()
        super.onResume()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 20) {
            if (resultCode == RESULT_OK) {
                data.setAction(Intent.ACTION_VIEW)
                handlelink(data)
            }
        }
    }

    private fun getFileName(uri: Uri?): String {
        var result: String? = null
        if (uri!!.scheme == "content") {
            val cursor = contentResolver.query(uri, null, null, null, null)
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                }
            } finally {
                cursor!!.close()
            }
            cursor.close()
        }
        if (result == null) {
            result = uri.path
            val cut = result!!.lastIndexOf('/')
            if (cut != -1) {
                result = result.substring(cut + 1)
            }
        }
        return result
    }
    private fun getBitmapFromView(view: View): Bitmap {
        val bitmap = Bitmap.createBitmap(
            view.width, view.height, Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }
    fun checkforAGB() {
        val sese = getSharedPreferences("Start", 0)
        val web = sese.getBoolean("agbs", false)
        if (!web && showdialog) {
            val dialog = Dialog(this, R.style.AppDialog)
            dialog.setContentView(R.layout.webdialog)
            val ja = dialog.findViewById<TextView>(R.id.textView5)
            val nein = dialog.findViewById<TextView>(R.id.textView8)
            ja.setOnClickListener {
                val ed = sese.edit()
                ed.putBoolean("agbs", true)
                ed.apply()
                dialog.dismiss()
            }
            nein.setOnClickListener {
                finishAndRemoveTask()
            }
            val textView = dialog.findViewById<TextView>(R.id.textView4)
            textView.text = Html.fromHtml(
                "Mit der Nutzung dieser App aktzeptiere ich die " +
                        "<a href=\"https://www.kruemelopment-dev.de/datenschutzerklaerung\">Datenschutzerklärung</a>" + " und die " + "<a href=\"https://www.kruemelopment-dev.de/nutzungsbedingungen\">Nutzungsbedingungen</a>" + " von Krümelopment Dev",HtmlCompat.FROM_HTML_MODE_LEGACY
            )
            textView.movementMethod = LinkMovementMethod.getInstance()
            dialog.setCancelable(false)
            dialog.show()
        }
    }
    private fun setFullScreen(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
        } else {
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.decorView.setOnApplyWindowInsetsListener { _, insets ->
                if (insets.displayCutout != null) {
                    insetpixels = insets.displayCutout!!.boundingRects[0].width()
                    inset = true
                }
                insets
            }
        }
    }
    private fun handleIntent(intent:Intent){
        try {
            val dm = intent.getStringExtra("Drück mich")
            if (dm != null && dm == "default") {
                val sp = getSharedPreferences("dms", 0)
                val tt = sp.getString("default", "")
                if (tt != null && tt.isEmpty()) {
                    start()
                    Toast.makeText(
                        this,
                        getString(R.string.no_dm_selected),
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(this, "$tt wird gespielt", Toast.LENGTH_SHORT).show()
                    drueckmich1()
                }
            } else {
                val myFile = dm?.let { File(getExternalFilesDir(null), it) }
                if (myFile!!.exists()) {
                    dateipfad = dm
                    drueckmich1()
                } else {
                    val widgetIDs = AppWidgetManager.getInstance(this@MainActivity)
                        .getAppWidgetIds(ComponentName(this@MainActivity, WigetListe::class.java))
                    for (id in widgetIDs) AppWidgetManager.getInstance(this@MainActivity)
                        .notifyAppWidgetViewDataChanged(id, R.id.widgetlistview)
                    start()
                }
            }
            intent.removeExtra("Drück mich")
        } catch (e: Exception) {
            start()
            if (!intent.getBooleanExtra("Shortcut", false)) handlelink(intent)
        }
    }
    private fun backToStart(dialog:Dialog){
        dialog.dismiss()
        start()
        zahl = 0
        symbol = 0
        layout = 0
        bewertung()
    }
    private fun resultClickProcess(inputNumber:Int,hackenView:AppCompatImageView,viewID:Int,value:Boolean){
        var checkvalue=zahl;
        if(value) checkvalue=symbol;
        if (checkvalue == inputNumber) {
            if (value)symbol = 0
            else zahl=0
            hackenView.visibility = View.INVISIBLE
        } else {
            if (value)symbol = inputNumber
            else zahl= inputNumber
            hackenView.visibility = View.VISIBLE
            val param = hackenView.layoutParams as RelativeLayout.LayoutParams
            param.addRule(RelativeLayout.ALIGN_TOP, viewID)
            param.addRule(RelativeLayout.ALIGN_RIGHT,viewID)
            param.addRule(RelativeLayout.ALIGN_END, viewID)
            hackenView.layoutParams = param
        }
        if (symbol != 0 && zahl!=0) {
            namenausloten()
        }
    }
}