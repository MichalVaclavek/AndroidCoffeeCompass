package cz.fungisoft.coffeecompass2.activity.interfaces.interfaces.login;

import cz.fungisoft.coffeecompass2.activity.data.Result;

/**
 * Interface to define local actions to be performed after
 * remote user account actions (login, logout, register, delete)
 * are finished within REST request.
 * It is expected, that some Service class, intended to perform
 * user account related actions, is implementing such
 * interface.
 */
public interface UserAccountActionsEvaluator {

    void evaluateLoginResult(Result result);
    void evaluateRegisterResult(Result result);
    void evaluateLogoutResult(Result result);
    void evaluateDeleteResult(Result result);
}
