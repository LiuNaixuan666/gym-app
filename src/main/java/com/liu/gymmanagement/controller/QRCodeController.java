package com.liu.gymmanagement.controller;

import com.liu.gymmanagement.service.QRCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/qrcode")
public class QRCodeController {

    @Autowired
    private QRCodeService qrCodeService;

//    // 处理 Raspberry Pi 传来的二维码数据
//    @PostMapping("/scan")
//    public ResponseEntity<String> processQRCodeScan(@RequestParam String qrCodeData) {
//        return qrCodeService.handleQRCodeScan(qrCodeData);
//    }

    // 处理 Raspberry Pi 传来的二维码数据（JSON 格式）
    @PostMapping("/scan")
    public ResponseEntity<String> processQRCodeScan(@RequestBody Map<String, String> payload) {
        String qrCodeData = payload.get("qrCodeData");
        return qrCodeService.handleQRCodeScan(qrCodeData);
    }
}
