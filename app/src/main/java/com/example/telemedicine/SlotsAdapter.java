package com.example.telemedicine;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.telemedicine.models.Slot;

import java.util.List;

public class SlotsAdapter extends RecyclerView.Adapter<SlotsAdapter.SlotViewHolder> {

    private List<Slot> slotList;
    private OnSlotSelectedListener slotSelectedListener;

    public SlotsAdapter(List<Slot> slotList, OnSlotSelectedListener listener) {
        this.slotList = slotList;
        this.slotSelectedListener = listener;
    }

    @NonNull
    @Override
    public SlotViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_slot, parent, false);
        return new SlotViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SlotViewHolder holder, int position) {
        Slot slot = slotList.get(position);
        holder.slotButton.setText(slot.getTime() + " - " + (slot.isBooked() ? "Booked" : "Available"));

        holder.itemView.setOnClickListener(v -> {
            if (!slot.isBooked() && slotSelectedListener != null) {
                slotSelectedListener.onSlotSelected(slot);
            }
        });
    }

    @Override
    public int getItemCount() {
        return slotList.size();
    }

    static class SlotViewHolder extends RecyclerView.ViewHolder {

        Button slotButton;

        public SlotViewHolder(@NonNull View itemView) {
            super(itemView);
            slotButton = itemView.findViewById(R.id.bookSlotButton);
        }

    }

    // Listener interface for slot selection
    public interface OnSlotSelectedListener {
        void onSlotSelected(Slot slot);
    }
}
