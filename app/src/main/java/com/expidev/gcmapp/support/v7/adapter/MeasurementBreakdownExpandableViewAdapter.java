package com.expidev.gcmapp.support.v7.adapter;

import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.expidev.gcmapp.R;
import com.expidev.gcmapp.model.MeasurementDetails;
import com.expidev.gcmapp.support.v7.adapter.MeasurementBreakdownExpandableViewAdapter.ItemViewHolder;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.primitives.Ints;
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractExpandableItemAdapter;

import org.ccci.gto.android.common.support.v4.util.IdUtils;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.Optional;

public class MeasurementBreakdownExpandableViewAdapter
        extends AbstractExpandableItemAdapter<ItemViewHolder, ItemViewHolder> {
    private final int GROUP_TOTAL = 1;
    private final int GROUP_LOCAL = 2;
    private final int GROUP_TEAM = 3;
    private final int GROUP_SUBMINISTRIES = 4;
    private final int GROUP_SELFASSIGNED = 5;

    private Joiner JOINER_NAME = Joiner.on(", ").skipNulls();

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
        switch (mGroups[groupPosition]) {
            case GROUP_TOTAL:
            case GROUP_LOCAL:
                return R.layout.list_item_measurement_breakdown_value;
            default:
                return R.layout.list_item_measurement_breakdown_section;
        }
    }

    @Override
    public int getChildCount(final int groupPosition) {
        assert mDetails != null : "There are only groups when we have MeasurementDetails";
        switch (mGroups[groupPosition]) {
            case GROUP_TOTAL:
            case GROUP_LOCAL:
                return 0;
            case GROUP_TEAM:
                return mDetails.getTeamBreakdown().length;
            case GROUP_SELFASSIGNED:
                return mDetails.getSelfAssignedBreakdown().length;
            case GROUP_SUBMINISTRIES:
                return mDetails.getSubMinistriesBreakdown().length;
            default:
                return 0;
        }
    }

    @Override
    public long getChildId(final int groupPosition, final int childPosition) {
        assert mDetails != null : "There are only children when we have MeasurementDetails";
        final Object rawId;
        switch (mGroups[groupPosition]) {
            case GROUP_TEAM:
                rawId = mDetails.getTeamBreakdown()[childPosition].getId();
                break;
            case GROUP_SELFASSIGNED:
                rawId = mDetails.getSelfAssignedBreakdown()[childPosition].getId();
                break;
            case GROUP_SUBMINISTRIES:
                rawId = mDetails.getSubMinistriesBreakdown()[childPosition].getId();
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
            final List<Integer> groups = Lists.newArrayList(GROUP_TOTAL, GROUP_LOCAL);
            if (mDetails.getTeamBreakdown().length > 0) {
                groups.add(GROUP_TEAM);
            }
            if (mDetails.getSubMinistriesBreakdown().length > 0) {
                groups.add(GROUP_SUBMINISTRIES);
            }
            if (mDetails.getSelfAssignedBreakdown().length > 0) {
                groups.add(GROUP_SELFASSIGNED);
            }
            mGroups = Ints.toArray(groups);
        } else {
            mGroups = new int[0];
        }
        notifyDataSetChanged();
    }

    /* BEGIN lifecycle */

    @Override
    public ItemViewHolder onCreateGroupViewHolder(@NonNull final ViewGroup parent, @LayoutRes final int layout) {
        return new ItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(layout, parent, false));
    }

    @Override
    public ItemViewHolder onCreateChildViewHolder(@NonNull final ViewGroup parent, @LayoutRes final int layout) {
        return new ItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(layout, parent, false));
    }

    @Override
    public void onBindGroupViewHolder(@NonNull final ItemViewHolder holder, final int groupPosition, final int layout) {
        String label = null;
        String value = null;
        switch (mGroups[groupPosition]) {
            case GROUP_LOCAL:
                label = "Local Team";
                //TODO: value
                break;
            case GROUP_TOTAL:
                label = "Total";
                //TODO: value
                break;
            case GROUP_SELFASSIGNED:
                label = "Self Assigned Members";
                break;
            case GROUP_TEAM:
                label = "Team Members";
                break;
            case GROUP_SUBMINISTRIES:
                label = "Sub-Ministries / Teams";
                break;
        }

        if (holder.mLabel != null) {
            holder.mLabel.setText(label);
        }
        if (holder.mValue != null) {
            holder.mValue.setText(value);
        }
    }

    @Override
    public void onBindChildViewHolder(@NonNull final ItemViewHolder holder, final int groupPosition,
                                      final int childPosition, final int layout) {
        assert mDetails != null : "There are only children to bind when we have MeasurementDetails";
        final MeasurementDetails.Breakdown breakdown;
        switch (mGroups[groupPosition]) {
            case GROUP_TEAM:
                breakdown = mDetails.getTeamBreakdown()[childPosition];
                break;
            case GROUP_SELFASSIGNED:
                breakdown = mDetails.getSelfAssignedBreakdown()[childPosition];
                break;
            case GROUP_SUBMINISTRIES:
                breakdown = mDetails.getSubMinistriesBreakdown()[childPosition];
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
    public boolean onCheckCanExpandOrCollapseGroup(final ItemViewHolder holder, final int groupPosition, final int x,
                                                   final int y, final boolean expand) {
        switch (mGroups[groupPosition]) {
            case GROUP_LOCAL:
            case GROUP_TOTAL:
                return false;
            case GROUP_SELFASSIGNED:
            case GROUP_TEAM:
            case GROUP_SUBMINISTRIES:
                return true;
            default:
                return false;
        }
    }

    /* END lifecycle */

    static final class GroupViewHolder extends RecyclerView.ViewHolder {
        public GroupViewHolder(@NonNull final View view) {
            super(view);
        }
    }

    static final class ItemViewHolder extends RecyclerView.ViewHolder {
        @Nullable
        @Optional
        @InjectView(R.id.label)
        TextView mLabel;
        @Nullable
        @Optional
        @InjectView(R.id.value)
        TextView mValue;

        public ItemViewHolder(@NonNull final View view) {
            super(view);
            ButterKnife.inject(this, view);
        }
    }
}
