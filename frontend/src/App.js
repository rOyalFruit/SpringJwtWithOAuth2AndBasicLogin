import React from 'react';
import { BrowserRouter as Router, Route, Routes } from 'react-router-dom';
import './App.css';
import AuthSuccess from './components/Auth/AuthSuccess.js';
import LoginPage from "./pages/LoginPage";
import MainPage from "./pages/MainPage";
import SignupPage from "./pages/SignupPage";
import FindIdPage from "./pages/FindIdPage";
import FindPasswordPage from "./pages/FindPasswordPage";

function App() {
    return (
        <Router>
            <Routes>
                <Route path="/" element={<MainPage />} />
                <Route path="/auth-success" element={<AuthSuccess />} />
                <Route path="/login" element={<LoginPage />} />
                <Route path="/signup" element={<SignupPage />} />
                <Route path="/find-id" element={<FindIdPage />} />
                <Route path="/find-password" element={<FindPasswordPage />} />
            </Routes>
        </Router>
    )
}

export default App;
