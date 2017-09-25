package com.loqunbai.android.detailfragment.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.loqunbai.android.commonresource.user.action.UserInfoActiion;
import com.loqunbai.android.commonresource.utils.StringUtils;
import com.loqunbai.android.commonresource.utils.TimeShowStyleUtil;
import com.loqunbai.android.commonresource.widget.LargeImageDialog;
import com.loqunbai.android.detailfragment.view.adapter.DailyLookThumbAdapter;
import com.loqunbai.android.fragment.R;
import com.loqunbai.android.models.DressingItemModel;
import com.loqunbai.android.utils.controller.ImageRequestController;
import com.loqunbai.android.utils.controller.storage.LruCache;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by user on 15/8/4.
 */
public class DailyLookDetailHeaderView extends View {

    private View mRoot;

    private ImageView mIvProfile;
    private TextView mTvName;
    private TextView mTvTime;
    private TextView mTvContent;
    private View mExpand;
    private ImageView mIvDailyLook1, mIvDailyLook2;
    private GridView mGridView;
    private TextView mTvComment;
    private TextView mTvLastLickName;
    private TextView mTvLickNum;
    private View mEmpty;

    private DressingItemModel mDressingItemModel;
    private int mCommentNum;
    private List<String> mImageList = new ArrayList<>();
    private LargeImageDialog mLargeImageDialog;
    private UserInfoActiion mUserInfoActiion;

    private DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
            .cacheOnDisk(true)
            .imageScaleType(ImageScaleType.EXACTLY)
            .bitmapConfig(Bitmap.Config.RGB_565)
            .showImageOnLoading(R.drawable.image_loading_pic)
            .considerExifParams(true)
            .build();

    private View.OnClickListener mOnStartUserInfoListener =
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if( mUserInfoActiion != null ){
                        mUserInfoActiion.startUserInfoActivity(mDressingItemModel.userid);
                    }
                }
            };

    public View getView(){
        return mRoot;
    }

    public DailyLookDetailHeaderView(Context context, ViewGroup root, DressingItemModel dressingItemModel) {
        super(context);

        LayoutInflater mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mRoot = mInflater.inflate(R.layout.view_dailylookdetail_header, root, false);

        mDressingItemModel = dressingItemModel;
        mUserInfoActiion = new UserInfoActiion(context);

        setView();
        init();
    }


    private void setView(){
        mIvProfile = (ImageView)mRoot.findViewById(R.id.iv_profile);
        mTvName = (TextView)mRoot.findViewById(R.id.tv_name);
        mTvTime = (TextView)mRoot.findViewById(R.id.tv_publishTime);
        mTvContent = (TextView)mRoot.findViewById(R.id.tv_content);
        mExpand = mRoot.findViewById(R.id.ll_expand);
        mIvDailyLook1 = (ImageView)mRoot.findViewById(R.id.iv_dailylook1);
        mIvDailyLook2 = (ImageView)mRoot.findViewById(R.id.iv_dailylook2);
        mGridView = (GridView)mRoot.findViewById(R.id.gv_collapse);
        mTvComment = (TextView)mRoot.findViewById(R.id.tv_comment);
        mTvLastLickName = (TextView)mRoot.findViewById(R.id.tv_last_lick_name);
        mTvLickNum = (TextView)mRoot.findViewById(R.id.tv_lick_num);
        mEmpty = mRoot.findViewById(R.id.empty_view);

        mIvDailyLook1.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mImageList.size() > 0) {
                    if (mLargeImageDialog == null) {
                        mLargeImageDialog = new LargeImageDialog(getContext(),
                                R.style.fullscreen_dialog);
                    }
                    mLargeImageDialog.setImageList(mImageList, 0);
                    mLargeImageDialog.setUserName(mDressingItemModel.name);
                    mLargeImageDialog.setWatermark(mDressingItemModel.watermark);
                    mLargeImageDialog.setIslocal(mDressingItemModel.isLocal);
                    mLargeImageDialog.show();
                }
            }
        });

        mIvDailyLook2.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mImageList.size() > 1) {
                    if (mLargeImageDialog == null) {
                        mLargeImageDialog = new LargeImageDialog(getContext(),
                                R.style.fullscreen_dialog);
                    }
                    mLargeImageDialog.setImageList(mImageList, 1);
                    mLargeImageDialog.setUserName(mDressingItemModel.name);
                    mLargeImageDialog.setWatermark(mDressingItemModel.watermark);
                    mLargeImageDialog.setIslocal(mDressingItemModel.isLocal);
                    mLargeImageDialog.show();
                }
            }
        });

        mGridView.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                        if (position < mImageList.size()) {
                            if (mLargeImageDialog == null) {
                                mLargeImageDialog = new LargeImageDialog(getContext(),
                                        R.style.fullscreen_dialog);
                            }
                            mLargeImageDialog.setImageList(mImageList, position);
                            mLargeImageDialog.setUserName(mDressingItemModel.name);
                            mLargeImageDialog.setWatermark(mDressingItemModel.watermark);
                            mLargeImageDialog.setIslocal(mDressingItemModel.isLocal);
                            mLargeImageDialog.show();
                        }
                    }
                });

        mIvProfile.setOnClickListener(mOnStartUserInfoListener);
        mTvName.setOnClickListener(mOnStartUserInfoListener);

    }

    private void init(){
        if( mDressingItemModel == null ){
            return;
        }

        String headerUrl = mDressingItemModel.header;
        if( !StringUtils.isEmpty(headerUrl)){
            ImageLoader.getInstance().displayImage( ImageRequestController.makeLargeUrl(headerUrl),
                    mIvProfile,
                    defaultOptions);
        }

        mTvName.setText(mDressingItemModel.name);
        mTvTime.setText(TimeShowStyleUtil.getHistoryTypeTime(getContext(), mDressingItemModel.date));
        mTvContent.setText(mDressingItemModel.content);
        mImageList = Arrays.asList(mDressingItemModel.images.split(","));
//        String watermark = mDressingItemModel.watermark;
        int imageCount = mImageList.size();
        if(imageCount == 1 ){
            mExpand.setVisibility(View.VISIBLE);
            mGridView.setVisibility(View.GONE);
            mIvDailyLook1.setVisibility(View.VISIBLE);
            mIvDailyLook2.setVisibility(View.INVISIBLE);

            String imgUrl = mImageList.get(0);
            if ( !StringUtils.isEmpty(imgUrl) ){
                String _url;
                if( mDressingItemModel.isLocal ){
                    _url = "file://" + imgUrl;
                }else {
                    _url = ImageRequestController.makeMiddleUrl(imgUrl);
                }
                ImageLoader.getInstance().displayImage(_url,
                        mIvDailyLook1,
                        defaultOptions);
            }
        }else if( imageCount == 2 ){
            mExpand.setVisibility(View.VISIBLE);
            mGridView.setVisibility(View.GONE);
            mIvDailyLook1.setVisibility(View.VISIBLE);
            mIvDailyLook2.setVisibility(View.VISIBLE);

            String imgUrl1 = mImageList.get(0);
            if ( !StringUtils.isEmpty(imgUrl1) ){
                String _url1;
                if( mDressingItemModel.isLocal ){
                    _url1 = "file://" + imgUrl1;
                }else {
                    _url1 = ImageRequestController.makeMiddleUrl(imgUrl1);
                }
                ImageLoader.getInstance().displayImage(
                        _url1,
                        mIvDailyLook1,
                        defaultOptions);
            }
            String imgUrl2 = mImageList.get(1);
            if ( !StringUtils.isEmpty(imgUrl2) ){
                String _url2;
                if( mDressingItemModel.isLocal ){
                    _url2 = "file://" + imgUrl2;
                }else {
                    _url2 = ImageRequestController.makeMiddleUrl(imgUrl2);
                }
                ImageLoader.getInstance().displayImage(
                        _url2,
                        mIvDailyLook2,
                        defaultOptions);
            }
        }else{
            //TODO: gridView
            mExpand.setVisibility(View.GONE);
            mGridView.setVisibility(View.VISIBLE);

            DailyLookThumbAdapter adapter = new DailyLookThumbAdapter(getContext(), mImageList, mDressingItemModel.isLocal);
            mGridView.setAdapter(adapter);
        }

        mCommentNum = mDressingItemModel.comments;
        String commentNum = String.format(getContext().getString(R.string.comment_num), mCommentNum);
        mTvComment.setText(commentNum);
        if( mDressingItemModel.suck > 0 ) {
            String lickNum = String.format(getContext().getString(R.string.lick_num), mDressingItemModel.suck);
            mTvLickNum.setText(lickNum);
            mTvLastLickName.setText(mDressingItemModel.lastsuck);
            mTvLastLickName.setVisibility(View.VISIBLE);
            mTvLickNum.setVisibility(View.VISIBLE);
        }else{
            mTvLastLickName.setVisibility(View.GONE);
            mTvLickNum.setVisibility(View.GONE);
        }

        if( mCommentNum > 0 ){
            mEmpty.setVisibility(View.GONE);
        }else{
            mEmpty.setVisibility(View.VISIBLE);
        }
    }

    public void update( DressingItemModel dressingItemModel ){
        if (dressingItemModel != null ){
            mDressingItemModel = dressingItemModel;
            init();
        }
    }

//    public void updateCommentNum( int num ){
//        mCommentNum = num;
//        String commentNum = String.format(getContext().getString(R.string.comment_num), mCommentNum);
//        mTvComment.setText(commentNum);
//
//        if( mCommentNum > 0 ){
//            mEmpty.setVisibility(View.GONE);
//        }else{
//            mEmpty.setVisibility(View.VISIBLE);
//        }
//    }
}
