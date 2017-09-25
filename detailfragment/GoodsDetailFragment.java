package com.loqunbai.android.detailfragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.loqunbai.android.commonresource.SpipeData;
import com.loqunbai.android.commonresource.constants.Constants;
import com.loqunbai.android.commonresource.push.JPushUtils;
import com.loqunbai.android.commonresource.utils.StringUtils;
import com.loqunbai.android.commonresource.utils.share.ShareController;
import com.loqunbai.android.commonresource.widget.AutoStrechImageView;
import com.loqunbai.android.commonresource.widget.FloatMessageDialog;
import com.loqunbai.android.commonresource.widget.LargeImageDialog;
import com.loqunbai.android.commonresource.widget.ProcessingDialog;
import com.loqunbai.android.detailfragment.dialog.DetailMoreActionDialog;
import com.loqunbai.android.fragment.R;
import com.loqunbai.android.models.FavAction;
import com.loqunbai.android.models.FavType;
import com.loqunbai.android.models.PostResultModel;
import com.loqunbai.android.models.UpcomingItemModel;
import com.loqunbai.android.utils.controller.FavListUpcomingController;
import com.loqunbai.android.utils.controller.FavLookupController;
import com.loqunbai.android.utils.controller.ImageRequestController;
import com.loqunbai.android.utils.controller.RemindLookupController;
import com.loqunbai.android.utils.controller.RequestParameterController;
import com.loqunbai.android.utils.controller.cacheupdater.ExistCacheUpdater;
import com.loqunbai.android.utils.controller.cacheupdater.ListCacheUpdater;
import com.loqunbai.android.utils.controller.storage.LruCache;
import com.loqunbai.android.utils.sdk.AbstractRequestController;
import com.loqunbai.android.utils.sdk.daliylookrepo.DressRemindRequest;
import com.loqunbai.android.utils.sdk.daliylookrepo.DressUnRemindRequest;
import com.loqunbai.android.utils.sdk.daliylookrepo.FavPostRequest;
import com.loqunbai.android.utils.sdk.upcoming.UpcomingItemRequest;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.umeng.analytics.MobclickAgent;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * （上新）商品详情界面
 */
public class GoodsDetailFragment extends Fragment {

    private ImageButton mIbtnBack;
    private TextView mTvTitle;
    private TextView mTvGoodsName;
    private TextView mTvPrice;
    private TextView mTvCurrencyName;
    private TextView mTvStartTime;
    private TextView mShopName;
    private ImageView mIvBrand;
    private TextView mTvType;
    private ViewGroup mContent;

    private View mFav, mFavOther;
    private View mBuy, mReminder, mRemindWhenConfirm;
    private View mShare, mShareOther;

    private TextView mTvFav, mTvFavOther;
    private TextView mTvBuy, mTvReminder, mTvRemindWhenConfirm;
    private View mBottomChina, mBottomOther;
    private TextView mTvShare, mTvShareOther;

    private ImageSize mImageSize = new ImageSize(100, 100);
    private ImageLoader mImageLoader;
    private SimpleImageLoadingListener mSimpleImageLoadingListener;
    private OnActionListener mOnActionListener;

    private UpcomingItemModel mUpcomingItemModel;
    private String mShareImgUrl;
    private LruCache<Bitmap> mBitmapCache;
    private LargeImageDialog mLargeImageDialog;

    private ProcessingDialog mUploadProcessingDialog;

    //Main for alarm
    private String mItemId;
    private boolean isSetRemind;


    Map<String, String> map_value = new HashMap<>();


    private AbstractRequestController.AsyncCallback<UpcomingItemModel> mUpcomingItemModelCallback
            = new AbstractRequestController.AsyncCallback<UpcomingItemModel>() {
        @Override
        public void getResult(UpcomingItemModel result) {
            mUpcomingItemModel = result;
            if (mUpcomingItemModel != null) {
                if (!mUpcomingItemModel.mark) {
                    init();
                } else {
                    //TODO: has deleted
                    getActivity().finish();
                }
            }
        }

        @Override
        public void getError(Exception exception) {
            exception.printStackTrace();
        }
    };

    private AbstractRequestController.AsyncCallback<PostResultModel> mFavCallback;

    private AbstractRequestController.AsyncCallback<PostResultModel> mSetReminderCallback
            = new AbstractRequestController.AsyncCallback<PostResultModel>() {
        @Override
        public void getResult(PostResultModel result) {

            final FloatMessageDialog floatDialog = new FloatMessageDialog(getActivity());
            floatDialog.setResId(R.drawable.float_msg_reminder_success);
            if (result.status == 0) {

                floatDialog.show();
                Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        if( floatDialog != null && floatDialog.isShowing() ) {
                            floatDialog.dismiss();
                        }
                    }
                }, 500);
                Log.d("Remind", "Remind success");
                mTvRemindWhenConfirm.setText(R.string.has_set);
                mTvRemindWhenConfirm.setSelected(true);

                ExistCacheUpdater.RemindListUpdater.localUpdateAsync(
                        mUpcomingItemModel.getId(), true);
                isSetRemind = true;
            }
        }

        @Override
        public void getError(Exception exception) {
            exception.printStackTrace();
        }
    };

    private AbstractRequestController.AsyncCallback<PostResultModel> mSetStartReminderCallback
            = new AbstractRequestController.AsyncCallback<PostResultModel>() {
        @Override
        public void getResult(PostResultModel result) {
            if (!isAdded()) {
                return;
            }
            final FloatMessageDialog floatDialog = new FloatMessageDialog(getActivity());
            floatDialog.setResId(R.drawable.float_msg_reminder_success);
            if (result.status == 0) {
                floatDialog.show();
                Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        if( floatDialog != null && floatDialog.isShowing() ) {
                            floatDialog.dismiss();
                        }
                    }
                }, 500);
                mOnActionListener.onRemind(mUpcomingItemModel.clockstamp,
                        mUpcomingItemModel.title,
                        mUpcomingItemModel.getId());
                mTvReminder.setSelected(true);
                mTvReminder.setText(R.string.cancel_reminder);
                ExistCacheUpdater.RemindListUpdater.localUpdateAsync(
                        mUpcomingItemModel.getId(), true);
                isSetRemind = true;
            }
        }

        @Override
        public void getError(Exception exception) {
            exception.printStackTrace();
        }
    };

    private AbstractRequestController.AsyncCallback<PostResultModel> mCancelStartReminderCallback
            = new AbstractRequestController.AsyncCallback<PostResultModel>() {
        @Override
        public void getResult(PostResultModel result) {
            if (result.status == 0) {
                mOnActionListener.onCancelReminder(mUpcomingItemModel.getId());
                mTvReminder.setSelected(false);
                mTvReminder.setText(R.string.start_reminder);

                ExistCacheUpdater.RemindListUpdater.localUpdateAsync(
                        mUpcomingItemModel.getId(), false);
                isSetRemind = false;
            }
        }

        @Override
        public void getError(Exception exception) {
            exception.printStackTrace();
        }
    };

    private AbstractRequestController.AsyncCallback<PostResultModel> mCancelConfirmedReminderCallback
            = new AbstractRequestController.AsyncCallback<PostResultModel>() {
        @Override
        public void getResult(PostResultModel result) {
            if (result.status == 0) {
                mTvRemindWhenConfirm.setText(R.string.remind_when_confirm);
                mTvRemindWhenConfirm.setSelected(false);

                ExistCacheUpdater.RemindListUpdater.localUpdateAsync(
                        mUpcomingItemModel.getId(), false);
                isSetRemind = false;
            }
        }

        @Override
        public void getError(Exception exception) {
            exception.printStackTrace();
        }
    };

    //处理分享（qq/微信/微博/复制链接）点击后的事件
    //点击后收起dialog
    private DetailMoreActionDialog.IActionClickListener mActionClickListener
            = new DetailMoreActionDialog.IActionClickListener() {
        @Override
        public void onActionClick(DialogInterface dialog, int action) {

            switch (action) {
                case DetailMoreActionDialog.WEIXIN_FRIEND_ACTION:

                    mUploadProcessingDialog = new ProcessingDialog(getActivity());
                    mUploadProcessingDialog.setMessage(getString(R.string.sharing));
                    mUploadProcessingDialog.show();

                    mSimpleImageLoadingListener = new SimpleImageLoadingListener() {
                        @Override
                        public void onLoadingComplete(String s, View view, Bitmap bitmap) {
                            if( mUploadProcessingDialog != null && mUploadProcessingDialog.isShowing() ) {
                                mUploadProcessingDialog.dismiss();
                            }
                            ShareController shareController = new ShareController(getActivity());
                            shareController.shareToWeixin(mUpcomingItemModel.title,
                                    "", getCopyLink(), bitmap);
                        }
                    };

                    mImageLoader.loadImage(mShareImgUrl,
                            mImageSize, mSimpleImageLoadingListener);

                    break;
                case DetailMoreActionDialog.QQ_FRIEND_ACTION:
                    ShareController shareController = new ShareController(getActivity());
                    shareController.shareToQq(mUpcomingItemModel.title,
                            "", getCopyLink(), mShareImgUrl);
                    break;
                case DetailMoreActionDialog.WEIBO_ACTION:

                    mUploadProcessingDialog = new ProcessingDialog(getActivity());
                    mUploadProcessingDialog.setMessage(getString(R.string.sharing));
                    mUploadProcessingDialog.show();

                    mSimpleImageLoadingListener = new SimpleImageLoadingListener() {
                        @Override
                        public void onLoadingComplete(String s, View view, Bitmap bitmap) {
                            if( mUploadProcessingDialog != null && mUploadProcessingDialog.isShowing() ) {
                                mUploadProcessingDialog.dismiss();
                            }
                            ShareController shareController = new ShareController(getActivity());
                            shareController.shareToWeibo(mUpcomingItemModel.title,
                                    "", getCopyLink(), bitmap);
                        }
                    };

                    mImageLoader.loadImage(mShareImgUrl,
                            mImageSize, mSimpleImageLoadingListener);
                    break;
                case DetailMoreActionDialog.COPYLINK_ACTION:
                    if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) {
                        android.text.ClipboardManager clipboard = (android.text.ClipboardManager)
                                getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                        clipboard.setText(getCopyLink());
                    } else {
                        android.content.ClipboardManager clipboard = (android.content.ClipboardManager)
                                getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                        android.content.ClipData clip = android.content.ClipData.newPlainText(
                                getResources().getString(R.string.copied_to_clipboard), getCopyLink());
                        clipboard.setPrimaryClip(clip);
                        Toast.makeText(getActivity(), "已经复制到粘贴板", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case DetailMoreActionDialog.CANCEL_ACTION:
                    dialog.dismiss();
                    break;
            }
        }
    };

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int id = v.getId();
            if (id == mBuy.getId()) {
                viewTaobaoDetail();
                MobclickAgent.onEvent(getActivity(), "goods_buy");
            } else if (id == mIbtnBack.getId()) {
                getActivity().finish();
            } else if (id == mShare.getId() || id == mShareOther.getId()) {
                DetailMoreActionDialog dialog = new DetailMoreActionDialog(getActivity(),
                        R.style.more_action_dialog, mActionClickListener
                );
                dialog.show();
                MobclickAgent.onEvent(getActivity(), "goods_share");
            } else if (id == mReminder.getId()) {
                if (isSetRemind) {
                    MobclickAgent.onEvent(getActivity(), "goods_cancel_share");
                    cancelStartRemind();
                } else {
                    setStartRemind();
                }


            } else if (id == mRemindWhenConfirm.getId()) {
                if (SpipeData.getInst().isLogin()) {

                    if (isSetRemind) {
                        cancelConfirmedRemind();
                    } else {
                        setConfirmedRemind();
                    }
                }
            } else if (id == mFav.getId()) {
                MobclickAgent.onEvent(getActivity(), "goods_collect");
                onFav(mTvFav);
            } else if (id == mFavOther.getId()) {
                onFav(mTvFavOther);
            }

        }
    };

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        mUpcomingItemModel = getArguments().getParcelable(Constants.DRESS_DETAIL);
        if (mUpcomingItemModel != null) {
//            if (mUpcomingItemModel.introImages.size() > 0) {
//                mShareImgUrl = mUpcomingItemModel.images.get(0);
//            }
        } else {
            mItemId = getArguments().getString(Constants.DRESS_DETAIL_ID);

        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_goods_detail, container, false);
        initImageLoader();

        setView(view);

        if (mUpcomingItemModel != null) {
            init();
        } else if (!StringUtils.isEmpty(mItemId)) {
            UpcomingItemRequest.startQuery(RequestParameterController.getInstance().getUrl(),
                    mItemId).requestAsync(mUpcomingItemModelCallback);
        }

        MobclickAgent.onEvent(getActivity(), "goods_detail");
        return view;
    }

    private void initImageLoader() {
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .cacheOnDisk(true).imageScaleType(ImageScaleType.EXACTLY)
                .bitmapConfig(Bitmap.Config.RGB_565).build();
        ImageLoaderConfiguration.Builder builder = new ImageLoaderConfiguration.Builder(
                getActivity()).defaultDisplayImageOptions(defaultOptions).memoryCache(
                new WeakMemoryCache());

        ImageLoaderConfiguration config = builder.build();
        mImageLoader = ImageLoader.getInstance();
        mImageLoader.init(config);
    }


    private void setView(View view) {
        //打开详情页次数
        MobclickAgent.onEvent(getActivity(), "open_goodsdetail");

        mIbtnBack = (ImageButton) view.findViewById(R.id.ibtn_back);
        mTvTitle = (TextView) view.findViewById(R.id.tv_title);
        mTvGoodsName = (TextView) view.findViewById(R.id.tv_goods_name);
        mTvPrice = (TextView) view.findViewById(R.id.tv_price);
        mTvCurrencyName = (TextView) view.findViewById(R.id.tv_currency_name);
        mTvStartTime = (TextView) view.findViewById(R.id.tv_startTime);
        mShopName = (TextView) view.findViewById(R.id.tv_shopname);
        mIvBrand = (ImageView) view.findViewById(R.id.iv_brand);
        mTvType = (TextView) view.findViewById(R.id.tv_type);
        mContent = (ViewGroup) view.findViewById(R.id.ll_content);

        mFav = view.findViewById(R.id.fav);
        mFavOther = view.findViewById(R.id.fav_other);
        mBuy = view.findViewById(R.id.buy);
        mReminder = view.findViewById(R.id.reminder);
        mRemindWhenConfirm = view.findViewById(R.id.reminder_when_confirm);
        mShare = view.findViewById(R.id.share);
        mShareOther = view.findViewById(R.id.share_other);

        mTvFav = (TextView) view.findViewById(R.id.tv_fav);
        mTvFavOther = (TextView) view.findViewById(R.id.tv_fav_other);
        mTvBuy = (TextView) view.findViewById(R.id.tv_buy);
        mTvReminder = (TextView) view.findViewById(R.id.tv_reminder);
        mTvRemindWhenConfirm = (TextView) view.findViewById(R.id.tv_reminder_when_confirm);
        mBottomChina = view.findViewById(R.id.chinese_bottom);
        mBottomOther = view.findViewById(R.id.other_bottom);
        mTvShare = (TextView) view.findViewById(R.id.tv_share);
        mTvShareOther = (TextView) view.findViewById(R.id.tv_share_other);

        mTvTitle.setText(R.string.goods_detail);

        mIbtnBack.setOnClickListener(mOnClickListener);

        mFav.setOnClickListener(mOnClickListener);
        mFavOther.setOnClickListener(mOnClickListener);
        mBuy.setOnClickListener(mOnClickListener);
        mReminder.setOnClickListener(mOnClickListener);
        mRemindWhenConfirm.setOnClickListener(mOnClickListener);
        mShare.setOnClickListener(mOnClickListener);
        mShareOther.setOnClickListener(mOnClickListener);
////////////////////////////////////////
//        mTvBuy.setOnClickListener(mOnClickListener);
//        mTvShare.setOnClickListener(mOnClickListener);
//        mTvReminder.setOnClickListener(mOnClickListener);
        //Remind when it's confirmed
//        mTvRemindWhenConfirm.setOnClickListener(mOnClickListener);
//        mTvFav.setOnClickListener(mOnClickListener);
//        mTvFavOther.setOnClickListener(mOnClickListener);
    }

    private void init() {
        if (mUpcomingItemModel.images.size() > 0) {
            mShareImgUrl = mUpcomingItemModel.images.get(0);
        }

        mTvGoodsName.setText(mUpcomingItemModel.title);
//
//        if (mUpcomingItemModel.price.length() > 10) {
//
//        }

        mTvPrice.setText(mUpcomingItemModel.price);
        String clock = mUpcomingItemModel.clock;
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        SimpleDateFormat format2 = new SimpleDateFormat("MM/dd HH:mm");
        if (!StringUtils.isEmpty(clock)) {

            try {
                Date date = format.parse(clock);
                String onSaleTime = format2.format(date);
                mTvStartTime.setText(onSaleTime);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        boolean isFaved = FavLookupController.getInstance().isItemFavored(mUpcomingItemModel.getId());
        isSetRemind = RemindLookupController.getInstance().isItemSetRemind(mUpcomingItemModel.getId());

        if (mUpcomingItemModel.brand.equals(Constants.BRAND_CHINA)) {
            mBottomChina.setVisibility(View.VISIBLE);
            mBottomOther.setVisibility(View.GONE);
            if (mUpcomingItemModel.type.equals(Constants.DRESS_CONFIRMMED)) {
                if (!StringUtils.isEmpty(clock)) {
                    try {
                        Date saleDate = format.parse(clock);
                        Long onSaleTime = saleDate.getTime();
                        Date curDate = new Date();
                        Long curTime = curDate.getTime();

                        if (onSaleTime <= curTime) {
                            mBuy.setVisibility(View.VISIBLE);
                            mReminder.setVisibility(View.GONE);
                            mRemindWhenConfirm.setVisibility(View.GONE);
                        } else {
                            mBuy.setVisibility(View.GONE);
                            mReminder.setVisibility(View.VISIBLE);
                            mRemindWhenConfirm.setVisibility(View.GONE);

                            if (isSetRemind) {
                                mReminder.setSelected(true);
                                mTvReminder.setText(R.string.cancel_reminder);
                            } else {
                                mReminder.setSelected(false);
                                mTvReminder.setText(R.string.start_reminder);
                            }
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                mBuy.setVisibility(View.GONE);
                mReminder.setVisibility(View.GONE);
                mRemindWhenConfirm.setVisibility(View.VISIBLE);

                if (isSetRemind) {
                    mRemindWhenConfirm.setSelected(true);
                    mTvRemindWhenConfirm.setText(R.string.has_set);
                } else {
                    mRemindWhenConfirm.setSelected(false);
                    mTvRemindWhenConfirm.setText(R.string.remind_when_confirm);
                }
            }

            mFav.setSelected(isFaved);
            if (isFaved) {
                mTvFav.setText(R.string.fav_done);
            } else {
                mTvFav.setText(R.string.fav);
            }
        } else {
            mBottomChina.setVisibility(View.GONE);
            mBottomOther.setVisibility(View.VISIBLE);

            mFavOther.setSelected(isFaved);
            if (isFaved) {
                mTvFavOther.setText(R.string.fav_done);
            } else {
                mTvFavOther.setText(R.string.fav);
            }
        }

        mShopName.setText(mUpcomingItemModel.master);
        String brand = mUpcomingItemModel.brand;
        if (brand.equals(Constants.BRAND_CHINA)) {
            mTvCurrencyName.setText(R.string.cny);
            mIvBrand.setImageResource(R.drawable.icon_brand_china);
        } else if (brand.equals(Constants.BRAND_JAPAN)) {
            mTvCurrencyName.setText(R.string.jpy);
            mIvBrand.setImageResource(R.drawable.icon_brand_japan);
        }

        if (mUpcomingItemModel.isnew) {
            mIvBrand.setImageResource(R.drawable.icon_new_brand_china_detail);
        } else {

        }

        String dressType = mUpcomingItemModel.dresstype;
        if (!StringUtils.isEmpty(dressType)
                && getActivity() != null
                && isAdded()) {
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

        //TODO:move to ActivityCreated
        if (isAdded()) {
            inflateContent();
        }

    }

    private void inflateContent() {

        mBitmapCache = new LruCache<Bitmap>(LruCache.DEFAULT_CAPACITY);

        final ArrayList<String> images = new ArrayList<>();
        images.addAll(mUpcomingItemModel.images);
        images.addAll(mUpcomingItemModel.introImages);
        int count = images.size();
        for (int i = 0; i < count; i++) {
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            layoutParams.setMargins(0, 10, 0, 10);
            AutoStrechImageView imageView = new AutoStrechImageView(getActivity());
            imageView.setLayoutParams(layoutParams);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setBackgroundColor(Color.TRANSPARENT);

            String url = images.get(i);
            if (!StringUtils.isEmpty(url)) {
                final String imageUrl = ImageRequestController.makeMiddleUrl(url);
                mSimpleImageLoadingListener = new SimpleImageLoadingListener() {
                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                        mBitmapCache.insert(imageUrl, loadedImage);
                    }
                };
                mImageLoader.displayImage(imageUrl,
                        imageView, mSimpleImageLoadingListener);
            }

            final int index = mContent.getChildCount();
            mContent.addView(imageView);

            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mLargeImageDialog == null) {
                        mLargeImageDialog = new LargeImageDialog(getActivity(),
                                R.style.fullscreen_dialog);
                    }
//                    mLargeImageDialog.setCache(mBitmapCache);
                    mLargeImageDialog.setImageList(images, index);
                    mLargeImageDialog.show();
                }
            });
        }

        if (mUpcomingItemModel.type.equals(Constants.DRESS_CONFIRMMED)) {
            ImageButton imgBtn = new ImageButton(getActivity());
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
    }

    private void viewTaobaoDetail() {
        String url = mUpcomingItemModel.taobaolink;
        if (!StringUtils.isEmpty(url)) {
            mOnActionListener.onViewTaobaoDetail(url);
        }
    }

    private String getCopyLink() {
        String link = "http://" + RequestParameterController.getInstance().getUrl()
                + "/dress.html?id=" + mUpcomingItemModel.getId();

        return link;
    }

    private void onFav(TextView view) {
        if (mUpcomingItemModel == null) {
            return;
        }
        final boolean isFaved = FavLookupController.getInstance().isItemFavored(
                mUpcomingItemModel.getId());
        view.setSelected(!isFaved);
        FavAction favAction;
        FavType favType = FavType.UPCOMING;
        if (isFaved) {
            favAction = FavAction.UNFAV;
            view.setText(R.string.fav);
        } else {
            favAction = FavAction.FAV;
            view.setText(R.string.fav_done);
        }


        final String id = mUpcomingItemModel.getId();
        ExistCacheUpdater.FavListUpdater.localUpdateAsync(
                id, !isFaved);


        final FloatMessageDialog floatMessageDialog = new FloatMessageDialog(getActivity(), R.drawable.float_msg_empty);
        floatMessageDialog.setMessage(getString(R.string.grassed));
        mFavCallback = new AbstractRequestController.AsyncCallback<PostResultModel>() {
            @Override
            public void getResult(PostResultModel result) {
                if (result.status == 0) {
                    //refresh local cache
                    ListCacheUpdater.upcomingCacheUpdater.localUpdateAsync(
                            id,
                            mUpcomingItemModel
                    );

                    if (!isFaved) {
                        if (!getActivity().isFinishing()) {
                            floatMessageDialog.show();
                            Timer timer = new Timer();
                            timer.schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    if( floatMessageDialog != null
                                            && floatMessageDialog.isShowing() ) {
                                        floatMessageDialog.dismiss();
                                    }
                                }
                            }, 500);
                        }

                    } else {
                        FavListUpcomingController.getInstance().removeFavItem(FavType.UPCOMING, id);
                    }

                } else {

                }
            }

            @Override
            public void getError(Exception exception) {
                exception.printStackTrace();
            }
        };

        FavPostRequest.startQuery(RequestParameterController.getInstance().getUrl(),
                favAction, favType,
                RequestParameterController.getInstance().getToken(), id).requestAsync(mFavCallback);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mOnActionListener = (OnActionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnActionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mOnActionListener = null;
    }

    private void setStartRemind() {
        DressRemindRequest.startQuery(
                RequestParameterController.getInstance().getUrl(),
                RequestParameterController.getInstance().getToken(),
                JPushUtils.getRegistrationID(getActivity()),
                mUpcomingItemModel.getId(),
                DressRemindRequest.REQ_TYPE_CLIENT).requestAsync(mSetStartReminderCallback
        );
    }

    private void cancelStartRemind() {

        DressUnRemindRequest.startQuery(
                RequestParameterController.getInstance().getUrl(),
                RequestParameterController.getInstance().getToken(),
                mUpcomingItemModel.getId()).requestAsync(mCancelStartReminderCallback);
    }

    private void setConfirmedRemind() {
        DressRemindRequest.startQuery(
                RequestParameterController.getInstance().getUrl(),
                RequestParameterController.getInstance().getToken(),
                JPushUtils.getRegistrationID(getActivity()),
                mUpcomingItemModel.getId(),
                DressRemindRequest.REQ_TYPE_SERVER).requestAsync(mSetReminderCallback);
    }

    private void cancelConfirmedRemind() {

        DressUnRemindRequest.startQuery(
                RequestParameterController.getInstance().getUrl(),
                RequestParameterController.getInstance().getToken(),
                mUpcomingItemModel.getId()).requestAsync(mCancelConfirmedReminderCallback);

    }

    public interface OnActionListener {
        void onViewTaobaoDetail(String url);

        void onRemind(long notifyTime, String name, String id);

        void onCancelReminder(String id);
    }

}
