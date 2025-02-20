package com.huanli233.biliterminal2.adapter.video;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.elvishew.xlog.XLog;
import com.huanli233.biliterminal2.R;
import com.huanli233.biliterminal2.model.VideoCard;
import com.huanli233.biliterminal2.util.GlideUtil;
import com.huanli233.biliterminal2.util.ToolsUtil;

import java.util.Objects;

public class VideoCardHolder extends RecyclerView.ViewHolder {
    TextView title, upName, viewCount;
    ImageView cover;

    public VideoCardHolder(@NonNull View itemView) {
        super(itemView);
        title = itemView.findViewById(R.id.text_title);
        upName = itemView.findViewById(R.id.text_upname);
        viewCount = itemView.findViewById(R.id.text_viewcount);
        cover = itemView.findViewById(R.id.img_cover);
    }

    @SuppressLint("SetTextI18n")
    public void showVideoCard(VideoCard videoCard, Context context) {
        String str_upName = videoCard.getUploader();
        if (str_upName == null || str_upName.isEmpty()) {
            upName.setVisibility(View.GONE);
        } else upName.setText(str_upName);


        String str_viewCount = videoCard.getView();
        if (str_viewCount == null || str_viewCount.isEmpty()) {
            viewCount.setVisibility(View.GONE);
        } else {
            viewCount.setText(str_viewCount);
        }

        try {
            String coverUrl = videoCard.getCover();
            if (coverUrl != null && !coverUrl.isEmpty()) {
                Glide.with(context).asDrawable().load(GlideUtil.url(coverUrl))
                        .transition(GlideUtil.getTransitionOptions())
                        .placeholder(R.mipmap.placeholder)
                        .format(DecodeFormat.PREFER_RGB_565)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .apply(RequestOptions.bitmapTransform(new RoundedCorners(ToolsUtil.dp2px(5))).sizeMultiplier(0.85f))
                        .into(cover);
            }
        } catch (Exception e) {
            XLog.e(e);
        }

        switch (videoCard.getType()) {
            case "live":
                SpannableString sstr_live = new SpannableString("[直播]" + ToolsUtil.htmlToString(videoCard.getTitle()));
                sstr_live.setSpan(new ForegroundColorSpan(Color.rgb(207, 75, 95)), 0, 4, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                title.setText(sstr_live);
                break;
            case "series":
                SpannableString sstr_series = new SpannableString("[系列]" + ToolsUtil.htmlToString(videoCard.getTitle()));
                sstr_series.setSpan(new ForegroundColorSpan(Color.rgb(207, 75, 95)), 0, 4, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                title.setText(sstr_series);
                break;
            default:
                title.setText(ToolsUtil.htmlToString(videoCard.getTitle()));
        }

    }
}
