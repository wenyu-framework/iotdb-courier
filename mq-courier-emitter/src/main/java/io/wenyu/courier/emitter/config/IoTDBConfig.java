package io.wenyu.courier.emitter.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.validation.constraints.Min;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "iot-db")
@Data
public class IoTDBConfig {
    private List<String> nodeUrl;
    private String username;
    private String password;
    private String basePath;
    private String timeParse = "yyyy-MM-ddTHH:mm:ss.sss";
    private String timeField = "Time";
    private String timeUnit = "ns";
    private String timeZone = "Asia/Shanghai";
    private String OverwriteDeviceId;
}
