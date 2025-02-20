import React, { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';

const AuthSuccess = () => {
    const navigate = useNavigate();

    useEffect(() => {
        const fetchJWT = async () => {
            try {
                const response = await axios.get('http://localhost:8080/jwt/cookie-to-header', {
                    withCredentials: true
                });

                const authHeader = response.headers['authorization'];
                if (authHeader) {
                    localStorage.setItem('Authorization', authHeader); // Bearer 토큰 전체를 저장
                    console.log('JWT 저장 성공:', authHeader);
                    navigate('/');
                } else {
                    console.error('Authorization 헤더가 없습니다.');
                    navigate('/login');
                }
            } catch (error) {
                console.error('JWT 요청 실패:', error);
                navigate('/login');
            }
        };

        fetchJWT();
    }, [navigate]);

    return <div>인증 처리 중...</div>;
};

export default AuthSuccess;
