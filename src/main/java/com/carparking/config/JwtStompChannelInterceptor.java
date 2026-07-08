package com.carparking.config;

import com.carparking.service.JwtService;
import com.carparking.service.UserRepository;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class JwtStompChannelInterceptor implements ChannelInterceptor {

    private final JwtService     jwtService;
    private final UserRepository userRepository;

    public JwtStompChannelInterceptor(JwtService jwtService,
                                      UserRepository userRepository) {
        this.jwtService     = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null || !StompCommand.CONNECT.equals(accessor.getCommand())) {
            return message;
        }

        String auth = accessor.getFirstNativeHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Missing WebSocket authorization");
        }

        String jwt = auth.substring(7);
        String email = jwtService.extractUsername(jwt);
        var userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty() || !jwtService.isTokenValid(jwt, userOpt.get())) {
            throw new IllegalArgumentException("Invalid WebSocket token");
        }

        var user = userOpt.get();
        var authentication = new UsernamePasswordAuthenticationToken(
                email, null,
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole())));
        accessor.setUser(authentication);
        return message;
    }
}
