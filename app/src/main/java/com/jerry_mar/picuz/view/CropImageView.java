package com.jerry_mar.picuz.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.view.ViewCompat;
import android.support.v7.content.res.AppCompatResources;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;

import com.jerry_mar.picuz.R;
import com.jerry_mar.picuz.config.Shape;
import com.jerry_mar.picuz.utils.ImageDataSource;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CropImageView extends AppCompatImageView {

    private Shape[] shapes = {Shape.RECTANGLE, Shape.CIRCLE, Shape.CORNER};

    private int color = 0xA0000000;   //暗色
    private int borderColor = Color.WHITE;//焦点框的边框颜色
    private int border = 5;         //焦点边框的宽度（画笔宽度）
    private int width;         //焦点框的宽度
    private int height;        //焦点框的高度
    private int shapeIndex;    //默认焦点框的形状

    private Shape shape = shapes[shapeIndex];
    private Paint borderPaint = new Paint();
    private Path borderPath = new Path();
    private RectF borderRange = new RectF();
    private RectF tb = new RectF();
    private RectF rb = new RectF();
    private RectF bb = new RectF();
    private RectF lb = new RectF();

    private static final float MAX_SCALE = 5.0f;  //最大缩放比，图片缩放后的大小与中间选中区域的比值
    private static final int INVALID = -1;   // 初始化
    private static final int INIT = 0;   // 初始化
    private static final int DRAG = 1;   // 拖拽
    private static final int ZOOM = 2;   // 缩放
    private static final int ROTATE = 3; // 旋转
    private static final int ZOOM_OR_ROTATE = 4;  // 缩放或旋转
    private static final int CHANGE_BORDER_HEIGHT = 5;
    private static final int CHANGE_BORDER_WIDTH = 6;

    private int imageWidth;
    private int imageHeight;
    private int changeImageWidth;
    private int changeImageHeight;
    private Matrix matrix = new Matrix();      //图片变换的matrix
    private Matrix savedMatrix = new Matrix(); //开始变幻的时候，图片的matrix
    private PointF pA = new PointF();          //第一个手指按下点的坐标
    private PointF pB = new PointF();          //第二个手指按下点的坐标
    private PointF op = new PointF();    //两个手指的中间点
    private PointF doubleClickPos = new PointF();  //双击图片的时候，双击点的坐标
    private PointF center = new PointF();  //中间View的中间点
    private int mode = INVALID;            //无效模式
    private long doubleClickTime = 0;   //第二次双击的时间
    private double rotation = 0;        //手指旋转的角度，不是90的整数倍，可能为任意值，需要转换成level
    private float oldDist = 1;          //双指第一次的距离
    private int rotate = 0;     //旋转的角度，90的整数倍
    private float maxScale;//程序根据不同图片的大小，动态得到的最大缩放比
    private float maxWidth;
    private float maxHeight;
    private boolean cs;
    private static Handler mHandler = new InnerHandler();

    public CropImageView(Context context) {
        this(context, null);
    }

    public CropImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CropImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CropImageView);
        color = a.getColor(R.styleable.CropImageView_color, color);
        borderColor = a.getColor(R.styleable.CropImageView_borderColor, borderColor);
        border = a.getDimensionPixelSize(R.styleable.CropImageView_border, border);
        width = a.getDimensionPixelSize(R.styleable.CropImageView_width, width);
        height = a.getDimensionPixelSize(R.styleable.CropImageView_height, height);
        shapeIndex = a.getInteger(R.styleable.CropImageView_shape, shapeIndex);
        cs = a.getBoolean(R.styleable.CropImageView_changeStyle, false);
        shape = shapes[shapeIndex];
        a.recycle();
        setScaleType(ScaleType.MATRIX);
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        setImageDrawable(new BitmapDrawable(getResources(), bm));
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        super.setImageDrawable(drawable);
        layout(drawable, true);
    }

    @Override
    public void setImageResource(int resId) {
        Drawable drawable = AppCompatResources.getDrawable(getContext(), resId);
        setImageDrawable(drawable);
    }

    @Override
    public void setImageURI(Uri uri) {
        ImageDataSource.load(uri.toString(), this, "operator:");
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mode = INIT;
        layout(getDrawable(), true);
    }

    private void layout(Drawable drawable, boolean draw) {
        float viewWidth = getWidth();
        float viewHeight = getHeight();
        if (drawable == null || mode == INVALID) return;
        imageWidth = changeImageWidth = drawable.getIntrinsicWidth();
        imageHeight = changeImageHeight = drawable.getIntrinsicHeight();
        if (maxWidth == 0 || maxHeight == 0) {
            maxWidth = viewWidth * 0.8F;
            maxHeight = viewHeight * 0.8F;
            if (width != 0 && height != 0) {
                float scale = Math.max(width / maxWidth, height / maxHeight);
                if (scale > 1) {
                    width /= scale;
                    height /= scale;
                }
                if ((width / viewWidth) > (height / viewHeight)) {
                    maxHeight = maxWidth * height / width;
                } else {
                    maxWidth = maxHeight * width / height;
                }
            } else {
                if (imageWidth < imageHeight) {
                    height = (int) (maxHeight);
                    width = (int) (imageWidth * 1.0F / imageHeight * height);
                } else {
                    width = (int) (maxWidth);
                    height = (int) (imageHeight * 1.0F / imageWidth * width);
                }
            }
        }
        if (shape == Shape.CIRCLE) {
            width = height = Math.min(width, height);
        }
        float cx = viewWidth / 2;
        float cy = viewHeight / 2;
        center.set(cx, cy);
        borderRange.left = center.x - width / 2;
        borderRange.right = center.x + width / 2;
        borderRange.top = center.y - height / 2;
        borderRange.bottom = center.y + height / 2;

        cx = (borderRange.right + borderRange.left) / 2;
        cy = (borderRange.bottom + borderRange.top) / 2;
        lb.set(borderRange.left - 50, cy - 50, borderRange.left + 50, cy + 50);
        tb.set(cx - 50, borderRange.top - 50, cx + 50, borderRange.top + 50);
        rb.set(borderRange.right - 50, cy - 50, borderRange.right + 50, cy + 50);
        bb.set(cx - 50, borderRange.bottom - 50, cx + 50, borderRange.bottom + 50);

        //适配焦点框的缩放比例（图片的最小边不小于焦点框的最小边）
        float fitScale = getScale(imageWidth, imageHeight, width, height, true);
        maxScale = fitScale * MAX_SCALE;
        if (draw) {
            matrix = getImageMatrix();
            //适配显示图片的ImageView的缩放比例（图片至少有一边是铺满屏幕的显示的情形）
            float scale = getScale(imageWidth, imageHeight, (int) viewWidth, (int) viewHeight, false);
            matrix.setScale(scale, scale, imageWidth / 2, imageHeight / 2);
            float[] matrixValue = new float[9];
            matrix.getValues(matrixValue); //获取缩放后的mImageMatrix的值
            float transX = center.x - (matrixValue[2] + imageWidth * matrixValue[0] / 2);  //X轴方向的位移
            float transY = center.y - (matrixValue[5] + imageHeight * matrixValue[4] / 2); //Y轴方向的位移
            matrix.postTranslate(transX, transY);
            setImageMatrix(matrix);
        }
        invalidate();
    }

    /** 计算边界缩放比例 isMinScale 是否最小比例，true 最小缩放比例， false 最大缩放比例 */
    private float getScale(int bitmapWidth, int bitmapHeight, int minWidth, int minHeight, boolean isMinScale) {
        float scale;
        float scaleX = (float) minWidth / bitmapWidth;
        float scaleY = (float) minHeight / bitmapHeight;
        if (isMinScale) {
            scale = scaleX > scaleY ? scaleX : scaleY;
        } else {
            scale = scaleX < scaleY ? scaleX : scaleY;
        }
        return scale;
    }

    /** 绘制焦点框 */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        switch (shape) {
            case RECTANGLE: {
                borderPath.addRect(borderRange, Path.Direction.CCW);
            }
            break;
            case CIRCLE: {
                float radius = (borderRange.right - borderRange.left) / 2;
                borderPath.addCircle(center.x, center.y, radius, Path.Direction.CCW);
            }
            break;
            case CORNER: {
                int rx = (int) ((borderRange.right - borderRange.left) / 10);
                int ry = (int) ((borderRange.bottom - borderRange.top) / 10);
                int r = Math.min(rx, ry);
                borderPath.addRoundRect(borderRange, r, r, Path.Direction.CCW);
            }
            break;
        }
        canvas.save();
        canvas.clipRect(0, 0, getWidth(), getHeight());
        canvas.clipPath(borderPath, Region.Op.DIFFERENCE);
        canvas.drawColor(color);
        canvas.restore();

        borderPaint.setColor(borderColor);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(border);
        borderPaint.setAntiAlias(true);
        canvas.drawPath(borderPath, borderPaint);
        borderPaint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(lb.right - 50, lb.bottom - 50, 20, borderPaint);
        canvas.drawCircle(tb.right - 50, tb.bottom - 50, 20, borderPaint);
        canvas.drawCircle(rb.right - 50, rb.bottom - 50, 20, borderPaint);
        canvas.drawCircle(bb.right - 50, bb.bottom - 50, 20, borderPaint);
        borderPath.reset();
    }

    private float opx, opy;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (null == getDrawable())
            return super.onTouchEvent(event);

        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:  //第一个点按下
                savedMatrix.set(matrix);   //以后每次需要变换的时候，以现在的状态为基础进行变换
                pA.set(event.getX(), event.getY());
                pB.set(event.getX(), event.getY());
                mode = DRAG;
                break;
            case MotionEvent.ACTION_POINTER_DOWN:  //第二个点按下
                if (event.getActionIndex() > 1) break;
                pA.set(event.getX(0), event.getY(0));
                pB.set(event.getX(1), event.getY(1));
                op.set((pA.x + pB.x) / 2, (pA.y + pB.y) / 2);
                oldDist = spacing(pA, pB);
                opx = op.x - width;
                opy = op.y - height;
                if ((lb.contains(pA.x, pA.y) && rb.contains(pB.x, pB.y)) ||
                        rb.contains(pA.x, pA.y) && lb.contains(pB.x, pB.y)) {
                    mode = CHANGE_BORDER_WIDTH;
                } else if ((tb.contains(pA.x, pA.y) && bb.contains(pB.x, pB.y)) ||
                        (bb.contains(pA.x, pA.y) && tb.contains(pB.x, pB.y))) {
                    mode = CHANGE_BORDER_HEIGHT;
                } else {
                    savedMatrix.set(matrix);  //以后每次需要变换的时候，以现在的状态为基础进行变换
                    if (oldDist > 10f) mode = ZOOM_OR_ROTATE;//两点之间的距离大于10才有效
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (mode == CHANGE_BORDER_HEIGHT) {
                    float newDist = spacing(event.getX(0), event.getY(0),
                            event.getX(1), event.getY(1));
                    float h = newDist / oldDist * (op.y - opy);
                    this.height = (int) Math.min(h, maxHeight);
                    if (cs) {
                        float w = newDist / oldDist * (op.x - opx);
                        this.width = (int) Math.min(w, maxWidth);
                    }
                    layout(getDrawable(), false);
                    fixScale();
                    fixTranslation();
                    setImageMatrix(matrix);
                    break;
                }
                if (mode == CHANGE_BORDER_WIDTH) {
                    float newDist = spacing(event.getX(0), event.getY(0),
                            event.getX(1), event.getY(1));
                    float w = newDist / oldDist * (op.x - opx);
                    this.width = (int) Math.min(w, maxWidth);
                    if (cs) {
                        float h = newDist / oldDist * (op.y - opy);
                        this.height = (int) Math.min(h, maxHeight);
                    }
                    layout(getDrawable(), false);
                    fixScale();
                    fixTranslation();
                    setImageMatrix(matrix);
                    break;
                }
                if (mode == ZOOM_OR_ROTATE) {
                    PointF pC = new PointF(event.getX(1) - event.getX(0) + pA.x, event.getY(1) - event.getY(0) + pA.y);
                    double a = spacing(pB.x, pB.y, pC.x, pC.y);
                    double b = spacing(pA.x, pA.y, pC.x, pC.y);
                    double c = spacing(pA.x, pA.y, pB.x, pB.y);
                    if (a >= 10) {
                        double cosB = (a * a + c * c - b * b) / (2 * a * c);
                        double angleB = Math.acos(cosB);
                        double PID4 = Math.PI / 4;
                        //旋转时，默认角度在 45 - 135 度之间
                        if (angleB > PID4 && angleB < 3 * PID4) mode = ROTATE;
                        else mode = ZOOM;
                    }
                }
                if (mode == DRAG) {
                    matrix.set(savedMatrix);
                    matrix.postTranslate(event.getX() - pA.x, event.getY() - pA.y);
                    fixTranslation();
                    setImageMatrix(matrix);
                } else if (mode == ZOOM) {
                    float newDist = spacing(event.getX(0), event.getY(0), event.getX(1), event.getY(1));
                    if (newDist > 10f) {
                        matrix.set(savedMatrix);
                        // 这里之所以用 maxPostScale 矫正一下，主要是防止缩放到最大时，继续缩放图片会产生位移
                        float tScale = Math.min(newDist / oldDist, maxPostScale());
                        if (tScale != 0) {
                            matrix.postScale(tScale, tScale, op.x, op.y);
                            fixScale();
                            fixTranslation();
                            setImageMatrix(matrix);
                        }
                    }
                } else if (mode == ROTATE) {
                    PointF pC = new PointF(event.getX(1) - event.getX(0) + pA.x, event.getY(1) - event.getY(0) + pA.y);
                    double a = spacing(pB.x, pB.y, pC.x, pC.y);
                    double b = spacing(pA.x, pA.y, pC.x, pC.y);
                    double c = spacing(pA.x, pA.y, pB.x, pB.y);
                    if (b > 10) {
                        double cosA = (b * b + c * c - a * a) / (2 * b * c);
                        double angleA = Math.acos(cosA);
                        double ta = pB.y - pA.y;
                        double tb = pA.x - pB.x;
                        double tc = pB.x * pA.y - pA.x * pB.y;
                        double td = ta * pC.x + tb * pC.y + tc;
                        if (td > 0) {
                            angleA = 2 * Math.PI - angleA;
                        }
                        rotation = angleA;
                        matrix.set(savedMatrix);
                        matrix.postRotate((float) (rotation * 180 / Math.PI), op.x, op.y);
                        setImageMatrix(matrix);
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                if (mode == DRAG) {
                    if (spacing(pA, pB) < 50) {
                        long now = System.currentTimeMillis();
                        if (now - doubleClickTime < 500 && spacing(pA, doubleClickPos) < 50) {
                            doubleClick(pA.x, pA.y);
                            now = 0;
                        }
                        doubleClickPos.set(pA);
                        doubleClickTime = now;
                    }
                } else if (mode == ROTATE) {
                    int rotateLevel = (int) Math.floor((rotation + Math.PI / 4) / (Math.PI / 2));
                    if (rotateLevel == 4) rotateLevel = 0;
                    matrix.set(savedMatrix);
                    matrix.postRotate(90 * rotateLevel, op.x, op.y);
                    if (rotateLevel == 1 || rotateLevel == 3) {
                        int tmp = changeImageWidth;
                        changeImageWidth = changeImageHeight;
                        changeImageHeight = tmp;
                    }
                    fixScale();
                    fixTranslation();
                    setImageMatrix(matrix);
                    rotate += rotateLevel;
                }
                mode = INIT;
                break;
        }
        //解决部分机型无法拖动的问题
        ViewCompat.postInvalidateOnAnimation(this);
        return true;
    }

    /** 修正图片的缩放比 */
    private void fixScale() {
        float imageMatrixValues[] = new float[9];
        matrix.getValues(imageMatrixValues);
        float currentScale = Math.abs(imageMatrixValues[0]) + Math.abs(imageMatrixValues[1]);
        float minScale = getScale(changeImageWidth, changeImageHeight, width, height, true);
        maxScale = minScale * MAX_SCALE;

        //保证图片最小是占满中间的焦点空间
        if (currentScale < minScale) {
            float scale = minScale / currentScale;
            matrix.postScale(scale, scale);
        } else if (currentScale > maxScale) {
            float scale = maxScale / currentScale;
            matrix.postScale(scale, scale);
        }
    }

    /** 修正图片的位移 */
    private void fixTranslation() {
        RectF imageRect = new RectF(0, 0, imageWidth, imageHeight);
        matrix.mapRect(imageRect);  //获取当前图片（缩放以后的）相对于当前控件的位置区域，超过控件的上边缘或左边缘为负
        float deltaX = 0, deltaY = 0;
        if (imageRect.left > borderRange.left) {
            deltaX = -imageRect.left + borderRange.left;
        } else if (imageRect.right < borderRange.right) {
            deltaX = -imageRect.right + borderRange.right;
        }
        if (imageRect.top > borderRange.top) {
            deltaY = -imageRect.top + borderRange.top;
        } else if (imageRect.bottom < borderRange.bottom) {
            deltaY = -imageRect.bottom + borderRange.bottom;
        }
        matrix.postTranslate(deltaX, deltaY);
    }

    /** 获取当前图片允许的最大缩放比 */
    private float maxPostScale() {
        float imageMatrixValues[] = new float[9];
        matrix.getValues(imageMatrixValues);
        float curScale = Math.abs(imageMatrixValues[0]) + Math.abs(imageMatrixValues[1]);
        return maxScale / curScale;
    }

    /** 计算两点之间的距离 */
    private float spacing(float x1, float y1, float x2, float y2) {
        float x = x1 - x2;
        float y = y1 - y2;
        return (float) Math.sqrt(x * x + y * y);
    }

    /** 计算两点之间的距离 */
    private float spacing(PointF pA, PointF pB) {
        return spacing(pA.x, pA.y, pB.x, pB.y);
    }

    /** 双击触发的方法 */
    private void doubleClick(float x, float y) {
        float p[] = new float[9];
        matrix.getValues(p);
        float curScale = Math.abs(p[0]) + Math.abs(p[1]);
        float minScale = getScale(changeImageWidth, changeImageHeight, width, height, true);
        if (curScale < maxScale) {
            //每次双击的时候，缩放加 minScale
            float toScale = Math.min(curScale + minScale, maxScale) / curScale;
            matrix.postScale(toScale, toScale, x, y);
        } else {
            float toScale = minScale / curScale;
            matrix.postScale(toScale, toScale, x, y);
            fixTranslation();
        }
        setImageMatrix(matrix);
    }

    /**
     * @param expectWidth     期望的宽度
     * @param exceptHeight    期望的高度
     * @return 裁剪后的Bitmap
     */
    public Bitmap getCropBitmap(int expectWidth, int exceptHeight, int border, int color) {
        if (expectWidth <= 0 || exceptHeight < 0) return null;
        Bitmap srcBitmap = ((BitmapDrawable) getDrawable()).getBitmap();
        srcBitmap = rotate(srcBitmap, rotate * 90);  //最好用level，因为角度可能不是90的整数
        return makeCropBitmap(srcBitmap, borderRange, getImageMatrixRect(), expectWidth, exceptHeight, border, color);
    }

    /**
     * @param bitmap  要旋转的图片
     * @param degrees 选择的角度（单位 度）
     * @return 旋转后的Bitmap
     */
    public Bitmap rotate(Bitmap bitmap, int degrees) {
        if (degrees != 0 && bitmap != null) {
            Matrix matrix = new Matrix();
            matrix.setRotate(degrees, (float) bitmap.getWidth() / 2, (float) bitmap.getHeight() / 2);
            try {
                Bitmap rotateBitmap = Bitmap.createBitmap(bitmap, 0, 0,
                        bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                if (bitmap != rotateBitmap) {
                    return rotateBitmap;
                }
            } catch (OutOfMemoryError ex) {
                ex.printStackTrace();
            }
        }
        return bitmap;
    }

    /**
     * @return 获取当前图片显示的矩形区域
     */
    private RectF getImageMatrixRect() {
        RectF rectF = new RectF();
        rectF.set(0, 0, getDrawable().getIntrinsicWidth(), getDrawable().getIntrinsicHeight());
        matrix.mapRect(rectF);
        return rectF;
    }

    /**
     * @param bitmap          需要裁剪的图片
     * @param focusRect       中间需要裁剪的矩形区域
     * @param imageMatrixRect 当前图片在屏幕上的显示矩形区域
     * @param expectWidth     希望获得的图片宽度，如果图片宽度不足时，拉伸图片
     * @param exceptHeight    希望获得的图片高度，如果图片高度不足时，拉伸图片
     * @return 裁剪后的图片的Bitmap
     */
    private Bitmap makeCropBitmap(Bitmap bitmap, RectF focusRect, RectF imageMatrixRect,
                                  int expectWidth, int exceptHeight, int border, int color) {
        float scale = imageMatrixRect.width() / bitmap.getWidth();
        int left = (int) ((focusRect.left - imageMatrixRect.left) / scale);
        int top = (int) ((focusRect.top - imageMatrixRect.top) / scale);
        int width = (int) (focusRect.width() / scale);
        int height = (int) (focusRect.height() / scale);

        if (left < 0) left = 0;
        if (top < 0) top = 0;
        if (left + width > bitmap.getWidth()) width = bitmap.getWidth() - left;
        if (top + height > bitmap.getHeight()) height = bitmap.getHeight() - top;

        try {
            Bitmap result = Bitmap.createBitmap(width, height, bitmap.getConfig());
            Canvas canvas = new Canvas(result);
            BitmapShader shader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
            Matrix matrix = new Matrix();
            matrix.setTranslate(-left, -top);
            shader.setLocalMatrix(matrix);
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
//            paint.setStrokeWidth(border);
//            paint.setStyle(Paint.Style.STROKE);
//            paint.setColor(color);
            paint.setShader(shader);

            switch (shape) {
                case CORNER: {
                    int rx = width / 10;
                    int ry = height / 10;
                    int r = Math.min(rx, ry);
                    canvas.drawRoundRect(new RectF(0, 0 , width, height), r, r, paint);
                    bitmap = result;
                }
                break;
                case CIRCLE: {
                    expectWidth = exceptHeight = Math.min(expectWidth, exceptHeight);
                    int length = Math.min(width, height);
                    int radius = length / 2;
                    canvas.drawCircle(radius, radius, radius, paint);
                    bitmap = result;
                }
                break;
                default:
                    canvas.drawRect(0, 0, width, height, paint);
                    bitmap = result;
            }
            if (expectWidth != width || exceptHeight != height) {
                bitmap = Bitmap.createScaledBitmap(bitmap, expectWidth, exceptHeight, true);
            }
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    /**
     * @param folder          希望保存的文件夹
     * @param expectWidth     希望保存的图片宽度
     * @param exceptHeight    希望保存的图片高度
     */
    public void save(File folder, int expectWidth, int exceptHeight, int border, int color) {
        if (expectWidth == 0 || exceptHeight == 0) {
            float w = getDrawable().getIntrinsicWidth();
            float h = getDrawable().getIntrinsicHeight();
            w = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                    w, getResources().getDisplayMetrics());
            h = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                    h, getResources().getDisplayMetrics());
            if (width / w > height / h) {
                exceptHeight = (int) (w / width * height);
                expectWidth = (int) w;
            } else {
                expectWidth = (int) (h / height * width);
                exceptHeight = (int) h;
            }
        }
        final Bitmap croppedImage = getCropBitmap(expectWidth, exceptHeight, border, color);
        Bitmap.CompressFormat outputFormat = Bitmap.CompressFormat.JPEG;
        if (shape != Shape.RECTANGLE)
            outputFormat = Bitmap.CompressFormat.PNG;
        File saveFile = createFile(folder, "IMG_", ".jpg");
        final Bitmap.CompressFormat finalOutputFormat = outputFormat;
        final File finalSaveFile = saveFile;
        new Thread() {
            @Override
            public void run() {
                saveOutput(croppedImage, finalOutputFormat, finalSaveFile);
            }
        }.start();
    }

    /** 根据系统时间、前缀、后缀产生一个文件 */
    private File createFile(File folder, String prefix, String suffix) {
        if (!folder.exists() || !folder.isDirectory()) folder.mkdirs();
        try {
            File nomedia = new File(folder, ".nomedia");  //在当前文件夹底下创建一个 .nomedia 文件
            if (!nomedia.exists()) nomedia.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA);
        String filename = prefix + dateFormat.format(new Date(System.currentTimeMillis())) + suffix;
        return new File(folder, filename);
    }

    /** 将图片保存在本地 */
    private void saveOutput(Bitmap croppedImage, Bitmap.CompressFormat outputFormat, File saveFile) {
        OutputStream outputStream = null;
        try {
            outputStream = getContext().getContentResolver().openOutputStream(Uri.fromFile(saveFile));
            if (outputStream != null) croppedImage.compress(outputFormat, 100, outputStream);
        } catch (IOException ex) {
            saveFile.delete();
            saveFile = null;
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        Message.obtain(mHandler, 0, saveFile).sendToTarget();
        croppedImage.recycle();
    }

    public void changeStyle(boolean b) {
        this.cs = b;
    }

    private static class InnerHandler extends Handler {
        public InnerHandler() {
            super(Looper.getMainLooper());
        }

        @Override
        public void handleMessage(Message msg) {
            File saveFile = (File) msg.obj;
            c.onFinish(saveFile);
        }
    }

    /** 图片保存完成的监听 */
    private static Callback c;

    public interface Callback {
        void onFinish(File file);
    }

    public void setCallback(Callback c) {
        this.c = c;
    }

    public void setWidth(int width) {
        this.width = width;
        layout(getDrawable(), false);
    }

    public void setHeight(int height) {
        this.height = height;
        layout(getDrawable(), false);
    }

    public void changeShape(Shape shape) {
        this.shape = shape;
        layout(getDrawable(), false);
    }

    public void changeShape(int index) {
        this.shape = shapes[index];
        layout(getDrawable(), false);
    }
}