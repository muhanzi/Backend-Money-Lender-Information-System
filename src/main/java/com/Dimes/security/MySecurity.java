package com.Dimes.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)

public class MySecurity extends WebSecurityConfigurerAdapter {

    private UserDetailsService userDetailsService;
    private JwtAuthenticationEntryPoint authenticationEntryPoint;
    private JwtRequestFilter requestFilter;

    @Autowired
    public MySecurity(UserDetailsService userDetailsService, JwtAuthenticationEntryPoint authenticationEntryPoint,
            JwtRequestFilter requestFilter) {
        this.userDetailsService = userDetailsService;
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.requestFilter = requestFilter;
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(authenticationProvider());
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        // configure AuthenticationManager so that it knows from where to load
        // user for matching credentials
        // Use BCryptPasswordEncoder
        auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    // lebon modified this // cors disabled
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // We don't need CSRF for this example
        // to dasble also cross origin resource sharing
        // http.cors().and().csrf().disable()
        http.csrf().disable().exceptionHandling().authenticationEntryPoint(authenticationEntryPoint).and()
                // make sure we use stateless session; session won't be used to
                // store user's state.
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
                // dont authenticate this particular request
                .authorizeRequests().antMatchers("/api/register").permitAll().antMatchers("/swagger-ui.html**")
                .permitAll().antMatchers("/webjars/**").permitAll().antMatchers("/swagger-resources/**").permitAll()
                .antMatchers("/v2/api-docs").permitAll().antMatchers("/api/authenticate").permitAll()
                .antMatchers(HttpMethod.OPTIONS, "/**").permitAll().
                // all other requests need to be authenticated
                anyRequest().authenticated();

        // Add a filter to validate the tokens with every request
        http.addFilterBefore(requestFilter, UsernamePasswordAuthenticationFilter.class);

    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());
        daoAuthenticationProvider.setUserDetailsService(this.userDetailsService);
        return daoAuthenticationProvider;
    }

}
