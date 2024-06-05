package com.RobinNotBad.BiliClient.api;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.RobinNotBad.BiliClient.BiliTerminal;
import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.model.Announcement;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;
import com.RobinNotBad.BiliClient.util.MsgUtil;
import com.RobinNotBad.BiliClient.util.NetWorkUtil;
import com.RobinNotBad.BiliClient.util.SharedPreferencesUtil;

import com.RobinNotBad.BiliClient.util.ToolsUtil;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class AppInfoApi {
    public static void check(Context context){
        try {
            int version = BiliTerminal.getVersion();
            int curr = ConfInfoApi.getDateCurr();

            checkAnnouncement(context);

            if (SharedPreferencesUtil.getInt("app_version_last", 0) < version) {
                MsgUtil.showText(context, "更新公告", context.getResources().getString(R.string.update_tip) + "\n\n更新日志：\n" + ToolsUtil.getUpdateLog(context));
                if(SharedPreferencesUtil.getInt("app_version_last", 0) < 20240606) MsgUtil.showDialog(context,"提醒","当前的新版本实现了对抗部分类型的风控，建议您重新登录账号以确保成功使用");
                SharedPreferencesUtil.putInt("app_version_last", version);
            }

            if(SharedPreferencesUtil.getInt("app_version_check",0) < curr) {    //限制一天一次
                Log.e("debug", "检查更新");
                SharedPreferencesUtil.putInt("app_version_check", curr);

                checkUpdate(context,false);
            }
        }catch (Exception e){
            CenterThreadPool.runOnUiThread(()->MsgUtil.toast(e.getMessage(),context));
        }
    }

    private static final ArrayList<String> customHeaders = new ArrayList<String>(){{
        add("User-Agent");
        add(NetWorkUtil.USER_AGENT_WEB);    //防止携带b站cookies导致可能存在的开发者盗号问题（
    }};

    public static void checkUpdate(Context context, boolean need_toast) throws Exception {
        String url = "http://api.biliterminal.cn/terminal/version/get_last";
        JSONObject result = NetWorkUtil.getJson(url,customHeaders);

        if(result.getInt("code")!=0) throw new Exception(result.getString("msg"));
        JSONObject data = result.getJSONObject("data");

        String version_name = data.getString("version_name");
        String update_log = data.getString("update_log");
        int latest = data.getInt("version_code");

        int version = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
        if(latest>version) MsgUtil.showText(context,version_name,update_log);
        else if(need_toast) CenterThreadPool.runOnUiThread(()->MsgUtil.toast("当前是最新版本！",context));
    }

    public static void checkAnnouncement(Context context) throws Exception {
        String url = "http://api.biliterminal.cn/terminal/announcement/get_last";
        JSONObject result = NetWorkUtil.getJson(url,customHeaders);

        if(result.getInt("code")!=0) throw new Exception("错误："+result.getString("msg"));
        JSONObject data = result.getJSONObject("data");

        int id = data.getInt("id");

        if(SharedPreferencesUtil.getInt("app_announcement_last",0) < id) {
            SharedPreferencesUtil.putInt("app_announcement_last", id);
            String title = data.getString("title");
            String content = data.getString("content");
            MsgUtil.showText(context,title,content);
        }
    }

    public static ArrayList<Announcement> getAnnouncementList() throws Exception {
        String url = "http://api.biliterminal.cn/terminal/announcement/get_list";
        JSONObject result = NetWorkUtil.getJson(url,customHeaders);

        if(result.getInt("code")!=0) throw new Exception("错误："+result.getString("msg"));
        JSONArray data = result.getJSONArray("data");

        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        ArrayList<Announcement> list = new ArrayList<>();
        for (int i = 0; i < data.length(); i++) {
            JSONObject section = data.getJSONObject(i);
            Announcement announcement = new Announcement();
            announcement.id = section.getInt("id");
            announcement.ctime = sdf.format(section.getLong("ctime") * 1000);
            announcement.title = section.getString("title");
            announcement.content = section.getString("content");
            list.add(announcement);
        }
        return list;
    }

    public static String uploadStack(String stack){
        //上传崩溃堆栈
        try {
            String url = "http://api.biliterminal.cn/terminal/upload/stack";

            JSONObject post_data = new JSONObject();
            post_data.put("stack",stack);
            post_data.put("device_sdk_version", Build.VERSION.SDK_INT);
            post_data.put("device_product",Build.PRODUCT);
            post_data.put("device_brand",Build.BRAND);
            
            JSONObject res = new JSONObject(NetWorkUtil.postJson(url,post_data.toString(),customHeaders).body().string());
            if(res.getInt("code") == 200) return "上传成功";
            else return res.getString("msg");
        }catch (Throwable e){
            e.printStackTrace();
            return "上传失败";
        }
    }
}
