package com.example.tabui;

import com.example.tabui.entity.GetDisplayRoutes;
import com.example.tabui.entity.GetRoutes;
import com.example.tabui.entity.GetSearchStops;
import com.example.tabui.entity.GetShortestRoute;
import com.example.tabui.entity.GetSideRoad;
import com.example.tabui.entity.GetSideroadConnect;
import com.example.tabui.entity.GetStopNumOnly;
import com.example.tabui.entity.GetStopXY;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;



//    retrofit will auto generate codes for this method.
//    similar to MVC model.
public interface JsonPlaceHolderApi {
    //busline1表
    @GET("getByBrName")
    Call<List<GetRoutes>> getByBrName(@Query("brName") String brName);

    @GET("getByStopName")
    Call<List<GetRoutes>> getByStopName(@Query("stopName") String stopName);


    //displayRoute表
    @GET("displayRoutes")
    Call<List<GetDisplayRoutes>> getDisplayBy2Param(@Query("line") String line, @Query("dire") String dire);

    @GET("displayByRoutesName")
    Call<List<GetDisplayRoutes>> getDisplayByAParam(@Query("busLine") String busLine);

    @GET("displayByID")
    Call<List<GetDisplayRoutes>> getDisplayByID(@Query("id") Integer id);

    //汽车行驶最短路径表
    @GET("getShortestRoute")
    Call<List<GetShortestRoute>> getStartCross(@Query("from") String from);

    @GET("getShortestRoute")
    Call<List<GetShortestRoute>> getEndCross(@Query("to") String to);

    @GET("getShortestRoute/all")
    Call<List<GetShortestRoute>> getAllCross();

    @GET("getSideRoads")
    Call<List<GetSideRoad>> getSingleRoad(@Query("edgeid") String edgeid);


    @GET("getSideRoads")
    Call<List<GetSideRoad>> getftID(@Query("fnodeID") Integer fnodeID,@Query("tnodeID") Integer tnodeID);


    @GET("getSideRoads/all")
    Call<List<GetSideRoad>> getAllRoad();

    @GET("getNode/all")
    Call<List<GetSideroadConnect>> getConnect();


    //公交最短路径表
    @GET("searchAll")
    Call <List<GetSearchStops>> getAllSearchStops();

    @GET("search")
    Call <List<GetSearchStops>> getSearchStops(@Query("startStop") String startStop, @Query("endStop") String endStop);//"内部"和String 内部,要保持一致

    @GET("searchByBusLine")
    Call <List<GetSearchStops>> getSameStopByBusline(@Query("busline") String busline);

    @GET("searchBySingleBusName")
    Call <List<GetSearchStops>> getSameStopBySingleBusName(@Query("singlename") String singlename);

    @GET("truncateByStopName")
    Call <List<GetStopNumOnly>> findBystationNames(@Query("stopName") String stopName, @Query("brname") String brname);
}
