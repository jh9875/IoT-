package com.cookandroid.oreosample;

import android.app.Dialog;
import android.content.Context;
import android.view.Window;

public class ProgressDialog extends Dialog { // 로딩화면 클래스
    public ProgressDialog(Context context){
        super(context);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_progress);

    }



}
