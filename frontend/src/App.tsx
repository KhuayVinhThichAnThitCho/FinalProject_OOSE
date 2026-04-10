import { RouterProvider } from "react-router-dom";
import { router } from "./app/router";
import { ToastProvider } from "./shared/ui";

function App() {
  return (
    <ToastProvider>
      <RouterProvider router={router} />
    </ToastProvider>
  );
}

export default App;
