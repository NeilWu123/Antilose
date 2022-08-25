package com.example.testmap;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class Emermessage extends Activity {

    private EditText PhNoEt;
    private EditText MsgEt;
    private Button saveBtn;
    private Button cancelBtn;
    private String mssg = null;

    //TODO add menu later
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emermessage);
        linkID();
        SharedPreferences sp2 = getSharedPreferences("user.xml",MODE_PRIVATE);
        if(sp2!=null){
            //获得存贮的数据
            String number = sp2.getString("number","");
            mssg = sp2.getString("mssg","");
            //更新UI
            PhNoEt.setText(number);
            MsgEt.setText(mssg);
        }

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //创建sharedpreferencees对象
                SharedPreferences sp = getSharedPreferences("user.xml",MODE_PRIVATE);
                //获得editor对象
                SharedPreferences.Editor editor = sp.edit();
                String number = PhNoEt.getText().toString();
                mssg = MsgEt.getText().toString();
                //将获得的数据写入到文本中
                editor.putString("number",number);
                editor.putString("mssg",mssg);
                //提交数据
                editor.commit();
                Toast.makeText(Emermessage.this,"已保存",Toast.LENGTH_SHORT).show();
                Intent intent=new Intent(Emermessage.this,MainActivity.class);
                intent.putExtra("number",number);
                intent.putExtra("mssg",mssg);
            }
        });
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(Emermessage.this,MainActivity.class);
                startActivity(intent);
                Toast.makeText(Emermessage.this,"已取消",Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void linkID() {
        PhNoEt = findViewById(R.id.PH_NO);
        MsgEt = findViewById(R.id.TXT_CON);
        saveBtn = findViewById(R.id.INFO_SAVE);
        cancelBtn = findViewById(R.id.INFO_CANCEL);
    }
}
