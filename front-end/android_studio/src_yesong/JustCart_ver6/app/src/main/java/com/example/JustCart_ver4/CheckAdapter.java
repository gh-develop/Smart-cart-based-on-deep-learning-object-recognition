package com.example.JustCart_ver4;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class CheckAdapter extends RecyclerView.Adapter<CheckAdapter.CustomViewHolder>{
    private ArrayList<com.example.JustCart_ver4.CheckData> mList = null;
    private Activity context = null;


    public CheckAdapter(Activity context, ArrayList<com.example.JustCart_ver4.CheckData> list) {
        this.context = context;
        this.mList = list;
    }

    class CustomViewHolder extends RecyclerView.ViewHolder {
        protected CheckBox Name;


        public CustomViewHolder(View view) {
            super(view);
            this.Name = (CheckBox) view.findViewById(R.id.checkBox);
        }
    }


    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {//리스트뷰가 처음으로 생성될 때 생명주기
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_row, null);
        CustomViewHolder viewHolder = new CustomViewHolder(view);

        return viewHolder;
    }

    @Override //각 아이템들에 대한 매칭
    public void onBindViewHolder(@NonNull CustomViewHolder viewholder, int position) {//실제 추가될 때에 대한 생명주기
        viewholder.Name.setText(mList.get(position).getName());
    }

    @Override
    public int getItemCount() {
        return (null != mList ? mList.size() : 0);
    }
}
