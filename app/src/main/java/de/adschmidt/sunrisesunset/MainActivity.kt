package de.adschmidt.sunrisesunset

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import de.adschmidt.sunrisesunset.persistence.WidgetPreferenceProvider
import de.adschmidt.sunrisesunset.preferences.WidgetPreferenceActivity


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val listView = findViewById<ListView>(R.id.main_widget_list)
        val widgetIds = WidgetPreferenceProvider.getWidgetIds(this)
            .toList().sorted()
        val widgetNames = widgetIds
            .map { id -> Pair(id,WidgetPreferenceProvider.getPreferencs(id,this)?.customName) }
            .map { (id, name) ->
                if(name != null && !name.isBlank()) name
                else getString(R.string.no_custom_widget_name)+" [$id]"}

        val testAdapter = ArrayAdapter<String>(this,
            android.R.layout.simple_list_item_1,
            widgetNames
        )
        listView.adapter = testAdapter
        listView.onItemClickListener = ListClickListener(widgetIds)
    }

    private inner class ListClickListener(val widgetIds: List<Int>) : AdapterView.OnItemClickListener {
        override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            val intent = Intent(this@MainActivity, WidgetPreferenceActivity::class.java)
            Log.i(TAG, "starting preference activity for widget ${widgetIds[position]}")
            intent.putExtra(WidgetPreferenceActivity.KEY_WIDGET_ID, widgetIds[position])
            startActivity(intent)
        }
    }
}