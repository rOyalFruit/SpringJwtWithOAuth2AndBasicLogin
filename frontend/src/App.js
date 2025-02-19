import React from 'react';
import { BrowserRouter as Router, Route, Routes } from 'react-router-dom';
import './App.css';
import AuthSuccess from './components/AuthSuccess.js';

const onNaverLogin = () => {
    window.location.href = "http://localhost:8080/oauth2/authorization/naver"
}

const onGoogleLogin = () => {
    window.location.href = "http://localhost:8080/oauth2/authorization/google"
}

const onKakaoLogin = () => {
    window.location.href = "http://localhost:8080/oauth2/authorization/kakao"
}

const getData = () => {
    fetch("http://localhost:8080/my", {
        method: "GET",
        credentials: "include"
    })
        .then((res) => res.text())
        .then((data) => {
            alert(data)
        })
        .catch(error => alert(error))
}

function App() {
    return (
        <Router>
            <Routes>
                <Route path="/" element={
                    <>
                        <button onClick={onNaverLogin}>Naver Login</button>
                        <button onClick={onGoogleLogin}>Google Login</button>
                        <button onClick={onKakaoLogin}>Kakao Login</button>
                        <button onClick={getData}>Get Data</button>
                    </>
                } />
                <Route path="/auth-success" element={<AuthSuccess />} />
            </Routes>
        </Router>
    )
}

export default App;
