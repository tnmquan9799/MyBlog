package com.example.myblog.Adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.myblog.Models.Comment;
import com.example.myblog.R;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    private Context mContext;
    private List<Comment> mData;

    public CommentAdapter(Context mContext, List<Comment> mData) {
        this.mContext = mContext;
        this.mData = mData;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View row = LayoutInflater.from(mContext).inflate(R.layout.row_comment, parent, false);
        return new CommentViewHolder(row);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {

        holder.comment_layout_container.setAnimation(AnimationUtils.loadAnimation(mContext, R.anim.fade_transition_animation));

        Glide.with(mContext).load(mData.get(position).getUimg()).into(holder.img_user);
        holder.tv_name.setText(mData.get(position).getUname());
        holder.tv_content.setText(mData.get(position).getContent());
        holder.tv_date.setText(timeStampToString((long) mData.get(position).getTimeStamp()));


    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public class CommentViewHolder extends RecyclerView.ViewHolder {

        CircleImageView img_user;
        TextView tv_name, tv_content, tv_date;
        ConstraintLayout comment_layout_container;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);

            img_user = (CircleImageView) itemView.findViewById(R.id.comment_user_img);
            tv_name = (TextView) itemView.findViewById(R.id.comment_username);
            tv_content = (TextView) itemView.findViewById(R.id.comment_content);
            tv_date = (TextView) itemView.findViewById(R.id.comment_date);
            comment_layout_container = (ConstraintLayout) itemView.findViewById(R.id.comment_layout_container);
        }
    }

    private String timeStampToString(long timeStamp) {

        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(timeStamp);
        String date = DateFormat.format("hh:mm", calendar).toString();

        return date;
    }
}
