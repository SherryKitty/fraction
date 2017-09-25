package com.loqunbai.android.detailfragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.loqunbai.android.actionhelper.listener.OnListCommentActionListener;
import com.loqunbai.android.commonresource.AppData;
import com.loqunbai.android.commonresource.SpipeData;
import com.loqunbai.android.commonresource.constants.Constants;
import com.loqunbai.android.commonresource.dialogactivity.PreviewToOfficialAlertDialogActivity;
import com.loqunbai.android.commonresource.push.JPushUtils;
import com.loqunbai.android.commonresource.utils.StringUtils;
import com.loqunbai.android.commonresource.utils.share.ShareController;
import com.loqunbai.android.commonresource.widget.FloatMessageDialog;
import com.loqunbai.android.commonresource.widget.ProcessingDialog;
import com.loqunbai.android.detailfragment.adapter.ShowCommentAdapter;
import com.loqunbai.android.detailfragment.dialog.DetailMoreActionDialog;
import com.loqunbai.android.detailfragment.view.GoodsHeaderView;
import com.loqunbai.android.fragment.R;
import com.loqunbai.android.models.DressingCommentModel;
import com.loqunbai.android.models.DressingCommentResultModel;
import com.loqunbai.android.models.FavAction;
import com.loqunbai.android.models.FavType;
import com.loqunbai.android.models.PostResultModel;
import com.loqunbai.android.models.UpcomingItemModel;
import com.loqunbai.android.utils.controller.FavListUpcomingController;
import com.loqunbai.android.utils.controller.FavLookupController;
import com.loqunbai.android.utils.controller.GoodsCommentListController;
import com.loqunbai.android.utils.controller.RemindLookupController;
import com.loqunbai.android.utils.controller.RequestParameterController;
import com.loqunbai.android.utils.controller.cacheupdater.AbsCacheUpdater;
import com.loqunbai.android.utils.controller.cacheupdater.ExistCacheUpdater;
import com.loqunbai.android.utils.controller.cacheupdater.ListCacheUpdater;
import com.loqunbai.android.utils.controller.controllercore.PagingListControllerCore;
import com.loqunbai.android.utils.sdk.AbstractRequestController;
import com.loqunbai.android.utils.sdk.comment.DressingCommentDeleteRequest;
import com.loqunbai.android.utils.sdk.comment.GoodsCommentsCountRequest;
import com.loqunbai.android.utils.sdk.daliylookrepo.DressRemindRequest;
import com.loqunbai.android.utils.sdk.daliylookrepo.DressUnRemindRequest;
import com.loqunbai.android.utils.sdk.daliylookrepo.FavPostRequest;
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
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Sherry on 15/11/2.
 */
public class DressDetailFragment extends Fragment {

    private ImageButton mIbtnBack;
    private TextView mTvTitle;
    private ListView mListView;

    //定金或全款
    private View mFav, mFavOther;
    private View mBuy, mReminder, mRemindWhenConfirm;
    private View mComment, mCommentOther;
    private ImageButton mIvShare;

    private TextView mTvFav, mTvFavOther;
    private TextView mTvBuy, mTvReminder, mTvRemindWhenConfirm;
    private View mBottomChina, mBottomOther, mBottomDeposit;
    private TextView mTvComment, mTvCommentOther;

    //尾款
    private ImageButton mIbtnFav, mIbtnRemainReminder, mIbtnComment, mIbtnBuy;
    private View mMask;

    private ImageSize mImageSize = new ImageSize(100, 100);
    private ImageLoader mImageLoader;
    private SimpleImageLoadingListener mSimpleImageLoadingListener;
    private OnActionListener mOnActionListener;

    private GoodsHeaderView mGoodsHeaderView;
    private ShowCommentAdapter mShowCommentAdapter;
    protected boolean isProcessingData = false;
    protected boolean hasMoreData = false;
    protected ArrayList<DressingCommentModel> mDressingCommentModelArr = new ArrayList<>();

    private UpcomingItemModel mUpcomingItemModel;
    private String mShareImgUrl;
//    private String str_http = null;
//    private String strwww = null;
    private int deleteCommentPosition;

    private ProcessingDialog mUploadProcessingDialog;

    //Main for alarm
    private String mItemId;
    private boolean isSetRemind;

    private PagingListControllerCore.Callback<DressingCommentModel> mDressCommentCallback;
    private AbstractRequestController.AsyncCallback<DressingCommentResultModel> mDressingCommentResultModelAsyncCallback;

    private AbsCacheUpdater.Callback<UpcomingItemModel> mUpcomingItemModelCallback
            = new AbsCacheUpdater.Callback<UpcomingItemModel>() {
        @Override
        public void getResult(UpcomingItemModel result) {
            if ((result != null)
                    && (!StringUtils.isEmpty(result.getId()))
                    && !result.mark ) {

//                mUpcomingItemModel = result;
//                mGoodsHeaderView.update(result);
//                init();

                if( mUpcomingItemModel == null ){
                    mUpcomingItemModel = result;
                    initHeaderView();
                    init();
                }else{
                    mUpcomingItemModel = result;
                    updateHeaderView();
                }


            } else {
                final FloatMessageDialog floatDialog = new FloatMessageDialog(getActivity());
                floatDialog.setResId(R.drawable.float_msg_empty);
                floatDialog.setMessage(getString(R.string.goods_is_deleted));
                floatDialog.show();
                Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        if (floatDialog != null && floatDialog.isShowing()) {
                            floatDialog.dismiss();
                        }
                        if (getActivity() != null
                                && !getActivity().isFinishing()) {
                            getActivity().finish();
                        }
                    }
                }, 1000);

            }

            /*TODO: old
            mUpcomingItemModel = result;
            if (mUpcomingItemModel != null) {
                if (!mUpcomingItemModel.mark) {
                    init();
                } else {
                    //TODO: has deleted
                    getActivity().finish();
                }
            }
            */
        }

        @Override
        public void getError(Exception exception) {
            exception.printStackTrace();
        }
    };

    private AbstractRequestController.AsyncCallback<PostResultModel>
            mDressingCommentDeleteCallback = new AbstractRequestController.AsyncCallback<PostResultModel>() {
        @Override
        public void getResult(PostResultModel result) {
            if( result.status == 0 ) {
                DressingCommentModel dressingCommentModel = mDressingCommentModelArr.get(deleteCommentPosition);
                GoodsCommentListController.getGoodsCommentListController().removeCommentById(
                        dressingCommentModel.feedid,
                        dressingCommentModel.getId()
                );
                mDressingCommentModelArr.remove(deleteCommentPosition);
                mShowCommentAdapter.removeData(deleteCommentPosition);

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

            if (getActivity() != null
                    && !getActivity().isFinishing()){
                final FloatMessageDialog floatDialog = new FloatMessageDialog(getActivity());

                if (result.status == 0) {

                    Log.d("Remind", "Remind success");
                    if (mUpcomingItemModel.type.equals(Constants.DRESS_CONFIRMMED)) {
                        //正式 定金
    //                    if( isOfficial ) {
                        //设置尾款提醒
                        floatDialog.setMessage(getString(R.string.set_remain_reminder_success));
    //                    }else{
    //                        //设置开拍提醒
    //                        floatDialog.setMessage(getString(R.string.set_deposit_start_success));
    //                    }
                        if (getActivity() != null
                                && !getActivity().isFinishing()) {
                            floatDialog.setResId(R.drawable.float_msg_empty);
                            floatDialog.show();
                            Timer timer = new Timer();
                            timer.schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    if (floatDialog != null && floatDialog.isShowing()) {
                                        floatDialog.dismiss();
                                    }
                                }
                            }, 1000);

                            mIbtnRemainReminder.setSelected(true);
                        }
                    } else {
                        //图透 设置动态提醒
                        if (getActivity() != null
                                && !getActivity().isFinishing()) {
                            floatDialog.setResId(R.drawable.float_msg_reminder_success);
                            floatDialog.show();
                            Timer timer = new Timer();
                            timer.schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    if (floatDialog != null && floatDialog.isShowing()) {
                                        floatDialog.dismiss();
                                    }
                                }
                            }, 1000);

                            mTvRemindWhenConfirm.setText(R.string.has_set);
                            mTvRemindWhenConfirm.setSelected(true);
                        }
                    }

                    ExistCacheUpdater.RemindListUpdater.localUpdateAsync(
                            mUpcomingItemModel.getId(), true);
                    isSetRemind = true;
                }

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
            if (getActivity() != null
                    && !getActivity().isFinishing()) {
                final FloatMessageDialog floatDialog = new FloatMessageDialog(getActivity());
                if (!StringUtils.isEmpty(mUpcomingItemModel.ordertype)
                        && mUpcomingItemModel.ordertype.equals(Constants.ORDERTYPE_DEPOSIT)) {
                    //定金设置开拍提醒
                    floatDialog.setResId(R.drawable.float_msg_empty);
                    floatDialog.setMessage(getString(R.string.set_deposit_start_success));
                } else {
                    floatDialog.setResId(R.drawable.float_msg_reminder_success);
                }
                if (result.status == 0) {
                    if (getActivity() != null
                            && !getActivity().isFinishing()) {
                        floatDialog.show();
                        Timer timer = new Timer();
                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                if (floatDialog != null && floatDialog.isShowing()) {
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
                if( mUpcomingItemModel.type.equals(Constants.DRESS_CONFIRMMED) ){
                    mIbtnRemainReminder.setSelected(false);
                }else{
                    mTvRemindWhenConfirm.setText(R.string.remind_when_confirm);
                    mTvRemindWhenConfirm.setSelected(false);
                }

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
            if (id == mBuy.getId() || id == mIbtnBuy.getId()) {
                viewTaobaoDetail();
                MobclickAgent.onEvent(getActivity(), "goods_buy");
            } else if (id == mIbtnBack.getId()) {
                getActivity().finish();
            } else if (id == mIvShare.getId()) {
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
//                    if( !StringUtils.isEmpty(mUpcomingItemModel.ordertype)
//                            && mUpcomingItemModel.ordertype.equals(ORDERTYPE_DEPOSIT) ){
//                        //定金开拍，服务器提醒
//                        setConfirmedRemind();
//                    }else {
                        setStartRemind();
//                    }
                }


            } else if (id == mRemindWhenConfirm.getId()
                    || id == mIbtnRemainReminder.getId() ) {
                if (SpipeData.getInst().isLogin()) {

                    if (isSetRemind) {
                        cancelConfirmedRemind();
                    } else {
                        setConfirmedRemind();
                    }
                }
            }
            else if (id == mFav.getId()) {
                MobclickAgent.onEvent(getActivity(), "goods_collect");
                onFav(mTvFav);
            } else if (id == mFavOther.getId()) {
                onFav(mTvFavOther);
            } else if( id == mIbtnFav.getId() ){
                onFav(mIbtnFav);
            } else if( id == mComment.getId() || id == mCommentOther.getId() || id == mIbtnComment.getId() ){
                mOnActionListener.onComment(mUpcomingItemModel.getId(), Constants.TAB_GOODS_DETAIL);
            } else if( id == mMask.getId() ){
                mMask.setVisibility(View.GONE);
                AppData.getInst().setDressDepositeGuideShowed(true);
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
        View view = inflater.inflate(R.layout.fragment_dress_detail, container, false);
        initImageLoader();

        setView(view);

        if (mUpcomingItemModel !=
                null) {
            mItemId = mUpcomingItemModel.getId();
            initHeaderView();
            init();

//            UpcomingItemRequest.startQuery(RequestParameterController.getInstance().getUrl(),
//                    mItemId).requestAsync(mUpcomingItemModelCallback);
            ListCacheUpdater.upcomingCacheUpdater.remoteUpdateAsync(
                    mItemId, mUpcomingItemModelCallback);
        }else if (!StringUtils.isEmpty(mItemId)) {
            ListCacheUpdater.upcomingCacheUpdater.remoteUpdateAsync(
                    mItemId, mUpcomingItemModelCallback);
        }

//        if (mUpcomingItemModel != null) {
//            initHeaderView();
//            init();
//        } else if (!StringUtils.isEmpty(mItemId)) {
//            UpcomingItemRequest.startQuery(RequestParameterController.getInstance().getUrl(),
//                    mItemId).requestAsync(mUpcomingItemModelCallback);
//        }

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
        mListView = (ListView) view.findViewById(R.id.lv_main);

        mFav = view.findViewById(R.id.fav);
        mFavOther = view.findViewById(R.id.fav_other);
        mBuy = view.findViewById(R.id.buy);
        mReminder = view.findViewById(R.id.reminder);
        mRemindWhenConfirm = view.findViewById(R.id.reminder_when_confirm);
        mComment = view.findViewById(R.id.comment);
        mCommentOther = view.findViewById(R.id.comment_other);
        mIvShare = (ImageButton)view.findViewById(R.id.iv_share);

        mTvFav = (TextView) view.findViewById(R.id.tv_fav);
        mTvFavOther = (TextView) view.findViewById(R.id.tv_fav_other);
        mTvBuy = (TextView) view.findViewById(R.id.tv_buy);
        mTvReminder = (TextView) view.findViewById(R.id.tv_reminder);
        mTvRemindWhenConfirm = (TextView) view.findViewById(R.id.tv_reminder_when_confirm);
        mBottomChina = view.findViewById(R.id.chinese_bottom);
        mBottomOther = view.findViewById(R.id.other_bottom);
        mBottomDeposit = view.findViewById(R.id.deposit_bottom);
        mTvComment = (TextView) view.findViewById(R.id.tv_comment);
        mTvCommentOther = (TextView) view.findViewById(R.id.tv_comment_other);

        mIbtnFav = (ImageButton) view.findViewById(R.id.ibtn_fav);
        mIbtnRemainReminder = (ImageButton) view.findViewById(R.id.ibtn_reminder);
        mIbtnComment = (ImageButton) view.findViewById(R.id.ibtn_comment);
        mIbtnBuy = (ImageButton) view.findViewById(R.id.ibtn_buy);
        mMask = view.findViewById(R.id.mask);

        mTvTitle.setText(R.string.goods_detail);

        mIbtnBack.setOnClickListener(mOnClickListener);

        mFav.setOnClickListener(mOnClickListener);
        mFavOther.setOnClickListener(mOnClickListener);
        mBuy.setOnClickListener(mOnClickListener);
        mReminder.setOnClickListener(mOnClickListener);
        mRemindWhenConfirm.setOnClickListener(mOnClickListener);
        mComment.setOnClickListener(mOnClickListener);
        mCommentOther.setOnClickListener(mOnClickListener);
        mIvShare.setOnClickListener(mOnClickListener);

        mIbtnFav.setOnClickListener(mOnClickListener);
        mIbtnRemainReminder.setOnClickListener(mOnClickListener);
        mIbtnComment.setOnClickListener(mOnClickListener);
        mIbtnBuy.setOnClickListener(mOnClickListener);
        mMask.setOnClickListener(mOnClickListener);
////////////////////////////////////////
//        mTvBuy.setOnClickListener(mOnClickListener);
//        mTvShare.setOnClickListener(mOnClickListener);
//        mTvReminder.setOnClickListener(mOnClickListener);
        //Remind when it's confirmed
//        mTvRemindWhenConfirm.setOnClickListener(mOnClickListener);
//        mTvFav.setOnClickListener(mOnClickListener);
//        mTvFavOther.setOnClickListener(mOnClickListener);



        mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {

            }

            @Override
            public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if ((mShowCommentAdapter != null)
                        && (hasMoreData)
                        && !isProcessingData) {
                    if ((firstVisibleItem + visibleItemCount == totalItemCount) && totalItemCount > 0) {

                        loadData();

                    }
                }
            }
        });

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                DressingCommentModel model = (DressingCommentModel) adapterView.getAdapter().getItem(i);
                if (model != null) {
                    mOnActionListener.onComment(mUpcomingItemModel.getId(), model.getId(), model.username,
                            model.userid);
                }
            }
        });

        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long id) {
                if (mDressingCommentModelArr.size() > 0) {
                    final int position = i - 1;
                    DressingCommentModel model = (DressingCommentModel) mShowCommentAdapter.getItem(position);
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setItems(R.array.popup_msg_arr, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String[] PK = getResources().getStringArray(
                                    R.array.popup_msg_arr);
                            if (PK[which].equals(getString(R.string.delete))) {
                                if (position >= 0) {
                                    deleteCommentItem(position);
                                }
                            }
                            //复制功能
                            if (PK[which].equals(getString(R.string.copy))) {
                                mShowCommentAdapter.copy(mDressingCommentModelArr.get(position).content.toString());
                            }
                        }

                    });
//                    try {
//                        str_http = mDressingCommentModelArr.get(position).content.substring(0, 4);
//                        strwww = mDressingCommentModelArr.get(position).content.substring(0, 3);
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
                    AlertDialog.Builder builder1 = new AlertDialog.Builder(getActivity());
                    builder1.setItems(R.array.popup_msg_copy, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String[] PK = getResources().getStringArray(
                                    R.array.popup_msg_copy);
                            //复制功能
                            if (PK[which].equals(getString(R.string.copy))) {
                                mShowCommentAdapter.copy(mDressingCommentModelArr.get(position).content.toString());
                            }
                        }
                    });

                    if ((!StringUtils.isEmpty(model.getId()))
                            && SpipeData.getInst().getUserInfo().getUid().equals(model.getUserid())) {
                        builder.show();
                    } else {
                        builder1.show();
                    }
                }
                return true;
            }
        });
    }

    private void deleteCommentItem(int position) {
        deleteCommentPosition = position;
        if( !StringUtils.isEmpty(mDressingCommentModelArr.get(position).getId()) ) {
            DressingCommentDeleteRequest.startQuery(RequestParameterController.getInstance().getUrl(),
                    RequestParameterController.getInstance().getToken(),
                    mDressingCommentModelArr.get(position).getId(), "dress").requestAsync(
                    mDressingCommentDeleteCallback);
        }else{

        }
    }

    protected void setAdapter() {
        if (getActivity() != null) {
            mShowCommentAdapter = new ShowCommentAdapter(getActivity(), mDressingCommentModelArr);
            mListView.setAdapter(mShowCommentAdapter);
        }
    }

    private void initHeaderView() {
        if( isAdded() ) {
            mGoodsHeaderView = new GoodsHeaderView(getActivity(), mListView, mUpcomingItemModel, mOnActionListener);
            mListView.addHeaderView(mGoodsHeaderView.getView(), null, false);
            setAdapter();
        }
    }

    private void updateHeaderView(){
        if( mGoodsHeaderView != null ){
            mGoodsHeaderView.update(mUpcomingItemModel);
        }
    }

    private void init() {

        if( AppData.getInst().isDressDepositeGuideShowed() ){

            mMask.setVisibility(View.GONE);
        }else{
            if( mUpcomingItemModel != null
                    && !StringUtils.isEmpty(mUpcomingItemModel.ordertype)
                    && mUpcomingItemModel.ordertype.equals(Constants.ORDERTYPE_DEPOSIT)){

                mMask.setVisibility(View.VISIBLE);

            }else{
                mMask.setVisibility(View.GONE);
            }

        }

        if ( mUpcomingItemModel.images != null
                && mUpcomingItemModel.images.size() > 0) {
            mShareImgUrl = mUpcomingItemModel.images.get(0);
        }

        String clock = mUpcomingItemModel.clock;
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        boolean isFaved = FavLookupController.getInstance().isItemFavored(mUpcomingItemModel.getId());
        isSetRemind = RemindLookupController.getInstance().isItemSetRemind(mUpcomingItemModel.getId());

        if ( !StringUtils.isEmpty(mUpcomingItemModel.brand)
                && mUpcomingItemModel.brand.equals(Constants.BRAND_CHINA)) {
                mBottomChina.setVisibility(View.VISIBLE);
                mBottomOther.setVisibility(View.GONE);
                mBottomDeposit.setVisibility(View.GONE);
                if (mUpcomingItemModel.type.equals(Constants.DRESS_CONFIRMMED)) {
                    //正式
                    if (!StringUtils.isEmpty(clock)) {
                        try {
                            Date saleDate = format.parse(clock);
                            Long onSaleTime = saleDate.getTime();
                            Date curDate = new Date();
                            Long curTime = curDate.getTime();

                            if (onSaleTime <= curTime) {//比现在时间早
                                if( !StringUtils.isEmpty(mUpcomingItemModel.ordertype)
                                        && mUpcomingItemModel.ordertype.equals(Constants.ORDERTYPE_DEPOSIT) ) {
                                    //定金
                                    mBottomChina.setVisibility(View.GONE);
                                    mBottomOther.setVisibility(View.GONE);
                                    mBottomDeposit.setVisibility(View.VISIBLE);

                                    if( isSetRemind ){
                                        mIbtnRemainReminder.setSelected(true);
                                    }else{
                                        mIbtnRemainReminder.setSelected(false);
                                    }

                                }else{
                                    //尾款或全款
                                    mBuy.setVisibility(View.VISIBLE);
                                    mReminder.setVisibility(View.GONE);
                                    mRemindWhenConfirm.setVisibility(View.GONE);

                                }
                            } else {//比现在时间晚
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
                    //图透
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

                mIbtnFav.setSelected(isFaved);
                mFav.setSelected(isFaved);
                if (isFaved) {
                    mTvFav.setText(R.string.fav_done);
                } else {
                    mTvFav.setText(R.string.fav);
                }
//            }else{

//
//            }
        } else {
            mBottomChina.setVisibility(View.GONE);
            mBottomOther.setVisibility(View.VISIBLE);
            mBottomDeposit.setVisibility(View.GONE);

            mFavOther.setSelected(isFaved);
            if (isFaved) {
                mTvFavOther.setText(R.string.fav_done);
            } else {
                mTvFavOther.setText(R.string.fav);
            }
        }

        initLoadComments();
//        updateCommentsCount();

    }

    private void initLoadComments(){
        isProcessingData = true;
        mDressCommentCallback = new PagingListControllerCore.Callback<DressingCommentModel>() {
            @Override
            public void getResult(PagingListControllerCore.Response<DressingCommentModel> result) {
                isProcessingData = false;
                if( result.wholeList != null ) {
                    mDressingCommentModelArr = result.wholeList;
                }
                if (mShowCommentAdapter != null) {
                    mShowCommentAdapter.notifyDataSetChanged();
                }

                setAdapter();

                if (result.tailNewItems.size() != 0) {
                    hasMoreData = true;
                } else {
                    hasMoreData = false;
                }
            }

            @Override
            public void getError(Exception exception) {
                isProcessingData = false;
                if (getActivity() != null) {
                    Toast.makeText(getActivity(), exception.toString(), Toast.LENGTH_SHORT).show();
                }
                exception.printStackTrace();
            }
        };
        GoodsCommentListController.getGoodsCommentListController().getNewComments(
                mItemId, mDressCommentCallback);

    }


    private void updateCommentsCount(){

        mDressingCommentResultModelAsyncCallback = new AbstractRequestController.AsyncCallback<DressingCommentResultModel>() {
            @Override
            public void getResult(DressingCommentResultModel result) {
                if( result.getStatus() == 0 ){
                    mGoodsHeaderView.updateCommentNum(result.data.count);
                }else{

                }
            }

            @Override
            public void getError(Exception exception) {
                exception.printStackTrace();
            }
        };

        GoodsCommentsCountRequest.startQuery(RequestParameterController.getInstance().getUrl(),
                mItemId).requestAsync(mDressingCommentResultModelAsyncCallback);
    }

    private void loadData() {

        isProcessingData = true;
        mDressCommentCallback = new PagingListControllerCore.Callback<DressingCommentModel>() {
            @Override
            public void getResult(PagingListControllerCore.Response<DressingCommentModel> result) {
                isProcessingData = false;
                mDressingCommentModelArr.addAll(result.tailNewItems);
                mShowCommentAdapter.addData(result.tailNewItems);

                if( result.tailNewItems.size() != 0 ){
                    hasMoreData = true;
                }else{
                    hasMoreData = false;
                }
            }

            @Override
            public void getError(Exception exception) {
                isProcessingData = false;
                if( getActivity() != null ) {
                    Toast.makeText(getActivity(), exception.toString(), Toast.LENGTH_SHORT).show();
                }
                exception.printStackTrace();
            }
        };
        GoodsCommentListController.getGoodsCommentListController().getNewComments(
                mItemId, mDressCommentCallback);

    }

    public void refreshData(){

        isProcessingData = true;
        mDressCommentCallback = new PagingListControllerCore.Callback<DressingCommentModel>() {
            @Override
            public void getResult(PagingListControllerCore.Response<DressingCommentModel> result) {
                isProcessingData = false;
                if( result.headNewItems != null ) {
                    mDressingCommentModelArr.addAll(0, result.headNewItems);
                }
                if( result.tailNewItems != null ) {
                    mDressingCommentModelArr.addAll(result.tailNewItems);
                }
                if( result.headNewItems != null ) {
                    mShowCommentAdapter.addData(0, result.headNewItems);
                }
                if( result.tailNewItems != null ) {
                    mShowCommentAdapter.addData(result.tailNewItems);
                }

                if( result.tailNewItems.size() != 0 ){
                    hasMoreData = true;
                }else{
                    hasMoreData = false;
                }

                updateCommentsCount();

            }

            @Override
            public void getError(Exception exception) {
                isProcessingData = false;
                if( getActivity() != null ) {
                    Toast.makeText(getActivity(), exception.toString(), Toast.LENGTH_SHORT).show();
                }
                exception.printStackTrace();
            }
        };
        GoodsCommentListController.getGoodsCommentListController().getNewComments(
                mItemId, mDressCommentCallback);

//        updateCommentsCount();
    }


    private String getCopyLink() {
        if( mUpcomingItemModel != null ) {
            String link = "http://" + RequestParameterController.getInstance().getUrl()
                    + "/dress.html?id=" + mUpcomingItemModel.getId();

            return link;
        }else{
            return "";
        }
    }

    private void onFav(View view) {
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
            if( view.getClass() == TextView.class ){
                ((TextView)view).setText(R.string.fav);
            }
        } else {
            favAction = FavAction.FAV;
            if( view.getClass() == TextView.class ){
                ((TextView)view).setText(R.string.fav_done);
            }
        }

        final String id = mUpcomingItemModel.getId();
        ExistCacheUpdater.FavListUpdater.localUpdateAsync(
                id, !isFaved);

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
                        AppData.getInst().showFloatDialog(getActivity(), getString(R.string.grassed), 1000);

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

    private void viewTaobaoDetail() {
        String url = mUpcomingItemModel.taobaolink;
        if (!StringUtils.isEmpty(url)) {
            mOnActionListener.onViewTaobaoDetail(url);
        }
    }

    public interface OnActionListener extends OnListCommentActionListener {
        void onViewTaobaoDetail(String url);

        void onRemind(long notifyTime, String name, String id);

        void onCancelReminder(String id);

        void onComment(String feedId, String refId, String toUserName, String toUserId);
    }
}
