package xyz.ls.sjtuforum.provider;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;

@Data
@ConfigurationProperties(prefix = "ucloud.ufile")
@Service
public class UFileProperties {
    private String publicKey;
    private String privateKey;
    private String bucketName;
    private String uploadDomain;
    private String downloadDomain;

    private String bucketType;

    private Integer expiresDuration;
}
