package com.expidevapps.android.measurements.db;

import static com.expidevapps.android.measurements.db.Contract.Mcc.COLUMN_MCC;
import static com.expidevapps.android.measurements.db.Contract.MinistryId.COLUMN_MINISTRY_ID;
import static com.expidevapps.android.measurements.db.Contract.Story.COLUMN_CONTENT;
import static com.expidevapps.android.measurements.db.Contract.Story.COLUMN_CREATED;
import static com.expidevapps.android.measurements.db.Contract.Story.COLUMN_CREATED_BY;
import static com.expidevapps.android.measurements.db.Contract.Story.COLUMN_ID;
import static com.expidevapps.android.measurements.db.Contract.Story.COLUMN_PRIVACY;
import static com.expidevapps.android.measurements.db.Contract.Story.COLUMN_STATE;
import static com.expidevapps.android.measurements.db.Contract.Story.COLUMN_TITLE;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;

import com.expidevapps.android.measurements.model.Ministry;
import com.expidevapps.android.measurements.model.Ministry.Mcc;
import com.expidevapps.android.measurements.model.Story;
import com.expidevapps.android.measurements.model.Story.Privacy;
import com.expidevapps.android.measurements.model.Story.State;

import org.joda.time.LocalDate;

class StoryMapper extends LocationMapper<Story> {
    @Override
    protected void mapField(@NonNull final ContentValues values, @NonNull final String field,
                            @NonNull final Story story) {
        switch (field) {
            case COLUMN_ID:
                values.put(field, story.getId());
                break;
            case COLUMN_MINISTRY_ID:
                values.put(field, story.getMinistryId());
                break;
            case COLUMN_MCC:
                values.put(field, story.getMcc().mJson);
                break;
            case COLUMN_TITLE:
                values.put(field, story.getTitle());
                break;
            case COLUMN_CONTENT:
                values.put(field, story.getContent());
                break;
            case COLUMN_STATE:
                values.put(field, story.getState().mJson);
                break;
            case COLUMN_PRIVACY:
                values.put(field, story.getPrivacy().mJson);
                break;
            case COLUMN_CREATED:
                values.put(field, story.getCreated().toString());
                break;
            case COLUMN_CREATED_BY:
                values.put(field, story.getCreatedBy());
                break;
            default:
                super.mapField(values, field, story);
                break;
        }
    }

    @NonNull
    @Override
    protected Story newObject(@NonNull final Cursor c) {
        return new Story();
    }

    @NonNull
    @Override
    public Story toObject(@NonNull Cursor c) {
        final Story story = super.toObject(c);

        story.setId(getLong(c, COLUMN_ID, Story.INVALID_ID));
        story.setMinistryId(getNonNullString(c, COLUMN_MINISTRY_ID, Ministry.INVALID_ID));
        story.setMcc(Mcc.fromJson(getString(c, COLUMN_MCC, Mcc.UNKNOWN.mJson)));
        story.setTitle(getNonNullString(c, COLUMN_TITLE, ""));
        story.setContent(getNonNullString(c, COLUMN_CONTENT, ""));
        story.setState(State.fromJson(getString(c, COLUMN_STATE, State.UNKNOWN.mJson)));
        story.setPrivacy(Privacy.fromJson(getString(c, COLUMN_PRIVACY, Privacy.DEFAULT.mJson)));
        story.setCreated(getNonNullLocalDate(c, COLUMN_CREATED, LocalDate.now()));
        story.setCreatedBy(getString(c, COLUMN_CREATED_BY, null));

        return story;
    }
}
