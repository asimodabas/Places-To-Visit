package com.asimodabas.places_to_visit.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.asimodabas.places_to_visit.R
import com.asimodabas.places_to_visit.adapter.PlaceAdapter
import com.asimodabas.places_to_visit.databinding.ActivityMainBinding
import com.asimodabas.places_to_visit.model.Place
import com.asimodabas.places_to_visit.roomdb.PlaceDao
import com.asimodabas.places_to_visit.roomdb.PlaceDatabase
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val compositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val db =
            Room.databaseBuilder(applicationContext, PlaceDatabase::class.java, "Places").build()
        val placeDao = db.placeDao()

        compositeDisposable.add(
            placeDao.getAll()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handlerResponse)
        )
    }

    private fun handlerResponse(placeList: List<Place>) {
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        val adapter = PlaceAdapter(placeList)
        binding.recyclerView.adapter = adapter
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.place_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.add_place) {
            Toast.makeText(this, "Add Place", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, MapsActivity::class.java)
            startActivity(intent)
        }
        return super.onOptionsItemSelected(item)
    }


}