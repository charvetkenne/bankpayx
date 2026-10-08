import axios from "axios";
import keycloak from "../auth/keycloak";

const api = axios.create({
  baseURL: import.meta.env.VITE_CARD_SERVICE_URL || "http://localhost:8082",
  headers: {
    "Content-Type": "application/json",
  },
});

// Injecte le JWT Bearer à chaque requête
api.interceptors.request.use(
  
  async (config) => {

    console.log("=== AXIOS START ===");

    try {
      const refreshed = await keycloak.updateToken(30);

      console.log("Token refresh ?", refreshed);
      console.log("Realm Roles =", keycloak.tokenParsed?.realm_access?.roles);

    } catch (e) {
      console.error("Erreur updateToken", e);
      throw e;
    }

    config.headers.Authorization = `Bearer ${keycloak.token}`;

    console.log(
      "Authorization Header FINAL =",
      config.headers.Authorization
    );

    console.log(
      "Request URL =",
      config.baseURL + config.url
    );

    console.log("=== AXIOS END ===");

    return config;
  },
  Error => Promise.reject(Error)

  // async (config) => {
  //       console.log("=== AXIOS START ===");

  //   // Rafraîchit le token s'il expire dans moins de 30s
  //   try {
  //     console.log("Avant updateToken");

  //     const refreshed = await keycloak.updateToken(30);
  //         console.log(
  //     "Authorization Header =",
  //     config.headers.Authorization);
  //         console.log(
  //     "Realm Roles =",
  //     keycloak.tokenParsed?.realm_access?.roles
  //   );
  //     console.log("Après updateToken");
  //     console.log("Token refresh ?", refreshed);
  //   } catch (e) {
  //     // keycloak.logout();
  //     // return Promise.reject(new Error("Session expirée"));
  //       console.error("Erreur updateToken", e);

  //     //keycloak.logout();

  //     return Promise.reject(e);
  //   }
  //   console.log("Token =", keycloak.token?.substring(0, 30));
  //   config.headers.Authorization = `Bearer ${keycloak.token}`;
  //   console.log("URL =", config.baseURL + config.url);

  //   console.log("=== AXIOS END ===");
  //   return config;
  // },
  // (error) => Promise.reject(error)
);

// Intercepteur de réponse : redirige vers Keycloak sur 401
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      keycloak.logout();
    }
    return Promise.reject(error);
  }
);

export default api;
