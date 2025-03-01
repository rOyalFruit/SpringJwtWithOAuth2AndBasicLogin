import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import QrCodePopup from "../components/join/QrCodePopup";

function SignupPage() {
    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");
    const [confirmPassword, setConfirmPassword] = useState("");
    const [nickname, setNickname] = useState("");
    const [email, setEmail] = useState("");
    const [phone, setPhone] = useState("");
    const [qrCodeImage, setQrCodeImage] = useState("");
    const [showPopup, setShowPopup] = useState(false);
    const [isVerified, setIsVerified] = useState(false);
    const [timer, setTimer] = useState(600); // 10분 = 600초
    const navigate = useNavigate();

    const isValidPhoneNumber = (number) => {
        const regex = /^010-\d{4}-\d{4}$/;
        return regex.test(number);
    };

    const handlePhoneChange = (e) => {
        let value = e.target.value;
        value = value.replace(/[^\d-]/g, '');
        if (value.length === 3 && !value.includes('-')) value += '-';
        if (value.length === 8 && value.split('-').length === 2) value += '-';
        value = value.slice(0, 13);
        setPhone(value);

        // 전화번호가 변경되면 인증 상태 초기화
        if (isVerified) {
            setIsVerified(false);
        }
    };

    const requestVerification = () => {
        if (!isValidPhoneNumber(phone)) {
            alert("올바른 전화번호 형식(010-1234-5678)으로 입력해주세요.");
            return;
        }

        const requestData = {
            phone: phone
        };

        fetch("http://localhost:8080/verification/qr", {
            method: "POST",
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(requestData)
        })
            .then(response => {
                if (response.ok) {
                    return response.blob();
                } else {
                    throw new Error("QR 코드 요청 중 오류가 발생했습니다.");
                }
            })
            .then(blob => {
                const qrCodeUrl = URL.createObjectURL(blob);
                setQrCodeImage(qrCodeUrl);
                setShowPopup(true);
                setTimer(600); // 타이머 초기화
            })
            .catch(error => {
                alert("QR 코드 요청 중 오류가 발생했습니다: " + error.message);
            });
    };

    const handleSignup = (e) => {
        e.preventDefault();

        if (!isVerified) {
            alert("전화번호 인증을 완료해주세요.");
            return;
        }

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
                    navigate('/login');
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

    // 팝업 닫기 핸들러
    const handlePopupClose = (verified) => {
        setShowPopup(false);

        // 인증 상태 업데이트
        if (verified) {
            setIsVerified(true);
            console.log("전화번호 인증 완료:", phone);
        }
    };

    useEffect(() => {
        let interval;
        if (showPopup && timer > 0) {
            interval = setInterval(() => {
                setTimer((prevTimer) => prevTimer - 1);
            }, 1000);
        } else if (timer === 0) {
            setShowPopup(false);
        }
        return () => clearInterval(interval);
    }, [showPopup, timer]);

    return (
        <div className="min-h-screen flex flex-col items-center justify-center bg-gray-50">
            <div className="w-full max-w-md bg-white p-8 rounded-lg shadow-md">
                <h2 className="text-3xl font-bold text-center mb-8">회원가입</h2>
                <form className="space-y-4" onSubmit={handleSignup}>
                    <input
                        type="text"
                        placeholder="사용자 이름"
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
                    <div className="space-y-2">
                        <div className="flex space-x-2">
                            <input
                                type="text"
                                placeholder="전화번호 (010-1234-5678)"
                                value={phone}
                                onChange={handlePhoneChange}
                                className="flex-1 border border-gray-300 rounded-lg px-4 py-2 focus:outline-none focus:ring focus:ring-gray-200"
                                required
                            />
                            <button
                                type="button"
                                onClick={requestVerification}
                                disabled={isVerified}
                                className={`px-4 py-2 rounded-lg ${
                                    isVerified
                                        ? "bg-green-200 text-green-800"
                                        : "bg-gray-200 text-gray-700 hover:bg-gray-300"
                                }`}
                            >
                                {isVerified ? "인증완료" : "인증하기"}
                            </button>
                        </div>

                        {/* 인증 상태 표시 */}
                        {isVerified && (
                            <div className="p-2 bg-green-100 text-green-800 rounded-lg text-sm text-center">
                                전화번호 인증이 완료되었습니다.
                            </div>
                        )}
                    </div>

                    <button
                        type="submit"
                        className="w-full bg-gray-200 rounded-lg py-2 text-gray-700 hover:bg-gray-300"
                    >
                        회원가입
                    </button>
                </form>
            </div>
            {showPopup && (
                <QrCodePopup
                    qrCodeImage={qrCodeImage}
                    timer={timer}
                    onClose={handlePopupClose}
                    phoneNumber={phone}
                />
            )}
        </div>
    );
}

export default SignupPage;