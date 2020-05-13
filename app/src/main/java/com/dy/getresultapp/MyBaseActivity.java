package com.dy.getresultapp;

import com.dy.fastframework.activity.BaseActivity;
import com.dy.fastframework.view.CommonMsgDialog;

public abstract class MyBaseActivity extends BaseActivity {
    public void showMsgDialog(String msg){
        CommonMsgDialog commonMsgDialog=new CommonMsgDialog(this);
        commonMsgDialog.showMsg(msg);
    }
}
