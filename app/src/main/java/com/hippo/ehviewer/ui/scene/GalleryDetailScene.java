/*
 * Copyright 2016 Hippo Seven
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hippo.ehviewer.ui.scene;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.DownloadManager;
import android.app.assist.AssistContent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.Html;
import android.text.TextUtils;
import android.util.Pair;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.DrawableRes;
import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.transition.TransitionInflater;

import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.hippo.app.CheckBoxDialogBuilder;
import com.hippo.app.EditTextDialogBuilder;
import com.hippo.beerbelly.BeerBelly;
import com.hippo.ehviewer.AppConfig;
import com.hippo.ehviewer.EhApplication;
import com.hippo.ehviewer.EhDB;
import com.hippo.ehviewer.R;
import com.hippo.ehviewer.Settings;
import com.hippo.ehviewer.UrlOpener;
import com.hippo.ehviewer.client.EhCacheKeyFactory;
import com.hippo.ehviewer.client.EhClient;
import com.hippo.ehviewer.client.EhFilter;
import com.hippo.ehviewer.client.EhRequest;
import com.hippo.ehviewer.client.EhTagDatabase;
import com.hippo.ehviewer.client.EhUrl;
import com.hippo.ehviewer.client.EhUtils;
import com.hippo.ehviewer.client.data.GalleryComment;
import com.hippo.ehviewer.client.data.GalleryCommentList;
import com.hippo.ehviewer.client.data.GalleryDetail;
import com.hippo.ehviewer.client.data.GalleryInfo;
import com.hippo.ehviewer.client.data.GalleryTagGroup;
import com.hippo.ehviewer.client.data.ListUrlBuilder;
import com.hippo.ehviewer.client.data.PreviewSet;
import com.hippo.ehviewer.client.exception.NoHAtHClientException;
import com.hippo.ehviewer.client.parser.ArchiveParser;
import com.hippo.ehviewer.client.parser.RateGalleryParser;
import com.hippo.ehviewer.client.parser.TorrentParser;
import com.hippo.ehviewer.client.parser.VoteTagParser;
import com.hippo.ehviewer.dao.DownloadInfo;
import com.hippo.ehviewer.dao.Filter;
import com.hippo.ehviewer.gallery.EhGalleryProvider;
import com.hippo.ehviewer.gallery.GalleryProvider2;
import com.hippo.ehviewer.spider.SpiderDen;
import com.hippo.ehviewer.ui.CommonOperations;
import com.hippo.ehviewer.ui.GalleryActivity;
import com.hippo.ehviewer.ui.MainActivity;
import com.hippo.ehviewer.ui.annotation.WholeLifeCircle;
import com.hippo.ehviewer.widget.GalleryRatingBar;
import com.hippo.scene.Announcer;
import com.hippo.scene.SceneFragment;
import com.hippo.scene.TransitionHelper;
import com.hippo.text.URLImageGetter;
import com.hippo.unifile.UniFile;
import com.hippo.util.AppHelper;
import com.hippo.util.ClipboardUtil;
import com.hippo.util.ExceptionUtils;
import com.hippo.util.IoThreadPoolExecutor;
import com.hippo.util.ReadableTime;
import com.hippo.view.ViewTransition;
import com.hippo.widget.AutoWrapLayout;
import com.hippo.widget.LoadImageView;
import com.hippo.widget.ObservedTextView;
import com.hippo.widget.SimpleGridAutoSpanLayout;
import com.hippo.yorozuya.AssertUtils;
import com.hippo.yorozuya.FileUtils;
import com.hippo.yorozuya.IOUtils;
import com.hippo.yorozuya.IntIdGenerator;
import com.hippo.yorozuya.SimpleHandler;
import com.hippo.yorozuya.ViewUtils;
import com.hippo.yorozuya.collect.IntList;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

import okhttp3.HttpUrl;
import rikka.core.res.ResourcesKt;

public class GalleryDetailScene extends BaseScene implements View.OnClickListener,
        com.hippo.ehviewer.download.DownloadManager.DownloadInfoListener,
        View.OnLongClickListener {

    public final static String KEY_ACTION = "action";
    public static final String ACTION_GALLERY_INFO = "action_gallery_info";
    public static final String ACTION_GID_TOKEN = "action_gid_token";
    public static final String KEY_GALLERY_INFO = "gallery_info";
    public static final String KEY_GID = "gid";
    public static final String KEY_TOKEN = "token";
    public static final String KEY_PAGE = "page";
    private static final int REQUEST_CODE_COMMENT_GALLERY = 0;
    private static final int STATE_INIT = -1;
    private static final int STATE_NORMAL = 0;
    private static final int STATE_REFRESH = 1;
    private static final int STATE_REFRESH_HEADER = 2;
    private static final int STATE_FAILED = 3;
    private static final String KEY_GALLERY_DETAIL = "gallery_detail";
    private static final String KEY_REQUEST_ID = "request_id";
    private static final boolean TRANSITION_ANIMATION_DISABLED = true;
    /*---------------
     View life cycle
     ---------------*/
    @Nullable
    private TextView mTip;
    @Nullable
    private ViewTransition mViewTransition;
    // Header
    @Nullable
    private FrameLayout mHeader;
    @Nullable
    private View mColorBg;
    @Nullable
    private LoadImageView mThumb;
    @Nullable
    private TextView mTitle;
    @Nullable
    private TextView mUploader;
    @Nullable
    private TextView mCategory;
    @Nullable
    private ImageView mBackAction;
    @Nullable
    private ImageView mOtherActions;
    @Nullable
    private ViewGroup mActionGroup;
    @Nullable
    private TextView mDownload;
    @Nullable
    private TextView mRead;
    // Below header
    @Nullable
    private View mBelowHeader;
    // Info
    @Nullable
    private View mInfo;
    @Nullable
    private TextView mLanguage;
    @Nullable
    private TextView mPages;
    @Nullable
    private TextView mSize;
    @Nullable
    private TextView mPosted;
    @Nullable
    private TextView mFavoredTimes;
    @Nullable
    private TextView mNewerVersion;
    // Actions
    @Nullable
    private View mActions;
    @Nullable
    private TextView mRatingText;
    @Nullable
    private RatingBar mRating;
    @Nullable
    private View mHeartGroup;
    @Nullable
    private TextView mHeart;
    @Nullable
    private TextView mHeartOutline;
    @Nullable
    private TextView mTorrent;
    @Nullable
    private TextView mArchive;
    @Nullable
    private TextView mShare;
    @Nullable
    private View mRate;
    @Nullable
    private TextView mSimilar;
    @Nullable
    private TextView mSearchCover;
    // Tags
    @Nullable
    private LinearLayout mTags;
    @Nullable
    private TextView mNoTags;
    // Comments
    @Nullable
    private LinearLayout mComments;
    @Nullable
    private TextView mCommentsText;
    // Previews
    @Nullable
    private View mPreviews;
    @Nullable
    private SimpleGridAutoSpanLayout mGridLayout;
    @Nullable
    private TextView mPreviewText;
    // Progress
    @Nullable
    private View mProgress;
    @Nullable
    private ViewTransition mViewTransition2;
    @Nullable
    private PopupMenu mPopupMenu;
    @WholeLifeCircle
    private int mDownloadState;
    @Nullable
    private String mAction;
    @Nullable
    private GalleryInfo mGalleryInfo;
    private long mGid;
    private String mToken;
    private int mPage;
    @Nullable
    private GalleryDetail mGalleryDetail;
    private int mRequestId = IntIdGenerator.INVALID_ID;
    private List<TorrentParser.Result> mTorrentList;
    ActivityResultLauncher<String> requestStoragePermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            result -> {
                if (result && mGalleryDetail != null) {
                    TorrentListDialogHelper helper = new TorrentListDialogHelper();
                    Dialog dialog = new AlertDialog.Builder(requireActivity())
                            .setTitle(R.string.torrents)
                            .setView(R.layout.dialog_torrent_list)
                            .setOnDismissListener(helper)
                            .show();
                    helper.setDialog(dialog, mGalleryDetail.torrentUrl);
                }
            });
    private String mArchiveFormParamOr;
    private List<ArchiveParser.Archive> mArchiveList;
    @State
    private int mState = STATE_INIT;
    private boolean mModifyingFavorites;

    @Nullable
    private static String getArtist(GalleryTagGroup[] tagGroups) {
        if (null == tagGroups) {
            return null;
        }
        for (GalleryTagGroup tagGroup : tagGroups) {
            if ("artist".equals(tagGroup.groupName) && tagGroup.size() > 0) {
                return tagGroup.getTagAt(0);
            }
        }
        return null;
    }

    private static void deleteFileAsync(UniFile... files) {
        //noinspection deprecation
        new AsyncTask<UniFile, Void, Void>() {
            @Override
            protected Void doInBackground(UniFile... params) {
                for (UniFile file : params) {
                    if (file != null) {
                        file.delete();
                    }
                }
                return null;
            }
        }.executeOnExecutor(IoThreadPoolExecutor.getInstance(), files);
    }

    @StringRes
    private int getRatingText(float rating) {
        return switch (Math.round(rating * 2)) {
            case 0 -> R.string.rating0;
            case 1 -> R.string.rating1;
            case 2 -> R.string.rating2;
            case 3 -> R.string.rating3;
            case 4 -> R.string.rating4;
            case 5 -> R.string.rating5;
            case 6 -> R.string.rating6;
            case 7 -> R.string.rating7;
            case 8 -> R.string.rating8;
            case 9 -> R.string.rating9;
            case 10 -> R.string.rating10;
            default -> R.string.rating_none;
        };
    }

    private void handleArgs(Bundle args) {
        if (args == null) {
            return;
        }

        String action = args.getString(KEY_ACTION);
        mAction = action;
        if (ACTION_GALLERY_INFO.equals(action)) {
            mGalleryInfo = args.getParcelable(KEY_GALLERY_INFO);
            // Add history
            if (null != mGalleryInfo) {
                EhDB.putHistoryInfo(mGalleryInfo);
            }
        } else if (ACTION_GID_TOKEN.equals(action)) {
            mGid = args.getLong(KEY_GID);
            mToken = args.getString(KEY_TOKEN);
            mPage = args.getInt(KEY_PAGE);
        }
    }

    @Nullable
    private String getGalleryDetailUrl() {
        long gid;
        String token;
        if (mGalleryDetail != null) {
            gid = mGalleryDetail.gid;
            token = mGalleryDetail.token;
        } else if (mGalleryInfo != null) {
            gid = mGalleryInfo.gid;
            token = mGalleryInfo.token;
        } else if (ACTION_GID_TOKEN.equals(mAction)) {
            gid = mGid;
            token = mToken;
        } else {
            return null;
        }
        return EhUrl.getGalleryDetailUrl(gid, token, 0, false);
    }

    // -1 for error
    private long getGid() {
        if (mGalleryDetail != null) {
            return mGalleryDetail.gid;
        } else if (mGalleryInfo != null) {
            return mGalleryInfo.gid;
        } else if (ACTION_GID_TOKEN.equals(mAction)) {
            return mGid;
        } else {
            return -1;
        }
    }

    private String getUploader() {
        if (mGalleryDetail != null) {
            return mGalleryDetail.uploader;
        } else if (mGalleryInfo != null) {
            return mGalleryInfo.uploader;
        } else {
            return null;
        }
    }

    // Judging by the uploader to exclude the cooldown period
    private boolean getDisowned() {
        return getUploader().equals("(Disowned)");
    }

    // -1 for error
    private int getCategory() {
        if (mGalleryDetail != null) {
            return mGalleryDetail.category;
        } else if (mGalleryInfo != null) {
            return mGalleryInfo.category;
        } else {
            return -1;
        }
    }

    private GalleryInfo getGalleryInfo() {
        if (null != mGalleryDetail) {
            return mGalleryDetail;
        } else if (null != mGalleryInfo) {
            return mGalleryInfo;
        } else {
            return null;
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            onInit();
        } else {
            onRestore(savedInstanceState);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mRead != null) {
            try {
                GalleryProvider2 galleryProvider = new EhGalleryProvider(requireContext(), mGalleryInfo);
                galleryProvider.start();
                int startPage = galleryProvider.getStartPage();
                if (startPage != 0) {
                    mRead.setText(getString(R.string.read_from, startPage + 1));
                }
                galleryProvider.stop();
            } catch (Exception ignore) {

            }
        }
    }

    private void onInit() {
        handleArgs(getArguments());
    }

    private void onRestore(Bundle savedInstanceState) {
        mAction = savedInstanceState.getString(KEY_ACTION);
        mGalleryInfo = savedInstanceState.getParcelable(KEY_GALLERY_INFO);
        mGid = savedInstanceState.getLong(KEY_GID);
        mToken = savedInstanceState.getString(KEY_TOKEN);
        mGalleryDetail = savedInstanceState.getParcelable(KEY_GALLERY_DETAIL);
        mRequestId = savedInstanceState.getInt(KEY_REQUEST_ID);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mAction != null) {
            outState.putString(KEY_ACTION, mAction);
        }
        if (mGalleryInfo != null) {
            outState.putParcelable(KEY_GALLERY_INFO, mGalleryInfo);
        }
        outState.putLong(KEY_GID, mGid);
        if (mToken != null) {
            outState.putString(KEY_TOKEN, mAction);
        }
        if (mGalleryDetail != null) {
            outState.putParcelable(KEY_GALLERY_DETAIL, mGalleryDetail);
        }
        outState.putInt(KEY_REQUEST_ID, mRequestId);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Get download state
        long gid = getGid();
        if (gid != -1) {
            Context context = getContext();
            AssertUtils.assertNotNull(context);
            mDownloadState = EhApplication.getDownloadManager(context).getDownloadState(gid);
        } else {
            mDownloadState = DownloadInfo.STATE_INVALID;
        }

        View view = inflater.inflate(R.layout.scene_gallery_detail, container, false);

        ViewGroup main = (ViewGroup) ViewUtils.$$(view, R.id.main);
        ScrollView mainView = (ScrollView) ViewUtils.$$(main, R.id.scroll_view);
        mainView.setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            if (mActionGroup != null && mHeader != null) {
                //noinspection IntegerDivisionInFloatingPointContext
                setLightStatusBar(mActionGroup.getY() - mHeader.findViewById(R.id.header_content).getPaddingTop() / 2 < scrollY);
            }
        });
        View progressView = ViewUtils.$$(main, R.id.progress_view);
        mTip = (TextView) ViewUtils.$$(main, R.id.tip);
        mViewTransition = new ViewTransition(mainView, progressView, mTip);

        Context context = getContext();
        AssertUtils.assertNotNull(context);

        Drawable drawable = ContextCompat.getDrawable(context, R.drawable.big_sad_pandroid);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        mTip.setCompoundDrawables(null, drawable, null, null);
        mTip.setOnClickListener(this);

        mHeader = (FrameLayout) ViewUtils.$$(mainView, R.id.header);
        mColorBg = ViewUtils.$$(mHeader, R.id.color_bg);
        mThumb = (LoadImageView) ViewUtils.$$(mHeader, R.id.thumb);
        mTitle = (TextView) ViewUtils.$$(mHeader, R.id.title);
        mUploader = (TextView) ViewUtils.$$(mHeader, R.id.uploader);
        mCategory = (TextView) ViewUtils.$$(mHeader, R.id.category);
        mBackAction = (ImageView) ViewUtils.$$(mHeader, R.id.back_action);
        mOtherActions = (ImageView) ViewUtils.$$(mHeader, R.id.other_actions);
        mActionGroup = (ViewGroup) ViewUtils.$$(mHeader, R.id.action_card);
        mDownload = (TextView) ViewUtils.$$(mActionGroup, R.id.download);
        mRead = (TextView) ViewUtils.$$(mActionGroup, R.id.read);

        mUploader.setOnClickListener(this);
        mCategory.setOnClickListener(this);
        mBackAction.setOnClickListener(this);
        mOtherActions.setOnClickListener(this);
        mDownload.setOnClickListener(this);
        mDownload.setOnLongClickListener(this);
        mRead.setOnClickListener(this);

        mUploader.setOnLongClickListener(this);

        mBelowHeader = mainView.findViewById(R.id.below_header);
        View belowHeader = mBelowHeader;

        mInfo = ViewUtils.$$(belowHeader, R.id.info);
        mLanguage = (TextView) ViewUtils.$$(mInfo, R.id.language);
        mPages = (TextView) ViewUtils.$$(mInfo, R.id.pages);
        mSize = (TextView) ViewUtils.$$(mInfo, R.id.size);
        mPosted = (TextView) ViewUtils.$$(mInfo, R.id.posted);
        mFavoredTimes = (TextView) ViewUtils.$$(mInfo, R.id.favoredTimes);
        mInfo.setOnClickListener(this);

        mActions = ViewUtils.$$(belowHeader, R.id.actions);
        mNewerVersion = (TextView) ViewUtils.$$(mActions, R.id.newerVersion);
        mRatingText = (TextView) ViewUtils.$$(mActions, R.id.rating_text);
        mRating = (RatingBar) ViewUtils.$$(mActions, R.id.rating);
        mHeartGroup = ViewUtils.$$(mActions, R.id.heart_group);
        mHeart = (TextView) ViewUtils.$$(mHeartGroup, R.id.heart);
        mHeartOutline = (TextView) ViewUtils.$$(mHeartGroup, R.id.heart_outline);
        mTorrent = (TextView) ViewUtils.$$(mActions, R.id.torrent);
        mArchive = (TextView) ViewUtils.$$(mActions, R.id.archive);
        mShare = (TextView) ViewUtils.$$(mActions, R.id.share);
        mRate = ViewUtils.$$(mActions, R.id.rate);
        mSimilar = (TextView) ViewUtils.$$(mActions, R.id.similar);
        mSearchCover = (TextView) ViewUtils.$$(mActions, R.id.search_cover);
        mNewerVersion.setOnClickListener(this);
        mHeartGroup.setOnClickListener(this);
        mHeartGroup.setOnLongClickListener(this);
        mTorrent.setOnClickListener(this);
        mArchive.setOnClickListener(this);
        mShare.setOnClickListener(this);
        mRate.setOnClickListener(this);
        mSimilar.setOnClickListener(this);
        mSearchCover.setOnClickListener(this);
        ensureActionDrawable();

        mTags = (LinearLayout) ViewUtils.$$(belowHeader, R.id.tags);
        mNoTags = (TextView) ViewUtils.$$(mTags, R.id.no_tags);

        mComments = (LinearLayout) ViewUtils.$$(belowHeader, R.id.comments);
        if (Settings.getShowComments()) {
            mCommentsText = (TextView) ViewUtils.$$(mComments, R.id.comments_text);
            mComments.setOnClickListener(this);
        } else {
            mComments.setVisibility(View.GONE);
        }

        mPreviews = ViewUtils.$$(belowHeader, R.id.previews);
        mGridLayout = (SimpleGridAutoSpanLayout) ViewUtils.$$(mPreviews, R.id.grid_layout);
        mPreviewText = (TextView) ViewUtils.$$(mPreviews, R.id.preview_text);
        mPreviews.setOnClickListener(this);

        mProgress = ViewUtils.$$(mainView, R.id.progress);

        mViewTransition2 = new ViewTransition(mBelowHeader, mProgress);

        if (prepareData()) {
            if (mGalleryDetail != null) {
                bindViewSecond();
                setTransitionName();
                adjustViewVisibility(STATE_NORMAL, false);
            } else if (mGalleryInfo != null) {
                bindViewFirst();
                setTransitionName();
                adjustViewVisibility(STATE_REFRESH_HEADER, false);
            } else {
                adjustViewVisibility(STATE_REFRESH, false);
            }
        } else {
            mTip.setText(R.string.error_cannot_find_gallery);
            adjustViewVisibility(STATE_FAILED, false);
        }

        EhApplication.getDownloadManager(context).addDownloadInfoListener(this);

        return view;
    }

    @Override
    public boolean needWhiteStatusBar() {
        return false;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        Context context = getContext();
        AssertUtils.assertNotNull(context);
        EhApplication.getDownloadManager(context).removeDownloadInfoListener(this);

        mTip = null;
        mViewTransition = null;

        mHeader = null;
        mColorBg = null;
        mThumb = null;
        mTitle = null;
        mUploader = null;
        mCategory = null;
        mBackAction = null;
        mOtherActions = null;
        mActionGroup = null;
        mDownload = null;
        mRead = null;
        mBelowHeader = null;

        mInfo = null;
        mLanguage = null;
        mPages = null;
        mSize = null;
        mPosted = null;
        mFavoredTimes = null;

        mActions = null;
        mNewerVersion = null;
        mRatingText = null;
        mRating = null;
        mHeartGroup = null;
        mHeart = null;
        mHeartOutline = null;
        mTorrent = null;
        mArchive = null;
        mShare = null;
        mRate = null;
        mSimilar = null;
        mSearchCover = null;

        mTags = null;
        mNoTags = null;

        mComments = null;
        mCommentsText = null;

        mPreviews = null;
        mGridLayout = null;
        mPreviewText = null;

        mProgress = null;

        mViewTransition2 = null;

        mPopupMenu = null;
    }

    private boolean prepareData() {
        Context context = getContext();
        AssertUtils.assertNotNull(context);

        if (mGalleryDetail != null) {
            return true;
        }

        long gid = getGid();
        if (gid == -1) {
            return false;
        }

        // Get from cache
        mGalleryDetail = EhApplication.getGalleryDetailCache(context).get(gid);
        if (mGalleryDetail != null) {
            return true;
        }

        EhApplication application = (EhApplication) context.getApplicationContext();
        if (application.containGlobalStuff(mRequestId)) {
            // request exist
            return true;
        }

        // Do request
        return request();
    }

    private boolean request() {
        Context context = getContext();
        MainActivity activity = getMainActivity();
        String url = getGalleryDetailUrl();
        if (null == context || null == activity || null == url) {
            return false;
        }

        EhClient.Callback<GalleryDetail> callback = new GetGalleryDetailListener(context,
                activity.getStageId(), getTag());
        mRequestId = ((EhApplication) context.getApplicationContext()).putGlobalStuff(callback);
        EhRequest request = new EhRequest()
                .setMethod(EhClient.METHOD_GET_GALLERY_DETAIL)
                .setArgs(url)
                .setCallback(callback);
        EhApplication.getEhClient(context).execute(request);

        return true;
    }

    private void setActionDrawable(@Nullable TextView text, @DrawableRes int resId) {
        if (text == null) return;
        Context context = text.getContext();
        Drawable drawable = ContextCompat.getDrawable(context, resId);
        if (drawable == null) return;
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        text.setCompoundDrawables(null, drawable, null, null);
    }

    private void ensureActionDrawable() {
        setActionDrawable(mHeart, R.drawable.v_heart_primary_x48);
        setActionDrawable(mHeartOutline, R.drawable.v_heart_outline_primary_x48);
        setActionDrawable(mTorrent, R.drawable.v_utorrent_primary_x48);
        setActionDrawable(mArchive, R.drawable.v_archive_primary_x48);
        setActionDrawable(mShare, R.drawable.v_share_primary_x48);
        setActionDrawable(mSimilar, R.drawable.v_similar_primary_x48);
        setActionDrawable(mSearchCover, R.drawable.v_file_find_primary_x48);
    }

    private boolean createCircularReveal() {
        if (mColorBg == null) {
            return false;
        }

        int w = mColorBg.getWidth();
        int h = mColorBg.getHeight();
        if (ViewCompat.isAttachedToWindow(mColorBg) && w != 0 && h != 0) {
            Resources resources = getContext().getResources();
            int keylineMargin = resources.getDimensionPixelSize(R.dimen.keyline_margin);
            int thumbWidth = resources.getDimensionPixelSize(R.dimen.gallery_detail_thumb_width);
            int thumbHeight = resources.getDimensionPixelSize(R.dimen.gallery_detail_thumb_height);

            int x = thumbWidth / 2 + keylineMargin;
            int y = thumbHeight / 2 + keylineMargin;

            int radiusX = Math.max(Math.abs(x), Math.abs(w - x));
            int radiusY = Math.max(Math.abs(y), Math.abs(h - y));
            float radius = (float) Math.hypot(radiusX, radiusY);

            ViewAnimationUtils.createCircularReveal(mColorBg, x, y, 0, radius).setDuration(300).start();
            return true;
        } else {
            return false;
        }
    }

    private void adjustViewVisibility(int state, boolean animation) {
        if (state == mState) {
            return;
        }
        if (mViewTransition == null || mViewTransition2 == null) {
            return;
        }

        int oldState = mState;
        mState = state;

        animation = !TRANSITION_ANIMATION_DISABLED && animation;

        switch (state) {
            case STATE_NORMAL -> {
                setLightStatusBar(false);
                // Show mMainView
                mViewTransition.showView(0, animation);
                // Show mBelowHeader
                mViewTransition2.showView(0, animation);
            }
            case STATE_REFRESH -> {
                setLightStatusBar(true);
                // Show mProgressView
                mViewTransition.showView(1, animation);
            }
            case STATE_REFRESH_HEADER -> {
                setLightStatusBar(false);
                // Show mMainView
                mViewTransition.showView(0, animation);
                // Show mProgress
                mViewTransition2.showView(1, animation);
            }
            case STATE_INIT, STATE_FAILED -> {
                setLightStatusBar(true);
                // Show mFailedView
                mViewTransition.showView(2, animation);
            }
        }

        if ((oldState == STATE_INIT || oldState == STATE_FAILED || oldState == STATE_REFRESH) &&
                (state == STATE_NORMAL || state == STATE_REFRESH_HEADER) && ResourcesKt.resolveBoolean(getTheme(), androidx.appcompat.R.attr.isLightTheme, false)) {
            if (!createCircularReveal()) {
                SimpleHandler.getInstance().post(this::createCircularReveal);
            }
        }
    }

    private void bindViewFirst() {
        if (mGalleryDetail != null) {
            return;
        }
        if (mThumb == null || mTitle == null || mUploader == null || mCategory == null) {
            return;
        }

        if (ACTION_GALLERY_INFO.equals(mAction) && mGalleryInfo != null) {
            GalleryInfo gi = mGalleryInfo;
            mThumb.load(EhCacheKeyFactory.getThumbKey(gi.gid), gi.thumb);
            mTitle.setText(EhUtils.getSuitableTitle(gi));
            mUploader.setText(gi.uploader);
            mUploader.setAlpha(gi.disowned ? .5f : 1f);
            mCategory.setText(EhUtils.getCategory(gi.category));
            mCategory.setTextColor(EhUtils.getCategoryColor(gi.category));
            updateDownloadText();
        }
    }

    private void updateFavoriteDrawable() {
        GalleryDetail gd = mGalleryDetail;
        if (gd == null) {
            return;
        }
        if (mHeart == null || mHeartOutline == null) {
            return;
        }

        if (gd.isFavorited || EhDB.containLocalFavorites(gd.gid)) {
            mHeart.setVisibility(View.VISIBLE);
            if (gd.favoriteName == null) {
                mHeart.setText(R.string.local_favorites);
            } else {
                mHeart.setText(gd.favoriteName);
            }
            mHeartOutline.setVisibility(View.GONE);
        } else {
            mHeart.setVisibility(View.GONE);
            mHeartOutline.setVisibility(View.VISIBLE);
        }
    }

    private void bindViewSecond() {
        GalleryDetail gd = mGalleryDetail;
        if (gd == null) {
            return;
        }
        if (mPage != 0) {
            Snackbar.make(requireActivity().findViewById(R.id.snackbar), getString(R.string.read_from, mPage), Snackbar.LENGTH_LONG)
                    .setAction(R.string.read, v -> {
                        Intent intent = new Intent(requireContext(), GalleryActivity.class);
                        intent.setAction(GalleryActivity.ACTION_EH);
                        intent.putExtra(GalleryActivity.KEY_GALLERY_INFO, mGalleryDetail);
                        intent.putExtra(GalleryActivity.KEY_PAGE, mPage);
                        startActivity(intent);
                    })
                    .show();
        }
        if (mThumb == null || mTitle == null || mUploader == null || mCategory == null ||
                mLanguage == null || mPages == null || mSize == null || mPosted == null ||
                mFavoredTimes == null || mRatingText == null || mRating == null || mTorrent == null || mNewerVersion == null) {
            return;
        }

        Resources resources = getResources();

        mThumb.load(EhCacheKeyFactory.getThumbKey(gd.gid), gd.thumb);
        mTitle.setText(EhUtils.getSuitableTitle(gd));
        mUploader.setText(gd.uploader);
        mUploader.setAlpha(gd.disowned ? .5f : 1f);
        mCategory.setText(EhUtils.getCategory(gd.category));
        mCategory.setTextColor(EhUtils.getCategoryColor(gd.category));
        updateDownloadText();

        mLanguage.setText(gd.language);
        mPages.setText(resources.getQuantityString(
                R.plurals.page_count, gd.pages, gd.pages));
        mSize.setText(gd.size);
        mPosted.setText(gd.posted);
        mFavoredTimes.setText(resources.getString(R.string.favored_times, gd.favoriteCount));
        if (gd.newerVersions != null && gd.newerVersions.size() != 0) {
            mNewerVersion.setVisibility(View.VISIBLE);
        }

        mRatingText.setText(getAllRatingText(gd.rating, gd.ratingCount));
        mRating.setRating(gd.rating);

        updateFavoriteDrawable();

        mTorrent.setText(resources.getString(R.string.torrent_count, gd.torrentCount));

        bindTags(gd.tags);
        bindComments(gd.comments.comments);
        bindPreviews(gd);
    }

    private void bindTags(GalleryTagGroup[] tagGroups) {
        Context context = getContext();
        LayoutInflater inflater = getLayoutInflater();
        if (null == context || null == mTags || null == mNoTags) {
            return;
        }

        mTags.removeViews(1, mTags.getChildCount() - 1);
        if (tagGroups == null || tagGroups.length == 0) {
            mNoTags.setVisibility(View.VISIBLE);
            return;
        } else {
            mNoTags.setVisibility(View.GONE);
        }

        EhTagDatabase ehTags = Settings.getShowTagTranslations() ? EhTagDatabase.getInstance(context) : null;
        int colorTag = ResourcesKt.resolveColor(getTheme(), R.attr.tagBackgroundColor);
        int colorName = ResourcesKt.resolveColor(getTheme(), R.attr.tagGroupBackgroundColor);
        for (GalleryTagGroup tg : tagGroups) {
            LinearLayout ll = (LinearLayout) inflater.inflate(R.layout.gallery_tag_group, mTags, false);
            ll.setOrientation(LinearLayout.HORIZONTAL);
            mTags.addView(ll);

            String readableTagName = null;
            if (ehTags != null) {
                readableTagName = ehTags.getTranslation("n:" + tg.groupName);
            }

            TextView tgName = (TextView) inflater.inflate(R.layout.item_gallery_tag, ll, false);
            ll.addView(tgName);
            tgName.setText(readableTagName != null ? readableTagName : tg.groupName);
            tgName.setBackgroundTintList(ColorStateList.valueOf(colorName));

            String prefix = EhTagDatabase.namespaceToPrefix(tg.groupName);
            if (prefix == null) {
                prefix = "";
            }

            AutoWrapLayout awl = new AutoWrapLayout(context);
            ll.addView(awl, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            for (int j = 0, z = tg.size(); j < z; j++) {
                TextView tag = (TextView) inflater.inflate(R.layout.item_gallery_tag, awl, false);
                awl.addView(tag);
                String tagStr = tg.getTagAt(j);

                String readableTag = null;
                if (ehTags != null) {
                    readableTag = ehTags.getTranslation(prefix + tagStr);
                }

                tag.setText(readableTag != null ? readableTag : tagStr);
                tag.setBackgroundTintList(ColorStateList.valueOf(colorTag));
                tag.setTag(R.id.tag, tg.groupName + ":" + tagStr);
                tag.setOnClickListener(this);
                tag.setOnLongClickListener(this);
            }
        }
    }

    private void bindComments(GalleryComment[] comments) {
        Context context = getContext();
        LayoutInflater inflater = getLayoutInflater();
        if (null == context || null == mComments || null == mCommentsText) {
            return;
        }

        mComments.removeViews(0, mComments.getChildCount() - 1);

        final int maxShowCount = 2;
        if (comments == null || comments.length == 0) {
            mCommentsText.setText(R.string.no_comments);
            return;
        } else if (comments.length <= maxShowCount) {
            mCommentsText.setText(R.string.no_more_comments);
        } else {
            mCommentsText.setText(R.string.more_comment);
        }

        int length = Math.min(maxShowCount, comments.length);
        for (int i = 0; i < length; i++) {
            GalleryComment comment = comments[i];
            View v = inflater.inflate(R.layout.item_gallery_comment, mComments, false);
            mComments.addView(v, i);
            TextView user = v.findViewById(R.id.user);
            user.setText(comment.user);
            user.setBackgroundColor(Color.TRANSPARENT);
            TextView time = v.findViewById(R.id.time);
            time.setText(ReadableTime.getTimeAgo(comment.time));
            ObservedTextView c = v.findViewById(R.id.comment);
            c.setMaxLines(5);
            c.setText(Html.fromHtml(comment.comment, Html.FROM_HTML_MODE_LEGACY,
                    new URLImageGetter(c, EhApplication.getConaco(context)), null));
            v.setBackgroundColor(Color.TRANSPARENT);
        }
    }

    @SuppressLint("SetTextI18n")
    private void bindPreviews(GalleryDetail gd) {
        LayoutInflater inflater = getLayoutInflater();
        Resources resources = getResourcesOrNull();
        int previewNum = Settings.getPreviewNum();
        if (null == resources || null == mGridLayout || null == mPreviewText) {
            return;
        }

        mGridLayout.removeAllViews();
        PreviewSet previewSet = gd.previewSet;
        if (gd.previewPages <= 0 || previewSet == null || previewSet.size() == 0) {
            mPreviewText.setText(R.string.no_previews);
            return;
        } else if (gd.previewPages == 1 && previewSet.size() <= previewNum) {
            mPreviewText.setText(R.string.no_more_previews);
        } else {
            mPreviewText.setText(R.string.more_previews);
        }

        int columnWidth = Settings.getPreviewSize();
        mGridLayout.setColumnSize(columnWidth);
        mGridLayout.setStrategy(SimpleGridAutoSpanLayout.STRATEGY_SUITABLE_SIZE);
        int size = Math.min(previewSet.size(), previewNum);
        for (int i = 0; i < size; i++) {
            View view = inflater.inflate(R.layout.item_gallery_preview, mGridLayout, false);
            mGridLayout.addView(view);

            LoadImageView image = view.findViewById(R.id.image);
            previewSet.load(image, gd.gid, i);
            image.setTag(R.id.index, i);
            image.setOnClickListener(this);
            TextView text = view.findViewById(R.id.text);
            text.setText(Integer.toString(previewSet.getPosition(i) + 1));
        }
    }

    private String getAllRatingText(float rating, int ratingCount) {
        return getString(R.string.rating_text, getString(getRatingText(rating)), rating, ratingCount);
    }

    private void setTransitionName() {
        long gid = getGid();

        if (gid != -1 && mThumb != null &&
                mTitle != null && mUploader != null && mCategory != null) {
            ViewCompat.setTransitionName(mThumb, TransitionNameFactory.getThumbTransitionName(gid));
            ViewCompat.setTransitionName(mTitle, TransitionNameFactory.getTitleTransitionName(gid));
            ViewCompat.setTransitionName(mUploader, TransitionNameFactory.getUploaderTransitionName(gid));
            ViewCompat.setTransitionName(mCategory, TransitionNameFactory.getCategoryTransitionName(gid));
        }
    }

    private void ensurePopMenu() {
        if (mPopupMenu != null) {
            return;
        }

        Context context = getContext();
        AssertUtils.assertNotNull(context);
        PopupMenu popup = new PopupMenu(context, mOtherActions, Gravity.TOP);
        mPopupMenu = popup;
        popup.getMenuInflater().inflate(R.menu.scene_gallery_detail, popup.getMenu());
        popup.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.action_open_in_other_app) {
                String url = getGalleryDetailUrl();
                Activity activity = getMainActivity();
                if (null != url && null != activity) {
                    UrlOpener.openUrl(activity, url, false);
                }
            } else if (itemId == R.id.action_refresh) {
                if (mState != STATE_REFRESH && mState != STATE_REFRESH_HEADER) {
                    adjustViewVisibility(STATE_REFRESH, true);
                    request();
                }
            } else if (itemId == R.id.action_add_tag) {
                if (mGalleryDetail == null) {
                    return false;
                }
                if (mGalleryDetail.apiUid < 0) {
                    showTip(R.string.error_please_login_first, LENGTH_LONG);
                    return false;
                }
                EditTextDialogBuilder builder = new EditTextDialogBuilder(context, "", getString(R.string.action_add_tag_tip));
                builder.setPositiveButton(android.R.string.ok, null);
                AlertDialog dialog = builder.setTitle(R.string.action_add_tag)
                        .show();
                dialog.getButton(DialogInterface.BUTTON_POSITIVE)
                        .setOnClickListener(v -> {
                            voteTag(builder.getText().trim(), 1);
                            dialog.dismiss();
                        });
            }
            return true;
        });
    }

    private void showSimilarGalleryList() {
        GalleryDetail gd = mGalleryDetail;
        if (null == gd) {
            return;
        }
        String keyword = EhUtils.extractTitle(gd.title);
        if (null != keyword) {
            ListUrlBuilder lub = new ListUrlBuilder();
            lub.setMode(ListUrlBuilder.MODE_NORMAL);
            lub.setKeyword("\"" + keyword + "\"");
            GalleryListScene.startScene(this, lub);
            return;
        }
        String artist = getArtist(gd.tags);
        if (null != artist) {
            ListUrlBuilder lub = new ListUrlBuilder();
            lub.setMode(ListUrlBuilder.MODE_TAG);
            lub.setKeyword("artist:" + artist);
            GalleryListScene.startScene(this, lub);
            return;
        }
        if (null != gd.uploader) {
            ListUrlBuilder lub = new ListUrlBuilder();
            lub.setMode(ListUrlBuilder.MODE_UPLOADER);
            lub.setKeyword(gd.uploader);
            GalleryListScene.startScene(this, lub);
        }
    }

    private void showCoverGalleryList() {
        Context context = getContext();
        if (null == context) {
            return;
        }
        long gid = getGid();
        if (-1L == gid) {
            return;
        }
        File temp = AppConfig.createTempFile();
        if (null == temp) {
            return;
        }
        BeerBelly beerBelly = EhApplication.getConaco(context).getBeerBelly();

        OutputStream os = null;
        try {
            os = new FileOutputStream(temp);
            if (beerBelly.pullFromDiskCache(EhCacheKeyFactory.getThumbKey(gid), os)) {
                ListUrlBuilder lub = new ListUrlBuilder();
                lub.setMode(ListUrlBuilder.MODE_IMAGE_SEARCH);
                lub.setImagePath(temp.getPath());
                lub.setUseSimilarityScan(true);
                GalleryListScene.startScene(this, lub);
            }
        } catch (FileNotFoundException e) {
            // Ignore
        } finally {
            IOUtils.closeQuietly(os);
        }
    }

    @Override
    public void onClick(View v) {
        Context context = getContext();
        MainActivity activity = getMainActivity();
        if (null == context || null == activity) {
            return;
        }

        if (mTip == v) {
            if (request()) {
                adjustViewVisibility(STATE_REFRESH, true);
            }
        } else if (mBackAction == v) {
            onBackPressed();
        } else if (mOtherActions == v) {
            ensurePopMenu();
            if (mPopupMenu != null) {
                mPopupMenu.show();
            }
        } else if (mUploader == v) {
            String uploader = getUploader();
            if (TextUtils.isEmpty(uploader) || getDisowned()) {
                return;
            }
            ListUrlBuilder lub = new ListUrlBuilder();
            lub.setMode(ListUrlBuilder.MODE_UPLOADER);
            lub.setKeyword(uploader);
            GalleryListScene.startScene(this, lub);
        } else if (mCategory == v) {
            int category = getCategory();
            if (category == EhUtils.NONE || category == EhUtils.PRIVATE || category == EhUtils.UNKNOWN) {
                return;
            }
            ListUrlBuilder lub = new ListUrlBuilder();
            lub.setCategory(category);
            GalleryListScene.startScene(this, lub);
        } else if (mDownload == v) {
            GalleryInfo galleryInfo = getGalleryInfo();
            if (galleryInfo != null) {
                if (EhApplication.getDownloadManager(context).getDownloadState(galleryInfo.gid) == DownloadInfo.STATE_INVALID) {
                    CommonOperations.startDownload(activity, galleryInfo, false);
                } else {
                    CheckBoxDialogBuilder builder = new CheckBoxDialogBuilder(context,
                            getString(R.string.download_remove_dialog_message, galleryInfo.title),
                            getString(R.string.download_remove_dialog_check_text),
                            Settings.getRemoveImageFiles());
                    DeleteDialogHelper helper = new DeleteDialogHelper(
                            EhApplication.getDownloadManager(context), galleryInfo, builder);
                    builder.setTitle(R.string.download_remove_dialog_title)
                            .setPositiveButton(android.R.string.ok, helper)
                            .show();
                }
            }
        } else if (mRead == v) {
            GalleryInfo galleryInfo = null;
            if (mGalleryInfo != null) {
                galleryInfo = mGalleryInfo;
            } else if (mGalleryDetail != null) {
                galleryInfo = mGalleryDetail;
            }
            if (galleryInfo != null) {
                Intent intent = new Intent(activity, GalleryActivity.class);
                intent.setAction(GalleryActivity.ACTION_EH);
                intent.putExtra(GalleryActivity.KEY_GALLERY_INFO, galleryInfo);
                startActivity(intent);
            }
        } else if (mNewerVersion == v) {
            if (mGalleryDetail != null) {
                ArrayList<CharSequence> titles = new ArrayList<>();
                for (GalleryInfo newerVersion : mGalleryDetail.newerVersions) {
                    titles.add(getString(R.string.newer_version_title, newerVersion.title, newerVersion.posted));
                }
                new AlertDialog.Builder(requireContext())
                        .setItems(titles.toArray(new CharSequence[0]), (dialog, which) -> {
                            GalleryInfo newerVersion = mGalleryDetail.newerVersions.get(which);
                            Bundle args = new Bundle();
                            args.putString(GalleryDetailScene.KEY_ACTION, GalleryDetailScene.ACTION_GID_TOKEN);
                            args.putLong(GalleryDetailScene.KEY_GID, newerVersion.gid);
                            args.putString(GalleryDetailScene.KEY_TOKEN, newerVersion.token);
                            startScene(new Announcer(GalleryDetailScene.class).setArgs(args));
                        })
                        .show();
            }
        } else if (mInfo == v) {
            Bundle args = new Bundle();
            args.putParcelable(GalleryInfoScene.KEY_GALLERY_DETAIL, mGalleryDetail);
            startScene(new Announcer(GalleryInfoScene.class).setArgs(args));
        } else if (mHeartGroup == v) {
            if (mGalleryDetail != null && !mModifyingFavorites) {
                boolean remove = false;
                if (EhDB.containLocalFavorites(mGalleryDetail.gid) || mGalleryDetail.isFavorited) {
                    mModifyingFavorites = true;
                    CommonOperations.removeFromFavorites(activity, mGalleryDetail,
                            new ModifyFavoritesListener(context,
                                    activity.getStageId(), getTag(), true));
                    remove = true;
                }
                if (!remove) {
                    mModifyingFavorites = true;
                    CommonOperations.addToFavorites(activity, mGalleryDetail,
                            new ModifyFavoritesListener(context,
                                    activity.getStageId(), getTag(), false));
                }
                // Update UI
                updateFavoriteDrawable();
            }
        } else if (mShare == v) {
            String url = getGalleryDetailUrl();
            if (url != null) {
                AppHelper.share(activity, url);
            }
        } else if (mTorrent == v) {
            if (mGalleryDetail != null) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q && ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    requestStoragePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                } else {
                    TorrentListDialogHelper helper = new TorrentListDialogHelper();
                    Dialog dialog = new AlertDialog.Builder(context)
                            .setTitle(R.string.torrents)
                            .setView(R.layout.dialog_torrent_list)
                            .setOnDismissListener(helper)
                            .show();
                    helper.setDialog(dialog, mGalleryDetail.torrentUrl);
                }
            }
        } else if (mArchive == v) {
            if (mGalleryDetail == null) {
                return;
            }
            if (mGalleryDetail.apiUid < 0) {
                showTip(R.string.error_please_login_first, LENGTH_LONG);
                return;
            }
            ArchiveListDialogHelper helper = new ArchiveListDialogHelper();
            Dialog dialog = new AlertDialog.Builder(context)
                    .setTitle(R.string.settings_download)
                    .setView(R.layout.dialog_archive_list)
                    .setOnDismissListener(helper)
                    .show();
            helper.setDialog(dialog, mGalleryDetail.archiveUrl);
        } else if (mRate == v) {
            if (mGalleryDetail == null) {
                return;
            }
            if (mGalleryDetail.apiUid < 0) {
                showTip(R.string.error_please_login_first, LENGTH_LONG);
                return;
            }
            RateDialogHelper helper = new RateDialogHelper();
            Dialog dialog = new AlertDialog.Builder(context)
                    .setTitle(R.string.rate)
                    .setView(R.layout.dialog_rate)
                    .setNegativeButton(android.R.string.cancel, null)
                    .setPositiveButton(android.R.string.ok, helper)
                    .show();
            helper.setDialog(dialog, mGalleryDetail.rating);
        } else if (mSimilar == v) {
            showSimilarGalleryList();
        } else if (mSearchCover == v) {
            showCoverGalleryList();
        } else if (mComments == v) {
            if (mGalleryDetail == null) {
                return;
            }
            Bundle args = new Bundle();
            args.putLong(GalleryCommentsScene.KEY_API_UID, mGalleryDetail.apiUid);
            args.putString(GalleryCommentsScene.KEY_API_KEY, mGalleryDetail.apiKey);
            args.putLong(GalleryCommentsScene.KEY_GID, mGalleryDetail.gid);
            args.putString(GalleryCommentsScene.KEY_TOKEN, mGalleryDetail.token);
            args.putParcelable(GalleryCommentsScene.KEY_COMMENT_LIST, mGalleryDetail.comments);
            args.putParcelable(GalleryCommentsScene.KEY_GALLERY_DETAIL, mGalleryDetail);
            startScene(new Announcer(GalleryCommentsScene.class)
                    .setArgs(args)
                    .setRequestCode(this, REQUEST_CODE_COMMENT_GALLERY));
        } else if (mPreviews == v) {
            if (null != mGalleryDetail) {
                int scrollTo = 0;
                if (mGalleryDetail.previewSet != null) {
                    int previewNum = Settings.getPreviewNum();
                    if (previewNum < mGalleryDetail.previewSet.size()) {
                        scrollTo = previewNum;
                    } else if (mGalleryDetail.previewPages > 1) {
                        scrollTo = -1;
                    }
                }
                Bundle args = new Bundle();
                args.putParcelable(GalleryPreviewsScene.KEY_GALLERY_INFO, mGalleryDetail);
                args.putInt(GalleryPreviewsScene.KEY_SCROLL_TO, scrollTo);
                startScene(new Announcer(GalleryPreviewsScene.class).setArgs(args));
            }
        } else {
            Object o = v.getTag(R.id.tag);
            if (o instanceof String tag) {
                ListUrlBuilder lub = new ListUrlBuilder();
                lub.setMode(ListUrlBuilder.MODE_TAG);
                lub.setKeyword(tag);
                GalleryListScene.startScene(this, lub);
                return;
            }

            GalleryInfo galleryInfo = getGalleryInfo();
            o = v.getTag(R.id.index);
            if (null != galleryInfo && o instanceof Integer) {
                int index = (Integer) o;
                Intent intent = new Intent(context, GalleryActivity.class);
                intent.setAction(GalleryActivity.ACTION_EH);
                intent.putExtra(GalleryActivity.KEY_GALLERY_INFO, galleryInfo);
                intent.putExtra(GalleryActivity.KEY_PAGE, index);
                startActivity(intent);
            }
        }
    }

    private void showFilterUploaderDialog() {
        Context context = getContext();
        String uploader = getUploader();
        if (context == null || uploader == null) {
            return;
        }

        new AlertDialog.Builder(context)
                .setMessage(getString(R.string.filter_the_uploader, uploader))
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    Filter filter = new Filter();
                    filter.mode = EhFilter.MODE_UPLOADER;
                    filter.text = uploader;
                    EhFilter.getInstance().addFilter(filter);

                    showTip(R.string.filter_added, LENGTH_SHORT);
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void showFilterTagDialog(String tag) {
        Context context = getContext();
        if (context == null) {
            return;
        }

        new AlertDialog.Builder(context)
                .setMessage(getString(R.string.filter_the_tag, tag))
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    Filter filter = new Filter();
                    filter.mode = EhFilter.MODE_TAG;
                    filter.text = tag;
                    EhFilter.getInstance().addFilter(filter);

                    showTip(R.string.filter_added, LENGTH_SHORT);
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void showTagDialog(TextView tv, final String tag) {
        final Context context = getContext();
        if (null == context) {
            return;
        }
        String temp;
        int index = tag.indexOf(':');
        if (index >= 0) {
            temp = tag.substring(index + 1);
        } else {
            temp = tag;
        }
        final String tag2 = temp;

        List<String> menu = new ArrayList<>();
        final IntList menuId = new IntList();
        Resources resources = context.getResources();

        menu.add(resources.getString(android.R.string.copy));
        menuId.add(R.id.copy);
        if (!tag2.equals(tv.getText().toString())) {
            menu.add(resources.getString(R.string.copy_trans));
            menuId.add(R.id.copy_trans);
        }
        menu.add(resources.getString(R.string.show_definition));
        menuId.add(R.id.show_definition);
        menu.add(resources.getString(R.string.add_filter));
        menuId.add(R.id.add_filter);
        if (mGalleryDetail != null && mGalleryDetail.apiUid >= 0) {
            menu.add(resources.getString(R.string.tag_vote_up));
            menuId.add(R.id.vote_up);
            menu.add(resources.getString(R.string.tag_vote_down));
            menuId.add(R.id.vote_down);
        }

        new AlertDialog.Builder(context)
                .setTitle(tag)
                .setItems(menu.toArray(new String[0]), (dialog, which) -> {
                    if (which < 0 || which >= menuId.size()) {
                        return;
                    }
                    int id = menuId.get(which);
                    if (id == R.id.vote_up) {
                        voteTag(tag, 1);
                    } else if (id == R.id.vote_down) {
                        voteTag(tag, -1);
                    } else if (id == R.id.show_definition) {
                        UrlOpener.openUrl(context, EhUrl.getTagDefinitionUrl(tag2), false);
                    } else if (id == R.id.add_filter) {
                        showFilterTagDialog(tag);
                    } else if (id == R.id.copy) {
                        ClipboardUtil.addTextToClipboard(tag);
                        showTip(R.string.copied_to_clipboard, LENGTH_SHORT);
                    } else if (id == R.id.copy_trans) {
                        ClipboardUtil.addTextToClipboard(tv.getText().toString());
                        showTip(R.string.copied_to_clipboard, LENGTH_SHORT);
                    }
                }).show();
    }

    private void voteTag(String tag, int vote) {
        Context context = getContext();
        MainActivity activity = getMainActivity();
        if (null == context || null == activity) {
            return;
        }

        EhRequest request = new EhRequest()
                .setMethod(EhClient.METHOD_VOTE_TAG)
                .setArgs(mGalleryDetail.apiUid, mGalleryDetail.apiKey, mGalleryDetail.gid, mGalleryDetail.token, tag.replace("，", ","), vote)
                .setCallback(new VoteTagListener(context,
                        activity.getStageId(), getTag()));
        EhApplication.getEhClient(context).execute(request);
    }

    @Override
    public boolean onLongClick(View v) {
        MainActivity activity = getMainActivity();
        if (null == activity) {
            return false;
        }

        if (mUploader == v) {
            if (TextUtils.isEmpty(getUploader()) || getDisowned()) {
                return false;
            }
            showFilterUploaderDialog();
        } else if (mDownload == v) {
            GalleryInfo galleryInfo = getGalleryInfo();
            if (galleryInfo != null) {
                CommonOperations.startDownload(activity, galleryInfo, true);
            }
            return true;
        } else if (mHeartGroup == v) {
            if (mGalleryDetail != null && !mModifyingFavorites) {
                boolean removeOrEdit = false;
                if (EhDB.containLocalFavorites(mGalleryDetail.gid)) {
                    mModifyingFavorites = true;
                    CommonOperations.removeFromFavorites(activity, mGalleryDetail,
                            new ModifyFavoritesListener(activity,
                                    activity.getStageId(), getTag(), true));
                    removeOrEdit = true;
                } else if (mGalleryDetail.isFavorited) {
                    mModifyingFavorites = true;
                    CommonOperations.doAddToFavorites(activity, mGalleryDetail, mGalleryDetail.favoriteSlot,
                            new ModifyFavoritesListener(activity,
                                    activity.getStageId(), getTag(), false), true);
                    removeOrEdit = true;
                }
                if (!removeOrEdit) {
                    mModifyingFavorites = true;
                    CommonOperations.addToFavorites(activity, mGalleryDetail,
                            new ModifyFavoritesListener(activity,
                                    activity.getStageId(), getTag(), false), true);
                }
                // Update UI
                updateFavoriteDrawable();
            }
        } else {
            String tag = (String) v.getTag(R.id.tag);
            if (null != tag) {
                showTagDialog((TextView) v, tag);
                return true;
            }
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        if (mViewTransition != null && mThumb != null &&
                mViewTransition.getShownViewIndex() == 0 && mThumb.isShown()) {
            int[] location = new int[2];
            mThumb.getLocationInWindow(location);
            // Only show transaction when thumb can be seen
            if (location[1] + mThumb.getHeight() > 0) {
                setTransitionName();
                finish(new ExitTransaction(mThumb));
                return;
            }
        }
        finish();
    }

    @Override
    protected void onSceneResult(int requestCode, int resultCode, Bundle data) {
        if (requestCode == REQUEST_CODE_COMMENT_GALLERY) {
            if (resultCode != RESULT_OK || data == null) {
                return;
            }
            GalleryCommentList comments = data.getParcelable(GalleryCommentsScene.KEY_COMMENT_LIST);
            if (mGalleryDetail == null && comments == null) {
                return;
            }
            mGalleryDetail.comments = comments;
            bindComments(comments.comments);
        } else {
            super.onSceneResult(requestCode, resultCode, data);
        }
    }

    private void updateDownloadText() {
        if (null == mDownload) {
            return;
        }
        switch (mDownloadState) {
            case DownloadInfo.STATE_INVALID -> mDownload.setText(R.string.download);
            case DownloadInfo.STATE_NONE -> mDownload.setText(R.string.download_state_none);
            case DownloadInfo.STATE_WAIT -> mDownload.setText(R.string.download_state_wait);
            case DownloadInfo.STATE_DOWNLOAD ->
                    mDownload.setText(R.string.download_state_downloading);
            case DownloadInfo.STATE_FINISH -> mDownload.setText(R.string.download_state_downloaded);
            case DownloadInfo.STATE_FAILED -> mDownload.setText(R.string.download_state_failed);
        }
    }

    private void updateDownloadState() {
        Context context = getContext();
        long gid = getGid();
        if (null == context || -1L == gid) {
            return;
        }

        int downloadState = EhApplication.getDownloadManager(context).getDownloadState(gid);
        if (downloadState == mDownloadState) {
            return;
        }
        mDownloadState = downloadState;
        updateDownloadText();
    }

    @Override
    public void onAdd(@NonNull DownloadInfo info, @NonNull List<DownloadInfo> list, int position) {
        updateDownloadState();
    }

    @Override
    public void onUpdate(@NonNull DownloadInfo info, @NonNull List<DownloadInfo> list) {
        updateDownloadState();
    }

    @Override
    public void onUpdateAll() {
        updateDownloadState();
    }

    @Override
    public void onReload() {
        updateDownloadState();
    }

    @Override
    public void onChange() {
        updateDownloadState();
    }

    @Override
    public void onRemove(@NonNull DownloadInfo info, @NonNull List<DownloadInfo> list, int position) {
        updateDownloadState();
    }

    @Override
    public void onRenameLabel(String from, String to) {
    }

    @Override
    public void onUpdateLabels() {
    }

    private void onGetGalleryDetailSuccess(GalleryDetail result) {
        mGalleryDetail = result;
        updateDownloadState();
        adjustViewVisibility(STATE_NORMAL, true);
        bindViewSecond();
    }

    private void onGetGalleryDetailFailure(Exception e) {
        e.printStackTrace();
        Context context = getContext();
        if (null != context && null != mTip) {
            String error = ExceptionUtils.getReadableString(e);
            mTip.setText(error);
            adjustViewVisibility(STATE_FAILED, true);
        }
    }

    private void onRateGallerySuccess(RateGalleryParser.Result result) {
        if (mGalleryDetail != null) {
            mGalleryDetail.rating = result.rating;
            mGalleryDetail.ratingCount = result.ratingCount;
        }

        // Update UI
        if (mRatingText != null && mRating != null) {
            mRatingText.setText(getAllRatingText(result.rating, result.ratingCount));
            mRating.setRating(result.rating);
        }
    }

    private void onModifyFavoritesSuccess(boolean addOrRemove) {
        mModifyingFavorites = false;
        if (mGalleryDetail != null) {
            mGalleryDetail.isFavorited = !addOrRemove && mGalleryDetail.favoriteName != null;
            updateFavoriteDrawable();
        }
    }

    private void onModifyFavoritesFailure() {
        mModifyingFavorites = false;
    }

    private void onModifyFavoritesCancel() {
        mModifyingFavorites = false;
    }

    @Override
    public void onProvideAssistContent(AssistContent outContent) {
        super.onProvideAssistContent(outContent);

        String url = getGalleryDetailUrl();
        if (url != null) {
            outContent.setWebUri(Uri.parse(url));
        }
    }

    @IntDef({STATE_INIT, STATE_NORMAL, STATE_REFRESH, STATE_REFRESH_HEADER, STATE_FAILED})
    @Retention(RetentionPolicy.SOURCE)
    private @interface State {
    }

    private static class ExitTransaction implements TransitionHelper {

        private final View mThumb;

        public ExitTransaction(View thumb) {
            mThumb = thumb;
        }

        @Override
        public boolean onTransition(Context context,
                                    FragmentTransaction transaction, Fragment exit, Fragment enter) {
            if (!(enter instanceof GalleryListScene) && !(enter instanceof DownloadsScene) &&
                    !(enter instanceof FavoritesScene) && !(enter instanceof HistoryScene)) {
                return false;
            }

            String transitionName = ViewCompat.getTransitionName(mThumb);
            if (transitionName != null) {
                exit.setSharedElementReturnTransition(
                        TransitionInflater.from(context).inflateTransition(R.transition.trans_move));
                exit.setExitTransition(
                        TransitionInflater.from(context).inflateTransition(R.transition.trans_fade));
                enter.setSharedElementEnterTransition(
                        TransitionInflater.from(context).inflateTransition(R.transition.trans_move));
                enter.setEnterTransition(
                        TransitionInflater.from(context).inflateTransition(R.transition.trans_fade));
                transaction.addSharedElement(mThumb, transitionName);
            }
            return true;
        }
    }

    private static class GetGalleryDetailListener extends EhCallback<GalleryDetailScene, GalleryDetail> {

        public GetGalleryDetailListener(Context context, int stageId, String sceneTag) {
            super(context, stageId, sceneTag);
        }

        @Override
        public void onSuccess(GalleryDetail result) {
            getApplication().removeGlobalStuff(this);

            // Put gallery detail to cache
            EhApplication.getGalleryDetailCache(getApplication()).put(result.gid, result);

            // Add history
            EhDB.putHistoryInfo(result);

            // Notify success
            GalleryDetailScene scene = getScene();
            if (scene != null) {
                scene.onGetGalleryDetailSuccess(result);
            }
        }

        @Override
        public void onFailure(Exception e) {
            getApplication().removeGlobalStuff(this);
            GalleryDetailScene scene = getScene();
            if (scene != null) {
                scene.onGetGalleryDetailFailure(e);
            }
        }

        @Override
        public void onCancel() {
            getApplication().removeGlobalStuff(this);
        }

        @Override
        public boolean isInstance(SceneFragment scene) {
            return scene instanceof GalleryDetailScene;
        }
    }

    private static class VoteTagListener extends EhCallback<GalleryDetailScene, VoteTagParser.Result> {

        public VoteTagListener(Context context, int stageId, String sceneTag) {
            super(context, stageId, sceneTag);
        }

        @Override
        public void onSuccess(VoteTagParser.Result result) {
            if (!TextUtils.isEmpty(result.error)) {
                showTip(result.error, LENGTH_SHORT);
            } else {
                showTip(R.string.tag_vote_successfully, LENGTH_SHORT);
            }
        }

        @Override
        public void onFailure(Exception e) {
            showTip(R.string.vote_failed, LENGTH_LONG);
        }

        @Override
        public void onCancel() {
        }

        @Override
        public boolean isInstance(SceneFragment scene) {
            return scene instanceof GalleryDetailScene;
        }
    }

    private static class RateGalleryListener extends EhCallback<GalleryDetailScene, RateGalleryParser.Result> {

        private final long mGid;

        public RateGalleryListener(Context context, int stageId, String sceneTag, long gid) {
            super(context, stageId, sceneTag);
            mGid = gid;
        }

        @Override
        public void onSuccess(RateGalleryParser.Result result) {
            showTip(R.string.rate_successfully, LENGTH_SHORT);

            GalleryDetailScene scene = getScene();
            if (scene != null) {
                scene.onRateGallerySuccess(result);
            } else {
                // Update rating in cache
                GalleryDetail gd = EhApplication.getGalleryDetailCache(getApplication()).get(mGid);
                if (gd != null) {
                    gd.rating = result.rating;
                    gd.ratingCount = result.ratingCount;
                }
            }
        }

        @Override
        public void onFailure(Exception e) {
            e.printStackTrace();
            showTip(R.string.rate_failed, LENGTH_LONG);
        }

        @Override
        public void onCancel() {
        }

        @Override
        public boolean isInstance(SceneFragment scene) {
            return scene instanceof GalleryDetailScene;
        }
    }

    private static class ModifyFavoritesListener extends EhCallback<GalleryDetailScene, Void> {

        private final boolean mAddOrRemove;

        /**
         * @param addOrRemove false for add, true for remove
         */
        public ModifyFavoritesListener(Context context, int stageId, String sceneTag, boolean addOrRemove) {
            super(context, stageId, sceneTag);
            mAddOrRemove = addOrRemove;
        }

        @Override
        public void onSuccess(Void result) {
            showTip(mAddOrRemove ? R.string.remove_from_favorite_success :
                    R.string.add_to_favorite_success, LENGTH_SHORT);
            GalleryDetailScene scene = getScene();
            if (scene != null) {
                scene.onModifyFavoritesSuccess(mAddOrRemove);
            }
        }

        @Override
        public void onFailure(Exception e) {
            showTip(mAddOrRemove ? R.string.remove_from_favorite_failure :
                    R.string.add_to_favorite_failure, LENGTH_LONG);
            GalleryDetailScene scene = getScene();
            if (scene != null) {
                scene.onModifyFavoritesFailure();
            }
        }

        @Override
        public void onCancel() {
            GalleryDetailScene scene = getScene();
            if (scene != null) {
                scene.onModifyFavoritesCancel();
            }
        }

        @Override
        public boolean isInstance(SceneFragment scene) {
            return scene instanceof GalleryDetailScene;
        }
    }

    private static class DownloadArchiveListener extends EhCallback<GalleryDetailScene, String> {

        private final GalleryInfo mGalleryInfo;

        public DownloadArchiveListener(Context context, int stageId, String sceneTag, GalleryInfo galleryInfo) {
            super(context, stageId, sceneTag);
            mGalleryInfo = galleryInfo;
        }

        @Override
        public void onSuccess(String result) {
            if (result != null) {
                // TODO: Don't use buggy system download service
                DownloadManager.Request r = new DownloadManager.Request(Uri.parse(result));
                var name = mGalleryInfo.gid + "-" + EhUtils.getSuitableTitle(mGalleryInfo) + ".zip";
                r.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,
                        FileUtils.sanitizeFilename(name));
                r.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                DownloadManager dm = (DownloadManager) getApplication().getSystemService(Context.DOWNLOAD_SERVICE);
                if (dm != null) {
                    try {
                        dm.enqueue(r);
                    } catch (Throwable e) {
                        e.printStackTrace();
                        ExceptionUtils.throwIfFatal(e);
                    }
                }
            }
            showTip(R.string.download_archive_started, LENGTH_SHORT);
        }

        @Override
        public void onFailure(Exception e) {
            if (e instanceof NoHAtHClientException) {
                showTip(R.string.download_archive_failure_no_hath, LENGTH_LONG);
            } else {
                showTip(R.string.download_archive_failure, LENGTH_LONG);
            }
        }

        @Override
        public void onCancel() {
        }

        @Override
        public boolean isInstance(SceneFragment scene) {
            return scene instanceof GalleryDetailScene;
        }
    }

    private class ArchiveListDialogHelper implements AdapterView.OnItemClickListener,
            DialogInterface.OnDismissListener, EhClient.Callback<ArchiveParser.Result> {

        @Nullable
        private CircularProgressIndicator mProgressView;
        @Nullable
        private TextView mErrorText;
        @Nullable
        private ListView mListView;
        @Nullable
        private EhRequest mRequest;
        @Nullable
        private Dialog mDialog;

        public void setDialog(@Nullable Dialog dialog, String url) {
            mDialog = dialog;
            mProgressView = (CircularProgressIndicator) ViewUtils.$$(dialog, R.id.progress);
            mErrorText = (TextView) ViewUtils.$$(dialog, R.id.text);
            mListView = (ListView) ViewUtils.$$(dialog, R.id.list_view);
            mListView.setOnItemClickListener(this);

            Context context = getContext();
            if (context != null) {
                if (mArchiveList == null) {
                    mErrorText.setVisibility(View.GONE);
                    mListView.setVisibility(View.GONE);
                    mRequest = new EhRequest().setMethod(EhClient.METHOD_ARCHIVE_LIST)
                            .setArgs(url, mGid, mToken)
                            .setCallback(this);
                    EhApplication.getEhClient(context).execute(mRequest);
                } else {
                    bind(mArchiveList);
                }
            }
        }

        private void bind(List<ArchiveParser.Archive> data) {
            if (null == mDialog || null == mProgressView || null == mErrorText || null == mListView) {
                return;
            }

            if (null == data || 0 == data.size()) {
                mProgressView.setVisibility(View.GONE);
                mErrorText.setVisibility(View.VISIBLE);
                mListView.setVisibility(View.GONE);
                mErrorText.setText(R.string.no_archives);
            } else {
                var nameArray = data.stream().map(archive -> archive.format(getResources()::getString)).toArray(String[]::new);
                mProgressView.setVisibility(View.GONE);
                mErrorText.setVisibility(View.GONE);
                mListView.setVisibility(View.VISIBLE);
                mListView.setAdapter(new ArrayAdapter<>(mDialog.getContext(), R.layout.item_select_dialog, nameArray));
            }
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Context context = getContext();
            MainActivity activity = getMainActivity();
            if (null != context && null != activity && null != mArchiveList && position < mArchiveList.size()) {
                String res = mArchiveList.get(position).res();
                boolean isHAtH = mArchiveList.get(position).isHAtH();
                EhRequest request = new EhRequest();
                request.setMethod(EhClient.METHOD_DOWNLOAD_ARCHIVE);
                request.setArgs(mGalleryDetail.gid, mGalleryDetail.token, mArchiveFormParamOr, res, isHAtH);
                request.setCallback(new DownloadArchiveListener(context, activity.getStageId(), getTag(), mGalleryDetail));
                EhApplication.getEhClient(context).execute(request);
            }

            if (mDialog != null) {
                mDialog.dismiss();
                mDialog = null;
            }
        }

        @Override
        public void onDismiss(DialogInterface dialog) {
            if (mRequest != null) {
                mRequest.cancel();
                mRequest = null;
            }
            mDialog = null;
            mProgressView = null;
            mErrorText = null;
            mListView = null;
        }

        @Override
        public void onSuccess(ArchiveParser.Result result) {
            if (mRequest != null) {
                mRequest = null;
                mArchiveFormParamOr = result.paramOr();
                mArchiveList = result.archiveList();
                bind(result.archiveList());
            }
        }

        @Override
        public void onFailure(Exception e) {
            mRequest = null;
            Context context = getContext();
            if (null != context && null != mProgressView && null != mErrorText && null != mListView) {
                mProgressView.setVisibility(View.GONE);
                mErrorText.setVisibility(View.VISIBLE);
                mListView.setVisibility(View.GONE);
                mErrorText.setText(ExceptionUtils.getReadableString(e));
            }
        }

        @Override
        public void onCancel() {
            mRequest = null;
        }
    }

    private class TorrentListDialogHelper implements AdapterView.OnItemClickListener,
            DialogInterface.OnDismissListener, EhClient.Callback<List<TorrentParser.Result>> {

        @Nullable
        private CircularProgressIndicator mProgressView;
        @Nullable
        private TextView mErrorText;
        @Nullable
        private ListView mListView;
        @Nullable
        private EhRequest mRequest;
        @Nullable
        private Dialog mDialog;

        public void setDialog(@Nullable Dialog dialog, String url) {
            mDialog = dialog;
            mProgressView = (CircularProgressIndicator) ViewUtils.$$(dialog, R.id.progress);
            mErrorText = (TextView) ViewUtils.$$(dialog, R.id.text);
            mListView = (ListView) ViewUtils.$$(dialog, R.id.list_view);
            mListView.setOnItemClickListener(this);

            Context context = getContext();
            if (context != null) {
                if (mTorrentList == null) {
                    mErrorText.setVisibility(View.GONE);
                    mListView.setVisibility(View.GONE);
                    mRequest = new EhRequest().setMethod(EhClient.METHOD_GET_TORRENT_LIST)
                            .setArgs(url, mGid, mToken)
                            .setCallback(this);
                    EhApplication.getEhClient(context).execute(mRequest);
                } else {
                    bind(mTorrentList);
                }
            }
        }

        private void bind(List<TorrentParser.Result> data) {
            if (null == mDialog || null == mProgressView || null == mErrorText || null == mListView) {
                return;
            }

            if (null == data || 0 == data.size()) {
                mProgressView.setVisibility(View.GONE);
                mErrorText.setVisibility(View.VISIBLE);
                mListView.setVisibility(View.GONE);
                mErrorText.setText(R.string.no_torrents);
            } else {
                var nameArray = data.stream().map(torrent -> torrent.format(getResources()::getString)).toArray(String[]::new);
                mProgressView.setVisibility(View.GONE);
                mErrorText.setVisibility(View.GONE);
                mListView.setVisibility(View.VISIBLE);
                mListView.setAdapter(new ArrayAdapter<>(mDialog.getContext(), R.layout.item_select_dialog, nameArray));
            }
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Context context = getContext();
            if (null != context && null != mTorrentList && position < mTorrentList.size()) {
                String url = mTorrentList.get(position).url();
                String name = mTorrentList.get(position).name();
                // TODO: Don't use buggy system download service
                DownloadManager.Request r = new DownloadManager.Request(Uri.parse(url.replace("exhentai.org", "ehtracker.org")));
                r.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,
                        FileUtils.sanitizeFilename(name + ".torrent"));
                r.allowScanningByMediaScanner();
                r.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                r.addRequestHeader("Cookie", EhApplication.getEhCookieStore(context).getCookieHeader(HttpUrl.get(url)));
                DownloadManager dm = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
                if (dm != null) {
                    try {
                        dm.enqueue(r);
                        showTip(R.string.download_torrent_started, LENGTH_SHORT);
                    } catch (Throwable e) {
                        e.printStackTrace();
                        ExceptionUtils.throwIfFatal(e);
                        showTip(R.string.download_torrent_failure, LENGTH_SHORT);
                    }
                }
            }

            if (mDialog != null) {
                mDialog.dismiss();
                mDialog = null;
            }
        }

        @Override
        public void onDismiss(DialogInterface dialog) {
            if (mRequest != null) {
                mRequest.cancel();
                mRequest = null;
            }
            mDialog = null;
            mProgressView = null;
            mErrorText = null;
            mListView = null;
        }

        @Override
        public void onSuccess(List<TorrentParser.Result> result) {
            if (mRequest != null) {
                mRequest = null;
                mTorrentList = result;
                bind(result);
            }
        }

        @Override
        public void onFailure(Exception e) {
            mRequest = null;
            Context context = getContext();
            if (null != context && null != mProgressView && null != mErrorText && null != mListView) {
                mProgressView.setVisibility(View.GONE);
                mErrorText.setVisibility(View.VISIBLE);
                mListView.setVisibility(View.GONE);
                mErrorText.setText(ExceptionUtils.getReadableString(e));
            }
        }

        @Override
        public void onCancel() {
            mRequest = null;
        }
    }

    private class RateDialogHelper implements GalleryRatingBar.OnUserRateListener,
            DialogInterface.OnClickListener {

        @Nullable
        private GalleryRatingBar mRatingBar;
        @Nullable
        private TextView mRatingText;

        public void setDialog(Dialog dialog, float rating) {
            mRatingText = (TextView) ViewUtils.$$(dialog, R.id.rating_text);
            mRatingBar = (GalleryRatingBar) ViewUtils.$$(dialog, R.id.rating_view);
            mRatingText.setText(getRatingText(rating));
            mRatingBar.setRating(rating);
            mRatingBar.setOnUserRateListener(this);
        }

        @Override
        public void onUserRate(float rating) {
            if (null != mRatingText) {
                mRatingText.setText(getRatingText(rating));
            }
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            Context context = getContext();
            MainActivity activity = getMainActivity();
            if (null == context || null == activity || which != DialogInterface.BUTTON_POSITIVE ||
                    null == mGalleryDetail || null == mRatingBar) {
                return;
            }

            EhRequest request = new EhRequest()
                    .setMethod(EhClient.METHOD_GET_RATE_GALLERY)
                    .setArgs(mGalleryDetail.apiUid, mGalleryDetail.apiKey,
                            mGalleryDetail.gid, mGalleryDetail.token, mRatingBar.getRating())
                    .setCallback(new RateGalleryListener(context,
                            activity.getStageId(), getTag(), mGalleryDetail.gid));
            EhApplication.getEhClient(context).execute(request);
        }
    }

    private class DeleteDialogHelper implements DialogInterface.OnClickListener{
        private final com.hippo.ehviewer.download.DownloadManager mDownloadManager;
        private final GalleryInfo mGalleryInfo;
        private final CheckBoxDialogBuilder mBuilder;

        public DeleteDialogHelper(com.hippo.ehviewer.download.DownloadManager downloadManager,
                                  GalleryInfo galleryInfo, CheckBoxDialogBuilder builder) {
            mDownloadManager = downloadManager;
            mGalleryInfo = galleryInfo;
            mBuilder = builder;
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            if (which != DialogInterface.BUTTON_POSITIVE) {
                return;
            }

            // Delete
            if (null != mDownloadManager) {
                mDownloadManager.deleteDownload(mGalleryInfo.gid);
            }

            // Delete image files
            boolean checked = mBuilder.isChecked();
            Settings.putRemoveImageFiles(checked);
            if (checked) {
                UniFile file = SpiderDen.getGalleryDownloadDir(mGalleryInfo.gid);
                EhDB.removeDownloadDirname(mGalleryInfo.gid);
                deleteFileAsync(file);
            }
        }
    }
}
