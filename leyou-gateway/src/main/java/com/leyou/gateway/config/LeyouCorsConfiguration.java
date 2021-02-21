package com.leyou.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class LeyouCorsConfiguration {

    /**
     * 配置过滤器，允许指定域名的跨域请求
     *
     * @return springMVC提供的跨域CORS过滤器
     */
    @Bean
    public CorsFilter corsFilter() {
        //初始化cors配置对象
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        // 打开允许cookie跨域访问
        corsConfiguration.setAllowCredentials(true);
        // 添加允许跨域的域名
        corsConfiguration.addAllowedOrigin("http://manage.leyou.com");
        corsConfiguration.addAllowedOrigin("http://www.leyou.com");
        // 添加允许跨域的请求方法，设为所有
        corsConfiguration.addAllowedMethod("*");
        // 添加允许跨域的头信息，设为所有
        corsConfiguration.addAllowedHeader("*");

        // 初始化cors数据源对象
        UrlBasedCorsConfigurationSource corsConfigurationSource = new UrlBasedCorsConfigurationSource();
        corsConfigurationSource.registerCorsConfiguration("/**", corsConfiguration);

        return new CorsFilter(corsConfigurationSource);
    }
}
