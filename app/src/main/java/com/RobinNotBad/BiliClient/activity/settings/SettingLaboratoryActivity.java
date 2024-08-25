package com.RobinNotBad.BiliClient.activity.settings;

import android.annotation.SuppressLint;
import android.os.Bundle;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.base.RefreshListActivity;
import com.RobinNotBad.BiliClient.adapter.SettingsAdapter;
import com.RobinNotBad.BiliClient.model.SettingSection;

import java.util.ArrayList;
import java.util.List;

public class SettingLaboratoryActivity extends RefreshListActivity {


    @SuppressLint({"MissingInflatedId", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setPageName("实验室");

        final List<SettingSection> sectionList = new ArrayList<>() {{
            add(new SettingSection("switch", "横屏模式", "ui_landscape", getString(R.string.setting_lab_ui_landscape), "false"));
            add(new SettingSection("input_string","开屏文字","ui_splashtext", "顾名思义，可以更改开屏文字，支持换行。开屏文字在网络请求完成后就会被打断。","欢迎使用\n哔哩终端"));
            //add(new SettingSection("input_int","test_int","test_int", "这是描述描述描述","123"));
            //add(new SettingSection("input_float","test_float","test_float_2", "这是描述描述描述","1.1"));
            //add(new SettingSection("switch", "圆屏模式", "ui_round", getString(R.string.setting_lab_ui_round), "false"));
        }};

        recyclerView.setHasFixedSize(true);

        SettingsAdapter adapter = new SettingsAdapter(this, sectionList);
        setAdapter(adapter);

        setRefreshing(false);
    }

}