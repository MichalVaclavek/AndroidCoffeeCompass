package cz.fungisoft.coffeecompass2.asynctask.user;

import cz.fungisoft.coffeecompass2.activity.data.UserAccountRepository;

/**
 * Async task for deleteUser user account
 */
public class DeleteUserRESTAsyncTask {

    private final UserAccountRepository registerRepository;

    public DeleteUserRESTAsyncTask(UserAccountRepository registerRepository) {
        super();
        this.registerRepository = registerRepository;
    }


    public void execute() {
        registerRepository.delete();
    }
}
