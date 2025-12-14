package com.example.datacountwebmongo.controller;

import com.example.datacountwebmongo.dto.DataCounterGroupResult;
import com.example.datacountwebmongo.entity.BaseResponse;
import com.example.datacountwebmongo.service.DataCounterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/data-counter")
@RequiredArgsConstructor
@CrossOrigin("*")
public class DataCounterController {

    private final DataCounterService dataCounterService;

    @GetMapping("/all")
    public ResponseEntity<BaseResponse<List<DataCounterGroupResult>>> getAllCounts() {
        return ok("Tüm veri sayıları", dataCounterService.getAllCounts());
    }

    @GetMapping("/anayasa")
    public ResponseEntity<BaseResponse<DataCounterGroupResult>> getAnayasaCounts() {
        return ok("Anayasa Mahkemesi", dataCounterService.getAnayasaCounts());
    }

    @GetMapping("/bam")
    public ResponseEntity<BaseResponse<DataCounterGroupResult>> getBamCounts() {
        return ok("BAM", dataCounterService.getBamCounts());
    }

    @GetMapping("/brsa")
    public ResponseEntity<BaseResponse<DataCounterGroupResult>> getBrsaCounts() {
        return ok("BDDK", dataCounterService.getBrsaCounts());
    }

    @GetMapping("/ca")
    public ResponseEntity<BaseResponse<DataCounterGroupResult>> getCaCounts() {
        return ok("Rekabet Kurumu", dataCounterService.getCaCounts());
    }

    @GetMapping("/cbrt")
    public ResponseEntity<BaseResponse<DataCounterGroupResult>> getCbrtCounts() {
        return ok("TCMB", dataCounterService.getCbrtCounts());
    }

    @GetMapping("/danistay")
    public ResponseEntity<BaseResponse<DataCounterGroupResult>> getDanistayCounts() {
        return ok("Danıştay", dataCounterService.getDanistayCounts());
    }

    @GetMapping("/dispute")
    public ResponseEntity<BaseResponse<DataCounterGroupResult>> getDisputeCounts() {
        return ok("Uyuşmazlık Mahkemesi", dataCounterService.getDisputeCounts());
    }

    @GetMapping("/echr")
    public ResponseEntity<BaseResponse<DataCounterGroupResult>> getEchrCounts() {
        return ok("ECHR (AİHM)", dataCounterService.getEchrCounts());
    }

    @GetMapping("/emra")
    public ResponseEntity<BaseResponse<DataCounterGroupResult>> getEmraCounts() {
        return ok("EPDK", dataCounterService.getEmraCounts());
    }

    @GetMapping("/gib")
    public ResponseEntity<BaseResponse<DataCounterGroupResult>> getGibCounts() {
        return ok("GİB", dataCounterService.getGibCounts());
    }

    @GetMapping("/ipprsa")
    public ResponseEntity<BaseResponse<DataCounterGroupResult>> getIpprsaCounts() {
        return ok("SEDDK", dataCounterService.getIpprsaCounts());
    }

    @GetMapping("/kapd")
    public ResponseEntity<BaseResponse<DataCounterGroupResult>> getKapdCounts() {
        return ok("KAP", dataCounterService.getKapdCounts());
    }

    @GetMapping("/kik")
    public ResponseEntity<BaseResponse<DataCounterGroupResult>> getKikCounts() {
        return ok("KİK", dataCounterService.getKikCounts());
    }

    @GetMapping("/omi")
    public ResponseEntity<BaseResponse<DataCounterGroupResult>> getOmiCounts() {
        return ok("Ombudsman", dataCounterService.getOmiCounts());
    }

    @GetMapping("/rthc")
    public ResponseEntity<BaseResponse<DataCounterGroupResult>> getRtukCounts() {
        return ok("RTÜK", dataCounterService.getRtukCounts());
    }

    @GetMapping("/sayistay")
    public ResponseEntity<BaseResponse<DataCounterGroupResult>> getSayistayCounts() {
        return ok("Sayıştay", dataCounterService.getSayistayCounts());
    }

    @GetMapping("/seb")
    public ResponseEntity<BaseResponse<DataCounterGroupResult>> getSebCounts() {
        return ok("YSK", dataCounterService.getSebCounts());
    }

    @GetMapping("/spk")
    public ResponseEntity<BaseResponse<DataCounterGroupResult>> getSpkCounts() {
        return ok("SPK", dataCounterService.getSpkCounts());
    }

    @GetMapping("/tihre")
    public ResponseEntity<BaseResponse<DataCounterGroupResult>> getTihreCounts() {
        return ok("TİHEK", dataCounterService.getTihreCounts());
    }

    @GetMapping("/utbadb")
    public ResponseEntity<BaseResponse<DataCounterGroupResult>> getUtbadbCounts() {
        return ok("TBB", dataCounterService.getUtbadbCounts());
    }

    @GetMapping("/yargitay")
    public ResponseEntity<BaseResponse<DataCounterGroupResult>> getYargitayCounts() {
        return ok("Yargıtay", dataCounterService.getYargitayCounts());
    }

    @GetMapping("/yoktez")
    public ResponseEntity<BaseResponse<DataCounterGroupResult>> getYoktezCounts() {
        return ok("YÖK Tez", dataCounterService.getYoktezCounts());
    }

    // Helper method
    private <T> ResponseEntity<BaseResponse<T>> ok(String source, T data) {
        return ResponseEntity.ok(BaseResponse.<T>builder()
                .success(true)
                .message(source + " veri sayıları başarıyla alındı")
                .code(200)
                .data(data)
                .build());
    }
}