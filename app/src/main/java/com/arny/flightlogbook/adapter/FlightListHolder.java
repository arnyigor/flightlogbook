package com.arny.flightlogbook.adapter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.arny.arnylib.adapters.BindableViewHolder;
import com.arny.arnylib.utils.DateTimeUtils;
import com.arny.arnylib.utils.Utility;
import com.arny.flightlogbook.R;
import com.arny.flightlogbook.models.Flight;
public class FlightListHolder extends BindableViewHolder<Flight> {
	private TextView date;
	private TextView logTime;
	private TextView tvType;
	private TextView tvRegNo;
	private TextView tvDesc;
	private LinearLayout llDescrBlock;
	private LinearLayout llItemBlock;
	private int pos;
	private SimpleActionListener simpleActionListener;

	public FlightListHolder(View itemView) {
		super(itemView);
		date  = itemView.findViewById(R.id.tvDate);
		logTime  = itemView.findViewById(R.id.tvLogTime);
		tvType  = itemView.findViewById(R.id.tvType);
		tvRegNo  = itemView.findViewById(R.id.tvRegNo);
		tvDesc  = itemView.findViewById(R.id.tvDesc);
		llDescrBlock  = itemView.findViewById(R.id.llDescrBlock);
		llItemBlock  = itemView.findViewById(R.id.llItemBlock);
	}

	@Override
	public void bindView(Context context, final int position, final Flight item, ActionListener actionListener) {
		super.bindView(context,position, item, actionListener);
		this.pos = position;
		int colorbg = ContextCompat.getColor(context, R.color.colorTextGrayBg);
		int colorTransparent = ContextCompat.getColor(context, R.color.colorTransparent);
		int color = position % 2 == 0 ? colorbg : colorTransparent;
		llItemBlock.setBackgroundColor(color);
		simpleActionListener = (SimpleActionListener) actionListener;
        long datetime = item.getDatetime();
        if (datetime > 0) {
            date.setText(DateTimeUtils.getDateTime(datetime, "dd MMM yyyy"));
        }
		logTime.setText(DateTimeUtils.strLogTime(item.getLogtime()));
		tvType.setText(item.getAirplanetypetitle());
		tvRegNo.setText(item.getReg_no());
		int visDesrcBlock = Utility.empty(item.getDescription()) ? View.GONE : View.VISIBLE;
		llDescrBlock.setVisibility(visDesrcBlock);
		tvDesc.setText(item.getDescription());
		if (simpleActionListener != null) {
			llItemBlock.setOnClickListener(v -> simpleActionListener.OnItemClickListener(position,item));
		}
	}

	public interface SimpleActionListener extends ActionListener {
	}
}