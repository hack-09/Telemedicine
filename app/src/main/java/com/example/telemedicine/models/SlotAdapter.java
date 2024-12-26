package com.example.telemedicine.models;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.telemedicine.R;

import java.util.List;

public class SlotAdapter extends RecyclerView.Adapter<SlotAdapter.SlotViewHolder> {

    private List<Slot> slots;
    private SlotClickListener listener;

    public SlotAdapter(List<Slot> slots, SlotClickListener listener) {
        this.slots = slots;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SlotViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_slot, parent, false);
        return new SlotViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SlotViewHolder holder, int position) {
        Slot slot = slots.get(position);
        holder.slotTime.setText(slot.getTime());
        holder.bookButton.setOnClickListener(v -> listener.onSlotClick(slot));
    }

    @Override
    public int getItemCount() {
        return slots.size();
    }

    public static class SlotViewHolder extends RecyclerView.ViewHolder {
        TextView slotTime;
        Button bookButton;

        public SlotViewHolder(@NonNull View itemView) {
            super(itemView);
            slotTime = itemView.findViewById(R.id.slotTime);
//            bookButton = itemView.findViewById(R.id.bookButton);
        }
    }

    public interface SlotClickListener {
        void onSlotClick(Slot slot);
    }
}
