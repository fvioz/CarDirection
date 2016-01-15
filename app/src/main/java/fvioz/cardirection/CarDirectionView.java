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

    class Point {
        int ID;
        float X, Y;

        public Point(float X0, float Y0) {
            ID = -1000;
            X = X0;
            Y = Y0;
        }

        public void SetID(int ID0) {
            ID = ID0;
        }

        public void SetPositions(float X0, float Y0) {
            X = X0;
            Y = Y0;
        }
    }

    class Triangle {
        Point p1, p2, p3;

        public Triangle(Point p01, Point p02, Point p03) {
            p1 = p01;
            p2 = p02;
            p3 = p03;
        }

        public double Angle()
        {
            Point center = GetCenter();
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

        public double CircleRadius(){
            Point center = GetCenter();
            Point direction = GetDirection();

            double maxDistance = Math.sqrt(Math.pow((center.X - direction.X), 2) + Math.pow((center.Y - direction.Y), 2));
            return maxDistance + 50;
        }


        public Point GetCenter() {
            Point center1 = new Point((p1.X + p2.X) / 2, (p1.Y + p2.Y) / 2);
            Point center2 = new Point((p2.X + p3.X) / 2, (p2.Y + p3.Y) / 2);
            Point center3 = new Point((p3.X + p1.X) / 2, (p3.Y + p1.Y) / 2);

            float centerX = (center1.X + center2.X + center3.X) / 3;
            float centerY = (center1.Y + center2.Y + center3.Y) / 3;

            //float centerX = (p1.X + p2.X + p3.X) / 3;
            //float centerY = (p1.Y + p2.Y + p3.Y) / 3;

            return new Point(centerX, centerY);
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
        p.SetID(1);

        Triangle triangle = new Triangle(p, new Point(x - 70, y + 100), new Point(x + 70, y + 100));
        triangles.add(triangle);

    }

    @Override
    public void onDraw(Canvas canvas) {

        for (Triangle triangle : triangles) {

            // Variables
            Path path = new Path();
            double angle = triangle.Angle();
            double radius = triangle.CircleRadius();
            Point center = triangle.GetCenter();
            Point direction = triangle.GetDirection();

            // Arrow
            paint.setStrokeWidth(40f);
            paint.setColor(Color.rgb(68, 180, 73));
            canvas.drawLine(center.X, center.Y, direction.X, direction.Y, paint);

            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.rgb(68, 180, 73));
            //path.reset();
            //path.moveTo(direction.X, direction.Y + 50);
            //path.lineTo(direction.X - 35, direction.Y);
            //path.lineTo(direction.X + 35, direction.Y);
            //path.lineTo(direction.X, direction.Y + 50);
            //path.close();
            //canvas.drawPath(path, paint);

            // Circle
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.rgb(68, 180, 73));
            canvas.drawCircle(center.X, center.Y, (float) radius, paint);



            // Orientation
            paint.setStrokeWidth(3f);
            paint.setColor(Color.BLACK);
            canvas.drawText(String.valueOf(angle), 30, 30, paint);
            //canvas.drawText("Direction", triangle.direction.X, triangle.direction.Y, paint);

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

        // get pointer ID
        int pointerId = event.getPointerId(pointerIndex);

        // get masked (not specific to a pointer) action
        int maskedAction = event.getActionMasked();

        switch (maskedAction) {

            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                point = new Point(event.getX(), event.getY());
                point.SetID(pointerId);
                points.put(pointerId, point);
                if ((points.size() %3) == 0) {
                    ArrayList<Point> p = (ArrayList<Point>) points.values();
                    Triangle t = new Triangle(p.get(0), p.get(1), p.get(2));
                    triangles.add(t);
                    points = new HashMap<>();
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                for (Triangle triangle : triangles ) {
                    if(triangle.p1.ID == pointerId || triangle.p2.ID == pointerId || triangle.p3.ID == pointerId) {
                        //triangles.remove(triangle);
                    }
                }
                if (points.containsKey(pointerId)) {
                    points.remove(pointerId);
                }
                break;

            case MotionEvent.ACTION_MOVE:
                for (Triangle triangle : triangles ) {
                    if(triangle.p1.ID == pointerId) {
                        triangle.p1.SetPositions(event.getX(), event.getY());
                    } else if(triangle.p2.ID == pointerId) {
                        triangle.p2.SetPositions(event.getX(), event.getY());
                    } else if(triangle.p3.ID == pointerId) {
                        triangle.p3.SetPositions(event.getX(), event.getY());
                    }
                }
                break;
        }

        this.invalidate();

        return true;
    }

}