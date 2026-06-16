package com.certak.ghcpmgmt;

import com.certak.ghcpmgmt.api.GitHubClient;
import com.certak.ghcpmgmt.model.User;
import picocli.CommandLine;
import picocli.CommandLine.Parameters;
import java.util.concurrent.Callable;

/**
 * GET /users/{username} — Get a user by username.
 *
 * API Reference: https://docs.github.com/en/enterprise-cloud@latest/rest/users/users?apiVersion=2026-03-10#get-a-user
 */
@CommandLine.Command(name = "show",
        description = "Get a user by username (GET /users/{username})",
        mixinStandardHelpOptions = true)
public class ShowUserCommand implements Callable<Integer> {

    @Parameters(arity = "1", description = "GitHub username")
    private String username;

    @Override
    public Integer call() {
        return ApiUtils.run(client -> client.get("/users/" + username, User.class), UserUtils::printUser);
    }
}
