package com.sky.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "sky.jwt")//这个是在common模块下,但是可以找server模块下的配置文件
@Data
public class JwtProperties {

    /**
     * 管理端员工生成jwt令牌相关配置
     */
    private String adminSecretKey;
    private long adminTtl;
    private String adminTokenName;

    /**
     * 用户端微信用户生成jwt令牌相关配置
     */
    private String userSecretKey;
    private long userTtl;
    private String userTokenName;
/*yml配置类就写了秘钥,过期时间(几小时),令牌名称(token)
sky:
  jwt:
    # 设置jwt签名加密时使用的秘钥
    admin-secret-key: itcast
    # 设置jwt过期时间  2000小时
    admin-ttl: 7200000000
    # 设置前端传递过来的令牌名称
    admin-token-name: token
    # 以下是用户端的
    user-secret-key: itheima
    user-ttl: 7200000000
    user-token-name: authentication
* */
}
