package com.hxgraph.adapter;

import android.graphics.Canvas;
import android.view.MotionEvent;

import com.hxgraph.graphstrategy.GraphStrategyImp;
import com.hxgraph.model.IPoint;
import com.hxgraph.model.imp.PointCollectionImp;
import com.hxgraph.model.param.IStrategyParams;
import com.hxgraph.model.param.IStrategyParamsImp;

import java.util.List;

/**
 * 数据适配器超类，负责主要控制流程
 * Created by liulinru on 2017/4/20.
 */

public abstract class GraphAdapterImp<T extends PointCollectionImp,P extends IStrategyParamsImp> implements IGraphAdapter<T,P> {

    protected Object mORawData;
    protected T mData;
    protected GraphStrategyImp<T> mStrategy;

    /**
     * 填充原始数据，原始数据的类型需要与对应的适配器相符
     * @param rawData
     */
    @Override
    public void receiveData(Object rawData) {
        this.mORawData = rawData;
    }

    /**
     * 获取对应数据结构的类型
     * @return
     */
    protected abstract T getNewModel();

    /**
     * 设置x方向上的比例，需要在 wrapRawData 方法之后调用才会有效
     * @param xsclae 0.0f - 1.0f
     */
    public void setXsclae(float xsclae){
        if(mData != null && xsclae >= 0.0f && xsclae <= 1.0f)
            mData.setmFXscale(xsclae);
    }

    /**
     * 设置y方向上的比例，需要在 wrapRawData 方法之后调用才会有效
     * @param ysclae 0.0f - 1.0f
     */
    public void setYsclae(float ysclae){
        if(mData != null && ysclae >= 0.0f && ysclae <= 1.0f)
            mData.setmFYscale(ysclae);
    }

    /**
     * 设置x方向上的步宽
     * @param step
     */
    public void setXstep(float step){
        if(mData != null)
            mData.setmFXSetp(step);
    }

    /**
     * 用于对原始数据的处理，例如保留待显示数据的显示范围，截断、保留位数等操作
     * @param values
     * @return
     */
    public abstract Object doSomethingWithRawData(Object values);

    /**
     * 绘制图形
     * @param canvas 画布对象
     * @param height 绘制高度
     */
    public  void draw(Canvas canvas,int height){
        draw(canvas,0,0,height);
    }

    /**
     * 绘制图形
     * @param canvas 画布对象
     * @param translateLeft 画布对象的绘制坐标系原点在画布坐标系的x坐标
     * @param tanslateTop 画布对象的绘制坐标系原点在画布坐标系的y坐标
     * @param height 绘制高度
     */
    public void draw(Canvas canvas,int translateLeft,int tanslateTop,int height){
        draw(canvas,translateLeft,tanslateTop,height,0);
    }

    /**
     * 绘制图形
     * @param canvas 画布对象
     * @param translateLeft 画布对象的绘制坐标系原点在画布坐标系的x坐标
     * @param translateTop 画布对象的绘制坐标系原点在画布坐标系的y坐标
     * @param height 绘制高度
     * @param width  绘制宽度 暂时没有用到
     */
    public void draw(Canvas canvas,int translateLeft,int translateTop,int height,int width){
        if(this.mData == null || this.mStrategy == null)
            return;
        this.mData.setmIHeight(height);
        calculateYcoordinate(height);
        //外部没有设入x坐标数据时，需要计算
        if(mData.getmFXCoordinates() == null)
            calculateXcoordinate();
        this.mStrategy.setmCanvasLeft(translateLeft);
        this.mStrategy.setmCanvasTop(translateTop);
        this.mStrategy.draw(mData,canvas);
    }

    /**
     * 计算每个点在x方向上的坐标
     */
    protected  void calculateXcoordinate(){}

    /**
     * 根据画布高度，计算每个点在y方向上的坐标
     * @param graphHeight
     */
    public abstract void calculateYcoordinate(int graphHeight);

    /**
     * 获取所需基本类型的数据结构结合
     * @return
     */
    @Override
    public T getPointCollection() {
        return mData;
    }

    @Override
    public IPoint onTouchAction(MotionEvent event) {
        return null;
    }

    /**
     * 获取绘图所需的策略
     * @return
     */
    public abstract GraphStrategyImp<T> getGraphStrategy();
}