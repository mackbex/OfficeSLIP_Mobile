package com.officeslip.Structure

class Shape
{
    companion object {
        const val DRAW_RECT = 4433
        const val DRAW_PEN = 4434
        const val DRAW_CIRCLE = 4435
        const val DRAW_MEMO = 4436
    }

    var shapeType:Int = DRAW_RECT
    var tag:String? = null
    var leftPoint:Float = 0f
    var topPoint:Float = 0f
    var rightPoint:Float = 0f
    var bottomPoint:Float = 0f
    var alpha:Float = 1f
    var lineColor:String? = null
    var bgColor:String? = null
    var text:String? = null
    var italic = "0"
    var bold = "0"
    var weight:Float = 0f
    var width = 0f
    var height = 0f
    var leftGap:Float = 0f
    var topGap:Float = 0f
    var backFlag = "0"
    var fontSize = 0
    var fontColor:String? = null


}