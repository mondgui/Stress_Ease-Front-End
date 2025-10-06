package com.example.stressease

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.widget.Spinner
import android.widget.ArrayAdapter

class SOS:AppCompatActivity(){

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SOSAdapter
    private lateinit var spinnerCountry: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sos)

        recyclerView = findViewById(R.id.rvSOSContacts)
        spinnerCountry = findViewById(R.id.spinnerCountry)

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = SOSAdapter()
        recyclerView.adapter = adapter

        setupCountryDropdown()
    }
    private fun setupCountryDropdown() {
        val countries = listOf("India", "United States", "United Kingdom", "Canada")
        val adapterSpinner = ArrayAdapter(this, android.R.layout.simple_spinner_item, countries)
        adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCountry.adapter = adapterSpinner

        spinnerCountry.setSelection(0)

        spinnerCountry.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) {
                val selectedCountry = countries[position]
                loadCrisisContacts(selectedCountry)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }
    private fun loadCrisisContacts(selectedCountry: String){
        val prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        val token = prefs.getString("authToken", null)

        if (token == null) {
            Toast.makeText(this, "Please log in again", Toast.LENGTH_SHORT).show()
            return
        }
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.api.getCrisisContacts("Bearer $token")
                withContext(Dispatchers.Main) {
                    if(response.isSuccessful && response.body()?.success == true){
                        adapter.setData(response.body()!!.contacts)
                    } else {
                        Toast.makeText(
                            this@SOS,
                            "Unable to load crisis contacts for $selectedCountry",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@SOS, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
