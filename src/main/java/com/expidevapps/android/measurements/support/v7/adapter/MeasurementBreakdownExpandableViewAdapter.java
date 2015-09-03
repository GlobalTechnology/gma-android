package com.expidevapps.android.measurements.support.v7.adapter;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static com.expidevapps.android.measurements.Constants.VISIBILITY;

import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.expidevapps.android.measurements.R;
import com.expidevapps.android.measurements.model.MeasurementDetails;
import com.expidevapps.android.measurements.support.v7.adapter.MeasurementBreakdownExpandableViewAdapter.ViewHolder;
import com.google.common.collect.Lists;
import com.google.common.primitives.Ints;
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractExpandableItemAdapter;
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractExpandableItemViewHolder;

import org.ccci.gto.android.common.support.v4.util.IdUtils;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.InjectViews;
import butterknife.Optional;

public class MeasurementBreakdownExpandableViewAdapter extends AbstractExpandableItemAdapter<ViewHolder, ViewHolder> {
    private final int GROUP_TOTAL = 1;
    private final int GROUP_LOCAL = 2;
    private final int GROUP_TEAM = 3;
    private final int GROUP_SUBMINISTRIES = 4;
    private final int GROUP_SELFASSIGNED = 5;
    private final int GROUP_SPLITMEASUREMENTS = 6;

    @Nullable
    private MeasurementDetails mDetails;
    @NonNull
    private int[] mGroups = {};

    public MeasurementBreakdownExpandableViewAdapter() {
        setHasStableIds(true);
    }

    @Override
    public int getGroupCount() {
        return mGroups.length;
    }

    @Override
    public long getGroupId(final int groupPosition) {
        return mGroups[groupPosition];
    }

    @Override
    public int getGroupItemViewType(final int groupPosition) {
        return R.layout.list_item_measurement_breakdown_section;
    }

    @Override
    public int getChildCount(final int groupPosition) {
        assert mDetails != null : "There are only groups when we have MeasurementDetails";
        switch (mGroups[groupPosition]) {
            case GROUP_TOTAL:
                return 0;
            case GROUP_LOCAL:
                return mDetails.getLocalBreakdown().length;
            case GROUP_TEAM:
                return mDetails.getTeamBreakdown().length;
            case GROUP_SELFASSIGNED:
                return mDetails.getSelfAssignedBreakdown().length;
            case GROUP_SUBMINISTRIES:
                return mDetails.getSubMinistriesBreakdown().length;
            case GROUP_SPLITMEASUREMENTS:
                return mDetails.getSplitMeasurementsBreakdown().length;
            default:
                return 0;
        }
    }

    @Override
    public long getChildId(final int groupPosition, final int childPosition) {
        assert mDetails != null : "There are only children when we have MeasurementDetails";
        final Object rawId;
        switch (mGroups[groupPosition]) {
            case GROUP_LOCAL:
                rawId = mDetails.getLocalBreakdown()[childPosition].getId();
                break;
            case GROUP_TEAM:
                rawId = mDetails.getTeamBreakdown()[childPosition].getId();
                break;
            case GROUP_SELFASSIGNED:
                rawId = mDetails.getSelfAssignedBreakdown()[childPosition].getId();
                break;
            case GROUP_SUBMINISTRIES:
                rawId = mDetails.getSubMinistriesBreakdown()[childPosition].getId();
                break;
            case GROUP_SPLITMEASUREMENTS:
                rawId = mDetails.getSplitMeasurementsBreakdown()[childPosition].getId();
                break;
            default:
                return RecyclerView.NO_ID;
        }

        return rawId != null ? IdUtils.convertId(rawId) : RecyclerView.NO_ID;
    }

    @Override
    public int getChildItemViewType(final int groupPosition, final int childPosition) {
        return R.layout.list_item_measurement_breakdown_value;
    }

    public void updateDetails(@Nullable final MeasurementDetails details) {
        mDetails = details;
        if (mDetails != null) {
            final List<Integer> groups = Lists.newArrayList(GROUP_TOTAL);
            if (mDetails.getLocalBreakdown().length > 0) {
                groups.add(GROUP_LOCAL);
            }
            if (mDetails.getTeamBreakdown().length > 0) {
                groups.add(GROUP_TEAM);
            }
            if (mDetails.getSubMinistriesBreakdown().length > 0) {
                groups.add(GROUP_SUBMINISTRIES);
            }
            if (mDetails.getSelfAssignedBreakdown().length > 0) {
                groups.add(GROUP_SELFASSIGNED);
            }
            if (mDetails.getSplitMeasurementsBreakdown().length > 0) {
                groups.add(GROUP_SPLITMEASUREMENTS);
            }
            mGroups = Ints.toArray(groups);
        } else {
            mGroups = new int[0];
        }
        notifyDataSetChanged();
    }

    /* BEGIN lifecycle */

    @Override
    public ViewHolder onCreateGroupViewHolder(@NonNull final ViewGroup parent, @LayoutRes final int layout) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(layout, parent, false));
    }

    @Override
    public ViewHolder onCreateChildViewHolder(@NonNull final ViewGroup parent, @LayoutRes final int layout) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(layout, parent, false));
    }

    @Override
    public void onBindGroupViewHolder(@NonNull final ViewHolder holder, final int groupPosition, final int layout) {
        int label = 0;
        String value = null;
        switch (mGroups[groupPosition]) {
            case GROUP_TOTAL:
                label = R.string.label_measurement_details_breakdown_total;
                value = Integer.toString(mDetails != null ? mDetails.getTotal() : 0);
                break;
            case GROUP_LOCAL:
                label = R.string.label_measurement_details_breakdown_local;
                value = Integer.toString(mDetails != null ? mDetails.getLocalBreakdownTotal() : 0);
                break;
            case GROUP_SELFASSIGNED:
                label = R.string.label_measurement_details_breakdown_selfassigned;
                value = Integer.toString(mDetails != null ? mDetails.getSelfAssignedBreakdownTotal() : 0);
                break;
            case GROUP_TEAM:
                label = R.string.label_measurement_details_breakdown_team;
                value = Integer.toString(mDetails != null ? mDetails.getTeamBreakdownTotal() : 0);
                break;
            case GROUP_SUBMINISTRIES:
                label = R.string.label_measurement_details_breakdown_subministries;
                value = Integer.toString(mDetails != null ? mDetails.getSubMinistriesBreakdownTotal() : 0);
                break;
            case GROUP_SPLITMEASUREMENTS:
                label = R.string.label_measurement_details_breakdown_split;
                value = Integer.toString(mDetails != null ? mDetails.getmSplitMeasurementsBreakdownTotal() : 0);
                break;
        }

        if (holder.mLabel != null) {
            holder.mLabel.setText(label);
        }
        if (holder.mValue != null) {
            holder.mValue.setText(value);
        }

        ButterKnife.apply(holder.mHiddenOnEmpty, VISIBILITY, getChildCount(groupPosition) == 0 ? INVISIBLE : VISIBLE);
    }

    @Override
    public void onBindChildViewHolder(@NonNull final ViewHolder holder, final int groupPosition,
                                      final int childPosition, final int layout) {
        assert mDetails != null : "There are only children to bind when we have MeasurementDetails";
        final MeasurementDetails.Breakdown breakdown;
        switch (mGroups[groupPosition]) {
            case GROUP_LOCAL:
                breakdown = mDetails.getLocalBreakdown()[childPosition];
                break;
            case GROUP_TEAM:
                breakdown = mDetails.getTeamBreakdown()[childPosition];
                break;
            case GROUP_SELFASSIGNED:
                breakdown = mDetails.getSelfAssignedBreakdown()[childPosition];
                break;
            case GROUP_SUBMINISTRIES:
                breakdown = mDetails.getSubMinistriesBreakdown()[childPosition];
                break;
            case GROUP_SPLITMEASUREMENTS:
                breakdown = mDetails.getSplitMeasurementsBreakdown()[childPosition];
                break;
            default:
                breakdown = null;
        }

        if (holder.mLabel != null) {
            holder.mLabel.setText(breakdown != null ? breakdown.getName() : null);
        }
        if (holder.mValue != null) {
            holder.mValue.setText(Integer.toString(breakdown != null ? breakdown.getValue() : 0));
        }
    }

    @Override
    public boolean onCheckCanExpandOrCollapseGroup(final ViewHolder holder, final int groupPosition, final int x,
                                                   final int y, final boolean expand) {
        return getChildCount(groupPosition) > 0;
    }

    /* END lifecycle */

    static final class ViewHolder extends AbstractExpandableItemViewHolder {
        @Nullable
        @Optional
        @InjectView(R.id.label)
        TextView mLabel;
        @Nullable
        @Optional
        @InjectView(R.id.value)
        TextView mValue;
        @Optional
        @InjectViews(R.id.icon)
        List<View> mHiddenOnEmpty;

        public ViewHolder(@NonNull final View view) {
            super(view);
            ButterKnife.inject(this, view);
        }
    }
}
