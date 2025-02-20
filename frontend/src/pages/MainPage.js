import React, { useState, useEffect } from "react";
import {useNavigate} from "react-router-dom";

function App() {
    // 로컬스토리지에서 Authorization 토큰 확인
    const [isLoggedIn, setIsLoggedIn] = useState(false);

    useEffect(() => {
        const token = localStorage.getItem("Authorization");
        setIsLoggedIn(!!token); // 토큰이 있으면 true, 없으면 false
    }, []);

    const getData = () => {
        const token = localStorage.getItem("Authorization");

        fetch("http://localhost:8080/my", {
            method: "GET",
            credentials: "include",
            headers: {
                Authorization: token,
            },
        })
            .then((res) => res.text())
            .then((data) => {
                alert(data);
            })
            .catch((error) => alert(error));
    };

    const handleLoginLogout = () => {
        if (isLoggedIn) {
            // 로그아웃 처리
            const token = localStorage.getItem("Authorization");

            fetch("http://localhost:8080/logout", {
                method: "POST",
                credentials: "include",
                headers: {
                    Authorization: token,
                },
            })
                .then((res) => {
                    if (res.ok) {
                        alert("로그아웃 성공");
                        localStorage.removeItem("Authorization"); // 로컬스토리지에서 토큰 제거
                        setIsLoggedIn(false); // 상태 업데이트
                    } else {
                        alert("로그아웃 실패");
                    }
                })
                .catch((error) => alert(`로그아웃 중 오류 발생: ${error}`));
        } else {
            // 로그인 페이지로 이동
            window.location.href = "/login";
        }
    };

    return (
        <div className="min-h-screen flex flex-col items-center justify-center bg-gray-50">
            <h1 className="text-2xl font-bold mb-4">Main Page</h1>

            {/* 로그인/로그아웃 버튼 */}
            <button
                onClick={handleLoginLogout}
                className="px-6 py-2 mb-4 bg-blue-500 text-white rounded-lg hover:bg-blue-600"
            >
                {isLoggedIn ? "로그아웃" : "로그인"}
            </button>

            {/* 데이터 가져오기 버튼 (로그인 상태일 때만 표시) */}
            {isLoggedIn && (
                <button
                    onClick={getData}
                    className="px-6 py-2 bg-gray-500 text-white rounded-lg hover:bg-gray-600"
                >
                    데이터 가져오기
                </button>
            )}
        </div>
    );
}

export default App;
