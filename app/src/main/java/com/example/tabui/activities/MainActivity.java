package com.example.tabui.activities;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ThemedSpinnerAdapter;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.tabui.JsonPlaceHolderApi;
import com.example.tabui.ObjRouteNid;
import com.example.tabui.R;
import com.example.tabui.entity.GetDisplayRoutes;
import com.example.tabui.entity.GetRoutes;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.infowindow.BasicInfoWindow;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class MainActivity extends AppCompatActivity {
    private JsonPlaceHolderApi jsonPlaceHolderApi;
    private ArrayList<GetRoutes> gpListzheng=new ArrayList<>();
    private ArrayList<GetRoutes> gpListni=new ArrayList<>();
    private ArrayList<GetDisplayRoutes> storeSameName=new ArrayList<>();

    private Polyline pl=new Polyline();
    private MapView mapview;
    private EditText editText;
    private Button buttonclear;
    private Polyline busstops;
    private Intent intent;

    private Marker startMarker;
    private Marker endMarker;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mapview=findViewById(R.id.map_view);
        editText=findViewById(R.id.myEditText);
        Toolbar toolbarFrom =findViewById(R.id.toolbarFrom);
        setSupportActionBar(toolbarFrom);
        querydb();
        showOfflineTile(mapview);
        editText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intent=new Intent(MainActivity.this, ChooseActivity.class);
                startActivityForResult(intent,1);
                mapview.getOverlays().remove(startMarker);
                mapview.getOverlays().remove(endMarker);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == 3) {
            //不用换乘
            //从intent取出bundle
            Bundle bundle1 = data.getBundleExtra("bundle1");
            String result = bundle1.getString("shortest_route_name");
            String resultfrom = bundle1.getString("shortest_from_name");
            String resultto = bundle1.getString("shortest_to_name");
            getRouteByBusLineName(result);//将一条线分开正逆方向
            String definedDire=getStopNum(resultfrom,resultto);//确定正逆方向
            ArrayList<GeoPoint> displayList=getDisplayRoutesByDireAndBrName(result,definedDire,resultfrom,resultto);//确定要显示哪一段
            //showStartMarker(displayList);
            //showEndMarker(displayList);
            showRoutesPolyline(displayList);
        }
        if (requestCode == 1 && resultCode == 4) {
            //一次换乘
            //从intent取出bundle
            Bundle bundle2 = data.getBundleExtra("bundle2");
            //获取数据
            String fromRouteName = bundle2.getString("from_route_name");
            String toRouteName = bundle2.getString("to_route_name");
            String transferStop=bundle2.getString("transfer_stop_name");
            String fromStopName=bundle2.getString("from_stop_name");
            String toStopName=bundle2.getString("to_stop_name");
            getRouteByBusLineName(fromRouteName);//将两条线分开正反方向
            getRouteByBusLineName(toRouteName);
            String definedNo1Dire=getStopNum(fromStopName,transferStop);
            String definedNo2Dire=getStopNum(transferStop,toStopName);
            ArrayList<GeoPoint> displayList=getDisplayRoutesByDireAndBrName(fromRouteName,definedNo1Dire,fromStopName,transferStop);
            ArrayList<GeoPoint> displayList2=getDisplayRoutesByDireAndBrNameSecRoute(toRouteName,definedNo2Dire,transferStop,toStopName,displayList);
            showRoutesPolyline(displayList2);
        }

    }

    public void querydb(){
        //数据库查询
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://192.168.0.196:8080/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        jsonPlaceHolderApi = retrofit.create(JsonPlaceHolderApi.class);
    }

    public void showOfflineTile(MapView mapview){
        //离线地图
        mapview.setTileSource(new XYTileSource("cite_HEMC",1, 4, 256,".png", new String[] {}));
        mapview.setMultiTouchControls(true);
        mapview.setMinZoomLevel(3.2);
        mapview.setVerticalMapRepetitionEnabled(false);
        mapview.setHorizontalMapRepetitionEnabled(false);
        IMapController mapController = mapview.getController();
        IGeoPoint mapcenter=mapview.getMapCenter();
        GeoPoint centerpoint=new GeoPoint(52625,23636);
        transXYtoGeo(centerpoint);
        mapController.setCenter(centerpoint);
        mapController.animateTo(centerpoint);
        mapController.setZoom(3.2);

    }

    public static GeoPoint transXYtoGeo(GeoPoint geoPoint){
        double webMer=40075016.6855784;
        double mer=58622.306112-46628.22966;
        double bilixishu=webMer/mer;
        double Y = (geoPoint.getLongitude()-23636)*bilixishu/20037508.34*180;
        double X = (geoPoint.getLatitude()-52625)*bilixishu/20037508.34*180;
        double Ylon= 180/Math.PI*(2*Math.atan(Math.exp(Y*Math.PI/180))-Math.PI/2);
        geoPoint.setLongitude(X);
        geoPoint.setLatitude(Ylon);
        return geoPoint;
    }



    public void getRouteByBusLineName(String inputBusLineName){//当直达：获得起终点同在的线路以及方向
        new Thread(new Runnable() {
            @Override
            public void run() {
                Call<List<GetRoutes>> call=jsonPlaceHolderApi.getByBrName(inputBusLineName);
                try {
                    Response<List<GetRoutes>> response=call.execute();
                    List<GetRoutes> displayRoutes=response.body();
                    if(displayRoutes != null){
                        for (GetRoutes testsingle:displayRoutes){
                            String dire=testsingle.getStopDire();
                            if(dire.equals("正")){
                                gpListzheng.add(testsingle);
                            }else{
                                gpListni.add(testsingle);
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    public String getStopNum(String from,String to){//输入站名，判断方向
        String dire=null;
        String fromid,toid=null;
        int fromNumInt=0;
        int toNumInt=0;
        for (GetRoutes i:gpListzheng){
            if (i.getStopName().equals(from)){
                fromid=i.getStopNum();
                fromNumInt=Integer.parseInt(fromid);
            }
            if (i.getStopName().equals(to)){
                toid=i.getStopNum();
                toNumInt=Integer.parseInt(toid);
            }
        }
        if (fromNumInt<toNumInt){
            dire="正";
        }else{
            dire="逆";
        }
        return dire;
    }

    public void showStartMarker(ArrayList<GeoPoint> list){
        Drawable fromMarkericon = getResources().getDrawable(R.drawable.ic_topin);
        Marker startMarker = new Marker(mapview);
        GeoPoint startPoint=list.get(0);
        startMarker.setPosition(startPoint);
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        startMarker.setIcon(fromMarkericon);
        mapview.getOverlays().add(startMarker);
    }

    public void showEndMarker(ArrayList<GeoPoint> list){
        Drawable toMarkericon = getResources().getDrawable(R.drawable.ic_frompin);
        Marker endMarker=new Marker(mapview);
        int last=list.size()-1;
        GeoPoint endPoint=list.get(last);
        endMarker.setPosition(endPoint);
        endMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        endMarker.setIcon(toMarkericon);
        mapview.getOverlays().add(endMarker);
    }


    private ArrayList<GeoPoint> getDisplayRoutesByDireAndBrName(String inputString,String dire,String fromStop,String toStop) {//已有线路和方向
        final int[] fromID = new int[1];
        final int[] toID = new int[1];
        ArrayList<GeoPoint> displayList=new ArrayList<>();
        new Thread(new Runnable() {
            @Override
            public void run() {
                Call<List<GetDisplayRoutes>> calld=jsonPlaceHolderApi.getDisplayBy2Param(inputString,dire);
                try {
                    Response<List<GetDisplayRoutes>> r=calld.execute();
                    List<GetDisplayRoutes> bodys=r.body();
                    for (GetDisplayRoutes i:bodys){
                        String name=i.getBusSName();
                        int id=i.getID();
                        if (name.equals(fromStop)){
                            fromID[0] =id;
                            break;
                        }
                    }
                    for (GetDisplayRoutes i:bodys){
                        String name=i.getBusSName();
                        int id=i.getID();
                        if(name.equals(toStop)){
                            toID[0]=id;
                            break;
                        }
                    }
                    int Fromid=fromID[0];
                    int Toid=toID[0];
                    for (GetDisplayRoutes i:bodys){
                        int id=i.getID();
                        String pointE_LAT=i.getBusPointE();
                        String pointN_LON=i.getBusPointN();
                        Double Dlat=Double.parseDouble(pointE_LAT);
                        Double Dlon=Double.parseDouble(pointN_LON);
                        GeoPoint displaypoint=new GeoPoint(Dlat,Dlon);
                        displaypoint=transXYtoGeo(displaypoint);
                        if (id>=Fromid&&id<=Toid){
                            displayList.add(displaypoint);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        return displayList;
    }


    public ArrayList<GeoPoint> getDisplayRoutesByDireAndBrNameSecRoute( String route2, String dire2, String transStop, String toStop,ArrayList<GeoPoint> displaylist){
        final int[] fromID = new int[1];
        final int[] toID = new int[1];
        new Thread(new Runnable() {
            @Override
            public void run() {
                Call<List<GetDisplayRoutes>> call2=jsonPlaceHolderApi.getDisplayBy2Param(route2,dire2);
                Response<List<GetDisplayRoutes>> c2= null;
                try {
                    c2 = call2.execute();
                    List<GetDisplayRoutes> bodys=c2.body();
                    for (GetDisplayRoutes i:bodys){
                        String name=i.getBusSName();
                        int id=i.getID();
                        if (name.equals(transStop)){
                            fromID[0] =id;
                            break;
                        }
                    }
                    for (GetDisplayRoutes i:bodys){
                        String name=i.getBusSName();
                        int id=i.getID();
                        if(name.equals(toStop)){
                            toID[0]=id;
                            break;
                        }
                    }
                    int Fromid=fromID[0];
                    int Toid=toID[0];
                    for (GetDisplayRoutes i:bodys){
                        int id=i.getID();
                        String pointE_LAT=i.getBusPointE();
                        String pointN_LON=i.getBusPointN();
                        Double Dlat=Double.parseDouble(pointE_LAT);
                        Double Dlon=Double.parseDouble(pointN_LON);
                        GeoPoint displaypoint=new GeoPoint(Dlat,Dlon);
                        displaypoint=transXYtoGeo(displaypoint);
                        if (id>=Fromid&&id<=Toid){
                            displaylist.add(displaypoint);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        return displaylist;
    }


    public void showRoutesPolyline(ArrayList<GeoPoint> inputGPpoints){
        pl.setPoints(inputGPpoints);
        //设置线宽度
        pl.getOutlinePaint().setStrokeWidth(7);
        //设置线的颜色
        pl.getOutlinePaint().setColor(Color.rgb(58,170,223));
        pl.setInfoWindow(new BasicInfoWindow(R.layout.bonuspack_bubble,mapview));
        //pl.setTitle();
        mapview.getOverlayManager().add(pl);

        Drawable fromMarkericon = getResources().getDrawable(R.drawable.ic_topin);
        startMarker = new Marker(mapview);
        GeoPoint startPoint=inputGPpoints.get(0);
        startMarker.setPosition(startPoint);
        startMarker.setInfoWindow(new BasicInfoWindow(R.layout.bonuspack_bubble,mapview));
        startMarker.setTitle("出发站点");
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        startMarker.setIcon(fromMarkericon);
        mapview.getOverlays().add(startMarker);

        Drawable toMarkericon = getResources().getDrawable(R.drawable.ic_frompin);
        endMarker=new Marker(mapview);
        int last=inputGPpoints.size()-1;
        GeoPoint endPoint=inputGPpoints.get(last);
        endMarker.setPosition(endPoint);
        endMarker.setTitle("抵达站点");
        endMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        endMarker.setIcon(toMarkericon);
        mapview.getOverlays().add(endMarker);
    }




}
