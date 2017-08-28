package com.arny.flightlogbook.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.arny.arnylib.adapters.BindableViewHolder;
import com.arny.flightlogbook.R;
import com.arny.flightlogbook.models.Type;
public class TypeListHolder extends BindableViewHolder<Type> {
    @BindView(R.id.typeTitle)
    TextView typeTitle;
    @BindView(R.id.edit)
    ImageButton edit;
    @BindView(R.id.delete)
    ImageButton delete;
	private int pos;
	private SimpleActionListener simpleActionListener;

	public TypeListHolder(View itemView) {
		super(itemView);
        ButterKnife.bind(this, itemView);
	}

	@SuppressLint("DefaultLocale")
    @Override
	public void bindView(Context context, final int position, final Type item, ActionListener actionListener) {
		super.bindView(context,position, item, actionListener);
		this.pos = position;
		int colorbg = ContextCompat.getColor(context, R.color.colorTextGrayBg);
		int colorTransparent = ContextCompat.getColor(context, R.color.colorTransparent);
		int color = position % 2 == 0 ? colorbg : colorTransparent;
        typeTitle.setText(String.format("%s:%d", context.getString(R.string.str_airplane_type), item.getTypeId()));
		simpleActionListener = (SimpleActionListener) actionListener;
		if (simpleActionListener != null) {
            edit.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					simpleActionListener.OnTypeEdit(position);
				}
			});
            delete.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					simpleActionListener.OnTypeDelete(position);
				}
			});
		}
	}

	public interface SimpleActionListener extends ActionListener {
        void OnTypeEdit(int position);
        void OnTypeDelete(int position);
    }
}