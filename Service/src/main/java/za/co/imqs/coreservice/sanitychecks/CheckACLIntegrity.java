package za.co.imqs.coreservice.sanitychecks;

public class CheckACLIntegrity implements Check {
    public void check() {
    }

    // TODO Check that the bitsets do not include bits that do not exist
    // While we can and should check this at the point of assigning the ACL to the user it may be possible to delete a permission bit?
    // then again we should probably check that condition at database level - that the bit is in use at the point of deleteion
    private void checkForInvalidPermissions() {
    }

    // Must also check that entity is in fact owned by somebody and that somebody has read access and that somebody has write access
    // what do we do when a user is deleted - we would need to reassign thing sto system?
}
