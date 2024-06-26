package com.yaquila.akiloyunlariapp.gameutils;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.Build;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.gridlayout.widget.GridLayout;

import com.yaquila.akiloyunlariapp.WordTourView;
import com.yaquila.akiloyunlariapp.R;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class SozcukTuruUtils {

    public static AppCompatActivity context;

    public static String previousCoor;
    public static String answer = "";
    public static String[] rowColumn;
    public static String[][] lineGrid = new String[81][81];
    public static int gridSizeX = 5;
    public static int gridSizeY = 5;
    public static int pxHeightX = 900;
    public static int pxHeightY = 900;

    public static List<String> operations = new ArrayList<>();
    public static List<String> opsForUndo = new ArrayList<>();
    public static Bitmap bitmap;
    public static Canvas canvas;
    public static Paint paint;

    public static void initVars(AppCompatActivity ctx){
        context = ctx;
        previousCoor = null;
        answer = "";
        rowColumn = null;
        lineGrid = new String[81][81];
        gridSizeX = 5;
        gridSizeY = 5;
        pxHeightX = 900;
        pxHeightY = 900;

        operations = new ArrayList<>();
        opsForUndo = new ArrayList<>();
        bitmap = null;
        canvas = null;
        paint = null;
    }

    public static String[] xyToRowColumn(final float x, final float y){
        String[] rowColumn = new String[2];
        float coefX = (float)pxHeightX/gridSizeX;
        float coefY = (float)pxHeightY/gridSizeY;
        if((((x / coefX) - Math.floor(x / coefX)) >= 0.2f) && (((y / coefY) - Math.floor(y / coefY)) >= 0.2f)){
            rowColumn[0] = Integer.toString((int) Math.floor(x/coefX));
            rowColumn[1] = Integer.toString((int) Math.floor(y/coefY));
        }
        else{
            rowColumn[0] = "";
            rowColumn[1] = "";
        }
        return rowColumn;
    }

    public static int[] middlePoint(final String coor){
        int x = Integer.parseInt(String.valueOf(coor.charAt(0)));
        int y = Integer.parseInt(String.valueOf(coor.charAt(1)));
        int[] middle_point = new int[2];
        float coefX = (float)pxHeightX/gridSizeX;
        float coefY = (float)pxHeightY/gridSizeY;
        middle_point[0] = (int) (coefX*x+coefX/2);
        middle_point[1] = (int) (coefY*y+coefY/2);
        return middle_point;
    }

    @SuppressLint("CutPasteId")
    public static void drawALine(final float startX, final float startY, final float stopX, final float stopY, final boolean erasing){
//        ImageView imageView = context.findViewById(R.id.canvasIV);
//        Log.i("x1,y1,x2,y2",startX+"  "+startY+"  "+stopX+"  "+stopY);
        canvas = ((WordTourView)context.findViewById(R.id.drawing)).getDrawCanvas();
        paint = ((WordTourView)context.findViewById(R.id.drawing)).getDrawPaint();
        bitmap = ((WordTourView)context.findViewById(R.id.drawing)).getCanvasBitmap();
        if(!erasing) {
            int offset = pxHeightY / 150;
            if (startY - stopY == 0 && startX - stopX != 0) {
                if (startX < stopX)
                    canvas.drawLine(startX - offset, startY, stopX + offset, stopY, paint);
                else canvas.drawLine(startX + offset, startY, stopX - offset, stopY, paint);
            } else if (startX - stopX == 0 && startY - stopY != 0){
                if (startY < stopY)
                    canvas.drawLine(startX, startY - offset, stopX, stopY + offset, paint);
                else canvas.drawLine(startX, startY + offset, stopX, stopY - offset, paint);
            }
            else {
                canvas.drawLine(startX,startY,stopX,stopY,paint);
//                if (startX < stopX) {
//                    if (startY < stopY)
//                        canvas.drawLine(startX-offset, startY - offset, stopX+offset, stopY + offset, paint);
//                    else canvas.drawLine(startX-offset, startY + offset, stopX+offset, stopY - offset, paint);
//                } else {
//                    if (startY < stopY)
//                        canvas.drawLine(startX+offset, startY - offset, stopX-offset, stopY + offset, paint);
//                    else canvas.drawLine(startX+offset, startY + offset, stopX-offset, stopY - offset, paint);
//                }
            }
        }
        else{
            String[] previousC = xyToRowColumn(startX,startY);
            int px = Integer.parseInt(previousC[0]);
            int py = Integer.parseInt(previousC[1]);
            String[] currentC = xyToRowColumn(stopX,stopY);
            int cx = Integer.parseInt(currentC[0]);
            int cy = Integer.parseInt(currentC[1]);

            if (startY - stopY == 0 && startX - stopX != 0) {
                int offset1 = pxHeightY / 120;
                int offset2 = pxHeightY / 120;
                if(lineGrid[px][py].length() == 2){
                    offset1 = -pxHeightY/120;
                }
                if(lineGrid[cx][cy].length() == 2){
                    offset2 = -pxHeightY/120;
                }
                if (startX < stopX)
                    canvas.drawLine(startX - offset1, startY, stopX + offset2, stopY, paint);
                else canvas.drawLine(startX + offset1, startY, stopX - offset2, stopY, paint);
            } else if (startX - stopX == 0 && startY - stopY != 0){
                int offset1 = pxHeightY / 120;
                int offset2 = pxHeightY / 120;
                if(lineGrid[px][py].length() == 2){
                    offset1 = -pxHeightY/120;
                }
                if(lineGrid[cx][cy].length() == 2){
                    offset2 = -pxHeightY/120;
                }
                if (startY < stopY)
                    canvas.drawLine(startX, startY - offset1, stopX, stopY + offset2, paint);
                else canvas.drawLine(startX, startY + offset1, stopX, stopY - offset2, paint);
            } else {
                int offset1 = pxHeightY / 120;
                int offset2 = pxHeightY / 120;
                if(lineGrid[px][py].length() == 2){
                    offset1 = -pxHeightY/120;
                }
                if(lineGrid[cx][cy].length() == 2){
                    offset2 = -pxHeightY/120;
                }

                if (startX < stopX) {
                    if (startY < stopY)
                        canvas.drawLine(startX-offset1, startY - offset1, stopX+offset2, stopY + offset2, paint);
                    else canvas.drawLine(startX-offset1, startY + offset1, stopX+offset2, stopY - offset2, paint);
                } else {
                    if (startY < stopY)
                        canvas.drawLine(startX+offset1, startY - offset1, stopX-offset2, stopY + offset2, paint);
                    else canvas.drawLine(startX + offset1, startY + offset1, stopX-offset2, stopY - offset2, paint);
                }
            }
        }
        ((WordTourView)context.findViewById(R.id.drawing)).setDrawCanvas(canvas);
        ((WordTourView)context.findViewById(R.id.drawing)).setCanvasBitmap(bitmap);
//        imageView.setImageBitmap(bitmap);
    }

    public static void addLine(final String firstRC, final String secondRC){
        int r1 = Integer.parseInt(String.valueOf(firstRC.charAt(0)));
        int c1 = Integer.parseInt(String.valueOf(firstRC.charAt(1)));
        int r2 = Integer.parseInt(String.valueOf(secondRC.charAt(0)));
        int c2 = Integer.parseInt(String.valueOf(secondRC.charAt(1)));
        if(r1 == r2 && c1 != c2){ // vertical line
            if(c1 < c2){ // first one is above second
                lineGrid[r1][c1] += "d";
                lineGrid[r2][c2] += "u";
            }
            else { // first one is below second
                lineGrid[r1][c1] += "u";
                lineGrid[r2][c2] += "d";
            }
        }
        else if(c1 == c2 && r1 != r2){ // horizontal line
            if(r1 < r2){ // first one is left of second
                lineGrid[r1][c1] += "r";
                lineGrid[r2][c2] += "l";
            }
            else { // first one is right of second
                lineGrid[r1][c1] += "l";
                lineGrid[r2][c2] += "r";
            }
        }
        else{
            if(c1 < c2){ // first one is above second
                if(r1 < r2){ // first one is left of second
                    lineGrid[r1][c1] += "s";
                    lineGrid[r2][c2] += "n";
                }
                else { // first one is right of second
                    lineGrid[r1][c1] += "n";
                    lineGrid[r2][c2] += "s";
                }
            }
            else { // first one is below second
                if(r1 < r2){ // first one is left of second
                    lineGrid[r1][c1] += "e";
                    lineGrid[r2][c2] += "w";
                }
                else { // first one is right of second
                    lineGrid[r1][c1] += "w";
                    lineGrid[r2][c2] += "e";
                }
            }
        }
    }

    public static void removeLine(final String firstRC, final String secondRC){
        int r1 = Integer.parseInt(String.valueOf(firstRC.charAt(0)));
        int c1 = Integer.parseInt(String.valueOf(firstRC.charAt(1)));
        int r2 = Integer.parseInt(String.valueOf(secondRC.charAt(0)));
        int c2 = Integer.parseInt(String.valueOf(secondRC.charAt(1)));
        if(r1 == r2 && c1 != c2){ // vertical line
            if(c1 < c2){ // first one is above second
                lineGrid[r1][c1] = lineGrid[r1][c1].replace("d","");
                lineGrid[r2][c2] = lineGrid[r2][c2].replace("u","");
            }
            else { // first one is below second
                lineGrid[r1][c1] = lineGrid[r1][c1].replace("u","");
                lineGrid[r2][c2] = lineGrid[r2][c2].replace("d","");
            }
        }
        else if(c1 == c2 && r1 != r2){ // horizontal line
            if(r1 < r2){ // first one is left of second
                lineGrid[r1][c1] = lineGrid[r1][c1].replace("r","");
                lineGrid[r2][c2] = lineGrid[r2][c2].replace("l","");
            }
            else { // first one is right of second
                lineGrid[r1][c1] = lineGrid[r1][c1].replace("l","");
                lineGrid[r2][c2] = lineGrid[r2][c2].replace("r","");

            }
        }
        else{
            if(c1 < c2){ // first one is above second
                if(r1 < r2){ // first one is left of second
                    lineGrid[r1][c1] = lineGrid[r1][c1].replace("s","");
                    lineGrid[r2][c2] = lineGrid[r2][c2].replace("n","");
                }
                else { // first one is right of second
                    lineGrid[r1][c1] = lineGrid[r1][c1].replace("n","");
                    lineGrid[r2][c2] = lineGrid[r2][c2].replace("s","");
                }
            }
            else { // first one is below second
                if(r1 < r2){ // first one is left of second
                    lineGrid[r1][c1] = lineGrid[r1][c1].replace("e","");
                    lineGrid[r2][c2] = lineGrid[r2][c2].replace("w","");
                }
                else { // first one is right of second
                    lineGrid[r1][c1] = lineGrid[r1][c1].replace("w","");
                    lineGrid[r2][c2] = lineGrid[r2][c2].replace("e","");
                }
            }
        }
//        Log.i("eraseModeRemoveLine",r1+" "+c1+" "+lineGrid[r1][c1]+" / "+r2+" "+c2+" "+lineGrid[r2][c2]);
    }

    public static boolean isMoreLineCanBeAdded(final String coor){
        int r = Integer.parseInt(String.valueOf(coor.charAt(0)));
        int c = Integer.parseInt(String.valueOf(coor.charAt(1)));
        Log.i("rc",r+"  "+c);
        return lineGrid[r][c].length() < 2;
    }

    public static boolean lineCanBeDrawn(final String currentC, final String previousC){
        return (!currentC.equals(previousC)
                && !currentC.equals("")
                && !previousC.equals("")
                && (
                (Math.abs(Integer.parseInt(String.valueOf(previousC.charAt(1))) - Integer.parseInt(String.valueOf(currentC.charAt(1)))) <= 1)

                        && (Math.abs(Integer.parseInt(String.valueOf(previousC.charAt(0))) - Integer.parseInt(String.valueOf(currentC.charAt(0)))) <= 1)
        )
                && isMoreLineCanBeAdded(previousC) && isMoreLineCanBeAdded(currentC));
    }

    public static boolean isGridFull(){
        boolean isfull = true;
        int notfullCount = 0;
        for(int i = 0; i < gridSizeX; i++){
            if(!isfull) break;
            for(int j = 0; j < gridSizeY; j++){
                if(lineGrid[i][j].length() < 2){
                    if(lineGrid[i][j].length() <= 1){
                        notfullCount += 1;
                    }
                    if(notfullCount > 2){
                        isfull=false;
                        break;
                    }
                }
            }
        }
        return isfull;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static void undoOperation(){
        if(opsForUndo.size() > 2){
            String op = opsForUndo.get(opsForUndo.size()-1);
            String previousC = op.substring(0,2);
            String currentC = op.substring(2,4);
            int[] firstMP = middlePoint(previousC);
            int[] secondMP = middlePoint(currentC);
            if(op.charAt(4) == '+'){
                paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
                paint.setStrokeWidth((float)pxHeightY/60);
                ((WordTourView)context.findViewById(R.id.drawing)).setDrawPaint(paint);
                drawALine(firstMP[0],firstMP[1],secondMP[0],secondMP[1], true);
                removeLine(previousC,currentC);
                for(int i = operations.size()-1; i >= 0; i--){
                    if(operations.get(i).equals(previousC + currentC)){
                        operations.remove(i);
                        break;
                    }
                }
                removeLine(currentC,previousC);
                for(int i = operations.size()-1; i >= 0; i--){
                    if(operations.get(i).equals(currentC + previousC)){
                        operations.remove(i);
                        break;
                    }
                }
                paint.setXfermode(null);
                paint.setStrokeWidth((float)pxHeightY/75);
                ((WordTourView)context.findViewById(R.id.drawing)).setDrawPaint(paint);

            } else {
                drawALine(firstMP[0],firstMP[1],secondMP[0],secondMP[1], false);
                addLine(previousC, currentC);
                operations.add(previousC+currentC);
            }
            for(int i = opsForUndo.size()-1; i >= 0; i--){
                if(opsForUndo.get(i).equals(op)){
                    opsForUndo.remove(i);
                    break;
                }
            }
            Log.i("opsforUndo",opsForUndo.toString());
        }
    }

    public static void resetGrid(final View view){
        try {
            final TextView resetTV = (TextView) view;
            resetTV.setTextColor(context.getResources().getColor(R.color.light_red));
            resetTV.setTextSize(TypedValue.COMPLEX_UNIT_SP, 19);
            resetTV.setText(R.string.ResetNormal);
            resetTV.postDelayed(new Runnable() {
                @Override
                public void run() {
                    resetTV.setTextColor(context.getResources().getColorStateList(R.color.reset_selector_tvcolor));
                    resetTV.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
                    resetTV.setText(R.string.ResetUnderlined);
                }
            }, 100);

            operations = new ArrayList<>();
            operations.add("--");
            operations.add("--");
            opsForUndo = new ArrayList<>();
            opsForUndo.add("--");
            opsForUndo.add("--");

            bitmap.eraseColor(Color.TRANSPARENT);
            canvas = new Canvas(bitmap);
            ((WordTourView)context.findViewById(R.id.drawing)).setDrawCanvas(canvas);
            ((WordTourView)context.findViewById(R.id.drawing)).setCanvasBitmap(bitmap);

            for (int i = 0; i < gridSizeX; i++){
                for(int j = 0; j < gridSizeY; j++){
                    if(lineGrid[i][j].length() <=2){
                        lineGrid[i][j] = "";
                    }
                }
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static boolean checkAnswer(){
        boolean checking=true;
        for(String s : operations.subList(2,operations.size())){
            String sR = s.substring(2) + s.substring(0,2);
            checking = answer.contains(s) || answer.contains(sR);
            Log.i("answer / s / sR",s + " / " + sR + " / " + answer);
            if(!checking) break;
        }
//        Log.i("answerComparison",opAnswer.toString()+" // "+answer);
        return checking;
    }

    public static void seperateGridAnswer(final JSONArray grid) throws JSONException {
        GridLayout gridLayout = context.findViewById(R.id.gridGL_ga);
        StringBuilder answerSB = new StringBuilder();
        answer = answer + ((JSONArray) grid.get(0)).getInt(0)+((JSONArray) grid.get(0)).getInt(1);
        StringBuilder words = new StringBuilder();
        for (int i = 0; i < grid.length(); i++){
            JSONArray box = ((JSONArray) grid.get(i));
            if(i != 0 && i != grid.length()-1)
                answerSB.append(box.getInt(0)).append(box.getInt(1)).append(" ").append(box.getInt(0)).append(box.getInt(1));
//                answer = answer + box.getInt(0)+box.getInt(1) + " " + box.getInt(0)+box.getInt(1);
            TextView currentTV = gridLayout.findViewWithTag(Integer.toString(box.getInt(0))+box.getInt(1));
            currentTV.setText(Character.toString((char)box.getInt(2)));
            words.append((char) box.getInt(2));
        }
        answer += answerSB.toString();
        answer = answer + ((JSONArray) grid.get(grid.length()-1)).getInt(0)+((JSONArray) grid.get(grid.length()-1)).getInt(1);

        Log.i("answerWords", words.toString());
    }

    public static void clearGrid(){
        operations = new ArrayList<>();
        operations.add("--");
        operations.add("--");
        opsForUndo = new ArrayList<>();
        opsForUndo.add("--");
        opsForUndo.add("--");

        GridLayout gridLayout = context.findViewById(R.id.gridGL_ga);
        for (int i = 0; i < SozcukTuruUtils.gridSizeY; i++) {
            for (int j = 0; j < SozcukTuruUtils.gridSizeX; j++) {
                TextView tv = gridLayout.findViewWithTag(Integer.toString(j) + i);
                tv.setText("");
                tv.setBackground(context.getResources().getDrawable(R.drawable.stroke_bg));
                tv.setEnabled(true);
            }
        }
        SozcukTuruUtils.answer = "";
        try{
            bitmap.eraseColor(Color.TRANSPARENT);
            canvas = new Canvas(bitmap);
            ((WordTourView)context.findViewById(R.id.drawing)).setDrawCanvas(canvas);
            ((WordTourView)context.findViewById(R.id.drawing)).setCanvasBitmap(bitmap);
        } catch (Exception e){
            e.printStackTrace();
        }

        for (int i = 0; i < SozcukTuruUtils.gridSizeX; i++){
            for(int j = 0; j < SozcukTuruUtils.gridSizeY; j++){
                lineGrid[i][j] = "";
            }
        }
    }

    public static void initSomeVar(){
        paint = new Paint();
        paint.setColor(context.getResources().getColor(R.color.shallow_light_red2));
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth((float)pxHeightY/75);
        paint.setAntiAlias(true);
        ((WordTourView)context.findViewById(R.id.drawing)).setDrawPaint(paint);

        for (int i = 0; i < SozcukTuruUtils.gridSizeX; i++){
            for(int j = 0; j < SozcukTuruUtils.gridSizeY; j++){
                lineGrid[j][i] = "";
            }
        }

    }

}
