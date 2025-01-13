package com.app.SpringSecurityApp.config;

import com.app.SpringSecurityApp.persistence.entity.RoleEnum;
import com.app.SpringSecurityApp.service.UserDetailServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity   //para poder utilizar las anotaciones @PreAuthorize() en el TestAuthController
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                .csrf(csrf -> csrf.disable())
                .httpBasic(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(http -> {
                    //Configurar los endpoints públicos
                    http.requestMatchers(HttpMethod.GET, "/auth/get").permitAll();

                    //Configurar los endpoints privados
                    //validando por el authority
//                    http.requestMatchers(HttpMethod.POST, "/auth/post").hasAnyAuthority("CREATE", "READ");
//                    http.requestMatchers(HttpMethod.PUT, "/auth/put").hasAuthority("UPDATE");
//                    http.requestMatchers(HttpMethod.DELETE, "/auth/delete").hasAuthority("DELETE");
//                    http.requestMatchers(HttpMethod.PATCH, "/auth/patch").hasAuthority("REFACTOR");

                    //validando por el Role
                    http.requestMatchers(HttpMethod.POST, "/auth/post").hasAnyRole(RoleEnum.DEVELOPER.name(), RoleEnum.ADMIN.name(), RoleEnum.USER.name());
                    http.requestMatchers(HttpMethod.PUT, "/auth/put").hasAnyRole(RoleEnum.DEVELOPER.name(), RoleEnum.ADMIN.name());
                    http.requestMatchers(HttpMethod.DELETE, "/auth/delete").hasAnyRole(RoleEnum.DEVELOPER.name(), RoleEnum.ADMIN.name());
                    http.requestMatchers(HttpMethod.PATCH, "/auth/patch").hasRole(RoleEnum.DEVELOPER.name());

                    //Configurar el resto de endpoits - NO ESPECIFICADOS
                    http.anyRequest().denyAll(); //denyAll() recahaza todo lo que yo no especifique, es más restrictivo
//                    http.anyRequest().authenticated(); //authenticated() aunque yo no especifique en los requestMatchers, si yo tengo credenciales correctas me va a dejar pasar, es más permisivo
                })
                .build();
    }

    /*
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                .csrf(csrf -> csrf.disable()) //no necesitamos tener habilitado esta proteccion por ahora, se utiliza mas que todo en tokens JWT para formularios
                .httpBasic(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .build();
    }
    */

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public AuthenticationProvider authenticationProvider(UserDetailServiceImpl userDetailService) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setPasswordEncoder(passwordEncoder());
        provider.setUserDetailsService(userDetailService);
        return provider;
    }

    /*@Bean
    public UserDetailsService userDetailsService() {
        List<UserDetails> userDetailsList = List.of(
                User.withUsername("santiago")
                        .password("1234")
                        .roles("ADMIN")
                        .authorities("READ", "CREATE")
                        .build(),
                User.withUsername("daniel")
                        .password("123")
                        .roles("USER")
                        .authorities("READ")
                        .build()
        );
        return new InMemoryUserDetailsManager(userDetailsList);
    }*/

    @Bean
    public PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance();
    }
}
