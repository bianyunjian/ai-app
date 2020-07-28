package com.hankutech.ax.appdemo.util;

import android.graphics.Color;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.util.ArrayList;
import java.util.List;

public class ChartUtils {


    /**
     * 更新图表
     *
     * @param barChart 图表
     * @param xValues  x数据
     * @param yValues  y数据
     */
    public static void notifyDataSetChanged(BarChart barChart, ArrayList<String> xValues, ArrayList<Integer> yValues) {
        barChart.invalidate();

        ArrayList<IBarDataSet> dataSets = new ArrayList<>();
        boolean newAdd = false;
        for (int i = 0; i < xValues.size(); i++) {

            ArrayList entries = new ArrayList<>();
            entries.add(new BarEntry(i, yValues.get(i)));

            if (barChart.getData() != null && barChart.getData().getDataSetCount() > 0) {
                BarDataSet dataset = (BarDataSet) barChart.getData().getDataSetByIndex(i);
                dataset.setValues(entries);
                barChart.getData().notifyDataChanged();
                barChart.notifyDataSetChanged();
            } else {
                newAdd = true;
                BarDataSet newDataset = new BarDataSet(entries, "");

                newDataset.setColor(getXColor(i));

                dataSets.add(newDataset);


            }
        }

        if (newAdd) {
            BarData data = new BarData(dataSets);
            data.setValueTextSize(18f);


            float barWidth = 0.7f;
            data.setBarWidth(barWidth);
            barChart.setData(data);

            barChart.invalidate();
        }
    }

    private static int getXColor(int value) {
        if (value == 0) return Color.RED;
        if (value == 1) return Color.YELLOW;
        if (value == 2) return Color.CYAN;
        if (value == 3) return Color.GREEN;
        return Color.GRAY;
    }

    private static String getXLabel(float value) {
        if (value == 0) return "干垃圾";
        if (value == 1) return "湿垃圾";
        if (value == 2) return "有害垃圾";
        if (value == 3) return "其他垃圾";
        return String.valueOf(value);
    }

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
        chart.setScaleEnabled(false);
        // 不显示y轴右边的值
        chart.getAxisRight().setEnabled(false);
        // 不显示图例
        Legend legend = chart.getLegend();
        legend.setEnabled(false);
        // 向左偏移15dp，抵消y轴向右偏移的30dp
        chart.setExtraLeftOffset(-15);

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
