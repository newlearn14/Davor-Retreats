package com.dr.DavorRetreats.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserPass {

	@Autowired
	AuthService authService;

	public static String encrypt(String input) {
		if (input == null || input.isEmpty()) {
			return input;
		}
		StringBuilder result = new StringBuilder();

		for (char ch : input.toCharArray()) {
			if (Character.isUpperCase(ch)) {
				result.append(Character.toLowerCase(ch));
			} else if (Character.isLowerCase(ch)) {
				result.append(Character.toUpperCase(ch));
			} else {
				result.append(ch);
			}
		}
		result.reverse();
		return result.toString();
	}

	public static String decrypt(String input) {
		if (input == null || input.isEmpty()) {
			return input;
		}
		StringBuilder result = new StringBuilder(input);

		result.reverse();

		for (int i = 0; i < result.length(); i++) {
			char ch = result.charAt(i);
			if (Character.isUpperCase(ch)) {
				result.setCharAt(i, Character.toLowerCase(ch));
			} else if (Character.isLowerCase(ch)) {
				result.setCharAt(i, Character.toUpperCase(ch));
			}
		}
		return result.toString();
	}

}