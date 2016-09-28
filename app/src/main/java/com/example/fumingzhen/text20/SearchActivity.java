package com.example.fumingzhen.text20;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.Map;

public class SearchActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        Intent intent=getIntent();
        Bundle bundle=intent.getExtras();
        ArrayList<Map<String,String>> items=(ArrayList<Map<String,String>>)bundle.getSerializable("result");
        SimpleAdapter adapter=new SimpleAdapter(this,items,R.layout.linear,
                new String[]{Words.Word._ID, Words.Word.COLUMN_NAME_WORD, Words.Word.COLUMN_NAME_MEANING, Words.Word.COLUMN_NAME_SAMPLE},
                new int[]{R.id.textid,R.id.word,R.id.meaning,R.id.sample});
        ListView list=(ListView)findViewById(R.id.searchlist);
        list.setAdapter(adapter);
//        Button b1=(Button)findViewById(R.id.rb);
//        b1.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent=new Intent(SearchActivity.this,MainActivity.class);
//                startActivity(intent);
//                finish();
//            }
//        });
    }
}
