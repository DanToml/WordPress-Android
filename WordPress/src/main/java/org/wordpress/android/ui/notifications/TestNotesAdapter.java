package org.wordpress.android.ui.notifications;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.simperium.client.Bucket;
import com.simperium.client.Query;

import org.wordpress.android.R;
import org.wordpress.android.models.Note;
import org.wordpress.android.util.PhotonUtils;
import org.wordpress.android.widgets.NoticonTextView;
import org.wordpress.android.widgets.WPNetworkImageView;

class TestNotesAdapter extends CursorAdapter {

    final private int mAvatarSz;
    private final Query mQuery;
    private int mSelectedPosition = ListView.INVALID_POSITION;
    private boolean mShouldHighlightRows;

    TestNotesAdapter(Context context, Bucket<Note> bucket) {
        super(context, null, 0x0);

        // build a query that sorts by timestamp descending
        mQuery = bucket.query().order(Note.Schema.TIMESTAMP_INDEX, Query.SortType.DESCENDING);

        mAvatarSz = (int) context.getResources().getDimension(R.dimen.avatar_sz_large);
    }

    public void closeCursor() {
        Cursor cursor = getCursor();
        if (cursor != null) {
            cursor.close();
        }
    }

    public void reloadNotes() {
        changeCursor(mQuery.execute());
    }

    public Note getNote(int position) {
        getCursor().moveToPosition(position);
        return getNote();
    }

    private Note getNote() {
        return ((Bucket.ObjectCursor<Note>) getCursor()).getObject();
    }

    public void setSelectedPosition(int selectedPosition) {
        mSelectedPosition = selectedPosition;
    }

    public int getSelectedPosition() {
        return mSelectedPosition;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.notifications_list_item, parent, false);
        NoteViewHolder holder = new NoteViewHolder(view);
        view.setTag(holder);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        if (cursor.isClosed())
            return;

        view.setActivated(mShouldHighlightRows && cursor.getPosition() == mSelectedPosition);

        Bucket.ObjectCursor<Note> bucketCursor = (Bucket.ObjectCursor<Note>) cursor;
        Note note = bucketCursor.getObject();

        NoteViewHolder noteViewHolder = (NoteViewHolder) view.getTag();

        noteViewHolder.txtLabel.setText(note.getFormattedSubject());
        if (note.isCommentType()) {
            noteViewHolder.txtLabel.setMaxLines(2);
            noteViewHolder.txtDetail.setText(note.getCommentPreview());
            noteViewHolder.txtDetail.setVisibility(View.VISIBLE);
        } else {
            noteViewHolder.txtLabel.setMaxLines(3);
            noteViewHolder.txtDetail.setVisibility(View.GONE);
        }

        String avatarUrl = PhotonUtils.fixAvatar(note.getIconURL(), mAvatarSz);
        noteViewHolder.imgAvatar.setImageUrl(avatarUrl, WPNetworkImageView.ImageType.AVATAR);

        if (!TextUtils.isEmpty(note.getNoticonCharacter())) {
            noteViewHolder.noteIcon.setText(note.getNoticonCharacter());
            if (note.isUnread()) {
                noteViewHolder.noteIcon.setBackgroundResource(R.drawable.shape_oval_blue);
            } else {
                noteViewHolder.noteIcon.setBackgroundResource(R.drawable.shape_oval_grey);
            }
            noteViewHolder.noteIcon.setVisibility(View.VISIBLE);
        } else {
            noteViewHolder.noteIcon.setVisibility(View.GONE);
        }

        noteViewHolder.unreadIndicator.setVisibility(note.isUnread() ? View.VISIBLE : View.INVISIBLE);
    }

    public void setShouldHighlightRows(boolean shouldHighlightRows) {
        mShouldHighlightRows = shouldHighlightRows;
    }

    private static class NoteViewHolder {
        private final TextView txtLabel;
        private final TextView txtDetail;
        private final View unreadIndicator;
        private final WPNetworkImageView imgAvatar;
        private final NoticonTextView noteIcon;

        NoteViewHolder(View view) {
            txtLabel = (TextView) view.findViewById(R.id.note_label);
            txtDetail = (TextView) view.findViewById(R.id.note_detail);
            unreadIndicator = view.findViewById(R.id.unread_indicator);
            imgAvatar = (WPNetworkImageView) view.findViewById(R.id.note_avatar);
            noteIcon = (NoticonTextView) view.findViewById(R.id.note_icon);
        }
    }
}
