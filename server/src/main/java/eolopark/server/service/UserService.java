package eolopark.server.service;

import eolopark.server.model.internal.User;
import eolopark.server.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    /* Attributes */
    private final UserRepository userRepository;

    /* Constructor */
    public UserService (UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /* Methods */
    public User getUserByName (String name) {
        return userRepository.findUserByName(name).orElseThrow();
    }

    public Page<User> getAllUser (Pageable page) {
        return userRepository.findAllUser(page);
    }

    public User getUserById (Long id) {
        return userRepository.findById(id).orElseThrow();
    }

    public void saveUser (User user) {
        User newUser = new User(user.getName(), user.getEncodedPassword());
        newUser.setParkMax(5);
        newUser.setRoles(List.of("user"));
        userRepository.save(newUser);
    }

    public void replaceUserById (Long id) {
        User user = userRepository.findById(id).orElseThrow();
        if (!user.getRoles().contains("premium")) {
            userRepository.updateUserParkMaxById(Integer.MAX_VALUE, id);
            userRepository.updateUserRolesById("premium", id);
        }
    }

    public void deleteUserById (Long id) {
        userRepository.deleteById(id);
    }

    // Trigger an exception if any SQL injection characters are found
    public String sanitizeString (String string) {

        String stringSanitized =
                string.trim().replaceAll(";", "").replaceAll("--", "").replaceAll("'", "").replaceAll("\"", "").replaceAll("=", "").replaceAll("<", "").replaceAll(">", "").replaceAll("[()]", "").replaceAll("[{}]", "").replaceAll("[\\[\\]]", "").replaceAll("SELECT", "").replaceAll("FROM", "").replaceAll("WHERE", "").replaceAll("AND", "").replaceAll("OR", "").replaceAll("select", "").replaceAll("from", "").replaceAll("where", "").replaceAll("and", "").replaceAll("or", "");
        if (!stringSanitized.equals(string)) throw new IllegalArgumentException();
        return stringSanitized;
    }
}