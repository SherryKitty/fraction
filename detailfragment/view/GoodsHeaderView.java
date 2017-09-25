package com.loqunbai.android.detailfragment.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.loqunbai.android.commonresource.constants.Constants;
import com.loqunbai.android.commonresource.utils.StringUtils;
import com.loqunbai.android.commonresource.widget.AutoStrechImageView;
import com.loqunbai.android.commonresource.widget.LargeImageDialog;
import com.loqunbai.android.detailfragment.DressDetailFragment;
import com.loqunbai.android.fragment.R;
import com.loqunbai.android.models.UpcomingItemModel;
import com.loqunbai.android.utils.baseutil.NetworkUtils;
import com.loqunbai.android.utils.controller.ImageRequestController;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by lm on 15/10/30.
 * 商品详情 Header
 */
public class GoodsHeaderView extends View {

    private TextView mTvGoodsName;
    private TextView mTvPrice;
    private TextView mTvCurrencyName;
    private TextView mTvStartTime;
    private TextView mShopName;
    private ImageView mIvBrand;
    private TextView mTvType;
    private ViewGroup mContent;
    private TextView mTvComment;
    private TextView mTvIntro;
    private TextView mTvPriceSignal;
    private View mEmpty;

    private UpcomingItemModel mUpcomingItemModel;
    private LargeImageDialog mLargeImageDialog;

    private DressDetailFragment.OnActionListener mOnActionListener;
    /**
     * HeaderView
     */
    private View mRoot;

    private DisplayImageOptions largeImageLoadOptions = new DisplayImageOptions.Builder()
            .cacheInMemory(true)
            .cacheOnDisk(true)
            .imageScaleType(ImageScaleType.EXACTLY)
            .bitmapConfig(Bitmap.Config.RGB_565)
            .build();

    public GoodsHeaderView(Context context, ViewGroup root, UpcomingItemModel upcomingItemModel,
                           DressDetailFragment.OnActionListener onActionListener) {
        super(context);

        LayoutInflater mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mRoot = mInflater.inflate(R.layout.view_goods_header, root, false);

        mUpcomingItemModel = upcomingItemModel;

        mOnActionListener = onActionListener;

        setView();
        init();
    }

    /**
     * 初始化控件,并且返回view
     *
     * @return
     */
    public View getView() {
        return mRoot;
    }

    private void setView(){
        mTvGoodsName = (TextView) mRoot.findViewById(R.id.tv_goods_name);
        mTvPrice = (TextView) mRoot.findViewById(R.id.tv_price);
        mTvCurrencyName = (TextView) mRoot.findViewById(R.id.tv_currency_name);
        mTvStartTime = (TextView) mRoot.findViewById(R.id.tv_startTime);
        mShopName = (TextView) mRoot.findViewById(R.id.tv_shopname);
        mIvBrand = (ImageView) mRoot.findViewById(R.id.iv_brand);
        mTvType = (TextView) mRoot.findViewById(R.id.tv_type);
        mContent = (ViewGroup) mRoot.findViewById(R.id.ll_content);
        mTvIntro = (TextView) mRoot.findViewById(R.id.tv_intro);
        mTvComment = (TextView)mRoot.findViewById(R.id.tv_comment);
        mTvPriceSignal = (TextView)mRoot.findViewById(R.id.price_signal);
        mEmpty = mRoot.findViewById(R.id.empty_view);
    }

    private void init(){
        if( mUpcomingItemModel == null ){
            return;
        }

        if( StringUtils.isEmpty( mUpcomingItemModel.ordertype) ){
            mTvGoodsName.setText(mUpcomingItemModel.title);
        }else{
            if(  mUpcomingItemModel.ordertype.equals(Constants.ORDERTYPE_ALL) ){
                mTvGoodsName.setText(mUpcomingItemModel.title);
            }else if( mUpcomingItemModel.ordertype.equals(Constants.ORDERTYPE_DEPOSIT) ) {
                SpannableStringBuilder builder = new SpannableStringBuilder();
                builder.append("  ").append(mUpcomingItemModel.title);
                builder.setSpan(new ImageSpan(getContext(), R.drawable.icon_front_pay),
                        0, 1, 0);
                mTvGoodsName.setText(builder);
            }else if( mUpcomingItemModel.ordertype.equals(Constants.ORDERTYPE_REMAIN) ) {
                SpannableStringBuilder builder = new SpannableStringBuilder();
                builder.append("  ").append(mUpcomingItemModel.title);
                builder.setSpan(new ImageSpan(getContext(), R.drawable.icon_final_pay),
                        0, 1, 0);
                mTvGoodsName.setText(builder);
            }

        }

        mTvPrice.setText(mUpcomingItemModel.price);
        String clock = mUpcomingItemModel.clock;
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        SimpleDateFormat format2 = new SimpleDateFormat("MM/dd HH:mm");
        if (!StringUtils.isEmpty(clock)) {

            if( !clock.equals("1990-08-16 00:00") ) {
                try {
                    Date date = format.parse(clock);
                    String onSaleTime = format2.format(date);
                    mTvStartTime.setText(onSaleTime);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }else{
                mTvStartTime.setText(R.string.unknown);
            }
        }

        mShopName.setText(mUpcomingItemModel.master);
        String brand = mUpcomingItemModel.brand;
        if( StringUtils.isEmpty(brand) ){
            return;
        }
        if (brand.equals(Constants.BRAND_CHINA)) {
            mTvPriceSignal.setText("￥");
            mTvCurrencyName.setText(R.string.cny);
            mIvBrand.setImageResource(R.drawable.icon_brand_china);
        } else if (brand.equals(Constants.BRAND_JAPAN)) {
            mTvPriceSignal.setText("￥");
            mTvCurrencyName.setText(R.string.jpy);
            mIvBrand.setImageResource(R.drawable.icon_brand_japan);
        } else if(brand.equals(Constants.BRAND_KOREA)){
            mTvPriceSignal.setText("₩");
            mTvCurrencyName.setText(R.string.kry);
            mIvBrand.setImageResource(R.drawable.icon_brand_others);
        }

        if (mUpcomingItemModel.isnew) {
            mIvBrand.setImageResource(R.drawable.icon_new_brand_china_detail);
        } else {

        }

        String dressType = mUpcomingItemModel.dresstype;
        if (!StringUtils.isEmpty(dressType)
                && getContext() != null ) {
            Drawable imgType = null;
            if (dressType.equals(Constants.GOODS_TYPE_CLOTH)) {
                imgType = getResources().getDrawable(R.drawable.icon_type_cloth);
            } else if (dressType.equals(Constants.GOODS_TYPE_ACCESSORY)) {
                imgType = getResources().getDrawable(R.drawable.icon_type_accessory);
            } else if (dressType.equals(Constants.GOODS_TYPE_SHOES)) {
                imgType = getResources().getDrawable(R.drawable.icon_type_shoes);
            } else if (dressType.equals(Constants.GOODS_TYPE_WIG)) {
                imgType = getResources().getDrawable(R.drawable.icon_type_wig);
            } else if (dressType.equals(Constants.GOODS_TYPE_BAG)) {
                imgType = getResources().getDrawable(R.drawable.icon_type_bag);
            } else if (dressType.equals(Constants.GOODS_TYPE_OTHERS)) {
                imgType = getResources().getDrawable(R.drawable.icon_type_others);
            } else {
                imgType = getResources().getDrawable(R.drawable.icon_type_others);
            }
            mTvType.setCompoundDrawablesWithIntrinsicBounds(null, imgType, null, null);
        }
        mTvType.setText(dressType);

//        int comments = 0;
//        if( !StringUtils.isEmpty(mUpcomingItemModel.comments)){
//            comments = Integer.valueOf(mUpcomingItemModel.comments);
//        }else{
//
//        }
//
//        String commentNum = getContext().getString(R.string.comment_num,comments);
//        mTvComment.setText(commentNum);
//
//        if( comments > 0 ){
//            mEmpty.setVisibility(View.GONE);
//        }else{
//            mEmpty.setVisibility(View.VISIBLE);
//        }

        inflateContent();

    }

    private void inflateContent() {

        if( mUpcomingItemModel == null ){
            return;
        }

        mTvIntro.setText( mUpcomingItemModel.intro );

        mContent.removeAllViews();
        mContent.addView(mTvIntro);
        final ArrayList<String> images = new ArrayList<>();
        images.addAll(mUpcomingItemModel.images);
        images.addAll(mUpcomingItemModel.introImages);
        int count = images.size();
        for (int i = 0; i < count; i++) {
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            layoutParams.setMargins(0, 10, 0, 10);
            AutoStrechImageView imageView = new AutoStrechImageView(getContext());
            imageView.setLayoutParams(layoutParams);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setBackgroundColor(Color.TRANSPARENT);

            String url = images.get(i);
            if (!StringUtils.isEmpty(url)) {
                if(NetworkUtils.getNetworkType(getContext()).equals(NetworkUtils.NetworkType.WIFI)){
                    ImageLoader.getInstance().displayImage(
                            ImageRequestController.makeLargeUrl(url),
                            imageView,
                            largeImageLoadOptions);
                }else {
                    ImageLoader.getInstance().displayImage(
                            ImageRequestController.makeMiddleUrl(url),
                            imageView);
                }
            }

            final int index = mContent.getChildCount() - 1;//the first child is textview
            mContent.addView(imageView);

            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mLargeImageDialog == null) {
                        mLargeImageDialog = new LargeImageDialog(getContext(),
                                R.style.fullscreen_dialog);
                    }
                    mLargeImageDialog.setImageList(images, index);

                    if (!((Activity)getContext()).isFinishing() && !mLargeImageDialog.isShowing()) {
                        mLargeImageDialog.show();
                    }
                }
            });
        }

        if (mUpcomingItemModel.type.equals(Constants.DRESS_CONFIRMMED)) {
            ImageButton imgBtn = new ImageButton(getContext());
            imgBtn.setImageResource(R.drawable.icon_view_taobao);
            imgBtn.setBackgroundColor(Color.TRANSPARENT);
            mContent.addView(imgBtn);

            imgBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    viewTaobaoDetail();
                }
            });
        }

        if(!StringUtils.isEmpty(mUpcomingItemModel.comments)) {
            int number = Integer.valueOf(mUpcomingItemModel.comments);
            String commentNum = String.format(getContext().getString(R.string.comment_num), number);
            mTvComment.setText(commentNum);

            if (number > 0) {
                mEmpty.setVisibility(View.GONE);
            } else {
                mEmpty.setVisibility(View.VISIBLE);
            }
        }else{
            String commentNum = String.format(getContext().getString(R.string.comment_num), 0);
            mTvComment.setText(commentNum);
            mEmpty.setVisibility(View.VISIBLE);
        }

    }

    private void viewTaobaoDetail() {
        String url = mUpcomingItemModel.taobaolink;
        if (!StringUtils.isEmpty(url)) {
            mOnActionListener.onViewTaobaoDetail(url);
        }
    }

    public void update(UpcomingItemModel upcomingItemModel){
        if( upcomingItemModel != null ){
            mUpcomingItemModel = upcomingItemModel;
            inflateContent();
        }
    }

    public void updateCommentNum( int num ){
        String commentNum = String.format(getContext().getString(R.string.comment_num), num);
        mTvComment.setText(commentNum);

        if( num > 0 ){
            mEmpty.setVisibility(View.GONE);
        }else{
            mEmpty.setVisibility(View.VISIBLE);
        }
    }
}
