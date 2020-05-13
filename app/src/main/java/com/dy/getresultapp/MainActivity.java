package com.dy.getresultapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseViewHolder;
import com.dy.fastframework.util.HtmlStrUtils;
import com.dy.fastframework.view.CommonMsgDialog;
import com.google.gson.Gson;
import com.scwang.smartrefresh.layout.help.MyQuckAdapter;
import com.vise.xsnow.common.GsonUtil;
import com.vise.xsnow.http.ViseHttp;
import com.vise.xsnow.http.callback.ACallback;
import com.vise.xsnow.http.config.HttpGlobalConfig;
import com.vise.xsnow.http.exception.ApiException;
import com.vise.xsnow.http.exception.IBaseRequestErroLitener;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.time.temporal.UnsupportedTemporalTypeException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import yin.deng.normalutils.utils.MyUtils;
import yin.deng.normalutils.utils.NoDoubleClickListener;
import yin.deng.normalutils.view.PopuwindowUtils;
import yin.deng.superbase.activity.LogUtils;

public class MainActivity extends MyBaseActivity {
    private EditText etNum;
    private Button btCount;
    private EditText etJz;
    private EditText etDay;
    private TextView tvResult;
    private TextView tvData;
    private ImageView ivNext;
    private List<JiJingInfo> datas=new ArrayList<>();

    @Override
    public int setLayout() {
        return R.layout.activity_main;
    }

    @Override
    public void bindViewWithId() {
        etNum = (EditText) findViewById(R.id.et_num);
        etJz = (EditText) findViewById(R.id.et_jz);
        btCount = (Button) findViewById(R.id.bt_count);
        tvResult = (TextView) findViewById(R.id.result);
        tvData = (TextView) findViewById(R.id.tv_data);
        ivNext = (ImageView) findViewById(R.id.iv_next);
        etDay=findViewById(R.id.et_day);
    }

    @Override
    public void initFirst() {
        ivNext.setOnClickListener(new NoDoubleClickListener() {
            @Override
            protected void onNoDoubleClick(View v) {
                showPop();
            }
        });
        btCount.setOnClickListener(new NoDoubleClickListener() {
            @Override
            protected void onNoDoubleClick(View v) {
                if(!MyUtils.isEmpty(etNum)){
                    getDetails(etNum.getText().toString().trim());
                }else{
                    showMsgDialog("请输入内容再进行此操作！");
                }
            }
        });
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (!MainActivity.this.isFinishing()) {
                    try {
                        getHs300Datas();
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }){
        }.start();
    }

    private void showPop() {
        getLocalData();
        if(datas.size()==0){
            showMsgDialog("请先收藏基金后再使用此功能");
            return;
        }
        final MyPopuwindowUtils popuwindowUtils=new MyPopuwindowUtils(this);
        popuwindowUtils.createPopupLayout(R.layout.pop_root);
        RecyclerView rc = popuwindowUtils.getContentView().findViewById(R.id.rcView);
        LinearLayoutManager manager=new LinearLayoutManager(this);
        manager.setOrientation(RecyclerView.VERTICAL);
        rc.setLayoutManager(manager);
        MyQuckAdapter<JiJingInfo> quckAdapter=new MyQuckAdapter<JiJingInfo>(R.layout.pop_item,datas,this) {
            @Override
            protected void convert(final BaseViewHolder helper, final JiJingInfo item) {
                helper.setText(R.id.tv_item, item.getName()+"("+item.getCode()+")");
                helper.getView(R.id.tv_item).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        popuwindowUtils.dismissPop();
                        etNum.setText(item.getCode());
                    }
                });
            }
        };
        rc.setAdapter(quckAdapter);
        popuwindowUtils.showPopWindow(etNum,0,0, Gravity.BOTTOM);
    }

    /**
     * 获取本地收藏的基金对象
     */
    private void getLocalData() {
       List<JiJingInfo> jiJingInfos= DbController.getInstance(this).searchAll();
       if(jiJingInfos!=null){
           datas.clear();
           datas.addAll(jiJingInfos);
       }
    }

    public void getDetails(final String findCode){
        ViseHttp.CONFIG().httpErroListener(new IBaseRequestErroLitener() {
            @Override
            public void onHttpErro(ApiException e) {
                closeDialog();
                showMsgDialog("查询失败，请重新输入");
            }
        });
        showLoadingDialog("正在计算...", true);
        ViseHttp.GET(BaseConfig.getOneDetails+findCode+BaseConfig.getOneDetails_end+new Date().getTime()+"000")
                .addHeader("Host", "api.fund.eastmoney.com")
                .addHeader("Referer", "http://fundf10.eastmoney.com/jjjz_"+findCode+".html")
                .tag(this)
                .request(new ACallback<String>() {
                    @Override
                    public void onSuccess(String data) {
                        LogUtils.i("data:"+data);
                        closeDialog();
                        if(data!=null) {
                            data=data.substring(data.indexOf("(")+1,data.lastIndexOf(")"));
                            JzDataListInfo jzDataListInfo = GsonUtil.gson().fromJson(data, JzDataListInfo.class);
                            if (jzDataListInfo.getData() == null) {
                                showMsgDialog("找不到对应基金，请重新输入编码");
                                return;
                            }
                        }
                        Intent intent=new Intent(MainActivity.this,ResultActivity.class);
                        intent.putExtra("result", data);
                        if(!MyUtils.isEmpty(etJz)){
                            double jzCustom = Double.parseDouble(etJz.getText().toString().trim());
                            intent.putExtra("etJz", jzCustom);
                        }else{
                            intent.putExtra("etJz", -1.0);
                        }
                        SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd");
                        if(!MyUtils.isEmpty(etDay)){
                            String jzCustom = etDay.getText().toString();
                            intent.putExtra("etDay", jzCustom);
                        }
                        intent.putExtra("code", findCode);
                        startActivity(intent);
                    }

                    @Override
                    public void onFail(int errCode, String errMsg) {
                        closeDialog();
                        showMsgDialog(errMsg);
                    }
                });
    }



    public void getHs300Datas(){
        ViseHttp.GET(BaseConfig.daPan+System.currentTimeMillis()+"000"+"&_="+System.currentTimeMillis()+"999")
                .baseUrl("http://push2.eastmoney.com/")
                .tag(this)
                .request(new ACallback<String>() {
                    @Override
                    public void onSuccess(String data) {
                        closeDialog();
                        if(data!=null) {
                            data=data.substring(data.indexOf("(")+1,data.lastIndexOf(")"));
                            LogUtils.i("data:"+data);
                            HsInfos hsInfos=new Gson().fromJson(data, HsInfos.class);
                            double nowPoint=new BigDecimal(hsInfos.getData().getF43())
                                    .divide(new BigDecimal(100),2,BigDecimal.ROUND_HALF_UP)
                                    .doubleValue();
                            double startPoint=new BigDecimal(hsInfos.getData().getF60())
                                    .divide(new BigDecimal(100), 2, BigDecimal.ROUND_HALF_UP)
                                    .doubleValue();
                            double mostLowPoint=new BigDecimal(hsInfos.getData().getF45())
                                    .divide(new BigDecimal(100), 2, BigDecimal.ROUND_HALF_UP)
                                    .doubleValue();
                            double mostHighPoint=new BigDecimal(hsInfos.getData().getF44())
                                    .divide(new BigDecimal(100), 2, BigDecimal.ROUND_HALF_UP)
                                    .doubleValue();
                            double nowPerUp = new BigDecimal(mostHighPoint-mostLowPoint)
                                    .divide(new BigDecimal(2),2,BigDecimal.ROUND_HALF_UP)
                                    .doubleValue();//波动的平均值
                            double upPercent=new BigDecimal(nowPoint-startPoint)
                                    .multiply(new BigDecimal(100))
                                    .divide(new BigDecimal(nowPoint),2,BigDecimal.ROUND_HALF_UP)
                                    .doubleValue();
                            if(upPercent>0){
                                tvData.setText("上证指数："+nowPoint+"    当前涨跌幅:+"+upPercent+"%");
                            }else{
                                tvData.setText("上证指数："+nowPoint+"    当前涨跌幅:"+upPercent+"%");
                            }
                            double upCode=50.00;
                            if(hsInfos.getData().getF60()>=hsInfos.getData().getF44()){
                                //起点比最高点还高--->在下方振荡
                                double maxLength = new BigDecimal(startPoint)
                                        .subtract(new BigDecimal(mostLowPoint))
                                        .doubleValue();
                                double nowLenth=startPoint-nowPoint;
                                BigDecimal bigDecimal1=new BigDecimal(nowLenth);
                                double percent = bigDecimal1.divide(new BigDecimal(maxLength) ,2, BigDecimal.ROUND_HALF_UP).doubleValue()  ;
                                upCode-=percent*(100-upCode)/2;
                            }else if(hsInfos.getData().getF60()<=hsInfos.getData().getF45()){
                                //起点比最低点还低--->在上方振荡
                                double maxLength = new BigDecimal(mostHighPoint)
                                        .subtract(new BigDecimal(startPoint))
                                        .doubleValue();
                                double nowLenth=nowPoint-startPoint;
                                BigDecimal bigDecimal1=new BigDecimal(nowLenth);
                                double percent = bigDecimal1.divide(new BigDecimal(maxLength) ,2, BigDecimal.ROUND_HALF_UP).doubleValue()  ;
                                upCode+=percent*(100-upCode)/2;
                            }else {
                                double nowPerupPoint=mostHighPoint-nowPerUp;
                                if(nowPerupPoint<startPoint){
                                    //均线位于下方
                                    double nowLength = Math.abs(nowPoint - startPoint);
                                    double maxLength = new BigDecimal(mostHighPoint)
                                            .subtract(new BigDecimal(mostLowPoint))
                                            .doubleValue();
                                    double percent = new BigDecimal(nowLength).divide(new BigDecimal(maxLength), 2, BigDecimal.ROUND_HALF_UP).doubleValue();
                                    if(nowPoint>startPoint){
                                        //当前处在水平线上方
                                        upCode += percent * 50;
                                    }else{
                                        //当前处在水平线下方
                                        upCode -= percent * 50;
                                    }

                                }else{
                                    //均线位于上方
                                    double nowLength = Math.abs(nowPoint - startPoint);
                                    double maxLength = new BigDecimal(mostHighPoint)
                                            .subtract(new BigDecimal(mostLowPoint))
                                            .doubleValue();
                                    double percent = new BigDecimal(nowLength).divide(new BigDecimal(maxLength), 2, BigDecimal.ROUND_HALF_UP).doubleValue();
                                    if(nowPoint>startPoint){
                                        //当前处在水平线上方
                                        upCode += percent * 50;
                                    }else{
                                        //当前处在水平线下方
                                        upCode -= percent * 50;
                                    }
                                }
                            }
                            java.text.DecimalFormat myformat=new java.text.DecimalFormat("0");
                            if(upCode>=50){
                                tvResult.setText("涨："+myformat.format(upCode)+"%\t\t"+"跌："+myformat.format(100-upCode)+"%");
                            }else{
                                tvResult.setText("涨："+myformat.format(upCode)+"%\t\t"+"跌："+myformat.format(100-upCode)+"%");
                            }
                        }
                    }

                    @Override
                    public void onFail(int errCode, String errMsg) {
                        closeDialog();
                        showMsgDialog(errMsg);
                    }
                });
    }




    @Override
    public boolean setIsExitActivity() {
        return true;
    }




   //  //累计净值
    //        if(pjLjOfWeek>nowLjJz){
    //            buffer.append("比最近7日累计净值低:"+myformat.format(pjLjOfWeek-nowLjJz)+"元\n");
    //        }else{
    //            buffer.append("比最近7日累计净值高:"+myformat.format(nowLjJz-pjLjOfWeek)+"元\n");
    //        }
    //        if(pjLjOfTwoWeek>nowLjJz){
    //            buffer.append("比最近14日累计净值低:"+myformat.format(pjLjOfTwoWeek-nowLjJz)+"元\n");
    //        }else{
    //            buffer.append("比最近14日累计净值高:"+myformat.format(nowLjJz-pjLjOfTwoWeek)+"元\n");
    //        }
    //        if(pjLjOfMonth>nowLjJz){
    //            buffer.append("比最近一个月累计净值低:"+myformat.format(pjLjOfMonth-nowLjJz)+"元\n");
    //        }else{
    //            buffer.append("比最近一个月累计净值高:"+myformat.format(nowLjJz-pjLjOfMonth)+"元\n");
    //        }
    //        if(pjLjOfMonthAndHalf>nowLjJz){
    //            buffer.append("比最近一个半月累计净值低:"+myformat.format(pjLjOfMonthAndHalf-nowLjJz)+"元\n");
    //        }else{
    //            buffer.append("比最近一个半月累计净值高:"+myformat.format(nowLjJz-pjLjOfMonthAndHalf)+"元\n");
    //        }
    //        if(pjLjOfThreeMonth>nowLjJz){
    //            buffer.append("比最近三个月累计净值低:"+myformat.format(pjLjOfThreeMonth-nowLjJz)+"元\n");
    //        }else{
    //            buffer.append("比最近三个月累计净值高:"+myformat.format(nowLjJz-pjLjOfThreeMonth)+"元\n");
    //        }
}
