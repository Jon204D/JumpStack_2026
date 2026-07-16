# JumpStack_2026
For all projects in the JumpStack July 13th 2026 to July 17th 2026

## JWT admin endpoint

Set a signing secret with at least 32 bytes before running outside local development:

```shell
export APP_JWT_SECRET="replace-with-a-long-random-secret"
```

The default token lifetime is 15 minutes. Override it with a Java duration such
as `APP_JWT_TTL=PT30M`.

### Postman test sequence

1. Obtain an admin token:

   - Method: `POST`
   - URL: `http://localhost:8080/api/auth/login`
   - Body: raw JSON

   ```json
   {
     "username": "admin",
     "password": "your-admin-password"
   }
   ```

2. Copy the `accessToken` from the response.
3. Request `GET http://localhost:8080/admin`.
4. In Authorization, choose **Bearer Token** and paste the token.

Expected results:

- `GET /public` without a token: `200 OK`
- `GET /admin` with a current admin token: `200 OK`
- `GET /admin` without a token: `403 Access Forbidden`
- `GET /admin` with an expired token: `403 Access Forbidden`
- `GET /admin` with a customer token: `403 Access Forbidden`

The existing bank API temporarily accepts either Bearer JWT or HTTP Basic
authentication so the frontend can be migrated without downtime. The
`/admin` endpoint itself is Bearer-only.
