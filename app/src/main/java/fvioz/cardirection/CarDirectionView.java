package fvioz.cardirection;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;
import android.view.MotionEvent;

import java.util.ArrayList;
import java.util.HashMap;


public class CarDirectionView extends View {

    public Point Midpoint(Point a, Point b) {
        return new Point((a.X + b.X) / 2, (a.Y + b.Y) / 2);
    }

    public float Slope(Point from, Point to) {
        return (to.Y - from.Y) / (to.X - from.X);
    }

    public double Magnitude(Point a, Point b) {
        return Math.sqrt(Math.pow((a.X - b.X), 2) + Math.pow((a.Y - b.Y), 2));
    }

    class Point {
        int INDEX;
        float X, Y;

        public Point(float X0, float Y0) {
            INDEX = -1000;
            X = X0;
            Y = Y0;
        }

        public void SetIndex(int ID0) {
            INDEX = ID0;
        }

        public void SetPositions(float X0, float Y0) {
            X = X0;
            Y = Y0;
        }

        public void Rotate(float angle, Point rcenter) {
            X -= rcenter.X;
            Y -= rcenter.Y;

            double rads = Math.toRadians(angle);
            float cos = (float)Math.cos(rads);
            float sin = (float)Math.sin(rads);
            float x_temp = X;
            X = X * cos - Y * sin;
            Y = x_temp * sin + Y * cos;

            X += rcenter.X;
            Y += rcenter.Y;
        }
    }

    class Triangle {
        Point p1, p2, p3;

        public Triangle(Point p01, Point p02, Point p03) {
            p1 = p01;
            p2 = p02;
            p3 = p03;
        }

        public double Angle() {
            Point center = GetCircumcenter();
            Point direction = GetDirection();

            double xDiff = direction.X - center.X;
            double yDiff = direction.Y - center.Y;
            double angle = Math.toDegrees(Math.atan2(yDiff, xDiff));

            if (angle < 0) {
                angle = angle * -1;
            } else {
                angle = 360 - angle;
            }

            return angle;
        }

        public Point GetCircumcenter() {
            Point p1_p2_mid = Midpoint(p1, p2);
            Point p1_p3_mid = Midpoint(p1, p3);
            Point p2_p3_mid = Midpoint(p2, p3);

            float slope_p1_p2 = -1 / Slope(p1, p2);
            float slope_p1_p3 = -1 / Slope(p1, p3);

            float b_p1_p2 = p1_p2_mid.Y - slope_p1_p2 * p1_p2_mid.X;
            float b_p1_p3 = p1_p3_mid.Y - slope_p1_p3 * p1_p3_mid.X;

            float x = (b_p1_p2 - b_p1_p3) / (slope_p1_p3 - slope_p1_p2);
            float y = slope_p1_p2 * x + b_p1_p2;

            return new Point(x, y);
        }

        public double CircleRadius() {
            Point center = GetCircumcenter();
            double line1 = Magnitude(p1, center);
            double line2 = Magnitude(p2, center);
            double line3 = Magnitude(p3, center);

            return Math.max(Math.max(line1, line2), line3);
        }

        public Point GetDirection() {
            double line1 = Math.sqrt(Math.pow((p2.X - p1.X), 2) + Math.pow((p2.Y - p1.Y), 2));
            double line2 = Math.sqrt(Math.pow((p3.X - p2.X), 2) + Math.pow((p3.Y - p2.Y), 2));
            double line3 = Math.sqrt(Math.pow((p1.X - p3.X), 2) + Math.pow((p1.Y - p3.Y), 2));

            if(line1 < line2 &&  line1 < line3) {
                return p3;
            }

            else if(line2 < line1 &&  line2 < line3) {
                return p1;
            }

            else if(line3 < line1 &&  line3 < line2) {
                return p2;
            }

            return p1;
        }

    }

    Paint paint = new Paint();
    ArrayList<Triangle> triangles = new ArrayList<>();
    HashMap<Integer, Point> points = new HashMap<>();

    public CarDirectionView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint.setAntiAlias(true);
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);

        float x = 220;
        float y = 300;
        Point p = new Point(x, y - 100);
        p.SetIndex(0);

        Triangle triangle = new Triangle(p, new Point(x - 70, y + 100), new Point(x + 70, y + 100));
        triangles.add(triangle);
    }

    @Override
    public void onDraw(Canvas canvas) {

        Path path = new Path();

        for (Triangle triangle : triangles) {

            // Variables

            float angle = (float)triangle.Angle();
            float radius = (float)triangle.CircleRadius();
            Point center = triangle.GetCircumcenter();
            Point direction = triangle.GetDirection();

            // Arrow Line
            paint.setStrokeWidth(radius * 0.45f);
            paint.setColor(Color.rgb(68, 180, 73));
            Point arrow_direction = new Point(
                    (direction.X - center.X) * 1.45f + center.X,
                    (direction.Y - center.Y) * 1.45f + center.Y
            );
            canvas.drawLine(center.X, center.Y, arrow_direction.X, arrow_direction.Y, paint);

            // Arrow Triangle
            float arrow_distance = (float)Magnitude(center, arrow_direction);
            Point arrow_p1 = new Point(center.X + arrow_distance - 1.f, center.Y + radius * 0.5f);
            Point arrow_p2 = new Point(center.X + arrow_distance - 1.f, center.Y - radius * 0.5f);
            Point arrow_p3 = new Point(center.X + arrow_distance - 1.f + radius * 0.5f, center.Y);

            arrow_p1.Rotate(-angle, center);
            arrow_p2.Rotate(-angle, center);
            arrow_p3.Rotate(-angle, center);

            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.rgb(68, 180, 73));
            path.reset();
            path.moveTo(arrow_p1.X, arrow_p1.Y);
            path.lineTo(arrow_p2.X, arrow_p2.Y);
            path.lineTo(arrow_p3.X, arrow_p3.Y);
            path.lineTo(arrow_p1.X, arrow_p1.Y);
            path.close();
            canvas.drawPath(path, paint);

            // Circle
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.rgb(68, 180, 73));
            canvas.drawCircle(center.X, center.Y, radius * 1.15f, paint);

            // Orientation
            paint.setStrokeWidth(3.f);
            paint.setTextSize(30.f);
            paint.setColor(Color.BLACK);
            canvas.drawText(String.valueOf(angle), arrow_p3.X, arrow_p3.Y, paint);

            // Triangle
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.YELLOW);
            path.reset();
            path.moveTo(triangle.p1.X, triangle.p1.Y);
            path.lineTo(triangle.p2.X, triangle.p2.Y);
            path.lineTo(triangle.p3.X, triangle.p3.Y);
            path.lineTo(triangle.p1.X, triangle.p1.Y);
            path.close();
            canvas.drawPath(path, paint);
        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        // Point
        Point point;

        // get pointer index from the event object
        int pointerIndex = event.getActionIndex();

        // get pointer INDEX
        int pointerId = event.getPointerId(pointerIndex);

        // get masked (not specific to a pointer) action
        int maskedAction = event.getActionMasked();

        switch (maskedAction) {

            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                point = new Point(event.getX(), event.getY());
                point.SetIndex(pointerIndex);
                points.put(pointerId, point);
                if ((points.size() %3) == 0) {
                    ArrayList<Point> p = new ArrayList<>(points.values());
                    Triangle t = new Triangle(p.get(0), p.get(1), p.get(2));
                    triangles.add(t);
                    points = new HashMap<>();
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                for (int i = 0; i < triangles.size(); i++) {
                    if(triangles.get(i).p1.INDEX == pointerIndex || triangles.get(i).p2.INDEX == pointerIndex || triangles.get(i).p3.INDEX == pointerIndex) {
                        //triangles.remove(i);
                    }
                }
                if (points.containsKey(pointerId)) {
                    points.remove(pointerId);
                }
                break;

            case MotionEvent.ACTION_MOVE:
                int pointerCount = event.getPointerCount();
                for (int i = 0; i < pointerCount; i++) {
                    for (Triangle triangle : triangles ) {
                        if(triangle.p1.INDEX == i) {
                            triangle.p1.SetPositions(event.getX(i), event.getY(i));
                        } else if(triangle.p2.INDEX == i) {
                            triangle.p2.SetPositions(event.getX(i), event.getY(i));
                        } else if(triangle.p3.INDEX == i) {
                            triangle.p3.SetPositions(event.getX(i), event.getY(i));
                        }
                    }
                }

                break;
        }

        this.invalidate();

        return true;
    }
}