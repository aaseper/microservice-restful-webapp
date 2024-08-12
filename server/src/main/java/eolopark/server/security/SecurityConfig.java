package eolopark.server.security;

import eolopark.server.controller.exception.CustomBearerTokenAccessDeniedHandler;
import eolopark.server.security.jwt.JwtRequestFilter;
import eolopark.server.security.jwt.UnauthorizedHandlerJwt;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomBearerTokenAccessDeniedHandler customBearerTokenAccessDeniedHandler;
    public RepositoryUserDetailsService userDetailService;
    /* Attributes */
    private final JwtRequestFilter jwtRequestFilter;
    private final UnauthorizedHandlerJwt unauthorizedHandlerJwt;

    /* Constructor */
    public SecurityConfig (JwtRequestFilter jwtRequestFilter, RepositoryUserDetailsService userDetailService,
                           UnauthorizedHandlerJwt unauthorizedHandlerJwt,
                           CustomBearerTokenAccessDeniedHandler customBearerTokenAccessDeniedHandler) {
        this.jwtRequestFilter = jwtRequestFilter;
        this.userDetailService = userDetailService;
        this.unauthorizedHandlerJwt = unauthorizedHandlerJwt;
        this.customBearerTokenAccessDeniedHandler = customBearerTokenAccessDeniedHandler;
    }

    /* Methods */
    @Bean
    public PasswordEncoder passwordEncoder () {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager (AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider () {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();

        authProvider.setUserDetailsService(userDetailService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    @Order (1)
    public SecurityFilterChain apiFilterChain (HttpSecurity http) throws Exception {

        http.authenticationProvider(authenticationProvider());

        http.securityMatcher("/api/**").exceptionHandling(handling -> handling.authenticationEntryPoint(unauthorizedHandlerJwt));

        http.authorizeHttpRequests(authorize -> authorize
                // PRIVATE ENDPOINTS
                .requestMatchers(HttpMethod.GET, "/api/users/me").hasAnyRole("user", "admin", "premium")
                .requestMatchers(HttpMethod.POST, "/api/users/register").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/parks/**").hasAnyRole("user", "admin", "premium")
                .requestMatchers(HttpMethod.POST, "/api/parks/**").hasAnyRole("user", "admin", "premium")
                .requestMatchers(HttpMethod.PUT, "/api" + "/parks/**").hasAnyRole("user", "admin", "premium")
                .requestMatchers(HttpMethod.DELETE, "/api" + "/parks/**").hasAnyRole("user", "admin", "premium")
                .requestMatchers(HttpMethod.GET, "/api" + "/users/**").hasRole("admin")
                .requestMatchers(HttpMethod.POST, "/api/users/**").hasRole("admin")
                .requestMatchers(HttpMethod.PUT, "/api/users/**").hasRole("admin")
                .requestMatchers(HttpMethod.DELETE, "/api/users/**").hasRole("admin")
                // PUBLIC ENDPOINTS
                .anyRequest().permitAll()).exceptionHandling().accessDeniedHandler(this.customBearerTokenAccessDeniedHandler);

        // Disable Form login Authentication
        http.formLogin(formLogin -> formLogin.disable());

        // Disable CSRF protection (it is difficult to implement in REST APIs)
        http.csrf(csrf -> csrf.disable());

        // Disable Basic Authentication
        http.httpBasic(httpBasic -> httpBasic.disable());

        // Stateless session
        http.sessionManagement(management -> management.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // Add JWT Token filter
        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    @Order (2)
    public SecurityFilterChain webFilterChain (HttpSecurity http) throws Exception {

        http.authenticationProvider(authenticationProvider());

        http.authorizeHttpRequests(authorize -> authorize
                // PUBLIC PAGES
                .requestMatchers("/", "/signup", "/error", "/400", "/unauthorized", "/403", "/404", "/429", "/report-updater.js", "report-updater.js", "/static/report-updater.js, /static**", "/static/**").permitAll()
                .requestMatchers("/users**", "/users/**", "/swagger-ui/**", "/v3/**").hasRole("admin")
                // PRIVATE PAGES
                .anyRequest().authenticated()).formLogin(formLogin -> formLogin.loginPage("/login").failureUrl(
                        "/loginerror").defaultSuccessUrl("/").permitAll()).logout(logout -> logout.logoutUrl("/logout"
        ).logoutSuccessUrl("/").permitAll()).exceptionHandling().accessDeniedHandler(this.customBearerTokenAccessDeniedHandler);

        return http.build();
    }
}