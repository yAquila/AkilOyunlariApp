package com.yaquila.akiloyunlariapp;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.gridlayout.widget.GridLayout;

import com.yaquila.akiloyunlariapp.gameutils.HazineAviUtils;
import com.yaquila.akiloyunlariapp.gameutils.PatikaUtils;
import com.yaquila.akiloyunlariapp.gameutils.PiramitUtils;
import com.yaquila.akiloyunlariapp.gameutils.SayiBulmacaUtils;
import com.yaquila.akiloyunlariapp.gameutils.SozcukTuruUtils;
import com.yaquila.akiloyunlariapp.gameutils.SudokuUtils;
import com.yaquila.akiloyunlariapp.media.RtcTokenBuilder;
import com.yaquila.akiloyunlariapp.model.AGEventHandler;
import com.yaquila.akiloyunlariapp.model.ConstantApp;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.RtcEngine;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class GroupSolvingActivity extends BaseActivityForVoice implements AGEventHandler {

    String dbGameName;
    String gameName;
    String difficulty;
    String type;
    String user_name;
    String classid;
    String instructorName = null;
    int gridSize = 5;
    boolean gotQuestion = false;
    boolean isPermitted = false;
    boolean isConnected = false;
    boolean isParticipantsShown = false;
    boolean isCorrectDialogShown = false;

    public static List<Object> currentGrid = new ArrayList<>();
    Map<String, Class<?>> utilsMap = new HashMap<>();
    List<String> newTaskProperties = new ArrayList<>();
    Map<String,Boolean> participantMap = new HashMap<>();
    LoadingDialog loadingDialog;
    public static GridLayout gridGL;
    ConstraintLayout participantsLayout;
    AlertDialog ntDialog;
    AlertDialog participantsDialog;
    AlertDialog correctDialog;
    Spinner gameSpinner;
    Spinner diffSpinner;

    public static AppCompatActivity mAppCompatActivity;

    public void wannaLeaveDialog(View view){
        LayoutInflater factory = LayoutInflater.from(this);
        final View leaveDialogView = factory.inflate(R.layout.leave_dialog, null);
        final AlertDialog leaveDialog = new AlertDialog.Builder(this).create();
        leaveDialog.setView(leaveDialogView);

        leaveDialogView.findViewById(R.id.leaveDialogYes).setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MyClassActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.left_to_right, R.anim.right_to_left);
                leaveDialog.dismiss();
            }
        });
        leaveDialogView.findViewById(R.id.leaveDialogNo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                leaveDialog.dismiss();
            }
        });
        leaveDialog.show();
    } // Ana Menüye dönmek istiyor musun?
    public void nextQuestion(View view){
        LayoutInflater factory = LayoutInflater.from(this);
        final View leaveDialogView = factory.inflate(R.layout.correct_dialog, null);
        correctDialog = new AlertDialog.Builder(this).create();
        correctDialog.setView(leaveDialogView);
        if(type.contains("nstructor")) {
            leaveDialogView.findViewById(R.id.timeTV_correctDialog).setVisibility(View.GONE);
            leaveDialogView.findViewById(R.id.correctDialogNext).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mainFunc();
                    correctDialog.dismiss();
                }
            });
            leaveDialogView.findViewById(R.id.correctDialogGameMenu).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((ConstraintLayout) findViewById(R.id.diffTV_game).getParent()).setVisibility(View.GONE);
                    RelativeLayout gridRL = findViewById(R.id.gridGL_ga);
                    gridRL.removeAllViews();
                    selectGameDiff(null);
                    correctDialog.dismiss();
                }
            });
        } else {
            leaveDialogView.findViewById(R.id.correctDialogNext).setVisibility(View.GONE);
            leaveDialogView.findViewById(R.id.correctDialogGameMenu).setVisibility(View.GONE);
            TextView tv = leaveDialogView.findViewById(R.id.timeTV_correctDialog);
            tv.setVisibility(View.VISIBLE);
            tv.setText(R.string.WaitingInsNextQ);
        }
        try {
            correctDialog.show();
            isCorrectDialogShown = true;
        } catch(Exception e){

        }
    } // Sonraki soruya geç
    @SuppressLint("UseCompatLoadingForDrawables")
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void changeClicked(View view){
        if(isPermitted || type.contains("nstructor")) {
            TextView box = (TextView) view;
            String answerIndex = box.getTag().toString();
            try {
                utilsMap.get(gameName).getDeclaredMethod("changeClicked", View.class).invoke(null, view);
                sendGrid(currentGrid,utilsMap.get(gameName).getDeclaredField("answer").get(null), socket);
                if (!utilsMap.get(gameName).getDeclaredField("clueIndexes").get(null).toString().contains(answerIndex))
                    checkAnswer(null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    } // Tıklanan kutuya elmas/çarpı koy
    public void changeSwitch(View view){
        if(isPermitted || type.contains("nstructor")) {
            HazineAviUtils.changeSwitch(view);
        }
    } // Elmas - çarpı değiştir
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @SuppressLint("SetTextI18n")
    public void numClicked(View view) throws NoSuchFieldException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        if((isPermitted || type.contains("nstructor")) &&
                (int)utilsMap.get(gameName).getDeclaredField("clickedBox").get(null) != -1 &&
                (boolean)utilsMap.get(gameName).getDeclaredMethod("numClicked", View.class)
                        .invoke(null,view))
            checkAnswer(null);
    }

    public void deleteNum(View view) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        if(isPermitted || type.contains("nstructor"))
            utilsMap.get(gameName).getDeclaredMethod("deleteNum", null).invoke(null,null);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    public void undoOperation(View view){
        if(isPermitted || type.contains("nstructor")){
            try {
                utilsMap.get(gameName).getDeclaredMethod("undoOperation", null).invoke(null, null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    } // Son işlemi geri al
    @SuppressLint("UseCompatLoadingForDrawables")
    public void resetGrid(View view){
        if(isPermitted || type.contains("nstructor")) {
            try {
                utilsMap.get(gameName).getDeclaredMethod("resetGrid", View.class).invoke(null,view);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void notesOnGrid(View view) {
        if(isPermitted || type.contains("nstructor")) {
            SayiBulmacaUtils.notesOnGrid(view);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void checkAnswer(View view) throws NoSuchFieldException, IllegalAccessException {
        boolean checking = false;
        try {
            checking = (boolean) utilsMap.get(gameName).getDeclaredMethod("checkAnswer", null).invoke(null, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(checking){ //&& ((Object)utilsMap.get(gameName).getDeclaredField("gridSize").get(null)).size()>0){ //&& type.contains("nstructor")){
            findViewById(R.id.clickView).setVisibility(View.VISIBLE);
            TextView undoTV = findViewById(R.id.undoTV_ga);
            TextView resetTV = findViewById(R.id.resetTV_game);
            GridLayout gridLayout = findViewById(R.id.gridGL_grid);
            for (int i = 0; i < Integer.parseInt(utilsMap.get(gameName).getDeclaredField("gridSize").get(null).toString()); i++) {
                try{
                    gridLayout.findViewWithTag("answer" + i).setEnabled(false);
                } catch (Exception e) {
//                    e.printStackTrace();
                }
                try {
                    for (int j = 0; j < Integer.parseInt(utilsMap.get(gameName).getDeclaredField("gridSize").get(null).toString()); j++) {
                        gridLayout.findViewWithTag(Integer.toString(j) + i).setEnabled(false);
                    }
                } catch(Exception e){
                    e.printStackTrace();
                }
            }
            try {
                TextView deleteTV = findViewById(R.id.deleteTV_ga);
                deleteTV.setEnabled(false);
                GridLayout numsLayout = findViewById(R.id.numsGL_ga);
                for (int i = 0; i < 10; i++) {
                    numsLayout.findViewWithTag(Integer.toString(i)).setEnabled(true);
                }
            } catch (Exception e){
//                e.printStackTrace();
            }
            undoTV.setEnabled(false);
            resetTV.setEnabled(false);
            Log.i("checkingTrue - type",type);
            nextQuestion(null);
        }

    } // Çözümün doğruluğunu kontrol et
    @SuppressWarnings("deprecation")
    @SuppressLint("StaticFieldLeak")
    public class GetRequest extends AsyncTask<String, Void, String> {

        SharedPreferences sharedPreferences = getSharedPreferences("com.yaquila.akiloyunlariapp",MODE_PRIVATE);

        @Override
        protected String doInBackground(String... strings) {
            try {
                StringBuilder result = new StringBuilder();
                String id = sharedPreferences.getString("id", "non");
                URL reqURL;
                reqURL = new URL(strings[0] + "/" + id + "?" + "Info=" + (1) + "&Token=" + strings[1]);
                HttpURLConnection connection = (HttpURLConnection) reqURL.openConnection();
                connection.setRequestMethod("GET");
                connection.setDoInput(true);
                connection.setDoOutput(false);
                connection.connect();
                InputStream in;
                int status = connection.getResponseCode();
                if (status != HttpURLConnection.HTTP_OK)  {
                    in = connection.getErrorStream();
                }
                else  {
                    in = connection.getInputStream();
                }
                InputStreamReader reader = new InputStreamReader(in);
                int data = reader.read();
                while (data != -1) {

                    char current = (char) data;
                    result.append(current);
                    data = reader.read();
                }
                Log.i("result", result.toString());
                return result.toString();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            try {
                JSONObject jb = new JSONObject(result.substring(result.indexOf("{"), result.lastIndexOf("}") + 1).replace("\\",""));
                JSONArray gridArrays = (JSONArray)jb.get("Info");
                JSONArray idArray = (JSONArray)jb.get("Ids");
                Log.i("idarray", idArray +"  "+idArray.length()+"    ga:"+gridArrays.length());
                seperateGridAnswer(gridArrays.getJSONArray(0).getJSONArray(0).getJSONArray(0), false);
            } catch (Exception e) {
                e.printStackTrace();
            }
            gotQuestion = true;
            loadingDialog.dismissDialog();
            ((ConstraintLayout)findViewById(R.id.diffTV_game).getParent()).setVisibility(View.VISIBLE);
        }
    } // API'den soru çek
    @SuppressLint({"UseCompatLoadingForDrawables", "SetTextI18n"})
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void seperateGridAnswer(JSONArray grid, boolean fromStudent) throws JSONException {
        GridLayout gridLayout = gridGL;
        try{
            correctDialog.dismiss();
        } catch (Exception e){
        }
        currentGrid = new ArrayList<>();
        if (gameName.equals(getString(R.string.HazineAvı))) {
            for (int i = 0; i < HazineAviUtils.gridSize; i++) {
                List<Object> row = new ArrayList<>();
                for (int j = 0; j < HazineAviUtils.gridSize; j++) {
                    row.add(0);
                }
                ((List<Object>)currentGrid).add(row);
            }
        } else if (gameName.equals(getString(R.string.SayıBulmaca))){
            List<List<Object>> cg = new ArrayList<>();
            for (int i = 0; i < HazineAviUtils.gridSize+1; i++) {
                List<Object> row = new ArrayList<>();
                for (int j = 0; j < HazineAviUtils.gridSize; j++) {
                    row.add(0);
                }
                row.add(new ArrayList<>(Arrays.asList(0,0)));
                cg.add(row);
            }
            ((List<Object>)currentGrid).add(cg);


        }
        Log.i("firstCurrentGrid",currentGrid.toString());

        if(gameName.equals(getString(R.string.HazineAvı))) {
            if (!type.contains("nstructor")) {
                HazineAviUtils.clueIndexes = new ArrayList<>();
//            answer = new ArrayList<>();
            }
            Log.i("gridGL tag",gridGL.getTag()+"");
            for (int i = 0; i < HazineAviUtils.gridSize; i++) {
                for (int j = 0; j < HazineAviUtils.gridSize; j++) {
                    String n = ((JSONArray) grid.get(i)).get(j).toString();
                    if (Integer.parseInt(n) > 0) {
                        ((List<Integer>)currentGrid.get(i)).set(j, Integer.parseInt(n));
                        if (!fromStudent || !type.contains("nstructor"))
                            HazineAviUtils.clueIndexes.add(Integer.toString(j) + i);
                        ((TextView) gridLayout.findViewWithTag(Integer.toString(j) + i)).setText(n);
                        HazineAviUtils.gridDCs[i][j] = n;
                    } else if (n.equals("-1")) {
                        if (!type.contains("nstructor") || fromStudent) {
                            ((List<Integer>)currentGrid.get(i)).set(j, Integer.parseInt(n));
                            gridLayout.findViewWithTag(Integer.toString(j) + i).setBackground(getResources().getDrawable(R.drawable.ic_diamond));
                            HazineAviUtils.gridDCs[i][j] = "-1";
                        }
                        if (!fromStudent)
                            HazineAviUtils.answer.add(Integer.toString(j) + i);
                    } else if (n.equals("-2") && (!type.contains("nstructor") || fromStudent)) {
                        ((List<Integer>)currentGrid.get(i)).set(j, Integer.parseInt(n));
                        gridLayout.findViewWithTag(Integer.toString(j) + i).setBackground(getResources().getDrawable(R.drawable.ic_cross));
                        HazineAviUtils.gridDCs[i][j] = "-2";
                    } else if (n.equals("0") && (!type.contains("nstructor") || fromStudent)) {
                        ((List<Integer>)currentGrid.get(i)).set(j, Integer.parseInt(n));
                        gridLayout.findViewWithTag(Integer.toString(j) + i).setBackground(getResources().getDrawable(R.drawable.stroke_bg));
                        HazineAviUtils.gridDCs[i][j] = "0";
                    }
                }
            }

            Log.i("answer", HazineAviUtils.answer + "");
        } else if (gameName.equals(getString(R.string.SayıBulmaca))){
            if (!fromStudent) SayiBulmacaUtils.answer = (JSONArray) grid.get(grid.length() - 1);
            Log.i("jsonGrid",""+grid);
            Log.i("answer",SayiBulmacaUtils.answer+" FromStudent: "+fromStudent);
            for (int i = 0; i < grid.length()-1; i++){
                JSONArray row = (JSONArray) grid.get(i);
                for (int j = 0; j< row.length()-1; j++){
                    TextView tv = gridLayout.findViewWithTag(Integer.toString(j)+ i);
                    tv.setText(row.get(j).toString());
                    ((List<Object>)currentGrid.get(i)).set(j,row.get(j));
                }
                JSONArray rguide = (JSONArray)row.get(row.length()-1);
                TextView rGuideTV = gridLayout.findViewWithTag("g"+i);
                if(rguide.get(0).toString().equals("0")){
                    rGuideTV.setText(rguide.get(1).toString());
                }
                else if(rguide.get(1).toString().equals("0")){
                    rGuideTV.setText("+"+rguide.get(0).toString());
                }
                else{
                    rGuideTV.setText("+"+rguide.get(0).toString()+"  "+rguide.get(1).toString());
                }
                List<Integer> rg_list = new ArrayList<>();
                for (int a = 0; a < rguide.length(); a++){
                    rg_list.add(rguide.getInt(a));
                }
                ((List<Object>)currentGrid.get(i)).set(row.length()-1,rg_list);
            }
            TextView guideanswer = gridLayout.findViewWithTag("answerguide");
            guideanswer.setText("+"+SayiBulmacaUtils.gridSize);
            if (!fromStudent)SayiBulmacaUtils.answer.remove(SayiBulmacaUtils.answer.length()-1);
        }
        if(type.contains("nstructor") && !isConnected) {
            isConnected = true;
            connectSocket();
            joinClass();
        } else if(!type.contains("nstructor") || fromStudent){
            try {
                checkAnswer(null);
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        else if (type.contains("nstructor")){
            try {
                sendGrid(currentGrid,utilsMap.get(gameName).getDeclaredField("answer").get(null), socket);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
        }

        Log.i("clueIndexes",HazineAviUtils.clueIndexes.toString());
        Log.i("currentGrid",currentGrid.toString());
    } // Çekilen soruyu kullanıcıya göster


    @SuppressLint("InflateParams")
    public void loadingDialogFunc(){
        loadingDialog = new LoadingDialog(GroupSolvingActivity.this, getLayoutInflater().inflate(R.layout.loading_dialog,null));
        loadingDialog.startLoadingAnimation();
    }

    public String shownToDatabase(String visibleOrDatabase, String string){
        Map<String,String> visibleToDB = new HashMap<>();
        List<String> shownGameNames = new ArrayList<>(Arrays.asList(
                getString(R.string.Sudoku) + " 6x6 "+getString(R.string.Easy), getString(R.string.Sudoku) + " 6x6 "+getString(R.string.Medium), getString(R.string.Sudoku) + " 6x6 "+getString(R.string.Hard),
                getString(R.string.Sudoku) + " 9x9 "+getString(R.string.Easy), getString(R.string.Sudoku) + " 9x9 "+getString(R.string.Medium), getString(R.string.Sudoku) + " 9x9 "+getString(R.string.Hard),
                getString(R.string.HazineAvı)+" "+getString(R.string.Easy), getString(R.string.HazineAvı)+" "+getString(R.string.Medium), getString(R.string.HazineAvı)+" "+getString(R.string.Hard),
                getString(R.string.Patika)+" "+getString(R.string.Easy), getString(R.string.Patika)+" "+getString(R.string.Medium), getString(R.string.Patika)+" "+getString(R.string.Hard),
                getString(R.string.SayıBulmaca)+" "+getString(R.string.Easy), getString(R.string.SayıBulmaca)+" "+getString(R.string.Medium), getString(R.string.SayıBulmaca)+" "+getString(R.string.Hard),
                getString(R.string.SözcükTuru)+" "+getString(R.string.Easy), getString(R.string.SözcükTuru)+" "+getString(R.string.Medium), getString(R.string.SözcükTuru)+" "+getString(R.string.Hard), getString(R.string.SözcükTuru)+" "+getString(R.string.VeryHard),
                getString(R.string.Piramit)+" "+getString(R.string.Easy), getString(R.string.Piramit)+" "+getString(R.string.Medium), getString(R.string.Piramit)+" "+getString(R.string.Hard), getString(R.string.Piramit)+" "+getString(R.string.VeryHard)));
        List<String> databaseGameNames = new ArrayList<>(Arrays.asList(
                getString(R.string.Sudoku) + ".6.Easy", "Sudoku.6.Medium", "Sudoku.6.Hard", "Sudoku.9.Easy", "Sudoku.9.Medium", "Sudoku.9.Hard",
                "HazineAvi.5", "HazineAvi.8", "HazineAvi.10", "Patika.5", "Patika.7", "Patika.9",
                "SayiBulmaca.3", "SayiBulmaca.4", "SayiBulmaca.5", "SozcukTuru.Easy", "SozcukTuru.Medium", "SozcukTuru.Hard", "SozcukTuru.Hardest",
                "Piramit.3", "Piramit.4","Piramit.5","Piramit.6"));
        for(int i = 0; i<shownGameNames.size(); i++)
            visibleToDB.put(shownGameNames.get(i),databaseGameNames.get(i));

        if(visibleOrDatabase.equals("shownToDatabase")){
            return visibleToDB.get(string);
        }
        else{
            Map<String, String> dbToVisible = new HashMap<>();
            for(Map.Entry<String, String> entry : visibleToDB.entrySet()){
                dbToVisible.put(entry.getValue(), entry.getKey());
            }
            return dbToVisible.get(string);
        }
    }
    public String databaseToLayout(String dbgn){
        List<String> databaseGameNames = new ArrayList<>(Arrays.asList(
                "Sudoku.6.Easy", "Sudoku.6.Medium", "Sudoku.6.Hard", "Sudoku.9.Easy", "Sudoku.9.Medium", "Sudoku.9.Hard",
                "HazineAvi.5", "HazineAvi.8", "HazineAvi.10", "Patika.5", "Patika.7", "Patika.9",
                "SayiBulmaca.3", "SayiBulmaca.4", "SayiBulmaca.5", "SozcukTuru.Easy", "SozcukTuru.Medium", "SozcukTuru.Hard", "SozcukTuru.Hardest",
                "Piramit.3", "Piramit.4","Piramit.5","Piramit.6"));
        List<String> layoutGameNames = new ArrayList<>(Arrays.asList(
                "sudoku6_easy", "sudoku6_medium", "sudoku6_hard", "sudoku9_easy", "sudoku9_medium", "sudoku9_hard",
                "hazineavi_easy", "hazineavi_medium", "hazineavi_hard", "patika_easy", "patika_medium", "patika_hard",
                "sayibulmaca_easy", "sayibulmaca_medium", "sayibulmaca_hard", "sozcukturu_easy", "sozcukturu_medium", "sozcukturu_hard",
                "piramit_easy", "Piramit_medium","piramit_hard","piramit_veryhard"));
        Map<String,String> map = new HashMap<>();
        for(int i = 0; i<databaseGameNames.size()-1; i++)
            map.put(databaseGameNames.get(i),layoutGameNames.get(i));
        return map.get(dbgn);
    }

    public boolean checkIfGridHasDC(JSONArray grid) throws JSONException{
        boolean flag = false;
        for(int i = 0; i < HazineAviUtils.gridSize; i++) {
            if(flag) break;
            for (int j = 0; j < HazineAviUtils.gridSize; j++) {
                String n = ((JSONArray)grid.get(i)).get(j).toString();
                if(n.equals("-1") || n.equals("-2")){
                    flag = true;
                    break;
                }
            }
        }
        return flag;
    }
    @SuppressLint("UseCompatLoadingForDrawables")
    public void clearGrid(){
        try {
            utilsMap.get(gameName).getDeclaredMethod("clearGrid", null).invoke(null, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void mainFunc(){
        TextView undoTV = findViewById(R.id.undoTV_ga);
        TextView resetTV = findViewById(R.id.resetTV_game);
        undoTV.setEnabled(true);
        resetTV.setEnabled(true);
        findViewById(R.id.clickView).setVisibility(View.GONE);
        try {
            TextView deleteTV = findViewById(R.id.deleteTV_ga);
            deleteTV.setEnabled(true);
            GridLayout numsLayout = findViewById(R.id.numsGL_ga);
            for (int i = 0; i < 10; i++) {
                numsLayout.findViewWithTag(Integer.toString(i)).setEnabled(true);
            }
        } catch (Exception e){
//            e.printStackTrace();
        }
        clearGrid();
        try {
            utilsMap.get(gameName).getDeclaredMethod("initVars", AppCompatActivity.class).invoke(null,mAppCompatActivity);
        } catch (Exception e) {
            e.printStackTrace();
        }
        HazineAviUtils.answer = new ArrayList<>();
        SayiBulmacaUtils.answer = new JSONArray();
        HazineAviUtils.clueIndexes = new ArrayList<>();
        GetRequest getRequest = new GetRequest();
        getRequest.execute("https://mind-plateau-api.herokuapp.com/"+newTaskProperties.get(0),"fx!Ay:;<p6Q?C8N{");
        loadingDialogFunc();
    }

    public void selectGameDiff(View view){
        try{
            LayoutInflater factory = LayoutInflater.from(GroupSolvingActivity.this);
            final View ntLayout = factory.inflate(R.layout.groupsolving_gameselection, null);
            ntDialog = new AlertDialog.Builder(this).create();
            ntDialog.setCancelable(false);
            ntDialog.setView(ntLayout);

            ntLayout.findViewById(R.id.startbutton).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        sendGameDiff(ntLayout);
                    } catch (NoSuchFieldException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                    ntDialog.dismiss();
                }
            });
            ntLayout.findViewById(R.id.closebutton).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(), MyClassActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.left_to_right, R.anim.right_to_left);
                    ntDialog.dismiss();
                }
            });
            ntDialog.show();

            gameSpinner = ntLayout.findViewById(R.id.gameSpinner);
            diffSpinner = ntLayout.findViewById(R.id.diffSpinner);
            ArrayAdapter<String> gameAdapter = new ArrayAdapter<>(this, R.layout.spinner_tv, new ArrayList<>(Arrays.asList(getString(R.string.HazineAvı))));
            gameSpinner.setAdapter(gameAdapter);
            final ArrayAdapter<String> diffAdapter = new ArrayAdapter<>(this, R.layout.spinner_tv,
                    new ArrayList<>(Arrays.asList(getString(R.string.Easy),getString(R.string.Medium),getString(R.string.Hard))));
            diffSpinner.setAdapter(diffAdapter);
            gameSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    String currentGame = adapterView.getItemAtPosition(i).toString();
                    Log.i("currentgame",currentGame);
                    if(currentGame.equals("Piramit") || currentGame.equals("Sözcük Turu")){
                        ArrayAdapter<String> changedDiffAdapter = new ArrayAdapter<>(GroupSolvingActivity.this, R.layout.spinner_tv,
                                new ArrayList<>(Arrays.asList(getString(R.string.Easy), getString(R.string.Medium), getString(R.string.Hard), getString(R.string.VeryHard))));
                        diffSpinner.setAdapter(changedDiffAdapter);
                    } else {
                        diffSpinner.setAdapter(diffAdapter);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendGameDiff(View view) throws NoSuchFieldException, IllegalAccessException {
        newTaskProperties = new ArrayList<>();
        newTaskProperties.add(shownToDatabase("shownToDatabase",gameSpinner.getSelectedItem() +" "+ diffSpinner.getSelectedItem()));
        dbGameName = newTaskProperties.get(0);
        String[] ntp = newTaskProperties.get(0).split("\\.");
        gridSize = Integer.parseInt(ntp[1]);
        gameName = (String) gameSpinner.getSelectedItem();
        utilsMap.get(gameName).getDeclaredField("gridSize").set(utilsMap.get(gameName),gridSize);
        Log.i("gridsize",gridSize+"");
        Log.i("gridSize2 ",(int)utilsMap.get(gameName).getDeclaredField("gridSize").get(null)+"");
        difficulty = (String) diffSpinner.getSelectedItem();
        ((TextView)findViewById(R.id.diffTV_game)).setText(difficulty);

        LayoutInflater inflater = getLayoutInflater();

        List<String> gns = new ArrayList<>(Arrays.asList(getString(R.string.Sudoku), getString(R.string.HazineAvı), getString(R.string.Patika), getString(R.string.SayıBulmaca), getString(R.string.SözcükTuru), getString(R.string.Piramit)));
        List<String> lns = new ArrayList<>(Arrays.asList("sudoku", "hazineavi", "patika", "sayibulmaca", "sozcukturu", "piramit"));
        Map<String,String> glmap = new HashMap<>();
        for(int i = 0; i<gns.size(); i++)
            glmap.put(gns.get(i),lns.get(i));
        List<String> d1 = new ArrayList<>(Arrays.asList(getString(R.string.Easy),getString(R.string.Medium),getString(R.string.Hard),getString(R.string.VeryHard)));
        List<String> d2 = new ArrayList<>(Arrays.asList("easy","medium","hard","veryhard"));
        Map<String,String> diffMap = new HashMap<>();
        for(int i = 0; i<d1.size(); i++)
            diffMap.put(d1.get(i),d2.get(i));

        gridGL = (GridLayout) inflater.inflate(this.getResources().getIdentifier(glmap.get(gameName)
                +"_"+diffMap.get(difficulty)+"_grid", "layout", this.getPackageName()),null);
        Log.i("gridGL tag-sendgame",gridGL.getTag().toString());
        RelativeLayout gridRL = findViewById(R.id.gridGL_ga);
        try{
            ((ViewGroup)gridGL.getParent()).removeView(gridGL);
        } catch (Exception e){

        }
        gridRL.addView(gridGL);
        if (gameName.equals(getString(R.string.HazineAvı))){
        } else if (gameName.equals(getString(R.string.SayıBulmaca))){
            ConstraintLayout cl = (ConstraintLayout) gridRL.getParent();
            LinearLayout ll1 = (LinearLayout) cl.getChildAt(4);

            LinearLayout ll2 = (LinearLayout) inflater.inflate(this.getResources().getIdentifier("nums_and_buttons_layout","layout",this.getPackageName()),null);
            ConstraintLayout.LayoutParams params =
                    (ConstraintLayout.LayoutParams) ll1.getLayoutParams();

            ConstraintLayout.LayoutParams newParams = new ConstraintLayout.LayoutParams(
                    ConstraintLayout.LayoutParams.WRAP_CONTENT,
                    ConstraintLayout.LayoutParams.WRAP_CONTENT);

            newParams.endToEnd = params.endToEnd;
            newParams.topToBottom = params.topToBottom;
            newParams.startToStart = params.startToStart;
            newParams.bottomToBottom = params.bottomToBottom;
            newParams.verticalBias = 0.2f;
            cl.removeView(cl.getChildAt(4));
            cl.addView(ll2,4,newParams);

        }
        initUIandEvent();
        mainFunc();
    }

    @SuppressLint({"SetTextI18n", "InflateParams"})
    public void openParticipants(View view){
        LayoutInflater factory = getLayoutInflater();
        participantsLayout = (ConstraintLayout) factory.inflate(R.layout.participants_dialog, null);
        ((LinearLayout)participantsLayout.findViewById(R.id.prtpLL)).removeAllViews();
        ConstraintLayout instructorRow= (ConstraintLayout) factory.inflate(this.getResources().getIdentifier("participant_row", "layout", this.getPackageName()),null);
        ((ImageView)instructorRow.findViewById(R.id.avatarS1)).setImageResource(R.drawable.ic_teacher_usericon);
        ((TextView)instructorRow.findViewById(R.id.usernameTV)).setText(instructorName);
        ((LinearLayout)participantsLayout.findViewById(R.id.prtpLL)).addView(instructorRow);
        for(int i = 0; i < participantMap.size(); i++){
            ConstraintLayout participantRow= (ConstraintLayout) factory.inflate(this.getResources().getIdentifier("participant_row", "layout", this.getPackageName()),null);
            ((TextView)participantRow.findViewById(R.id.usernameTV)).setText((String) participantMap.keySet().toArray()[i]);
            if(type.contains("nstructor")){
                participantRow.findViewById(R.id.permissionButton).setVisibility(View.VISIBLE);
                if((Boolean) participantMap.values().toArray()[i]) {
                    ((ImageView)participantRow.findViewById(R.id.permissionButton)).setImageResource(R.drawable.ic_delete_icon);
                } else {
                    ((ImageView)participantRow.findViewById(R.id.permissionButton)).setImageResource(R.drawable.ic_key_icon);
                }
            }
            ((LinearLayout)participantsLayout.findViewById(R.id.prtpLL)).addView(participantRow);
        }

        participantsDialog = new AlertDialog.Builder(this).create();
        participantsDialog.setCancelable(false);
        participantsDialog.setView(participantsLayout);
        participantsLayout.findViewById(R.id.closebutton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                participantsDialog.dismiss();
                isParticipantsShown = false;
            }
        });
        participantsDialog.show();
        isParticipantsShown = true;
    }

    public void changeParticipantsInRT(){
        LayoutInflater factory = getLayoutInflater();
        ((LinearLayout)participantsLayout.findViewById(R.id.prtpLL)).removeAllViews();
        ConstraintLayout instructorRow= (ConstraintLayout) factory.inflate(this.getResources().getIdentifier("participant_row", "layout", this.getPackageName()),null);
        ((ImageView)instructorRow.findViewById(R.id.avatarS1)).setImageResource(R.drawable.ic_teacher_usericon);
        ((TextView)instructorRow.findViewById(R.id.usernameTV)).setText(instructorName);
        ((LinearLayout)participantsLayout.findViewById(R.id.prtpLL)).addView(instructorRow);
        for(int i = 0; i < participantMap.size(); i++){
            ConstraintLayout participantRow= (ConstraintLayout) factory.inflate(this.getResources().getIdentifier("participant_row", "layout", this.getPackageName()),null);
            ((TextView)participantRow.findViewById(R.id.usernameTV)).setText((String) participantMap.keySet().toArray()[i]);
            if(type.contains("nstructor")){
                participantRow.findViewById(R.id.permissionButton).setVisibility(View.VISIBLE);
                if((Boolean) participantMap.values().toArray()[i]) {
                    ((ImageView)participantRow.findViewById(R.id.permissionButton)).setImageResource(R.drawable.ic_delete_icon);
                } else {
                    ((ImageView)participantRow.findViewById(R.id.permissionButton)).setImageResource(R.drawable.ic_key_icon);
                }
            }
            ((LinearLayout)participantsLayout.findViewById(R.id.prtpLL)).addView(participantRow);
        }
        participantsDialog.setView(participantsLayout);
    }

    public void giveOrRemovePermission(View view){
        String username = (String) ((TextView)(((ConstraintLayout)view.getParent()).findViewById(R.id.usernameTV))).getText();
        if(!participantMap.get(username)){
            givePermission(username);
            ((ImageView)view).setImageResource(R.drawable.ic_delete_icon);
        } else {
            removePermission(username);
            ((ImageView)view).setImageResource(R.drawable.ic_key_icon);
        }
    }


//    private final static Logger log = LoggerFactory.getLogger(ChatActivity.class);

    private volatile boolean mAudioMuted = true;

    private volatile int mAudioRouting = 3; // Default

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return false;
    }

    @Override
    protected void initUIandEvent() {
        event().addEventHandler(this);

        String channelName = getSharedPreferences("com.yaquila.akiloyunlariapp", MODE_PRIVATE).getString("classid", getString(R.string.Unknown));
        vSettings().mChannelName = channelName;

        RtcTokenBuilder token = new RtcTokenBuilder();
        vSettings().mToken =
//                "006098bfc97f1b44408b6dedc2c0d6e5568IAD3nQlRYp8qhtzZ71u9UM/HBErFPMmtdUNdbyShjwIKbH2D22oAAAAAEAD89T1fsoSTYQEAAQC3hJNh";

                token.buildTokenWithUid(getString(R.string.private_app_id), getString(R.string.private_app_certificate),
                        channelName, 0, RtcTokenBuilder.Role.Role_Publisher, (int)(System.currentTimeMillis() / 1000 + 36000));



        Log.i("channelName - token", channelName+" token: "+vSettings().mToken);
        /*
          Allows a user to join a channel.

          Users in the same channel can talk to each other, and multiple users in the same channel can start a group chat. Users with different App IDs cannot call each other.

          You must call the leaveChannel method to exit the current call before joining another channel.

          A successful joinChannel method call triggers the following callbacks:

          The local client: onJoinChannelSuccess.
          The remote client: onUserJoined, if the user joining the channel is in the Communication profile, or is a BROADCASTER in the Live Broadcast profile.

          When the connection between the client and Agora's server is interrupted due to poor
          network conditions, the SDK tries reconnecting to the server. When the local client
          successfully rejoins the channel, the SDK triggers the onRejoinChannelSuccess callback
          on the local client.

         */
        worker().joinChannel(channelName, config().mUid);
        rtcEngine().setEnableSpeakerphone(true);
//        TextView textChannelName = (TextView) findViewById(R.id.channel_name);
//        textChannelName.setText(channelName);

        optional();

//        LinearLayout bottomContainer = (LinearLayout) findViewById(R.id.bottom_container);
//        FrameLayout.MarginLayoutParams fmp = (FrameLayout.MarginLayoutParams) bottomContainer.getLayoutParams();
//        fmp.bottomMargin = virtualKeyHeight() + 16;
    }

    private Handler mMainHandler;

    private static final int UPDATE_UI_MESSAGE = 0x1024;

    EditText mMessageList;

    StringBuffer mMessageCache = new StringBuffer();

    private void notifyMessageChanged(String msg) {
        if (mMessageCache.length() > 10000) { // drop messages
            mMessageCache = new StringBuffer(mMessageCache.substring(10000 - 40));
        }

        mMessageCache.append(System.currentTimeMillis()).append(": ").append(msg).append("\n"); // append timestamp for messages

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isFinishing()) {
                    return;
                }

                if (mMainHandler == null) {
                    mMainHandler = new Handler(getMainLooper()) {
                        @Override
                        public void handleMessage(Message msg) {
                            super.handleMessage(msg);

                            if (isFinishing()) {
                                return;
                            }

                            if (msg.what == UPDATE_UI_MESSAGE) {
                                String content = (String) (msg.obj);
                                Log.i("UPDATE_UI_MESSAGE", content);
//                                    mMessageList.setText(content);
//                                    mMessageList.setSelection(content.length());
                            }

                        }
                    };

//                    mMessageList = (EditText) findViewById(R.id.msg_list);
                }

                mMainHandler.removeMessages(UPDATE_UI_MESSAGE);
                Message envelop = new Message();
                envelop.what = UPDATE_UI_MESSAGE;
                envelop.obj = mMessageCache.toString();
                mMainHandler.sendMessageDelayed(envelop, 1000L);
            }
        });
    }

    private void optional() {
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
    }

    private void optionalDestroy() {
    }

    public void onSwitchSpeakerClicked(View view) {
        Log.i("onSwitchSpeakerClicked ",  view + " " + mAudioMuted + " " + mAudioRouting);

        RtcEngine rtcEngine = rtcEngine();

        /*
          Enables/Disables the audio playback route to the speakerphone.
          This method sets whether the audio is routed to the speakerphone or earpiece.
          After calling this method, the SDK returns the onAudioRouteChanged callback
          to indicate the changes.
         */
        rtcEngine.setEnableSpeakerphone(mAudioRouting != 3);
    }

    @Override
    protected void deInitUIandEvent() {
        optionalDestroy();

        doLeaveChannel();
        event().removeEventHandler(this);
    }

    /**
     * Allows a user to leave a channel.
     *
     * After joining a channel, the user must call the leaveChannel method to end the call before
     * joining another channel. This method returns 0 if the user leaves the channel and releases
     * all resources related to the call. This method call is asynchronous, and the user has not
     * exited the channel when the method call returns. Once the user leaves the channel,
     * the SDK triggers the onLeaveChannel callback.
     *
     * A successful leaveChannel method call triggers the following callbacks:
     *
     * The local client: onLeaveChannel.
     * The remote client: onUserOffline, if the user leaving the channel is in the
     * Communication channel, or is a BROADCASTER in the Live Broadcast profile.
     *
     */
    private void doLeaveChannel() {
        worker().leaveChannel(config().mChannel);
    }

    public void onEndCallClicked(View view) {
        Log.i("onEndCallClicked ", ""+view);

        quitCall();
    }

    private void quitCall() {
//        Intent intent = new Intent(this, MainActivity.class);
//        startActivity(intent);

        finish();
    }

    public void onVoiceMuteClicked(View view) {
        Log.i("onVoiceMuteClicked ", view + " audio_status: " + mAudioMuted);

        RtcEngine rtcEngine = rtcEngine();
        rtcEngine.muteLocalAudioStream(mAudioMuted = !mAudioMuted);

        ImageView iv = (ImageView) view;

        if (mAudioMuted) {
            iv.setImageResource(R.drawable.ic_microphone_closed);
        } else {
            iv.setImageResource(R.drawable.ic_microphone_open);
        }
    }

    @Override
    public void onJoinChannelSuccess(String channel, final int uid, int elapsed) {
        String msg = "onJoinChannelSuccess " + channel + " " + (uid & 0xFFFFFFFFL) + " " + elapsed;
        Log.d("onJoinChannelSuccess", msg);

        notifyMessageChanged(msg);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isFinishing()) {
                    return;
                }

                rtcEngine().muteLocalAudioStream(mAudioMuted);
            }
        });
    }

    @Override
    public void onUserOffline(int uid, int reason) {
        String msg = "onUserOffline " + (uid & 0xFFFFFFFFL) + " " + reason;
        Log.d("onUserOffline",msg);

        notifyMessageChanged(msg);

    }

    @Override
    public void onExtraCallback(final int type, final Object... data) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isFinishing()) {
                    return;
                }

                doHandleExtraCallback(type, data);
            }
        });
    }

    private void doHandleExtraCallback(int type, Object... data) {
        int peerUid;
        boolean muted;

        switch (type) {
            case AGEventHandler.EVENT_TYPE_ON_USER_AUDIO_MUTED: {
                peerUid = (Integer) data[0];
                muted = (boolean) data[1];

                notifyMessageChanged("mute: " + (peerUid & 0xFFFFFFFFL) + " " + muted);
                break;
            }

            case AGEventHandler.EVENT_TYPE_ON_AUDIO_QUALITY: {
                peerUid = (Integer) data[0];
                int quality = (int) data[1];
                short delay = (short) data[2];
                short lost = (short) data[3];

                notifyMessageChanged("quality: " + (peerUid & 0xFFFFFFFFL) + " " + quality + " " + delay + " " + lost);
                break;
            }

            case AGEventHandler.EVENT_TYPE_ON_SPEAKER_STATS: {
                IRtcEngineEventHandler.AudioVolumeInfo[] infos = (IRtcEngineEventHandler.AudioVolumeInfo[]) data[0];

                if (infos.length == 1 && infos[0].uid == 0) { // local guy, ignore it
                    break;
                }

                StringBuilder volumeCache = new StringBuilder();
                for (IRtcEngineEventHandler.AudioVolumeInfo each : infos) {
                    peerUid = each.uid;
                    int peerVolume = each.volume;

                    if (peerUid == 0) {
                        continue;
                    }

                    volumeCache.append("volume: ").append(peerUid & 0xFFFFFFFFL).append(" ").append(peerVolume).append("\n");
                }

                if (volumeCache.length() > 0) {
                    String volumeMsg = volumeCache.substring(0, volumeCache.length() - 1);
                    notifyMessageChanged(volumeMsg);

                    if ((System.currentTimeMillis() / 1000) % 10 == 0) {
                        Log.d("volumeMsg",volumeMsg);
                    }
                }
                break;
            }

            case AGEventHandler.EVENT_TYPE_ON_APP_ERROR: {
                int subType = (int) data[0];

                if (subType == ConstantApp.AppError.NO_NETWORK_CONNECTION) {
                    showLongToast(getString(R.string.msg_no_network_connection));
                }

                break;
            }

            case AGEventHandler.EVENT_TYPE_ON_AGORA_MEDIA_ERROR: {
                int error = (int) data[0];
                String description = (String) data[1];

                notifyMessageChanged(error + " " + description);

                break;
            }

            case AGEventHandler.EVENT_TYPE_ON_AUDIO_ROUTE_CHANGED: {
                notifyHeadsetPlugged((int) data[0]);

                break;
            }
        }
    }

    public void notifyHeadsetPlugged(final int routing) {
        Log.i("notifyHeadsetPlugged ", ""+routing);

        mAudioRouting = routing;

//        ImageView iv = (ImageView) findViewById(R.id.switch_speaker_id);
//        if (mAudioRouting == 3) { // Speakerphone
//            iv.setColorFilter(getResources().getColor(R.color.agora_blue), PorterDuff.Mode.MULTIPLY);
//        } else {
//            iv.clearColorFilter();
//        }
    }

    public static Socket socket;
    static {
        try {
            socket = IO.socket("https://plato-all-in-one.herokuapp.com");
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public void connectSocket(){
        socket.connect();
        Log.i("Connected","Connected");
        socket.on("digiEdu_joinToRoom", new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String msg =(String) args[0];
                        Log.i("joinToRoom",msg+ ".");
                        if(msg.equals("failed")){
                            disconnectSocket(null);
                            Toast.makeText(GroupSolvingActivity.this, R.string.Instructor_Hasnt_started, Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(getApplicationContext(), MyClassActivity.class);
                            startActivity(intent);
                            overridePendingTransition(R.anim.left_to_right, R.anim.right_to_left);
                        }
                    }
                });
            }
        });
        socket.on("gameType", new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(!type.contains("nstructor")) {
                            dbGameName = (String) args[0];
                            String[] ntp = dbGameName.split("\\.");
                            List<String> dbns = new ArrayList<>(Arrays.asList("Sudoku","HazineAvi","Patika","SayiBulmaca","SozcukTuru","Piramit"));
                            List<String> gns = new ArrayList<>(Arrays.asList(getString(R.string.Sudoku), getString(R.string.HazineAvı), getString(R.string.Patika), getString(R.string.SayıBulmaca), getString(R.string.SözcükTuru), getString(R.string.Piramit)));
                            Map<String,String> nameMap = new HashMap<>();
                            for(int i = 0; i<gns.size(); i++)
                                nameMap.put(dbns.get(i),gns.get(i));

                            gameName = nameMap.get(ntp[0]);
                            Log.i("gameName",gameName);



                            gridSize = Integer.parseInt(ntp[1]);
                            try {
                                utilsMap.get(gameName).getDeclaredField("gridSize").set(utilsMap.get(gameName),gridSize);
                                Log.i("gridsize",gridSize+"");
                                Log.i("gridSize2 ",(int)utilsMap.get(gameName).getDeclaredField("gridSize").get(null)+"");
                            } catch (IllegalAccessException | NoSuchFieldException e) {
                                e.printStackTrace();
                            }

//                            currentGrid = new ArrayList<>();
//                            if (gameName.equals(getString(R.string.HazineAvı))) {
//                                for (int i = 0; i < gridSize; i++) {
//                                    List<Object> row = new ArrayList<>();
//                                    for (int j = 0; j < gridSize; j++) {
//                                        row.add(0);
//                                    }
//                                    ((List<Object>)currentGrid).add(row);
//                                }
//                            } else if (gameName.equals(getString(R.string.SayıBulmaca))){
//                                for (int i = 0; i < gridSize+1; i++) {
//                                    List<Object> row = new ArrayList<>();
//                                    for (int j = 0; j < gridSize; j++) {
//                                        row.add(0);
//                                    }
//                                    row.add(new ArrayList<>(Arrays.asList(0,0)));
//                                    ((List<Object>)currentGrid).add(row);
//                                }
//                            }

                            Log.i("layoutname",databaseToLayout(dbGameName));
                            gridGL = (GridLayout) getLayoutInflater().inflate(getApplicationContext().getResources().getIdentifier(databaseToLayout(dbGameName)
                                    + "_grid", "layout", getApplicationContext().getPackageName()),null);
                            ((TextView)findViewById(R.id.diffTV_game)).setText(databaseToLayout(dbGameName).split("_")[1]);
                            String df = databaseToLayout(dbGameName).split("_")[1];
                            if(df.equals("easy")) ((TextView)findViewById(R.id.diffTV_game)).setText(getString(R.string.Easy));
                            else if(df.equals("medium")) ((TextView)findViewById(R.id.diffTV_game)).setText(getString(R.string.Medium));
                            else if(df.equals("hard")) ((TextView)findViewById(R.id.diffTV_game)).setText(getString(R.string.Hard));

//                            gridGL = (GridLayout) getLayoutInflater().inflate(getResources().getIdentifier(gameName.toLowerCase().replace(" ","").
//                                    replace("ı","i").replace("ö","o").replace("ü","u")
//                                    +gridSize+"_grid", "layout", getPackageName()),null);
                        }
                        Log.i("gameType", args[0] + ".");
                    }
                });
            }
        });
        socket.on("sendGrid", new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                runOnUiThread(new Runnable() {
                    @SuppressLint("DefaultLocale")
                    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                    @Override
                    public void run() {
//                        if(!type.contains("nstructor")) {
                        ((ConstraintLayout) findViewById(R.id.diffTV_game).getParent()).setVisibility(View.VISIBLE);
                        if(isCorrectDialogShown && !type.contains("nstructor")){
                            correctDialog.dismiss();
                            TextView undoTV = findViewById(R.id.undoTV_ga);
                            TextView resetTV = findViewById(R.id.resetTV_game);
                            undoTV.setEnabled(true);
                            resetTV.setEnabled(true);
                            findViewById(R.id.clickView).setVisibility(View.GONE);
                            try {
                                TextView deleteTV = findViewById(R.id.deleteTV_ga);
                                deleteTV.setEnabled(true);
                                GridLayout numsLayout = findViewById(R.id.numsGL_ga);
                                for (int i = 0; i < 10; i++) {
                                    numsLayout.findViewWithTag(Integer.toString(i)).setEnabled(true);
                                }
                            } catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                        try {
                            JSONArray grid;
                            String result = (String) args[0];
                            JSONArray resultJB = new JSONArray(result);
                            if(resultJB.length()==2){
                                grid = resultJB.getJSONArray(0);
                                if(!type.contains("nstructor")) {
                                    try {
                                        utilsMap.get(gameName).getDeclaredMethod("initVars", AppCompatActivity.class).invoke(null,mAppCompatActivity);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    JSONArray answerJA = resultJB.getJSONArray(1);
                                    if(gameName.equals(getString(R.string.HazineAvı))) {
                                        HazineAviUtils.answer = new ArrayList<>();
                                        for (int i = 0; i < answerJA.length(); i++) {
                                            HazineAviUtils.answer.add(String.format("%02d", (int) answerJA.get(i)));
                                        }
                                        Log.i("answerInSendGrid", HazineAviUtils.answer.toString());
                                    } else if (gameName.equals(getString(R.string.SayıBulmaca))){
                                        SayiBulmacaUtils.answer = answerJA;
                                    }
                                }
                            } else {
                                grid = resultJB;
                            }
                            if(gameName.equals(getString(R.string.HazineAvı)) && !type.contains("nstructor"))
                                if (!checkIfGridHasDC(grid)){
                                    clearGrid();
                                    Log.i("grid","cleared");
                                }
                            Log.i("grid",grid.toString());
                            seperateGridAnswer(grid, true);
                            RelativeLayout gridRL = findViewById(R.id.gridGL_ga);
                            if(!Objects.equals(gridRL.getChildAt(0),gridGL)){
                                gridRL.removeAllViews();
                                try{
                                    gridRL.addView(gridGL);
                                } catch(IllegalStateException e){
                                    e.printStackTrace();
                                }
                            }
                            if (gameName.equals(getString(R.string.HazineAvı))){
                            } else if (gameName.equals(getString(R.string.SayıBulmaca))){
                                ConstraintLayout cl = (ConstraintLayout) gridRL.getParent();
                                LinearLayout ll1 = (LinearLayout) cl.getChildAt(4);

                                LinearLayout ll2 = (LinearLayout) getLayoutInflater().inflate(getApplicationContext().getResources().getIdentifier("nums_and_buttons_layout","layout",getApplicationContext().getPackageName()),null);
                                ConstraintLayout.LayoutParams params =
                                        (ConstraintLayout.LayoutParams) ll1.getLayoutParams();

                                ConstraintLayout.LayoutParams newParams = new ConstraintLayout.LayoutParams(
                                        ConstraintLayout.LayoutParams.WRAP_CONTENT,
                                        ConstraintLayout.LayoutParams.WRAP_CONTENT);

                                newParams.endToEnd = params.endToEnd;
                                newParams.topToBottom = params.topToBottom;
                                newParams.startToStart = params.startToStart;
                                newParams.bottomToBottom = params.bottomToBottom;
                                newParams.verticalBias = 0.2f;
                                cl.removeView(cl.getChildAt(4));
                                cl.addView(ll2,4,newParams);

                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
//                        }

                        Log.i("sendGrid", args[0] + ".");
                    }
                });
            }
        });
        socket.on("participants", new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            JSONObject room = (JSONObject) args[0];
                            JSONObject prtps = room.getJSONObject("participants");
                            if(instructorName == null){
                                instructorName = prtps.getJSONObject("instructor").getString("username");
                                if(!type.contains("nstructor")){
                                    Log.i("instructor","degil");
                                    initUIandEvent();
                                }
                            }
                            JSONArray students = prtps.getJSONArray("students");
                            List<String> stNameList = new ArrayList<>();
                            for(int i = 0; i<students.length(); i++){
                                String stName = students.getJSONObject(i).getString("username");
                                stNameList.add(stName);
                                if(participantMap.get(stName)==null){
                                    participantMap.put(stName,false);
                                }
                            }
                            for(String s : participantMap.keySet()){
                                if(!stNameList.contains(s)){
                                    try {
                                        participantMap.remove(s);
                                    } catch(Exception e){
                                        e.printStackTrace();
                                    }
                                }
                            }
                            if(isParticipantsShown){
                                changeParticipantsInRT();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        Log.i("participantMap",participantMap.toString());
                        Log.i("participants",args[0]+ ".");
                    }
                });
            }
        });
        socket.on("permissionGranted", new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        isPermitted = true;
//                        operations = new ArrayList<>();
//                        operations.add(new ArrayList<>(Arrays.asList("00", "0")));
                        Toast.makeText(GroupSolvingActivity.this, getString(R.string.permissionGranted), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        socket.on("permissionRemoved", new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        isPermitted = false;
//                        operations = new ArrayList<>();
//                        operations.add(new ArrayList<>(Arrays.asList("00", "0")));
                        Toast.makeText(GroupSolvingActivity.this, getString(R.string.permissionRemoved), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });


//        socket.on("voiceChat", new Emitter.Listener() {
//            @Override
//            public void call(final Object... args) {
//                runOnUiThread(new Runnable() {
//                    @RequiresApi(api = Build.VERSION_CODES.M)
//                    @Override
//                    public void run() {
////                        byte[] byteArray = (byte[])args[0];
////
////                        playMp3(byteArray);
////                        Log.i("socket- voiceChat", new String(byteArray)+".");
//
//                    }
//                });
//            }
//        });
    }

    public void disconnectSocket(View view){
        socket.disconnect();
    }

    public void joinClass(){
        Map<String, String> map = new HashMap<>();
        map.put("user_name", user_name);
        map.put("role", type);
        map.put("room_id", classid);
        if(type.contains("nstructor")) {
            map.put("grid", currentGrid.toString());
            map.put("gameType", dbGameName);
        }
        socket.emit("usageType","digiEdu");
        socket.emit("digiEdu_joinToRoom", new JSONObject(map));
        Log.i("socket","digiEdu_joinToRoom");
    }

    public static void sendGrid(List<?> grid, Object answer, Socket socket){

//        Map<String, Object> map = new HashMap<>();
//        map.put("grid",grid);
//        map.put("answer",answer);
        List<Object> lst = null;
        lst = new ArrayList<>(Arrays.asList(grid, answer));
        socket.emit("sendGrid", lst);
    }

    public void givePermission(String username){
        socket.emit("givePermission", username);
        participantMap.put(username,true);

    }

    public void removePermission(String username){
        socket.emit("removePermission", username);
        participantMap.put(username,false);
    }


    //    private boolean playPause;
//    private MediaPlayer mediaPlayer;
//    private boolean initialStage = true;
//
//    Button btn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_solving);

        mAppCompatActivity = this;
        utilsMap = new HashMap<>();
        utilsMap.put(getString(R.string.Sudoku), SudokuUtils.class);
        utilsMap.put(getString(R.string.HazineAvı), HazineAviUtils.class);
        utilsMap.put(getString(R.string.Patika), PatikaUtils.class);
        utilsMap.put(getString(R.string.SayıBulmaca), SayiBulmacaUtils.class);
        utilsMap.put(getString(R.string.SözcükTuru), SozcukTuruUtils.class);
        utilsMap.put(getString(R.string.Piramit), PiramitUtils.class);

        SharedPreferences sp = getSharedPreferences("com.yaquila.akiloyunlariapp", MODE_PRIVATE);
        type = sp.getString("type", getString(R.string.Unknown));
        user_name = sp.getString("username", getString(R.string.Unknown));
        classid = sp.getString("classid", getString(R.string.Unknown));
        if (type.contains("nstructor")) {
            selectGameDiff(null);
        } else {
            connectSocket();
            joinClass();
        }
    }

    @Override
    protected void onDestroy() {
        disconnectSocket(null);
        try {
            onEndCallClicked(null);
        }catch(Exception e){
            e.printStackTrace();
        }
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        disconnectSocket(null);
        try {
            onEndCallClicked(null);
        }catch(Exception e){
            e.printStackTrace();
        }
//        if(currentScreen.equals("selection")) {
//            ntDialog.dismiss();
//            Intent intent = new Intent(getApplicationContext(), MyClassActivity.class);
//            startActivity(intent);
//            overridePendingTransition(R.anim.left_to_right, R.anim.right_to_left);
//        }
//        else if(currentScreen.equals("game")){
//            wannaLeaveDialog(null);
//        }
        Intent intent = new Intent(getApplicationContext(), MyClassActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.left_to_right, R.anim.right_to_left);
    }
}