package com.loqunbai.android.detailfragment.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.loqunbai.android.commonresource.AppData;
import com.loqunbai.android.fragment.R;

/**
 * Created by user on 15/8/13.
 * （上新）商品详情页面--分享界面
 */
public class DetailMoreActionDialog extends Dialog {

    public static final int WEIXIN_FRIEND_ACTION = 0;
    public static final int QQ_FRIEND_ACTION = 1;
    public static final int WEIBO_ACTION = 2;
    public static final int COPYLINK_ACTION = 3;
    public static final int CANCEL_ACTION = 4;

    public static final int[] mActionId = new int[]{
            WEIXIN_FRIEND_ACTION,
            QQ_FRIEND_ACTION,
            WEIBO_ACTION,
            COPYLINK_ACTION
    };

    private Context mContext;

    private Button mBtnShareToQQ;
    private Button mBtnShareToWeixin;
    private Button mBtnShareToWeibo;
    private Button mBtnCopylink;
    private Button mBtnCancel;
    private ImageView ivBorderLace;

    private IActionClickListener mIActionClickListener;

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            int id = view.getId();
            if (id == R.id.btn_share_qq) {
                mIActionClickListener.onActionClick(DetailMoreActionDialog.this, QQ_FRIEND_ACTION);
                DetailMoreActionDialog.this.dismiss();
            } else if (id == R.id.btn_share_weibo) {
                //微博分享
                mIActionClickListener.onActionClick(DetailMoreActionDialog.this, WEIBO_ACTION);
                DetailMoreActionDialog.this.dismiss();
            } else if (id == R.id.btn_share_weixin) {
                //微信分享
                mIActionClickListener.onActionClick(DetailMoreActionDialog.this, WEIXIN_FRIEND_ACTION);
                DetailMoreActionDialog.this.dismiss();
            } else if (id == R.id.btn_share_copylink) {
                mIActionClickListener.onActionClick(DetailMoreActionDialog.this, COPYLINK_ACTION);
                DetailMoreActionDialog.this.dismiss();
            } else if (id == R.id.btn_cancel) {
                DetailMoreActionDialog.this.dismiss();
            }
        }
    };

    public DetailMoreActionDialog(Context context, IActionClickListener actionClickListener) {
        super(context);
        mContext = context;
        mIActionClickListener = actionClickListener;
    }

    public DetailMoreActionDialog(Context context, int theme, IActionClickListener actionClickListener) {
        super(context, theme);
        mContext = context;
        mIActionClickListener = actionClickListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail_more_action_dialog);
        getWindow().setGravity(Gravity.BOTTOM);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        setCanceledOnTouchOutside(true);
        setViews();
        init();
    }

    private void setViews() {
        mBtnShareToQQ = (Button) findViewById(R.id.btn_share_qq);
        mBtnShareToWeibo = (Button) findViewById(R.id.btn_share_weibo);
        mBtnShareToWeixin = (Button) findViewById(R.id.btn_share_weixin);
        mBtnCopylink = (Button) findViewById(R.id.btn_share_copylink);
        mBtnCancel = (Button) findViewById(R.id.btn_cancel);
        ivBorderLace = (ImageView) findViewById(R.id.iv_border_lace);

        AppData.getInst().fixBackgroundRepeat(ivBorderLace);
    }

    private void init() {
        mBtnShareToQQ.setOnClickListener(mOnClickListener);
        mBtnShareToWeibo.setOnClickListener(mOnClickListener);
        mBtnShareToWeixin.setOnClickListener(mOnClickListener);
        mBtnCopylink.setOnClickListener(mOnClickListener);
        mBtnCancel.setOnClickListener(mOnClickListener);
    }

    public interface IActionClickListener {
        void onActionClick(DialogInterface dialog, int action);
    }

}
