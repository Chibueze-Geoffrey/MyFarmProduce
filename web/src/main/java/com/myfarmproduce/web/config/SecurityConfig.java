package com.myfarmproduce.web.config;

import com.myfarmproduce.web.security.LoginSuccessHandler;
import com.myfarmproduce.web.security.RoleAwareLogoutSuccessHandler;
import com.myfarmproduce.web.security.SingleLoginAuthenticationProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final SingleLoginAuthenticationProvider authProvider;
    private final LoginSuccessHandler loginSuccessHandler;
    private final RoleAwareLogoutSuccessHandler logoutSuccessHandler;

    public SecurityConfig(SingleLoginAuthenticationProvider authProvider, LoginSuccessHandler loginSuccessHandler,
                           RoleAwareLogoutSuccessHandler logoutSuccessHandler) {
        this.authProvider = authProvider;
        this.loginSuccessHandler = loginSuccessHandler;
        this.logoutSuccessHandler = logoutSuccessHandler;
    }

    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authenticationProvider(authProvider)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/css/**", "/js/**", "/webjars/**", "/uploads/**").permitAll()
                        // More specific account rules must come before the broad "/account/**" permitAll below.
                        .requestMatchers("/account/change-password").hasRole("CUSTOMER")
                        .requestMatchers("/", "/catalog", "/catalog/**", "/home/**", "/account/**").permitAll()
                        .requestMatchers("/payment/webhook").permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/cart/**", "/checkout/**", "/orders/**", "/profile/**",
                                "/support/**", "/chat/**", "/payment/**", "/ws/**").hasRole("CUSTOMER")
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/account/login")
                        .loginProcessingUrl("/account/login")
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .successHandler(loginSuccessHandler)
                        .failureUrl("/account/login?error"))
                .logout(logout -> logout
                        .logoutRequestMatcher(new AntPathRequestMatcher("/account/logout", "POST"))
                        .logoutSuccessHandler(logoutSuccessHandler))
                .csrf(csrf -> csrf.ignoringRequestMatchers("/payment/webhook", "/ws/**"));

        return http.build();
    }
}
