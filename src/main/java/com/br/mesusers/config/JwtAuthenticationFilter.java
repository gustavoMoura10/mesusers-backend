package com.br.mesusers.config;

import com.br.mesusers.auth.AuthService;
import com.br.mesusers.user.UserEntity;

import com.br.mesusers.user.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final AuthService authService;
    private final UserService userService;

    public JwtAuthenticationFilter(AuthService authService,
            UserService userService) {
        this.authService = authService;
        this.userService = userService;

    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        try {
            final String authHeader = request.getHeader("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                filterChain.doFilter(request, response);
                return;
            }

            final String jwt = authHeader.substring(7);
            String userEmail = authService.validateAndParseToken(jwt).getSubject();
            UserEntity userEntity = this.authService.getCurrentUser(jwt);
            if (userEmail != null && userEntity == null) {
                throw new RuntimeException("Usuário não encontrado");
            }
            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userEntity,
                        null,
                        userEntity.getAuthorities());

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);

                // Adiciona atributos úteis na request para acesso em controllers, se necessário
                request.setAttribute("userId", userEntity.getId());
                request.setAttribute("userRoles",
                        authService.validateAndParseToken(jwt).get("roles", String.class));
            }

        } catch (Exception e) {
            SecurityContextHolder.clearContext();
            System.out.println("Falha na autenticação: " + e.getMessage());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Falha na autenticação: " + e.getMessage());
            return;
        }

        filterChain.doFilter(request, response);
    }

}