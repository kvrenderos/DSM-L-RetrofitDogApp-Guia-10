package com.example.retrofitdogapp

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.retrofitdogapp.databinding.ActivityMainBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Locale

class MainActivity : AppCompatActivity(), SearchView.OnQueryTextListener {
    private lateinit var binding: ActivityMainBinding
    private lateinit var dogAdapter: DogAdapter
    private var images: MutableList<String> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inflar el diseño de la actividad y establecerlo como el contenido de la vista
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        initRecyclerView()
        binding.searchDogs.setOnQueryTextListener(this)
    }

    private fun initRecyclerView() {
        dogAdapter = DogAdapter(images)
        binding.listDogs.layoutManager = LinearLayoutManager(this)
        binding.listDogs.adapter = dogAdapter
    }

    private fun searchByName(raza: String) {
        val batch: Call<DogsResponse?>? = RetrofitClient.instance.getDogsByBreed(raza)
        batch?.enqueue(object : Callback<DogsResponse?> {
            override fun onResponse(call: Call<DogsResponse?>, response: Response<DogsResponse?>) {
                if (response.isSuccessful && response.body() != null) {
                    val responseImages = response.body()?.getImages()?.filterNotNull() ?: emptyList()
                    images.clear()
                    images.addAll(responseImages)
                    dogAdapter.notifyDataSetChanged()
                }
            }

            override fun onFailure(call: Call<DogsResponse?>, t: Throwable) {
                showError()
            }
        })
    }

    private fun showError() {
        Toast
            .makeText(this, "Ocurrió un error", Toast.LENGTH_SHORT)
            .show()
    }

    override fun onQueryTextChange(query: String?): Boolean {
        return true
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        if (!query.isNullOrEmpty()) {
            searchByName(query.lowercase(Locale.getDefault()))
        }
        return true
    }
}
