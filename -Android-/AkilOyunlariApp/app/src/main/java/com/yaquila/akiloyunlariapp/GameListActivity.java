package com.yaquila.akiloyunlariapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class GameListActivity extends AppCompatActivity {

    int currentExtendedRow = 0;

    public void goToHowtoplay(View view){
        Intent intent = new Intent(getApplicationContext(), HowToPlayActivity.class);
        if(currentExtendedRow % 2 == 1){
            intent.putExtra("gameName", ((TextView)((LinearLayout)((LinearLayout)((RelativeLayout) view.getParent()).getParent()).getChildAt(0)).getChildAt(1)).getText());
        }
        else{
            intent.putExtra("gameName", ((TextView)((LinearLayout)((LinearLayout)((RelativeLayout) view.getParent()).getParent()).getChildAt(0)).getChildAt(0)).getText());
        }
        startActivity(intent);
        overridePendingTransition(R.anim.enter, R.anim.exit);
    }

    public void goToMainMenu(View view){
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.left_to_right, R.anim.right_to_left);
    }

    public void goToDifficulty(View view){
        Intent intent = new Intent(getApplicationContext(), DifficultyActivity.class);
        if(currentExtendedRow == 1){
            intent = new Intent(getApplicationContext(), SizeActivityForTwoSizedGames.class);
        }
        else if(currentExtendedRow % 2 == 1){
            intent.putExtra("gameName", ((TextView)((LinearLayout)((LinearLayout) view.getParent()).getChildAt(0)).getChildAt(1)).getText());
        }
        else{
            intent.putExtra("gameName", ((TextView)((LinearLayout)((LinearLayout) view.getParent()).getChildAt(0)).getChildAt(0)).getText());
        }
        startActivity(intent);
        overridePendingTransition(R.anim.enter, R.anim.exit);
    }

    public void extendListItem(View view){
        int rowNum = Integer.parseInt(view.getTag().toString());
        if(currentExtendedRow == 0){
            final LinearLayout ll = (LinearLayout) view.getParent();
            RelativeLayout rl = (RelativeLayout) ll.getChildAt(1);
            ll.setBackground(getResources().getDrawable(R.drawable.rounded_yellowish_bg));
            rl.setVisibility(View.VISIBLE);
            currentExtendedRow = rowNum;
//            Log.i("rowNum/childCount",rowNum+" / "+((LinearLayout)ll.getParent()).getChildCount());

            final ScrollView scrollView = findViewById(R.id.scrollView_gl);
            scrollView.post(new Runnable() {
                @Override
                public void run() {
                    scrollView.smoothScrollTo(0,ll.getBottom()-scrollView.getBottom());
                }
            });

        }
        else if(currentExtendedRow == rowNum){
            LinearLayout ll = (LinearLayout) view.getParent();
            RelativeLayout rl = (RelativeLayout) ll.getChildAt(1);
            ll.setBackground(getResources().getDrawable(R.drawable.rounded_f7f5fa_bg));
            rl.setVisibility(View.GONE);
            currentExtendedRow = 0;
        }
        else {
            final LinearLayout ll = (LinearLayout) ((LinearLayout) ((LinearLayout)view.getParent()).getParent()).getChildAt(currentExtendedRow);
            RelativeLayout rl = (RelativeLayout) ll.getChildAt(1);
            ll.setBackground(getResources().getDrawable(R.drawable.rounded_f7f5fa_bg));
            rl.setVisibility(View.GONE);
            final LinearLayout ll_2 = (LinearLayout) view.getParent();
            RelativeLayout rl_2 = (RelativeLayout) ll_2.getChildAt(1);
            ll_2.setBackground(getResources().getDrawable(R.drawable.rounded_yellowish_bg));
            rl_2.setVisibility(View.VISIBLE);
            currentExtendedRow = rowNum;
            Log.i("rowNum/childCount",rowNum+" / "+((LinearLayout)ll.getParent()).getChildCount());
            final ScrollView scrollView = findViewById(R.id.scrollView_gl);
            scrollView.post(new Runnable() {
                @Override
                public void run() {
                    scrollView.smoothScrollTo(0,ll_2.getBottom()-scrollView.getBottom());
                }
            });
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_list);

        Intent intent = getIntent();
        String message = intent.getStringExtra("message");
        if(message!=null){
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        }

    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.left_to_right, R.anim.right_to_left);
    }
}