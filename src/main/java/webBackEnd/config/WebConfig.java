package webBackEnd.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import webBackEnd.successfullyDat.PathCheck;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final PathCheck pathCheck;

    public WebConfig(PathCheck pathCheck) {
        this.pathCheck = pathCheck;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:D:/img/");
    }

}

