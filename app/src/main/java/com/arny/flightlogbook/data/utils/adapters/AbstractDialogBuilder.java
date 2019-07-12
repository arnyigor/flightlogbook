package com.arny.flightlogbook.data.utils.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
public abstract class AbstractDialogBuilder extends AlertDialog.Builder {
	protected AlertDialog mAlertDialog;
    private View view;

	public AbstractDialogBuilder(Context context, int themeResId) {
		super(context, themeResId);
		view = LayoutInflater.from(getContext()).inflate(themeResId, null);
	}

	public boolean isShowing(){
	    return mAlertDialog.isShowing();
    }

    public AbstractDialogBuilder(Context context) {
		super(context);
	}

	@Override
	public AlertDialog show() {
		if (view == null) {
			view = getView();
		}
		this.setView(view);
		this.setTitle(getTitle());
		initUI(view);
		mAlertDialog = super.show();
        updateDialogView();
        return mAlertDialog;
	}

    public void dismiss() {
        if (mAlertDialog != null && mAlertDialog.isShowing()) {
            mAlertDialog.dismiss();
        }
    }

    protected abstract void initUI(View view);
	protected abstract String getTitle();
	protected abstract View getView();
	protected abstract void updateDialogView();
}
