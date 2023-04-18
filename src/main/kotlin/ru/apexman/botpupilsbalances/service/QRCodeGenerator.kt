package ru.apexman.botpupilsbalances.service

import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.client.j2se.MatrixToImageConfig
import com.google.zxing.client.j2se.MatrixToImageWriter
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter
import org.springframework.stereotype.Service
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.charset.StandardCharsets
import javax.imageio.ImageIO


@Service
class QRCodeGenerator {

    fun generate(text: String, background: Int): ByteArray {
        val barcodeWriter = QRCodeWriter()
        val properties = mutableMapOf(Pair(EncodeHintType.CHARACTER_SET, StandardCharsets.UTF_8))
        val bitMatrix: BitMatrix = barcodeWriter.encode(text, BarcodeFormat.QR_CODE, 200, 200, properties)
        val conf = MatrixToImageConfig(MatrixToImageConfig.BLACK, background)
        val qrcode = MatrixToImageWriter.toBufferedImage(bitMatrix, conf)
        ImageIO.write(qrcode, "png", File("out.png"))
        return toByteArray(qrcode)
    }

    private fun toByteArray(image: BufferedImage): ByteArray {
        val byteArrayOutputStream = ByteArrayOutputStream()
        ImageIO.write(image, "png", byteArrayOutputStream)
        return byteArrayOutputStream.toByteArray()
    }

    companion object {
        const val GREEN: Int = -0xff00ff
        const val RED: Int = -0x00ff00
    }

}