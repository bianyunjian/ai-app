package com.hankutech.ax.appdemo.util;

import android.graphics.Color;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ChartUtils {


    /**
     * 更新图表
     *
     * @param barChart  图表
     * @param xValues   x数据
     * @param dataLists y数据
     */
    public static void notifyDataSetChanged(BarChart barChart, ArrayList<String> xValues, LinkedHashMap<String, List<Integer>> dataLists) {
        barChart.invalidate();

        ArrayList<IBarDataSet> dataSets = new ArrayList<>();
        int currentPosition = 0;//用于柱状图颜色集合的index

        for (Map.Entry<String, List<Integer>> entry : dataLists.entrySet()) {
            String name = entry.getKey();
            List<Integer> yValueList = entry.getValue();

            List<BarEntry> entries = new ArrayList<>();

            for (int i = 0; i < yValueList.size(); i++) {
                entries.add(new BarEntry(i, yValueList.get(i)));
            }
            // 每一个BarDataSet代表一类柱状图
            BarDataSet barDataSet = new BarDataSet(entries, name);
            barDataSet.setColor(getXColor(currentPosition));
            dataSets.add(barDataSet);

            currentPosition++;
        }

        //X轴自定义值
        barChart.getXAxis().setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                if (xValues != null && xValues.size() > 0) {
                    return xValues.get((int) value % xValues.size());
                }

                return "";
            }
        });


        BarData data = new BarData(dataSets);
        data.setValueTextSize(18f);

/**
 * float groupSpace = 0.3f;   //柱状图组之间的间距
 * float barSpace =  0.05f;  //每条柱状图之间的间距  一组两个柱状图
 * float barWidth = 0.3f;    //每条柱状图的宽度     一组两个柱状图
 * (barWidth + barSpace) * barAmount + groupSpace = (0.3 + 0.05) * 2 + 0.3 = 1.00
 * 3个数值 加起来 必须等于 1 即100% 按照百分比来计算 组间距 柱状图间距 柱状图宽度
 */

//设置组间距占比30% 每条柱状图宽度占比 70% /barAmount  柱状图间距占比 0%
        float groupSpace = 0.2f; //柱状图组之间的间距
        float barSpace = 0.05f;
        float barWidth = 0.3f;

//设置柱状图宽度
        data.setBarWidth(barWidth);
//(起始点、柱状图组间距、柱状图之间间距)
        data.groupBars(-0.35f, groupSpace, barSpace);
        barChart.setData(data);

        barChart.invalidate();

    }

    private static int getXColor(int value) {
        if (value == 0) return Color.argb(255, 91, 155, 213);
        if (value == 1) return Color.argb(255, 237, 125, 49);
        if (value == 2) return Color.CYAN;
        if (value == 3) return Color.GREEN;
        return Color.GRAY;
    }

//    private static String getXLabel(float value) {
//        if (value == 0) return "干垃圾";
//        if (value == 1) return "湿垃圾";
//        if (value == 2) return "有害垃圾";
//        if (value == 3) return "其他垃圾";
//        return String.valueOf(value);
//    }

    /**
     * 初始化图表
     *
     * @param chart      原始图表
     * @param xAxisValue
     * @return 初始化后的图表
     */
    public static BarChart initChart(BarChart chart, List<String> xAxisValue) {
        // 不显示数据描述
        chart.getDescription().setEnabled(false);
        // 没有数据的时候，显示“暂无数据”
        chart.setNoDataText("暂无数据");
        // 不显示表格颜色
        chart.setDrawGridBackground(false);
        // 不可以缩放
        chart.setScaleEnabled(true);
        // 不显示y轴右边的值
        chart.getAxisRight().setEnabled(false);
        // 不显示图例
        Legend legend = chart.getLegend();
        legend.setEnabled(true);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        legend.setTextColor(Color.argb(255, 117, 117, 117));
        legend.setTextSize(12);


        //x坐标轴设置
        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawLabels(true);
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(xAxisValue.size());
//        xAxis.setCenterAxisLabels(true);//设置标签居中
        xAxis.setValueFormatter(new IndexAxisValueFormatter(xAxisValue));
//        xAxis.setAxisMinimum(1f);
        xAxis.setTextColor(Color.argb(255, 117, 117, 117));
        xAxis.setTextSize(13);
        chart.setExtraBottomOffset(10f);

        //y轴设置
        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        leftAxis.setDrawGridLines(false);
        leftAxis.setDrawLabels(false);
        leftAxis.setDrawAxisLine(false);


        chart.animateX(2000);//数据显示动画，从左往右依次显示
        chart.invalidate();
        return chart;
    }


}
