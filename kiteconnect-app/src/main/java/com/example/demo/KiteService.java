package com.example.demo;

import com.zerodhatech.kiteconnect.KiteConnect;
import com.zerodhatech.ticker.KiteTicker;
import com.zerodhatech.kiteconnect.kitehttp.exceptions.KiteException;
import com.zerodhatech.models.HistoricalData;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

@Service
public class KiteService {

    private static final Logger logger = LoggerFactory.getLogger(KiteService.class);

    @Value("${KITE_API_KEY}")
    private String apiKey;

    @Value("${KITE_API_SECRET}")
    private String apiSecret;

    @Value("${KITE_USER_ID}")
    private String userId;

    private KiteConnect kiteConnect;

    @PostConstruct
    public void init() {
        kiteConnect = new KiteConnect(apiKey);
        kiteConnect.setUserId(userId);
    }

    public String getLoginUrl() {
        return kiteConnect.getLoginURL();
    }

    public void generateSession(String requestToken) throws KiteException, IOException {
        com.zerodhatech.models.User user = kiteConnect.generateSession(requestToken, apiSecret);
        kiteConnect.setAccessToken(user.accessToken);
        kiteConnect.setPublicToken(user.publicToken);
    }

    public HistoricalData getHistoricalData(long instrumentToken, String from, String to, String interval) throws KiteException, IOException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime fromDateTime = LocalDateTime.parse(from, formatter);
        LocalDateTime toDateTime = LocalDateTime.parse(to, formatter);
        Date fromDate = Date.from(fromDateTime.atZone(ZoneId.systemDefault()).toInstant());
        Date toDate = Date.from(toDateTime.atZone(ZoneId.systemDefault()).toInstant());

        String token = String.valueOf(instrumentToken);
        logger.info("Fetching historical data for instrument: {}, from: {}, to: {}, interval: {}", token, from, to, interval);
        return kiteConnect.getHistoricalData(fromDate, toDate, token, interval, false, false);
    }

    public void getLiveData(List<Long> instrumentTokens, SseEmitter emitter) {
        KiteTicker ticker = new KiteTicker(kiteConnect.getAccessToken(), kiteConnect.getApiKey());

        emitter.onCompletion(() -> {
            logger.info("SSE connection completed.");
            ticker.disconnect();
        });

        emitter.onTimeout(() -> {
            logger.info("SSE connection timed out.");
            ticker.disconnect();
        });

        ticker.setOnConnectedListener(() -> {
            logger.info("Connected to Kite Ticker");
            ticker.subscribe(new java.util.ArrayList<>(instrumentTokens));
        });

        ticker.setOnDisconnectedListener(() -> logger.info("Disconnected from Kite Ticker"));

        ticker.setOnTickerArrivalListener(ticks -> {
            try {
                emitter.send(ticks);
            } catch (IOException e) {
                logger.error("Error sending ticks to client", e);
            }
        });

        try {
            ticker.setTryReconnection(true);
            ticker.setMaximumRetries(10);
            ticker.setMaximumRetryInterval(30);
        } catch (KiteException e) {
            logger.error("Error setting ticker reconnection properties", e);
        }

        ticker.connect();
    }

    public com.zerodhatech.models.Order placeOrder(com.zerodhatech.models.OrderParams orderParams) throws KiteException, IOException {
        logger.info("Placing order: {}", orderParams);
        return kiteConnect.placeOrder(orderParams, com.zerodhatech.kiteconnect.utils.Constants.VARIETY_REGULAR);
    }
}