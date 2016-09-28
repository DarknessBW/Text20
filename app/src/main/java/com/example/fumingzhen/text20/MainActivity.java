package com.example.fumingzhen.text20;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.preference.DialogPreference;
import android.provider.ContactsContract;
import android.provider.UserDictionary;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    WordsDBHelper mDbHelper;
    //ListView list;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ListView list =(ListView)findViewById(R.id.list);
        registerForContextMenu(list);
        mDbHelper =new WordsDBHelper(this);
        ArrayList<Map<String, String>> items =getAll();
        setWordsListView(items);
    }
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        getMenuInflater().inflate(R.menu.contextmenu_wordslistview,menu);
    }
    public boolean onContextItemSelected(MenuItem item){
        TextView textId=null;
        TextView textWord=null;
        TextView textMeaning=null;
        TextView textSample=null;
        AdapterView.AdapterContextMenuInfo info=null;
        View itemView=null;
        switch (item.getItemId()){
            case R.id.action_delete:
                info=(AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
                itemView=info.targetView;
                textId =(TextView)itemView.findViewById(R.id.textid);
                if(textId!=null){
                    String strId=textId.getText().toString();
                    DeleteDialog(strId);
                }
                break;
            case R.id.action_update:{
                info=(AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
                itemView=info.targetView;
                textId =(TextView)itemView.findViewById(R.id.textid);
                textWord =(TextView)itemView.findViewById(R.id.word);
                textMeaning =(TextView)itemView.findViewById(R.id.meaning);
                textSample =(TextView)itemView.findViewById(R.id.sample);
                if(textId!=null && textWord!=null && textMeaning!=null && textSample!=null){
                    String strId=textId.getText().toString();
                    String strWord=textWord.getText().toString();
                    String strMeaning=textMeaning.getText().toString();
                    String strSample=textSample.getText().toString();
                    UpdateDialog(strId, strWord, strMeaning, strSample);
                }
                break;
            }
        }
        return true;
    }
    private void UpdateDialog(final String strId, final String strWord, final String strMeaning, final String strSample) {
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        final LinearLayout layout=(LinearLayout)this.getLayoutInflater().inflate(R.layout.insert, null);
        builder.setTitle("修改单词");
        builder.setView(layout);
        ((EditText)layout.findViewById(R.id.txtWord)).setText(strWord);
        ((EditText)layout.findViewById(R.id.txtMeaning)).setText(strMeaning);
        ((EditText)layout.findViewById(R.id.txtSample)).setText(strSample);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                String strNewWord = ((EditText) layout.findViewById(R.id.txtWord)).getText().toString();
                String strNewMeaning = ((EditText) layout.findViewById(R.id.txtMeaning)).getText().toString();
                String strNewSample = ((EditText) layout.findViewById(R.id.txtSample)).getText().toString();
                UpdateUseSql(strId, strNewWord, strNewMeaning, strNewSample);
                setWordsListView(getAll());
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        builder.create();
        builder.show();
    }

    private void UpdateUseSql(String strId,String strWord, String strMeaning, String strSample) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        String sql="update words set word=?,meaning=?,sample=? where _id=?";
        db.execSQL(sql, new String[]{strWord, strMeaning, strSample,strId});
    }


    private void DeleteDialog(final String strId){
        new AlertDialog.Builder(this).setTitle("删除单词").setMessage("是否真的删除单词?").setPositiveButton("确定", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                DeleteUseSql(strId);
                setWordsListView(getAll());
            }
        }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        }).create().show();
    }
    private void DeleteUseSql(String strId){
        String sql="delete from words where _id='"+strId+"'";
        SQLiteDatabase db=mDbHelper.getReadableDatabase();
        db.execSQL(sql);
    }
    protected void onDestroy(){
        super.onDestroy();
        mDbHelper.close();
    }
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_main,menu);
        return  true;
    }
    public boolean onOptionsItemSelected(MenuItem item){
        int id=item.getItemId();
        switch (id){
            case R.id.action_search:
                SearchDialog();
                return true;
            case R.id.action_insert:
                InsertDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    private ArrayList<Map<String, String>> SearchUseSql(String strWordSearch) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        String sql="select * from words where word like ? order by word desc";
        Cursor c=db.rawQuery(sql,new String[]{"%"+strWordSearch+"%"});
        return ConvertCursor2List(c);
    }

    private ArrayList<Map<String,String>> ConvertCursor2List(Cursor c) {
        ArrayList<Map<String,String>> result=new ArrayList<>();
        while(c.moveToNext()){
            Map<String,String> map=new HashMap<>();
            map.put(Words.Word._ID,String.valueOf(c.getInt(0)));
            map.put(Words.Word.COLUMN_NAME_WORD,c.getString(c.getColumnIndex(Words.Word.COLUMN_NAME_WORD)));
            map.put(Words.Word.COLUMN_NAME_MEANING,c.getString(c.getColumnIndex(Words.Word.COLUMN_NAME_MEANING)));
            map.put(Words.Word.COLUMN_NAME_SAMPLE,c.getString(c.getColumnIndex(Words.Word.COLUMN_NAME_SAMPLE)));
            result.add(map);
        }
        return result;
    }

    private void SearchDialog() {
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        final LinearLayout layout=(LinearLayout)this.getLayoutInflater().inflate(R.layout.searchterm, null);
        builder.setTitle("查询单词");
        builder.setView(layout);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialogInterface, int i) {
                String txtSearchWord=((EditText)layout.findViewById(R.id.txtSearchWord)).getText().toString();
                ArrayList<Map<String, String>> items=null;
                items=SearchUseSql(txtSearchWord);
                if(items.size()>0) {
                    Bundle bundle=new Bundle();
                    bundle.putSerializable("result",items);
                    Intent intent=new Intent(MainActivity.this,SearchActivity.class);
                    intent.putExtras(bundle);
                    startActivity(intent);
                }else
                    Toast.makeText(MainActivity.this,"没有找到",Toast.LENGTH_LONG).show();
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        builder.create();
        builder.show();
    }
    private void Insert(String strWord, String strMeaning, String strSample) {
        //Gets the data repository in write mode*/
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(Words.Word.COLUMN_NAME_WORD, strWord);
        values.put(Words.Word.COLUMN_NAME_MEANING, strMeaning);
        values.put(Words.Word.COLUMN_NAME_SAMPLE, strSample);
        long newRowId;
        newRowId = db.insert(
                Words.Word.TABLE_NAME,
                null,
                values);
    }

    private void InsertDialog() {
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        final LinearLayout layout=(LinearLayout)this.getLayoutInflater().inflate(R.layout.insert, null);
        builder.setTitle("新增单词");
        builder.setView(layout);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface,int i){
                String strWord=((EditText)layout.findViewById(R.id.txtWord)).getText().toString();
                String strMeaning=((EditText)layout.findViewById(R.id.txtMeaning)).getText().toString();
                String strSample=((EditText)layout.findViewById(R.id.txtSample)).getText().toString();
                Insert(strWord,strMeaning,strSample);
                ArrayList<Map<String, String>> items =getAll();
                setWordsListView(items);
            }
        });
        builder.setNegativeButton("取消",new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialogInterface,int i){

            }
        });
        builder.create();
        builder.show();

    }

    public ArrayList<Map<String, String>> getAll() {
        SQLiteDatabase db;
        db=mDbHelper.getReadableDatabase();
        ArrayList<Map<String,String>> items=new ArrayList<>();
        String searchSQL="select * from words";
        Cursor cursor=db.rawQuery(searchSQL,null);
        while (cursor.moveToNext()){
            //items.add(cursor.getString(cursor.getColumnIndex(Words.Word.COLUMN_NAME_WORD)));
            Map<String,String>item=new HashMap<>();
            item.put(Words.Word._ID,String.valueOf(cursor.getInt(0)));
            item.put(Words.Word.COLUMN_NAME_WORD,cursor.getString(cursor.getColumnIndex(Words.Word.COLUMN_NAME_WORD)));
            item.put(Words.Word.COLUMN_NAME_MEANING,cursor.getString(cursor.getColumnIndex(Words.Word.COLUMN_NAME_MEANING)));
            item.put(Words.Word.COLUMN_NAME_SAMPLE,cursor.getString(cursor.getColumnIndex(Words.Word.COLUMN_NAME_SAMPLE)));
            items.add(item);
        }
        return items;
    }
    public void setWordsListView(ArrayList<Map<String, String>> items) {
        SimpleAdapter adapter=new SimpleAdapter(this,items,R.layout.linear,new String[]{Words.Word._ID,Words.Word.COLUMN_NAME_WORD,Words.Word.COLUMN_NAME_MEANING,Words.Word.COLUMN_NAME_SAMPLE},new int[]{R.id.textid,R.id.word,R.id.meaning,R.id.sample});
        ListView list=(ListView)findViewById(R.id.list);
        list.setAdapter(adapter);
    }
}