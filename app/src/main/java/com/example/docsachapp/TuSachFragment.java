package com.example.docsachapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class TuSachFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tu_sach, container, false);
        
        Button btnWriteNew = view.findViewById(R.id.btn_write_new);
        Button btnEditOther = view.findViewById(R.id.btn_edit_other);
        
        btnWriteNew.setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), BookCreateActivity.class));
        });
        
        btnEditOther.setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), AuthorWorksActivity.class));
        });
        
        return view;
    }
}
