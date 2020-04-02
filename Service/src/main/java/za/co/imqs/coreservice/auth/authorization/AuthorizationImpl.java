package za.co.imqs.coreservice.auth.authorization;

import lombok.extern.slf4j.Slf4j;
import za.co.imqs.common.security.Permissions;
import za.co.imqs.libimqs.auth.AuthResponse;
import za.co.imqs.spring.service.auth.authorization.Authorization;
import za.co.imqs.spring.service.auth.authorization.UserContext;

/**
 * (c) 2016 IMQS Software
 * <p>
 * User: AbramS
 * Date: 2016/08/30
 *
 */
@Slf4j
public class AuthorizationImpl implements Authorization {

    // TODO  Get the users's membership to the groups associated with a specific CD or Dept for first authorisation  then do teh call to the database function to check other permissions
    public boolean authorize(AuthResponse authAuthResponse) {
        return true;
    }

    public boolean authorize(UserContext uCtx, Permissions p){
        return true;
    }

    public boolean authorize(UserContext uCtx, String eventId) {
        return true;
    }

}
