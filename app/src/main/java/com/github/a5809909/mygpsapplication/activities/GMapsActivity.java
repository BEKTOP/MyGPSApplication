package com.github.a5809909.mygpsapplication.activities;

//import com.google.android.maps.MapActivity;
//import com.google.android.maps.MapController;
//import com.google.android.maps.MapView;
//import com.google.android.maps.Overlay;

public class GMapsActivity extends MapActivity {

//    private MapView map;
//    private LocationManager manager;
//    private Location loc;
//    private LocationListener listener = new LocationListener() {
//
//        @Override
//        public void onLocationChanged(Location loc) {
//            setLocation(loc);
//            GeoPoint p = new GeoPoint((int) (loc.getLatitude() * 1e6), (int) (loc.getLongitude() * 1e6));
//            map.getOverlays().add(new MarkerOverlay(p));
//            map.invalidate();
//            map.getController().animateTo(p);
//        }
//
//        @Override
//        public void onProviderDisabled(String provider) {
//            Toast.makeText(GMapsActivity.this, provider + " disabled", Toast.LENGTH_SHORT).show();
//        }
//
//        @Override
//        public void onProviderEnabled(String provider) {
//            Toast.makeText(GMapsActivity.this, provider + " enabled", Toast.LENGTH_SHORT).show();
//        }
//
//        @Override
//        public void onStatusChanged(String provider, int status, Bundle extras) {
//        }
//    };
//
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
//        loc = getLastCopords();
//        buildUI();
//        requestNewCoordinates();
//    }
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//        if (manager != null)
//            manager.removeUpdates(listener);
//    }
//
//    public void setLocation(Location loc) {
//        this.loc = loc;
//    }
//
//    private Location getLastCopords() {
//        String[] providers = new String[]
//                { LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER, LocationManager.PASSIVE_PROVIDER };
//        Location loc = null;
//        for (String provider : providers) {
//            loc = manager.getLastKnownLocation(provider);
//            if (loc != null) {
//                break;
//            }
//        }
//        return loc;
//    }
//
//    private void buildUI() {
//        RelativeLayout root = new RelativeLayout(this);
//
//        // creating map with zoom controls and set center
//        map = new MapView(this, "0kUYZ329eS_2MX4EyZ6YbJq4KFLm0hjiK1zjxLw");
//        map.setBuiltInZoomControls(true);
//        map.setClickable(true);
//        MapController controller = map.getController();
//        if (loc != null) {
//            GeoPoint p = new GeoPoint((int) (loc.getLatitude() * 1e6), (int) (loc.getLongitude() * 1e6));
//            controller.setCenter(p);
//            map.getOverlays().add(new MarkerOverlay(p));
//        } else {
//            controller.setCenter(new GeoPoint(0, 0));
//        }
//        controller.setZoom(4);
//        root.addView(map, LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
//
//        // creating custom controls panel
//        LinearLayout panel = new LinearLayout(this);
//        panel.setOrientation(LinearLayout.VERTICAL);
//        panel.setBackgroundColor(Color.argb(200, 200, 200, 200));
//        RelativeLayout.LayoutParams plp = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
//        plp.addRule(RelativeLayout.ALIGN_PARENT_TOP | RelativeLayout.ALIGN_PARENT_RIGHT);
//
//        // map mode button
//        ImageView mode = new ImageView(this);
//        mode.setImageResource(android.R.drawable.ic_menu_mapmode);
//        mode.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                map.setSatellite(!map.isSatellite());
//            }
//        });
//        panel.addView(mode, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
//
//        // go to my location button
//        ImageView my = new ImageView(this);
//        my.setImageResource(android.R.drawable.ic_menu_mylocation);
//        my.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (loc != null) {
//                    map.getController()
//                            .animateTo(new GeoPoint((int) (loc.getLatitude() * 1e6), (int) (loc.getLongitude() * 1e6)));
//                } else {
//                    Toast.makeText(GMapsActivity.this, "Coordinates is not found", Toast.LENGTH_SHORT).show();
//                }
//            }
//        });
//        panel.addView(my, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
//        root.addView(panel, plp);
//
//        // show all
//        setContentView(root, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
//    }
//
//    private void requestNewCoordinates() {
//        manager.removeUpdates(listener);
//
//        final WifiManager wfManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
//        if (manager.getAllProviders().contains(LocationManager.GPS_PROVIDER)
//                && manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
//            manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, listener);
//        } else if (manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
//                && wfManager.isWifiEnabled()) {
//            manager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, listener);
//        } else if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.ECLAIR_MR1
//                && manager.isProviderEnabled(LocationManager.PASSIVE_PROVIDER)) {
//            manager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 0, 0, listener);
//        } else {
//            Toast.makeText(this, "Location providers not found", Toast.LENGTH_SHORT).show();
//        }
//    }
//
//    @Override
//    protected boolean isRouteDisplayed() {
//        return false;
//    }
//
//    private class MarkerOverlay extends Overlay {
//        private GeoPoint p;
//
//        public MarkerOverlay(GeoPoint p) {
//            this.p = p;
//        }
//
//        @Override
//        public boolean draw(Canvas canvas, MapView mapView, boolean shadow, long when) {
//            super.draw(canvas, mapView, shadow);
//
//            // translate the GeoPoint to screen pixels
//            Point screenPts = new Point();
//            mapView.getProjection().toPixels(p, screenPts);
//
//            // add the marker
//            Bitmap bmp = BitmapFactory.decodeResource(getResources(), android.R.drawable.ic_menu_myplaces);
//            canvas.drawBitmap(bmp, screenPts.x, screenPts.y - 50, null);
//            return true;
//        }
//    }
}