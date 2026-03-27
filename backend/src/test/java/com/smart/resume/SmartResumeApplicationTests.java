package com.smart.resume;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@SpringBootTest
class SmartResumeApplicationTests {

    @Test
    void contextLoads() {
    }

    @Test
    void testDigest() {
        String password = "123456";
        // 这里用UUID随机生成作为salt，比较简单的示例
        String encryptPwd = DigestUtils.md5DigestAsHex((UUID.randomUUID().
                toString().replace("-", "") + password)
                .getBytes(StandardCharsets.UTF_8));
        System.out.println(encryptPwd);
    }
}
