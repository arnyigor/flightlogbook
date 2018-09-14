package com.arny.flightlogbook.presenter.viewflights;

import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.arny.flightlogbook.R;
import com.arny.flightlogbook.data.models.Flight;
import com.arny.flightlogbook.utils.DateTimeUtils;
import com.arny.flightlogbook.utils.adapters.AbstractRecyclerViewAdapter;

public class FlightsAdapter extends AbstractRecyclerViewAdapter<Flight> {
    public FlightsAdapter(Context context, OnViewHolderListener listener) {
        super(context, listener);
    }

    @Override
    protected View createView(Context context, ViewGroup viewGroup, int viewType) {
        return LayoutInflater.from(getContext()).inflate(R.layout.flight_list_item, null);
    }

    @Override
    protected void bindView(Flight item, AbstractRecyclerViewAdapter.ViewHolder viewHolder) {
        if (viewHolder != null && item != null) {
            TextView date = (TextView) viewHolder.getView(R.id.tvDate);
            TextView logTime = (TextView) viewHolder.getView(R.id.tvLogTime);
            TextView tvType = (TextView) viewHolder.getView(R.id.tvType);
            RelativeLayout llItemBlock = (RelativeLayout) viewHolder.getView(R.id.itemBlock);
//            int colorbg = ContextCompat.getColor(getContext(), R.color.colorTextGrayBg);
//            int colorTransparent = ContextCompat.getColor(getContext(), R.color.colorTransparent);
//            int color = viewHolder.getAdapterPosition() % 2 == 0 ? colorbg : colorTransparent;
//            llItemBlock.setBackgroundColor(color);
            long datetime = item.getDatetime() == null ? 0 : item.getDatetime();
            if (datetime > 0) {
                date.setText(DateTimeUtils.getDateTime(datetime, "dd MMM yyyy"));
            }
            Integer logtime = item.getLogtime() == null ? 0 : item.getLogtime();
            logTime.setText(DateTimeUtils.strLogTime(logtime));
            tvType.setText(item.getAirplanetypetitle());
            if (listener != null) {
                llItemBlock.setOnClickListener(v -> listener.onHolderItemClick(llItemBlock, viewHolder.getAdapterPosition(), item));
            }
        }
    }
}
