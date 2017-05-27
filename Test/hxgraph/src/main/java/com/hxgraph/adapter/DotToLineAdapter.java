package com.hxgraph.adapter;

import android.graphics.Canvas;

import com.hxgraph.graphstrategy.DotToLineStrategy;
import com.hxgraph.graphstrategy.GraphStrategyImp;
import com.hxgraph.model.Constant;
import com.hxgraph.model.GraphUtils;
import com.hxgraph.model.imp.group.LineModel;
import com.hxgraph.model.imp.raw.LinePointModel;
import com.hxgraph.model.param.DotToLineStrategyParam;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 点画线数据适配器
 * Created by liulinru on 2017/4/20.
 */

public class DotToLineAdapter extends GraphAdapterImp<LineModel,DotToLineStrategyParam> {


    protected double[] mDValues;//转换并处理后的原始数据
    protected double mDMaxValue;//mDValues 范围内的最大值
    protected int mIMaxIndex;//mDValues 范围内的最大值的坐标
    protected double mDMinValue;//mDValues 范围内的最小值
    protected int mIMinIndex;//mDValues 范围内的最小值坐标
    protected GraphOrientation orientation = GraphOrientation.UP;

    @Override
    public GraphStrategyImp<LineModel> getGraphStrategy() {
        mStrategy = new DotToLineStrategy();
        return mStrategy;
    }

    @Override
    protected LineModel getNewModel() {
        return new LineModel();
    }

    @Override
    public LineModel wrapRawData(DotToLineStrategyParam params) {
        if(this.mORawData == null || !(mORawData instanceof double[]))
            return null;
        double[] values = (double[])this.mORawData;
        Object obj = doSomethingWithRawData(values);
        if(obj == null || !(obj instanceof double[]))
            return null;
        this.mDValues =  (double[])obj;
        mData = getNewModel();
        //线条参数设置
        if(params != null){
            mData.setmIColor(params.getColor());
            mData.setmFStrokeWidth(params.getStrokeWidth());
            mData.setmOLineType(params.getLineType());
            mData.setmDotLineParam(params.getDotLineParam());
            if(params.getxCoordinates() != null)
                mData.setmFXCoordinates(params.getxCoordinates());
        }
        maxMinValue();
        calculateYcoordinateScale(orientation);
        return mData;
    }

    /**
     * 根据画布高度，计算每个点在y方向上的坐标
     * @param graphHeight
     */
    public void calculateYcoordinate(int graphHeight){
        if(mData == null)
            return;
        List<LinePointModel> list = mData.getIPointSet();
        if(list != null){
            for(LinePointModel pointModel : list){
                float scale = pointModel.getfYValuePercent();
                pointModel.setfYcoordinateRaw(graphHeight*scale);
            }
        }
    }

    @Override
    protected void calculateXcoordinate() {
    }

    /**
     * 根据最值和展示方向，计算每个点在y方向上最大值最小值之间所占比例
     * @param orientation
     */
    protected void calculateYcoordinateScale(GraphOrientation orientation){
        List<LinePointModel> list = new ArrayList<LinePointModel>();
        double diff = mDMaxValue - mDMinValue;
        diff = diff < 0.0 ? 0.0 : diff;
        //在这个范围内，近似看做最值相等
//        if(GraphUtils.doubleEqual0(diff)){
//
//        }else {
//
//        }
        for (int i = 0; i < mDValues.length; i++) {
            LinePointModel point = new LinePointModel();
            point.setfXcoordinateRaw(Constant.fDefaultX);
            point.setfYValuePercent((float) orientation.getScale(diff,mDMinValue,mDValues[i]));
            point.setfValue((float) mDValues[i]);
            list.add(point);
            if(mIMinIndex == i){
                mData.getmMaxMinPoints().setMinPoint(point,i);
            }else if(mIMaxIndex == i){
                mData.getmMaxMinPoints().setMaxPoint(point,i);
            }
        }
        mData.setIPointSet(list);
    }

    //搜索最值以及下标
    private void maxMinValue(){
        if(mDValues != null){
            mIMaxIndex = 0;
            mIMinIndex = 0;
            mDMaxValue = mDValues[0];
            mDMinValue= mDValues[0];
            for(int i=0;i<mDValues.length;i++){
                if(mDMaxValue < mDValues[i]){
                    mDMaxValue = mDValues[i];
                    mIMaxIndex = i;
                }else if(mDMinValue > mDValues[i]){
                    mDMinValue = mDValues[i];
                    mIMinIndex = i;
                }
            }
        }
    }

    /**
     * 用于对原始数据的处理，例如保留待显示数据的显示范围，截断、保留位数等操作
     * @param values
     * @return
     */
    @Override
    public Object doSomethingWithRawData(Object values){
        if(values != null && values instanceof double[])
            return ((double[])values).clone();
        return null;
    }

    public GraphOrientation getOrientation() {
        return orientation;
    }

    public void setOrientation(GraphOrientation orientation) {
        this.orientation = orientation;
    }

    public double[] getmDValues() {
        return mDValues;
    }

    public double getmDMaxValue() {
        return mDMaxValue;
    }

    public int getmIMaxIndex() {
        return mIMaxIndex;
    }

    public double getmDMinValue() {
        return mDMinValue;
    }

    public int getmIMinIndex() {
        return mIMinIndex;
    }

    public enum GraphOrientation{
        UP(0),//图线x轴为下标轴，y轴负向为数据轴递增方向
        DOWM(1),//图线x轴为下标轴，y轴正向为数据轴递增方向
        LEFT(2),//图线y轴为下标轴，x轴负向为数据轴递增方向
        RIGHT(3);//图线y轴为下标轴，x轴负向为数据轴递增方向

        private int tag;
        private GraphOrientation(int tag){
            this.tag = tag;
        }

        /**
         * 获取比例
         * @param diff
         * @param min
         * @param value
         * @return
         */
        public double getScale(double diff,double min,double value){
            Boolean result = GraphUtils.doubleCompare(value,min);
            switch (this) {
                case UP:
                case RIGHT:
                    if(GraphUtils.doubleEqual0(diff) || result == null) {
                        return 1.0;
                    }else if(result){
                        return 1.0 - (value-min)/diff;
                    }

                case DOWM:
                case LEFT:
                    if(GraphUtils.doubleEqual0(diff) || result == null) {
                        return 0.0;
                    }
                    else if(result){
                        return (value-min)/diff;
                    }
            }
            return 0.0;
        }
    }
}