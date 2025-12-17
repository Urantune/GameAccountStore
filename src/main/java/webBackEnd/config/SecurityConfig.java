package webBackEnd.config;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
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
                .requestMatchers("/api/**").permitAll()
                .requestMatchers(
                        "/css/**", "/js/**", "/img/**",
                        "/bestseller/**", "/assets/**", "/lib/**",
                        "/scss/**", "/images/**"
                ).permitAll()
                .requestMatchers("/register", "/register/**").permitAll()
                .requestMatchers("/home", "/home/**").permitAll()


                .requestMatchers("/adminHome", "/adminHome/**").permitAll()
                .requestMatchers("/staffHome", "/staffHome/**").permitAll()


                .requestMatchers("/Admin/edit/**").hasRole("ADMIN")
                .requestMatchers("/Staff/**").hasRole("STAFF")
                .requestMatchers("/home/add/**").authenticated()
                .requestMatchers("/home/cart").authenticated()
                .anyRequest().authenticated()
        );
        http.exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) -> {

                    if (request.getRequestURI().startsWith("/api/")) {
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        response.setContentType("application/json; charset=UTF-8");
                        response.getWriter().write("{\"error\":\"Unauthorized\"}");
                    } else {
                        response.sendRedirect("/home");
                    }

                })
        );


        http.formLogin(form -> form
                .loginPage("/home")
                .loginProcessingUrl("/login")
                .usernameParameter("username")
                .passwordParameter("password")

                .successHandler((req, res, auth) -> {
                    res.setContentType("application/json");
                    res.getWriter().write(
                            auth.getAuthorities().stream()
                                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))
                                    ? "{\"redirect\":\"/Admin\"}"
                                    : "{\"redirect\":\"/home\"}"
                    );
                })

                .failureHandler((req, res, ex) -> {
                    res.setStatus(401);
                    res.setContentType("application/json; charset=UTF-8");
                    res.setCharacterEncoding("UTF-8");

                    Throwable cause = ex;
                    while (cause.getCause() != null) {
                        cause = cause.getCause();
                    }

                    if (cause instanceof DisabledException) {
                        res.getWriter().write("{\"error\":\"Tài khoản đã bị khóa\"}");
                    }
                    else if (cause instanceof AccountExpiredException) {
                        res.getWriter().write(
                                "{\"error\":\"Tài khoản đăng ký sau thời hạn cho phép\"}"
                        );
                    }
                    else {
                        res.getWriter().write(
                                "{\"error\":\"Sai username hoặc password\"}"
                        );
                    }
                })

                .permitAll()
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
