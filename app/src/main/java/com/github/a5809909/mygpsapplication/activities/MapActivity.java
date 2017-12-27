package com.github.a5809909.mygpsapplication.activities;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.github.a5809909.mygpsapplication.R;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapActivity extends AppCompatActivity {

        GoogleMap googleMap;

        //координаты для маркера
        private static final double TARGET_LATITUDE = 53.6781235;
        private static final double TARGET_LONGITUDE = 23.8298073;


    @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_map);
            createMapView();
            addMarker();
            //добавляем на карту свое местоположение
            googleMap.setMyLocationEnabled(true);
        }
        //создаем карту
        private void createMapView(){

            try {
                if(null == googleMap){
                    googleMap = ((MapFragment) getFragmentManager().findFragmentById(
                            R.id.mapView)).getMap();

                    if(null == googleMap) {
                        Toast.makeText(getApplicationContext(),
                                "Error creating map",Toast.LENGTH_SHORT).show();
                    }
                }
            } catch (NullPointerException exception){
                Log.e("mapApp", exception.toString());
            }

        }
        //добавляем маркер на карту
        private void addMarker(){

            double lat = TARGET_LATITUDE;
            double lng = TARGET_LONGITUDE;
            //устанавливаем позицию и масштаб отображения карты
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(lat, lng))
                    .zoom(15)
                    .build();
            CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
            googleMap.animateCamera(cameraUpdate);

            if(null != googleMap){
                googleMap.addMarker(new MarkerOptions()
                        .position(new LatLng(lat, lng))
                        .title("Mark")
                        .draggable(false)
                );
            }
        }

    }

