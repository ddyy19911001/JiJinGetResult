package com.dy.getresultapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.dy.fastframework.util.HtmlStrUtils;
import com.vise.xsnow.common.GsonUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import yin.deng.normalutils.utils.MyUtils;
import yin.deng.normalutils.utils.NoDoubleClickListener;
import yin.deng.superbase.activity.LogUtils;

public class ResultActivity extends MyBaseActivity{
    private TextView tvResult;
    private ArrayList<JzDataListInfo.DataBean.LSJZListBean> dataListWeek;
    private ArrayList<JzDataListInfo.DataBean.LSJZListBean> dataListTwoWeek;
    private ArrayList<JzDataListInfo.DataBean.LSJZListBean> dataListMonth;
    private ArrayList<JzDataListInfo.DataBean.LSJZListBean> dataListMonthAndHalf;
    private ArrayList<JzDataListInfo.DataBean.LSJZListBean> dataListThreeMonth;
    private double speedWeek;
    private double speedTwoWeek;
    private double speedMonth;
    private double speedMonthAndHalf;
    private double speedThreeMonth;
    private double pjdwOfWeek;
    private double pjdwOfTwoWeek;
    private double pjdwOfMonth;
    private double pjdwOfMonthAndHalf;
    private double pjdwOfThreeMonth;
    private double pjLjOfWeek;
    private double pjLjOfTwoWeek;
    private double pjLjOfMonth;
    private double pjLjOfMonthAndHalf;
    private double pjLjOfThreeMonth;
    private JzDataListInfo.DataBean.LSJZListBean todayJzInfo;
    private double threeDaysPjUpSpeed;
    private double nowJz;
    private double sevenDaysPjUpSpeed;
    private String day;
    private JzDataListInfo.DataBean.LSJZListBean lastDayInfo;
    private String code;
    private ImageView ivCollect;

    @Override
    public int setLayout() {
        return R.layout.activity_result;
    }

    @Override
    public void bindViewWithId() {
        tvResult = (TextView) findViewById(R.id.tv_result);
        ivCollect = (ImageView) findViewById(R.id.iv_collect);

    }

    @Override
    public void initFirst() {
        String data=getIntent().getStringExtra("result");
        day=getIntent().getStringExtra("etDay");
        code=getIntent().getStringExtra("code");
        JiJingInfo jiJingInfos= DbController.getInstance(ResultActivity.this).searchByWhere(code);
        if(jiJingInfos==null){
            //还未收藏
           ivCollect.setImageResource(R.mipmap.ic_collect_normal);
        }else{
           ivCollect.setImageResource(R.mipmap.ic_collect_selected);
            //已收藏
        }
        ivCollect.setOnClickListener(new NoDoubleClickListener() {
            @Override
            protected void onNoDoubleClick(View v) {
                JiJingInfo jiJingInfos= DbController.getInstance(ResultActivity.this).searchByWhere(code);
                if(jiJingInfos==null){
                    //还未收藏
                    final EditText inputServer = new EditText(ResultActivity.this);
                    AlertDialog.Builder builder = new AlertDialog.Builder(ResultActivity.this);
                    builder.setTitle("请为该基金取一个名字").setIcon(R.mipmap.jijing).setView(inputServer)
                            .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            String mMeetName = inputServer.getText().toString();
                            JiJingInfo jiJingInfo=new JiJingInfo();
                            jiJingInfo.setCode(code);
                            jiJingInfo.setName(mMeetName);
                            dialog.dismiss();
                            DbController.getInstance(ResultActivity.this).insert(jiJingInfo);
                            showTs("收藏成功");
                            ivCollect.setImageResource(R.mipmap.ic_collect_selected);
                        }
                    });
                    builder.show();
                }else{
                    //已收藏
                    showTs("已取消收藏");
                    DbController.getInstance(ResultActivity.this).delete(jiJingInfos.getCode());
                    ivCollect.setImageResource(R.mipmap.ic_collect_normal);
                }
            }
        });
        nowJz=getIntent().getDoubleExtra("etJz",-1.0);
        if(data!=null){
            LogUtils.w(data);
            JzDataListInfo jzDataListInfo= GsonUtil.gson().fromJson(data, JzDataListInfo.class);
            LogUtils.d("净值数据获取成功："+jzDataListInfo);
            count(jzDataListInfo);
        }
    }

    /**
     * 这里开始计算涨跌概率
     * @param jzDataListInfo
     */
    private void count(JzDataListInfo jzDataListInfo) {
        List<JzDataListInfo.DataBean.LSJZListBean> listBeans = jzDataListInfo.getData().getLSJZList();
        if (!MyUtils.isEmpty(day)) {
            int xPosition = -1;
            List<JzDataListInfo.DataBean.LSJZListBean> newDatas = new ArrayList<>();
            for (int i = 0; i < listBeans.size(); i++) {
                if (listBeans.get(i).getFSRQ().equals(day)) {
                    xPosition = i;
                }
                if (xPosition > 0) {
                    newDatas.add(listBeans.get(i));
                }
            }
            if(newDatas.size()>0) {
                listBeans.clear();
                listBeans.addAll(newDatas);
            }
        }
        if(nowJz>0){
            JzDataListInfo.DataBean.LSJZListBean bean=new JzDataListInfo.DataBean.LSJZListBean();
            SimpleDateFormat dateFormat=new SimpleDateFormat("yyyy-MM-dd");
            bean.setFSRQ(dateFormat.format(new Date()));
            bean.setDWJZ(String.valueOf(nowJz));
            bean.setLJJZ(String.valueOf(nowJz));
            double lastJz = Double.parseDouble(listBeans.get(0).getDWJZ());
            double zzL = (nowJz - lastJz) * 100 / nowJz;
            java.text.DecimalFormat myformat=new java.text.DecimalFormat("0.000");
            bean.setJZZZL(myformat.format(zzL));
            listBeans.add(0,bean);
        }
        todayJzInfo = listBeans.get(0);
        lastDayInfo = listBeans.get(1);
        dataListWeek = new ArrayList<>();//最近7天的净值数据集合
        dataListTwoWeek = new ArrayList<>();//最近30天的净值数据集合
        dataListMonth = new ArrayList<>();//最近30天的净值数据集合
        dataListMonthAndHalf = new ArrayList<>();//最近30天的净值数据集合
        dataListThreeMonth = new ArrayList<>();//最近三个月的净值数据集合
        for (int i = 0; i < 7; i++) {
            if(listBeans.size()<=i){
                continue;
            }
            dataListWeek.add(listBeans.get(i));
        }
        for (int i = 0; i < 14; i++) {
            if(listBeans.size()<=i){
                continue;
            }
            dataListTwoWeek.add(listBeans.get(i));
        }
        for (int i = 0; i < 30; i++) {
            if(listBeans.size()<=i){
                continue;
            }
            dataListMonth.add(listBeans.get(i));
        }
        for (int i = 0; i < 45; i++) {
            if(listBeans.size()<=i){
                continue;
            }
            dataListMonthAndHalf.add(listBeans.get(i));
        }
        for (int i = 0; i < 90; i++) {
            if(listBeans.size()<=i){
                continue;
            }
            dataListThreeMonth.add(listBeans.get(i));
        }
        dealWithAllDatas();
    }

    /**
     * 此处开始对每个时间段的净值做分析
     */
    private void dealWithAllDatas() {
        double allSpeed=0;
        double allDwJz=0;
        double alLjJz=0;
        double threedDaysAllSpeed=0;
        double sevenDaysAllSpeed=0;
        double twoWeekAllSpeed=0;
        double maxJzOfWeek=0;
        double minJzOfWeek=Double.parseDouble(todayJzInfo.getDWJZ());
        double maxJzOfTwoWeek=0;
        double minJzOfTwoWeek=Double.parseDouble(todayJzInfo.getDWJZ());
        for(int i=0;i<dataListWeek.size();i++){
            allSpeed+=Double.parseDouble(dataListWeek.get(i).getJZZZL());
            allDwJz+=Double.parseDouble(dataListWeek.get(i).getDWJZ());
            alLjJz+=Double.parseDouble(dataListWeek.get(i).getLJJZ());
        }
        threeDaysPjUpSpeed=threedDaysAllSpeed/3;
        sevenDaysPjUpSpeed=threedDaysAllSpeed/7;
        speedWeek=allSpeed/dataListWeek.size();
        pjdwOfWeek=allDwJz/dataListWeek.size();
        pjLjOfWeek=alLjJz/dataListWeek.size();

        allSpeed=0;
        allDwJz=0;
        alLjJz=0;
        for(int i=0;i<dataListTwoWeek.size();i++){
            allSpeed+=Double.parseDouble(dataListTwoWeek.get(i).getJZZZL());
            allDwJz+=Double.parseDouble(dataListTwoWeek.get(i).getDWJZ());
            alLjJz+=Double.parseDouble(dataListTwoWeek.get(i).getLJJZ());
            if(i<3) {
                //最近3天增长率总值
                threedDaysAllSpeed += Double.parseDouble(dataListTwoWeek.get(i).getJZZZL());
            }
            if(i<7) {
                double nowWeekJzL = Double.parseDouble(dataListTwoWeek.get(i).getJZZZL());
                double nowWeekJz = Double.parseDouble(dataListTwoWeek.get(i).getDWJZ());
                if(nowWeekJz>=maxJzOfWeek){
                    maxJzOfWeek=nowWeekJz;
                }
                if(nowWeekJz<=minJzOfWeek){
                    minJzOfWeek=nowWeekJz;
                }
                //最近7天增长率总值
                sevenDaysAllSpeed +=nowWeekJzL ;
            }
            if(i<14) {
                double nowWeekJzL = Double.parseDouble(dataListTwoWeek.get(i).getJZZZL());
                double nowWeekJz = Double.parseDouble(dataListTwoWeek.get(i).getDWJZ());
                if(nowWeekJz>=maxJzOfTwoWeek){
                    maxJzOfTwoWeek=nowWeekJz;
                }
                if(nowWeekJz<=minJzOfTwoWeek){
                    minJzOfTwoWeek=nowWeekJz;
                }
                //最近14天增长率总值
                twoWeekAllSpeed += nowWeekJzL;
            }
        }
        speedTwoWeek=allSpeed/dataListTwoWeek.size();
        pjdwOfTwoWeek=allDwJz/dataListTwoWeek.size();
        pjLjOfTwoWeek=alLjJz/dataListTwoWeek.size();

        allSpeed=0;
        allDwJz=0;
        alLjJz=0;
        for(int i=0;i<dataListMonth.size();i++){
            allSpeed+=Double.parseDouble(dataListMonth.get(i).getJZZZL());
            allDwJz+=Double.parseDouble(dataListMonth.get(i).getDWJZ());
            alLjJz+=Double.parseDouble(dataListMonth.get(i).getLJJZ());
        }
        speedMonth=allSpeed/dataListMonth.size();
        pjdwOfMonth=allDwJz/dataListMonth.size();
        pjLjOfMonth=alLjJz/dataListMonth.size();

        allSpeed=0;
        allDwJz=0;
        alLjJz=0;
        for(int i=0;i<dataListMonthAndHalf.size();i++){
            allSpeed+=Double.parseDouble(dataListMonthAndHalf.get(i).getJZZZL());
            allDwJz+=Double.parseDouble(dataListMonthAndHalf.get(i).getDWJZ());
            alLjJz+=Double.parseDouble(dataListMonthAndHalf.get(i).getLJJZ());
        }
        speedMonthAndHalf=allSpeed/dataListMonthAndHalf.size();
        pjdwOfMonthAndHalf=allDwJz/dataListMonthAndHalf.size();
        pjLjOfMonthAndHalf=alLjJz/dataListMonthAndHalf.size();

        allSpeed=0;
        allDwJz=0;
        alLjJz=0;
        for(int i=0;i<dataListThreeMonth.size();i++){
            allSpeed+=Double.parseDouble(dataListThreeMonth.get(i).getJZZZL());
            allDwJz+=Double.parseDouble(dataListThreeMonth.get(i).getDWJZ());
            alLjJz+=Double.parseDouble(dataListThreeMonth.get(i).getLJJZ());
        }
        speedThreeMonth=allSpeed/dataListThreeMonth.size();
        pjdwOfThreeMonth=allDwJz/dataListThreeMonth.size();
        pjLjOfThreeMonth=alLjJz/dataListThreeMonth.size();
        java.text.DecimalFormat myformat=new java.text.DecimalFormat("0.000");
        StringBuffer buffer=new StringBuffer();
        buffer.append("<font color='#2234F0' size='50px'>");
        buffer.append("--------------------------------------------------------------------------<br>");
        buffer.append("</font>");
        double nowDwJz = Double.parseDouble(todayJzInfo.getDWJZ());
        double nowLjJz = Double.parseDouble(todayJzInfo.getLJJZ());
        double oneWeekper2Jz=(maxJzOfWeek-minJzOfWeek)/2;
        double twoWeekper2Jz=(maxJzOfTwoWeek-minJzOfTwoWeek)/2;
        double perJz=(pjdwOfWeek+pjdwOfTwoWeek)/2;
        double data = Math.abs(pjdwOfWeek - nowDwJz);
        String endStr="";
        if(pjdwOfWeek>nowDwJz){
            endStr=("-"+myformat.format(pjdwOfWeek-nowDwJz)+"元（<font color='#228922'>-"+myformat.format(data*100/nowDwJz)+"%）</font><br>");
        }else{
            endStr=("+"+myformat.format(nowDwJz-pjdwOfWeek)+"元（<font color='#FF2222'>+"+myformat.format(data*100/nowDwJz)+"%）</font><br>");
        }
        buffer.append("<br>近7日平均净值为：<br>"+myformat.format(pjdwOfWeek)+"元"+"\t\t"+endStr+"");
        data = Math.abs(pjdwOfTwoWeek - nowDwJz);
        if(pjdwOfTwoWeek>nowDwJz){
            endStr=("-"+myformat.format(pjdwOfTwoWeek-nowDwJz)+"元（<font color='#228922'>-"+myformat.format(data*100/nowDwJz)+"%）</font><br>");
        }else{
            endStr=("+"+myformat.format(nowDwJz-pjdwOfTwoWeek)+"元（<font color='#FF2222'>+"+myformat.format(data*100/nowDwJz)+"%）</font><br>");
        }
        buffer.append("近14日平均净值为：<br>"+myformat.format(pjdwOfTwoWeek)+"元"+"\t\t"+endStr+"");
        data = Math.abs(pjdwOfMonth - nowDwJz);
        if(pjdwOfMonth>nowDwJz){
            endStr=("-"+myformat.format(pjdwOfMonth-nowDwJz)+"元（<font color='#228922'>-"+myformat.format(data*100/nowDwJz)+"%）</font><br>");
        }else{
            endStr=("+"+myformat.format(nowDwJz-pjdwOfMonth)+"元（<font color='#FF2222'>+"+myformat.format(data*100/nowDwJz)+"%）</font><br>");
        }
        buffer.append("近一个月平均净值为：<br>"+myformat.format(pjdwOfMonth)+"元"+"\t\t"+endStr+"");
        data = Math.abs(pjdwOfMonthAndHalf - nowDwJz);
        if(pjdwOfMonthAndHalf>nowDwJz){
            endStr=("-"+myformat.format(pjdwOfMonthAndHalf-nowDwJz)+"元（<font color='#228922'>-"+myformat.format(data*100/nowDwJz)+"%）</font><br>");
        }else{
            endStr=("+"+myformat.format(nowDwJz-pjdwOfMonthAndHalf)+"元（<font color='#FF2222'>+"+myformat.format(data*100/nowDwJz)+"%）</font><br>");
        }
        buffer.append("近一个半月平均净值为：<br>"+myformat.format(pjdwOfMonthAndHalf)+"元"+"\t\t"+endStr+"");
        data = Math.abs(pjdwOfMonthAndHalf - nowDwJz);
        if(pjdwOfThreeMonth>nowDwJz){
            endStr=("-"+myformat.format(pjdwOfThreeMonth-nowDwJz)+"元（<font color='#228922'>-"+myformat.format(data*100/nowDwJz)+"%）</font><br>");
        }else{
            endStr=("+"+myformat.format(nowDwJz-pjdwOfThreeMonth)+"元（<font color='#FF2222'>+"+myformat.format(data*100/nowDwJz)+"%）</font><br>");
        }
        buffer.append("近三个月平均净值为：<br>"+myformat.format(pjdwOfThreeMonth)+"元"+"\t\t"+endStr+"<br>");

        buffer.append("近7日平均涨幅为："+myformat.format(speedWeek)+"%<br>");
        buffer.append("近14日平均涨幅为："+myformat.format(speedTwoWeek)+"%<br>");
        buffer.append("近一个月平均涨幅为："+myformat.format(speedMonth)+"%<br>");
        buffer.append("近一个半月平均涨幅为："+myformat.format(speedMonthAndHalf)+"%<br>");
        buffer.append("近三个月平均涨幅为："+myformat.format(speedThreeMonth)+"%<br>");
        String details=buffer.toString();
        buffer=new StringBuffer();
        buffer.append("当前日期："+todayJzInfo.getFSRQ()+(day==null?"（昨日最新）":"（模拟日期测试）"));
        if(nowJz>0){
            buffer.append("<br>当前查看净值为:" + nowDwJz+ "元\t\t\t最新涨幅：" + todayJzInfo.getJZZZL() + "%"+"<br><br>平均单位净值："+myformat.format(perJz)+"元<br>");
        }else {
            buffer.append("<br>当前净值：" + nowDwJz + "元\t\t\t最新涨幅：" + todayJzInfo.getJZZZL() + "%"+"<br>平均单位净值："+myformat.format(perJz)+"元<br>");
        }
        Double toDayJz=Double.parseDouble(todayJzInfo.getDWJZ());
        Double lastDayJz=Double.parseDouble(lastDayInfo.getDWJZ());
        double upLv=toDayJz-lastDayJz;
        int buyCode=0;//买入指数
        String result="";
        String msDetails="";
        if(upLv>0){
            //昨日正在上涨
            msDetails+="昨日该基金上涨+"+todayJzInfo.getJZZZL()+"%，";
            if(sevenDaysAllSpeed>=0){
                //近一周正在上涨
                if(sevenDaysAllSpeed>=3){
                    msDetails+="近一周该基金大幅上涨+"+myformat.format(sevenDaysAllSpeed)+"%，";
                }else{
                    msDetails+="近一周该基金小幅上涨+"+myformat.format(sevenDaysAllSpeed)+"%，";
                }
                double xdHightJz = toDayJz - minJzOfWeek;
                if(xdHightJz<=oneWeekper2Jz){
                    //七日均线下方
                    if(xdHightJz<=oneWeekper2Jz/2){
                        //比均值的一半还低
                        msDetails+="超跌小幅反弹，当前净值位于7日均线下方探底，可分批抄底，建议两层仓（值得买入）";
                        buyCode=40;
                    }else{
                        //比均值的一半高一点但不超过均值
                        msDetails+="超跌大幅反弹，当前净值位于7日均线下方中部，可分批买入（适量买入）";
                        buyCode=10;
                    }
                }else{
                    //七日均线上方
                    //极速上涨中，分批卖出
                    if(xdHightJz<=oneWeekper2Jz+oneWeekper2Jz/2){
                        msDetails+="当前净值位于7日均线上方低压区域，可持基待涨或分批卖出";
                        buyCode=20;
                    }else{
                        msDetails+="当前净值位于7日均线上方高压区域，建议大量卖出";
                        buyCode=10;
                    }
                }
            }else{
                //近一周正在下跌
                //下跌趋势减缓
                if(sevenDaysAllSpeed<=-3){
                    msDetails+="近一周该基金大幅下跌"+myformat.format(sevenDaysAllSpeed)+"%，";
                }else{
                    msDetails+="近一周该基金小幅下跌"+myformat.format(sevenDaysAllSpeed)+"%，";
                }
                double xdHightJz = toDayJz - minJzOfWeek;
                if(xdHightJz<=oneWeekper2Jz){
                    //低于七日均线
                    if(xdHightJz<=oneWeekper2Jz/2){
                        //比七日均线一半还低
                        //上涨遇到阻力增大可分批卖出
                        msDetails+="净值位于7日均线下方探底，处于超跌状态，可分批抄底，建议两层仓（值得买入），";
                        buyCode=30;
                    }else{
                        //上涨遇到阻力增大可分批卖出
                        msDetails+="净值位于7日均线下方中部，继续看好，可少量分批买入（少量买入），";
                        buyCode=10;
                    }
                }else{
                    if(xdHightJz<=oneWeekper2Jz+oneWeekper2Jz/2){
                        msDetails+="当前净值位于7日均线上方低压区域，估值适中，可持基待涨或分批卖出";
                        buyCode=10;
                    }else{
                        msDetails+="当前净值位于7日均线上方高压区域，估值偏高，建议大量卖出";
                        buyCode=20;
                    }
                }
            }
            if(twoWeekAllSpeed>=0){
                //近两周正在上涨
                if(twoWeekAllSpeed>=3){
                    msDetails+="<br>近两周该基金大幅上涨+"+myformat.format(twoWeekAllSpeed)+"%，";
                }else{
                    msDetails+="<br>近两周该基金小幅上涨+"+myformat.format(twoWeekAllSpeed)+"%，";
                }
                msDetails+="上涨趋势持续中，";
                double xdHightJz = toDayJz - minJzOfTwoWeek;
                if(xdHightJz<=twoWeekper2Jz){
                    //低于七日均线
                    if(xdHightJz<=twoWeekper2Jz/2){
                        //比七日均线一半还低
                        //上涨遇到阻力增大可分批卖出
                        msDetails+="净值位于14日均线下方探底，超跌状态，可分批抄底，建议三层仓（推荐买入）";
                        buyCode+=30;
                    }else{
                        //上涨遇到阻力增大可分批卖出
                        msDetails+="净值位于14日均线下方中部，继续看好，可少量分批买入（适量买入）";
                        buyCode+=10;
                    }
                }else{
                    //极速上涨中，可定投买入
                    if(xdHightJz<=twoWeekper2Jz+twoWeekper2Jz/2){
                        msDetails+="当前净值位于14日均线上方低压区域，估值适中，可持基待涨或分批卖出（可继续持有）";
                        buyCode-=10;
                    }else{
                        msDetails+="当前净值位于14日均线上方高压区域，估值偏高，建议大量卖出（建议卖出）";
                        buyCode-=20;
                    }
                }
            }else{
                //近两周正在下跌
                //下跌趋势减缓
                if(twoWeekAllSpeed<=-3){
                    msDetails+="<br>近两周该基金大幅下跌"+myformat.format(twoWeekAllSpeed)+"%，";
                }else{
                    msDetails+="<br>近两周该基金小幅下跌"+myformat.format(twoWeekAllSpeed)+"%，";
                }
                msDetails+="下跌趋势有所减缓，";
                double xdHightJz = toDayJz - minJzOfTwoWeek;
                if(xdHightJz<=twoWeekper2Jz){
                    //低于七日均线
                    if(xdHightJz<=twoWeekper2Jz/2){
                        //比七日均线一半还低
                        //上涨遇到阻力增大可分批卖出
                        msDetails+="当前净值位于14日均线下方探底，估值低位，可分批抄底，建议三层仓（值得买入），";
                        buyCode+=30;
                    }else{
                        //上涨遇到阻力增大可分批卖出
                        msDetails+="当前净值位于14日均线下方中部，继续看好，可少量分批买入（适当买入），";
                        buyCode+=10;
                    }
                }else{
                    //极速上涨中，可定投买入
                    if(xdHightJz<=twoWeekper2Jz+twoWeekper2Jz/2){
                        msDetails+="当前净值位于14日均线上方低压区域，估值适中，可持基待涨或分批卖出";
                        buyCode-=10;
                    }else{
                        msDetails+="当前净值位于14日均线上方高压区域，估值偏高，建议大量卖出（建议卖出）";
                        buyCode-=20;
                    }
                }
            }
        }else{
            //昨日正在下跌
            msDetails+="昨日该基金下跌"+todayJzInfo.getJZZZL()+"%，";
            if(sevenDaysAllSpeed>0){
               //近一周正在上涨
               //上涨遇到阻力
                if(sevenDaysAllSpeed>=3){
                    msDetails+="近一周该基金大幅上涨+"+myformat.format(sevenDaysAllSpeed)+"%，";
                }else{
                    msDetails+="近一周该基金小幅上涨+"+myformat.format(sevenDaysAllSpeed)+"%，";
                }
                double xdHightJz = toDayJz - minJzOfTwoWeek;
                if(xdHightJz<=oneWeekper2Jz){
                    //低于七日均线
                    if(xdHightJz<=oneWeekper2Jz/2){
                        //比七日均线一半还低
                        //上涨遇到阻力增大可分批卖出
                        msDetails+="净值位于7日均线下方探底，处于超跌状态，可分批抄底，建议两层仓（值得买入），";
                        buyCode=30;
                    }else{
                        //上涨遇到阻力增大可分批卖出
                        msDetails+="净值位于7日均线下方中部，继续看好，可少量分批买入（适量买入），";
                        buyCode=10;
                    }
                }else{
                    //极速上涨中，可定投买入
                    if(xdHightJz<=oneWeekper2Jz+oneWeekper2Jz/2){
                        msDetails+="当前净值位于7日均线上方低压区域，估值适中，可持基待涨或分批卖出";
                        buyCode=10;
                    }else{
                        msDetails+="当前净值位于7日均线上方高压区域，估值偏高，建议大量卖出（建议卖出）";
                        buyCode=20;
                    }
                }
            }else{
                //近一周正在下跌
                if(sevenDaysAllSpeed<=-3){
                    msDetails+="近一周该基金大幅下跌"+myformat.format(sevenDaysAllSpeed)+"%，";
                }else{
                    msDetails+="近一周该基金小幅下跌"+myformat.format(sevenDaysAllSpeed)+"%，";
                }
                double xdHightJz = toDayJz - minJzOfWeek;
                if(xdHightJz<=oneWeekper2Jz){
                    //低于七日均线
                    if(xdHightJz<=oneWeekper2Jz/2){
                        //比七日均线一半还低
                        //上涨遇到阻力增大可分批卖出
                        msDetails+="净值位于7日均线下方探底，超跌状态，可分批抄底，建议三层仓（值得买入）";
                        buyCode=30;
                    }else{
                        //上涨遇到阻力增大可分批卖出
                        msDetails+="净值位于7日均线下方中部，可考虑少量分批买入（适量买入）";
                        buyCode=10;
                    }
                }else{
                    //极速上涨中，可定投买入
                    if(xdHightJz<=oneWeekper2Jz+oneWeekper2Jz/2){
                        msDetails+="当前净值位于7日均线上方低压区域，估值适中，可持基待涨或分批卖出";
                        buyCode=10;
                    }else{
                        msDetails+="当前净值位于7日均线上方高压区域，估值偏高，建议大量卖出";
                        buyCode=20;
                    }
                }
            }

            if(twoWeekAllSpeed>=0){
                //近两周正在上涨
                if(twoWeekAllSpeed>=3){
                    msDetails+="<br>近两周该基金大幅上涨+"+myformat.format(twoWeekAllSpeed)+"%，";
                }else{
                    msDetails+="<br>近两周该基金小幅上涨+"+myformat.format(twoWeekAllSpeed)+"%，";
                }
                msDetails+="上涨趋势有所减缓，";

                double xdHightJz = toDayJz - minJzOfTwoWeek;
                if(xdHightJz<=twoWeekper2Jz){
                    //低于七日均线
                    if(xdHightJz<=twoWeekper2Jz/2){
                        //比七日均线一半还低
                        //上涨遇到阻力增大可分批卖出
                        msDetails+="净值位于14日均线下方探底，超跌状态，可分批抄底，建议三层仓（推荐买入）";
                        buyCode+=30;
                    }else{
                        //上涨遇到阻力增大可分批卖出
                        msDetails+="净值位于14日均线下方中部，继续看好，可少量分批买入（适量买入）";
                        buyCode+=10;
                    }
                }else{
                    //极速上涨中，可定投买入
                    if(xdHightJz<=twoWeekper2Jz+twoWeekper2Jz/2){
                        msDetails+="当前净值位于14日均线上方低压区域，估值适中，可持基待涨或分批卖出";
                        buyCode-=10;
                    }else{
                        msDetails+="当前净值位于14日均线上方高压区域，估值偏高，建议大量卖出（建议卖出）";
                        buyCode-=20;
                    }
                }
            }else{
                //近两周正在下跌
                //下跌趋势减缓
                if(twoWeekAllSpeed<=-3){
                    msDetails+="<br>近两周该基金大幅下跌"+myformat.format(twoWeekAllSpeed)+"%，";
                }else{
                    msDetails+="<br>近两周该基金小幅下跌"+myformat.format(twoWeekAllSpeed)+"%，";
                }
                msDetails+="下跌趋势持续中，";
                double xdHightJz = toDayJz - minJzOfTwoWeek;
                if(xdHightJz<=twoWeekper2Jz){
                    //低于七日均线
                    if(xdHightJz<=twoWeekper2Jz/2){
                        //比七日均线一半还低
                        //上涨遇到阻力增大可分批卖出
                        msDetails+="当前净值位于14日均线下方探底，估值低位，可分批抄底，建议三层仓（推荐买入），";
                        buyCode+=30;
                    }else{
                        //上涨遇到阻力增大可分批卖出
                        msDetails+="当前净值位于14日均线下方中部，继续看好，可少量分批买入（适量买入），";
                        buyCode+=10;
                    }
                }else{
                    //极速上涨中，可定投买入
                    if(xdHightJz<=twoWeekper2Jz+twoWeekper2Jz/2){
                        msDetails+="当前净值位于14日均线上方低压区域，估值适中，可持基待涨或分批卖出";
                        buyCode-=10;
                    }else{
                        msDetails+="当前净值位于14日均线上方高压区域，估值偏高，建议大量卖出（建议卖出）";
                        buyCode-=20;
                    }
                }
            }


        }
        int perCang = buyCode / 10;
        int saleCang=10-perCang;
        if(buyCode>=3) {
            result = "当前可买入仓位：" + Math.abs(perCang -1)+ "层，" + "当前可卖出:"+Math.abs(saleCang/2-1)+"层";
        }else{
            result="当前建议等待时机买入，当前可卖出仓位："+Math.abs(saleCang/2)+"层";
        }
        buffer.append("<br><font color='#000000' size='90px'>操作建议：</font><br><br><font color='#24a233' size='50px' >"+msDetails+"<br><br>"+result+"</font><br><br>");
        buffer.append(details);
        tvResult.setText(HtmlStrUtils.getHtmlStr(this, tvResult, buffer.toString()));
    }

}
