import logo from './logo.svg';
import './App.css';

const onNaverLogin = () => {
  window.location.href = "http://localhost:8080/oauth2/authorization/naver"
}

const onGoogleLogin = () => {
  window.location.href = "http://localhost:8080/oauth2/authorization/google"
}

function App() {
  return (
      <>
        <button onClick={onNaverLogin}>Naver Login</button>
        <button onClick={onGoogleLogin}>Google Login</button>
      </>
  )
}

export default App;
