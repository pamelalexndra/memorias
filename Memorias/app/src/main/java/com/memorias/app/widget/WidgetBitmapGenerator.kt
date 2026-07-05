package com.memorias.app.widget

import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.provider.MediaStore
import android.util.Size
import com.memorias.app.domain.model.Favorite
import kotlin.math.ceil

class WidgetBitmapGenerator(private val context: Context) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    private val bgColor = android.graphics.Color.parseColor("#1F1A14")
    private val gapPx = 4

    fun generate(
        favorites: List<Favorite>,
        widthPx: Int = DEFAULT_WIDTH,
        heightPx: Int = DEFAULT_HEIGHT,
        ): Bitmap? {
        val items = favorites.take(MAX_PHOTOS)
        if (items.isEmpty()) return null
        val (cols, rows) = gridLayout(items.size)
        val cellW = (widthPx - gapPx * (cols + 1)) / cols
        val cellH = (heightPx - gapPx * (rows + 1)) / rows

        val result = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        canvas.drawColor(bgColor)

        var loaded = 0
        items.forEachIndexed { index, fav ->
            val thumbnail = loadThumbnail(fav.mediaStoreId, minOf(cellW, cellH))
                ?: return@forEachIndexed

            val col = index % cols
            val row = index / cols
            val left   = gapPx + col * (cellW + gapPx)
            val top    = gapPx + row * (cellH + gapPx)

            val dst = RectF(
                left.toFloat(),
                top.toFloat(),
                (left + cellW).toFloat(),
                (top + cellH).toFloat(),
                )
            val src = centerCropRect(thumbnail.width, thumbnail.height, cellW, cellH)
            canvas.drawBitmap(thumbnail, src, dst, paint)
            thumbnail.recycle()
            loaded++

        }

        if (loaded == 0) {

            result.recycle()

            return null

        }

        return result

    }
    private fun loadThumbnail(mediaStoreId: Long, sizePx: Int): Bitmap? = runCatching {
        val uri = ContentUris.withAppendedId(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            mediaStoreId,
            )
        context.contentResolver.loadThumbnail(uri, Size(sizePx, sizePx), null)

    }.getOrNull()

    private fun centerCropRect(srcW: Int, srcH: Int, dstW: Int, dstH: Int): Rect {
        val scale = maxOf(dstW.toFloat() / srcW, dstH.toFloat() / srcH)
        val scaledW = (srcW * scale).toInt()
        val scaledH = (srcH * scale).toInt()
        val offsetX = (scaledW - dstW) / 2
        val offsetY = (scaledH - dstH) / 2
        val invScale = 1f / scale

        return Rect(
            (offsetX * invScale).toInt(),
            (offsetY * invScale).toInt(),
            ((offsetX + dstW) * invScale).toInt(),
            ((offsetY + dstH) * invScale).toInt(),
            )
    }
    private fun gridLayout(count: Int): Pair<Int, Int> {
        val cols = when {
            count <= 1 -> 1
            count <= 2 -> 2
            else       -> 3
        }
        val rows = ceil(count.toDouble() / cols).toInt()
        return cols to rows
    }
    companion object {
        const val MAX_PHOTOS    = 6
        const val DEFAULT_WIDTH  = 630
        const val DEFAULT_HEIGHT = 360

    }

}