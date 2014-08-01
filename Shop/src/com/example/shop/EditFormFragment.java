package com.example.shop;

import android.app.Fragment;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class EditFormFragment extends Fragment {
	
	Long mId = null;
	EditText mEditItem;
	
	InputMethodManager mImm;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.edit_form, container, false);

		setHasOptionsMenu(true);
		
		mEditItem = (EditText)view.findViewById(R.id.editItem);
		
		Bundle args = getArguments();
		
		if(args != null && args.containsKey(ShopDb._ID)) {
			mId = args.getLong(ShopDb._ID);
		}
		
		if(mId != null) {
			Cursor cursor = getActivity().getContentResolver().query(
					ContentUris.withAppendedId(ShopDb.TABLE_URI, mId), 
					null, null, null, null);
			if(cursor != null) {
				if(cursor.moveToFirst()) {
					mEditItem.setText(ShopDb.getItem(cursor));
				}
				cursor.close();
			}
		}
		
		mImm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
		
		mEditItem.setOnEditorActionListener(new OnEditorActionListener() {
		    @Override
		    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		        if (actionId == EditorInfo.IME_ACTION_DONE) {
		        	return actionAccept();
		        }
		        return false;
		    }
		});		
		
		return view;
	}

	
	@Override
	public void onResume() {
		if(mEditItem.requestFocus())
			mImm.showSoftInput(mEditItem, InputMethodManager.SHOW_IMPLICIT);
		super.onResume();
	}


	@Override
	public void onPause() {
		mImm.hideSoftInputFromWindow(mEditItem.getWindowToken(), 0);
		super.onPause();
	}


	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.form, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case R.id.action_accept:
			return actionAccept();
		case R.id.action_discard:
			if(mId != null) {
				getActivity().getContentResolver().delete(
						ContentUris.withAppendedId(ShopDb.TABLE_URI, mId)
						, null, null);
			}
			getActivity().getFragmentManager().popBackStack();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private boolean actionAccept() {
		ContentValues values = new ContentValues();
		values.put(ShopDb.ITEM, mEditItem.getText().toString());
		values.put(ShopDb.CHECK, true);
		if(mId == null) {
			getActivity().getContentResolver().insert(ShopDb.TABLE_URI, values);
		} else {
			getActivity().getContentResolver().update(
					ContentUris.withAppendedId(ShopDb.TABLE_URI, mId), 
					values, null, null);
		}
		getActivity().getFragmentManager().popBackStack();
		return true;
	}
	
}
