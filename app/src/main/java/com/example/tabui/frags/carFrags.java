package com.example.tabui.frags;

import android.os.Bundle;
import android.text.GetChars;
import android.util.Log;
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

import com.example.tabui.JsonPlaceHolderApi;
import com.example.tabui.MinShortPath;
import com.example.tabui.R;
import com.example.tabui.Side;
import com.example.tabui.entity.GetSideRoad;
import com.example.tabui.entity.GetSideroadConnect;

import org.osmdroid.util.GeoPoint;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class carFrags extends Fragment {
    private JsonPlaceHolderApi jsonPlaceHolderApi;
    public static int nodeTotalNum=1147;
    double[][] array= new double[nodeTotalNum][nodeTotalNum];
    ArrayList<Side> map = new ArrayList<>();
    // 初始化已知最短路径的顶点集，即红点集，只加入顶点0
    ArrayList<Integer> redAgg = new ArrayList<>();
    // 初始化未知最短路径的顶点集，即蓝点集
    ArrayList<Integer> blueAgg = new ArrayList<>();

    Side[] parents = null;

    String fromStop,toStop=null;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.shortest_main,container,false);
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Button searchBtnCar = (Button) getActivity().findViewById(R.id.search_btn_car);
        TextView textViewCar=(TextView) getActivity().findViewById(R.id.show_result_tv_car);
        AutoCompleteTextView autoFromCar = (AutoCompleteTextView)  getActivity().findViewById(R.id.actv_from_car);
        AutoCompleteTextView autoToCar = (AutoCompleteTextView)  getActivity().findViewById(R.id.actv_to_car);
        String[] stopsName=getResources().getStringArray(R.array.stopsname);
        ArrayAdapter<String> adapterfrom = new ArrayAdapter<String>(this.getActivity(), android.R.layout.simple_list_item_1,stopsName);
        ArrayAdapter<String> adapterto = new ArrayAdapter<String>(this.getActivity(), android.R.layout.simple_list_item_1,stopsName);
        autoFromCar.setAdapter(adapterfrom);
        autoToCar.setAdapter(adapterto);

        searchBtnCar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fromStopStr=autoFromCar.getText().toString();
                String toStopStr=autoToCar.getText().toString();
                textViewCar.setText("从"+fromStopStr+"到"+toStopStr);
                fromStop=fromStopStr;
                toStop=toStopStr;
                querydb();
                redAgg.add(Integer.parseInt(fromStopStr));//保存输入的from node id
                buildGraph();
                addToBlueAgg(fromStopStr);
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


    public void buildGraph(){//构建矩阵
        new Thread(new Runnable() {
            @Override
            public void run() {
                Call<List<GetSideRoad>> call =jsonPlaceHolderApi.getAllRoad();//获得所有路段
                try {
                    Response<List<GetSideRoad>> response = call.execute();
                    List<GetSideRoad> allRoads = response.body();
                    //获得点的数量N，构建N*N矩阵
                    for (GetSideRoad road:allRoads) {
                        //将路段权重填进表中
                        double roadW=Double.parseDouble(road.getWeight());
                        int arrayx=road.getFnodeID()-1;
                        int arrayy=road.getTnodeID()-1;
                        array[arrayx][arrayy]=roadW;
                    }


                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        for (int i = 0; i < nodeTotalNum; i++) {
            for (int j = 0; j < nodeTotalNum; j++) {
                if (array[i][j] == 0) {
                    array[i][j] = -1;
                }
            }
        }
        int fromid=Integer.parseInt(fromStop);
        // 初始化已知最短路径的顶点集，即红点集，只加入顶点0
        redAgg = new ArrayList<Integer>();
        redAgg.add(fromid);

        // 初始化未知最短路径的顶点集，即蓝点集
        ArrayList<Integer> blueAggsPart=setUpBlue();
        blueAgg=blueAggsPart;
        // 初始化每个顶点在最短路径中的父节点，及它们之间的权重，权重-1表示不连通
        parents = new Side[65];
        parents[0] = new Side(-1, fromid, 0);
        for (int i = 0; i < blueAgg.size(); i++) {
            int n = blueAgg.get(i);
            parents[i + 1] = new Side(fromid, n, getWeight(fromid, n));
        }

        // 从蓝点集中找出权重最小的那个顶点，并把它加入到红点集中
        while (blueAgg.size() > 0) {
            MinShortPath msp = getMinSideNode();
//            if (msp.getWeight() == -1) {
//                msp.outputPath(nodes[0]);
//            } else {
//                msp.outputPath();
//            }
            int node = msp.getLastNode();
            redAgg.add(node);
            // 如果因为加入了新的顶点，而导致蓝点集中的顶点的最短路径减小，则要重新设置
            setWeight(node);
        }
    }


    public ArrayList<Integer> setUpBlue(){
        ArrayList<Integer> blue = new ArrayList<>();
        new Thread(new Runnable() {
            @Override
            public void run() {
                Call<List<GetSideroadConnect>> call=jsonPlaceHolderApi.getConnect();
                Response<List<GetSideroadConnect>> res= null;
                try {
                    res = call.execute();
                    List<GetSideroadConnect> body=res.body();
                    for (GetSideroadConnect i:body){
                        int nodeid=i.getNodeID();
                        if (nodeid!=Integer.parseInt(fromStop)){
                            blue.add(nodeid);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        return blue;
    }

    public void addToBlueAgg(String inputNodeId){
        new Thread(new Runnable() {
            @Override
            public void run() {
                Call<List<GetSideroadConnect>> call=jsonPlaceHolderApi.getConnect();
                try {
                    Response<List<GetSideroadConnect>> response = call.execute();
                    List<GetSideroadConnect> allConnects = response.body();
                    for(GetSideroadConnect conn:allConnects){
                        if(conn.getNodeID()!=Integer.parseInt(inputNodeId)){
                            blueAgg.add(conn.getNodeID());
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }).start();
    }

    /*
     * 得到两点之间的权重
     */
    public double getWeight(int preNode, int nextNode) {
        final double[] reDou = {0.0};
        new Thread(new Runnable() {
            @Override
            public void run() {
                Call<List<GetSideRoad>> call=jsonPlaceHolderApi.getftID(preNode,nextNode);
                Response<List<GetSideRoad>> response= null;
                try {
                    response = call.execute();
                    List<GetSideRoad> body=response.body();
                    if (body != null) {
                        String weight=body.get(0).getWeight();
                        double weightDou=Double.parseDouble(weight);
                        reDou[0] =weightDou;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        if (reDou[0]==0){
            return -1;
        }else{
            return reDou[0];
        }
    }


    /*
     * 从蓝点集合中找出路径最小的节点
     */
    public MinShortPath getMinSideNode() {
        MinShortPath minMsp = null;
        if (blueAgg.size() > 0) {
            int index = 0;
            for (int j = 0; j < blueAgg.size(); j++) {
                MinShortPath msp = getMinPath(blueAgg.get(j));
                if (minMsp == null || msp.getWeight() != -1
                        && msp.getWeight() < minMsp.getWeight()) {
                    minMsp = msp;
                    index = j;
                }
            }
            blueAgg.remove(index);
        }
        return minMsp;
    }

    /*
     * 得到某一节点的最短路径（实际有多条，现在只考虑一条）
     */
    public MinShortPath getMinPath(int node) {
        MinShortPath msp = new MinShortPath(node);
        if (parents != null && redAgg != null) {
            for (int i = 0; i < redAgg.size(); i++) {
                MinShortPath tempMsp = new MinShortPath(node);
                int parent = redAgg.get(i);
                int curNode = node;
                while (parent > -1) {
                    double weight = getWeight(parent, curNode);
                    if (weight > -1) {
                        tempMsp.addNode(parent);
                        tempMsp.addWeight(weight);
                        curNode = parent;
                        parent = getParent(parents, parent);
                    } else {
                        break;
                    }
                }
                if (msp.getWeight() == -1 || tempMsp.getWeight() != -1
                        && msp.getWeight() > tempMsp.getWeight()) {
                    msp = tempMsp;
                }
            }
        }
        return msp;
    }

    /*
     * 得到一个节点的父节点
     */
    public int getParent(Side[] parents, int node) {
        if (parents != null) {
            for (Side nd : parents) {
                if (nd.getNextNode() == node) {
                    return nd.getPreNode();
                }
            }
        }
        return -1;
    }

    /*
     * 重新设置蓝点集中剩余节点的最短路径长度
     */

    public void setWeight(int preNode) {
        if (map != null && parents != null && blueAgg != null) {
            for (int node : blueAgg) {
                MinShortPath msp = getMinPath(node);
                double w1 = msp.getWeight();
                if (w1 == -1) {
                    continue;
                }
                for (Side n : parents) {
                    if (n.getNextNode() == node) {
                        if (n.getWeight() == -1 || n.getWeight() > w1) {
                            n.setWeight(w1);
                            n.setPreNode(preNode);
                            break;
                        }
                    }
                }
            }
        }
    }
}
