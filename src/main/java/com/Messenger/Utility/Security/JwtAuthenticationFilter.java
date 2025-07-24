package com.Messenger.Utility.Security;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;

import com.Messenger.Utility.CommonUtils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtUtil jwtUtil;
	private final CustomUserDetailsService customUserDetailsService;

	public JwtAuthenticationFilter(JwtUtil jwtUtil, CustomUserDetailsService customUserDetailsService) {
		super();
		this.jwtUtil = jwtUtil;
		this.customUserDetailsService = customUserDetailsService;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		CommonUtils.logMethodEntry(this);

		String header = request.getHeader("Authorization");

		if (header != null && header.startsWith("Bearer ")) {
			String token = header.substring(7);
			try {
				Claims claims = jwtUtil.validateToken(token);
				String username = claims.getSubject();

				UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);
				if (!userDetails.getUsername().equals(username)) {
					sendErrorResponse(response, "Unauthorized: Token username mismatch.", HttpServletResponse.SC_UNAUTHORIZED);
					return;
				}

				UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
						userDetails, null, userDetails.getAuthorities());

				SecurityContextHolder.getContext().setAuthentication(auth);

			} catch (ExpiredJwtException ex) {
				CommonUtils.logError(ex);
				sendErrorResponse(response, "Token expired", 498);
				return;
			} catch (JwtException | IllegalArgumentException ex) {
				CommonUtils.logError(ex);
				sendErrorResponse(response, "Unauthorized: Invalid token.", HttpServletResponse.SC_UNAUTHORIZED);
				return;
			}
		}
		filterChain.doFilter(request, response);
	}

	private void sendErrorResponse(HttpServletResponse response, String message, int statusCode) throws IOException {
		response.setStatus(statusCode);
		response.setContentType("application/json");
		response.getWriter().write("{\"message\": \"" + message + "\", \"status\": \"" + statusCode + "\"}");
	}

	// For WebSocket handshake bypass
	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		CommonUtils.logMethodEntry(this);
		String path = request.getRequestURI();
		return path.startsWith("/ws");
	}
}
