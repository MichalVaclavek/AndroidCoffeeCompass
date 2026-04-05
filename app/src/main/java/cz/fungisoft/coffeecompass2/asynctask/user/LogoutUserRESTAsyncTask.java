package cz.fungisoft.coffeecompass2.asynctask.user;

import cz.fungisoft.coffeecompass2.activity.data.UserAccountRepository;

/**
 * Async task for new user registering REST request call
 */
public class LogoutUserRESTAsyncTask {

    private final UserAccountRepository registerRepository;

    public LogoutUserRESTAsyncTask(UserAccountRepository registerRepository) {
        super();
        this.registerRepository = registerRepository;
    }


    public void execute() {
        registerRepository.logout();
    }
}
