package se.trollektivet.cap_android;

import se.trollektivet.database.Post;
import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

public class PostsListAdapter extends CursorAdapter {

	private LayoutInflater inflater;

	public PostsListAdapter(Context context, Cursor c, int flags) {
		super(context, c, flags);
		inflater = LayoutInflater.from(context);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		View v = inflater.inflate(R.layout.post_adapter, null, false);
		//Set more padding if its your own post;
		//TODO Get string from settings
		return v;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		Post post = new Post(cursor);
		setViewText(view, R.id.posterName, post.getUser());
		setViewText(view, R.id.postTime, post.getDate());
		setViewText(view, R.id.postContent, post.getPost());
	}

	private void setViewText(View view, int id, String text) {
		((TextView) view.findViewById(id)).setText(text);
	}

}
