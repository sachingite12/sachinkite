package com.example.demo;

import com.zerodhatech.models.HistoricalData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.zerodhatech.kiteconnect.kitehttp.exceptions.KiteException;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

@RestController
public class KiteController {

    @Autowired
    private KiteService kiteService;

    @GetMapping("/login")
    public void login(HttpServletResponse response) throws IOException {
        response.sendRedirect(kiteService.getLoginUrl());
    }

    @GetMapping("/callback")
    public void callback(@RequestParam("request_token") String requestToken) throws KiteException, IOException {
        kiteService.generateSession(requestToken);
    }

    @GetMapping("/historical-data")
    public HistoricalData getHistoricalData(@RequestParam long instrumentToken,
                                                  @RequestParam String from,
                                                  @RequestParam String to,
                                                  @RequestParam String interval) throws KiteException, IOException {
        return kiteService.getHistoricalData(instrumentToken, from, to, interval);
    }

    @GetMapping("/live-data")
    public SseEmitter getLiveData(@RequestParam List<Long> instrumentTokens) {
        SseEmitter emitter = new SseEmitter();
        kiteService.getLiveData(instrumentTokens, emitter);
        return emitter;
    }

    @PostMapping("/place-order")
    public com.zerodhatech.models.Order placeOrder(@RequestBody com.zerodhatech.models.OrderParams orderParams) throws KiteException, IOException {
        return kiteService.placeOrder(orderParams);
    }
}
