package com.loqunbai.android.detailfragment.view.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.Toast;

import com.loqunbai.android.commonresource.utils.StringUtils;
import com.loqunbai.android.commonresource.utils.TimeShowStyleUtil;
import com.loqunbai.android.commonresource.widget.LargeImageDialog;
import com.loqunbai.android.commonresource.widget.ScrollGridView;
import com.loqunbai.android.commonresource.widget.SquareImageView;
import com.loqunbai.android.fragment.R;
import com.loqunbai.android.models.DressingItemModel;
import com.loqunbai.android.utils.controller.ImageRequestController;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Sherry on 15/8/5.
 * 修改 song，未完整，超过3图预览，不能获得点击那张
 */
public class DailyLookThumbAdapter extends BaseAdapter {

    private Context mContext;
    private LayoutInflater mInflater;
    private List<String> mUrlArr = new ArrayList<>();
    private boolean mIsLocal;
    private DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
            .cacheOnDisk(true)
            .imageScaleType(ImageScaleType.EXACTLY)
            .bitmapConfig(Bitmap.Config.RGB_565)
            .showImageOnLoading(R.drawable.image_loading_pic)
            .considerExifParams(true)
            .build();

    public DailyLookThumbAdapter(Context context, List<String> urlArr, boolean isLocal) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
        mUrlArr = urlArr;
        mIsLocal = isLocal;
    }

    @Override
    public int getCount() {
        return mUrlArr.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        ViewHolder holder = null;
        if (convertView == null) {
            View view = mInflater.inflate(R.layout.item_dailylook_detail, viewGroup, false);
            view.getPaddingTop();
            holder = new ViewHolder();
            holder.mIvThumb = (ImageView) view.findViewById(R.id.iv_pic);
            convertView = view;
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.mIvThumb.setImageResource(R.drawable.image_loading_pic);


        final String url = mUrlArr.get(position);

        if (!StringUtils.isEmpty(url)) {
            String _url;
            if( mIsLocal ){
                _url = "file://" + url;
            }else {
                _url = ImageRequestController.makeThumbUrl(url);
            }
            ImageLoader.getInstance().displayImage(_url,
                    holder.mIvThumb, defaultOptions);

        }
        return convertView;
    }

    private static class ViewHolder {
        public ImageView mIvThumb;
    }
}
