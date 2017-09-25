package com.loqunbai.android.detailfragment;

import android.widget.Toast;

import com.loqunbai.android.actionhelper.LikeCommentFavHelper;
import com.loqunbai.android.commonresource.AppData;
import com.loqunbai.android.commonresource.utils.StringUtils;
import com.loqunbai.android.detailfragment.view.DailyLookDetailHeaderView;
import com.loqunbai.android.fragment.R;
import com.loqunbai.android.models.DressingCommentModel;
import com.loqunbai.android.models.DressingItemModel;
import com.loqunbai.android.models.PostResultModel;
import com.loqunbai.android.utils.controller.DressingCommentListController;
import com.loqunbai.android.utils.controller.RequestParameterController;
import com.loqunbai.android.utils.controller.cacheupdater.ListCacheUpdater;
import com.loqunbai.android.utils.controller.controllercore.PagingListControllerCore;
import com.loqunbai.android.utils.sdk.AbstractRequestController;
import com.loqunbai.android.utils.sdk.comment.DressingCommentDeleteRequest;
import com.loqunbai.android.utils.sdk.daliylookrepo.DressingDeleteRequest;

/**
 * Created by user on 15/8/4.
 * 装扮详情
 */
public class DailyLookDetailFragment extends BaseDetailFragment {
    private DailyLookDetailHeaderView mDailyLookDetailHeaderView;

    private AbstractRequestController.AsyncCallback<PostResultModel>
            mDressingItemDeleteCallback = new AbstractRequestController.AsyncCallback<PostResultModel>() {
        @Override
        public void getResult(PostResultModel result) {
            if( result.status == 0 ){
                mDressingItemModel.mark = true;
                ListCacheUpdater.dressupCacheUpdater.localUpdateAsync(mDressingItemModel.getId(), mDressingItemModel);
                AppData.getInst().notifyAllOnRemoveUserPostListeners(mDressingItemModel.getId());

                if( isAdded() && getActivity() != null ) {
                    getActivity().finish();
                }

            }else{

            }
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
                DressingCommentListController.getDressUpCommentListController().removeCommentById(
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

    @Override
    protected void loadDetail() {

        ListCacheUpdater.dressupCacheUpdater.remoteUpdateAsync(
                mItemId,mDressingItemCallback);
    }

    @Override
    protected void initHeaderView() {
        if( isAdded() ) {
            mDailyLookDetailHeaderView = new DailyLookDetailHeaderView(getActivity(), mListView, mDressingItemModel);
            mListView.addHeaderView(mDailyLookDetailHeaderView.getView(), null, false);
            setAdapter();
        }
    }

    @Override
    protected void init(){
        super.init();
        mLikeCommentFavHelper = new LikeCommentFavHelper( getActivity(),
                mLick, mComment, mFav, LikeCommentFavHelper.ViewType.DAILYLOOK, mOnActionListener);
        mLikeCommentFavHelper.setDressingItemModel(mDressingItemModel);
    }

    @Override
    protected void loadData() {

        isProcessingData = true;
        mDressingCommentCallback = new PagingListControllerCore.Callback<DressingCommentModel>() {
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
        DressingCommentListController.getDressUpCommentListController().getNewComments(
                mDressingItemModel.getId(),
                mDressingCommentCallback);

    }

    @Override
    protected void deleteCommentItem(int position) {
        deleteCommentPosition = position;
        if( !StringUtils.isEmpty(mDressingCommentModelArr.get(position).getId()) ) {
            DressingCommentDeleteRequest.startQuery(RequestParameterController.getInstance().getUrl(),
                    RequestParameterController.getInstance().getToken(),
                    mDressingCommentModelArr.get(position).getId(),"feed").requestAsync(
                    mDressingCommentDeleteCallback);
        }else{

        }
    }

    @Override
    protected void deleteCurrentItem() {
        DressingDeleteRequest.startQuery(RequestParameterController.getInstance().getUrl(),
                RequestParameterController.getInstance().getToken(),
                mDressingItemModel.getId()).requestAsync(mDressingItemDeleteCallback);
    }

    @Override
    protected void updateHeaderView( DressingItemModel dressingItemModel) {

        mDailyLookDetailHeaderView.update(dressingItemModel);
    }

    @Override
    protected int getTitle() {
        return R.string.dailylook_detail;
    }

    @Override
    protected void updateDetail() {
        ListCacheUpdater.dressupCacheUpdater.localUpdateAsync(
                mItemId,
                mDressingItemModel
        );
    }


}
