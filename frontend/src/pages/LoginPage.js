import React, { useState } from "react";
import { Link, useNavigate } from "react-router-dom";

function App() {
    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");
    const navigate = useNavigate();

    // 소셜 로그인 함수
    const onNaverLogin = () => {
        window.location.href = "http://localhost:8080/oauth2/authorization/naver";
    };

    const onGoogleLogin = () => {
        window.location.href = "http://localhost:8080/oauth2/authorization/google";
    };

    const onKakaoLogin = () => {
        window.location.href = "http://localhost:8080/oauth2/authorization/kakao";
    };

    // 로그인 요청 함수
    const handleLogin = (e) => {
        e.preventDefault(); // 폼 제출 기본 동작 방지

        const formData = new FormData();
        formData.append("username", username);
        formData.append("password", password);

        fetch("http://localhost:8080/login", {
            method: "POST",
            body: formData,
            credentials: "include",
        })
            .then((res) => {
                if (res.ok) {
                    // 응답 헤더에서 Authorization 토큰 가져오기
                    const token = res.headers.get("Authorization");
                    if (token) {
                        localStorage.setItem("Authorization", token); // 로컬 스토리지에 저장
                        alert("로그인 성공");
                        window.location.href = "/"; // 메인 페이지로 리다이렉트
                    } else {
                        alert("로그인 성공했지만 토큰이 없습니다.");
                    }
                } else {
                    alert("로그인 실패");
                }
            })
            .catch((error) => alert(`로그인 중 오류 발생: ${error}`));
    };

    return (
        <div className="min-h-screen flex flex-col items-center bg-gray-50">
            <main className="flex-grow flex flex-col items-center justify-center px-4">
                <div className="w-full max-w-md bg-white p-8 rounded-lg shadow-md">
                    <h2 className="text-3xl font-bold text-center mb-8">로그인</h2>
                    <form className="space-y-4" onSubmit={handleLogin}>
                        <input
                            type="text"
                            placeholder="아이디를 입력해주세요"
                            value={username}
                            onChange={(e) => setUsername(e.target.value)}
                            className="w-full border border-gray-300 rounded-lg px-4 py-2 focus:outline-none focus:ring focus:ring-gray-200"
                        />
                        <input
                            type="password"
                            placeholder="비밀번호를 입력해주세요"
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            className="w-full border border-gray-300 rounded-lg px-4 py-2 focus:outline-none focus:ring focus:ring-gray-200"
                        />
                        <button
                            type="submit"
                            className="w-full bg-gray-200 rounded-lg py-2 text-gray-700 hover:bg-gray-300"
                        >
                            로그인
                        </button>
                    </form>

                    <div className="mt-4 text-center">
                        <Link to="/signup" className="text-blue-500 hover:underline">
                            회원가입
                        </Link>
                        <span className="mx-2">|</span>
                        <Link to="/find-id" className="text-blue-500 hover:underline">
                            아이디 찾기
                        </Link>
                        <span className="mx-2">|</span>
                        <Link to="/find-password" className="text-blue-500 hover:underline">
                            비밀번호 찾기
                        </Link>
                    </div>

                    {/* 소셜 로그인 */}
                    <div className="mt-8 text-center">
                        <p className="text-sm text-gray-500 mb-4">소셜 로그인</p>
                        {/* 카카오톡 로그인 */}
                        <button
                            onClick={onKakaoLogin}
                            className="w-full bg-yellow-400 rounded-lg py-2 text-black font-bold hover:bg-yellow-500 mb-4"
                        >
                            KAKAO TALK LOGIN
                        </button>
                        {/* 네이버 로그인 */}
                        <button
                            onClick={onNaverLogin}
                            className="w-full bg-green-500 rounded-lg py-2 text-white font-bold hover:bg-green-600 mb-4"
                        >
                            NAVER LOGIN
                        </button>
                        {/* 구글 로그인 */}
                        <button
                            onClick={onGoogleLogin}
                            className="w-full bg-blue-500 rounded-lg py-2 text-white font-bold hover:bg-blue-600"
                        >
                            GOOGLE LOGIN
                        </button>
                    </div>
                </div>
            </main>
        </div>
    );
}

export default App;
