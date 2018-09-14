package com.arny.flightlogbook.utils.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractRecyclerViewAdapter<T> extends RecyclerView.Adapter<AbstractRecyclerViewAdapter.ViewHolder> {
    private List<T> items;
    private Context context;
    protected OnViewHolderListener listener;

    public interface OnViewHolderListener {
        void onHolderItemClick(View view, int position, Object item);
    }

    protected class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private Map<Integer, View> views;

        public ViewHolder(View view, OnViewHolderListener listener) {
            super(view);
            views = new HashMap<>();
            views.put(0, view);

            if (listener != null)
                view.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (listener != null)
                listener.onHolderItemClick(view, getAdapterPosition(), getItem(getAdapterPosition()));
        }

        public void initViewList(int[] idList) {
            for (int id : idList)
                initViewById(id);
        }

        public void initViewById(int id) {
            View view = (getView() != null ? getView().findViewById(id) : null);

            if (view != null)
                views.put(id, view);
        }

        public View getView() {
            return getView(0);
        }

        public View getView(int id) {
            if (views.containsKey(id))
                return views.get(id);
            else
                initViewById(id);

            return views.get(id);
        }
    }

    protected abstract View createView(Context context, ViewGroup viewGroup, int viewType);

    protected abstract void bindView(T item, AbstractRecyclerViewAdapter.ViewHolder viewHolder);

    public AbstractRecyclerViewAdapter(Context context) {
        this(context, null);
    }

    public AbstractRecyclerViewAdapter(Context context, OnViewHolderListener listener) {
        super();
        this.context = context;
        this.listener = listener;
        items = new ArrayList<>();
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        return new ViewHolder(createView(context, viewGroup, viewType), listener);
    }

    @Override
    public void onBindViewHolder(@NonNull AbstractRecyclerViewAdapter.ViewHolder holder, int position) {
        bindView(getItem(position), holder);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }


    public T getItem(int index) {
        return ((items != null && index >= 0 && index < items.size()) ? items.get(index) : null);
    }

    public Context getContext() {
        return context;
    }

    public void setList(List<T> list) {
        items = list;
    }

    public List<T> getItems() {
        return items;
    }

    public void setActionListener(OnViewHolderListener listener) {
        this.listener = listener;
    }

    public void addAll(List<T> list) {
        items.addAll(list);
        notifyDataSetChanged();
    }

    public void add(T item) {
        items.add(item);
        notifyDataSetChanged();
    }

    public void add(int position, T item) {
        items.add(position, item);
        notifyDataSetChanged();
    }

    public void remove(int position) {
        items.remove(position);
        notifyDataSetChanged();
    }

    public void remove(T item) {
        items.remove(item);
        notifyDataSetChanged();
    }

    public void clear() {
        items.clear();
        notifyDataSetChanged();
    }
}