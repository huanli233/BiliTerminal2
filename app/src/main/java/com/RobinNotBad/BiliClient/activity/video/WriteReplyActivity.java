package com.RobinNotBad.BiliClient.activity.video;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.base.BaseActivity;
import com.RobinNotBad.BiliClient.api.ReplyApi;
import com.google.android.material.card.MaterialCardView;

import org.json.JSONException;

import java.io.IOException;

public class WriteReplyActivity extends BaseActivity {

    boolean sent = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reply_write);


        Intent intent = getIntent();
        long oid = intent.getLongExtra("oid",0);
        long rpid = intent.getLongExtra("rpid",0);
        long parent = intent.getLongExtra("parent",0);
        String parentSender = intent.getStringExtra("parentSender");

        findViewById(R.id.top).setOnClickListener(view -> finish());

        EditText editText = findViewById(R.id.editText);
        MaterialCardView send = findViewById(R.id.send);

        Log.e("debug-发送评论",String.valueOf(rpid));

        send.setOnClickListener(view -> {
            if(!sent) {
                new Thread(() -> {
                    String text = editText.getText().toString();
                    if(!text.equals("")) {
                        try {
                            if(!parentSender.equals("")) text = "回复 @" + parentSender + " :" + text;

                            Log.e("debug-评论内容",text);

                            int result = ReplyApi.sendReply(oid, rpid, parent, text);

                            sent = true;
                            if (result == 0)
                                runOnUiThread(() -> Toast.makeText(WriteReplyActivity.this, "发送成功>w<", Toast.LENGTH_SHORT).show());
                            else
                                runOnUiThread(() -> Toast.makeText(WriteReplyActivity.this, "发送失败TAT", Toast.LENGTH_SHORT).show());
                            finish();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    else runOnUiThread(()-> Toast.makeText(this, "你还没输入内容呢~", Toast.LENGTH_SHORT).show());
                }).start();
            }
            else Toast.makeText(WriteReplyActivity.this, "正在发送中，\n别急嘛~", Toast.LENGTH_SHORT).show();
        });
    }
}