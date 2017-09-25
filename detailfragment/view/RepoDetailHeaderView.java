package com.loqunbai.android.detailfragment.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.loqunbai.android.commonresource.user.action.UserInfoActiion;
import com.loqunbai.android.commonresource.utils.StringUtils;
import com.loqunbai.android.commonresource.utils.TimeShowStyleUtil;
import com.loqunbai.android.commonresource.widget.LargeImageDialog;
import com.loqunbai.android.fragment.R;
import com.loqunbai.android.models.DressingItemModel;
import com.loqunbai.android.utils.controller.ImageRequestController;
import com.loqunbai.android.utils.controller.storage.LruCache;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by user on 15/8/5.
 */
public class RepoDetailHeaderView extends View{

    private View mRoot;

    private ImageView mIvProfile;
    private TextView mTvName;
    private TextView mTvTime;

    private TextView mTvMerchantName;
    private TextView mTvGoodsName;
    private TextView mTvRepoContent;
    private TextView mTvRepoPic;
    private ViewGroup mRepoPicGallery;
    private TextView mTvComment;
    private TextView mTvLastLickName;
    private TextView mTvLickNum;
    private View mEmpty;

    private List<String> mImageList = new ArrayList<>();
    private DressingItemModel mDressingItemModel;
    private LargeImageDialog mLargeImageDialog;

    private int mCommentNum;

    private UserInfoActiion mUserInfoActiion;

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

    public RepoDetailHeaderView(Context context, ViewGroup root, DressingItemModel dressingItemModel) {
        super(context);

        LayoutInflater mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mRoot = mInflater.inflate(R.layout.view_repodetail_header, root, false);

        mDressingItemModel = dressingItemModel;
        mUserInfoActiion = new UserInfoActiion(context);

        setView();
        init();
    }

    private void setView(){
        mIvProfile = (ImageView)mRoot.findViewById(R.id.iv_profile);
        mTvName = (TextView)mRoot.findViewById(R.id.tv_name);
        mTvTime = (TextView)mRoot.findViewById(R.id.tv_publishTime);
        mTvComment = (TextView)mRoot.findViewById(R.id.tv_comment);
        mTvLastLickName = (TextView)mRoot.findViewById(R.id.tv_last_lick_name);
        mTvLickNum = (TextView)mRoot.findViewById(R.id.tv_lick_num);
        mTvRepoPic = (TextView)mRoot.findViewById(R.id.tv_repopic);

        mTvMerchantName = (TextView)mRoot.findViewById(R.id.tv_merchant_name);
        mTvGoodsName = (TextView)mRoot.findViewById(R.id.tv_goods_name);
        mTvRepoContent = (TextView)mRoot.findViewById(R.id.tv_repo_content);
        mRepoPicGallery = (ViewGroup)mRoot.findViewById(R.id.ll_gallery);

        mEmpty = mRoot.findViewById(R.id.empty_view);

        mIvProfile.setOnClickListener(mOnStartUserInfoListener);
        mTvName.setOnClickListener(mOnStartUserInfoListener);
    }

    private void init(){
        if( mDressingItemModel == null ){
            return;
        }

        String headerUrl = mDressingItemModel.header;
        if( !StringUtils.isEmpty(headerUrl)){
            ImageLoader.getInstance().displayImage(ImageRequestController.makeLargeUrl(headerUrl),
                    mIvProfile);
        }

        mTvName.setText(mDressingItemModel.name);
        mTvTime.setText(TimeShowStyleUtil.getHistoryTypeTime(getContext(), mDressingItemModel.date));

        String merchantName = mDressingItemModel.merchant;
        if( StringUtils.isEmpty(merchantName) ){
            mTvMerchantName.setText(R.string.not_exist);
        }else{
            mTvMerchantName.setText(mDressingItemModel.merchant);
        }
        mTvGoodsName.setText(mDressingItemModel.item);
        mTvRepoContent.setText(mDressingItemModel.content);

        mImageList = Arrays.asList(mDressingItemModel.images.split(","));
        int imageCount = mImageList.size();

        mRepoPicGallery.removeAllViews();
        int width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 180, getResources().getDisplayMetrics());
        String watermark = mDressingItemModel.watermark;
        for( int i = 0; i < imageCount; i++ ){
            ImageView imageView = new ImageView(getContext());
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    width, ViewGroup.LayoutParams.MATCH_PARENT
            );
            layoutParams.setMargins(0, 0, 8, 0);
            imageView.setLayoutParams(layoutParams);
            String imgUrl = mImageList.get(i);
            if( !StringUtils.isEmpty(imgUrl) ) {
                String _url;
                if( mDressingItemModel.isLocal ){
                    _url = "file://" + imgUrl;
                }else {
                    _url = ImageRequestController.makeMiddleUrl(imgUrl);
                }
                ImageLoader.getInstance().displayImage(
                        _url,
                        imageView);
                final int index = mRepoPicGallery.getChildCount();
                mRepoPicGallery.addView(imageView);
                imageView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (mLargeImageDialog == null) {
                            mLargeImageDialog = new LargeImageDialog(getContext(),
                                    R.style.fullscreen_dialog);
                        }
                        mLargeImageDialog.setImageList(mImageList, index);
                        mLargeImageDialog.setUserName(mDressingItemModel.name);
                        mLargeImageDialog.setWatermark(mDressingItemModel.watermark);
                        mLargeImageDialog.setIslocal(mDressingItemModel.isLocal);
                        mLargeImageDialog.show();
                    }
                });
            }

        }

        mCommentNum = mDressingItemModel.comments;
        String commentNum = String.format(getContext().getString(R.string.comment_num), mCommentNum);
        mTvComment.setText(commentNum);
        String repoPicNum = String.format(getContext().getString(R.string.repo_pic), imageCount);
        mTvRepoPic.setText(repoPicNum);

        if( mDressingItemModel.suck > 0 ) {
            String lickNum = String.format(getContext().getString(R.string.appreciate_num), mDressingItemModel.suck);
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
