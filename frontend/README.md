# React 프론트엔드

개발 중에는 Spring Boot(3020)와 Vite 개발 서버가 별도 포트를 사용합니다.

```powershell
cd frontend
npm install
npm run build
```

`npm run build`는 React 결과물을 Spring Boot의 정적 파일 경로로 생성합니다.
그 후 Spring Boot를 실행하면 프론트엔드와 API를 모두 `http://localhost:3020`에서 사용할 수 있습니다.
