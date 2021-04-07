package dev.evertonsavio.fcmpushnotifications.firebase;

import com.google.firebase.messaging.*;
import dev.evertonsavio.fcmpushnotifications.model.PushNotificationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Service
public class FCMService {

    private Logger logger = LoggerFactory.getLogger(FCMService.class);

    public void sendMessage(Map<String, String> data, PushNotificationRequest request)
            throws InterruptedException, ExecutionException {
        Message message = getPreconfiguredMessageWithData(data, request);
        String response = sendAndGetResponse(message);
        logger.info("Sent message with data. Topic: " + request.getTopic() + ", " + response);
    }

    public void sendMessageWithoutData(PushNotificationRequest request)
            throws InterruptedException, ExecutionException {
        Message message = getPreconfiguredMessageWithoutData(request);
        String response = sendAndGetResponse(message);
        logger.info("Sent message without data. Topic: " + request.getTopic() + ", " + response);
    }

    public void sendMessageToToken(PushNotificationRequest request) throws InterruptedException, ExecutionException, FirebaseMessagingException {

        Message message = Message.builder()
                .putData("score", "850")
                .putData("time", "2:45")
                .setToken(request.getToken())
                .build();

        sendAndGetResponseVoid(message);

        logger.info("Sent message to token. Device token: " + request.getToken());
    }
//
//    public void sendMessageToToken(PushNotificationRequest request)
//            throws InterruptedException, ExecutionException {
//        Message message = getPreconfiguredMessageToToken(request);
//        String response = sendAndGetResponse(message);
//        logger.info("Sent message to token. Device token: " + request.getToken() + ", " + response);
//    }

    private void sendAndGetResponseVoid(Message message) throws FirebaseMessagingException {
        FirebaseMessaging.getInstance().send(message);
        //.sendAsync(message).get();
    }

    private String sendAndGetResponse(Message message) throws ExecutionException, InterruptedException {
        return FirebaseMessaging.getInstance()
                .sendAsync(message).get();
    }

    private AndroidConfig getAndroidConfig(String topic) {
        return AndroidConfig.builder()
                .setTtl(Duration.ofMinutes(2).toMillis()).setCollapseKey(topic)
                .setPriority(AndroidConfig.Priority.HIGH)
                .setNotification(AndroidNotification.builder().setSound(NotificationParameter.SOUND.getValue())
                        .setColor(NotificationParameter.COLOR.getValue()).setTag(topic).build()).build();
    }

    private ApnsConfig getApnsConfig(String topic) {
        return ApnsConfig.builder()
                .setAps(Aps.builder().setCategory(topic).setThreadId(topic).build()).build();
    }

    private Message getPreconfiguredMessageToToken(PushNotificationRequest request) {
        return getPreconfiguredMessageBuilder(request).setToken(request.getToken())
                .build();
    }

    private Message getPreconfiguredMessageWithoutData(PushNotificationRequest request) {
        return getPreconfiguredMessageBuilder(request).setTopic(request.getTopic())
                .build();
    }

    private Message getPreconfiguredMessageWithData(Map<String, String> data, PushNotificationRequest request) {
        return getPreconfiguredMessageBuilder(request).putAllData(data).setTopic(request.getTopic())
                .build();
    }

    private Message.Builder getPreconfiguredMessageBuilder(PushNotificationRequest request) {
        AndroidConfig androidConfig = getAndroidConfig(request.getTopic());
        ApnsConfig apnsConfig = getApnsConfig(request.getTopic());
        Notification.Builder builder = Notification.builder();
        return Message.builder()
                .setApnsConfig(apnsConfig).setAndroidConfig(androidConfig)
                //.setNotification(new Notification(request.getTitle(), request.getMessage()));
        .setNotification(builder.build());
    }


}
