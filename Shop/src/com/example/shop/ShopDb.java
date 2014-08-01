package com.example.shop;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.provider.BaseColumns;

public class ShopDb extends ContentProvider {
	
	public static final String SHOP = "shop";
	public static final String EDIT = "edit";
	
	public final static String AUTHORITY = "my.example.shop";
	public final static Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);

	public static final String _TABLE = "weight";

	public static final String _TOGGLE = "toggle";
	
	public static final Uri TABLE_URI = Uri.withAppendedPath(CONTENT_URI, _TABLE);
	public static final Uri SHOP_URI = Uri.withAppendedPath(CONTENT_URI, SHOP);
	public static final Uri EDIT_URI = Uri.withAppendedPath(CONTENT_URI, EDIT);
	public static final Uri TOGGLE_URI = Uri.withAppendedPath(CONTENT_URI, _TOGGLE);
		
	public static final String _ID = BaseColumns._ID;
	public static final String ITEM = "item";
	public static final String CHECK = "check1";

	public static final String _CREATE_SQL = "create table " + _TABLE + "("
				+ _ID 	+ " integer primary key autoincrement, " 
				+ ITEM 	+ " text, "
				+ CHECK + " boolean )";
	
	private static final UriMatcher sUriMatcher;

	public static final int TABLE_QUERY = 1;
	public static final int TABLE_ROW_QUERY  = 2;
	public static final int SHOP_QUERY = 3;
	public static final int EDIT_QUERY = 4;
	public static final int TOGGLE_QUERY = 5;

	static {
		sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		sUriMatcher.addURI(AUTHORITY, _TABLE, TABLE_QUERY);
		sUriMatcher.addURI(AUTHORITY, _TABLE + "/#", TABLE_ROW_QUERY);
		sUriMatcher.addURI(AUTHORITY, SHOP, SHOP_QUERY);
		sUriMatcher.addURI(AUTHORITY, EDIT, EDIT_QUERY);
		sUriMatcher.addURI(AUTHORITY, _TOGGLE + "/#", TOGGLE_QUERY);
	}

	private static class DataHelper extends SQLiteOpenHelper {

		private static final String DATABASE_NAME = "shop";
		private static final int DATABASE_VERSION = 1;

		public DataHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(_CREATE_SQL);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("drop table if exists " + _TABLE);
			onCreate(db);
		}

	}

	private DataHelper mDataHelper;

	@Override
	public boolean onCreate() {
		mDataHelper = new DataHelper(getContext());
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		switch (sUriMatcher.match(uri)) {
		case TABLE_QUERY:
			Cursor cursor = mDataHelper.getReadableDatabase()
				.query(_TABLE, projection, selection, selectionArgs, null, null, sortOrder);
			cursor.setNotificationUri(getContext().getContentResolver(), CONTENT_URI);
			return cursor;
		case TABLE_ROW_QUERY:
		case TOGGLE_QUERY:
			return mDataHelper.getReadableDatabase()
					.query(_TABLE, null, BaseColumns._ID + " = " + "?",
							new String[] { uri.getLastPathSegment() }, 
							null, null, null);
		case SHOP_QUERY:
			cursor = mDataHelper.getReadableDatabase()
				.query(_TABLE, null, CHECK + " > 0", null, null, null, null);
			cursor.setNotificationUri(getContext().getContentResolver(), CONTENT_URI);
			return cursor;
		case EDIT_QUERY:
			cursor = mDataHelper.getReadableDatabase()
				.query(_TABLE, null, null, null, null, null, null);
			cursor.setNotificationUri(getContext().getContentResolver(), CONTENT_URI);
			return cursor;
		}
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		switch (sUriMatcher.match(uri)) {
		case TABLE_QUERY:
			values.put(CHECK, true);
			long id = mDataHelper.getWritableDatabase().insert(_TABLE, null, values);
			getContext().getContentResolver().notifyChange(CONTENT_URI, null);
			return ContentUris.withAppendedId(uri, id);
		}
		return null;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		switch (sUriMatcher.match(uri)) {
		case TABLE_ROW_QUERY:
			int rows = mDataHelper.getWritableDatabase().update(_TABLE,
					values, BaseColumns._ID + " = " + uri.getLastPathSegment(),
					null);
			if (rows > 0)
				getContext().getContentResolver().notifyChange(CONTENT_URI, null);
			return rows;
		case TOGGLE_QUERY:
			Cursor cursor = query(uri, null, null, null, null);
			if(values == null) values = new ContentValues();
			if(cursor != null) {
				if(cursor.moveToFirst()) {
					values.put(CHECK, !getCheck(cursor));
				}
				cursor.close();
			}
			rows = mDataHelper.getWritableDatabase().update(_TABLE,
					values, BaseColumns._ID + " = " + uri.getLastPathSegment(),
					null);
			if (rows > 0)
				getContext().getContentResolver().notifyChange(CONTENT_URI, null);
			return rows;
		}
		return 0;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		switch (sUriMatcher.match(uri)) {
		case TABLE_ROW_QUERY:
			int rows = mDataHelper.getWritableDatabase().delete(_TABLE,
					BaseColumns._ID + " = " + uri.getLastPathSegment(), null);
			if (rows > 0)
				getContext().getContentResolver().notifyChange(CONTENT_URI, null);
			return rows;
		}
		return 0;
	}

	@Override
	public String getType(Uri uri) {
		return null;
	}

	public static long getId(Cursor cursor) {
		return cursor.getLong(cursor.getColumnIndex(_ID));
	}
	
	public static String getItem(Cursor cursor) {
		return cursor.getString(cursor.getColumnIndex(ITEM));
	}
	
	public static boolean getCheck(Cursor cursor) {
		return cursor.getInt(cursor.getColumnIndex(CHECK)) > 0;
	}

}
