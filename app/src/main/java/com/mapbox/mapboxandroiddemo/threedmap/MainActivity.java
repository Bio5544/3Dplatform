package com.mapbox.mapboxandroiddemo.threedmap;


// Java
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
// android
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
// mapbox
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.FillExtrusionLayer;
import com.mapbox.mapboxsdk.style.layers.Layer;
import com.mapbox.mapboxsdk.style.layers.Property;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.mapboxsdk.style.sources.Source;
import static com.mapbox.mapboxsdk.style.expressions.Expression.eq;
import static com.mapbox.mapboxsdk.style.layers.Property.TEXT_ANCHOR_CENTER;
import static com.mapbox.mapboxsdk.style.expressions.Expression.get;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillExtrusionBase;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillExtrusionColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillExtrusionHeight;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillExtrusionOpacity;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillExtrusionVerticalGradient;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAnchor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconPitchAlignment;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconRotationAlignment;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconSize;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconTranslateAnchor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textAnchor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textField;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textFont;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textJustify;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textKeepUpright;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textMaxAngle;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textRadialOffset;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textSize;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textTranslateAnchor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textVariableAnchor;


/**
 * Create a 3D indoor map with the fill-extrude-height paint property
 */
public class MainActivity extends AppCompatActivity {
// define the variables
    private MapView mapView;
    public  TextView label;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Collect the parameters from Seite2 class
        String myStringFromseite2 = getIntent().getStringExtra("mes");// message
        String myStringFromseite3 = getIntent().getStringExtra("ddd");// the information from Json File on The Server .

        // convert the information in Json File to Array .
        List<String> dataFetch = Arrays.asList(myStringFromseite3.split(";"));
        // extract the color and the name of Application from the Array .
        String color_sys = dataFetch.get(dataFetch.size()-4);
        String name_sys = dataFetch.get(dataFetch.size()-3);

// Mapbox access token is configured here. This needs to be called either in your application
// object or in the same activity which contains the mapview.
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token));

// This contains the MapView in XML and needs to be called after the access token is configured.
        setContentView(R.layout.activity_main);

        label= (TextView) findViewById(R.id.textView3);
        label.setText(name_sys);
        label.setTextSize(18f);
        label.setBackgroundColor(Color.parseColor(String.valueOf(color_sys)));

        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull final MapboxMap mapboxMap) {
                // Set the style for the map of the Mapbox that is in the background
                mapboxMap.setStyle(new Style.Builder().fromUri("mapbox://styles/amr00002/ck9yb0mh527s31ipe837clqpf"),
                        new Style.OnStyleLoaded() {
                            @Override
                            public void onStyleLoaded(@NonNull Style style) {
                                mapboxMap.getUiSettings().setLogoEnabled(false);
                                mapboxMap.getUiSettings().setAttributionEnabled(false);
                                // Extract the coordinates of the center of the camera from the Array dataFetch
                                List<Float> cam = new ArrayList<>();
                                String res11 = dataFetch.get(dataFetch.size()-1);
                                String res12 = dataFetch.get(dataFetch.size()-2);
                                cam.add(Float.valueOf(res11));
                                cam.add(Float.valueOf(res12));
                                CameraPosition position55 = new CameraPosition.Builder()
                                        .target(new LatLng(cam.get(1), cam.get(0)))
                                        .build();
                                mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(position55), 4000);
                                List<Integer> lev2 = new ArrayList<>();
                                for (int i=0; i<dataFetch.size()-4; i++) {
                                    String res = dataFetch.get(i);
                                    lev2.add(Integer.valueOf(res));
                                }
                                Collections.sort(lev2);

                                // in this Part all geojson files are read from the server and displayed on the map with no opacity.
                                // If the connection does not work, the Geojson files are taken from the asset folder
                                for (int g =0 ; g < lev2.size(); g++){
                                    Handler handler0 = new Handler();
                                    int finalg = lev2.get(g);
                                    handler0.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                                try {
                                                    style.addSource(new GeoJsonSource("room-data"+ finalg, new URL(myStringFromseite2+"/"+finalg+".geojson")));
                                                    Log.d("message_test", String.valueOf(myStringFromseite2));
                                                } catch (MalformedURLException e) {
                                                    e.printStackTrace();
                                                    try {
                                                        style.addSource(new GeoJsonSource("room-data"+ finalg, new URI("asset://levelTestt/"+finalg+".geojson")));
                                                    } catch (URISyntaxException ex) {
                                                        ex.printStackTrace();
                                                    }
                                                }
                                            style.addLayer(new FillExtrusionLayer(
                                                    "room-extrusion"+ finalg, "room-data"+ finalg).withProperties(
                                                    fillExtrusionColor(get("color")),
                                                    fillExtrusionVerticalGradient(true),
                                                    fillExtrusionHeight(get("height")),
                                                    fillExtrusionBase(get("base_height"))
                                            ));
                                        }
                                    }, 1000+finalg);
                                }
                            }
                        });
            }
        });

        //add dynamic buttons//
        // add the H button (Home button)

        LinearLayout lm = (LinearLayout) findViewById(R.id.linearLayout2);
        Button btn2 = new Button(this);
        // Give button an ID
        btn2.setId(50+2);
        btn2.setText("H");
        btn2.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        String color_sys2 = dataFetch.get(dataFetch.size()-4);
        GradientDrawable shape2 = new GradientDrawable();
        shape2.setShape(GradientDrawable.RECTANGLE);
        shape2.setColor(Color.parseColor(String.valueOf(color_sys2)));
        shape2.setStroke(4, Color.WHITE);
        shape2.setCornerRadius(35);
        btn2.setBackground(shape2);
        btn2.setClickable(true);
        lm.addView(btn2);

        //if you click on H, all floors are displayed with Opacity 1. and all labels and symbols of stairs and toilets are deleted
        View.OnClickListener onButtonClickListener2 = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mapView.getMapAsync(new OnMapReadyCallback() {
                    @Override
                    public void onMapReady(@NonNull final MapboxMap mapboxMap) {
                        List<Float> cam2 = new ArrayList<>();
                        String res13 = dataFetch.get(dataFetch.size()-1);
                        String res14 = dataFetch.get(dataFetch.size()-2);
                        cam2.add(Float.valueOf(res13));
                        cam2.add(Float.valueOf(res14));
                        CameraPosition position2 = new CameraPosition.Builder()
                                .target(new LatLng(cam2.get(1),cam2.get(0)))
                                .zoom(15)
                                .tilt(0)
                                .build();
                        mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(position2), 4000);
                        mapboxMap.getUiSettings().setTiltGesturesEnabled(true);
                        mapboxMap.getStyle(new Style.OnStyleLoaded() {
                            @Override
                            public void onStyleLoaded(@NonNull Style style) {

                                List<Integer> lev3 = new ArrayList<>();
                                for (int i=0; i<dataFetch.size()-4; i++) {
                                    String res1 = dataFetch.get(i);
                                    lev3.add(Integer.valueOf(res1));
                                }
                                Collections.sort(lev3);
                                try {int sszu9 = lev3.get(0);
                                    while ((sszu9) < lev3.size()) {

                                        Layer waterLayer9999= style.getLayer("room-extrusion" +sszu9+99);
                                        Source watersource9= style.getSource("room-data"+sszu9+99);
                                        Layer waterLayer99999= style.getLayer("room-extrusion" + (sszu9));

                                        if (waterLayer9999 != null) {
                                            style.removeLayer(waterLayer9999);}
                                        if (watersource9!= null) {
                                            style.removeSource(watersource9);}
                                        if (waterLayer99999 != null) {
                                            waterLayer99999.setProperties(fillExtrusionOpacity(1f));}
                                        sszu9++;}

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                                try{int sszuuu9 = lev3.get(0);
                                    while ((sszuuu9) < lev3.size()) {

                                        Layer str3 =style.getLayer("room-extrusion"+sszuuu9+55);
                                        Layer toli=style.getLayer("room-extrusion"+sszuuu9+44);

                                        if (str3 != null ) {
                                            style.removeLayer(str3);
                                        }
                                        if (toli != null ) {
                                            style.removeLayer(toli);
                                        }
                                        sszuuu9++;}

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                            }});
                    }});}};btn2.setOnClickListener(onButtonClickListener2);


            List<Integer> lev4 = new ArrayList<>();
            for (int i=0; i<dataFetch.size()-4; i++) {
                String res2 = dataFetch.get(i);
                lev4.add(Integer.valueOf(res2));
            }
            Collections.sort(lev4);


        // dynamic buttons for levels
        for (int j = lev4.size()-1; j >= 0 ; j--) {

            // Create Button
            Button btn = new Button(this);
            // Give button an ID
            btn.setId(j + 1);
            btn.setText(String.valueOf(lev4.get(j)));
            btn.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
            //Add button to LinearLayout
            LayoutParams params = (LayoutParams) btn.getLayoutParams();
            params.setMargins(0, 4, 1, 4);
            params.gravity = Gravity.BOTTOM;
            params.height = 110;
            params.width = 100;
            String color_sys3 = dataFetch.get(dataFetch.size()-4);
            GradientDrawable shape = new GradientDrawable();
            shape.setShape(GradientDrawable.RECTANGLE);
            shape.setColor(Color.parseColor(String.valueOf(color_sys3)));
            shape.setStroke(4, Color.WHITE);
            shape.setCornerRadius(35);
            btn.setBackground(shape);
            btn.setClickable(true);
            try {
                int finalJ = lev4.get(j);
                int finalJ2 = j;

                //When you click on a specific floor, that floor is displayed with opacity 1, and all lower floors are displayed with opacity 0.1.
                // All upper floors are displayed with opacity 0.
                //For the clicked floor, the label is also displayed with the symbols for tolites and stairs.
                View.OnClickListener onButtonClickListener = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mapView.getMapAsync(new OnMapReadyCallback() {
                            @Override
                            public void onMapReady(@NonNull final MapboxMap mapboxMap) {
                                CameraPosition position = new CameraPosition.Builder()
                                        .zoom(18)
                                        .tilt(0)
                                        .build();
                                mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(position), 3000);
                                mapboxMap.getUiSettings().setTiltGesturesEnabled(false);
                                mapboxMap.getStyle(new Style.OnStyleLoaded() {
                                    @Override
                                    public void onStyleLoaded(@NonNull Style style) {


                                        try {for(int ssz=0;ssz<lev4.size();ssz++){
                                            Layer waterLayer99= style.getLayer("room-extrusion" + (lev4.get(ssz)));
                                            if (waterLayer99 != null) {
                                                waterLayer99.setProperties(fillExtrusionOpacity(1f));}
                                        }

                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }

                                        try{int sszzz = lev4.get(0);
                                            while ((sszzz) < lev4.size()) {
                                                Layer stair4 =style.getLayer("room-extrusion"+sszzz+55);
                                                Layer tolie = style.getLayer("room-extrusion"+sszzz+44);
                                                if (stair4 != null) {
                                                    style.removeLayer(stair4);}
                                                if (tolie != null) {
                                                    style.removeLayer(tolie);}
                                                sszzz++;
                                            }

                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }

                                        try {int sszu = lev4.get(0);
                                            while ((sszu) < lev4.size()) {

                                                Layer waterLayer999= style.getLayer("room-extrusion" +sszu+99);
                                                Source watersource= style.getSource("room-data"+sszu+99);
                                                if (waterLayer999 != null) {
                                                    style.removeLayer(waterLayer999);}
                                                if (watersource!= null) {
                                                    style.removeSource(watersource);}
                                                sszu++;}

                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }

                                        try {
                                            try {
                                                style.addSource(new GeoJsonSource("room-data" + finalJ + 99, new URL(myStringFromseite2+"/"+ String.valueOf(finalJ) + "POI" + "." + "geojson")));
                                            } catch (MalformedURLException e) {
                                                e.printStackTrace();
                                                try{
                                                    style.addSource(new GeoJsonSource("room-data"+finalJ+99, new URI("asset://levelTestt/" + String.valueOf(finalJ) +"POI"+ "." + "geojson")));


                                                } catch (URISyntaxException ex) {
                                                    ex.printStackTrace();
                                                }
                                            }
                                            SymbolLayer label =new SymbolLayer("room-extrusion"+finalJ+99, "room-data"+finalJ+99);
                                            label.setProperties( textField(get("name")),
                                                    textSize(10f),
                                                    textColor(Color.BLUE),
                                                    textVariableAnchor(
                                                            new String[]{TEXT_ANCHOR_CENTER}),
                                                    textJustify(Property.TEXT_JUSTIFY_CENTER),
                                                    textFont(new String[] {"Ubuntu Medium", "Arial Unicode MS Regular"}),
                                                    textMaxAngle(0f),
                                                    textRadialOffset(0f),
                                                    textAnchor(TEXT_ANCHOR_CENTER),
                                                    textTranslateAnchor(Property.TEXT_TRANSLATE_ANCHOR_MAP),
                                                    textKeepUpright(false)
                                                    );
                                            label.withFilter(eq(get("level"),finalJ));
                                            label.setMinZoom(16);

                                            style.addLayerAbove(label,"room-extrusion"+finalJ);

                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }

                                        try{ SymbolLayer stairs =new SymbolLayer("room-extrusion"+finalJ+55, "room-data"+finalJ+99);
                                            Bitmap myImage = BitmapFactory.decodeResource(MainActivity.this.getResources(), R.drawable.str3);
                                            style.addImage("image22",myImage);
                                            stairs.withFilter(eq(get("color"),"red"));
                                            stairs.setProperties(iconImage("image22"),
                                                    iconSize(0.02f)
                                            );
                                            stairs.setMinZoom(16);


                                            style.addLayerAbove(stairs,"room-extrusion"+finalJ+99);
                                            } catch (Exception e) {
                                            e.printStackTrace();
                                        }


                                        try{SymbolLayer toliete =new SymbolLayer("room-extrusion"+finalJ+44, "room-data"+finalJ+99);
                                            Bitmap myImage11 = BitmapFactory.decodeResource(MainActivity.this.getResources(), R.drawable.toilet);
                                            style.addImage("image11",myImage11);
                                            toliete.withFilter(eq(get("color"),"yellow"));
                                            toliete.setProperties(iconImage("image11"),
                                                    iconSize(0.02f),
                                                    iconTranslateAnchor(Property.ICON_TRANSLATE_ANCHOR_MAP),
                                                    iconPitchAlignment(Property.ICON_PITCH_ALIGNMENT_MAP),
                                                    iconAnchor(Property.ICON_ANCHOR_CENTER),
                                                    iconRotationAlignment(Property.ICON_ROTATION_ALIGNMENT_VIEWPORT)

                                            );
                                            toliete.setMinZoom(16);
                                            style.addLayerAbove(toliete,"room-extrusion"+finalJ+99);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }

                                        try{
                                            for(int i=finalJ2;i<lev4.size();i++){
                                                Layer waterLayer= style.getLayer("room-extrusion" + (lev4.get(i+1)));
                                                if (waterLayer != null) {
                                                    waterLayer.setProperties(fillExtrusionOpacity(0.0f));

                                                }
                                            }

                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }

                                        try{
                                            for(int i=finalJ2;i>=0;i--){
                                                Layer waterLayer2= style.getLayer("room-extrusion" + (lev4.get(i-1)));
                                                if (waterLayer2 != null) {
                                                    waterLayer2.setProperties(fillExtrusionOpacity(0.1f));

                                                }
                                            }

                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                } );
                            }
                        });
                    }
                };
                btn.setOnClickListener(onButtonClickListener);
                    } catch(
                    Exception e)
                    {
                        e.printStackTrace();
                    }
            lm.addView(btn);
                }
    }

    @Override
        protected void onResume () {
            super.onResume();
            mapView.onResume();
        }

        @Override
        protected void onStart () {
            super.onStart();
            mapView.onStart();
        }

        @Override
        protected void onStop () {
            super.onStop();
            mapView.onStop();
        }

        @Override
        protected void onPause () {
            super.onPause();
            mapView.onPause();
        }

        @Override
        protected void onDestroy () {
            super.onDestroy();
            mapView.onDestroy();
        }

        @Override
        public void onLowMemory () {
            super.onLowMemory();
            mapView.onLowMemory();
        }

        @Override
        protected void onSaveInstanceState (Bundle outState){
            super.onSaveInstanceState(outState);
            mapView.onSaveInstanceState(outState);
        }
    }

