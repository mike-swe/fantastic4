import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMapping(CorsRegistry registry) {
        registry.addMapping("/**")
         .allowedOriginPatterns("http://localhost:4200")
         .allowedMethods("POST", "GET");
    }
}
