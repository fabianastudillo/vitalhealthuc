package com.example.ecg;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ViewHolder> {
    private List<ListElement> mData;
    private LayoutInflater mInflater;
    private Context context;

    final ListAdapter.OnItemClickListener listener;

    public interface OnItemClickListener{
        void onItemClick(ListElement item);
    }

    public ListAdapter(List<ListElement> itemList, Context context, ListAdapter.OnItemClickListener listener){
        this.mInflater = LayoutInflater.from(context);
        this.context = context;
        this.mData = itemList;
        this.listener = listener;
    }

    @Override
    public int getItemCount() {return mData.size();}

    @Override
    public ListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View view = mInflater.from(parent.getContext()).inflate(R.layout.list_archive, parent, false);
        return new ListAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ListAdapter.ViewHolder holder, final int position){
        holder.cv.setAnimation(AnimationUtils.loadAnimation(context, R.anim.fade_transition));
        holder.bindData(mData.get(position));
    }

    public void setItems(List<ListElement> items) {mData = items;}

    public class ViewHolder extends RecyclerView.ViewHolder{
        ImageView iconImageView;
        TextView dateTextView, bpm;
        CardView cv;

        ViewHolder(View itemView){
            super(itemView);
            iconImageView = itemView.findViewById(R.id.iconImageView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            bpm = itemView.findViewById(R.id.bpm);
            cv = itemView.findViewById(R.id.cv);
        }

        void bindData(final ListElement item){
            dateTextView.setText(item.getFecha());
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(item);
                }
            });
        }
    }
}
