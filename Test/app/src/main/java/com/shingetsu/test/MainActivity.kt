package com.shingetsu.test

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button
import android.widget.Toast

class MainActivity : AppCompatActivity() {
    private val fruits = arrayOf("Apple", "Banana", "Cherry", "Date", "Grape", "Kiwi", "Mango", "Pear")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        title = "KotlinApp"
        val autoTextView: AutoCompleteTextView = findViewById(R.id.text)
        val adapter: ArrayAdapter<String> = ArrayAdapter<String>(this,
                android.R.layout.select_dialog_item, fruits)
        autoTextView.threshold = 1
        autoTextView.setAdapter(adapter)
    }
}