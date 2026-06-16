package com.certak.ghcpmgmt;

import com.certak.ghcpmgmt.api.GitHubClient;
import com.certak.ghcpmgmt.model.User;
import picocli.CommandLine;
import java.util.concurrent.Callable;

/**
 * GET /user — Get the authenticated user.
 *
 * API Reference: https://docs.github.com/en/rest/users/users?apiVersion=2026-03-10#get-the-authenticated-user
 */
@CommandLine.Command(name = "me",
        description = "Get the authenticated user (GET /user)",
        mixinStandardHelpOptions = true)
public class MeCommand implements Callable<Integer> {

    @Override
    public Integer call() {
        return ApiUtils.run(client -> client.get("/user", User.class), UserUtils::printUser);
    }
}
