package com.example.left4candy.placeholderapp;

import android.app.Activity;
import android.app.TabActivity;
import android.content.Intent;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ListFragment extends Fragment {
    private static final String TAG = "ListFragment";
    String userUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private DatabaseReference databaseMarkerRef;

    private RecyclerView listRecyclerView;
    private ListRecyclerViewAdapter recyclerViewAdapter;

    private List<CustomMarker> customMarkerList;

    CustomViewPager vP;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.list_fragment, container, false);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference().child(userUid);
        databaseMarkerRef = mDatabase.child("markers/");

        listRecyclerView = view.findViewById(R.id.listRecycler);
        vP = getActivity().findViewById(R.id.view_pager_container);

        customMarkerList = new ArrayList<>();

        databaseMarkerRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d("onDataChange() in Listfragment", "reached");
                getDatabase(dataSnapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
        initRecyclerView(view, customMarkerList);
        return view;
    }

    public void getDatabase(DataSnapshot dataSnapshot){
        Log.d("getDatabase() Added on listfragment", "reached");
        //myLayout.removeAllViewsInLayout();
        customMarkerList.clear();
        for(DataSnapshot ds : dataSnapshot.getChildren()){
            CustomMarker cmMarker = new CustomMarker();
            cmMarker = ds.getValue(CustomMarker.class);
            CustomMarker.setId(cmMarker.getMarkerId());
            customMarkerList.add(cmMarker);
        }
        recyclerViewAdapter.notifyDataSetChanged();
    }

    private void initRecyclerView(View view, List<CustomMarker> customMarkers){
        recyclerViewAdapter = new ListRecyclerViewAdapter(customMarkers, getActivity());
        listRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        listRecyclerView.setAdapter(recyclerViewAdapter);
        recyclerViewAdapter.setOnItemClickListener(new ListRecyclerViewAdapter.onRecyclerViewItemClickListener() {
            @Override
            public void onItemClickListener(View view, int position) {
                ((MainAdminActivity)getActivity()).itemClickedFragmentSwitch(position);
            }
        });
    }
}
