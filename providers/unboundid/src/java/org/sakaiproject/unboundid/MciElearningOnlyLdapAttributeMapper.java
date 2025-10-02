package org.sakaiproject.unboundid;

import com.unboundid.ldap.sdk.migrate.ldapjdk.LDAPAttribute;
import com.unboundid.ldap.sdk.migrate.ldapjdk.LDAPEntry;
/**
 * MCI-specific LDAP attribute mapper.
 * <p>
 * Overrides the default mapping to enforce e-learning access rules:
 * - Checks the LDAP attribute defined by {@link #EMPLOYEE_TYPE}.
 * - If the user does not have the required e-learning value ({@link #ELEARNING}),
 * the user's type is set to {@link #MCI_BLOCKED_ELEARNING} and their email
 * is redirected to {@link #DUMMY_EMAIL_ADDRESS}.
 * </p>
 * <p>
 * This ensures that users without the e-learning designation cannot receive
 * email notifications and are clearly marked in the system.
 * </p>
 *
 * @author Sebastian Riemer
 */
public class MciElearningOnlyLdapAttributeMapper extends SimpleLdapAttributeMapper {
	public final static String EMPLOYEE_TYPE         = "employeeType";

	public final static String ELEARNING             = "elearning";

	public final static String DUMMY_EMAIL_ADDRESS   = "blackhole@mci4me.at";

	@Override
	public void mapLdapEntryOntoUserData(LDAPEntry ldapEntry, LdapUserData userData) {
		super.mapLdapEntryOntoUserData(ldapEntry, userData);
		// Check if user has the elearning type
		if (!hasElearningType(ldapEntry)) {
			userData.getProperties().setProperty("disabled", "true"); // will be checked in UserAuthnComponent, the "orchestrator" of authentication
			userData.setEmail(DUMMY_EMAIL_ADDRESS);       // Apply email-bending
		}
	}

	/**
	 * Helper to check if LDAPEntry has the "elearning" employee type.
	 */
	private boolean hasElearningType(LDAPEntry entry) {
		LDAPAttribute attr = entry.getAttribute(EMPLOYEE_TYPE);
		if (attr == null)
			return false;
		// Check each value
		for (String value : attr.getStringValueArray()) {
			if (ELEARNING.equalsIgnoreCase(value)) {
				return true;
			}
		}
		return false;
	}

}

