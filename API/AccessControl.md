
To use this API you must be authenticated via Auth and have permissions to manage security on
Asset system. Either token or inter service auth.

GET assets/authorisation/users

POST assets/authorisation/users
    {
        "name"
        "user_uuid"    
    }

DELETE assets/authorisation/users/{user_uuid}

GET assets/authorisation/users

POST assets/authorisation/groups
    {
        "name"
        "group_uuid"    
    }

DELETE assets/authorisation/groups/{group_uuid}

POST assets/authorisation/groups/{group_uuid}
    {
        "user_uuid"
        "group_uuid"
    }

DELETE assets/authorisation/groups/{group_uuid}/{user_uuid}

POST assets/authorisation/entity/{entity_uuid}
    {
        "principal_uuid",
        "access_type",
        "grant_type"
    }

POST assets/authorisation/entity/{entity_uuid}/{principal_uuid}
DELETE assets/authorisation/entity/{entity_uuid}/{principal_uuid}