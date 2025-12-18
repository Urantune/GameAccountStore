package webBackEnd.config;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
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
import webBackEnd.service.AppUserDetailsService;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final AppUserDetailsService appUserDetailsService;

    public SecurityConfig(AppUserDetailsService appUserDetailsService) {
        this.appUserDetailsService = appUserDetailsService;
    }

    @Bean
    public AuthenticationManager authManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder builder = http.getSharedObject(AuthenticationManagerBuilder.class);
        builder.userDetailsService(appUserDetailsService).passwordEncoder(passwordEncoder());
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
                .requestMatchers("/api/**").permitAll()
                .requestMatchers(
                        "/css/**", "/js/**", "/img/**",
                        "/bestseller/**", "/assets/**", "/lib/**",
                        "/scss/**", "/images/**"
                ).permitAll()

                .requestMatchers("/register", "/register/**").permitAll()

                .requestMatchers(HttpMethod.GET, "/home", "/home/").permitAll()
                .requestMatchers(HttpMethod.GET, "/home/gameDetail/**").permitAll()

                .requestMatchers(HttpMethod.GET, "/adminHome", "/adminHome/").permitAll()
                .requestMatchers(HttpMethod.GET, "/staffHome", "/staffHome/").permitAll()

                .requestMatchers("/adminHome/**").hasRole("ADMIN")
                .requestMatchers("/staffHome/**").hasRole("STAFF")

                .requestMatchers("/home/**").authenticated()

                .anyRequest().authenticated()
        );

        http.exceptionHandling(ex -> ex.authenticationEntryPoint((request, response, authException) -> {

            if (request.getRequestURI().startsWith("/api/")) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json; charset=UTF-8");
                response.getWriter().write("{\"error\":\"Unauthorized\"}");
                return;
            }

            String uri = request.getRequestURI();
            if (uri.startsWith("/adminHome")) {
                response.sendRedirect("/adminHome?login=true");
            } else if (uri.startsWith("/staffHome")) {
                response.sendRedirect("/staffHome?login=true");
            } else {
                response.sendRedirect("/home?login=true");
            }
        }));

        http.formLogin(form -> form
                .loginProcessingUrl("/login")
                .usernameParameter("username")
                .passwordParameter("password")
                .defaultSuccessUrl("/home", true)

                .successHandler((req, res, auth) -> {
                    boolean isAjax = "XMLHttpRequest".equals(req.getHeader("X-Requested-With"))
                            || (req.getHeader("Accept") != null && req.getHeader("Accept").contains("application/json"));

                    String redirect =
                            auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))
                                    ? "/adminHome"
                                    : auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_STAFF"))
                                    ? "/staffHome"
                                    : "/home";

                    if (isAjax) {
                        res.setContentType("application/json; charset=UTF-8");
                        res.getWriter().write("{\"redirect\":\"" + redirect + "\"}");
                    } else {
                        res.sendRedirect(redirect);
                    }
                })

                .failureHandler((req, res, exx) -> {
                    boolean isAjax = "XMLHttpRequest".equals(req.getHeader("X-Requested-With"))
                            || (req.getHeader("Accept") != null && req.getHeader("Accept").contains("application/json"));

                    Throwable cause = exx;
                    while (cause.getCause() != null) cause = cause.getCause();

                    String msg;
                    if (cause instanceof DisabledException) msg = "Tài khoản đã bị khóa";
                    else if (cause instanceof AccountExpiredException) msg = "Tài khoản hết hạn";
                    else msg = "Sai username hoặc password";

                    if (isAjax) {
                        res.setStatus(401);
                        res.setContentType("application/json; charset=UTF-8");
                        res.getWriter().write("{\"error\":\"" + msg + "\"}");
                    } else {
                        String target = req.getParameter("target");
                        if (target == null || target.isBlank()) target = "/home";
                        String url = target + (target.contains("?") ? "&" : "?")
                                + "login=true&errorMsg=" + URLEncoder.encode(msg, StandardCharsets.UTF_8);
                        res.sendRedirect(url);
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
