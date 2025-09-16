package tech.chhsich.backend.service;

import tech.chhsich.backend.entity.Administrator;
import tech.chhsich.backend.entity.ResponseMessage;

import java.util.List;

public interface AdminService {
    /**
 * Authenticates an administrator using the provided credentials.
 *
 * @param username the administrator's username
 * @param password the administrator's plaintext password to validate
 * @return a ResponseMessage describing the authentication outcome (success/failure and any relevant message)
 */
ResponseMessage login(String username, String password);

    /**
 * Update an administrator's password after verifying the current password.
 *
 * Attempts to change the password for the account identified by {@code username}
 * by validating {@code oldPassword} and setting {@code newPassword} if validation succeeds.
 *
 * @param username the administrator's username whose password will be changed
 * @param oldPassword the current password to validate ownership of the account
 * @param newPassword the new password to set if validation succeeds
 * @return a ResponseMessage describing the outcome (e.g., success, authentication failure, validation error)
 */
ResponseMessage updatePassword(String username, String oldPassword, String newPassword);

    /**
 * Retrieves all administrator accounts.
 *
 * @return a list of all Administrator entities; an empty list if no administrators exist
 */
List<Administrator> getAllMembers();

    /**
 * Deletes the administrator account identified by the given username.
 *
 * @param username the unique username of the administrator to delete
 * @return a ResponseMessage indicating whether the deletion succeeded and any relevant details
 */
ResponseMessage deleteMember(String username);

    /**
 * Retrieves the Administrator with the given username.
 *
 * @param username the administrator username to look up
 * @return the Administrator matching the username, or null if none exists
 */
Administrator getMemberByUsername(String username);
}
