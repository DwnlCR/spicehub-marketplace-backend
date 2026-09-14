package br.com.dwnl.spicehub.identity.application.usecase;

import br.com.dwnl.spicehub.identity.application.exception.EmailAlreadyExistsException;
import br.com.dwnl.spicehub.identity.application.port.PasswordEncoder;
import br.com.dwnl.spicehub.identity.domain.model.Email;
import br.com.dwnl.spicehub.identity.domain.model.User;
import br.com.dwnl.spicehub.identity.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RegisterUserUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User execute(String name, String email, String password){
        Email userEmail = new Email(email);

        if (userRepository.existsByEmail(userEmail)){
            throw new EmailAlreadyExistsException(userEmail);
        }

        String passwordHash = passwordEncoder.encode(password);

        User user = User.create(name, userEmail, passwordHash);

        return userRepository.save(user);
    }
}
