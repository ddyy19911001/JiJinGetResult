package com.dy.getresultapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.dy.fastframework.util.HtmlStrUtils;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.vise.xsnow.common.GsonUtil;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.BitSet;
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
    private LineChart mLineChart;
    private TextView tvChange;
    private int nowLineType=7;
    private TextView tvDetails;
    private double maxUpPercentOfWeek;
    private double maxUpPercentOfTwoWeek;
    private double maxUpPercentOfMonth;
    private double maxJzOfTwoWeek;
    private double minJzOfTwoWeek;
    private double maxJzOfWeek;
    private double minJzOfWeek;
    private double maxJzOfMonth;
    private double minJzOfMonth;
    private double lowAndAddedMoney=100;
    private double upAndAddMoney =0;
    private double createStockMoney=2000;
    private double tEarnedMoney;
    private List<AddedMoneyInfo> tDaysAddedMoney=new ArrayList<>();
    private double todayNeedAdd;
    private double leavePercent=0.6;//剩余仓位百分数
    private double getMoneyPercent=3;
    private double sevenDaysHasCounts;
    private double twoWeeksHasCounts;
    private double monthHasCounts;
    private double threeMonthHasCounts;
    private TextView tvChangeGetMoneyPercent;
    private JzDataListInfo jzDataListInfo;
    private double tAddedAllMoney;

    @Override
    public int setLayout() {
        return R.layout.activity_result;
    }

    @Override
    public void bindViewWithId() {
        tvResult = (TextView) findViewById(R.id.tv_result);
        tvDetails = (TextView) findViewById(R.id.tv_details);
        tvChange = (TextView) findViewById(R.id.tv_change);
        tvChangeGetMoneyPercent = (TextView) findViewById(R.id.change_get_moneyPercent);
        ivCollect = (ImageView) findViewById(R.id.iv_collect);
        mLineChart = (LineChart) findViewById(R.id.mLineChar);
        tvChange.setOnClickListener(new NoDoubleClickListener() {
            @Override
            protected void onNoDoubleClick(View v) {
                initLineChart(nowLineType==7?14:(nowLineType==14?30:7));
                initLimitLine();
            }
        });
        tvChangeGetMoneyPercent.setOnClickListener(new NoDoubleClickListener() {
            @Override
            protected void onNoDoubleClick(View v) {
                if(getMoneyPercent<=4) {
                    getMoneyPercent += 0.5;
                }else{
                    getMoneyPercent=2;
                }
                tvChangeGetMoneyPercent.setText("预测结果："+getMoneyPercent);
                count(jzDataListInfo);
            }
        });
        tvChangeGetMoneyPercent.setText("预测结果："+getMoneyPercent);
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
            jzDataListInfo= GsonUtil.gson().fromJson(data, JzDataListInfo.class);
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
        double zzl = Double.parseDouble(todayJzInfo.getJZZZL());
        if(zzl<0){
            todayNeedAdd=Math.abs(lowAndAddedMoney*(zzl/0.1));
        }else{
            todayNeedAdd= upAndAddMoney *(1-zzl);
            if(todayNeedAdd<=0){
                todayNeedAdd=0;
            }
        }
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

    private void initLimitLine() {
        // 设置x轴的LimitLine
        float nowPjData=0f;
        if(nowLineType==7){
            nowPjData= (float) pjdwOfWeek;
        }else if(nowLineType==14){
            nowPjData= (float) pjdwOfTwoWeek;
        }else{
            nowPjData= (float) pjdwOfMonth;
        }
        mLineChart.getAxisLeft().removeAllLimitLines();
        LimitLine yLimitLine = new LimitLine(nowPjData, nowLineType + "日均值：" + myformat.format(nowPjData));
        yLimitLine.setLineColor(Color.RED);
        yLimitLine.setTextColor(Color.RED);
        // 获得左侧侧坐标轴
        YAxis leftAxis = mLineChart.getAxisLeft();
        leftAxis.addLimitLine(yLimitLine);
        mLineChart.invalidate();
    }

    java.text.DecimalFormat myformat=new java.text.DecimalFormat("0.000");
    private void initLineChart(int type) {
        nowLineType=type;
        mLineChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {

            }

            @Override
            public void onNothingSelected() {

            }
        });
        mLineChart.getDescription().setEnabled(false);
        mLineChart.setBackgroundColor(Color.WHITE);

        //自定义适配器，适配于X轴
        ValueFormatter xAxisFormatter = new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                java.text.DecimalFormat myformat=new java.text.DecimalFormat("0");
                return myformat.format(value);
            }
        };

        XAxis xAxis = mLineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(xAxisFormatter);

        //自定义适配器，适配于Y轴
        ValueFormatter yAxisFormatter = new ValueFormatter(){
            @Override
            public String getFormattedValue(float value) {
                java.text.DecimalFormat myformat=new java.text.DecimalFormat("0.0000");
                return myformat.format(value);
            }

        };

        YAxis leftAxis = mLineChart.getAxisLeft();
        leftAxis.setLabelCount(7, false);
        leftAxis.setValueFormatter(yAxisFormatter);
        leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        leftAxis.setSpaceTop(2f);
        float max=0f;
        float min=Float.parseFloat(todayJzInfo.getDWJZ());
        if(type==7){
            double canKaoSaleJz=(1+(maxUpPercentOfWeek/2+maxUpPercentOfWeek/4)/100)*pjdwOfWeek;
            double canKaoBuyJz=(1-(maxUpPercentOfWeek/2-maxUpPercentOfWeek/4)/100)*pjdwOfWeek;
            String weekStr="（卖出："+myformat.format(canKaoSaleJz)+"，买入："+myformat.format(canKaoBuyJz)+")";
            tvChange.setText("近7日数据"+weekStr+"\n"+"（振幅："+maxUpPercentOfWeek+"%，卖出："+myformat.format(maxUpPercentOfWeek/2+maxUpPercentOfWeek/4)+"%，买入："+myformat.format(maxUpPercentOfWeek/2-maxUpPercentOfWeek/4)+"%）");
            for(int i=0;i<dataListWeek.size();i++){
                float nowJz=Float.parseFloat(dataListWeek.get(i).getDWJZ());
                if(nowJz>max){
                    max=nowJz;
                }
                if(nowJz<min){
                    min=nowJz;
                }
            }
        }else if(type==14){
            double canKaoSaleJz=(1+(maxUpPercentOfTwoWeek/2+maxUpPercentOfTwoWeek/4)/100)*pjdwOfTwoWeek;
            double canKaoBuyJz=(1-(maxUpPercentOfTwoWeek/2-maxUpPercentOfTwoWeek/4)/100)*pjdwOfTwoWeek;
            String twoWeekOfStr="（卖出："+myformat.format(canKaoSaleJz)+"，买入："+myformat.format(canKaoBuyJz)+")";
            tvChange.setText("近14日数据"+twoWeekOfStr+"\n"+"（振幅："+maxUpPercentOfTwoWeek+"%，卖出："+myformat.format(maxUpPercentOfTwoWeek/2+maxUpPercentOfTwoWeek/4)+"%，买入："+myformat.format(maxUpPercentOfTwoWeek/2-maxUpPercentOfTwoWeek/4)+"%）");
            for(int i=0;i<dataListTwoWeek.size();i++){
                float nowJz=Float.parseFloat(dataListTwoWeek.get(i).getDWJZ());
                if(nowJz>max){
                    max=nowJz;
                }
                if(nowJz<min){
                    min=nowJz;
                }
            }
        }else{
            double canKaoSaleJz=(1+(maxUpPercentOfMonth/2+maxUpPercentOfMonth/4)/100)*pjdwOfMonth;
            double canKaoBuyJz=(1-(maxUpPercentOfMonth/2-maxUpPercentOfMonth/4)/100)*pjdwOfMonth;
            String twoWeekOfStr="（卖出："+myformat.format(canKaoSaleJz)+"，买入："+myformat.format(canKaoBuyJz)+")";
            tvChange.setText("近30日数据"+twoWeekOfStr+"\n"+"（振幅："+maxUpPercentOfMonth+"%，卖出："+myformat.format(maxUpPercentOfMonth/2+maxUpPercentOfMonth/4)+"%，买入："+myformat.format(maxUpPercentOfMonth/2-maxUpPercentOfMonth/4)+"%）");
            for(int i=0;i<dataListMonth.size();i++){
                float nowJz=Float.parseFloat(dataListMonth.get(i).getDWJZ());
                if(nowJz>max){
                    max=nowJz;
                }
                if(nowJz<min){
                    min=nowJz;
                }
            }
        }
        leftAxis.setAxisMaximum(max+0.001f);
        leftAxis.setAxisMinimum(min-0.001f);

        initLimitLine();
        mLineChart.getAxisRight().setEnabled(false);
        setLineChartData(type);
    }

    private void setLineChartData(int type) {
        //填充数据，在这里换成自己的数据源
        List<Entry> valsComp1 = new ArrayList<>();

        if(type==7){
            for(int i=0;i<dataListWeek.size();i++){
                valsComp1.add(new Entry((i+1),Float.parseFloat(dataListWeek.get(dataListWeek.size()-1-i).getDWJZ())));
            }
        }else if(type==14){
            for(int i=0;i<dataListTwoWeek.size();i++){
                valsComp1.add(new Entry((i+1),Float.parseFloat(dataListTwoWeek.get(dataListTwoWeek.size()-1-i).getDWJZ())));
            }
        }else{
            for(int i=0;i<dataListMonth.size();i++){
                valsComp1.add(new Entry((i+1),Float.parseFloat(dataListMonth.get(dataListMonth.size()-1-i).getDWJZ())));
            }
        }

        //这里，每重新new一个LineDataSet，相当于重新画一组折线
        //每一个LineDataSet相当于一组折线。比如:这里有两个LineDataSet：setComp1，setComp2。
        //则在图像上会有两条折线图，分别表示公司1 和 公司2 的情况.还可以设置更多
        LineDataSet setComp1 = new LineDataSet(valsComp1, "近"+type+"日基金走势");
        setComp1.setAxisDependency(YAxis.AxisDependency.LEFT);
        setComp1.setColor(getResources().getColor(R.color.retry_color));
        setComp1.setDrawCircles(false);
        setComp1.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);
        List<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(setComp1);
        LineData lineData = new LineData(dataSets);
        mLineChart.setData(lineData);
        mLineChart.invalidate();
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
        maxJzOfWeek=0;
        minJzOfWeek=Double.parseDouble(todayJzInfo.getDWJZ());
        maxJzOfTwoWeek=0;
        minJzOfTwoWeek=Double.parseDouble(todayJzInfo.getDWJZ());
        maxJzOfMonth=0;
        sevenDaysHasCounts=createStockMoney;
        twoWeeksHasCounts=createStockMoney;
        monthHasCounts=createStockMoney;
        threeMonthHasCounts=createStockMoney;
        minJzOfMonth=Double.parseDouble(todayJzInfo.getDWJZ());
        double sevenDaysNowMoney=createStockMoney;
        double addedMoney=0;
        double nowSevenDaysGetMoney=0;
        double sevenDaysearnEdMoney=0;
        for(int i=0;i<dataListWeek.size();i++){
            if(MyUtils.isEmpty(dataListWeek.get(i).getJZZZL())){
                continue;
            }
            allSpeed+=Double.parseDouble(dataListWeek.get(i).getJZZZL());
            allDwJz+=Double.parseDouble(dataListWeek.get(i).getDWJZ());
            alLjJz+=Double.parseDouble(dataListWeek.get(i).getLJJZ());
            double zzl = Double.parseDouble(dataListWeek.get(dataListWeek.size()-1-i).getJZZZL());
            if(zzl<0){
                addedMoney=Math.abs(lowAndAddedMoney*(zzl/0.1));
            }else{
                addedMoney= upAndAddMoney *(1-zzl);
                if(addedMoney<=0){
                    addedMoney=0;
                }
            }
            if(sevenDaysNowMoney>0) {
                //加仓钱赚的的钱
                sevenDaysearnEdMoney += zzl * sevenDaysNowMoney / 100;
                //加仓后的钱
                sevenDaysNowMoney += addedMoney;
                double earnPercent = sevenDaysearnEdMoney * 100 / sevenDaysNowMoney;
                if(earnPercent>=getMoneyPercent){
                    sevenDaysNowMoney=sevenDaysNowMoney*leavePercent;
                    sevenDaysearnEdMoney=sevenDaysearnEdMoney*leavePercent;
                    nowSevenDaysGetMoney+=sevenDaysearnEdMoney*(1-leavePercent);
                }
                if(addedMoney>0){
                    sevenDaysHasCounts+=addedMoney;
                }
            }
        }
        threeDaysPjUpSpeed=threedDaysAllSpeed/3;
        sevenDaysPjUpSpeed=threedDaysAllSpeed/7;
        speedWeek=allSpeed/dataListWeek.size();
        pjdwOfWeek=allDwJz/dataListWeek.size();
        pjLjOfWeek=alLjJz/dataListWeek.size();

        allSpeed=0;
        allDwJz=0;
        alLjJz=0;
        double twoWeeksNowMoney=createStockMoney;
        double twoWeeksEarnEdMoney=0;
        double nowTwoWeeksGetMoney=0;
        addedMoney=0;
        for(int i=0;i<dataListTwoWeek.size();i++){
            if(MyUtils.isEmpty(dataListTwoWeek.get(i).getJZZZL())){
                continue;
            }
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

            double zzl = Double.parseDouble(dataListTwoWeek.get(dataListTwoWeek.size()-1-i).getJZZZL());
            if(zzl<0){
                addedMoney=Math.abs(lowAndAddedMoney*(zzl/0.1));
            }else{
                addedMoney= upAndAddMoney *(1-zzl);
                if(addedMoney<=0){
                    addedMoney=0;
                }
            }
            if(twoWeeksNowMoney>0) {
                //加仓钱赚的的钱
                twoWeeksEarnEdMoney += zzl * twoWeeksNowMoney / 100;
                //加仓后的钱
                twoWeeksNowMoney += addedMoney;
                double earnPercent = twoWeeksEarnEdMoney * 100 / twoWeeksNowMoney;
                if(earnPercent>=getMoneyPercent){
                    twoWeeksNowMoney=twoWeeksNowMoney*leavePercent;
                    twoWeeksEarnEdMoney=twoWeeksEarnEdMoney*leavePercent;
                    nowTwoWeeksGetMoney+=twoWeeksEarnEdMoney*(1-leavePercent);
                }
                if(addedMoney>0){
                    twoWeeksHasCounts+=addedMoney;
                }
            }
        }
        speedTwoWeek=allSpeed/dataListTwoWeek.size();
        pjdwOfTwoWeek=allDwJz/dataListTwoWeek.size();
        pjLjOfTwoWeek=alLjJz/dataListTwoWeek.size();
        allSpeed=0;
        allDwJz=0;
        alLjJz=0;

        double monthNowMoney=createStockMoney;
        double monthEarnEdMoney=0;
        double monthGetMoney=0;
        addedMoney=0;
        for(int i=0;i<dataListMonth.size();i++){
            allSpeed+=Double.parseDouble(dataListMonth.get(i).getJZZZL());
            allDwJz+=Double.parseDouble(dataListMonth.get(i).getDWJZ());
            alLjJz+=Double.parseDouble(dataListMonth.get(i).getLJJZ());
            if(i<30) {
                double nowWeekJz = Double.parseDouble(dataListMonth.get(i).getDWJZ());
                if(nowWeekJz>=maxJzOfMonth){
                    maxJzOfMonth=nowWeekJz;
                }
                if(nowWeekJz<=minJzOfMonth){
                    minJzOfMonth=nowWeekJz;
                }
            }

            double zzl = Double.parseDouble(dataListMonth.get(dataListMonth.size()-1-i).getJZZZL());
            if(zzl<0){
                addedMoney=Math.abs(lowAndAddedMoney*(zzl/0.1));
            }else{
                addedMoney= upAndAddMoney *(1-zzl);
                if(addedMoney<=0){
                    addedMoney=0;
                }
            }

            //计算做T的情况收益如何
            if(zzl<0){
                //做T开始
                AddedMoneyInfo addedMoneyInfo=new AddedMoneyInfo();
                addedMoneyInfo.setAddedMoney(addedMoney);
                addedMoneyInfo.setNowDayStr(dataListMonth.get(dataListMonth.size()-1).getFSRQ());
                addedMoneyInfo.setNowJz(Double.parseDouble(dataListMonth.get(dataListMonth.size()-1-i).getDWJZ()));
                addedMoneyInfo.setNowHasCount(new BigDecimal(addedMoney)
                        .divide(new BigDecimal(addedMoneyInfo.getNowJz()),2,BigDecimal.ROUND_HALF_UP)
                        .doubleValue());
                tAddedAllMoney+=addedMoney;
                tDaysAddedMoney.add(addedMoneyInfo);
            }else{
                List<AddedMoneyInfo> clearedList=new ArrayList<>();
                for(int j=0;j<tDaysAddedMoney.size();j++) {
                    double tDwjz=tDaysAddedMoney.get(j).getNowJz();
                    double nowdwJz = Double.parseDouble(dataListMonth.get(dataListMonth.size() - 1 - i).getDWJZ());
                    if(tDwjz>nowdwJz){
                        continue;
                    }
                    double percent = (nowdwJz - tDwjz) * 100 / nowdwJz;
                    if(percent/2.5>=1){
                     //全T出去
                      tEarnedMoney+=tDaysAddedMoney.get(j).getAddedMoney()*percent/100;
                     clearedList.add(tDaysAddedMoney.get(j));
                    }else if(percent/1.5>=1) {
                     //T一半出去
                      tEarnedMoney+=tDaysAddedMoney.get(j).getAddedMoney()/2*percent/100;
                      tDaysAddedMoney.get(j).setAddedMoney(tDaysAddedMoney.get(j).getAddedMoney()/2);
                      tDaysAddedMoney.get(j).setNowHasCount(tDaysAddedMoney.get(j).getNowHasCount()/2);
                    }
                }
                tDaysAddedMoney.removeAll(clearedList);
            }


            if(monthNowMoney>0) {
                //加仓钱赚的的钱
                monthEarnEdMoney += zzl * monthNowMoney / 100;
                //加仓后的钱
                monthNowMoney += addedMoney;
                double earnPercent = monthEarnEdMoney * 100 / twoWeeksNowMoney;
                if(earnPercent>=getMoneyPercent){
                    monthNowMoney=monthNowMoney*leavePercent;
                    monthEarnEdMoney=monthEarnEdMoney*leavePercent;
                    monthGetMoney+=monthEarnEdMoney*(1-leavePercent);
                }
                if(addedMoney>0){
                    monthHasCounts+=addedMoney;
                }
            }
        }



        maxUpPercentOfWeek= new BigDecimal(Math.abs(maxJzOfWeek-minJzOfWeek)*100)
                .divide(new BigDecimal(minJzOfWeek),2,BigDecimal.ROUND_HALF_UP)
                .doubleValue();
        maxUpPercentOfTwoWeek= new BigDecimal(Math.abs(maxJzOfTwoWeek-minJzOfTwoWeek)*100)
                .divide(new BigDecimal(minJzOfTwoWeek),2,BigDecimal.ROUND_HALF_UP)
                .doubleValue();
        maxUpPercentOfMonth= new BigDecimal(Math.abs(maxJzOfMonth-minJzOfMonth)*100)
                .divide(new BigDecimal(minJzOfMonth),2,BigDecimal.ROUND_HALF_UP)
                .doubleValue();
        speedMonth=allSpeed/dataListMonth.size();
        pjdwOfMonth=allDwJz/dataListMonth.size();
        pjLjOfMonth=alLjJz/dataListMonth.size();

        allSpeed=0;
        allDwJz=0;
        alLjJz=0;
        for(int i=0;i<dataListMonthAndHalf.size();i++){
            if(MyUtils.isEmpty(dataListMonthAndHalf.get(i).getJZZZL())){
                continue;
            }
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

        double threeMonthNowMoney=createStockMoney;
        double threeMonthEarnEdMoney=0;
        double threeMonthGetMoney=0;
        addedMoney=0;

        for(int i=0;i<dataListThreeMonth.size();i++){
            if(MyUtils.isEmpty(dataListThreeMonth.get(i).getJZZZL())){
                continue;
            }
            allSpeed+=Double.parseDouble(dataListThreeMonth.get(i).getJZZZL());
            allDwJz+=Double.parseDouble(dataListThreeMonth.get(i).getDWJZ());
            alLjJz+=Double.parseDouble(dataListThreeMonth.get(i).getLJJZ());

            double zzl = Double.parseDouble(dataListThreeMonth.get(dataListThreeMonth.size()-1-i).getJZZZL());
            if(zzl<0){
                addedMoney=Math.abs(lowAndAddedMoney*(zzl/0.1));
            }else{
                addedMoney= upAndAddMoney *(1-zzl);
                if(addedMoney<=0){
                    addedMoney=0;
                }
            }
            if(threeMonthNowMoney>0) {
                //加仓钱赚的的钱
                threeMonthEarnEdMoney += zzl * threeMonthNowMoney / 100;
                //加仓后的钱
                threeMonthNowMoney += addedMoney;
                double earnPercent = threeMonthEarnEdMoney * 100 / threeMonthNowMoney;
                if(earnPercent>=getMoneyPercent){
                    threeMonthNowMoney=threeMonthNowMoney*leavePercent;
                    threeMonthEarnEdMoney=threeMonthEarnEdMoney*leavePercent;
                    threeMonthGetMoney+=threeMonthEarnEdMoney*(1-leavePercent);
                }

                if(addedMoney>0){
                    threeMonthHasCounts+=addedMoney;
                }
            }

        }
        speedThreeMonth=allSpeed/dataListThreeMonth.size();
        pjdwOfThreeMonth=allDwJz/dataListThreeMonth.size();
        pjLjOfThreeMonth=alLjJz/dataListThreeMonth.size();
        initLineChart(nowLineType);
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
        buffer.append("<br><font color='#000000' size='90px'>操作建议：</font><br><br><font color='#24a233' size='50px' ><br>"+msDetails+"<br><br>"+result+"</font><br><br>");
        if(todayNeedAdd>0) {
            buffer.append("<br>根据公式，今日该基金应该加仓" + myformat.format(todayNeedAdd) + "元<br>");
        }else{
            buffer.append("<br>根据公式，今日该基金无需进行操作<br>");
        }
        tvResult.setText(HtmlStrUtils.getHtmlStr(this, tvResult, buffer.toString()));
        StringBuffer otherWayData=new StringBuffer();
        otherWayData.append("<font color='#f43633' size='60px'><br><br>一周买入卖出后预计回报："+myformat.format(sevenDaysearnEdMoney+nowSevenDaysGetMoney)+"元，剩余持仓金额："+myformat.format(sevenDaysNowMoney)+"元。总计投入："+myformat.format(sevenDaysHasCounts)+"<br>");
        otherWayData.append("<br>两周买入卖出后预计回报："+myformat.format(twoWeeksEarnEdMoney+nowTwoWeeksGetMoney)+"元，剩余持仓金额："+myformat.format(twoWeeksNowMoney)+"元总计投入："+myformat.format(twoWeeksHasCounts)+"。<br>");
        otherWayData.append("<br>一月买入卖出后预计回报："+myformat.format(monthEarnEdMoney+monthGetMoney)+"元，剩余持仓金额："+myformat.format(monthNowMoney)+"元。总计投入："+myformat.format(monthHasCounts)+"<br>");
        otherWayData.append("<br>三个月买入卖出后预计回报："+myformat.format(threeMonthEarnEdMoney+threeMonthGetMoney)+"元，剩余持仓金额："+myformat.format(threeMonthNowMoney)+"元。总计投入："+myformat.format(threeMonthHasCounts)+"<br></font>");
        double leaveTdaysMoney=0;
        for(int i=0;i<tDaysAddedMoney.size();i++){
            leaveTdaysMoney+=tDaysAddedMoney.get(i).getAddedMoney();
        }
        otherWayData.append("<br>一个月做T后预计回报："+myformat.format(tEarnedMoney)+"元，总计投入："+myformat.format(tAddedAllMoney)+"元，剩余持仓T本金："+myformat.format(leaveTdaysMoney)+"元<br></font>");
        tvDetails.setText(HtmlStrUtils.getHtmlStr(this,tvDetails,details+"<br><br>"+otherWayData.toString()));
    }



}
