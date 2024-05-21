package com.example.lento;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class EndFragment extends Fragment {
    private RecyclerView recyclerView;
    private ChallengeAdapter adapter;
    private List<Challenge> challengeList;
    private SQLiteHelper dbHelper;
    private SQLiteDatabase database;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //return inflater.inflate(R.layout.fragment_end, container, false);
        View rootView = inflater.inflate(R.layout.fragment_end, container, false);

        recyclerView = rootView.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        challengeList = new ArrayList<>();
        dbHelper = new SQLiteHelper(getContext());
        database = dbHelper.getReadableDatabase();

        // 현재 로그인한 사용자의 이메일을 가져온다
        CurrentUser user = (CurrentUser) getActivity().getApplicationContext();
        String currentUserEmail = user.getCurrentUser();

        // 현재 로그인한 사용자의 마감된 챌린지를 가져온다
        loadChallenges(currentUserEmail);

        adapter = new ChallengeAdapter(challengeList);
        recyclerView.setAdapter(adapter);

        return rootView;
    }

    private void loadChallenges(String userEmail) {
        // 사용자별로 마감된 챌린지를 가져오는 SQL 쿼리 실행
        String query = "SELECT * FROM CHALLENGE WHERE ENROLL_USER = ?";
        Cursor cursor = database.rawQuery(query, new String[]{userEmail});
        if (cursor.moveToFirst()) {
            do {
                int codeColumnIndex = cursor.getColumnIndex("CODE");
                int code = cursor.getInt(codeColumnIndex);

                int categoryColumnIndex = cursor.getColumnIndex("CATEGORY");
                String category = cursor.getString(categoryColumnIndex);

                int titleColumnIndex = cursor.getColumnIndex("TITLE");
                String title = cursor.getString(titleColumnIndex);

                int startDateColumnIndex = cursor.getColumnIndex("STARTDATE");
                String startDateStr = cursor.getString(startDateColumnIndex);

                int deadlineColumnIndex = cursor.getColumnIndex("DEADLINE");
                String deadlineStr = cursor.getString(deadlineColumnIndex);

                int progressColumnIndex = cursor.getColumnIndex("COMPLETE");
                int progress = cursor.getInt(progressColumnIndex);

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                Date startDate = null, deadline = null;
                try {
                    startDate = sdf.parse(startDateStr);
                    deadline = sdf.parse(deadlineStr);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                Date today = new Date();
                if (deadline.before(today)) {
                    Challenge challenge = new Challenge(code, category, title, startDate, deadline, progress);
                    challengeList.add(challenge);
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
    }
    private class ChallengeAdapter extends RecyclerView.Adapter<ChallengeAdapter.ChallengeViewHolder> {
        private List<Challenge> challengeList;

        public ChallengeAdapter(List<Challenge> challengeList) {
            this.challengeList = challengeList;
        }

        @Override
        public ChallengeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chellenge, parent, false);
            return new ChallengeViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ChallengeViewHolder holder, int position) {
            Challenge challenge = challengeList.get(position);
            holder.bind(challenge);
        }

        @Override
        public int getItemCount() {
            return challengeList.size();
        }

        public class ChallengeViewHolder extends RecyclerView.ViewHolder {
            private TextView categoryTextView, titleTextView, dateTextView;
            private ProgressBar progressBar;
            private CardView cardView;
            private ImageView chellimg;

            public ChallengeViewHolder(View itemView) {
                super(itemView);
                categoryTextView = itemView.findViewById(R.id.categoryTextView);
                titleTextView = itemView.findViewById(R.id.titleTextView);
                dateTextView = itemView.findViewById(R.id.dateTextView);
                progressBar = itemView.findViewById(R.id.progressBar);
                cardView = itemView.findViewById(R.id.cardView);
                chellimg = itemView.findViewById(R.id.chellimg);
            }

            public void bind(Challenge challenge) {
                categoryTextView.setText(challenge.getCategory());
                titleTextView.setText(challenge.getTitle());
                String dateString = formatDateRange(challenge.getStartDate(), challenge.getDeadline());
                dateTextView.setText(dateString);
                progressBar.setProgress(challenge.getProgress());

                // 카테고리별로 배경색 변경
                switch (challenge.getCategory()) {
                    case "완곡목표":
                        cardView.setCardBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.second));
                        chellimg.setImageResource(R.drawable.finish);
                        break;
                    case "연습목표":
                        cardView.setCardBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.third));
                        chellimg.setImageResource(R.drawable.practice);
                        break;
                    default:
                        cardView.setCardBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.main));
                        chellimg.setImageResource(R.drawable.etc);
                        break;
                }
            }
        }
    }

    private String formatDateRange(Date startDate, Date deadline) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String startDateStr = sdf.format(startDate);
        String deadlineStr = sdf.format(deadline);
        return startDateStr + " ~ " + deadlineStr;
    }
}
