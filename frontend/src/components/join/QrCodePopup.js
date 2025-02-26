import React from 'react';

const QrCodePopup = ({ qrCodeImage, timer, onClose }) => (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center">
        <div className="bg-white p-8 rounded-lg max-w-md w-full">
            <div className="flex flex-col items-center">
                <h3 className="text-xl font-bold mb-4 text-center">QR 코드 인증</h3>
                <img src={qrCodeImage} alt="QR Code" className="mb-4 max-w-full h-auto" />
                <p className="text-red-500 text-center mb-4">
                    남은 시간: {Math.floor(timer / 60)}:{timer % 60 < 10 ? '0' : ''}{timer % 60}
                </p>
                <p className="mb-4 text-center">
                    1. 스마트폰으로 위 QR 코드를 스캔해주세요.<br />
                    2. QR 코드 스캔 후 나타나는 메시지를 그대로 전송해주세요.<br />
                    3. 메시지 전송 후 자동으로 인증이 완료됩니다.
                </p>
                <button
                    onClick={onClose}
                    className="w-full bg-gray-200 rounded-lg py-2 text-gray-700 hover:bg-gray-300"
                >
                    닫기
                </button>
            </div>
        </div>
    </div>
);

export default QrCodePopup;
