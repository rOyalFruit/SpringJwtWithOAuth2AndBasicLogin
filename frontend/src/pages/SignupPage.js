import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';

function SignupPage() {
    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");
    const [confirmPassword, setConfirmPassword] = useState("");
    const [nickname, setNickname] = useState("");
    const [email, setEmail] = useState("");
    const [phone, setPhone] = useState("");
    const navigate = useNavigate();

    const handleSignup = (e) => {
        e.preventDefault();

        const userData = {
            username,
            password,
            confirmPassword,
            nickname,
            email,
            phone
        };

        fetch("http://localhost:8080/join", {
            method: "POST",
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(userData),
        })
            .then(response => {
                if (response.ok) {
                    alert("회원가입이 성공적으로 완료되었습니다.");
                    navigate('/login'); // 로그인 페이지로 이동
                } else {
                    return response.json().then(data => {
                        throw new Error(data.message || "회원가입 중 오류가 발생했습니다.");
                    });
                }
            })
            .catch(error => {
                alert("회원가입 중 오류가 발생했습니다: " + error.message);
            });
    };

    return (
        <div className="min-h-screen flex flex-col items-center justify-center bg-gray-50">
            <div className="w-full max-w-md bg-white p-8 rounded-lg shadow-md">
                <h2 className="text-3xl font-bold text-center mb-8">회원가입</h2>
                <form className="space-y-4" onSubmit={handleSignup}>
                    <input
                        type="text"
                        placeholder="아이디"
                        value={username}
                        onChange={(e) => setUsername(e.target.value)}
                        className="w-full border border-gray-300 rounded-lg px-4 py-2 focus:outline-none focus:ring focus:ring-gray-200"
                        required
                    />
                    <input
                        type="password"
                        placeholder="비밀번호"
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                        className="w-full border border-gray-300 rounded-lg px-4 py-2 focus:outline-none focus:ring focus:ring-gray-200"
                        required
                    />
                    <input
                        type="password"
                        placeholder="비밀번호 확인"
                        value={confirmPassword}
                        onChange={(e) => setConfirmPassword(e.target.value)}
                        className="w-full border border-gray-300 rounded-lg px-4 py-2 focus:outline-none focus:ring focus:ring-gray-200"
                        required
                    />
                    <input
                        type="text"
                        placeholder="닉네임"
                        value={nickname}
                        onChange={(e) => setNickname(e.target.value)}
                        className="w-full border border-gray-300 rounded-lg px-4 py-2 focus:outline-none focus:ring focus:ring-gray-200"
                        required
                    />
                    <input
                        type="email"
                        placeholder="이메일"
                        value={email}
                        onChange={(e) => setEmail(e.target.value)}
                        className="w-full border border-gray-300 rounded-lg px-4 py-2 focus:outline-none focus:ring focus:ring-gray-200"
                        required
                    />
                    <input
                        type="text"
                        placeholder="전화번호"
                        value={phone}
                        onChange={(e) => setPhone(e.target.value)}
                        className="w-full border border-gray-300 rounded-lg px-4 py-2 focus:outline-none focus:ring focus:ring-gray-200"
                        required
                    />
                    <button
                        type="submit"
                        className="w-full bg-gray-200 rounded-lg py-2 text-gray-700 hover:bg-gray-300"
                    >
                        회원가입
                    </button>
                </form>
            </div>
        </div>
    );
}

export default SignupPage;
