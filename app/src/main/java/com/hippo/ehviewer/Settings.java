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

package com.hippo.ehviewer;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.DimenRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;

import com.hippo.ehviewer.client.EhConfig;
import com.hippo.ehviewer.client.EhUtils;
import com.hippo.ehviewer.client.data.FavListUrlBuilder;
import com.hippo.ehviewer.ui.CommonOperations;
import com.hippo.ehviewer.ui.scene.GalleryListScene;
import com.hippo.glgallery.GalleryView;
import com.hippo.unifile.UniFile;
import com.hippo.util.ExceptionUtils;
import com.hippo.yorozuya.AssertUtils;
import com.hippo.yorozuya.FileUtils;
import com.hippo.yorozuya.MathUtils;
import com.hippo.yorozuya.NumberUtils;

import java.io.File;
import java.util.Locale;
import java.util.Set;

public class Settings {

    /********************
     ****** Eh
     ********************/
    public static final String KEY_GALLERY_SITE = "gallery_site";
    private static final int DEFAULT_GALLERY_SITE = 1;
    public static final String KEY_THEME = "theme";
    private static final int DEFAULT_THEME = -1;
    public static final String KEY_BLACK_DARK_THEME = "black_dark_theme";
    private static final String KEY_LAUNCH_PAGE = "launch_page";
    private static final int DEFAULT_LAUNCH_PAGE = 0;
    public static final String KEY_LIST_MODE = "list_mode";
    private static final int DEFAULT_LIST_MODE = 0;
    public static final String KEY_DETAIL_SIZE = "detail_size_";
    private static final int DEFAULT_DETAIL_SIZE = 8;
    public static final String KEY_LIST_THUMB_SIZE = "list_tile_size";
    private static final int DEFAULT_LIST_THUMB_SIZE = 40;
    public static boolean LIST_THUMB_SIZE_INITED = false;
    private static int LIST_THUMB_SIZE = 40;
    public static final String KEY_THUMB_SIZE = "thumb_size_";
    private static final int DEFAULT_THUMB_SIZE = 4;
    public static final String KEY_THUMB_SHOW_TITLE = "thumb_show_title";
    private static final boolean DEFAULT_THUMB_SHOW_TITLE = true;
    private static final String KEY_THUMB_RESOLUTION = "thumb_resolution";
    private static final int DEFAULT_THUMB_RESOLUTION = 0;
    private static final String KEY_SHOW_JPN_TITLE = "show_jpn_title";
    private static final boolean DEFAULT_SHOW_JPN_TITLE = false;
    private static final String KEY_SHOW_GALLERY_PAGES = "show_gallery_pages";
    private static final boolean DEFAULT_SHOW_GALLERY_PAGES = true;
    private static final String KEY_SHOW_COMMENTS = "show_gallery_comments";
    private static final boolean DEFAULT_SHOW_COMMENTS = true;
    private static final String KEY_PREVIEW_NUM = "preview_num";
    private static final int DEFAULT_PREVIEW_NUM = 60;
    private static final String KEY_PREVIEW_SIZE = "preview_size";
    private static final int DEFAULT_PREVIEW_SIZE = 3;
    public static final String KEY_SHOW_TAG_TRANSLATIONS = "show_tag_translations";
    private static final boolean DEFAULT_SHOW_TAG_TRANSLATIONS = false;
    private static final String KEY_METERED_NETWORK_WARNING = "cellular_network_warning";
    private static final boolean DEFAULT_METERED_NETWORK_WARNING = false;
    private static final String KEY_REQUEST_NEWS = "request_news";
    private static final boolean DEFAULT_REQUEST_NEWS = false;
    private static final String KEY_HIDE_HV_EVENTS = "hide_hv_events";
    private static final boolean DEFAULT_HIDE_HV_EVENTS = false;
    /********************
     ****** Read
     ********************/
    private static final String KEY_SCREEN_ROTATION = "screen_rotation";
    private static final int DEFAULT_SCREEN_ROTATION = 0;
    private static final String KEY_READING_DIRECTION = "reading_direction";
    private static final int DEFAULT_READING_DIRECTION = GalleryView.LAYOUT_RIGHT_TO_LEFT;
    private static final String KEY_PAGE_SCALING = "page_scaling";
    private static final int DEFAULT_PAGE_SCALING = GalleryView.SCALE_FIT;
    private static final String KEY_START_POSITION = "start_position";
    private static final int DEFAULT_START_POSITION = GalleryView.START_POSITION_TOP_RIGHT;
    private static final String KEY_READ_THEME = "read_theme";
    private static final int DEFAULT_READ_THEME = 1;
    private static final String KEY_KEEP_SCREEN_ON = "keep_screen_on";
    private static final boolean DEFAULT_KEEP_SCREEN_ON = false;
    private static final String KEY_SHOW_CLOCK = "gallery_show_clock";
    private static final boolean DEFAULT_SHOW_CLOCK = true;
    private static final String KEY_SHOW_PROGRESS = "gallery_show_progress";
    private static final boolean DEFAULT_SHOW_PROGRESS = true;
    private static final String KEY_SHOW_BATTERY = "gallery_show_battery";
    private static final boolean DEFAULT_SHOW_BATTERY = true;
    private static final String KEY_SHOW_PAGE_INTERVAL = "gallery_show_page_interval";
    private static final boolean DEFAULT_SHOW_PAGE_INTERVAL = false;
    private static final String KEY_VOLUME_PAGE = "volume_page";
    private static final boolean DEFAULT_VOLUME_PAGE = false;
    private static final String KEY_REVERSE_VOLUME_PAGE = "reserve_volume_page";
    private static final boolean DEFAULT_REVERSE_VOLUME_PAGE = false;
    private static final String KEY_READING_FULLSCREEN = "reading_fullscreen";
    private static final boolean VALUE_READING_FULLSCREEN = true;
    private static final String KEY_CUSTOM_SCREEN_LIGHTNESS = "custom_screen_lightness";
    private static final boolean DEFAULT_CUSTOM_SCREEN_LIGHTNESS = false;
    private static final String KEY_SCREEN_LIGHTNESS = "screen_lightness";
    private static final int DEFAULT_SCREEN_LIGHTNESS = 50;
    /********************
     ****** Download
     ********************/
    private static final String KEY_DOWNLOAD_SAVE_SCHEME = "image_scheme";
    private static final String KEY_DOWNLOAD_SAVE_AUTHORITY = "image_authority";
    private static final String KEY_DOWNLOAD_SAVE_PATH = "image_path";
    private static final String KEY_DOWNLOAD_SAVE_QUERY = "image_query";
    private static final String KEY_DOWNLOAD_SAVE_FRAGMENT = "image_fragment";
    public static final String KEY_MEDIA_SCAN = "media_scan";
    private static final boolean DEFAULT_MEDIA_SCAN = false;
    private static final String KEY_MULTI_THREAD_DOWNLOAD = "download_thread";
    private static final int DEFAULT_MULTI_THREAD_DOWNLOAD = 3;
    private static final String KEY_DOWNLOAD_DELAY = "download_delay";
    private static final int DEFAULT_DOWNLOAD_DELAY = 0;
    private static final String KEY_PRELOAD_IMAGE = "preload_image";
    private static final int DEFAULT_PRELOAD_IMAGE = 5;
    private static final String KEY_DOWNLOAD_ORIGIN_IMAGE = "download_origin_image_";
    private static final int DEFAULT_DOWNLOAD_ORIGIN_IMAGE = 0;
    /********************
     ****** Privacy and Security
     ********************/
    private static final String KEY_SECURITY = "security";
    private static final String DEFAULT_SECURITY = "";
    private static final String KEY_ENABLE_FINGERPRINT = "enable_fingerprint";
    private static final boolean DEFAULT_ENABLE_FINGERPRINT = false;
    private static final String KEY_SEC_SECURITY = "enable_secure";
    private static final boolean DEFAULT_SEC_SECURITY = false;
    /********************
     ****** Advanced
     ********************/
    private static final String KEY_SAVE_PARSE_ERROR_BODY = "save_parse_error_body";
    private static final boolean DEFAULT_SAVE_PARSE_ERROR_BODY = true;
    private static final String KEY_SAVE_CRASH_LOG = "save_crash_log";
    private static final boolean DEFAULT_SAVE_CRASH_LOG = true;
    private static final String KEY_READ_CACHE_SIZE = "read_cache_size";
    private static final int DEFAULT_READ_CACHE_SIZE = 320;
    public static final String KEY_APP_LANGUAGE = "app_language";
    private static final String DEFAULT_APP_LANGUAGE = "system";
    private static final String KEY_PROXY_TYPE = "proxy_type";
    private static final int DEFAULT_PROXY_TYPE = EhProxySelector.TYPE_SYSTEM;
    private static final String KEY_PROXY_IP = "proxy_ip";
    private static final String DEFAULT_PROXY_IP = null;
    private static final String KEY_PROXY_PORT = "proxy_port";
    private static final int DEFAULT_PROXY_PORT = -1;
    private static final String KEY_BUILT_IN_HOSTS = "built_in_hosts_2";
    private static final boolean DEFAULT_BUILT_IN_HOSTS = false;
    private static final String KEY_DOMAIN_FRONTING = "domain_fronting";
    private static final boolean DEFAULT_DOMAIN_FRONTING = false;
    private static final String KEY_APP_LINK_VERIFY_TIP = "app_link_verify_tip";
    private static final boolean DEFAULT_APP_LINK_VERIFY_TIP = false;
    /********************
     ****** Favorites
     ********************/
    private static final String KEY_FAV_CAT_0 = "fav_cat_0";
    private static final String KEY_FAV_CAT_1 = "fav_cat_1";
    private static final String KEY_FAV_CAT_2 = "fav_cat_2";
    private static final String KEY_FAV_CAT_3 = "fav_cat_3";
    private static final String KEY_FAV_CAT_4 = "fav_cat_4";
    private static final String KEY_FAV_CAT_5 = "fav_cat_5";
    private static final String KEY_FAV_CAT_6 = "fav_cat_6";
    private static final String KEY_FAV_CAT_7 = "fav_cat_7";
    private static final String KEY_FAV_CAT_8 = "fav_cat_8";
    private static final String KEY_FAV_CAT_9 = "fav_cat_9";
    private static final String DEFAULT_FAV_CAT_0 = "Favorites 0";
    private static final String DEFAULT_FAV_CAT_1 = "Favorites 1";
    private static final String DEFAULT_FAV_CAT_2 = "Favorites 2";
    private static final String DEFAULT_FAV_CAT_3 = "Favorites 3";
    private static final String DEFAULT_FAV_CAT_4 = "Favorites 4";
    private static final String DEFAULT_FAV_CAT_5 = "Favorites 5";
    private static final String DEFAULT_FAV_CAT_6 = "Favorites 6";
    private static final String DEFAULT_FAV_CAT_7 = "Favorites 7";
    private static final String DEFAULT_FAV_CAT_8 = "Favorites 8";
    private static final String DEFAULT_FAV_CAT_9 = "Favorites 9";
    private static final String KEY_FAV_COUNT_0 = "fav_count_0";
    private static final String KEY_FAV_COUNT_1 = "fav_count_1";
    private static final String KEY_FAV_COUNT_2 = "fav_count_2";
    private static final String KEY_FAV_COUNT_3 = "fav_count_3";
    private static final String KEY_FAV_COUNT_4 = "fav_count_4";
    private static final String KEY_FAV_COUNT_5 = "fav_count_5";
    private static final String KEY_FAV_COUNT_6 = "fav_count_6";
    private static final String KEY_FAV_COUNT_7 = "fav_count_7";
    private static final String KEY_FAV_COUNT_8 = "fav_count_8";
    private static final String KEY_FAV_COUNT_9 = "fav_count_9";
    private static final String KEY_FAV_LOCAL = "fav_local";
    private static final String KEY_FAV_CLOUD = "fav_cloud";
    private static final int DEFAULT_FAV_COUNT = 0;
    private static final String KEY_RECENT_FAV_CAT = "recent_fav_cat";
    private static final int DEFAULT_RECENT_FAV_CAT = FavListUrlBuilder.FAV_CAT_ALL;
    // -1 for local, 0 - 9 for cloud favorite, other for no default fav slot
    public static final int INVALID_DEFAULT_FAV_SLOT = -2;
    private static final String KEY_DEFAULT_FAV_SLOT = "default_favorite_2";
    private static final int DEFAULT_DEFAULT_FAV_SLOT = INVALID_DEFAULT_FAV_SLOT;
    private static final String KEY_NEVER_ADD_FAV_NOTES = "never_add_favorite_notes";
    private static final boolean DEFAULT_NEVER_ADD_FAV_NOTES = false;
    /********************
     ****** Guide
     ********************/
    private static final String KEY_GUIDE_QUICK_SEARCH = "guide_quick_search";
    private static final boolean DEFAULT_GUIDE_QUICK_SEARCH = true;
    private static final String KEY_GUIDE_COLLECTIONS = "guide_collections";
    private static final boolean DEFAULT_GUIDE_COLLECTIONS = true;
    private static final String KEY_GUIDE_DOWNLOAD_THUMB = "guide_download_thumb";
    private static final boolean DEFAULT_GUIDE_DOWNLOAD_THUMB = true;
    private static final String KEY_GUIDE_DOWNLOAD_LABELS = "guide_download_labels";
    private static final boolean DEFAULT_GUIDE_DOWNLOAD_LABELS = true;
    private static final String KEY_GUIDE_GALLERY = "guide_gallery";
    private static final boolean DEFAULT_GUIDE_GALLERY = true;
    /********************
     ****** Others
     ********************/
    private static final String TAG = Settings.class.getSimpleName();
    private static final String KEY_VERSION_CODE = "version_code";
    private static final int DEFAULT_VERSION_CODE = 0;
    private static final String KEY_SHOW_WARNING = "show_warning";
    private static final boolean DEFAULT_SHOW_WARNING = true;
    private static final String KEY_SELECT_SITE = "select_site";
    private static final boolean DEFAULT_SELECT_SITE = true;
    private static final String KEY_NEED_SIGN_IN = "need_sign_in";
    private static final boolean DEFAULT_NEED_SIGN_IN = true;
    private static final String KEY_DISPLAY_NAME = "display_name";
    private static final String DEFAULT_DISPLAY_NAME = null;
    private static final String KEY_AVATAR = "avatar";
    private static final String DEFAULT_AVATAR = null;
    private static final String KEY_QUICK_SEARCH_TIP = "quick_search_tip";
    private static final boolean DEFAULT_QUICK_SEARCH_TIP = true;
    private static final String KEY_QS_SAVE_PROGRESS = "qs_save_progress";
    private static final boolean DEFAULT_QS_SAVE_PROGRESS = false;
    private static final String KEY_HAS_DEFAULT_DOWNLOAD_LABEL = "has_default_download_label";
    private static final boolean DEFAULT_HAS_DOWNLOAD_LABEL = false;
    private static final String KEY_DEFAULT_DOWNLOAD_LABEL = "default_download_label";
    private static final String DEFAULT_DOWNLOAD_LABEL = null;
    private static final String KEY_RECENT_DOWNLOAD_LABEL = "recent_download_label";
    private static final String DEFAULT_RECENT_DOWNLOAD_LABEL = null;
    private static final String KEY_REMOVE_IMAGE_FILES = "include_pic";
    private static final boolean DEFAULT_REMOVE_IMAGE_FILES = true;
    private static final String KEY_CLIPBOARD_TEXT_HASH_CODE = "clipboard_text_hash_code";
    private static final int DEFAULT_CLIPBOARD_TEXT_HASH_CODE = 0;
    private static final String KEY_ARCHIVE_PASSWDS = "archive_passwds";
    private static Context sContext;
    private static SharedPreferences sSettingsPre;
    private static EhConfig sEhConfig;

    public static void initialize(Context context) {
        sContext = context.getApplicationContext();
        sSettingsPre = PreferenceManager.getDefaultSharedPreferences(sContext);
        sEhConfig = loadEhConfig();
        fixDefaultValue(context);
    }

    private static void fixDefaultValue(Context context) {
        if ("zh".equals(Locale.getDefault().getLanguage())) {
            // Enable show tag translations if the language is zh
            if (!sSettingsPre.contains(KEY_SHOW_TAG_TRANSLATIONS)) {
                putShowTagTranslations(true);
            }
        }
    }

    public static Locale getLocale() {
        Locale locale = null;
        String language = Settings.getAppLanguage();
        if (language != null && !language.equals("system")) {
            return Locale.forLanguageTag(language);
        } else {
            return Locale.getDefault();
        }
    }

    private static EhConfig loadEhConfig() {
        EhConfig ehConfig = new EhConfig();
        return ehConfig;
    }

    public static boolean getBoolean(String key, boolean defValue) {
        try {
            return sSettingsPre.getBoolean(key, defValue);
        } catch (ClassCastException e) {
            Log.d(TAG, "Get ClassCastException when get " + key + " value", e);
            return defValue;
        }
    }

    public static void putBoolean(String key, boolean value) {
        sSettingsPre.edit().putBoolean(key, value).apply();
    }

    public static int getInt(String key, int defValue) {
        try {
            return sSettingsPre.getInt(key, defValue);
        } catch (ClassCastException e) {
            Log.d(TAG, "Get ClassCastException when get " + key + " value", e);
            return defValue;
        }
    }

    public static void putInt(String key, int value) {
        sSettingsPre.edit().putInt(key, value).apply();
    }

    public static long getLong(String key, long defValue) {
        try {
            return sSettingsPre.getLong(key, defValue);
        } catch (ClassCastException e) {
            Log.d(TAG, "Get ClassCastException when get " + key + " value", e);
            return defValue;
        }
    }

    public static void putLong(String key, long value) {
        sSettingsPre.edit().putLong(key, value).apply();
    }

    public static float getFloat(String key, float defValue) {
        try {
            return sSettingsPre.getFloat(key, defValue);
        } catch (ClassCastException e) {
            Log.d(TAG, "Get ClassCastException when get " + key + " value", e);
            return defValue;
        }
    }

    public static void putFloat(String key, float value) {
        sSettingsPre.edit().putFloat(key, value).apply();
    }

    public static String getString(String key, String defValue) {
        try {
            return sSettingsPre.getString(key, defValue);
        } catch (ClassCastException e) {
            Log.d(TAG, "Get ClassCastException when get " + key + " value", e);
            return defValue;
        }
    }

    public static void putString(String key, String value) {
        sSettingsPre.edit().putString(key, value).apply();
    }

    public static Set<String> getStringSet(String key) {
        return sSettingsPre.getStringSet(key, null);
    }

    public static void putStringToStringSet(String key, String value) {
        Set<String> set = getStringSet(key);
        if (set == null)
            set = Set.of(value);
        else if (set.contains(value))
            return;
        else
            set.add(value);
        sSettingsPre.edit().putStringSet(key, set).apply();
    }

    public static int getIntFromStr(String key, int defValue) {
        try {
            return NumberUtils.parseIntSafely(sSettingsPre.getString(key, Integer.toString(defValue)), defValue);
        } catch (ClassCastException e) {
            Log.d(TAG, "Get ClassCastException when get " + key + " value", e);
            return defValue;
        }
    }

    public static void putIntToStr(String key, int value) {
        sSettingsPre.edit().putString(key, Integer.toString(value)).apply();
    }

    public static int getGallerySite() {
        return getIntFromStr(KEY_GALLERY_SITE, DEFAULT_GALLERY_SITE);
    }

    public static void putGallerySite(int value) {
        putIntToStr(KEY_GALLERY_SITE, value);
    }

    public static int getTheme() {
        return getIntFromStr(KEY_THEME, DEFAULT_THEME);
    }

    public static void putTheme(int theme) {
        putIntToStr(KEY_THEME, theme);
    }

    public static String getLaunchPageGalleryListSceneAction() {
        int value = getIntFromStr(KEY_LAUNCH_PAGE, DEFAULT_LAUNCH_PAGE);
        return switch (value) {
            case 3 -> GalleryListScene.ACTION_TOP_LIST;
            case 2 -> GalleryListScene.ACTION_WHATS_HOT;
            case 1 -> GalleryListScene.ACTION_SUBSCRIPTION;
            default -> GalleryListScene.ACTION_HOMEPAGE;
        };
    }

    public static int getListMode() {
        return getIntFromStr(KEY_LIST_MODE, DEFAULT_LIST_MODE);
    }

    public static int getDetailSize() {
        return dip2px(40 * getInt(KEY_DETAIL_SIZE, DEFAULT_DETAIL_SIZE));
    }

    public static int getListThumbSize() {
        if (LIST_THUMB_SIZE_INITED) {
            return LIST_THUMB_SIZE;
        }
        int size = 3 * getInt(KEY_LIST_THUMB_SIZE, DEFAULT_LIST_THUMB_SIZE);
        LIST_THUMB_SIZE = size;
        LIST_THUMB_SIZE_INITED = true;
        return size;
    }

    public static int getThumbSize() {
        return dip2px(40 * getInt(KEY_THUMB_SIZE, DEFAULT_THUMB_SIZE));
    }

    public static int dip2px(int dpValue) {
        final float scale = sContext.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public static boolean getThumbShowTitle() {
        return getBoolean(KEY_THUMB_SHOW_TITLE, DEFAULT_THUMB_SHOW_TITLE);
    }

    public static int getThumbResolution() {
        return getIntFromStr(KEY_THUMB_RESOLUTION, DEFAULT_THUMB_RESOLUTION);
    }

    public static boolean getShowJpnTitle() {
        return getBoolean(KEY_SHOW_JPN_TITLE, DEFAULT_SHOW_JPN_TITLE);
    }

    public static boolean getShowGalleryPages() {
        return getBoolean(KEY_SHOW_GALLERY_PAGES, DEFAULT_SHOW_GALLERY_PAGES);
    }

    public static boolean getShowComments() {
        return getBoolean(KEY_SHOW_COMMENTS, DEFAULT_SHOW_COMMENTS);
    }

    public static int getPreviewNum() {
        return getInt(KEY_PREVIEW_NUM, DEFAULT_PREVIEW_NUM);
    }

    public static int getPreviewSize() {
        return dip2px(40 * getInt(KEY_PREVIEW_SIZE, DEFAULT_PREVIEW_SIZE));
    }

    public static boolean getShowTagTranslations() {
        return getBoolean(KEY_SHOW_TAG_TRANSLATIONS, DEFAULT_SHOW_TAG_TRANSLATIONS);
    }

    public static void putShowTagTranslations(boolean value) {
        putBoolean(KEY_SHOW_TAG_TRANSLATIONS, value);
    }

    public static boolean getMeteredNetworkWarning() {
        return getBoolean(KEY_METERED_NETWORK_WARNING, DEFAULT_METERED_NETWORK_WARNING);
    }

    public static boolean getRequestNews() {
        return getBoolean(KEY_REQUEST_NEWS, DEFAULT_REQUEST_NEWS);
    }

    public static boolean getHideHvEvents() {
        return getBoolean(KEY_HIDE_HV_EVENTS, DEFAULT_HIDE_HV_EVENTS);
    }

    public static int getScreenRotation() {
        return getIntFromStr(KEY_SCREEN_ROTATION, DEFAULT_SCREEN_ROTATION);
    }

    public static void putScreenRotation(int value) {
        putIntToStr(KEY_SCREEN_ROTATION, value);
    }

    @GalleryView.LayoutMode
    public static int getReadingDirection() {
        return GalleryView.sanitizeLayoutMode(getIntFromStr(KEY_READING_DIRECTION, DEFAULT_READING_DIRECTION));
    }

    public static void putReadingDirection(int value) {
        putIntToStr(KEY_READING_DIRECTION, value);
    }

    @GalleryView.ScaleMode
    public static int getPageScaling() {
        return GalleryView.sanitizeScaleMode(getIntFromStr(KEY_PAGE_SCALING, DEFAULT_PAGE_SCALING));
    }

    public static void putPageScaling(int value) {
        putIntToStr(KEY_PAGE_SCALING, value);
    }

    @GalleryView.StartPosition
    public static int getStartPosition() {
        return GalleryView.sanitizeStartPosition(getIntFromStr(KEY_START_POSITION, DEFAULT_START_POSITION));
    }

    public static void putStartPosition(int value) {
        putIntToStr(KEY_START_POSITION, value);
    }

    public static int getReadTheme() {
        return getIntFromStr(KEY_READ_THEME, DEFAULT_READ_THEME);
    }

    public static void putReadTheme(int value) {
        putIntToStr(KEY_READ_THEME, value);
    }

    public static boolean getKeepScreenOn() {
        return getBoolean(KEY_KEEP_SCREEN_ON, DEFAULT_KEEP_SCREEN_ON);
    }

    public static void putKeepScreenOn(boolean value) {
        putBoolean(KEY_KEEP_SCREEN_ON, value);
    }

    public static boolean getShowClock() {
        return getBoolean(KEY_SHOW_CLOCK, DEFAULT_SHOW_CLOCK);
    }

    public static void putShowClock(boolean value) {
        putBoolean(KEY_SHOW_CLOCK, value);
    }

    public static boolean getShowProgress() {
        return getBoolean(KEY_SHOW_PROGRESS, DEFAULT_SHOW_PROGRESS);
    }

    public static void putShowProgress(boolean value) {
        putBoolean(KEY_SHOW_PROGRESS, value);
    }

    public static boolean getShowBattery() {
        return getBoolean(KEY_SHOW_BATTERY, DEFAULT_SHOW_BATTERY);
    }

    public static void putShowBattery(boolean value) {
        putBoolean(KEY_SHOW_BATTERY, value);
    }

    public static boolean getShowPageInterval() {
        return getBoolean(KEY_SHOW_PAGE_INTERVAL, DEFAULT_SHOW_PAGE_INTERVAL);
    }

    public static void putShowPageInterval(boolean value) {
        putBoolean(KEY_SHOW_PAGE_INTERVAL, value);
    }

    public static boolean getVolumePage() {
        return getBoolean(KEY_VOLUME_PAGE, DEFAULT_VOLUME_PAGE);
    }

    public static void putVolumePage(boolean value) {
        putBoolean(KEY_VOLUME_PAGE, value);
    }

    public static boolean getReverseVolumePage() {
        return getBoolean(KEY_REVERSE_VOLUME_PAGE, DEFAULT_REVERSE_VOLUME_PAGE);
    }

    public static void putReverseVolumePage(boolean value) {
        putBoolean(KEY_REVERSE_VOLUME_PAGE, value);
    }

    public static boolean getReadingFullscreen() {
        return getBoolean(KEY_READING_FULLSCREEN, VALUE_READING_FULLSCREEN);
    }

    public static void putReadingFullscreen(boolean value) {
        putBoolean(KEY_READING_FULLSCREEN, value);
    }

    public static boolean getCustomScreenLightness() {
        return getBoolean(KEY_CUSTOM_SCREEN_LIGHTNESS, DEFAULT_CUSTOM_SCREEN_LIGHTNESS);
    }

    public static void putCustomScreenLightness(boolean value) {
        putBoolean(KEY_CUSTOM_SCREEN_LIGHTNESS, value);
    }

    public static int getScreenLightness() {
        return getInt(KEY_SCREEN_LIGHTNESS, DEFAULT_SCREEN_LIGHTNESS);
    }

    public static void putScreenLightness(int value) {
        putInt(KEY_SCREEN_LIGHTNESS, value);
    }

    @Nullable
    public static UniFile getDownloadLocation() {
        UniFile dir = null;
        try {
            Uri.Builder builder = new Uri.Builder();
            builder.scheme(getString(KEY_DOWNLOAD_SAVE_SCHEME, null));
            builder.encodedAuthority(getString(KEY_DOWNLOAD_SAVE_AUTHORITY, null));
            builder.encodedPath(getString(KEY_DOWNLOAD_SAVE_PATH, null));
            builder.encodedQuery(getString(KEY_DOWNLOAD_SAVE_QUERY, null));
            builder.encodedFragment(getString(KEY_DOWNLOAD_SAVE_FRAGMENT, null));
            dir = UniFile.fromUri(sContext, builder.build());
        } catch (Throwable e) {
            ExceptionUtils.throwIfFatal(e);
            // Ignore
        }
        return dir != null ? dir : UniFile.fromFile(AppConfig.getDefaultDownloadDir());
    }

    public static void putDownloadLocation(@NonNull UniFile location) {
        Uri uri = location.getUri();
        putString(KEY_DOWNLOAD_SAVE_SCHEME, uri.getScheme());
        putString(KEY_DOWNLOAD_SAVE_AUTHORITY, uri.getEncodedAuthority());
        putString(KEY_DOWNLOAD_SAVE_PATH, uri.getEncodedPath());
        putString(KEY_DOWNLOAD_SAVE_QUERY, uri.getEncodedQuery());
        putString(KEY_DOWNLOAD_SAVE_FRAGMENT, uri.getEncodedFragment());

        if (getMediaScan()) {
            CommonOperations.removeNoMediaFile(location);
        } else {
            CommonOperations.ensureNoMediaFile(location);
        }
    }

    public static boolean getMediaScan() {
        return getBoolean(KEY_MEDIA_SCAN, DEFAULT_MEDIA_SCAN);
    }

    public static int getMultiThreadDownload() {
        return getIntFromStr(KEY_MULTI_THREAD_DOWNLOAD, DEFAULT_MULTI_THREAD_DOWNLOAD);
    }

    public static int getDownloadDelay() {
        return getIntFromStr(KEY_DOWNLOAD_DELAY, DEFAULT_DOWNLOAD_DELAY);
    }

    public static int getPreloadImage() {
        return getIntFromStr(KEY_PRELOAD_IMAGE, DEFAULT_PRELOAD_IMAGE);
    }

    public static boolean getDownloadOriginImage(boolean mode) {
        int value = getIntFromStr(KEY_DOWNLOAD_ORIGIN_IMAGE, DEFAULT_DOWNLOAD_ORIGIN_IMAGE);
        return switch (value) {
            case 2 -> mode;
            case 1 -> true;
            default -> false;
        };
    }

    public static boolean getSkipCopyImage() {
        return getIntFromStr(KEY_DOWNLOAD_ORIGIN_IMAGE, DEFAULT_DOWNLOAD_ORIGIN_IMAGE) == 2;
    }

    public static String getSecurity() {
        return getString(KEY_SECURITY, DEFAULT_SECURITY);
    }

    public static void putSecurity(String value) {
        putString(KEY_SECURITY, value);
    }

    public static boolean getEnableFingerprint() {
        return getBoolean(KEY_ENABLE_FINGERPRINT, DEFAULT_ENABLE_FINGERPRINT);
    }

    public static void putEnableFingerprint(boolean value) {
        putBoolean(KEY_ENABLE_FINGERPRINT, value);
    }

    public static boolean getEnabledSecurity() {
        return getBoolean(KEY_SEC_SECURITY, DEFAULT_SEC_SECURITY);
    }

    public static boolean getSaveParseErrorBody() {
        return getBoolean(KEY_SAVE_PARSE_ERROR_BODY, DEFAULT_SAVE_PARSE_ERROR_BODY);
    }

    public static boolean getSaveCrashLog() {
        return getBoolean(KEY_SAVE_CRASH_LOG, DEFAULT_SAVE_CRASH_LOG);
    }

    public static int getReadCacheSize() {
        return getIntFromStr(KEY_READ_CACHE_SIZE, DEFAULT_READ_CACHE_SIZE);
    }

    public static String getAppLanguage() {
        return getString(KEY_APP_LANGUAGE, DEFAULT_APP_LANGUAGE);
    }

    public static int getProxyType() {
        return getInt(KEY_PROXY_TYPE, DEFAULT_PROXY_TYPE);
    }

    public static void putProxyType(int value) {
        putInt(KEY_PROXY_TYPE, value);
    }

    public static String getProxyIp() {
        return getString(KEY_PROXY_IP, DEFAULT_PROXY_IP);
    }

    public static void putProxyIp(String value) {
        putString(KEY_PROXY_IP, value);
    }

    public static int getProxyPort() {
        return getInt(KEY_PROXY_PORT, DEFAULT_PROXY_PORT);
    }

    public static void putProxyPort(int value) {
        putInt(KEY_PROXY_PORT, value);
    }

    public static boolean getBuiltInHosts() {
        return getBoolean(KEY_BUILT_IN_HOSTS, DEFAULT_BUILT_IN_HOSTS);
    }

    public static void putBuiltInHosts(boolean value) {
        putBoolean(KEY_BUILT_IN_HOSTS, value);
    }

    public static boolean getDF() {
        return getBoolean(KEY_DOMAIN_FRONTING, DEFAULT_DOMAIN_FRONTING);
    }

    public static void putDF(boolean value) {
        putBoolean(KEY_DOMAIN_FRONTING, value);
    }

    public static boolean getAppLinkVerifyTip() {
        return getBoolean(KEY_APP_LINK_VERIFY_TIP, DEFAULT_APP_LINK_VERIFY_TIP);
    }

    public static void putAppLinkVerifyTip(boolean value) {
        putBoolean(KEY_APP_LINK_VERIFY_TIP, value);
    }

    public static String[] getFavCat() {
        String[] favCat = new String[10];
        favCat[0] = sSettingsPre.getString(KEY_FAV_CAT_0, DEFAULT_FAV_CAT_0);
        favCat[1] = sSettingsPre.getString(KEY_FAV_CAT_1, DEFAULT_FAV_CAT_1);
        favCat[2] = sSettingsPre.getString(KEY_FAV_CAT_2, DEFAULT_FAV_CAT_2);
        favCat[3] = sSettingsPre.getString(KEY_FAV_CAT_3, DEFAULT_FAV_CAT_3);
        favCat[4] = sSettingsPre.getString(KEY_FAV_CAT_4, DEFAULT_FAV_CAT_4);
        favCat[5] = sSettingsPre.getString(KEY_FAV_CAT_5, DEFAULT_FAV_CAT_5);
        favCat[6] = sSettingsPre.getString(KEY_FAV_CAT_6, DEFAULT_FAV_CAT_6);
        favCat[7] = sSettingsPre.getString(KEY_FAV_CAT_7, DEFAULT_FAV_CAT_7);
        favCat[8] = sSettingsPre.getString(KEY_FAV_CAT_8, DEFAULT_FAV_CAT_8);
        favCat[9] = sSettingsPre.getString(KEY_FAV_CAT_9, DEFAULT_FAV_CAT_9);
        return favCat;
    }

    public static void putFavCat(String[] value) {
        AssertUtils.assertEquals(10, value.length);
        sSettingsPre.edit()
                .putString(KEY_FAV_CAT_0, value[0])
                .putString(KEY_FAV_CAT_1, value[1])
                .putString(KEY_FAV_CAT_2, value[2])
                .putString(KEY_FAV_CAT_3, value[3])
                .putString(KEY_FAV_CAT_4, value[4])
                .putString(KEY_FAV_CAT_5, value[5])
                .putString(KEY_FAV_CAT_6, value[6])
                .putString(KEY_FAV_CAT_7, value[7])
                .putString(KEY_FAV_CAT_8, value[8])
                .putString(KEY_FAV_CAT_9, value[9])
                .apply();
    }

    public static int[] getFavCount() {
        int[] favCount = new int[10];
        favCount[0] = sSettingsPre.getInt(KEY_FAV_COUNT_0, DEFAULT_FAV_COUNT);
        favCount[1] = sSettingsPre.getInt(KEY_FAV_COUNT_1, DEFAULT_FAV_COUNT);
        favCount[2] = sSettingsPre.getInt(KEY_FAV_COUNT_2, DEFAULT_FAV_COUNT);
        favCount[3] = sSettingsPre.getInt(KEY_FAV_COUNT_3, DEFAULT_FAV_COUNT);
        favCount[4] = sSettingsPre.getInt(KEY_FAV_COUNT_4, DEFAULT_FAV_COUNT);
        favCount[5] = sSettingsPre.getInt(KEY_FAV_COUNT_5, DEFAULT_FAV_COUNT);
        favCount[6] = sSettingsPre.getInt(KEY_FAV_COUNT_6, DEFAULT_FAV_COUNT);
        favCount[7] = sSettingsPre.getInt(KEY_FAV_COUNT_7, DEFAULT_FAV_COUNT);
        favCount[8] = sSettingsPre.getInt(KEY_FAV_COUNT_8, DEFAULT_FAV_COUNT);
        favCount[9] = sSettingsPre.getInt(KEY_FAV_COUNT_9, DEFAULT_FAV_COUNT);
        return favCount;
    }

    public static void putFavCount(int[] count) {
        AssertUtils.assertEquals(10, count.length);
        sSettingsPre.edit()
                .putInt(KEY_FAV_COUNT_0, count[0])
                .putInt(KEY_FAV_COUNT_1, count[1])
                .putInt(KEY_FAV_COUNT_2, count[2])
                .putInt(KEY_FAV_COUNT_3, count[3])
                .putInt(KEY_FAV_COUNT_4, count[4])
                .putInt(KEY_FAV_COUNT_5, count[5])
                .putInt(KEY_FAV_COUNT_6, count[6])
                .putInt(KEY_FAV_COUNT_7, count[7])
                .putInt(KEY_FAV_COUNT_8, count[8])
                .putInt(KEY_FAV_COUNT_9, count[9])
                .apply();
    }

    public static int getFavLocalCount() {
        return sSettingsPre.getInt(KEY_FAV_LOCAL, DEFAULT_FAV_COUNT);
    }

    public static void putFavLocalCount(int count) {
        sSettingsPre.edit().putInt(KEY_FAV_LOCAL, count).apply();
    }

    public static int getFavCloudCount() {
        return sSettingsPre.getInt(KEY_FAV_CLOUD, DEFAULT_FAV_COUNT);
    }

    public static void putFavCloudCount(int count) {
        sSettingsPre.edit().putInt(KEY_FAV_CLOUD, count).apply();
    }

    public static int getRecentFavCat() {
        return getInt(KEY_RECENT_FAV_CAT, DEFAULT_RECENT_FAV_CAT);
    }

    public static void putRecentFavCat(int value) {
        putInt(KEY_RECENT_FAV_CAT, value);
    }

    public static int getDefaultFavSlot() {
        return getInt(KEY_DEFAULT_FAV_SLOT, DEFAULT_DEFAULT_FAV_SLOT);
    }

    public static void putDefaultFavSlot(int value) {
        putInt(KEY_DEFAULT_FAV_SLOT, value);
    }

    public static boolean getNeverAddFavNotes() {
        return getBoolean(KEY_NEVER_ADD_FAV_NOTES, DEFAULT_NEVER_ADD_FAV_NOTES);
    }

    public static void putNeverAddFavNotes(boolean value) {
        putBoolean(KEY_NEVER_ADD_FAV_NOTES, value);
    }

    public static boolean getGuideQuickSearch() {
        return getBoolean(KEY_GUIDE_QUICK_SEARCH, DEFAULT_GUIDE_QUICK_SEARCH);
    }

    public static void putGuideQuickSearch(boolean value) {
        putBoolean(KEY_GUIDE_QUICK_SEARCH, value);
    }

    public static boolean getGuideCollections() {
        return getBoolean(KEY_GUIDE_COLLECTIONS, DEFAULT_GUIDE_COLLECTIONS);
    }

    public static void putGuideCollections(boolean value) {
        putBoolean(KEY_GUIDE_COLLECTIONS, value);
    }

    public static boolean getGuideDownloadThumb() {
        return getBoolean(KEY_GUIDE_DOWNLOAD_THUMB, DEFAULT_GUIDE_DOWNLOAD_THUMB);
    }

    public static void putGuideDownloadThumb(boolean value) {
        putBoolean(KEY_GUIDE_DOWNLOAD_THUMB, value);
    }

    public static boolean getGuideDownloadLabels() {
        return getBoolean(KEY_GUIDE_DOWNLOAD_LABELS, DEFAULT_GUIDE_DOWNLOAD_LABELS);
    }

    public static void puttGuideDownloadLabels(boolean value) {
        putBoolean(KEY_GUIDE_DOWNLOAD_LABELS, value);
    }

    public static boolean getGuideGallery() {
        return getBoolean(KEY_GUIDE_GALLERY, DEFAULT_GUIDE_GALLERY);
    }

    public static void putGuideGallery(boolean value) {
        putBoolean(KEY_GUIDE_GALLERY, value);
    }

    public static int getVersionCode() {
        return getInt(KEY_VERSION_CODE, DEFAULT_VERSION_CODE);
    }

    public static void putVersionCode(int value) {
        putInt(KEY_VERSION_CODE, value);
    }

    public static boolean getShowWarning() {
        return getBoolean(KEY_SHOW_WARNING, DEFAULT_SHOW_WARNING);
    }

    public static void putShowWarning(boolean value) {
        putBoolean(KEY_SHOW_WARNING, value);
    }

    public static boolean getSelectSite() {
        return getBoolean(KEY_SELECT_SITE, DEFAULT_SELECT_SITE);
    }

    public static void putSelectSite(boolean value) {
        putBoolean(KEY_SELECT_SITE, value);
    }

    public static boolean getNeedSignIn() {
        return getBoolean(KEY_NEED_SIGN_IN, DEFAULT_NEED_SIGN_IN);
    }

    public static void putNeedSignIn(boolean value) {
        putBoolean(KEY_NEED_SIGN_IN, value);
    }
    public static String getDisplayName() {
        return getString(KEY_DISPLAY_NAME, DEFAULT_DISPLAY_NAME);
    }

    public static void putDisplayName(String value) {
        putString(KEY_DISPLAY_NAME, value);
    }

    public static String getAvatar() {
        return getString(KEY_AVATAR, DEFAULT_AVATAR);
    }

    public static void putAvatar(String value) {
        putString(KEY_AVATAR, value);
    }

    public static boolean getQuickSearchTip() {
        return getBoolean(KEY_QUICK_SEARCH_TIP, DEFAULT_QUICK_SEARCH_TIP);
    }

    public static void putQuickSearchTip(boolean value) {
        putBoolean(KEY_QUICK_SEARCH_TIP, value);
    }

    public static boolean getQSSaveProgress() {
        return getBoolean(KEY_QS_SAVE_PROGRESS, DEFAULT_QS_SAVE_PROGRESS);
    }

    public static void putQSSaveProgress(boolean value) {
        putBoolean(KEY_QS_SAVE_PROGRESS, value);
    }

    public static boolean getHasDefaultDownloadLabel() {
        return getBoolean(KEY_HAS_DEFAULT_DOWNLOAD_LABEL, DEFAULT_HAS_DOWNLOAD_LABEL);
    }

    public static void putHasDefaultDownloadLabel(boolean hasDefaultDownloadLabel) {
        putBoolean(KEY_HAS_DEFAULT_DOWNLOAD_LABEL, hasDefaultDownloadLabel);
    }

    public static String getDefaultDownloadLabel() {
        return getString(KEY_DEFAULT_DOWNLOAD_LABEL, DEFAULT_DOWNLOAD_LABEL);
    }

    public static void putDefaultDownloadLabel(String value) {
        putString(KEY_DEFAULT_DOWNLOAD_LABEL, value);
    }

    public static String getRecentDownloadLabel() {
        return getString(KEY_RECENT_DOWNLOAD_LABEL, DEFAULT_RECENT_DOWNLOAD_LABEL);
    }

    public static void putRecentDownloadLabel(String value) {
        putString(KEY_RECENT_DOWNLOAD_LABEL, value);
    }

    public static boolean getRemoveImageFiles() {
        return getBoolean(KEY_REMOVE_IMAGE_FILES, DEFAULT_REMOVE_IMAGE_FILES);
    }

    public static void putRemoveImageFiles(boolean value) {
        putBoolean(KEY_REMOVE_IMAGE_FILES, value);
    }

    public static int getClipboardTextHashCode() {
        return getInt(KEY_CLIPBOARD_TEXT_HASH_CODE, DEFAULT_CLIPBOARD_TEXT_HASH_CODE);
    }

    public static void putClipboardTextHashCode(int value) {
        putInt(KEY_CLIPBOARD_TEXT_HASH_CODE, value);
    }

    public static EhConfig getEhConfig() {
        return sEhConfig;
    }

    public static Set<String> getArchivePasswds() {
        return getStringSet(KEY_ARCHIVE_PASSWDS);
    }

    public static void putPasswdToArchivePasswds(String value) {
        putStringToStringSet(KEY_ARCHIVE_PASSWDS, value);
    }
}
