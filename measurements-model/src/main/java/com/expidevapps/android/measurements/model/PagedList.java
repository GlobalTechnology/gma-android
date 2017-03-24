package com.expidevapps.android.measurements.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.collect.ForwardingList;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public final class PagedList<E> extends ForwardingList<E> {
    static final String JSON_META = "meta";
    private static final String JSON_TOTAL = "total";
    private static final String JSON_FROM = "from";
    private static final String JSON_TO = "to";
    private static final String JSON_PAGE = "page";
    private static final String JSON_PAGES = "total_pages";

    @NonNull
    private final List<E> mDelegate = new ArrayList<>();

    private final int mTotal;
    private final int mFrom;
    private final int mTo;
    private final int mPage;
    private final int mPages;

    private PagedList(@Nullable final JSONObject meta) throws JSONException {
        if (meta != null) {
            mTotal = meta.optInt(JSON_TOTAL, 0);
            mFrom = meta.optInt(JSON_FROM, 1);
            mTo = meta.optInt(JSON_TO, 0);
            mPage = meta.optInt(JSON_PAGE, 1);
            mPages = meta.optInt(JSON_PAGES, 0);
        } else {
            mTotal = 0;
            mFrom = 1;
            mTo = 0;
            mPage = 1;
            mPages = 0;
        }
    }

    @NonNull
    static <E> PagedList<E> fromMetaJson(@NonNull final JSONObject meta) throws JSONException {
        return new PagedList<>(meta);
    }

    @Override
    protected final List<E> delegate() {
        return mDelegate;
    }

    public int getTotal() {
        return mTotal;
    }

    public int getFrom() {
        return mFrom;
    }

    public int getTo() {
        return mTo;
    }

    public int getPage() {
        return mPage;
    }

    public int getPages() {
        return mPages;
    }

    public boolean hasMore() {
        return mTo < mTotal || mPage < mPages;
    }
}
