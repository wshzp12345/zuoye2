package com.lagou.edu.factory;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProxyInfo {
    private String proxyClassName;
    private Class proxyClass;
    private String propertyName;
    private String ref;
}
