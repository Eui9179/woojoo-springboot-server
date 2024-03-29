package leui.woojoo.base.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.util.Collections;

@Configuration
public class FirebaseConfig {
    @Value("${project.properties.firebase-create-scoped}")
    String fireBaseCreateScoped;

    @Bean
    GoogleCredentials googleCredentials() throws IOException {
        return GoogleCredentials
                .fromStream(new ClassPathResource("firebase-admin.json").getInputStream())
                .createScoped(Collections.singletonList(fireBaseCreateScoped));
    }

    @Bean
    FirebaseApp firebaseApp(GoogleCredentials credentials) {
        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(credentials)
                .build();
        return FirebaseApp.initializeApp(options);
    }

    @Bean
    FirebaseMessaging firebaseMessaging(FirebaseApp firebaseApp) {
        return FirebaseMessaging.getInstance(firebaseApp);
    }
}
