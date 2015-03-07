package com.expidev.gcmapp.support.v4.fragment.measurement;

import static com.expidev.gcmapp.Constants.ARG_GUID;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.expidev.gcmapp.R;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.Optional;

public class ColumnsListFragment extends Fragment {
    @Nullable
    @Optional
    @InjectView(R.id.faithHeader)
    View mFaithHeader;
    @Nullable
    @Optional
    @InjectView(R.id.fruitHeader)
    View mFruitHeader;
    @Nullable
    @Optional
    @InjectView(R.id.outcomesHeader)
    View mOutcomesHeader;

    @Nullable
    @Optional
    @InjectView(R.id.faithContent)
    View mFaithContent;
    @Nullable
    @Optional
    @InjectView(R.id.fruitContent)
    View mFruitContent;
    @Nullable
    @Optional
    @InjectView(R.id.outcomesContent)
    View mOutcomesContent;

    public static ColumnsListFragment newInstance(final String guid) {
        final ColumnsListFragment fragment = new ColumnsListFragment();

        final Bundle args = new Bundle();
        args.putString(ARG_GUID, guid);
        fragment.setArguments(args);

        return fragment;
    }

    /* BEGIN lifecycle */

    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable final ViewGroup container,
                             @Nullable final Bundle savedState) {
        final View view = inflater.inflate(R.layout.fragment_measurement_columns_accordion, container, false);
        ButterKnife.inject(this, view);
        return view;
    }

    @Override
    public void onViewCreated(final View view, @Nullable final Bundle savedState) {
        super.onViewCreated(view, savedState);
    }

    @Optional
    @OnClick({R.id.faithHeader, R.id.fruitHeader, R.id.outcomesHeader})
    void onToggleSection(@NonNull final View view) {
        if (mFaithContent != null) {
            mFaithContent.setVisibility(mFaithHeader == view ? View.VISIBLE : View.GONE);
        }
        if (mFruitContent != null) {
            mFruitContent.setVisibility(mFruitHeader == view ? View.VISIBLE : View.GONE);
        }
        if (mOutcomesContent != null) {
            mOutcomesContent.setVisibility(mOutcomesHeader == view ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    /* END lifecycle */
}
