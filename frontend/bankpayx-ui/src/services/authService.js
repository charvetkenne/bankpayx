import keycloak from "../auth/keycloak";

export const getCurrentUser = () => ({
  username: keycloak.tokenParsed?.preferred_username,
  firstName: keycloak.tokenParsed?.given_name,
  lastName: keycloak.tokenParsed?.family_name,
  email: keycloak.tokenParsed?.email,
  roles: keycloak.tokenParsed?.realm_access?.roles || [],
  initials: `${(keycloak.tokenParsed?.given_name || "?")[0]}${(keycloak.tokenParsed?.family_name || "?")[0]}`.toUpperCase(),
});

export const hasRole = (role) =>
  keycloak.tokenParsed?.realm_access?.roles?.includes(role) ?? false;

export const logout = () => keycloak.logout();

export const getToken = () => keycloak.token;
