package ua.nure.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ua.nure.models.User;
import ua.nure.services.RSAService;
import ua.nure.services.UserService;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

@RestController
@RequestMapping("/RSA")
public class RSAController {

    @Autowired
    RSAService rsaService;

    @Autowired
    UserService userService;

    @GetMapping
    public ResponseEntity<String> getOpenKey() {
        String message;

        try {
            message = rsaService.getKeyAsString("public");
            return new ResponseEntity<>(message, HttpStatus.OK);
        } catch (NoSuchAlgorithmException e) {
            return new ResponseEntity<>("Error", HttpStatus.EXPECTATION_FAILED);
        }

    }

    @PostMapping
    public ResponseEntity<String> create(@RequestBody User user) {
        try {
            User encodedUser = new User();
            encodedUser.setLogin(rsaService.decryptString(user.getLogin()));
            encodedUser.setPassword(rsaService.decryptString(user.getPassword()));

            String message;

            if (userService.isAccessGranted(encodedUser)) {
                message = "Hello, " + encodedUser.getLogin();
            } else {
                message = "Access blocked";
            }

            message = rsaService.encryptString(message, user.getKey());

            return new ResponseEntity<>(message, HttpStatus.OK);
        } catch (NoSuchPaddingException | IllegalBlockSizeException | NoSuchAlgorithmException | BadPaddingException |
                 InvalidKeySpecException | InvalidKeyException e) {
            return new ResponseEntity<>("Error", HttpStatus.EXPECTATION_FAILED);
        }
    }
}
