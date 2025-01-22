package com.example.project1311;

import androidx.appcompat.app.AppCompatActivity;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;

public class TestFamily extends AppCompatActivity implements View.OnClickListener {

    String apiUrl = "https://mocki.io/v1/fe40109d-0f2e-40de-9e23-297f93fd6ecb";

    TextView questionTextView, scoreTextView, questionNumberTextView, countdownTextView, highScoreTextView;
    Button optionButton1, optionButton2, optionButton3, optionButton4, Return;
    CheckMarkView checkMarkView;
    XMarkView xMarkView;
    int score = 0;
    int currentQuestionIndex = -1;
    JSONArray questionsArray = null;
    List<JSONObject> remainingQuestions = new ArrayList<>();
    private WinningAnimationView winningAnimationView;
    private ViewGroup rootLayout;

    private Handler timerHandler;
    private Runnable countdownRunnable;
    private int countdownTime = 20;

    public static class CheckMarkView extends View {
        private Paint paint;
        private ValueAnimator checkAnimator;
        private float progress = 0f;
        private int checkColor = Color.GREEN;
        private int strokeWidth = 10;

        public CheckMarkView(Context context) {
            super(context);
            init();
        }

        public CheckMarkView(Context context, AttributeSet attrs) {
            super(context, attrs);
            init();
        }

        private void init() {
            paint = new Paint();
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(strokeWidth);
            paint.setColor(checkColor);
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setAntiAlias(true);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            float width = getWidth();
            float height = getHeight();

            float startX = width * 0.2f;
            float startY = height * 0.5f;
            float middleX = width * 0.4f;
            float middleY = height * 0.7f;
            float endX = width * 0.8f;
            float endY = height * 0.3f;

            Path checkPath = new Path();
            checkPath.moveTo(startX, startY);
            checkPath.lineTo(middleX, middleY);
            checkPath.lineTo(endX, endY);

            PathMeasure pathMeasure = new PathMeasure(checkPath, false);
            Path extractedPath = new Path();
            pathMeasure.getSegment(0f, pathMeasure.getLength() * progress, extractedPath, true);

            canvas.drawPath(extractedPath, paint);
        }

        public void startAnimation() {
            if (checkAnimator != null) {
                checkAnimator.cancel();
            }

            checkAnimator = ValueAnimator.ofFloat(0f, 1f);
            checkAnimator.setDuration(200);
            checkAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
            checkAnimator.addUpdateListener(animation -> {
                progress = (float) animation.getAnimatedValue();
                invalidate();
            });
            checkAnimator.start();
        }

        public void reset() {
            progress = 0f;
            invalidate();
        }
    }

    public static class XMarkView extends View {
        private Paint paint;
        private ValueAnimator xAnimator;
        private float progress = 0f;
        private int xColor = Color.RED;
        private int strokeWidth = 10;

        public XMarkView(Context context) {
            super(context);
            init();
        }

        public XMarkView(Context context, AttributeSet attrs) {
            super(context, attrs);
            init();
        }

        private void init() {
            paint = new Paint();
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(strokeWidth);
            paint.setColor(xColor);
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setAntiAlias(true);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            float width = getWidth();
            float height = getHeight();

            Path xPath1 = new Path();
            xPath1.moveTo(width * 0.2f, height * 0.2f);
            xPath1.lineTo(width * 0.8f, height * 0.8f);

            Path xPath2 = new Path();
            xPath2.moveTo(width * 0.8f, height * 0.2f);
            xPath2.lineTo(width * 0.2f, height * 0.8f);

            PathMeasure pathMeasure1 = new PathMeasure(xPath1, false);
            PathMeasure pathMeasure2 = new PathMeasure(xPath2, false);

            Path extractedPath1 = new Path();
            Path extractedPath2 = new Path();

            pathMeasure1.getSegment(0f, pathMeasure1.getLength() * progress, extractedPath1, true);
            pathMeasure2.getSegment(0f, pathMeasure2.getLength() * progress, extractedPath2, true);

            canvas.drawPath(extractedPath1, paint);
            canvas.drawPath(extractedPath2, paint);
        }

        public void startAnimation() {
            if (xAnimator != null) {
                xAnimator.cancel();
            }

            xAnimator = ValueAnimator.ofFloat(0f, 1f);
            xAnimator.setDuration(200);
            xAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
            xAnimator.addUpdateListener(animation -> {
                progress = (float) animation.getAnimatedValue();
                invalidate();
            });
            xAnimator.start();
        }

        public void reset() {
            progress = 0f;
            invalidate();
        }
    }

    private class WinningAnimationView extends View {
        private Paint paint;
        private Paint textPaint;
        private List<Particle> particles;
        private Random random;
        private ValueAnimator animator;
        private ValueAnimator textAnimator;
        private float textScale = 1.0f;
        private final int[] FESTIVE_COLORS = {
                Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW,
                Color.MAGENTA, Color.CYAN, Color.YELLOW, Color.WHITE
        };

        public WinningAnimationView(Context context) {
            super(context);
            init();
        }

        public WinningAnimationView(Context context, AttributeSet attrs) {
            super(context, attrs);
            init();
        }

        private void init() {
            paint = new Paint();
            paint.setStyle(Paint.Style.FILL);

            textPaint = new Paint();
            textPaint.setColor(Color.WHITE);
            textPaint.setTextSize(120);
            textPaint.setTextAlign(Paint.Align.CENTER);
            textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            textPaint.setShadowLayer(10, 0, 0, Color.BLACK);

            particles = new ArrayList<>();
            random = new Random();

            for (int i = 0; i < 150; i++) {
                particles.add(new Particle());
            }

            startAnimation();
        }

        private void startAnimation() {
            animator = ValueAnimator.ofFloat(0f, 1f);
            animator.setDuration(1500);
            animator.setRepeatCount(ValueAnimator.INFINITE);
            animator.addUpdateListener(animation -> {
                for (Particle particle : particles) {
                    particle.update();
                }
                invalidate();
            });
            animator.start();

            textAnimator = ValueAnimator.ofFloat(1f, 1.2f);
            textAnimator.setDuration(1000);
            textAnimator.setRepeatCount(ValueAnimator.INFINITE);
            textAnimator.setRepeatMode(ValueAnimator.REVERSE);
            textAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
            textAnimator.addUpdateListener(animation -> {
                textScale = (float) animation.getAnimatedValue();
                invalidate();
            });
            textAnimator.start();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            for (Particle particle : particles) {
                paint.setColor(particle.color);
                paint.setAlpha(particle.alpha);
                canvas.drawCircle(particle.x, particle.y, particle.radius, paint);
            }

            float centerX = getWidth() / 2f;
            float centerY = getHeight() / 2f;

            canvas.save();
            canvas.scale(textScale, textScale, centerX, centerY);
            canvas.drawText("YOU WIN!", centerX, centerY, textPaint);
            canvas.restore();
        }

        private class Particle {
            float x, y;
            float speedX, speedY;
            float radius;
            int color;
            int alpha;
            float angle;
            float speed;

            Particle() {
                reset();
            }

            void reset() {
                x = getWidth() / 2f;
                y = getHeight() / 2f;
                angle = random.nextFloat() * (float) (2 * Math.PI);
                speed = random.nextFloat() * 15 + 5;
                speedX = (float) Math.cos(angle) * speed;
                speedY = (float) Math.sin(angle) * speed;
                radius = random.nextFloat() * 12 + 4;
                color = FESTIVE_COLORS[random.nextInt(FESTIVE_COLORS.length)];
                alpha = 255;
            }

            void update() {
                x += speedX;
                y += speedY;
                alpha = Math.max(0, alpha - 3);
                speedY += 0.1f;

                if (alpha <= 0 || x < 0 || x > getWidth() || y < 0 || y > getHeight()) {
                    reset();
                }
            }
        }

        public void stopAnimation() {
            if (animator != null) {
                animator.cancel();
            }
            if (textAnimator != null) {
                textAnimator.cancel();
            }
        }
    }

    private class DownloadJson extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            StringBuilder result = new StringBuilder();
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
            return result.toString();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_family);

        rootLayout = findViewById(android.R.id.content);

        questionTextView = findViewById(R.id.questionTextView);
        scoreTextView = findViewById(R.id.scoreTextView);
        questionNumberTextView = findViewById(R.id.questionCounterTextView);
        countdownTextView = findViewById(R.id.countdownTextView);
        highScoreTextView = findViewById(R.id.highScoreTextView);
        optionButton1 = findViewById(R.id.optionButton1);
        optionButton2 = findViewById(R.id.optionButton2);
        optionButton3 = findViewById(R.id.optionButton3);
        optionButton4 = findViewById(R.id.optionButton4);
        Return = findViewById(R.id.button);
        checkMarkView = findViewById(R.id.checkMarkView);
        xMarkView = findViewById(R.id.xMarkView);
        Return.setOnClickListener(this);

        winningAnimationView = new WinningAnimationView(this);
        winningAnimationView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        winningAnimationView.setVisibility(View.GONE);
        rootLayout.addView(winningAnimationView);

        loadQuestionsFromAPI();
        loadHighScore();

        optionButton1.setOnClickListener(v -> checkAnswer(optionButton1));
        optionButton2.setOnClickListener(v -> checkAnswer(optionButton2));
        optionButton3.setOnClickListener(v -> checkAnswer(optionButton3));
        optionButton4.setOnClickListener(v -> checkAnswer(optionButton4));
    }

    private void loadHighScore() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            DatabaseReference userScoreRef = FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(user.getUid())
                    .child("scores")
                    .child("family");

            userScoreRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Integer currentHighScore = task.getResult().getValue(Integer.class);
                    if (currentHighScore != null) {
                        highScoreTextView.setText("High Score: " + currentHighScore);
                    }
                }
            });
        }
    }

    private void loadQuestionsFromAPI() {
        DownloadJson downloadJson = new DownloadJson();
        String result = null;

        try {
            result = downloadJson.execute(apiUrl).get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }

        if (result != null && result.length() > 2) {
            try {
                questionsArray = new JSONArray(result);
                for (int i = 0; i < questionsArray.length(); i++) {
                    remainingQuestions.add(questionsArray.getJSONObject(i));
                }
                loadNewQuestion();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(TestFamily.this, "Error parsing the question data.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(TestFamily.this, "Error: No data found.", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadNewQuestion() {
        if (remainingQuestions.size() > 0) {
            Random rand = new Random();
            int randomIndex = rand.nextInt(remainingQuestions.size());
            JSONObject currentQuestion = remainingQuestions.get(randomIndex);
            remainingQuestions.remove(randomIndex);

            try {
                String question = currentQuestion.getString("question");
                JSONArray optionsArray = currentQuestion.getJSONArray("options");
                int correctAnswerIndex = currentQuestion.getInt("correctAnswer") - 1;

                currentQuestionIndex++;
                questionNumberTextView.setText("Question " + (currentQuestionIndex + 1));
                questionTextView.setText(question);

                optionButton1.setText(optionsArray.getString(0));
                optionButton2.setText(optionsArray.getString(1));
                optionButton3.setText(optionsArray.getString(2));
                optionButton4.setText(optionsArray.getString(3));

                optionButton1.setTag(correctAnswerIndex == 0);
                optionButton2.setTag(correctAnswerIndex == 1);
                optionButton3.setTag(correctAnswerIndex == 2);
                optionButton4.setTag(correctAnswerIndex == 3);

                startCountdownTimer();

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(TestFamily.this, "Error loading new question.", Toast.LENGTH_SHORT).show();
            }
        } else {
            disableButtons();
            showWinningAnimation();
        }
    }

    private void startCountdownTimer() {
        countdownTime = 20;
        countdownTextView.setText(String.valueOf(countdownTime));
        if (timerHandler != null) {
            timerHandler.removeCallbacks(countdownRunnable);
        }

        countdownRunnable = new Runnable() {
            @Override
            public void run() {
                countdownTime--;
                countdownTextView.setText(String.valueOf(countdownTime));

                if (countdownTime <= 0) {
                    loadNewQuestion();
                } else {
                    timerHandler.postDelayed(this, 1000);
                }
            }
        };

        timerHandler = new Handler();
        timerHandler.postDelayed(countdownRunnable, 1000);
    }

    private void checkAnswer(Button selectedButton) {
        boolean isCorrect = (boolean) selectedButton.getTag();
        int originalColor = selectedButton.getBackgroundTintList().getDefaultColor();

        if (isCorrect) {
            score++;
            scoreTextView.setText("Score: " + score);
            selectedButton.setBackgroundColor(Color.GREEN);

            checkMarkView.setVisibility(View.VISIBLE);
            xMarkView.setVisibility(View.INVISIBLE);
            checkMarkView.startAnimation();
        } else {
            selectedButton.setBackgroundColor(Color.RED);

            xMarkView.setVisibility(View.VISIBLE);
            checkMarkView.setVisibility(View.INVISIBLE);
            xMarkView.startAnimation();
        }

        new Handler().postDelayed(() -> {
            selectedButton.setBackgroundColor(originalColor);
            checkMarkView.setVisibility(View.INVISIBLE);
            xMarkView.setVisibility(View.INVISIBLE);
            checkMarkView.reset();
            xMarkView.reset();

            if (remainingQuestions.isEmpty()) {
                stopCountdownTimer();
                disableButtons();

                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {
                    DatabaseReference userScoreRef = FirebaseDatabase.getInstance()
                            .getReference("users")
                            .child(user.getUid())
                            .child("scores")
                            .child("family");

                    userScoreRef.get().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Integer currentHighScore = task.getResult().getValue(Integer.class);
                            if (currentHighScore == null || score > currentHighScore) {
                                userScoreRef.setValue(score);
                                loadHighScore();
                            }
                        }
                    });
                }

                if (score > 5) {
                    showWinningAnimation();
                } else {
                    Toast.makeText(this, "Quiz finished! Your score: " + score, Toast.LENGTH_LONG).show();
                }
            } else {
                loadNewQuestion();
            }
        }, 300);
    }

    private void showWinningAnimation() {
        winningAnimationView.setVisibility(View.VISIBLE);
        new Handler().postDelayed(() -> {
            winningAnimationView.setVisibility(View.GONE);
            Toast.makeText(this, "Congratulations! You've completed the quiz with a high score!", Toast.LENGTH_LONG).show();
        }, 3000);
    }

    private void disableButtons() {
        optionButton1.setEnabled(false);
        optionButton2.setEnabled(false);
        optionButton3.setEnabled(false);
        optionButton4.setEnabled(false);
    }

    private void stopCountdownTimer() {
        if (timerHandler != null) {
            timerHandler.removeCallbacks(countdownRunnable);
            countdownTime = 0;
            countdownTextView.setText(String.valueOf(countdownTime));
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.button) {
            finish();
        }
    }
}