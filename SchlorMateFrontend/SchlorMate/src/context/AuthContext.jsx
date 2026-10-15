import { createContext, useContext, useEffect, useState } from "react";

const AuthContext = createContext();

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    const savedUser = localStorage.getItem("scholarmate_user");

    try {
      return savedUser ? JSON.parse(savedUser) : null;
    } catch {
      return null;
    }
  });

  useEffect(() => {
    if (user) {
      localStorage.setItem("scholarmate_user", JSON.stringify(user));
    } else {
      localStorage.removeItem("scholarmate_user");
    }
  }, [user]);

  const login = (userData, password) => {
    if (userData?.token) {
      localStorage.setItem("scholarmate_token", userData.token);
    }
    if (password && userData?.email) {
      const credentials = btoa(`${userData.email}:${password}`);
      localStorage.setItem("scholarmate_credentials", credentials);
    }
    setUser(userData);
  };

  const logout = () => {
    setUser(null);
    localStorage.removeItem("scholarmate_token");
    localStorage.removeItem("scholarmate_credentials");
    localStorage.removeItem("scholarmate_user");
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        login,
        logout,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}

// eslint-disable-next-line react-refresh/only-export-components
export function useAuth() {
  return useContext(AuthContext);
}
