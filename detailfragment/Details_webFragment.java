package com.loqunbai.android.detailfragment;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.TextView;

import com.loqunbai.android.actionhelper.listener.OnAetailsActionListener;
import com.loqunbai.android.commonresource.constants.Constants;
import com.loqunbai.android.fragment.R;

/**
 * Created by lm on 15/10/28.
 */
public class Details_webFragment extends Fragment {
    private ImageButton mIbtnBack;
    private TextView mTvTitle;
    //淘宝的webview页面
    private WebView mWebView;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_details_web, container, false);
        setView(view);
        init();
        return view;
    }

    private void setView(View view) {
        mIbtnBack = (ImageButton) view.findViewById(R.id.ibtn_back);
        mTvTitle = (TextView) view.findViewById(R.id.tv_title);
        mWebView = (WebView) view.findViewById(R.id.wv_taobao);

        mTvTitle.setText(R.string.www_detail);

        mIbtnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().finish();
            }
        });
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
            }
        });
        WebSettings webSettings = mWebView.getSettings();
        //支持js
        webSettings.setJavaScriptEnabled(true);
        //支持对网页缩放
        webSettings.setSupportZoom(true);
        //支持android4.0
        webSettings.setBuiltInZoomControls(true);

    }

    public interface OnActionListener extends OnAetailsActionListener {
        void getUrl(String url);

    }


    private void init() {
        Bundle bundle = getArguments();
        String url = bundle.getString(Constants.DRESS_TAOBAO_URL);
        mWebView.loadUrl(url);
    }
}
