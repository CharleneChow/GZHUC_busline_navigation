package com.example.tabui.frags;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.tabui.entity.GetRoutes;
import com.example.tabui.entity.GetSearchStops;
import com.example.tabui.JsonPlaceHolderApi;
import com.example.tabui.R;

import org.osmdroid.api.IGeoPoint;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class busFrags extends Fragment {
    private TextView textView;
    private JsonPlaceHolderApi jsonPlaceHolderApi;
    public String gloFromStr=null;
    public String gloToStr=null;
    private static List<IGeoPoint> stopXYList=new ArrayList<>();
    private ArrayList listonlynew=new ArrayList<>();
    private ArrayList<Integer> sizeArr=new ArrayList<>();
    private ArrayList<String> a=new ArrayList<>();
    private Intent intent=new Intent();
    private Bundle bundle1 = new Bundle();
    private Bundle bundle2 = new Bundle();


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.search_main,container,false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Button searchBtn = (Button) getActivity().findViewById(R.id.search_btn);
        textView=(TextView) getActivity().findViewById(R.id.show_result_tv);
        AutoCompleteTextView autoFrom = (AutoCompleteTextView)  getActivity().findViewById(R.id.actv_from);
        AutoCompleteTextView autoTo = (AutoCompleteTextView)  getActivity().findViewById(R.id.actv_to);
        String[] stopsName=getResources().getStringArray(R.array.stopsname);
        ArrayAdapter<String> adapterfrom = new ArrayAdapter<String>(this.getActivity(), android.R.layout.simple_list_item_1,stopsName);
        ArrayAdapter<String> adapterto = new ArrayAdapter<String>(this.getActivity(), android.R.layout.simple_list_item_1,stopsName);
        autoFrom.setAdapter(adapterfrom);
        autoTo.setAdapter(adapterto);




        searchBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                String fromStopStr=autoFrom.getText().toString();
                String toStopStr=autoTo.getText().toString();
                gloFromStr=fromStopStr;
                gloToStr=toStopStr;
                textView.setText("从"+gloFromStr+"到"+gloToStr);
                querydb();
                needToTransfer(gloFromStr,gloToStr);
            }
        });
    }

    public void querydb(){
        //数据库查询
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://192.168.0.196:8080/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        jsonPlaceHolderApi = retrofit.create(JsonPlaceHolderApi.class);
    }




    public void needToTransfer(String fromStop, String toStop){
        ArrayList<String> fromStopRoute=new ArrayList<>();
        ArrayList<String> toStopRoute=new ArrayList<>();
        new Thread(new Runnable() {
            @Override
            public void run() {
                Call<List<GetSearchStops>> call =jsonPlaceHolderApi.getSearchStops(fromStop,toStop);//经过此站点的线路
                try {
                    Response<List<GetSearchStops>> response = call.execute();
                    List<GetSearchStops> passRoutes = response.body();
                    assert passRoutes != null;
                    for (GetSearchStops passroute:passRoutes){
                        String currStopName=passroute.getBusname();
                        String currBusline=passroute.getBusline();
                        String currDire=passroute.getBusdire();
                        if(currStopName.equals(fromStop)){
                            fromStopRoute.add(currBusline);//经过fromStop的路线
                        }else if(currStopName.equals(toStop)){
                            toStopRoute.add(currBusline);//经过toStop的路线
                        }
                    }
//                    ArrayList<String> nfromStopRoute=have2Dires(fromStopRoute);
//                    ArrayList<String> ntoStopRoute=have2Dires(toStopRoute);
                    int sumSharedStop=0;//统计fromStopRoute和toStopRoute有多少个共同站点
                    for (int i=0;i<fromStopRoute.size();i++){//fi tj是线路名
                        String fi=fromStopRoute.get(i);
                        for(int j=0;j<toStopRoute.size();j++){
                            String tj=toStopRoute.get(j);
                            if(fi.equals(tj)){
                                sumSharedStop++;
                                listonlynew.add(fi);
                            }
                        }
                    }
                    listonlynew= onlyList((ArrayList) listonlynew);//去除重复线路名
                    ArrayList<String> array=removeHX(listonlynew);
                    if(sumSharedStop==0){//当没有直达的
                        ArrayList newfromStopRoute=onlyList((ArrayList) fromStopRoute);
                        ArrayList<String> arrayfrom=removeHX(newfromStopRoute);
                        ArrayList newtoStopRoute=onlyList((ArrayList) toStopRoute);
                        ArrayList<String> arrayto=removeHX(newtoStopRoute);
                        transferOnce(arrayfrom,arrayto);
                    }else{//有直达线
                        //判断最短线路
                        findShortest(array);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }).start();
    }

    public ArrayList<String> removeHX(ArrayList<String> list){
        ArrayList<String> array=new ArrayList<>();
        for (String i:list){
            if (!i.equals("大学城环线1")&&!i.equals("大学城环线2")){
                array.add(i);
            }
        }
        return array;
    }




    public void transferOnce(ArrayList<String> fromRoute,ArrayList<String> toRoute){//传入参数：经过起点和终点的所有线路
        //遍历“同站线路”表，找到某站包含两个线路表表中的任一元素
        new Thread(new Runnable() {
            @Override
            public void run() {
                Call<List<GetRoutes>> callStopsInBusline =null;
                Response<List<GetRoutes>> responseStopsInBusline = null;

                Call<List<GetSearchStops>> callByBusStop =null;
                Response<List<GetSearchStops>> responseByBusStop = null;
                found:
                for (int i =0;i<fromRoute.size();i++) {
                    String fromRouteListEle = fromRoute.get(i);//得到fromRoute的元素
                    callStopsInBusline = jsonPlaceHolderApi.getByBrName(fromRouteListEle);//查询某线路所有站点
                    try {
                        responseStopsInBusline = callStopsInBusline.execute();
                        List<GetRoutes> StopsInR = responseStopsInBusline.body();
                        assert StopsInR != null;
                        for (GetRoutes stop:StopsInR){
                            String currStopName=stop.getStopName();
                            callByBusStop = jsonPlaceHolderApi.getSameStopBySingleBusName(currStopName);//查询同站所有线路
                            responseByBusStop = callByBusStop.execute();
                            List<GetSearchStops> RoutesSinBN = responseByBusStop.body();
                            assert RoutesSinBN != null;
                            for(GetSearchStops routePassStop:RoutesSinBN){//查询经过这个站点的所有线路，和经过目的站点的线路进行匹配
                                String hasbusLine=routePassStop.getBusline();
                                String stationName=routePassStop.getBusname();
                                for (int j=0;j<toRoute.size();j++){
                                    String toRouteListEle = toRoute.get(j);//得到fromRoute的元素
                                    if (hasbusLine.equals(toRouteListEle)){
                                        passtoMain(fromRouteListEle,toRouteListEle,stationName);
                                        break found;
                                    }else{
                                        String y="通过一次换乘也不能到达";
                                    }
                                }
                            }

                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }


    public void passtoMain(String fromRouteListEle,String toRouteListEle,String stationName){
        //定义数据
        String fromRN=fromRouteListEle;
        String toRN=toRouteListEle;
        //把数据保存到Bundle里
        bundle2.putString("from_route_name",fromRN);
        bundle2.putString("to_route_name", toRN);
        bundle2.putString("from_stop_name",gloFromStr);
        bundle2.putString("transfer_stop_name",stationName);
        bundle2.putString("to_stop_name",gloToStr);
        //把bundle放入intent里
        intent.putExtra("bundle2",bundle2);
        getActivity().setResult(4, intent);
        getActivity().finish();
    }


    public void testToStoreTrans(String stationName){
        String t=stationName;
    }

    public void findShortest(ArrayList<String> list) throws IOException {
        int numsOfRoute=list.size();
        int[]lengths=new int[numsOfRoute];
        for (int i=0;i<numsOfRoute;i++){
            String currRoute=list.get(i);
            Call<List<GetRoutes>> call=jsonPlaceHolderApi.getByBrName(currRoute);
            Response<List<GetRoutes>> response = call.execute();
            List<GetRoutes> apiResponse = response.body();
            assert apiResponse != null;
            int currRouteLength=apiResponse.size();
            lengths[i]=currRouteLength;
        }
        int min=lengths[0];//将数组的第一个元素赋给min
        int minIndex=0;
        for(int i=1;i<lengths.length;i++){//从数组的第二个元素开始赋值，依次比较
            if(lengths[i]<min){//如果arr[i]小于最小值，就将arr[i]赋给最小值
                min=lengths[i];
                minIndex=i;
            }
        }
        String shortestRouteName=list.get(minIndex);
        bundle1.putString("shortest_route_name",shortestRouteName);
        bundle1.putString("shortest_from_name",gloFromStr);
        bundle1.putString("shortest_to_name",gloToStr);
        intent.putExtra("bundle1", bundle1);
        getActivity().setResult(3, intent);
        getActivity().finish();
    }



    public static ArrayList onlyList(ArrayList list){//去除存储路线的Arraylist中的存储元素
        ArrayList<String> list1 = new ArrayList<>();
        Iterator it = list.iterator();
        while (it.hasNext()){
            String s = (String) it.next();
            if (!list1.contains(s)){    //判断list1中是否包括该元素
                list1.add(s);
            }
        }
        return list1;
    }

}
