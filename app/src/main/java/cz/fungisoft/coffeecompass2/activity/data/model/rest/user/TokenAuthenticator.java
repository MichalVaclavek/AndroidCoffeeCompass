package cz.fungisoft.coffeecompass2.activity.data.model.rest.user;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.IOException;

import cz.fungisoft.coffeecompass2.activity.interfaces.login.UserAccountActionsProvider;
import okhttp3.Authenticator;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;

public class TokenAuthenticator implements Authenticator {

    private final UserAccountActionsProvider userAccountService;

    public TokenAuthenticator(UserAccountActionsProvider userAccountService) {
        this.userAccountService = userAccountService;
    }

    @Nullable
    @Override
    public Request authenticate(Route route, Response response) throws IOException {
        final String accessToken = userAccountService.getAccessToken();
        if (!isRequestWithAccessToken(response) || accessToken == null) {
            return null;
        }
        synchronized (this) { // might be called from another thread
            final String newAccessToken = userAccountService.getAccessToken();
            // Access token is refreshed in another thread.
            if (!accessToken.equals(newAccessToken)) {
                return newRequestWithAccessToken(response.request(), newAccessToken);
            }

            // Need to refresh an access token
            JwtUserToken newToken = userAccountService.refreshTokenSync();
            if (newToken == null) {
                throw new IOException("Refreshing access token failed.");
            }
            final String updatedAccessToken = newToken.getAccessToken();
            return newRequestWithAccessToken(response.request(), updatedAccessToken);
        }
    }

    private boolean isRequestWithAccessToken(@NonNull Response response) {
        String header = response.request().header("Authorization");
        return header != null && header.startsWith("Bearer");
    }

    @NonNull
    private Request newRequestWithAccessToken(@NonNull Request request, @NonNull String accessToken) {
        return request.newBuilder()
                .header("Authorization", "Bearer " + accessToken)
                .build();
    }
}
