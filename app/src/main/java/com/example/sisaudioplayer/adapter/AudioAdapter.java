package com.example.sisaudioplayer.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sisaudioplayer.R;
import com.example.sisaudioplayer.data.ModelAudio;

import java.util.ArrayList;

public class AudioAdapter extends RecyclerView.Adapter<AudioAdapter.AudioViewHolder> {
    Context context;
    ArrayList<ModelAudio> audioArrayList;
    OnItemClickListener onItemClickListener;

    public AudioAdapter(Context context, ArrayList<ModelAudio> audioArray) {
        this.context = context;
        this.audioArrayList = audioArray;
    }

    @NonNull
    @Override
    public AudioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.audio_list, parent, false);
        return new AudioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AudioAdapter.AudioViewHolder holder, int position) {
        holder.bind(audioArrayList.get(position));
        // holder.title.setText(audioArrayList.get(position).getaudioTitle());
        // holder.artiest.setText(audioArrayList.get(position).getaudioArtist());
    }

    @Override
    public int getItemCount() {
        return audioArrayList.size();
    }

    public class AudioViewHolder extends RecyclerView.ViewHolder {
        TextView title, artiest;

        public AudioViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            artiest = itemView.findViewById(R.id.artist);
        }

        void bind(final ModelAudio audio) {
            title.setText(audio.getaudioTitle());
            artiest.setText(audio.getaudioArtist());
            itemView.setOnClickListener(v -> onItemClickListener.onItemClick(getAdapterPosition(), v));
        }
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void onItemClick(int pos, View v);
    }
}
