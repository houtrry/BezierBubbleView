package com.houtrry.bezierbubbleview;

/**
 * @author houtrry
 * @version $Rev$
 * @time 2017/9/21 14:00
 * @desc 气泡的状态
 * @updateAuthor $Author$
 * @updateDate $Date$
 * @updateDesc $TODO$
 */

public enum BezierBubbleStatus {
    STATUS_DISMISSED,//消失状态, 已经消失
    STATUS_CONNECT,//拖动, 随手指移动, 有贝塞尔曲线
    STATUS_DRAG,//拖动, 随手指移动, 无贝塞尔曲线

    STATUS_RECOVER,//手指松开, 正在恢复初始化状态
    STATUS_DISMISSING,//手指松开, 正在消失

    STATUS_IDLE,//空闲状态
}
