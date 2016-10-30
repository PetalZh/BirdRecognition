package xiaoyu.recorder;


import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by Xiaoyu on 9/7/2016.
 */
public class MapFragment extends Fragment  implements OnMapReadyCallback {
    private MapView mapView;
    private GoogleMap googleMap;
    private String markerInfos;
    private String connectionStatus;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.map_fragment, container, false);

        Thread thread = new Thread(new GetMarkers());
        thread.start();

        mapView = (MapView) view.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        //setContentView(R.layout.map_fragment);
        mapView.onResume(); // get map to display immediately
        MapsInitializer.initialize(getActivity().getApplicationContext());

        while(true)
        {
            if(connectionStatus != null)
            {
                if (connectionStatus.equals("Network Error!"))
                {
                    Toast.makeText(getActivity(), "Network Error!", Toast.LENGTH_LONG).show();
                    mapView.getMapAsync(new OnMapReadyCallback() {
                        @Override
                        public void onMapReady(GoogleMap map) {
                            googleMap = map;
                            googleMap.setMyLocationEnabled(true);
                            LatLng Melbourne = new LatLng(-37.812415, 144.962765);
                            googleMap.addMarker(new MarkerOptions().position(Melbourne).title("Marker Title").snippet("Marker Description"));

                            CameraPosition cameraPosition = new CameraPosition.Builder().target(Melbourne).zoom(10).build();
                            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                        }
                    });
                    break;
                }else if(connectionStatus.equals("Request success"))
                {
                    final ArrayList<MarkerInfo> markerList = getMarkerInfo(markerInfos);
                    mapView.getMapAsync(new OnMapReadyCallback() {
                        @Override
                        public void onMapReady(GoogleMap map) {
                            googleMap = map;
                            googleMap.setMyLocationEnabled(true);
                            for(int i = 0; i < markerList.size(); i++)
                            {
                                MarkerInfo marker = markerList.get(i);
                                LatLng point = new LatLng(marker.getLatitude(), marker.getLongitude());
                                //System.out.println("hahaha:" + ma);
                                // .snippet("Marker Description")
                                googleMap.addMarker(new MarkerOptions()
                                                .position(point)
                                                .title(marker.getBirdName())
                                                .alpha(0.7f)
                                                .icon(BitmapDescriptorFactory.fromAsset("b"+marker.getBirdId()+".jpg"))
                                        // (getImage("b"+marker.getBirdId()+".jpg"))
                                );

                            }
                            CameraPosition cameraPosition = new CameraPosition.Builder().target(new LatLng(-37.812415, 144.962765)).zoom(8).build();
                            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                        }
                    });
                    break;
                }
            }

        }


        //mapView.getMapAsync(this);

        return view;
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        //googleMap.setMyLocationEnabled(true);
    }

    public Bitmap getImage(String fileName)
    {
        Bitmap img = null;
        AssetManager am = getActivity().getAssets();
        try {
            InputStream is = am.open(fileName);
            img = BitmapFactory.decodeStream(is);
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return img;
    }

    public ArrayList<MarkerInfo> getMarkerInfo(String markers)
    {
        ArrayList<MarkerInfo> markerInfoList = new ArrayList<MarkerInfo>();
        try {
            JSONParser parser =new JSONParser();
            JSONObject obj = (JSONObject)parser.parse(markers);
            JSONArray markerDict= (JSONArray)obj.get("record_list");

            for(int i =0; i< markerDict.size(); i++)
            {
                JSONObject info = (JSONObject) markerDict.get(i);


                if(info.get("latitude") != null && info.get("longitude") != null && !info.get("latitude").equals("")&& !info.get("longitude").equals(""))
                {
                    MarkerInfo markerInfo = new MarkerInfo();

                    markerInfo.setBirdId(((Long)info.get("top_estimation_code")).intValue());
                    markerInfo.setBirdName((String)info.get("top_estimation_bird"));
                    markerInfo.setLatitude(Double.parseDouble((String) info.get("latitude")));
                    markerInfo.setLongitude(Double.parseDouble((String) info.get("longitude")));

                    markerInfoList.add(markerInfo);
                }

            }

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return markerInfoList;
    }

    class GetMarkers implements Runnable
    {

        public void run() {
            UploadUtil uploadUtil = new UploadUtil();
            markerInfos = uploadUtil.httpGet("http://115.146.90.254:5001/index?view=all");
            //System.out.println("response: "+markerInfos);
            if(markerInfos != null){
                connectionStatus = "Request success";
            }else
            {
                connectionStatus = "Network Error!";

            }

        }
    }
}
