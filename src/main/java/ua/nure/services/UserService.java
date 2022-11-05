package ua.nure.services;

import com.google.gson.Gson;
import org.springframework.stereotype.Service;
import ua.nure.models.User;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@Service
public class UserService {

    public boolean isAccessGranted (User user) {
        boolean granted = false;

        try {
            File usersFile = new File("users.txt");
            String JSON = new String(Files.readAllBytes(usersFile.toPath()));
            User[] users = new Gson().fromJson(JSON, User[].class);
            for (int i = 0; i < users.length; i++) {
                if (user.equals(users[i])) {
                    granted = true;
                    i = users.length;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return granted;
    }
}
