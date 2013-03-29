package org.feit.jokesmk;

import java.util.HashMap;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;

public class CategoriesProcessor {
	private static final String TAG = "CategoriesProcessor";

	private Context mContext;

	public CategoriesProcessor(Context context) {
		mContext = context;
	}

	public HashMap<Long, Category> getCategories() {
		ContentResolver content = mContext.getContentResolver();
		Cursor cursor = content.query(CategoriesProvider.CONTENT_URI, null, null,
				null, null);
		HashMap<Long, Category> categories = new HashMap<Long, Category>(cursor.getCount());
		while (cursor.moveToNext()) {
			Category category = new Category();
			category.id = cursor.getLong(0);
			category.name = cursor.getString(1);
			category.total = cursor.getInt(2);
			category.slug = cursor.getString(4);
			categories.put(category.id, category);
		}
		cursor.close();
		return categories;
	}
}
