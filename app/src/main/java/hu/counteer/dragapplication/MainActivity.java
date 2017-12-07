package hu.counteer.dragapplication;

import android.content.ClipData;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.media.Image;
import android.preference.PreferenceGroup;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.DragEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    Map<Integer, BoardElement> gameBoard;
    Player player;
    private Map<String, Tuple> pairingsWithTag;
    private int round;
    private List<String> tags = Arrays.asList("yellow", "blue", "purple", "red");
    private ImageView todrag;
    private TextView timerText;
    private TextView playerNameText;


    private List<ImageView> toDrops;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toDrops = new ArrayList<>();
        todrag = (ImageView) findViewById(R.id.drag1);
        toDrops.add((ImageView) findViewById(R.id.target1));
        toDrops.add((ImageView) findViewById(R.id.target2));
        toDrops.add((ImageView) findViewById(R.id.target3));
        toDrops.add((ImageView) findViewById(R.id.target4));


        timerText = (TextView) findViewById(R.id.timer_text);
        playerNameText = (TextView) findViewById(R.id.player_name_text);
        initDefaultSettings();
        initBoard();
        initPlayer();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    private void initDefaultSettings() {

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        preferences.registerOnSharedPreferenceChangeListener(this);
        boolean isTimerShown = preferences.getBoolean(getString(R.string.pref_show_time_key),
                getResources().getBoolean(R.bool.pref_show_time_default));
        Log.i("main", isTimerShown + "");
        timerText.setVisibility(isTimerShown?View.VISIBLE:View.INVISIBLE);
        playerNameText.setText(preferences.getString(getString(R.string.pref_player_name_key),""));
        String boardTileShape = preferences.getString(getString(R.string.pref_board_element_shape_key), "square");
        pairingsWithTag = new HashMap<>();
        switch (boardTileShape) {
            case "circle":
                pairingsWithTag.put("red", new Tuple(R.drawable.full_circ, R.drawable.out_circ));
                pairingsWithTag.put("blue", new Tuple(R.drawable.full_circ_blue, R.drawable.out_circ_blue));
                pairingsWithTag.put("yellow", new Tuple(R.drawable.full_circ_yellow, R.drawable.out_circ_yellow));
                pairingsWithTag.put("purple", new Tuple(R.drawable.full_circ_purple, R.drawable.out_circ_purple));
                break;
            case "square":
                pairingsWithTag.put("red", new Tuple(R.drawable.full_sq, R.drawable.out_sq));
                pairingsWithTag.put("blue", new Tuple(R.drawable.full_sq_blue, R.drawable.out_sq_blue));
                pairingsWithTag.put("yellow", new Tuple(R.drawable.full_sq_yellow, R.drawable.out_sq_yellow));
                pairingsWithTag.put("purple", new Tuple(R.drawable.full_sq_purple, R.drawable.out_sq_purple));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int actualMenu = item.getItemId();
        if(actualMenu == R.id.action_restart){
            initDefaultSettings();
            initBoard();
            initPlayer();
        }
        if (actualMenu == R.id.action_settings) {
            Intent startSettingsActivity = new Intent(this, SettingsActivity.class);
            startActivity(startSettingsActivity);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initBoard(){

        round = 0;
        todrag.setOnTouchListener(new CustomOnTouchListener() );
        List<String> randomTags = getRandomTagList();
        String actualTag ;
        for(int i = 0; i < 4; ++i){
            actualTag = randomTags.get(i);
            toDrops.get(i).setImageResource(pairingsWithTag.get(actualTag).getBoardElementColor());
            toDrops.get(i).setTag(actualTag);
            toDrops.get(i).setOnDragListener(new CustomDragListener());
        }
    }

    private void initPlayer() {
        ImageView todrag = (ImageView) findViewById(R.id.drag1);
        if(round<4){
            this.player = new Player(1);
            String tag = getRandomTag();
            todrag.setImageResource(pairingsWithTag.get(tag).getPlayerColor());
            todrag.setTag(getRandomTag());
            round++;
        } else {
            Toast.makeText(MainActivity.this, "You Won!", Toast.LENGTH_LONG).show();
            todrag.setOnTouchListener(null);
        }
    }
    private String getRandomTag(){
        switch (round){
            case 1: return "red";
            case 2: return "yellow";
            case 3: return "blue";
            case 0: return "purple";
            default: return "notag";
        }
    }

    private List<String> getRandomTagList(){
        Collections.shuffle(tags);
        return tags;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(key.equals(getString(R.string.pref_show_time_key))){
            boolean isTimerShown = sharedPreferences.getBoolean(key, getResources().getBoolean(R.bool.pref_show_time_default));
            timerText.setVisibility(isTimerShown?View.VISIBLE:View.INVISIBLE);
        } else if (key.equals(getString(R.string.pref_player_name_key))){
            String playerName = sharedPreferences.getString(key, "");
            playerNameText.setText(playerName);
        }
    }

    class CustomDragListener implements View.OnDragListener {
        @Override
        public boolean onDrag(View v, DragEvent event) {
            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    break;
                case DragEvent.ACTION_DRAG_ENTERED:
                    break;
                case DragEvent.ACTION_DRAG_EXITED:
                    break;
                case DragEvent.ACTION_DROP:
                    View view = (View) event.getLocalState();
                    //stop displaying the view where it was before it was dragged
                    view.setVisibility(View.VISIBLE);
                    ImageView dropTarget = (ImageView) v;
                    String tag = (String) dropTarget.getTag();
                    View vi3 = (View) event.getLocalState();
                    String tag2 = (String) vi3.getTag();
                    if(tag.equals(tag2)){
                        dropTarget.setImageResource(pairingsWithTag.get(tag).getPlayerColor());
//                        Toast.makeText(MainActivity.this, "Yeeees", Toast.LENGTH_SHORT).show();
                        initPlayer();
                    } else {
//                        Toast.makeText(MainActivity.this, "Noooo", Toast.LENGTH_SHORT).show();
                    }

                    break;
                case DragEvent.ACTION_DRAG_ENDED:
                    //no action necessary
                    View vi2 = (View) event.getLocalState();
                    vi2.setVisibility(View.VISIBLE);
//                    Toast.makeText(MainActivity.this, "Noooo", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }

            return true;
        }
    }
    class CustomOnTouchListener implements View.OnTouchListener {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
            /*
             * Drag details: we only need default behavior
             * - clip data could be set to pass data as part of drag
             * - shadow can be tailored
             */
                ClipData data = ClipData.newPlainText("", "");
                View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);
                //start dragging the item touched
                view.startDrag(data, shadowBuilder, view, 0);
                view.setVisibility(View.INVISIBLE);
                return true;
            } else {
                return false;
            }
        }
    }
}
