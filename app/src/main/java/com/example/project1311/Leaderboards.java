package com.example.project1311;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Leaderboards extends AppCompatActivity {

    private RecyclerView testRecyclerView;
    private RecyclerView numbersRecyclerView;
    private RecyclerView familyRecyclerView;

    private LeaderboardAdapter testAdapter;
    private LeaderboardAdapter numbersAdapter;
    private LeaderboardAdapter familyAdapter;

    private String currentUserEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboards);

        currentUserEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();

        initRecyclerViews();

        loadLeaderboard("test", testAdapter);
        loadLeaderboard("numbers", numbersAdapter);
        loadLeaderboard("family", familyAdapter);
    }

    private void initRecyclerViews() {
        testRecyclerView = findViewById(R.id.testLeaderboardRecyclerView);
        numbersRecyclerView = findViewById(R.id.numbersLeaderboardRecyclerView);
        familyRecyclerView = findViewById(R.id.familyLeaderboardRecyclerView);

        testRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        numbersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        familyRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        testAdapter = new LeaderboardAdapter(currentUserEmail);
        numbersAdapter = new LeaderboardAdapter(currentUserEmail);
        familyAdapter = new LeaderboardAdapter(currentUserEmail);

        testRecyclerView.setAdapter(testAdapter);
        numbersRecyclerView.setAdapter(numbersAdapter);
        familyRecyclerView.setAdapter(familyAdapter);
    }

    private void loadLeaderboard(String category, LeaderboardAdapter adapter) {
        DatabaseReference scoresRef = FirebaseDatabase.getInstance().getReference("users");

        scoresRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<LeaderboardEntry> entries = new ArrayList<>();

                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    String email = userSnapshot.child("email").getValue(String.class);
                    Integer score = userSnapshot.child("scores").child(category).getValue(Integer.class);

                    if (score != null && email != null) {
                        entries.add(new LeaderboardEntry(email, score));
                    }
                }

                Collections.sort(entries, (entry1, entry2) -> Integer.compare(entry2.score, entry1.score));
                if (entries.size() > 3) {
                    entries = entries.subList(0, 3);
                }

                adapter.setEntries(entries);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(Leaderboards.this, "Error loading leaderboard", Toast.LENGTH_SHORT).show();
            }
        });
    }

    static class LeaderboardEntry {
        String email;
        int score;

        LeaderboardEntry(String email, int score) {
            this.email = email;
            this.score = score;
        }
    }

    private static class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.ViewHolder> {
        private List<LeaderboardEntry> entries = new ArrayList<>();
        private final String currentUserEmail;
        private final String[] RANK_COLORS = {"#FFD700", "#C0C0C0", "#CD7F32"}; // Gold, Silver, Bronze

        LeaderboardAdapter(String currentUserEmail) {
            this.currentUserEmail = currentUserEmail;
        }

        void setEntries(List<LeaderboardEntry> entries) {
            this.entries = entries;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            LeaderboardEntry entry = entries.get(position);

            // Set rank number
            holder.rankTextView.setText(String.valueOf(position + 1));

            // Set rank color based on position
            holder.rankTextView.setBackgroundColor(Color.parseColor(position < RANK_COLORS.length ? RANK_COLORS[position] : "#A9A9A9"));

            // Set username
            String displayName = entry.email.split("@")[0];
            boolean isCurrentUser = entry.email.equals(currentUserEmail);
            holder.usernameTextView.setText(isCurrentUser ? displayName + " (You)" : displayName);

            // Set score
            holder.scoreTextView.setText(String.valueOf(entry.score));

            // Highlight if current user
            if (isCurrentUser) {
                holder.itemView.setBackgroundColor(Color.parseColor("#E3F2FD"));
                holder.usernameTextView.setTextColor(Color.parseColor("#1565C0"));
            } else {
                holder.itemView.setBackgroundColor(Color.parseColor("#F8F9FA"));
                holder.usernameTextView.setTextColor(Color.parseColor("#263238"));
            }
        }

        @Override
        public int getItemCount() {
            return entries.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView rankTextView, usernameTextView, scoreTextView;

            ViewHolder(View view) {
                super(view);
                rankTextView = view.findViewById(R.id.rankTextView);
                usernameTextView = view.findViewById(R.id.usernameTextView);
                scoreTextView = view.findViewById(R.id.scoreTextView);
            }
        }
    }
}