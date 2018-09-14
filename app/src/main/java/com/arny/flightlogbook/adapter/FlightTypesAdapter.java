package com.arny.flightlogbook.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.arny.flightlogbook.R;
import com.arny.flightlogbook.data.models.AircraftType;
import com.arny.flightlogbook.utils.adapters.AbstractRecyclerViewAdapter;

public class FlightTypesAdapter extends AbstractRecyclerViewAdapter<AircraftType> {
    private FlightTypesListener listener;

    public FlightTypesAdapter(Context context, OnViewHolderListener listener) {
        super(context, listener);
        this.listener = (FlightTypesListener) listener;
    }

    public interface FlightTypesListener extends OnViewHolderListener {
        void onTypeEdit(int position);

        void onTypeDelete(int position);
    }

    @Override
    protected View createView(Context context, ViewGroup viewGroup, int viewType) {
        return LayoutInflater.from(getContext()).inflate(R.layout.typeitem, null);
    }

    @Override
    protected void bindView(AircraftType item, AbstractRecyclerViewAdapter.ViewHolder viewHolder) {
        if (viewHolder != null && item != null) {
            TextView typeTitle = (TextView) viewHolder.getView(R.id.typeTitle);
            ImageView edit = (ImageView) viewHolder.getView(R.id.edit);
            ImageView delete = (ImageView) viewHolder.getView(R.id.delete);
            typeTitle.setText(item.getTypeName());
            if (listener != null) {
                edit.setOnClickListener(v -> listener.onTypeEdit(viewHolder.getAdapterPosition()));
                delete.setOnClickListener(v -> listener.onTypeDelete(viewHolder.getAdapterPosition()));
            }
        }
    }
}
