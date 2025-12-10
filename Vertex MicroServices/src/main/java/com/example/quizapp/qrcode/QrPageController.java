package com.example.quizapp.qrcode;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class QrPageController {
    //shows the QR generator page when the user visits.
    @GetMapping("/qr-generator")
    public String showQrPage() {
        return "qr-generator";
    }
}
