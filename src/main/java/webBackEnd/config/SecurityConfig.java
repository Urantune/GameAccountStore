package webBackEnd.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import webBackEnd.service.CustomerService;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private CustomerService customerService;

    @Bean
    public AuthenticationManager authManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder builder = http.getSharedObject(AuthenticationManagerBuilder.class);
        builder.userDetailsService(customerService)
                .passwordEncoder(passwordEncoder());
        return builder.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable());

        http.authorizeHttpRequests(auth -> auth
                // static + public
                .requestMatchers(
                        "/css/**", "/js/**", "/img/**",
                        "/bestseller/**", "/assets/**", "/lib/**",
                        "/scss/**", "/images/**"
                ).permitAll()
                .requestMatchers("/register", "/register/**").permitAll()
                .requestMatchers("/home", "/home/**").permitAll()

                // CHO PHÉP /adminHome KHÔNG CẦN LOGIN
                .requestMatchers("/adminHome", "/adminHome/**").permitAll()
                .requestMatchers("/staffHome", "/staffHome/**").permitAll()

                // bảo vệ các URL khác
                .requestMatchers("/Admin/edit/**").hasRole("ADMIN")
                .requestMatchers("/Staff/**").hasRole("STAFF")
                .anyRequest().authenticated()
        );

        http.formLogin(form -> form
                .loginPage("/home")              // trang login
                .loginProcessingUrl("/login")
                .usernameParameter("username")
                .passwordParameter("password")
                .successHandler((req, res, auth) -> {
                    res.setContentType("application/json");
                    res.getWriter().write(
                            auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))
                                    ? "{\"redirect\":\"/Admin\"}"
                                    : "{\"redirect\":\"/home\"}"
                    );
                })
                .failureHandler((req, res, ex) -> {
                    res.setContentType("application/json");
                    res.setStatus(401);
                    res.getWriter().write("{\"error\":\"Error username or password\"}");
                })
                .permitAll() // 🟢 rất quan trọng – cho phép ai cũng gọi /home, /login
        );

        http.logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/home?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .clearAuthentication(true)
                .permitAll()
        );

        return http.build();
    }
}
