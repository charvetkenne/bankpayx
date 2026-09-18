package com.mansa.dto;

//import com.mansa.domain.User.UserBuilder;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {
  
    private String message;
      // utile si tu ajoutes plus tard gateway token relay
    private String accessToken;

    private Long expiresIn;
    
}
