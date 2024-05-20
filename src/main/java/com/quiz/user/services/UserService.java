package com.quiz.user.services;

import com.quiz.user.entities.Login;
import com.quiz.user.entities.LoginResponse;
import com.quiz.user.entities.User;
import com.quiz.user.entities.UserDTO;

import java.util.List;

public interface UserService {

	public List<UserDTO> getUsers();
	public UserDTO getUsers(int userId);
	public UserDTO registerUser(User user);
	public LoginResponse loginUser(Login user);
	public User getUserDetails(String email);

}
