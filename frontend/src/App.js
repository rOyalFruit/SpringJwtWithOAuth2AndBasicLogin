import React from 'react';
import { BrowserRouter as Router, Route, Routes } from 'react-router-dom';
import './App.css';
import AuthSuccess from './components/Auth/AuthSuccess.js';
import LoginPage from "./pages/LoginPage";
import MainPage from "./pages/MainPage";




function App() {
    return (
        <Router>
            <Routes>
                <Route path="/" element={<MainPage />} />
                <Route path="/auth-success" element={<AuthSuccess />} />
                <Route path="/login" element={<LoginPage />} />
            </Routes>
        </Router>
    )
}

export default App;
