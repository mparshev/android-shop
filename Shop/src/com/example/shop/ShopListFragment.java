package com.example.shop;

import android.app.Fragment;
import android.app.ListFragment;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.ContentUris;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.CheckedTextView;
import android.widget.CursorAdapter;

public class ShopListFragment extends ListFragment implements LoaderCallbacks<Cursor> {
	
	ShopListAdapter mAdapter;

	boolean mExpanded = false;
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		
		setHasOptionsMenu(true);
		setEmptyText(getString(R.string.empty_list));
		
		mAdapter = new ShopListAdapter(getActivity(), null);
		setListAdapter(mAdapter);
		getLoaderManager().initLoader(0, null, this);		

		getListView().setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				getActivity().getContentResolver().update(
						ContentUris.withAppendedId(ShopDb.TOGGLE_URI, id), 
						null, null, null);
			}
		});
		
		getListView().setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				Fragment fragment = new EditFormFragment();
				Bundle args = new Bundle();
				args.putLong(ShopDb._ID, id);
				fragment.setArguments(args);
				
				getActivity().getFragmentManager().beginTransaction()
					.replace(R.id.container, fragment)
					.addToBackStack(null)
					.commit();
				return false;
			}
		});
		
		super.onActivityCreated(savedInstanceState);
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.shop, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		menu.findItem(R.id.action_done).setVisible(mExpanded);
		super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case R.id.action_new:
			if(mExpanded) {
				getActivity().getFragmentManager().beginTransaction()
					.replace(R.id.container, new EditFormFragment())
					.addToBackStack(null)
					.commit();
			} else {
				setExpanded(true);
			}
			return true;
		case R.id.action_done:
			setExpanded(false);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}


	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return new CursorLoader(getActivity(), mExpanded ? ShopDb.EDIT_URI : ShopDb.SHOP_URI, null, null, null, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		mAdapter.swapCursor(data);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		mAdapter.swapCursor(null);
	}

	private void setExpanded(boolean expanded) {
		mExpanded = expanded;
		getLoaderManager().restartLoader(0, null, this);
		getActivity().invalidateOptionsMenu();
	}
	
	public class ShopListAdapter extends CursorAdapter {

		public ShopListAdapter(Context context, Cursor c) {
			super(context, c, true);
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			return LayoutInflater.from(context).inflate(R.layout.shop_list_item, parent, false);
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			//((TextView)view).setText(ShopDb.getItem(cursor));
			CheckedTextView check = (CheckedTextView)view; 
			check.setText(ShopDb.getItem(cursor));
			check.setChecked(ShopDb.getCheck(cursor));
		}
		
	}
}
