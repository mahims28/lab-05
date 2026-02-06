package com.example.lab5_starter;


import com.google.firebase.firestore.CollectionReference;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.Firebase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements CityDialogFragment.CityDialogListener {

    private Button addCityButton;
    private ListView cityListView;

    private ArrayList<City> cityArrayList;
    private ArrayAdapter<City> cityArrayAdapter;

    private FirebaseFirestore db;
    private CollectionReference citiesRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        db= FirebaseFirestore.getInstance();
        citiesRef = db.collection("cities");

        EdgeToEdge.enable(this);


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Set views
        addCityButton = findViewById(R.id.buttonAddCity);
        cityListView = findViewById(R.id.listviewCities);

        // create city array
        cityArrayList = new ArrayList<>();
        cityArrayAdapter = new CityArrayAdapter(this, cityArrayList);
        cityListView.setAdapter(cityArrayAdapter);

        citiesRef.addSnapshotListener(( value, error) -> {
            if (error != null){
                Log.e( "Firestore", error.toString());
            }

            if (value != null && !value.isEmpty()){
                cityArrayList.clear();
                for (QueryDocumentSnapshot snapshot : value){
                    String name = snapshot.getString("name");
                    String province = snapshot.getString( "province");

                    cityArrayList.add(new City(name, province));
                }

                cityArrayAdapter.notifyDataSetChanged();
            }

        });
        cityListView.setOnItemLongClickListener((adapterView, view, i, l) -> {
            City city = cityArrayAdapter.getItem(i);
            new AlertDialog.Builder(this)
                    .setTitle("Delete City")
                    .setMessage("Are you sure you want to delete " + city.getName() + "?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        deleteCity(city);
                    })
                    .setNegativeButton("Cancel", null)
                    .show();

            return true;
        });



        // set listeners
        addCityButton.setOnClickListener(view -> {
            CityDialogFragment cityDialogFragment = new CityDialogFragment();
            cityDialogFragment.show(getSupportFragmentManager(), "Add City");
        });

        cityListView.setOnItemClickListener((adapterView, view, i, l) -> {
            City city = cityArrayAdapter.getItem(i);
            CityDialogFragment cityDialogFragment = CityDialogFragment.newInstance(city);
            cityDialogFragment.show(getSupportFragmentManager(), "City Details");
        });


    }

    @Override
    public void updateCity(City city, String title, String year) {
        String oldName = city.getName();
        city.setName(title);
        city.setProvince(year);
        cityArrayAdapter.notifyDataSetChanged();

        // Updating the database using delete + addition
        citiesRef.document(oldName).delete();
        citiesRef.document(city.getName()).set(city);

    }

    @Override
    public void addCity(City city) {
        cityArrayList.add(city);
        cityArrayAdapter.notifyDataSetChanged();

        DocumentReference docRef = citiesRef.document(city.getName());
        docRef.set(city);


    }
    public void deleteCity(City city) {
        cityArrayList.remove(city);
        cityArrayAdapter.notifyDataSetChanged();
        citiesRef.document(city.getName()).delete();
    }

}