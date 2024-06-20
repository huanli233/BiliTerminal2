package com.RobinNotBad.BiliClient.api;

import android.text.TextUtils;

import com.RobinNotBad.BiliClient.model.LivePlayInfo;
import com.RobinNotBad.BiliClient.model.LiveRoom;
import com.RobinNotBad.BiliClient.util.NetWorkUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LiveApi {
    public static List<LiveRoom> getRecommend(int page) throws JSONException, IOException {
        String url = "https://api.live.bilibili.com/xlive/web-interface/v1/second/getUserRecommend" + new NetWorkUtil.FormData().setUrlParam(true)
                .put("page", page)
                .put("page_size", 10)
                .put("platform", "web");
        JSONObject result = NetWorkUtil.getJson(url);
        if (result.getInt("code") != 0) throw new JSONException(result.getString("message"));

        JSONObject data = result.optJSONObject("data");
        if (data != null) {
            JSONArray list = data.optJSONArray("list");
            if (list != null) {
                return analyzeLiveRooms(list);
            }
        }
        return null;
    }

    public static List<LiveRoom> getFollowed(int page) throws JSONException, IOException {
        String url = "https://api.live.bilibili.com/xlive/web-ucenter/v1/xfetter/GetWebList" + new NetWorkUtil.FormData().setUrlParam(true)
                .put("page", page)
                .put("page_size", 10);
        JSONObject result = NetWorkUtil.getJson(url);
        if (result.getInt("code") != 0) throw new JSONException(result.getString("message"));

        JSONObject data = result.optJSONObject("data");
        if (data != null) {
            JSONArray list = data.optJSONArray("rooms");
            if (list != null) {
                return analyzeLiveRooms(list);
            }
        }
        return null;
    }

    public static LivePlayInfo getRoomPlayInfo(long roomId, int qn) throws JSONException, IOException {
        String url = "https://api.live.bilibili.com/xlive/web-ucenter/v1/xfetter/GetWebList" + new NetWorkUtil.FormData().setUrlParam(true)
                .put("room_id", roomId)
                .put("qn", qn)
                .put("protocol", "0,1")
                .put("format", "0,1,2")
                .put("codec", "0,1,2")
                .put("platform", "web")
                .put("ptype", 8)
                .put("dolby", 5)
                .put("panorama", 1);
        JSONObject result = NetWorkUtil.getJson(url);
        if (result.getInt("code") != 0) throw new JSONException(result.getString("message"));

        JSONObject data = result.optJSONObject("data");
        if (data != null) {
            LivePlayInfo livePlayInfo = new LivePlayInfo();
            livePlayInfo.roomid = data.getLong("room_id");
            livePlayInfo.short_id = data.optLong("short_id", -1);
            livePlayInfo.uid = data.getLong("uid");
            livePlayInfo.isHidden = data.optBoolean("is_hidden", false);
            livePlayInfo.isLocked = data.optBoolean("is_locked", false);
            livePlayInfo.isPortrait = data.optBoolean("is_portrait", false);
            livePlayInfo.live_status = data.optInt("live_status", -1);
            livePlayInfo.encrypted = data.optBoolean("encrypted", false);
            livePlayInfo.pwd_verified = data.optBoolean("pwd_verified", false);
            livePlayInfo.live_time = data.optLong("live_time", -1);
            JSONObject playurl_info = data.optJSONObject("playurl_info");
            if (playurl_info != null) {
                livePlayInfo.conf_json = playurl_info.optString("conf_json");
                JSONObject play_url = playurl_info.optJSONObject("play_url");
                if (play_url != null) {
                    LivePlayInfo.PlayUrl playUrl = new LivePlayInfo.PlayUrl();
                    playUrl.cid = play_url.optLong("cid", -1);
                    JSONArray g_qn_descs = play_url.optJSONArray("g_qn_desc");
                    if (g_qn_descs != null) {
                        List<LivePlayInfo.QnDesc> qnDescs = new ArrayList<>();
                        for (int i = 0; i < g_qn_descs.length(); i++) {
                            JSONObject jsonObject = g_qn_descs.getJSONObject(i);
                            LivePlayInfo.QnDesc qnDesc = new LivePlayInfo.QnDesc();
                            qnDesc.qn = jsonObject.optInt("qn", -1);
                            qnDesc.desc = jsonObject.optString("desc");
                            qnDesc.hdr_desc = jsonObject.optString("hdr_desc");
                            qnDesc.attr_desc = jsonObject.optString("attr_desc");
                            qnDescs.add(qnDesc);
                        }
                        playUrl.g_qn_desc = qnDescs;
                    }
                    JSONArray streams = play_url.optJSONArray("stream");
                    if (streams != null) {
                        List<LivePlayInfo.ProtocolInfo> protocolInfos = new ArrayList<>();
                        for (int i = 0; i < streams.length(); i++) {
                            JSONObject jsonObject = streams.getJSONObject(i);
                            LivePlayInfo.ProtocolInfo protocolInfo = new LivePlayInfo.ProtocolInfo();
                            // TODO protocalInfo analyze
                            protocolInfos.add(protocolInfo);
                        }
                        playUrl.stream = protocolInfos;
                    }
                    JSONObject p2p_data = play_url.optJSONObject("p2p_data");
                    if (p2p_data != null) {
                        LivePlayInfo.P2PData p2PData = new LivePlayInfo.P2PData();
                        p2PData.p2p = p2p_data.optBoolean("p2p");
                        p2PData.p2p_type = p2p_data.optInt("p2p_type", -1);
                        p2PData.m_p2p = p2p_data.optBoolean("m_p2p");
                        JSONArray m_servers = p2p_data.optJSONArray("m_servers");
                        if (m_servers != null) {
                            List<String> mServers = new ArrayList<>();
                            for (int i = 0; i < m_servers.length(); i++) {
                                mServers.add(m_servers.optString(i));
                            }
                            p2PData.m_servers = mServers;
                        }
                    }
                    playUrl.dolby_qn = play_url.optInt("dolby_qn", -1);
                    livePlayInfo.playUrl = playUrl;
                }
            }
            livePlayInfo.official_type = data.optInt("official_type", -1);
            livePlayInfo.official_room_id = data.optInt("official_room_id", -1);
            livePlayInfo.risk_with_delay = data.optInt("risk_with_delay", -1);
        }
        return null;
    }

    public static List<LiveRoom> analyzeLiveRooms(JSONArray list) throws JSONException {
        List<LiveRoom> liveRooms = new ArrayList<LiveRoom>();
        for (int i = 0; i < list.length(); i++) {
            JSONObject jsonObject = list.getJSONObject(i);
            LiveRoom liveRoom = new LiveRoom();
            liveRoom.roomid = jsonObject.optLong("roomid", -1);
            if (liveRoom.roomid == -1) {
                liveRoom.roomid = jsonObject.getLong("room_id");
            }
            liveRoom.uid = jsonObject.getLong("uid");
            liveRoom.title = jsonObject.getString("title");
            liveRoom.uname = jsonObject.getString("uname");
            liveRoom.online = jsonObject.optInt("online", -1);
            liveRoom.user_cover = jsonObject.optString("user_cover");
            liveRoom.user_cover_flag = jsonObject.optInt("user_cover_flag", -1);
            liveRoom.system_cover = jsonObject.optString("system_cover");
            liveRoom.cover = jsonObject.optString("cover");
            if (TextUtils.isEmpty(liveRoom.cover)) {
                liveRoom.cover = jsonObject.optString("cover_from_user");
            }
            liveRoom.show_cover = jsonObject.optString("show_cover");
            liveRoom.face = jsonObject.optString("face");
            liveRoom.area_parent_id = jsonObject.optInt("area_v2_parent_id", -1);
            liveRoom.area_parent_name = jsonObject.optString("area_v2_parent_name");
            liveRoom.area_id = jsonObject.optInt("area_v2_id", -1);
            liveRoom.area_name = jsonObject.optString("area_v2_name");
            liveRoom.session_id = jsonObject.optString("session_id");
            liveRoom.group_id = jsonObject.optInt("group_id");
            liveRoom.show_callback = jsonObject.optString("show_callback");
            liveRoom.click_callback = jsonObject.optString("click_callback");
            JSONObject verify = jsonObject.optJSONObject("verify");
            if (verify != null) {
                LiveRoom.Verify verifyObj = new LiveRoom.Verify();
                verifyObj.desc = verify.optString("desc");
                verifyObj.type = verify.optInt("type", -1);
                verifyObj.role = verify.optInt("role", -1);
                liveRoom.verify = verifyObj;
            }
            JSONObject watched_show = jsonObject.optJSONObject("watched_show");
            if (watched_show != null) {
                LiveRoom.Watched watched = new LiveRoom.Watched();
                watched.isSwitch = watched_show.optBoolean("switch", false);
                watched.num = watched_show.optInt("num", -1);
                watched.text_small = watched_show.optString("text_small");
                watched.text_large = watched_show.optString("text_large");
                watched.icon = watched_show.optString("icon");
                watched.icon_location = watched_show.optInt("icon_location", -1);
                watched.icon_web = watched_show.optString("icon_web");
                liveRoom.watched_show = watched;
            }
            liveRoom.liveTime = jsonObject.optLong("liveTime", -1);
            liveRooms.add(liveRoom);
        }
        return liveRooms;
    }
}
