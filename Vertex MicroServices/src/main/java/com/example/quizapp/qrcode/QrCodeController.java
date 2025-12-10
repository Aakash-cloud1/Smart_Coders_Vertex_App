package com.example.quizapp.qrcode;

import com.google.zxing.WriterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

@RestController
public class QrCodeController {

    private static final Logger log = LoggerFactory.getLogger(QrCodeController.class);

    private final QrCodeService qrCodeService;
    //injects the service that handles actual qr code creation
    @Autowired
    public QrCodeController(QrCodeService qrCodeService) {
        this.qrCodeService = qrCodeService;
    }
    //handles the post request that creates a PNG QR code for a given URL
    @PostMapping(value = "/generate", consumes = "application/x-www-form-urlencoded")
    public ResponseEntity<byte[]> generateQRCode(@ModelAttribute QrRequest request) {
        try {
            log.info("Generating QR for URL: {}", request.getUrl());
            //to generate raw PNG image bytes
            byte[] pngData = qrCodeService.generateQrCodeImage(request.getUrl(), request.getSize());
            //tells browser this is a PNG image to diaplay inline
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_PNG);
            headers.set("Content-Disposition", "inline; filename=\"qrcode.png\"");

            return ResponseEntity.ok().headers(headers).body(pngData);
        } catch (IllegalArgumentException | WriterException e) {
            log.error("Invalid input", e);
            return ResponseEntity.badRequest().build();//when something goes wrong with input
        } catch (Exception e) {
            log.error("Exception while generating QR code", e);
            return ResponseEntity.internalServerError().build();//any unexpected failure
        }
    }
}
