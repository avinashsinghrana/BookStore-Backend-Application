package com.bridgelabz.bookstore.utility;

import com.bridgelabz.bookstore.exception.UserException;
import com.bridgelabz.bookstore.model.UserModel;
import com.bridgelabz.bookstore.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class CommonUtility {

    @Autowired
    private static UserRepository userRepository;

    public static UserModel validateUser(String token) {
        Long id = JwtGenerator.decodeJWT(token);
        Optional<UserModel> user = userRepository.findById(id);
        if (user.isPresent())
            return user.get();
        throw new UserException("Invalid user", 200);
    }
}
