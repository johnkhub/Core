API Security
============

Token base security
-------------------

To access the functionality of the API, the user must be authenticated vie the Auth service and the Auth *session token* must be supplied in all requests. This means that the a REST call must contain a Cookie containing the session token supplied by the Auth Service.

### Obtaining a session token ###

 **Add a link to the Auth documentation here**

### Using the session token

Add a header to your request called 'Cookie' and set it to the value you received from the Auth Service. E.g.

```
session=TWvZFtKsWxuPV0RblerXgG4gmwAXJD; Path=/; Expires=Fri, 01 May 2020 15:16:28 GMT
```


Inter-service security
----------------------
TBD
