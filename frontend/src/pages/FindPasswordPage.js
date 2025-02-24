import React, { useState } from 'react';

function FindPasswordPage() {
    const [username, setUsername] = useState("");
    const [email, setEmail] = useState("");

    const handleFindPassword = (e) => {
        e.preventDefault();
        // 비밀번호 찾기 로직 구현
        alert("비밀번호 찾기 요청 전송");
    };

    return (
        <div className="min-h-screen flex flex-col items-center justify-center bg-gray-50">
            <div className="w-full max-w-md bg-white p-8 rounded-lg shadow-md">
                <h2 className="text-3xl font-bold text-center mb-8">비밀번호 찾기</h2>
                <form className="space-y-4" onSubmit={handleFindPassword}>
                    <input
                        type="text"
                        placeholder="아이디"
                        value={username}
                        onChange={(e) => setUsername(e.target.value)}
                        className="w-full border border-gray-300 rounded-lg px-4 py-2 focus:outline-none focus:ring focus:ring-gray-200"
                    />
                    <input
                        type="email"
                        placeholder="가입시 등록한 이메일"
                        value={email}
                        onChange={(e) => setEmail(e.target.value)}
                        className="w-full border border-gray-300 rounded-lg px-4 py-2 focus:outline-none focus:ring focus:ring-gray-200"
                    />
                    <button
                        type="submit"
                        className="w-full bg-gray-200 rounded-lg py-2 text-gray-700 hover:bg-gray-300"
                    >
                        비밀번호 찾기
                    </button>
                </form>
            </div>
        </div>
    );
}

export default FindPasswordPage;
