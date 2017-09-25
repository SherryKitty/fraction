package com.loqunbai.android.detailfragment.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.annotation.ColorRes;
import android.text.ClipboardManager;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.loqunbai.android.commonresource.user.action.UserInfoActiion;
import com.loqunbai.android.commonresource.utils.StringUtils;
import com.loqunbai.android.commonresource.utils.TimeShowStyleUtil;
import com.loqunbai.android.detailfragment.Details_webFragment;
import com.loqunbai.android.fragment.R;
import com.loqunbai.android.models.DressingCommentModel;
import com.loqunbai.android.utils.controller.ImageRequestController;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;

/**
 * Created by user on 15/8/6.
 */
public class ShowCommentAdapter extends BaseAdapter implements Details_webFragment.OnActionListener {


    public String mUrl;

    private Context mContext;
    private LayoutInflater mInflater;
    private ArrayList<DressingCommentModel> mDressingCommentModelArr = new ArrayList<>();
    private UserInfoActiion mUserInfoActiion;

    public ShowCommentAdapter(Context context, ArrayList<DressingCommentModel> dressingCommentModelArr) {
        this.mContext = context;
        this.mInflater = LayoutInflater.from(context);
        this.mDressingCommentModelArr = new ArrayList<>(dressingCommentModelArr);
        mUserInfoActiion = new UserInfoActiion(context);
    }

    public void addData(ArrayList<DressingCommentModel> dressingCommentModelArr) {
        mDressingCommentModelArr.addAll(dressingCommentModelArr);
        notifyDataSetChanged();
    }

    public void addData(int index, ArrayList<DressingCommentModel> dressingCommentModelArr) {
        mDressingCommentModelArr.addAll(index, dressingCommentModelArr);
        notifyDataSetChanged();
    }

    public void setData(ArrayList<DressingCommentModel> dressingCommentModelArr) {
        mDressingCommentModelArr.clear();
        mDressingCommentModelArr.addAll(dressingCommentModelArr);
        notifyDataSetChanged();
    }

    public void removeData(int position) {
        mDressingCommentModelArr.remove(position);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
//        if( mDressingCommentModelArr.size() == 0 ){
//            return 1;
//        }else {
        return mDressingCommentModelArr.size();
//        }
    }

    @Override
    public DressingCommentModel getItem(int i) {
//        if( mDressingCommentModelArr.size() == 0 ){
//            return null;
//        }
        return mDressingCommentModelArr.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }


    public void copy(String content) {
        // 得到剪贴板管理器
        ClipboardManager cmb = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
        cmb.setText(content.trim());
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup viewGroup) {

//        if( mDressingCommentModelArr.size() == 0 ){
//            View emptyView = mInflater.inflate(R.layout.view_comment_empty_hint,viewGroup,false);
//            return emptyView;
//        }else {
        ViewHolder holder = null;

        if (convertView == null) {
            View view = mInflater.inflate(R.layout.item_show_comment, viewGroup, false);
            view.getPaddingTop();
            holder = new ViewHolder();
            holder.ivProfile = (ImageView) view.findViewById(R.id.iv_profile);
            holder.tvName = (TextView) view.findViewById(R.id.tv_name);
            holder.tvTime = (TextView) view.findViewById(R.id.tv_publishTime);
            holder.tvContent = (TextView) view.findViewById(R.id.tv_content);
            holder.tvReply = (TextView) view.findViewById(R.id.tv_reply);
            holder.tvToName = (TextView) view.findViewById(R.id.tv_toname);
            convertView = view;
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final DressingCommentModel dressingCommentModel = mDressingCommentModelArr.get(position);
        String imgUrl = dressingCommentModel.userheader;
        if (!StringUtils.isEmpty(imgUrl)) {
            ImageLoader.getInstance().displayImage(ImageRequestController.makeLargeUrl(imgUrl),
                    holder.ivProfile);
        }
        holder.tvName.setText(dressingCommentModel.username);

        if( StringUtils.isEmpty(dressingCommentModel.toname) ){
            holder.tvContent.setText(dressingCommentModel.content);
        }else {
            String str = mContext.getString(R.string.reply) + " " + dressingCommentModel.toname + " " + dressingCommentModel.content;
            int fstart = str.indexOf(dressingCommentModel.toname);
            int fend = fstart + dressingCommentModel.toname.length();
            SpannableStringBuilder style = new SpannableStringBuilder(str);
            //设置局部字体颜色

            style.setSpan(new ForegroundColorSpan(Color.parseColor("#707b99")),
                    fstart, fend, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
            holder.tvContent.setText(style);
        }

        String createTime = TimeShowStyleUtil.getHistoryTypeTime(mContext, dressingCommentModel.ctime);
        holder.tvTime.setText(createTime);
        String toName = dressingCommentModel.toname;
        if (!StringUtils.isEmpty(toName)) {

        } else {
            holder.tvReply.setVisibility(View.GONE);
            holder.tvToName.setVisibility(View.GONE);
        }
//        holder.tvContent.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                if (event.getAction() == MotionEvent.ACTION_UP) {
//                    try {
//                        mUrl = null;
//                        //打开系统浏览器
//                        if (mDressingCommentModelArr.get(position).content.substring(0, 4).equals("http")) {
//                            mUrl = "http" + mDressingCommentModelArr.get(position).content.substring(4, mDressingCommentModelArr.get(position).content.length());
//                        }
//                        if (mDressingCommentModelArr.get(position).content.substring(0, 5).equals("https")) {
//                            mUrl = "https" + mDressingCommentModelArr.get(position).content.substring(5, mDressingCommentModelArr.get(position).content.length());
//                        }
//                        if (mUrl != null) {
//                            Toast.makeText(mContext, "打开网址:" + mUrl, Toast.LENGTH_SHORT).show();
//                        }
//                        Uri uri = Uri.parse(mUrl);
//                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
//                        mContext.startActivity(intent);
//                    } catch (Exception e) {
//                        Log.i("打开网址问题", e.getMessage());
//                    }
//
//                    return false;
//                } else return true;
//            }
//        });

        View.OnClickListener mOnStartUserInfoListener =
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mUserInfoActiion != null) {
                            mUserInfoActiion.startUserInfoActivity(dressingCommentModel.userid);
                        }
                    }
                };

        holder.ivProfile.setOnClickListener(mOnStartUserInfoListener);
        holder.tvName.setOnClickListener(mOnStartUserInfoListener);


        return convertView;
//        }
    }

    @Override
    public void getUrl(String url) {

    }


    private static class ViewHolder {
        public ImageView ivProfile;
        public TextView tvName;
        public TextView tvTime;
        public TextView tvContent;
        public TextView tvReply;
        public TextView tvToName;

    }
}
