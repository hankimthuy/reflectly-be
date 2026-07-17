package org.mentorship.reflectly;

import java.util.TimeZone;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ReflectlyBeApplication {

    public static void main(String[] args) {
        // Must run before any DataSource is created: the Postgres JDBC driver sends the
        // JVM default zone as a session TimeZone param, and some tzdata builds (e.g. the
        // postgres:13 image) reject legacy aliases like "Asia/Saigon".
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        SpringApplication.run(ReflectlyBeApplication.class, args);
    }

}