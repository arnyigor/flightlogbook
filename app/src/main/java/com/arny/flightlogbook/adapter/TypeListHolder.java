package com.arny.flightlogbook.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.arny.arnylib.adapters.BindableViewHolder;
import com.arny.flightlogbook.R;
import com.arny.flightlogbook.data.models.AircraftType;
public class TypeListHolder extends BindableViewHolder<AircraftType> {
    TextView typeTitle;
    ImageView edit;
    ImageView delete;
	private int pos;
	private SimpleActionListener simpleActionListener;

	public TypeListHolder(View itemView) {
		super(itemView);
        typeTitle = itemView.findViewById(R.id.typeTitle);
        edit = itemView.findViewById(R.id.edit);
        delete = itemView.findViewById(R.id.delete);
	}

	@SuppressLint("DefaultLocale")
    @Override
	public void bindView(Context context, int position, final AircraftType item, ActionListener actionListener) {
		super.bindView(context,position, item, actionListener);
		this.pos = position;
		int colorbg = ContextCompat.getColor(context, R.color.colorTextGrayBg);
		int colorTransparent = ContextCompat.getColor(context, R.color.colorTransparent);
		int color = position % 2 == 0 ? colorbg : colorTransparent;
        typeTitle.setText(item.getTypeName());
		simpleActionListener = (SimpleActionListener) actionListener;
		if (simpleActionListener != null) {
            edit.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					simpleActionListener.OnTypeEdit(pos);
				}
			});
            delete.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					simpleActionListener.OnTypeDelete(pos);
				}
			});
		}
	}

	public interface SimpleActionListener extends ActionListener {
        void OnTypeEdit(int position);
        void OnTypeDelete(int position);
    }
}