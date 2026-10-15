import { createContext, useContext, useEffect, useState } from "react";

const AuthContext = createContext();

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    const savedUser = localStorage.getItem("scholarmate_user");

    return savedUser ? JSON.parse(savedUser) : null;
  });

  const [accessToken, setAccessToken] = useState(() =>
    localStorage.getItem("scholarmate_access_token"),
  );

  useEffect(() => {
    if (user) {
      localStorage.setItem("scholarmate_user", JSON.stringify(user));
    } else {
      localStorage.removeItem("scholarmate_user");
    }
  }, [user]);

  const login = (authResponse) => {
    const userData = {
      id: authResponse.id,
      name: authResponse.name,
      email: authResponse.email,
      role: authResponse.role,
    };

    setUser(userData);
    setAccessToken(authResponse.token);
    localStorage.setItem("scholarmate_access_token", authResponse.token);
    if (authResponse.refreshToken) {
      localStorage.setItem("scholarmate_refresh_token", authResponse.refreshToken);
    }
  };

  const logout = () => {
    setUser(null);
    setAccessToken(null);
    localStorage.removeItem("scholarmate_access_token");
    localStorage.removeItem("scholarmate_refresh_token");
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        accessToken,
        login,
        logout,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  return useContext(AuthContext);
}
