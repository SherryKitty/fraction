package com.loqunbai.android.detailfragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.loqunbai.android.actionhelper.LikeCommentFavHelper;
import com.loqunbai.android.actionhelper.listener.OnAetailsActionListener;
import com.loqunbai.android.actionhelper.listener.OnListCommentActionListener;
import com.loqunbai.android.commonresource.AppData;
import com.loqunbai.android.commonresource.SpipeData;
import com.loqunbai.android.commonresource.constants.Constants;
import com.loqunbai.android.commonresource.utils.StringUtils;
import com.loqunbai.android.commonresource.utils.TimeShowStyleUtil;
import com.loqunbai.android.commonresource.widget.FloatMessageDialog;
import com.loqunbai.android.detailfragment.adapter.ShowCommentAdapter;
import com.loqunbai.android.fragment.R;
import com.loqunbai.android.models.DressingCommentModel;
import com.loqunbai.android.models.DressingItemModel;
import com.loqunbai.android.utils.controller.DressingCommentListController;
import com.loqunbai.android.utils.controller.cacheupdater.AbsCacheUpdater;
import com.loqunbai.android.utils.controller.controllercore.PagingListControllerCore;

import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by user on 15/8/10.
 * （装扮和repo）详情页面————基类，
 */
public abstract class BaseDetailFragment extends Fragment implements OnAetailsActionListener {

    private Button btn_report;
    private ImageButton mIbtnBack;
    private ImageButton mIbtnMore;
    protected TextView mTvTitle;
    protected ListView mListView;
    protected View mLick, mComment, mFav;

    protected ShowCommentAdapter mShowCommentAdapter;
    protected DressingItemModel mDressingItemModel;
    protected ArrayList<DressingCommentModel> mDressingCommentModelArr = new ArrayList<>();
//    private PopupMenu mPopupMenu;

    private View contentView;
    protected OnActionListener mOnActionListener;

    protected boolean isProcessingData = false;
    protected boolean hasMoreData = false;
    protected String mItemId;
    protected LikeCommentFavHelper mLikeCommentFavHelper;
    protected int deleteCommentPosition;

    private PopupWindow actionPopup;
    private FloatMessageDialog floatDialog;

    protected PagingListControllerCore.Callback<DressingCommentModel> mDressingCommentCallback;


    protected AbsCacheUpdater.Callback<DressingItemModel> mDressingItemCallback
            = new AbsCacheUpdater.Callback<DressingItemModel>() {
        @Override
        public void getResult(DressingItemModel result) {
            if ((result != null)
                    && (!StringUtils.isEmpty(result.getId()))) {
                mDressingItemModel = result;
                AppData.getInst().notifyDressupItemChanged(mDressingItemModel);

                updateHeaderView(result);
                /*TODO:
                initHeaderView();
                init();
                */
            } else {
                //It has been deleted
                floatDialog = new FloatMessageDialog(getActivity());
                floatDialog.setResId(R.drawable.float_msg_empty);
                floatDialog.setMessage(getString(R.string.dailylook_deleted_tips));
                floatDialog.show();
                Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        if( floatDialog != null && floatDialog.isShowing()){
                            floatDialog.dismiss();
                        }
                        if( getActivity() != null
                                && !getActivity().isFinishing() ) {
                            getActivity().finish();
                        }
                    }
                }, 1000);

            }

        }

        @Override
        public void getError(Exception exception) {
            exception.printStackTrace();
        }
    };

    @Override
    public void getUrl(String url) {
        url = mShowCommentAdapter.mUrl;
    }

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        mDressingItemModel = getArguments().getParcelable(Constants.DRESSUPITEM_DETAIL);
        mItemId = getArguments().getString(Constants.DRESSUPITEM_DETAIL_ID);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_base_detail, container, false);
        contentView = inflater.inflate(R.layout.popup_menu, null);

        setView(rootView);

        if (mDressingItemModel != null) {
            mItemId = mDressingItemModel.getId();
            initHeaderView();
            init();

            loadDetail();
        }

        return rootView;
    }


    @Override
    public void onStart() {
        super.onStart();

//        if (mDressingItemModel == null
//                && (!StringUtils.isEmpty(mItemId))) {
//            loadDetail();
//        }
    }

    @Override
    public void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        dismissPopupWindow();
        //if(popupWindow != null)
    }

    //判断PopupWindow是不是存在，存在就把它dismiss掉
    private void dismissPopupWindow() {

        if (actionPopup != null && actionPopup.isShowing()) {
            actionPopup.dismiss();
            actionPopup = null;
        }
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

    @Override
    public void onDestroy(){
        if( floatDialog != null && floatDialog.isShowing()){
            floatDialog.dismiss();
        }
        super.onDestroy();

    }

    protected abstract void loadDetail();

    protected abstract void initHeaderView();

    protected abstract void updateHeaderView(DressingItemModel dressingItemModel);

    protected abstract int getTitle();

    protected abstract void updateDetail();

    abstract protected void loadData();

    abstract protected void deleteCommentItem(int position);

    abstract protected void deleteCurrentItem();

    String str_http = null;
    String strwww = null;

    private void setView(final View view) {

        mIbtnBack = (ImageButton) view.findViewById(R.id.ibtn_back);
        mIbtnMore = (ImageButton) view.findViewById(R.id.ibtn_more);
        mTvTitle = (TextView) view.findViewById(R.id.tv_title);
        mListView = (ListView) view.findViewById(R.id.lv_main);
        mLick = view.findViewById(R.id.like);
        mComment = view.findViewById(R.id.comment);
        mFav = view.findViewById(R.id.fav);
        mTvTitle.setText(getTitle());

        mIbtnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().finish();
            }
        });

        //举报按钮
        mIbtnMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if( !StringUtils.isEmpty(mDressingItemModel.getId()) ) {
                    myPopup(mIbtnMore);
                }
            }
        });

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
                    mOnActionListener.onComment(mDressingItemModel.getId(), model.getId(), model.username,
                            model.userid);
                }
            }
        });


        //长按删除
        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                 @Override
                 public boolean onItemLongClick(AdapterView<?> parent, View view, int i, long id) {
                     if (mDressingCommentModelArr.size() > 0) {
                         final int position = i - 1;
                         DressingCommentModel model = mDressingCommentModelArr.get(position);
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
                                     Log.i("测试", mDressingCommentModelArr.get(position).content.toString());
                                 }
                             }

                         });
                         try {
                             str_http = mDressingCommentModelArr.get(position).content.substring(0, 4);
                             strwww = mDressingCommentModelArr.get(position).content.substring(0, 3);
                         } catch (Exception e) {
                             Log.i("字数不够!", "");
                         }
                         AlertDialog.Builder builder1 = new AlertDialog.Builder(getActivity());
                         builder1.setItems(R.array.popup_msg_copy, new DialogInterface.OnClickListener() {
                             @Override
                             public void onClick(DialogInterface dialog, int which) {
                                 String[] PK = getResources().getStringArray(
                                         R.array.popup_msg_copy);
                                 //复制功能
                                 if (PK[which].equals(getString(R.string.copy))) {
                                     mShowCommentAdapter.copy(mDressingCommentModelArr.get(position).content.toString());
                                     Log.i("测试", mDressingCommentModelArr.get(position).content.toString());
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
             }

        );


        //右上角，举报...
        /*
        mPopupMenu = new PopupMenu(getActivity(), mIbtnMore);
        mPopupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {

                int id = item.getItemId();

                if (id == R.id.report) {

                    mOnActionListener.onReport(mDressingItemModel.getId(), mDressingItemModel.userid);
                } else if (id == R.id.delete) {
                    deleteCurrentItem();
                }
                return true;
            }
        });
        */
    }


    protected void setAdapter() {
        if (getActivity() != null) {
            mShowCommentAdapter = new ShowCommentAdapter(getActivity(), mDressingCommentModelArr);
            mListView.setAdapter(mShowCommentAdapter);
        }
    }



    protected void init() {

//        String uId = mDressingItemModel.userid;
//
//        if (uId.equals(SpipeData.getInst().getUserInfo().getId())) {
//
//            //删除
//
//            contentView = LayoutInflater.from(getActivity()).inflate(
//                    R.layout.popup_menu, null);
//
//        } else {
//            //举报
//            contentView = LayoutInflater.from(getActivity()).inflate(
//                    R.layout.popup_menu, null);
//
//        }

        isProcessingData = true;
        mDressingCommentCallback = new PagingListControllerCore.Callback<DressingCommentModel>() {
            @Override
            public void getResult(PagingListControllerCore.Response<DressingCommentModel> result) {
                isProcessingData = false;
                mDressingCommentModelArr = result.wholeList;
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

        if( !mDressingItemModel.isLocal ) {
            DressingCommentListController.getDressUpCommentListController().getNewComments(
                    mItemId, mDressingCommentCallback);
        }


    }

    public void addRealTimeComment(String content, String toUsername) {

        DressingCommentModel dressingCommentModel = new DressingCommentModel();
        dressingCommentModel.userheader = SpipeData.getInst().getUserInfo().getHeader();
        dressingCommentModel.username = SpipeData.getInst().getUserInfo().getName();
        dressingCommentModel.content = content;
        dressingCommentModel.toname = toUsername;
        dressingCommentModel.ctime = TimeShowStyleUtil.getUTCTypeTime(getActivity(), new Date());

        mDressingCommentModelArr.add(0, dressingCommentModel);
        mShowCommentAdapter.setData(mDressingCommentModelArr);
        mDressingItemModel.comments += 1;

        AppData.getInst().notifyDressupItemChanged(mDressingItemModel);

        updateHeaderView(mDressingItemModel);
        updateDetail();
    }

    public interface OnActionListener extends OnListCommentActionListener {
        void onComment(String feedId, String refId, String toUserName, String toUserId);

        void onReport(String feedId, String userId);

        void onLogin();

    }


    /**
     * 打开popupMeun
     *
     * @param imageView
     */
    private void myPopup(ImageView imageView) {
        // 一个自定义的布局，作为显示的内容
//        contentView = LayoutInflater.from(getActivity()).inflate(
//                R.layout.popup_menu, null);
        // 设置按钮的点击事件
        btn_report = (Button) contentView.findViewById(R.id.btn_report);
        String uId = mDressingItemModel.userid;

        if (!uId.equals(SpipeData.getInst().getUserInfo().getUid())) {
            btn_report.setTag("举报");
            btn_report.setBackgroundResource(R.drawable.action_pop_report);

        } else {
            btn_report.setBackgroundResource(R.drawable.action_pop_delete);
            btn_report.setTag("删除");
        }

        btn_report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (btn_report.getTag().equals("删除")) {
                    //删除
                    deleteCurrentItem();
                } else if (btn_report.getTag().equals("举报")) {
                    //举报
                    mOnActionListener.onReport(mDressingItemModel.getId(), mDressingItemModel.userid);
                }
            }
        });

        if(actionPopup == null ){
            actionPopup = new PopupWindow(contentView,
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        }

        actionPopup.setTouchable(true);
        actionPopup.setTouchInterceptor(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return false;
                // 这里如果返回true的话，touch事件将被拦截
                // 拦截后 PopupWindow的onTouchEvent不被调用，这样点击外部区域无法dismiss
            }
        });
        // 如果不设置PopupWindow的背景，无论是点击外部区域还是Back键都无法dismiss弹框
        // 我觉得这里是API的一个bug
        actionPopup.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        actionPopup.showAsDropDown(imageView);
    }

}
