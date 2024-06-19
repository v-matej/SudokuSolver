package com.example.sudokusolver


import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import org.opencv.utils.Converters

object SudokuDetectionHelper {

    fun detectSudoku(originalImage: Mat): Mat {
        val grayImage = Mat()
        val blurredImage = Mat()
        val thresholdedImage = Mat()

        Imgproc.cvtColor(originalImage, grayImage, Imgproc.COLOR_BGR2GRAY)

        Imgproc.GaussianBlur(grayImage, blurredImage, Size(5.0, 5.0), 0.0)

        Imgproc.adaptiveThreshold(blurredImage, thresholdedImage, 255.0, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY_INV, 11, 2.0)

        val contours = ArrayList<MatOfPoint>()
        val hierarchy = Mat()
        Imgproc.findContours(thresholdedImage, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE)

        var largestContour: MatOfPoint? = null
        var maxArea = 0.0
        for (contour in contours) {
            val area = Imgproc.contourArea(contour)
            if (area > maxArea) {
                maxArea = area
                largestContour = contour
            }
        }

        val epsilon = 0.02 * Imgproc.arcLength(MatOfPoint2f(*largestContour!!.toArray()), true)
        val approx = MatOfPoint2f()
        Imgproc.approxPolyDP(MatOfPoint2f(*largestContour.toArray()), approx, epsilon, true)

        val srcPoints = ArrayList<Point>()
        val approxPoints = approx.toList()
        srcPoints.add(approxPoints[0])
        srcPoints.add(approxPoints[1])
        srcPoints.add(approxPoints[3])
        srcPoints.add(approxPoints[2])

        val destPoints = ArrayList<Point>()
        destPoints.add(Point(400.0, 0.0))
        destPoints.add(Point(0.0, 0.0))
        destPoints.add(Point(400.0, 400.0))
        destPoints.add(Point(0.0, 400.0))

        val srcMat = Converters.vector_Point2f_to_Mat(srcPoints)
        val destMat = Converters.vector_Point2f_to_Mat(destPoints)
        val transformationMatrix = Imgproc.getPerspectiveTransform(srcMat, destMat)
        val warpedImage = Mat(400, 400, originalImage.type())
        Imgproc.warpPerspective(originalImage, warpedImage, transformationMatrix, Size(400.0, 400.0))

        return warpedImage
    }

    fun extractCells(croppedSudoku: Mat): List<Mat> {
        val cellSize = Size(400.0 / 9.0, 400.0 / 9.0)

        val cells = ArrayList<Mat>()
        for (i in 0 until 9) {
            for (j in 0 until 9) {
                val cellRect = Rect(j * cellSize.width.toInt(), i * cellSize.height.toInt(), cellSize.width.toInt(), cellSize.height.toInt())
                val cell = Mat(croppedSudoku, cellRect)

                val grayCell = Mat()
                Imgproc.cvtColor(cell, grayCell, Imgproc.COLOR_BGR2GRAY)

                val invertedCell = Mat()
                Core.bitwise_not(grayCell, invertedCell)

                val binaryCell = Mat()
                Imgproc.threshold(invertedCell, binaryCell, 0.0, 255.0, Imgproc.THRESH_BINARY + Imgproc.THRESH_OTSU)

                cells.add(binaryCell)
            }
        }

        return cells
    }
}
