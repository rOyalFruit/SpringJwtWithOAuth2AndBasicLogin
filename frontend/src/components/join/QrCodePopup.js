import React, { useEffect, useRef, useState } from 'react';

const QrCodePopup = ({ qrCodeImage, timer, onClose, phoneNumber }) => {
    // SSE 연결을 위한 ref 생성 (컴포넌트 리렌더링 시에도 유지)
    const eventSourceRef = useRef(null);
    // 연결 상태 추적
    const [connectionStatus, setConnectionStatus] = useState('connecting');
    // 타이머 ID 저장
    const timerIdRef = useRef(null);

    // SSE 연결 종료 함수
    const closeEventSource = () => {
        if (eventSourceRef.current) {
            eventSourceRef.current.close();
            eventSourceRef.current = null;
        }
    };

    // SSE 연결 설정 함수
    const setupEventSource = () => {
        // 이미 연결이 있으면 닫기
        closeEventSource();

        try {
            // SSE 연결 생성 (전화번호 형식 유지: 010-1111-1111)
            const eventSource = new EventSource(`http://localhost:8080/verification/subscribe/${phoneNumber}`);
            eventSourceRef.current = eventSource;

            console.log('SSE 연결 시작:', phoneNumber);
            setConnectionStatus('connecting');

            // 연결 성공 이벤트
            eventSource.addEventListener('connect', (event) => {
                console.log('SSE 연결 성공:', event.data);
                setConnectionStatus('connected');
            });

            // 인증 완료 이벤트
            eventSource.addEventListener('verificationComplete', (event) => {
                console.log('인증 완료 이벤트 수신:', event.data);
                setConnectionStatus('verified');

                // 연결 종료 후 인증 성공 상태로 팝업 닫기
                closeEventSource();

                // 약간의 지연 후 알림 표시 (UI 업데이트 후)
                setTimeout(() => {
                    alert('전화번호 인증이 완료되었습니다.');
                    onClose(true);
                }, 100);
            });

            // 에러 처리
            eventSource.onerror = (error) => {
                console.error('SSE 연결 오류:', error);
                setConnectionStatus('error');

                // 연결 오류 시 재연결 시도하지 않음
                closeEventSource();

                // 5초 후 자동 재연결 시도
                timerIdRef.current = setTimeout(() => {
                    console.log('SSE 연결 재시도...');
                    setConnectionStatus('connecting');
                    setupEventSource();
                }, 5000);
            };
        } catch (error) {
            console.error('SSE 연결 생성 중 오류:', error);
            setConnectionStatus('error');
        }
    };

    // 컴포넌트 마운트 시 한 번만 실행
    useEffect(() => {
        setupEventSource();

        // 컴포넌트 언마운트 시 정리
        return () => {
            closeEventSource();
            if (timerIdRef.current) {
                clearTimeout(timerIdRef.current);
            }
        };
    }, []); // 빈 의존성 배열로 마운트 시 한 번만 실행

    // 타이머 표시 형식 변환 함수
    const formatTime = (seconds) => {
        const mins = Math.floor(seconds / 60);
        const secs = seconds % 60;
        return `${mins}:${secs < 10 ? '0' : ''}${secs}`;
    };

    return (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
            <div className="bg-white p-8 rounded-lg max-w-md w-full">
                <div className="flex flex-col items-center">
                    <h3 className="text-xl font-bold mb-4 text-center">QR 코드 인증</h3>
                    <img src={qrCodeImage} alt="QR Code" className="mb-4 max-w-full h-auto" />
                    <p className="text-red-500 text-center mb-4">
                        남은 시간: {formatTime(timer)}
                    </p>

                    {/* 연결 상태 표시 */}
                    <div className={`text-sm mb-3 px-3 py-1 rounded-full ${
                        connectionStatus === 'connected' ? 'bg-blue-100 text-blue-800' :
                            connectionStatus === 'verified' ? 'bg-green-100 text-green-800' :
                                connectionStatus === 'error' ? 'bg-red-100 text-red-800' :
                                    'bg-gray-100 text-gray-800'
                    }`}>
                        {connectionStatus === 'connecting' && '연결 중...'}
                        {connectionStatus === 'connected' && '연결됨 - 인증 대기 중'}
                        {connectionStatus === 'verified' && '인증 완료'}
                        {connectionStatus === 'error' && '연결 오류'}
                    </div>

                    <p className="mb-4 text-center">
                        1. 스마트폰으로 위 QR 코드를 스캔해주세요.<br />
                        2. QR 코드 스캔 후 나타나는 메시지를 그대로 전송해주세요.<br />
                        3. 메시지 전송 후 자동으로 인증이 완료됩니다.
                    </p>
                    <button
                        onClick={() => {
                            // 연결 종료 후 인증 실패 상태로 팝업 닫기
                            closeEventSource();
                            onClose(false);
                        }}
                        className="w-full bg-gray-200 rounded-lg py-2 text-gray-700 hover:bg-gray-300"
                    >
                        닫기
                    </button>
                </div>
            </div>
        </div>
    );
};

export default QrCodePopup;