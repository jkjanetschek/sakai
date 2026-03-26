# Guide for (MCI-specific) DDL-Conversions

## Ratio

In production, the *sakai.properties* **auto.dll** is set to `false` and **hibernate.hbm2ddl.auto** should be set to `validate`.
This makes explicit DDL-Conversions necessary. To keep things tracked, I've included these conversions as they ultimately are part of our custom MCI Sakai Instance.

## HowTo
1. Simply add a new file following the naming pattern `<SAKAI_BASE_VERSION>_mci_<DD>.sql` which contains your runnable DDL statement.
1. Introduce a top-level comment hinting to any Issue-tracking number (SAKAIME, MCI#, ...) and feel free to add a short description.
1. When deploying to a server, manually **execute** the newly added file(s).
