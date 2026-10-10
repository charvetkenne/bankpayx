import { useEffect } from "react";
import AuthProvider from "./auth/AuthProvider";
import AppRouter from "./router/AppRouter";
import globalStyle, { injectFonts } from "./styles/globalStyle";

function App() {
  useEffect(() => {
    injectFonts();
  }, []);

  return (
    <AuthProvider>
      <div className="bpx-root">
        <style>{globalStyle}</style>
        <AppRouter />
      </div>
    </AuthProvider>
  );
}

export default App;
