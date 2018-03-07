# BezierBubbleView

## 效果图.

![](https://raw.githubusercontent.com/houtrry/BezierBubbleView/master/img/bubble.gif)
![](https://raw.githubusercontent.com/houtrry/BezierBubbleView/master/img/gif1.gif)

## 实现
### 控件的状态拆分
* 空闲状态
* 拖动控件, 随手指移动, 有贝塞尔曲线
* 拖动控件, 随手指移动, 无贝塞尔曲线
* 手指松开, 正在恢复初始化状态
* 手指松开, 正在消失
* 消失状态, 已经消失

### 分步实现
1. 画圆（Canvas#drawCircle）， 画文字(Canvas#drawText)
2. 拖动控件, 随手指移动: 岁手指移动，获取手指当前的位置，然后在该位置上画圆，画文字
3. 贝塞尔曲线：可以参考[QQ 未读消息的拖拽动态效果是如何实现的？](https://www.zhihu.com/question/37231903)的分析，通过两条贝塞尔曲线和两个圆来实现该View的效果。贝塞尔曲线部分，可以通过Path.quadTo和Canvas#drawPath来实现。
4. 手指松开, 正在恢复初始化状态：通过属性动画，修改该控件当前的位置，实现手指松开后回到原位置的效果。至于最后的回弹，通过设置属性动画ObjectAnimator.setInterpolator(new OvershootInterpolator(3))来实现。
5. 手指松开, 正在消失：通过属性动画控制消失的进度，不同进度显示不同的图片，实现类似帧动画的效果。
6. 控件可以在父控件的任意位置显示：设置父控件android:clipChildren="false"属性。注意：要实现效果图中的可以在RecyclerView中随处移动，除了要设置RecyclerView的android:clipChildren="false"，还要设置RecyclerView的父控件android:clipChildren="false"。

## 参考
[QQ 未读消息的拖拽动态效果是如何实现的？](https://www.zhihu.com/question/37231903)