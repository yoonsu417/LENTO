package com.example.lento;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;

import com.github.mikephil.charting.animation.ChartAnimator;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.dataprovider.BarDataProvider;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.renderer.BarChartRenderer;
import com.github.mikephil.charting.utils.Transformer;
import com.github.mikephil.charting.utils.ViewPortHandler;

public class RoundedBarChartRenderer extends BarChartRenderer {

    private final float radius; // 둥근 모서리 반경

    public RoundedBarChartRenderer(BarDataProvider chart, ChartAnimator animator, ViewPortHandler viewPortHandler, float radius) {
        super(chart, animator, viewPortHandler);
        this.radius = radius;
    }

    @Override
    public void drawDataSet(Canvas c, IBarDataSet dataSet, int index) {
        Transformer transformer = mChart.getTransformer(dataSet.getAxisDependency());

        mBarBorderPaint.setColor(dataSet.getBarBorderColor());
        mBarBorderPaint.setStrokeWidth(dataSet.getBarBorderWidth());

        final boolean drawBorder = dataSet.getBarBorderWidth() > 0.0f;

        float phaseX = mAnimator.getPhaseX();
        float phaseY = mAnimator.getPhaseY();

        // Initialize the buffer
        mBarBuffers[index].setPhases(phaseX, phaseY);
        mBarBuffers[index].setDataSet(index);
        mBarBuffers[index].setInverted(mChart.isInverted(dataSet.getAxisDependency()));
        mBarBuffers[index].setBarWidth(mChart.getBarData().getBarWidth());

        mBarBuffers[index].feed(dataSet);

        final RectF barRect = new RectF();

        for (int j = 0; j < mBarBuffers[index].buffer.length; j += 4) {

            // Create path for rounded rectangle
            barRect.left = mBarBuffers[index].buffer[j];
            barRect.top = mBarBuffers[index].buffer[j + 1];
            barRect.right = mBarBuffers[index].buffer[j + 2];
            barRect.bottom = mBarBuffers[index].buffer[j + 3];
            // Adjust bar height
            barRect.top += 0.3f; // 막대의 상단 위치를 0.3만큼 증가
            barRect.bottom += 0.3f; // 막대의 하단 위치를 0.3만큼 증가

            // Transform the rectangle
            transformer.rectToPixelPhase(barRect, phaseY);

            if (!mViewPortHandler.isInBoundsLeft(barRect.right))
                continue;

            if (!mViewPortHandler.isInBoundsRight(barRect.left))
                break;

            Path path = new Path();

            // 색상 설정
            int color;
            if (dataSet.getEntryForIndex(j / 4).getY() < 100) {
                color = Color.parseColor("#FF95A9FE"); // 정확도가 100 미만일 때 색상
            } else {
                color = Color.parseColor("#EEEFF3"); // 정확도가 100일 때 색상
            }

            mRenderPaint.setColor(color);

            // Draw rounded rectangle
            float barRadius = radius; // 막대의 둥근 모서리 반경
            // 막대의 상단과 하단에 둥근 모서리 적용
            path.addRoundRect(barRect, barRadius, barRadius, Path.Direction.CW);
            c.drawPath(path, mRenderPaint);

            if (drawBorder) {
                c.drawPath(path, mBarBorderPaint);
            }
        }
    }



    @Override
    public void drawHighlighted(Canvas c, Highlight[] indices) {
        BarData barData = mChart.getBarData();

        for (Highlight high : indices) {

            IBarDataSet set = barData.getDataSetByIndex(high.getDataSetIndex());

            if (set == null || !set.isHighlightEnabled())
                continue;

            BarEntry e = set.getEntryForXValue(high.getX(), high.getY());

            if (!isInBoundsX(e, set))
                continue;

            Transformer transformer = mChart.getTransformer(set.getAxisDependency());

            mHighlightPaint.setColor(set.getHighLightColor());
            mHighlightPaint.setAlpha(set.getHighLightAlpha());

            RectF barRect = new RectF(e.getX(), 0, e.getX() + barData.getBarWidth(), e.getY());
            transformer.rectToPixelPhase(barRect, mAnimator.getPhaseY());

            // Highlight rounded bar
            Path path = new Path();
            path.addRoundRect(barRect, radius, radius, Path.Direction.CW);
            c.drawPath(path, mHighlightPaint);
        }
    }
}

