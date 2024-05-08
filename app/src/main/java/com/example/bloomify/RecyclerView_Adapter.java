package com.example.bloomify;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import kotlinx.coroutines.flow.Flow;

public class RecyclerView_Adapter extends RecyclerView.Adapter<RecyclerView_Adapter.ViewHolder> {
    ArrayList<Flower_List> arrayList;
    LayoutInflater mInflater;
    Select_Listener listener;
    Context context;

    public RecyclerView_Adapter(Context context, ArrayList<Flower_List> arrayList, Select_Listener listener) {
        this.context = context;
        this.arrayList = arrayList;
        this.listener = listener;
        mInflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public RecyclerView_Adapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.grid_items, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView_Adapter.ViewHolder holder, int position) {
        Flower_List flower = arrayList.get(position);
        holder.textView_flower_name.setText(flower.getName());

        String path = flower.getName().toLowerCase().trim().replace(" ", "");
        int resourceId = context.getResources().getIdentifier(path, "drawable", context.getPackageName());
        if (resourceId != 0) {
            holder.imageView_flowers.setImageResource(resourceId);
        }

        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final int pos = holder.getAdapterPosition();
                listener.onClickFlower(arrayList.get(pos));
            }
        });

    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView_id;
        TextView textView_flower_name;
        ImageView imageView_flowers;
        CardView cardView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textView_flower_name = itemView.findViewById(R.id.textView_flower_name);
            imageView_flowers = itemView.findViewById(R.id.imageView_flowers);
            cardView = itemView.findViewById(R.id.cardview);

        }

    }
}
