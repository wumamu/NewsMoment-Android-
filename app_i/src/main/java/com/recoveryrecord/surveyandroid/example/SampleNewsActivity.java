package com.recoveryrecord.surveyandroid.example;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.icu.lang.UCharacter;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
//import android.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import static java.security.AccessController.getContext;
//import android.support.v7.widget.Toolbar;

@RequiresApi(api = Build.VERSION_CODES.O)
public class SampleNewsActivity extends AppCompatActivity implements MySimpleGestureListener.SimpleGestureListener {
    //    String TagCycle = "my activity cycle";
    volatile boolean activityStopped = false;
    volatile boolean activityEnd = false;
    boolean share_clicked = false;
    boolean document_create = false;
    long in_time = System.currentTimeMillis();
    private ScreenStateReceiver mReceiver;//screen on or off

    String time_ss = "";//time series
    String tmp_record = "";//viewport
    private String mUrl, mImg, mTitle, mDate, mSource;

    private static final String DEBUG_TAG = "Gestures";
    private MySimpleGestureListener detector;
    List<DragObj> dragObjArrayListArray = new ArrayList<>();//drag gesture

    ReadingBehavior myReadingBehavior = new ReadingBehavior();//sqlite
    ReadingBehaviorDbHelper dbHandler;

    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(api = Build.VERSION_CODES.O)
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("log: activity cycle", "On create");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample_news);
        //open database ############################################################################
        dbHandler = new ReadingBehaviorDbHelper(SampleNewsActivity.this);
        //check trigger from #######################################################################
        if (getIntent().getExtras() != null) {
            Bundle b = getIntent().getExtras();
            myReadingBehavior.setKEY_TRIGGER_BY(b.getString("trigger_from"));
        }
        Log.d("log: trigger_by", myReadingBehavior.getKEY_TRIGGER_BY());
        //set time in ##############################################################################
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
        String time_now = formatter.format(date);
//        LocalDate l_date = LocalDate.now();
//        LocalDateTime now = LocalDateTime.now();
//        int hour = now.getHour();
//        int minute = now.getMinute();
//        int second = now.getSecond();
//        String time_string = hour + ":" + minute + ":" + second;
        myReadingBehavior.setKEY_TIME_IN(time_now);
        Log.d("log: time_in", myReadingBehavior.getKEY_TIME_IN());
        //set gesture listener #####################################################################
        detector = new MySimpleGestureListener(this,this);
//        mUrl = "https://news.tvbs.com.tw/focus/1460200";
//        URL img_url = null;
//        try {
//            img_url = new URL("https://news.tvbs.com.tw/focus/1460200");
//        } catch (MalformedURLException e) {
//            e.printStackTrace();
//        }
//        mImg = "https://cc.tvbs.com.tw/img/upload/2021/02/05/20210205183845-85cd46f0.jpg";
//        mTitle = "外交回歸　拜登懲俄抗中　美戰艦駛台灣海峽";
//        mDate = "2021/02/05 20:00";
//        mSource = "TVBS";
//        mAuthor = "孟心怡";
        //check screen on or off ###################################################################
        //screen off #########################
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        mReceiver = new ScreenStateReceiver();
        registerReceiver(mReceiver, intentFilter);
        //screen size ##############################################################################
        Display display = getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);
        float density = getResources().getDisplayMetrics().density;
        float dpHeight = outMetrics.heightPixels / density;
        final float dpWidth = outMetrics.widthPixels / density;
        myReadingBehavior.setKEY_DISPLAY_HEIGHT(String.valueOf(dpHeight));
        myReadingBehavior.setKEY_DISPLAY_WIDTH(String.valueOf(dpWidth));
        Log.d("log: display_width_dp", myReadingBehavior.getKEY_DISPLAY_WIDTH());
        Log.d("log: display_height_dp", myReadingBehavior.getKEY_DISPLAY_HEIGHT());
//        Point size = new Point();
//        display.getSize(size);
//        int width = size.x;
//        int height = size.y;
//        height = Math.round(height / (outMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
//        width = Math.round(width / (outMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
//        Log.d("log: display_width_dpc", String.valueOf(width));
//        Log.d("log: display_height_dpc", String.valueOf(height));
        //whether is chinese #######################################################################
        final Set<UCharacter.UnicodeBlock> chineseUnicodeBlocks = new HashSet<UCharacter.UnicodeBlock>() {{
            add(UCharacter.UnicodeBlock.CJK_COMPATIBILITY);
            add(UCharacter.UnicodeBlock.CJK_COMPATIBILITY_FORMS);
            add(UCharacter.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS);
            add(UCharacter.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS_SUPPLEMENT);
            add(UCharacter.UnicodeBlock.CJK_RADICALS_SUPPLEMENT);
            add(UCharacter.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION);
            add(UCharacter.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS);
            add(UCharacter.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A);
            add(UCharacter.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B);
            add(UCharacter.UnicodeBlock.KANGXI_RADICALS);
            add(UCharacter.UnicodeBlock.IDEOGRAPHIC_DESCRIPTION_CHARACTERS);
        }};
        //Scrollview has lost the acceleration######################################################
//        ScrollView mScrollView = findViewById(R.id.scroll_view);
//        mScrollView.setNestedScrollingEnabled(false);
        //news generate from server ################################################################
        Random rand = new Random();
        int random_news = ThreadLocalRandom.current().nextInt(1, 3 + 1);
        Log.d("log: firebase news", String.valueOf(random_news));
        DocumentReference docRef = db.collection("News").document("ettoday").collection("20210309").document(String.valueOf(random_news));
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d("log: firebase", "Success");
//                        Log.d("log: firebase", "DocumentSnapshot data: " + document.getData());
                        mUrl = document.getString("url");
                        mImg = "";
//                        mImg = "https://cc.tvbs.com.tw/img/upload/2021/02/05/20210205183845-85cd46f0.jpg";
                        mTitle = document.getString("title");
                        mDate = document.getString("time");
                        mSource = document.getString("src");
//                        mAuthor = "孟心怡";
                        myReadingBehavior.setKEY_NEWS_ID(document.getString("id"));
                        ArrayList<String> c_list = (ArrayList<String>) document.get("content");
//                        for (int i = 0; i < c_list.size(); i++) {
//                            Log.d("log: firebase", "DocumentSnapshot content: " + c_list.get(i));
//                        }
                        List<String> divList = new ArrayList<>();
//                        int cut_size = (int) (dpWidth / 26);
                        int cut_size = (int) (dpWidth / 24);
                        myReadingBehavior.setKEY_BYTE_PER_LINE(cut_size*2);
                        for (int i = 0; i < c_list.size(); i++) {
                            String str = c_list.get(i);
                            int remainder = (str.length()) % cut_size;
                            int str_length_byte = str.length();
                            for (char c : str.toCharArray()) {
                                if (chineseUnicodeBlocks.contains(UCharacter.UnicodeBlock.of(c))) {
                                    //System.out.println(c + " is chinese");
                                    str_length_byte += 1;
                                }
                            }
                            // 一共要分割成幾段
                            int front = 0, end = 0, iter_char = 0, line_count = 1;
                            char[] c = str.toCharArray();
                            while (true) {
                                //System.out.println("line  "+line_count);
                                line_count++;
                                //one paragraph
                                //end = front; // index for word
                                int count_byte = 0;
                                //System.out.print(c[i]);
                                while (count_byte < cut_size * 2) {
                                    //one sentence at most 40 bytes
                                    if (c[iter_char] == '\n') {
                                        break;
                                    }
                                    //System.out.println(iter_char);
                                    //System.out.println(c[iter_char]);
                                    if (isChineseChar(c[iter_char])) {
                                        //System.out.println(c[iter_char] + " is chinese");
                                        count_byte += 2;
                                    } else if ((c[iter_char] >= 'a' && c[iter_char] <= 'z') || (c[iter_char] >= 'A' && c[iter_char] <= 'Z')) {
                                        //english letter
                                        //check word
                                        int word_length = 0, word_index, tmp_count_byte = count_byte;
                                        for (word_index = iter_char; word_index + 1 <= str.length(); word_index++) {
                                            if ((c[word_index] >= 'a' && c[word_index] <= 'z') || (c[word_index] >= 'A' && c[word_index] <= 'Z' ) || c[word_index] == '-') {
                                                word_length += 1;
                                                tmp_count_byte += 1;
                                            } else {
                                                break;
                                            }
                                        }
                                        word_index -= 1;
                                        if (tmp_count_byte < cut_size * 2) {
                                            iter_char = word_index;
                                            count_byte = tmp_count_byte;
                                        } else {
                                            iter_char -= 1;
                                            break;
                                        }
//                        count_byte+=1;
                                    } else {
//                        System.out.println(c[iter_char] + " is not chinese");
                                        count_byte += 1;
                                    }
                                    if (iter_char + 1 < str.length()) {
                                        iter_char += 1;//c[iter_char]
                                    } else {
                                        break;
                                    }
                                }
                                String childStr = str.substring(front, iter_char);
                                divList.add(childStr);
                                front = iter_char;
                                if (iter_char + 1 == str.length()) {
                                    break;
                                }
                            }
                        }
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        params.setMargins(40, 10, 40, 10);
                        //set viewport number ######################################################
                        int textview_num = divList.size();
                        myReadingBehavior.setKEY_VIEW_PORT_NUM(textview_num);
                        Log.d("log: view_port_num", String.valueOf(myReadingBehavior.getKEY_VIEW_PORT_NUM()));
                        //put textview into layout #################################################
                        //int text_size = (int) (dpWidth /30);
                        int text_size = 20;
                        final TextView myTextViewsTitle = new TextView(SampleNewsActivity.this);
                        final TextView myTextViewsDate = new TextView(SampleNewsActivity.this);
                        final TextView myTextViewsSrc = new TextView(SampleNewsActivity.this);
                        myTextViewsTitle.setText(mTitle);
                        myTextViewsDate.setText(mDate);
                        myTextViewsSrc.setText(mSource);
                        myTextViewsTitle.setTextColor(Color.parseColor("black"));
                        myTextViewsDate.setTextColor(Color.parseColor("black"));
                        myTextViewsSrc.setTextColor(Color.parseColor("black"));
                        myTextViewsTitle.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 28);
                        myTextViewsDate.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
                        myTextViewsSrc.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
                        myTextViewsTitle.setGravity(Gravity.LEFT);
                        myTextViewsDate.setGravity(Gravity.LEFT);
                        myTextViewsSrc.setGravity(Gravity.LEFT);
                        myTextViewsTitle.setLayoutParams(params);
                        myTextViewsDate.setLayoutParams(params);
                        myTextViewsSrc.setLayoutParams(params);
                        ((LinearLayout) findViewById(R.id.layout_inside)).addView(myTextViewsTitle);
                        ((LinearLayout) findViewById(R.id.layout_inside)).addView(myTextViewsDate);
                        ((LinearLayout) findViewById(R.id.layout_inside)).addView(myTextViewsSrc);
                        if(mImg!=""){
                            ImageView imageView = new ImageView(SampleNewsActivity.this);
                            new DownloadImageTask(imageView).execute(mImg);
                            ((LinearLayout) findViewById(R.id.layout_inside)).addView(imageView);
                        }
                        final TextView[] myTextViews = new TextView[textview_num]; // create an empty array;
                        for (int i = 0; i < divList.size(); i++) {
                            final TextView rowTextView = new TextView(SampleNewsActivity.this);
                            String tmp = divList.get(i);
                            rowTextView.setText(tmp);
                            rowTextView.setTextColor(Color.parseColor("black"));
                            rowTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, text_size);
//                            rowTextView.setGravity(Gravity.LEFT);
                            rowTextView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                            rowTextView.setSingleLine(true);
                            rowTextView.setLayoutParams(params);
                            //rowTextView.setBackgroundColor(0xFFFFFF99);
                            ((LinearLayout) findViewById(R.id.layout_inside)).addView(rowTextView);
                            myTextViews[i] = rowTextView;
//                            Rect bounds = new Rect();
//                            myTextViews[i].getPaint().getTextBounds(myTextViews[i].getText().toString(), 0, myTextViews[i].getText().length(), bounds);
//                            myTextViews[i].measure(0, 0);
//                            int h_dp_unit = pxToDp(myTextViews[i].getMeasuredHeight(), myTextViews[i].getContext());
//                            int w_dp_unit = pxToDp(myTextViews[i].getMeasuredWidth(), myTextViews[i].getContext());
//                            Log.d("log: textview h", String.valueOf(h_dp_unit));
//                            Log.d("log: textview w", String.valueOf(w_dp_unit));
                        }
                        myTextViews[0].measure(0, 0);
                        int h_dp_unit = pxToDp(myTextViews[0].getMeasuredHeight(), myTextViews[0].getContext());
                        myReadingBehavior.setKEY_ROW_SPACING(h_dp_unit);
                        final LinearLayout content_view = findViewById(R.id.layout_inside);
                        ViewTreeObserver viewTreeObserver = content_view.getViewTreeObserver();
                        if (viewTreeObserver.isAlive()) {
                            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                                @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
                                @Override
                                public void onGlobalLayout() {
                                    content_view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                                    int viewWidth = content_view.getWidth();
                                    int viewHeight = content_view.getHeight();
                                    int dp_unit = pxToDp(viewHeight, content_view.getContext());
                                    myReadingBehavior.setKEY_CONTENT_LENGTH(String.valueOf(dp_unit));
                                    Log.d("log: content_length_dp", myReadingBehavior.getKEY_CONTENT_LENGTH());
                                }
                            });
                        }
                        //create document
                        addReadingBehavior();
                        final int N = textview_num;
                        class SquareCalculator {
                            private ExecutorService executor = Executors.newFixedThreadPool(1);
                            //...
                            //private ExecutorService executor = Executors.newSingleThreadExecutor();

                            public Future<Integer> calculate(final Integer input) {
                                return executor.submit(new Callable<Integer>() {
                                    @Override
                                    public Integer call() throws Exception {
                                        Thread.sleep(1000);
                                        return input * input;
                                    }
                                });
                            }

                            public Future<Integer> no_cal(final int input, final int start_count) {
                                return executor.submit(new Callable<Integer>() {
                                    float count_running = 0;
                                    int[] count = new int[N];
                                    boolean[] old_flag = new boolean[N];
                                    boolean[] new_flag = new boolean[N];

                                    @Override
                                    public Integer call() throws Exception {
                                        Arrays.fill(old_flag, Boolean.FALSE);
                                        Arrays.fill(new_flag, Boolean.FALSE);
                                        Arrays.fill(count, 0);
                                        Log.d("log: MyScrollView", "Start");
//                                        myReadingBehavior.setKEY_TIME_ON_PAGE(count_running / 10);
                                        while (!activityEnd) {
//                                            Log.d("log: time_on_page", String.valueOf(myReadingBehavior.getKEY_TIME_ON_PAGE()));
                                            if (activityStopped && !activityEnd) {
                                                Log.d("log: MyScrollView", "Stop");
                                                tmp_record = "";
                                                for (int i = 0; i < N; i++) {
//                                    Log.d("log: MyScrollView", i + " count: " + count[i] / 10);
//                                                    tmp_record+=i+1 + ": " + count[i] / 10 + "\n";
                                                    tmp_record+=count[i] / 10 + "#";
                                                }
                                                myReadingBehavior.setKEY_VIEW_PORT_RECORD(tmp_record);
                                                Log.d("log: view_port_record", myReadingBehavior.getKEY_VIEW_PORT_RECORD());
//                                                myReadingBehavior.setKEY_TIME_ON_PAGE(count_running / 10);
//                                                Log.d("log: time_on_page", String.valueOf(myReadingBehavior.getKEY_TIME_ON_PAGE()));
                                                while (activityStopped) {
                                                    Thread.sleep(100);
                                                }
                                                Log.d("log: MyScrollView", "Restart");
                                            }
                                            Rect scrollBounds = new Rect();
//                        mScrollView.getHitRect(scrollBounds);
                                            int first_view = -1, last_view = -1;
                                            for (int i = 0; i < N; i++) {
                                                if (!myTextViews[i].getLocalVisibleRect(scrollBounds)) {
                                                    new_flag[i] = false;
//                                Log.d(TAG, i + " false");
                                                } else {
                                                    new_flag[i] = true;
                                                    if(first_view==-1){
                                                        first_view = i;
                                                    } else {
                                                        last_view = i;
                                                    }
//                                Log.d(TAG, i + " true");
                                                }

                                                if (old_flag[i] && new_flag[i]) {
                                                    //                            Log.d(TAG, "still visible "+ block + " count: " + count);
                                                    count[i]++;
                                                } else if (old_flag[i] && !new_flag[i]) {
                                                    //                            Log.d(TAG, "no longer visible "+ block + " count: " + count);
                                                    old_flag[i] = new_flag[i];
                                                } else if (!old_flag[i] && new_flag[i]) {
                                                    //                            Log.d(TAG, "start visible "+ block +" count: " + count);
                                                    count[i]++;
                                                    old_flag[i] = new_flag[i];
                                                } else {
                                                    //                            Log.d(TAG, "still not visible "+ block + " count: " + count);
                                                }
//                            Log.d(TAG, i + " count: " + count[i]);
                                            }
                                            Thread.sleep(100);
                                            count_running++;
                                            float temp = count_running/10;
//                                            String output_string = temp + " top: " + (first_view+1) + " bottom: " + (last_view+1) + "\n";
                                            String output_string = temp + "," + (first_view+1) + "," + (last_view+1) + "#";
                                            time_ss+=output_string;
                                            tmp_record = "";
                                            for (int i = 0; i < N; i++) {
//                                                tmp_record+=i+1 + ": " + count[i] / 10 + "\n";
                                                tmp_record+=count[i] / 10 + "#";
                                            }
                                            myReadingBehavior.setKEY_VIEW_PORT_RECORD(tmp_record);
                                        }
                                        Log.d("log: MyScrollView", "Finish");
                                        String finish_record = "";
                                        for (int i = 0; i < N; i++) {
//                            Log.d("log: MyScrollView", i + " count: " + count[i] / 10);
                                            finish_record+=i+1 + ": " + count[i] / 10 + "\n";
                                            finish_record+=count[i] / 10 + "#";
                                        }
                                        myReadingBehavior.setKEY_VIEW_PORT_RECORD(finish_record);
                                        Log.d("log: view_port_record", myReadingBehavior.getKEY_VIEW_PORT_RECORD());
//                                        myReadingBehavior.setKEY_TIME_ON_PAGE(count_running / 10);
//                                        Log.d("log: time_on_page", String.valueOf(myReadingBehavior.getKEY_TIME_ON_PAGE()));
//                        Log.d("log: MyScrollView", "time_on_page: " + count_running / 10);
                                        return 1;
                                    }
                                });
                            }
                        }
                        SquareCalculator squareCalculator = new SquareCalculator();
                        Future<Integer> future1 = squareCalculator.no_cal(1, 0);
                    } else {
                        Log.d("log: firebase", "No such document");
                    }
                } else {
                    Log.d("log: firebase", "get failed with ", task.getException());
                }
            }
        });
//        List<String> content_list = new ArrayList<>();
//        final int paragraph_number = 16; // total number of paragraph
//        content_list.add("美國總統拜登和副手賀錦麗，周四前往國務院發表談話，強調新政府重視外交，將與盟國修復關係，重返國際舞台。拜登宣布了他的外交重大政策，首先是要反制俄羅斯侵略，並將中國大陸視為是具有威脅性的對手，拜登也觸及了大陸的人權問題。拜登雖然未提及敏感的美中台關係，不過他上任後，也首次派出美國戰艦航行台灣海峽。拜登其他的外交政策，還包括取消撤軍德國的計畫、不再支持沙烏地阿拉伯對葉門的軍事行動；另外他也簽署行政命令，將增加美國收容難民的數量。\n");
//        content_list.add("美國總統拜登和副手賀錦麗，周四前往國務院發表談話，拜登開宗明義對外交官員們表示，外交將重回美國對外政策的中心，致力於修復與盟友間的裂痕。\n");
//        content_list.add("美國總統 拜登：「美國回來了！美國回來了！外交回來。」\n");
//        content_list.add("拜登上任後首次公布重大外交政策，他第一個點名的國家就是俄羅斯，強調他不會像川普政府的無作為，將對俄羅的侵略，採取反制行動。\n");
//        content_list.add("美國總統 拜登：「今日的美國任其所為，面對俄羅斯侵略、干預我們的選舉、網路攻擊和對人民下毒，全都結束了！我們不會遲疑，定要俄羅斯付出更高的代價，捍衛美國人利益。」\n");
//        content_list.add("拜登也公開要求俄羅斯政府，釋放反對派派領袖納瓦尼。\n");
//        content_list.add("美國總統 拜登：「他是因為揭露貪腐而被鎖定，應該立刻無條件釋放他。」\n");
//        content_list.add("拜登20分鐘的演說中，中國大陸被點名5次，拜登並沒有具體說明對中政策，但明確將中國列為有威脅的競爭對象。拜登也觸及大陸的人權問題。\n");
//        content_list.add("美國總統 拜登：「我們要正視中國，迎戰他們的經濟施暴，對抗好鬥和強制的脅迫行動。反對中國侵害人權、智慧財產以及全球管理機制。但我們也準備好要和北京合作，基於美國的利益。」\n");
//        content_list.add("美國和歐盟都譴責大陸迫害維吾爾族人，大陸外交部近日也在日內瓦，邀請50多位駐聯合國大使，觀看大陸治理新疆的宣傳影片，大陸也藉此反擊西方，將人權問題政治化。\n");
//        content_list.add("大陸外交部發言人 汪文斌：「不少使節(聯合國)回憶起近年來，應邀訪問新疆的情況，高度讚揚中國政府致力於促進可持續發展、維護社會安寧、保障人權、增進人民福祉，堅決反對有關國家，將人權問題政治化，利用涉疆問題干涉中國內政。」\n");
//        content_list.add("除了人權，美中間另一個敏感議題就是美中台關係，拜登在演說中並雖沒有提及；但美中近日在台海的軍事較勁越趨頻繁，4號，美國第七艦隊對外宣布，伯克級神盾驅逐艦麥肯號 （USS John S. McCain , DDG 56），依國際法執行通過台灣海峽的例行任務。這也是拜登上任來，首次有美戰艦通過台灣海峽。中國大陸也立即回應。\n");
//        content_list.add("大陸外交部發言人 汪文斌：「中方密切關注並全程掌握，美國軍艦過航台灣海峽的情況。中方將時刻繼續保持高度戒備，隨時應對一切威脅挑釁，堅決捍衛國家主權和領土完整。希望美方為地區和平穩定，發揮建設性作用，而不是相反。」\n");
//        content_list.add("拜登其他的外交重大決定，還包括結束支持沙烏地阿拉伯對葉門的攻擊。前任川普政府因為不滿德國積欠北約組織 (NATO)經費，去年7月宣布要從德國撤出1.2萬名美軍；拜登宣布將停止這項政策，北約組織對拜登新政府的回歸，表示歡迎。\n");
//        content_list.add("北約組織秘書長 史托騰伯格：「不可否認，在過去4年，我們面臨挑戰，這也不是秘密了，我們和美國前總統川普有些溝通困難。觀察美國，我們看到兩黨都強力支持NATO。所以我認為跨大西洋的結合，端看我們實際的作為，還有就是過去8年，我們看到有更多美軍駐守歐洲。」\n");
//        content_list.add("拜登也宣布將簽署行政命令，增加美國收容難民數量，從川普時期的1萬5千人增加到12.5萬人。過去四年，川普常以推特宣達外交政策，國務院幾乎失去功能；拜登也在演說中向外交官員保證，他們現在可以各司其職，政府將是他們最大的後盾。\n");
//        List<String> divList = new ArrayList<>();
////        int cut_size = 20;
//        int cut_size = (int) (dpWidth / 15);
////        dynamically adjust
//        for (int i = 0; i < paragraph_number; i++) {
//            String str = content_list.get(i);
//            int remainder = (str.length()) % cut_size;
//            int str_length_byte = str.length();
//            for (char c : str.toCharArray()) {
//                if (chineseUnicodeBlocks.contains(UCharacter.UnicodeBlock.of(c))) {
////                        System.out.println(c + " is chinese");
//                    str_length_byte += 1;
//                }
//            }
//            // 一共要分割成幾段
//            int front = 0, end = 0, iter_char = 0, line_count = 1;
//            char[] c = str.toCharArray();
//            while (true) {
////                System.out.println("line  "+line_count);
//                line_count++;
//                //one paragraph
////                end = front; // index for word
//                int count_byte = 0;
////                System.out.print(c[i]);
////                System.out.println(end.getClass());
//                while (count_byte < cut_size * 2) {
//
//                    //one sentence at most 40 bytes
//                    if (c[iter_char] == '\n') {
//                        break;
//                    }
////                    System.out.println(iter_char);
////                    System.out.println(c[iter_char]);
//                    if (isChineseChar(c[iter_char])) {
////                        System.out.println(c[iter_char] + " is chinese");
//                        count_byte += 2;
//                    } else if ((c[iter_char] >= 'a' && c[iter_char] <= 'z') || (c[iter_char] >= 'A' && c[iter_char] <= 'Z')) {
//                        //english letter
//                        //check word
//                        int word_length = 0, word_index, tmp_count_byte = count_byte;
//                        for (word_index = iter_char; word_index + 1 <= str.length(); word_index++) {
//                            if ((c[word_index] >= 'a' && c[word_index] <= 'z') || (c[word_index] >= 'A' && c[word_index] <= 'Z')) {
//                                word_length += 1;
//                                tmp_count_byte += 1;
//                            } else {
//                                break;
//                            }
//                        }
//                        word_index -= 1;
//                        if (tmp_count_byte < cut_size * 2) {
//                            iter_char = word_index;
//                            count_byte = tmp_count_byte;
//                        } else {
//                            iter_char -= 1;
//                            break;
//                        }
////                        count_byte+=1;
//                    } else {
////                        System.out.println(c[iter_char] + " is not chinese");
//                        count_byte += 1;
//                    }
//                    if (iter_char + 1 < str.length()) {
//                        iter_char += 1;//c[iter_char]
//                    } else {
//                        break;
//                    }
//                }
//                String childStr = str.substring(front, iter_char);
//                divList.add(childStr);
//                front = iter_char;
//                if (iter_char + 1 == str.length()) {
//                    break;
//                }
//            }
//        }
//        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
//        params.setMargins(40, 10, 40, 10);
//        int textview_num = divList.size();
//        myReadingBehavior.setKEY_VIEW_PORT_NUM(textview_num);
//        Log.d("log: view_port_num", String.valueOf(myReadingBehavior.getKEY_VIEW_PORT_NUM()));
//
//
//
////        int text_size = (int) (dpWidth /30);
//        int text_size = 12;
//
//        final TextView myTextViewsTitle = new TextView(this);
//        final TextView myTextViewsDate = new TextView(this);
//        final TextView myTextViewsAuthor = new TextView(this);
//        final TextView myTextViewsSrc = new TextView(this);
//        myTextViewsTitle.setText(mTitle);
//        myTextViewsDate.setText(mDate);
//        myTextViewsAuthor.setText(mAuthor);
//        myTextViewsSrc.setText(mSource);
//        myTextViewsTitle.setTextColor(Color.parseColor("black"));
//        myTextViewsDate.setTextColor(Color.parseColor("black"));
//        myTextViewsAuthor.setTextColor(Color.parseColor("black"));
//        myTextViewsSrc.setTextColor(Color.parseColor("black"));
//        myTextViewsTitle.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
//        myTextViewsDate.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 10);
//        myTextViewsAuthor.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 10);
//        myTextViewsSrc.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 10);
//        myTextViewsTitle.setGravity(Gravity.LEFT);
//        myTextViewsDate.setGravity(Gravity.LEFT);
//        myTextViewsAuthor.setGravity(Gravity.LEFT);
//        myTextViewsSrc.setGravity(Gravity.LEFT);
//        myTextViewsTitle.setLayoutParams(params);
//        myTextViewsDate.setLayoutParams(params);
//        myTextViewsAuthor.setLayoutParams(params);
//        myTextViewsSrc.setLayoutParams(params);
//        ((LinearLayout) findViewById(R.id.layout_inside)).addView(myTextViewsTitle);
//        ((LinearLayout) findViewById(R.id.layout_inside)).addView(myTextViewsDate);
//        ((LinearLayout) findViewById(R.id.layout_inside)).addView(myTextViewsAuthor);
//        ((LinearLayout) findViewById(R.id.layout_inside)).addView(myTextViewsSrc);
////        ImageView image = listItemView.findViewById(R.id.article_image);
//
//        ImageView imageView = new ImageView(this);
//        new DownloadImageTask(imageView).execute(mImg);
//        ((LinearLayout) findViewById(R.id.layout_inside)).addView(imageView);
//
//        final TextView[] myTextViews = new TextView[textview_num]; // create an empty array;
//        for (int i = 0; i < divList.size(); i++) {
//            final TextView rowTextView = new TextView(this);
//            String tmp = divList.get(i);
//            rowTextView.setText(tmp);
//            rowTextView.setTextColor(Color.parseColor("black"));
////            rowTextView.setTextSize(TypedValue.COMPLEX_UNIT_MM, 2);
//            rowTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, text_size);
////            rowTextView.setWidth(250);
//            rowTextView.setGravity(Gravity.LEFT);
//            rowTextView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
////            rowTextView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, width));
//            rowTextView.setSingleLine(true);
////            rowTextView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, height));
//
//            rowTextView.setLayoutParams(params);
////            rowTextView.setBackgroundColor(0xFFFFFF99);
//            ((LinearLayout) findViewById(R.id.layout_inside)).addView(rowTextView);
//
//            myTextViews[i] = rowTextView;
//        }
        // textview generate JSON ##################################################################
//        final int N = textview_num;
//        class SquareCalculator {
//            private ExecutorService executor = Executors.newFixedThreadPool(1);
//            //...
//            //private ExecutorService executor = Executors.newSingleThreadExecutor();
//
//            public Future<Integer> calculate(final Integer input) {
//                return executor.submit(new Callable<Integer>() {
//                    @Override
//                    public Integer call() throws Exception {
//                        Thread.sleep(1000);
//                        return input * input;
//                    }
//                });
//            }
//
//            public Future<Integer> no_cal(final int input, final int start_count) {
//                return executor.submit(new Callable<Integer>() {
//                    int count_running = 0;
//                    int[] count = new int[N];
//                    boolean[] old_flag = new boolean[N];
//                    boolean[] new_flag = new boolean[N];
//
//                    @Override
//                    public Integer call() throws Exception {
//                        Arrays.fill(old_flag, Boolean.FALSE);
//                        Arrays.fill(new_flag, Boolean.FALSE);
//                        Arrays.fill(count, 0);
//                        Log.d("log: MyScrollView", "Start");
//                        while (!activityEnd) {
//                            if (activityStopped && !activityEnd) {
//                                Log.d("log: MyScrollView", "Stop");
//                                String tmp_record = "";
//                                for (int i = 0; i < N; i++) {
////                                    Log.d("log: MyScrollView", i + " count: " + count[i] / 10);
//                                    tmp_record+=i+1 + ": " + count[i] / 10 + "\n";
//                                }
//                                myReadingBehavior.setKEY_VIEW_PORT_RECORD(tmp_record);
//                                Log.d("log: view_port_record", myReadingBehavior.getKEY_VIEW_PORT_RECORD());
//                                myReadingBehavior.setKEY_TIME_ON_PAGE(String.valueOf(count_running / 10));
//                                Log.d("log: time_on_page", myReadingBehavior.getKEY_TIME_ON_PAGE());
//                                while (activityStopped) {
//                                    Thread.sleep(100);
//                                }
//                                Log.d("log: MyScrollView", "Restart");
//                            }
//                            Rect scrollBounds = new Rect();
////                        mScrollView.getHitRect(scrollBounds);
//                            int first_view = -1, last_view = -1;
//                            for (int i = 0; i < N; i++) {
//                                if (!myTextViews[i].getLocalVisibleRect(scrollBounds)) {
//                                    new_flag[i] = false;
////                                Log.d(TAG, i + " false");
//                                } else {
//                                    new_flag[i] = true;
//                                    if(first_view==-1){
//                                        first_view = i;
//                                    } else {
//                                        last_view = i;
//                                    }
////                                Log.d(TAG, i + " true");
//                                }
//
//                                if (old_flag[i] && new_flag[i]) {
//                                    //                            Log.d(TAG, "still visible "+ block + " count: " + count);
//                                    count[i]++;
//                                } else if (old_flag[i] && !new_flag[i]) {
//                                    //                            Log.d(TAG, "no longer visible "+ block + " count: " + count);
//                                    old_flag[i] = new_flag[i];
//                                } else if (!old_flag[i] && new_flag[i]) {
//                                    //                            Log.d(TAG, "start visible "+ block +" count: " + count);
//                                    count[i]++;
//                                    old_flag[i] = new_flag[i];
//                                } else {
//                                    //                            Log.d(TAG, "still not visible "+ block + " count: " + count);
//                                }
////                            Log.d(TAG, i + " count: " + count[i]);
//                            }
//                            Thread.sleep(100);
//                            count_running++;
//                            float temp = count_running/10;
//                            String output_string = temp + " top: " + first_view + " bottom: " + last_view + "\n";
//                            time_ss+=output_string;
//                        }
//                        Log.d("log: MyScrollView", "Finish");
//                        String finish_record = "";
//                        for (int i = 0; i < N; i++) {
////                            Log.d("log: MyScrollView", i + " count: " + count[i] / 10);
//                            finish_record+=i+1 + ": " + count[i] / 10 + "\n";
//                        }
//                        myReadingBehavior.setKEY_VIEW_PORT_RECORD(finish_record);
//                        Log.d("log: view_port_record", myReadingBehavior.getKEY_VIEW_PORT_RECORD());
//                        myReadingBehavior.setKEY_TIME_ON_PAGE(String.valueOf(count_running / 10));
//                        Log.d("log: time_on_page", myReadingBehavior.getKEY_TIME_ON_PAGE());
////                        Log.d("log: MyScrollView", "time_on_page: " + count_running / 10);
//                        return 1;
//                    }
//                });
//            }
//        }
//        SquareCalculator squareCalculator = new SquareCalculator();
//        Future<Integer> future1 = squareCalculator.no_cal(1, 0);
        // screen total height #####################################################################
//        final LinearLayout content_view = findViewById(R.id.layout_inside);
//        ViewTreeObserver viewTreeObserver = content_view.getViewTreeObserver();
//        if (viewTreeObserver.isAlive()) {
//            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
//                @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
//                @Override
//                public void onGlobalLayout() {
//                    content_view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
//                    int viewWidth = content_view.getWidth();
//                    int viewHeight = content_view.getHeight();
//                    int dp_unit = pxToDp(viewHeight, content_view.getContext());
//                    myReadingBehavior.setKEY_CONTENT_LENGTH(String.valueOf(dp_unit));
//                    Log.d("log: content_length_dp", myReadingBehavior.getKEY_CONTENT_LENGTH());
//                }
//            });
//        }
        //drag position ############################################################################

//        final LinearLayout outside_view = findViewById(R.id.layout_outside);
//        imageView.setOnDragListener(new View.OnDragListener() {
//            @Override
//            public boolean onDrag(View v, DragEvent event) {
//                //get event positions
//                switch (event.getAction()) {
//                    case DragEvent.ACTION_DRAG_STARTED:
//                        //etc etc. do some stuff with the drag event
//                        Log.d("log: DragListener", "ACTION_DRAG_STARTED");
//                        break;
//                    case DragEvent.ACTION_DRAG_LOCATION:
//                        Point touchPosition = getTouchPositionFromDragEvent(v, event);
//                        Log.d("log: DragListener", String.valueOf(touchPosition));
//                        //do something with the position (a scroll i.e);
//                        break;
//                    default:
//                        Log.d("log: DragListener", "************");
//                }
//                return true;
//            }
//        });
        // screen touch position ###################################################################
//        content_view.setOnTouchListener(new View.OnTouchListener() {
//            @SuppressLint("ClickableViewAccessibility")
//            @Override
//            public boolean onTouch(View view, MotionEvent motionEvent) {
//                switch (motionEvent.getAction()) {
//                    case MotionEvent.ACTION_MOVE:
////                        Log.d("position", "moving: (" + motionEvent.getX() + ", " + motionEvent.getY() + ")");
//                        return true;
//                    case MotionEvent.ACTION_DOWN:
//                        Log.d("log: on touch", "touched down");
//                        Log.d("log: on touch", "moving: (" +  motionEvent.getXPrecision()*motionEvent.getX() + ", " + motionEvent.getY() + ")");
//                        return true;
//                    case MotionEvent.ACTION_UP:
//                        Log.d("log: on touch", "touched up");
//                        Log.d("log: on touch", "moving: (" + motionEvent.getX() + ", " + motionEvent.getY() + ")");
//                        return true;
//                }
//                Log.d("log: on touch", "pressure: " + motionEvent.getPressure());
//                return false;
//            }
//        });
        // on scroll change ########################################################################
//        content_view.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
//            @Override
//            public void onScrollChanged() {
//                int scrollY = content_view.getScrollY();
//                //Log.d("scrollY ",  Integer.toString(scrollY));
//            }
//        });
//        super.onCreate(savedInstanceState);
    }
    @Override
    protected void onStart() {
        super.onStart();
        Log.d("log: activity cycle", "On start");
        activityStopped = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("log: activity cycle", "On resume");
        activityStopped = false;
        //isScreenOn(R.layout.activity_news_detail);
        in_time = System.currentTimeMillis();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("log: activity cycle", "On pause");
        activityStopped = true;
        myReadingBehavior.setKEY_PAUSE_ON_PAGE(myReadingBehavior.getKEY_PAUSE_ON_PAGE()+1);
        long tmp = myReadingBehavior.getKEY_TIME_ON_PAGE() + (System.currentTimeMillis()-in_time)/1000;
        myReadingBehavior.setKEY_TIME_ON_PAGE(tmp);
//        Log.d("log: pause count", String.valueOf(myReadingBehavior.getKEY_PAUSE_ON_PAGE()));
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
        myReadingBehavior.setKEY_TIME_OUT(formatter.format(date));
//        Log.d("log: time_out", myReadingBehavior.getKEY_TIME_OUT());

        myReadingBehavior.setKEY_TIME_SERIES(time_ss);
        Log.d("log: time_series", myReadingBehavior.getKEY_TIME_SERIES());

//        myReadingBehavior.setKEY_PAUSE_ON_PAGE(myReadingBehavior.getKEY_PAUSE_ON_PAGE()-1);
//        Log.d("log: fling_count", String.valueOf(myReadingBehavior.getKEY_FLING_NUM()));
//        Log.d("log: fling_record", String.valueOf(myReadingBehavior.getKEY_FLING_RECORD()));
//        Log.d("log: final pause count", String.valueOf(myReadingBehavior.getKEY_PAUSE_ON_PAGE()));
//        Log.d("log: share", String.valueOf(myReadingBehavior.getKEY_SHARE()));
//        activityStopped = true;
//        activityEnd = true;
//        if (mReceiver != null) {
//            unregisterReceiver(mReceiver);
//        }
        String drag_str = "";
        int drag_count = 0;
        float drag_x_1 = 0;
        float drag_y_1 = 0;
        float drag_x_2 = 0;
        float drag_y_2 = 0;
        long time_one = 0, time_two = 0, tmp_long = 0;
        double duration = 0;

        for(int iter = 0; iter < dragObjArrayListArray.size(); iter++){
            if (drag_x_1==0 && drag_y_1==0){
                time_one = dragObjArrayListArray.get(iter).getTIME_ONE();
                drag_x_1 = dragObjArrayListArray.get(iter).getPOINT_ONE_X();
                drag_y_1 = dragObjArrayListArray.get(iter).getPOINT_ONE_Y();
                drag_x_2 = dragObjArrayListArray.get(iter).getPOINT_TWO_X();
                drag_y_2 = dragObjArrayListArray.get(iter).getPOINT_TWO_Y();
            } else if (drag_x_1==dragObjArrayListArray.get(iter).getPOINT_ONE_X() && drag_y_1==dragObjArrayListArray.get(iter).getPOINT_ONE_Y()){
                time_two = dragObjArrayListArray.get(iter).getTIME_ONE();
                drag_x_2 = dragObjArrayListArray.get(iter).getPOINT_TWO_X();
                drag_y_2 = dragObjArrayListArray.get(iter).getPOINT_TWO_Y();
            } else {
                //find end
//                duration = (time_two-time_one)/1000;
                drag_count+=1;
                if(time_one!=time_two){
                    tmp_long = time_two-time_one;
                    duration = (double)tmp_long/1000;
                } else {
                    duration = 0;
                }
//                drag_str+="duration:" + time_two + " " + time_one + "\n";
//                Log.d("log: drag_str", drag_str);
//                Log.d("log: drag_str", String.valueOf(drag_count));
//                Log.d("log: drag_str", drag_str);
//                Log.d("log: drag_str", drag_str);

//                drag_str+="drag num:" + drag_count + "\n";
//                drag_str+="duration:" + duration + "\n";
//                drag_str+="point1: (" + drag_x_1 + ", " + drag_y_1 + ")\n";
//                drag_str+="point2: (" + drag_x_2 + ", " + drag_y_2 + ")\n";
//                String direction = "";
//                direction += drag_y_1 < drag_y_2 ? "N" : drag_y_1 > drag_y_2 ? "S" : "";
//                direction += drag_x_1 < drag_x_2 ? "E" : drag_x_1 > drag_x_2 ? "W" : "";
//                drag_str+="direction:" + direction + "\n#";

                drag_str+=duration + "/";
                drag_str+="(" + drag_x_1 + "," + drag_y_1 + ")/";
                drag_str+="(" + drag_x_2 + "," + drag_y_2 + ")/";
                String direction = "";
                direction += drag_y_1 < drag_y_2 ? "N" : drag_y_1 > drag_y_2 ? "S" : "";
                direction += drag_x_1 < drag_x_2 ? "E" : drag_x_1 > drag_x_2 ? "W" : "";
                drag_str+=direction + "#";

                drag_x_1 = 0;
                drag_y_1 = 0;
                drag_x_2 = 0;
                drag_y_2 = 0;
                duration = 0;
            }
        }
        //last drag
        if ((drag_x_1+drag_y_1+drag_x_2+drag_y_2)==0){
            //end at final else
//            Log.d("log: drag_str", "123");
        } else {
            drag_count+=1;
            if(time_one!=time_two){
                tmp_long = time_two-time_one;
                duration = (double) tmp_long/1000;
            } else {
                duration = 0;
            }
//            drag_str+="duration:" + time_two + " " + time_one + "\n";
//            Log.d("log: drag_str", drag_str);
//            drag_str+="drag num:" + drag_count + "\n";
//            drag_str+="duration:" + duration + "\n";
//            drag_str+="point1: (" + drag_x_1 + ", " + drag_y_1 + ")\n";
//            drag_str+="point2: (" + drag_x_2 + ", " + drag_y_2 + ")\n";
//            String direction = "";
//            direction += drag_y_1 < drag_y_2 ? "N" : drag_y_1 > drag_y_2 ? "S" : "";
//            direction += drag_x_1 < drag_x_2 ? "E" : drag_x_1 > drag_x_2 ? "W" : "";
//            drag_str+="direction:" + direction + "\n#";
            drag_str+=duration + "/";
            drag_str+="(" + drag_x_1 + "," + drag_y_1 + ")/";
            drag_str+="(" + drag_x_2 + "," + drag_y_2 + ")/";
            String direction = "";
            direction += drag_y_1 < drag_y_2 ? "N" : drag_y_1 > drag_y_2 ? "S" : "";
            direction += drag_x_1 < drag_x_2 ? "E" : drag_x_1 > drag_x_2 ? "W" : "";
            drag_str+=direction + "#";
        }
        myReadingBehavior.setKEY_DRAG_NUM(drag_count);
        myReadingBehavior.setKEY_DRAG_RECORD(drag_str);
//        Log.d("log: drag_count", String.valueOf(myReadingBehavior.getKEY_DRAG_NUM()));
//        Log.d("log: drag_record", String.valueOf(myReadingBehavior.getKEY_DRAG_RECORD()));
//        dbHandler.insertReadingBehaviorDetails(myReadingBehavior);
//        addReadingBehavior();
//        if(document_create==false){
//            addReadingBehavior();
//        } else {
//            updateReadingBehavior();
//        }
        updateReadingBehavior();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onStop() {
        super.onStop();
        Log.d("log: activity cycle", "On stop");
        activityStopped = true;

    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d("log: activity cycle", "On restart");
        activityStopped = false;
//        in_time = System.currentTimeMillis();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("log: activity cycle", "On destroy");
        activityEnd = true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        supportFinishAfterTransition();
    }

    public int pxToDp(int px, Context tmp) {
        DisplayMetrics displayMetrics = tmp.getResources().getDisplayMetrics();
        int dp = Math.round(px / (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        return dp;
    }

    private boolean isChineseChar(char c) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
        if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
                || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION
                || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION ) {
            return true;
        }
        return false;
    }

    //image download ###############################################################################
    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_sample_news, menu);
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if  (id == R.id.share){
            Toast.makeText(this, "share is being clicked", Toast.LENGTH_LONG).show();
            String share_field = "";
//            final DocumentReference rbRef = db.collection(Build.ID).document(String.valueOf(l_date)).collection("reading_behaviors").document(myReadingBehavior.getKEY_TIME_IN());
            final DocumentReference rbRef = db.collection("test_users").document(Build.ID).collection("reading_behaviors").document(myReadingBehavior.getKEY_TIME_IN());
            if(share_clicked){
//                Date date = new Date(System.currentTimeMillis());
//                SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
//                String time_now = formatter.format(date);
//                share_field = time_now;
//                rbRef.update("share", FieldValue.arrayUnion(share_field));
                rbRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                Log.d("log: firebase", "Success");
                                List<String> share_result = (List<String>) document.get("share");
                                share_result.add("none");
//                                share_result.set(0,"none");
                                rbRef.update("share", share_result)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Log.d("log: firebase", "DocumentSnapshot successfully updated!");
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.w("log: firebase", "Error updating document", e);
                                            }
                                        });
                            } else {
                                Log.d("log: firebase", "No such document");
                            }
                        } else {
                            Log.d("log: firebase", "get failed with ", task.getException());
                        }
                    }
                });

            } else {
                //first time
                share_clicked = true;

                rbRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                Log.d("log: firebase", "Success");
                                List<String> share_result = (List<String>) document.get("share");
                                share_result.set(0,"none");
                                rbRef.update("share", share_result)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Log.d("log: firebase", "DocumentSnapshot successfully updated!");
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.w("log: firebase", "Error updating document", e);
                                            }
                                        });
                            } else {
                                Log.d("log: firebase", "No such document");
                            }
                        } else {
                            Log.d("log: firebase", "get failed with ", task.getException());
                        }
                    }
                });

//                Date date = new Date(System.currentTimeMillis());
//                SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
//                String time_now = formatter.format(date);
//                share_field = time_now;
//                Map<String, Object> data = new HashMap<>();
//                rbRef.update("share", FieldValue.arrayRemove("NA"));
//                rbRef.update("share", FieldValue.arrayUnion(share_field));
//                db.collection(Build.ID)
//                        .document(String.valueOf(l_date))
//                        .collection("reading_behaviors")
//                        .document(myReadingBehavior.getKEY_TIME_IN())
//                        .set(data, SetOptions.merge());
            }



//            myReadingBehavior.setKEY_SHARE(1);
            try{
//                Intent i = new Intent(Intent.ACTION_SEND);
//                i.setType("text/plan");
//                i.putExtra(Intent.EXTRA_SUBJECT, mSource);
//                String body = mTitle + "\n" + mUrl + "\n" + "Share from the News App" + "\n";
//                i.putExtra(Intent.EXTRA_TEXT, body);
//                startActivity(Intent.createChooser(i, "Share with :"));
                String url = mUrl;
//                Toast.makeText(this, "share is being clicked", Toast.LENGTH_LONG).show();
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT,url); // your above url

                Intent receiver = new Intent(this, ApplicationSelectorReceiver.class);
                receiver.putExtra("doc_time", myReadingBehavior.getKEY_TIME_IN());
                receiver.putExtra("doc_date", String.valueOf(l_date));
                receiver.putExtra("share_field", share_field);
//                receiver.putExtra("sh", String.valueOf(l_date));
//                receiver.putExtra("share_via", "none");
                PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, receiver, PendingIntent.FLAG_UPDATE_CURRENT);
                Intent chooser = Intent.createChooser(shareIntent, "Share via...", pendingIntent.getIntentSender());
//                Log.d("log: share via", String.valueOf(pendingIntent.getIntentSender()));
                startActivity(chooser);

            }catch (Exception e){
                Toast.makeText(this, "Hmm.. Sorry, \nCannot be share", Toast.LENGTH_SHORT).show();
            }
        }
//        else if (id == R.id.action_esm){
//            Intent intent = new Intent();
////            intent.setClass(NewsDetailActivity.this, ExampleSurveyActivity.class);
//            intent.setClass(SampleNewsActivity.this, ExampleSurveyActivity.class);
//            startActivity(intent);
//        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent me){
        this.detector.onTouchEvent(me);
        return super.dispatchTouchEvent(me);
    }

    @Override
    public void onSwipe(int direction, FlingObj flingObj) {
        myReadingBehavior.setKEY_FLING_NUM(myReadingBehavior.getKEY_FLING_NUM()+1);
        flingObj.setFLING_ID(myReadingBehavior.getKEY_FLING_NUM());
        String str_fling = myReadingBehavior.getKEY_FLING_RECORD();
//        str_fling+="fling num:" + flingObj.getFLING_ID() + "/";
        str_fling+="(" + flingObj.getPOINT_ONE_X() + "," + flingObj.getPOINT_ONE_Y() + ")/";
        str_fling+="(" + flingObj.getPOINT_TWO_X() + "," + flingObj.getPOINT_TWO_Y() + ")/";
        str_fling+=flingObj.getDISTANCE_X() + "/";
        str_fling+=flingObj.getDISTANCE_Y() + "/";
        str_fling+=flingObj.getVELOCITY_X() + "/";
        str_fling+=flingObj.getVELOCITY_Y() + "/";
//        str_fling+="fling num:" + flingObj.getFLING_ID() + "\n";
//        str_fling+="point1: (" + flingObj.getPOINT_ONE_X() + ", " + flingObj.getPOINT_ONE_Y() + ")\n";
//        str_fling+="point2: (" + flingObj.getPOINT_TWO_X() + ", " + flingObj.getPOINT_TWO_Y() + ")\n";
//        str_fling+="distance_x:" + flingObj.getDISTANCE_X() + "\n";
//        str_fling+="distance_y:" + flingObj.getDISTANCE_Y() + "\n";
//        str_fling+="velocity_x:" + flingObj.getVELOCITY_X() + "\n";
//        str_fling+="velocity_y:" + flingObj.getVELOCITY_Y() + "\n";
        String direction_f = "";
        direction_f += flingObj.getPOINT_ONE_Y() < flingObj.getPOINT_TWO_Y() ? "N" : flingObj.getPOINT_ONE_Y() > flingObj.getPOINT_TWO_Y() ? "S" : "";
        direction_f += flingObj.getPOINT_ONE_X() < flingObj.getPOINT_TWO_X() ? "E" : flingObj.getPOINT_ONE_X() > flingObj.getPOINT_TWO_X() ? "W" : "";
        str_fling+=direction_f + "#";
//        switch (direction) {
//            case MySimpleGestureListener.SWIPE_RIGHT : str_fling += "Swipe Right#";
//                break;
//            case MySimpleGestureListener.SWIPE_LEFT : str_fling += "Swipe Left#";
//                break;
//            case MySimpleGestureListener.SWIPE_DOWN : str_fling += "Swipe Down#";
//                break;
//            case MySimpleGestureListener.SWIPE_UP : str_fling += "Swipe Up#";
//                break;
//        }
        myReadingBehavior.setKEY_FLING_RECORD(str_fling);
//        Toast.makeText(this, str_fling, Toast.LENGTH_SHORT).show();
//        Log.d("log: per_fling_record", myReadingBehavior.getKEY_FLING_RECORD());
    }

    @Override
    public void onOnePoint(DragObj dragObj) {

        dragObjArrayListArray.add(dragObj);
//        Log.d("log: drag_debug", "point1: (" + dragObj.getPOINT_ONE_X() + ", " + dragObj.getPOINT_ONE_Y() + ")\n");
//        Log.d("log: drag_debug", "point2: (" + dragObj.getPOINT_TWO_X() + ", " + dragObj.getPOINT_TWO_Y() + ")\n");
    }

//    @Override
//    public void setIsLongpressEnabled(boolean isLongpressEnabled) {
//        isLongpressEnabled = false;
//    }

//    @Override
//    public void onDoubleTap() {
////        Toast.makeText(this, "Double Tap", Toast.LENGTH_SHORT).show();
//    }

    public static Point getTouchPositionFromDragEvent(View item, DragEvent event) {
        Rect rItem = new Rect();
        item.getGlobalVisibleRect(rItem);
        return new Point(rItem.left + Math.round(event.getX()), rItem.top + Math.round(event.getY()));
    }

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    LocalDate l_date = LocalDate.now();
//    LocalDateTime now = LocalDateTime.now();
//    int hour = now.getHour();
//    int minute = now.getMinute();
//    int second = now.getSecond();
//    String time_string = hour + ":" + minute + ":" + second;

    @SuppressLint("MissingPermission")
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void addReadingBehavior() {
        List<String> in_tt = new ArrayList<String>(Arrays.asList(myReadingBehavior.getKEY_TIME_IN().split(" ")));
        // [START add_ada_lovelace]
        // Create a new user with a first and last name
        Map<String, Object> readingBehavior = new HashMap<>();
        readingBehavior.put("news_id",  myReadingBehavior.getKEY_NEWS_ID());
        readingBehavior.put("trigger_by", myReadingBehavior.getKEY_TRIGGER_BY());
//        readingBehavior.put("time_in", myReadingBehavior.getKEY_TIME_IN());
        readingBehavior.put("in_date", in_tt.get(0));
        readingBehavior.put("in_time", in_tt.get(2));
        readingBehavior.put("out_date", "NA");
        readingBehavior.put("out_time", "NA");
        readingBehavior.put("content_length(dp)", "NA");
        readingBehavior.put("display_width(dp)", myReadingBehavior.getKEY_DISPLAY_WIDTH());
        readingBehavior.put("display_height(dp)", myReadingBehavior.getKEY_DISPLAY_HEIGHT());
        readingBehavior.put("time_on_page(s)", myReadingBehavior.getKEY_TIME_ON_PAGE());
        readingBehavior.put("pause_count", myReadingBehavior.getKEY_PAUSE_ON_PAGE());
        readingBehavior.put("viewport_num", myReadingBehavior.getKEY_VIEW_PORT_NUM());
        readingBehavior.put("viewport_record", Arrays.asList("NA"));
        readingBehavior.put("fling_num", myReadingBehavior.getKEY_FLING_NUM());
        readingBehavior.put("fling_record", Arrays.asList("NA"));
        readingBehavior.put("drag_num", myReadingBehavior.getKEY_DRAG_NUM());
        readingBehavior.put("drag_record", Arrays.asList("NA"));
        readingBehavior.put("share", Arrays.asList("NA"));
//        readingBehavior.put("share_via", "none");
        readingBehavior.put("time_series(s)", Arrays.asList("NA"));
        readingBehavior.put("byte_per_line", myReadingBehavior.getKEY_BYTE_PER_LINE());
        readingBehavior.put("row_spacing(dp)", myReadingBehavior.getKEY_ROW_SPACING());

//        LocalDate l_date = LocalDate.now();
//        myReadingBehavior.setKEY_TIME_OUT(formatter.format(date));
        // Add a new document with my id ///////////////////////a generated ID
        TelephonyManager telephonyManager;
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        db.collection("test_users")
                .document(Build.ID)
                .collection("reading_behaviors")
                .document(myReadingBehavior.getKEY_TIME_IN())
                .set(readingBehavior);
//        db.collection(Build.ID)
//                .document(String.valueOf(l_date))
//                .collection("reading_behaviors")
//                .document(myReadingBehavior.getKEY_TIME_IN())
//                .set(readingBehavior);
        document_create = true;
//                .add(readingBehavior)
//                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
//                    @Override
//                    public void onSuccess(DocumentReference documentReference) {
//                        Log.d("log: firebase", "DocumentSnapshot added with ID: " + documentReference.getId());
//                        //Log.d( tag: "firebase", "DocumentSnapshot added with ID: " + documentReference.getId());
//                    }
//                })
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        Log.d("log: firebase", "Error adding document");
//                        //Log.w(tag: "firebase", "Error adding document", e);
//                    }
//                });
    }
    public void updateReadingBehavior(){
        TelephonyManager telephonyManager;
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
//        DocumentReference rbRef = db.collection(Build.ID).document(String.valueOf(l_date)).collection("reading_behaviors").document(myReadingBehavior.getKEY_TIME_IN());
        @SuppressLint("MissingPermission")
        DocumentReference rbRef = db.collection("test_users").document(Build.ID).collection("reading_behaviors").document(myReadingBehavior.getKEY_TIME_IN());

        List<String> time_series_list = new ArrayList<String>(Arrays.asList(myReadingBehavior.getKEY_TIME_SERIES().split("#")));
        List<String> viewport_record_list = new ArrayList<String>(Arrays.asList(myReadingBehavior.getKEY_VIEW_PORT_RECORD().split("#")));
        List<String> drag_record_list = new ArrayList<String>(Arrays.asList(myReadingBehavior.getKEY_DRAG_RECORD().split("#")));
        List<String> fling_record_list = new ArrayList<String>(Arrays.asList(myReadingBehavior.getKEY_FLING_RECORD().split("#")));
        for (int i = 0; i < fling_record_list.size(); i++) {
//            System.out.println(time_series_list.get(i));
            Log.d("log: firebase", fling_record_list.get(i));
        }
//        for (int i = 0; i < drag_record_list.size(); i++) {
////            System.out.println(time_series_list.get(i));
//            Log.d("log: firebase", drag_record_list.get(i));
//        }
        List<String> out_tt = new ArrayList<String>(Arrays.asList(myReadingBehavior.getKEY_TIME_OUT().split(" ")));

        // Set the "isCapital" field of the city 'DC'
        rbRef.update("content_length(dp)", myReadingBehavior.getKEY_CONTENT_LENGTH(),
                "drag_num", myReadingBehavior.getKEY_DRAG_NUM(),
                "drag_record", drag_record_list,
                "fling_num", myReadingBehavior.getKEY_FLING_NUM(),
                "fling_record", fling_record_list,
                "pause_count", myReadingBehavior.getKEY_PAUSE_ON_PAGE(),//auto
//                "share", myReadingBehavior.getKEY_SHARE(),//none
//                "share_via", "none",//none
                "time_on_page(s)", myReadingBehavior.getKEY_TIME_ON_PAGE(),//auto
//                "time_out", myReadingBehavior.getKEY_TIME_OUT(),
                "out_time", out_tt.get(2),
                "out_date", out_tt.get(0),
                "time_series(s)", time_series_list,//auto
                "viewport_record", viewport_record_list)//auto
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("log: firebase", "DocumentSnapshot successfully updated!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("log: firebase", "Error updating document", e);
                    }
                });
    }


}