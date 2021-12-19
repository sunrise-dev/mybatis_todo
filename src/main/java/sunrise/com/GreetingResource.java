package sunrise.com;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.jboss.resteasy.annotations.jaxrs.PathParam;

import javax.ws.rs.core.MediaType;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import sunrise.Util.DataCenter;
import sunrise.Util.SearchRule;

@Path("/hello")
public class GreetingResource {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
        return "Hello RESTEasy";
    }


    @GET
    @Path("/request/{data}")
    @Produces("application/json")
    public String GetData(@PathParam String request) {
        
        //request,前端Json参数

        //datas
        // {
        //     "route": "Rack/GetRacks",
        //     "orderby": "id asc",
        //     "SearchRule": [
        //         {
        //             "field": "line",
        //             "value": "2"
        //         },
        //         {
        //             "field": "momno",
        //             "value": "135246",
        //             "op": "="
        //         },
        //         {
        //             "field": "pos",
        //             "value": "1,6",
        //             "op": "range"
        //         ],
        //     "pageNo": 1,    //页码
        //     "pageSize": 10  //页行数
        // }


        JSONObject jsonDatas = JSON.parseObject(request).getJSONObject("datas");

        String route  = String.valueOf(jsonDatas.get("route"));
        String orderby = String.valueOf(jsonDatas.get("orderby"));
        int pageNo = Integer.parseInt(jsonDatas.get("pageNo").toString());
        int pageSize = Integer.parseInt(jsonDatas.get("pageSize").toString());
        List<SearchRule> rules = JSONObject.parseArray(jsonDatas.get("rules").toString(), SearchRule.class);
        
        return DataCenter.QueryPageData(route, orderby, pageNo, pageSize, rules);

        //datas
        // {
        //     "msg": "数据获取成功！",
        //     "code": 0,
        //     "data": [
        //         {
        //             "momno": "135246",
        //             "id": 20001,
        //             "line"": 2,
        //             "pos": 2
        //         },
        //         {
        //             "momno": "135246",
        //             "id": 20003,
        //             "line"": 2,
        //             "pos": 6
        //         }
        //         ],
        //     "count": 1,
        // }

    }

    @POST
    @Path("/request/{data}")
    @Produces("application/json")
    public String SaveData(@PathParam String request) {
        
        //request,前端Json参数

        // {
        //     "route": "Rack/UpdateRack",
        //     "SearchRule": [
        //         {
        //             "field": "id",
        //             "value": "20001"
        //         },
        //         {
        //             "field": "pos",
        //             "value": "6"
        //         ]
        // }


        JSONObject jsonDatas = JSON.parseObject(request).getJSONObject("datas");

        String route  = String.valueOf(jsonDatas.get("route"));
        List<SearchRule> rules = JSONObject.parseArray(jsonDatas.get("rules").toString(), SearchRule.class);

        return DataCenter.SaveData(route, rules);
    }

}